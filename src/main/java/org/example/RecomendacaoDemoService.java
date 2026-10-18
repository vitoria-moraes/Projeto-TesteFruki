package org.example;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Resposta de demonstração para o pitch.
 *
 * <p>CNPJs cadastrados em {@link DemoCnpjDirectory} recebem uma recomendação
 * contextual: 3 grupos com 3 opções cada, quantidade sugerida e o motivo
 * (clima + eventos simulados + perfil do estabelecimento). Qualquer outro CNPJ
 * recebe a seleção fixa de 3 produtos, que é o comportamento pedido pelo PO.</p>
 *
 * <p>O catálogo completo vai junto na resposta para o chat conseguir montar a
 * opção "ver catálogo inteiro" sem depender da recomendação. A resposta também
 * inclui os campos que a UI nova (login/cadastro) espera — porte, resumo do
 * contexto e quantidade em caixas.</p>
 */
@Service
public class RecomendacaoDemoService {

    public static final long ID_CLIENTE_DEMO = 149062L;

    private final ClimaService climaService;
    private final CatalogoService catalogoService;
    private final DemoCnpjDirectory directory;

    public RecomendacaoDemoService(ClimaService climaService,
                                   CatalogoService catalogoService,
                                   DemoCnpjDirectory directory) {
        this.climaService = climaService;
        this.catalogoService = catalogoService;
        this.directory = directory;
    }

    public Map<String, Object> recomendarPorCnpj(String cnpj) {
        return recomendarPorCnpj(cnpj, null, null);
    }

    /**
     * @param temperatura   opcional: força a temperatura (cenários do chat)
     * @param diasFeriado   opcional: força um feriado daqui a N dias (demonstração)
     */
    public Map<String, Object> recomendarPorCnpj(String cnpj, Double temperatura, Integer diasFeriado) {
        Optional<DemoCnpjDirectory.PerfilDemo> perfil = directory.buscar(cnpj);
        return perfil
                .map(p -> montarPerfil(cnpj, p, temperatura, diasFeriado))
                .orElseGet(() -> montarPadrao(cnpj, temperatura));
    }

    private Map<String, Object> montarPerfil(String cnpj, DemoCnpjDirectory.PerfilDemo perfil,
                                             Double temperatura, Integer diasFeriado) {
        ClimaService.Clima clima = temperatura != null
                ? new ClimaService.Clima(temperatura, "simulado (demonstração)",
                        perfil.cidade(), "simulado")
                : climaService.paraCoordenada(perfil.latitude(), perfil.longitude(), perfil.cidade());

        List<Map<String, Object>> grupos = new ArrayList<>();
        List<Map<String, Object>> recomendados = new ArrayList<>();

        int indiceGrupo = 0;
        String fraseClima = fraseClima(clima);
        for (DemoCnpjDirectory.GrupoDemo grupo : perfil.grupos()) {
            List<Map<String, Object>> itens = new ArrayList<>();
            String motivoGrupo = (indiceGrupo == 0 ? fraseClima + " " : "") + grupo.motivo();

            for (int i = 0; i < grupo.codigos().size(); i++) {
                String codigo = grupo.codigos().get(i);
                Optional<CatalogoService.ProdutoDemo> produto = catalogoService.porCodigo(codigo);
                if (produto.isEmpty()) continue;

                Map<String, Object> item = itemRecomendado(
                        produto.get(), grupo.titulo(), motivoGrupo,
                        grupo.quantidadeSugerida(), 96 - (indiceGrupo * 3) - i);
                itens.add(item);
                recomendados.add(item);
            }

            if (!itens.isEmpty()) {
                Map<String, Object> grupoMap = new LinkedHashMap<>();
                grupoMap.put("titulo", grupo.titulo());
                grupoMap.put("motivo", motivoGrupo);
                grupoMap.put("quantidadeSugerida", grupo.quantidadeSugerida());
                grupoMap.put("itens", itens);
                grupos.add(grupoMap);
            }
            indiceGrupo++;
        }

        String resumo = montarResumo(clima, perfil.cidade(), perfil.eventos(), grupos.size(), diasFeriado);
        return montarResposta(cnpj, perfil.idCliente(), perfil.codCliente(), perfil.nome(),
                perfil.perfilMercado(), clima, perfil.eventos(), "mock-perfil",
                resumo, grupos, recomendados);
    }

    private Map<String, Object> montarPadrao(String cnpj, Double temperatura) {
        ClimaService.Clima clima = temperatura != null
                ? new ClimaService.Clima(temperatura, "simulado (demonstração)", "Erechim", "simulado")
                : climaService.paraDemo();
        String motivo = "Seleção rápida do pitch: 3 produtos fixos, sem contexto de evento.";

        List<Map<String, Object>> itens = new ArrayList<>();
        List<String> codigos = List.of("AGUA-500", "REFRI-GUARANA-2L", "ENERG-ORIGINAL-473");
        for (int i = 0; i < codigos.size(); i++) {
            Optional<CatalogoService.ProdutoDemo> produto = catalogoService.porCodigo(codigos.get(i));
            if (produto.isPresent()) {
                itens.add(itemRecomendado(produto.get(), "Seleção rápida", motivo, 3, 92 - i));
            }
        }

        Map<String, Object> grupo = new LinkedHashMap<>();
        grupo.put("titulo", "Seleção rápida");
        grupo.put("motivo", motivo);
        grupo.put("quantidadeSugerida", 3);
        grupo.put("itens", itens);

        String resumo = "Seleção fixa do pitch: 3 produtos. "
                + "Use um CNPJ da lista de demonstração para ver a recomendação contextual por cidade e evento.";

        return montarResposta(cnpj, ID_CLIENTE_DEMO, 239516L, "Cliente Demo — Erechim/RS",
                "Mercadinho", clima, List.of(), "mock-padrao",
                resumo, List.of(grupo), itens);
    }

    private Map<String, Object> itemRecomendado(CatalogoService.ProdutoDemo produto,
                                                 String grupo, String motivo,
                                                 int quantidade, int score) {
        Map<String, Object> item = catalogoService.paraMap(produto);
        item.put("quantidadeSugerida", quantidade);
        item.put("qtdSugeridaCaixas", quantidade);
        item.put("grupo", grupo);
        item.put("motivo", motivo);
        item.put("motivos", List.of(motivo));
        item.put("score", score);
        item.put("justificativaVolume", "Volume sugerido para a demonstração do pitch");
        return item;
    }

    private String fraseClima(ClimaService.Clima clima) {
        String temperatura = String.format(Locale.forLanguageTag("pt-BR"), "%.1f", clima.temperatura());
        if (clima.temperatura() >= 28) {
            return "Calor de " + temperatura + " °C.";
        }
        if (clima.temperatura() <= 12) {
            return "Frio de " + temperatura + " °C.";
        }
        return "Tempo ameno (" + temperatura + " °C).";
    }

    private String montarResumo(ClimaService.Clima clima, String cidade,
                                List<String> eventos, int totalGrupos, Integer diasFeriado) {
        String temperatura = String.format(Locale.forLanguageTag("pt-BR"), "%.1f", clima.temperatura());
        StringBuilder resumo = new StringBuilder();
        resumo.append("Contexto: ").append(temperatura).append(" °C em ").append(cidade).append(".");
        if (clima.temperatura() >= 28) {
            resumo.append(" Calor: água e refrigerante ganham peso.");
        } else if (clima.temperatura() <= 12) {
            resumo.append(" Frio: o foco é o evento e o jogo.");
        } else {
            resumo.append(" Tempo ameno: foco no evento e no jogo.");
        }
        if (!eventos.isEmpty()) {
            resumo.append(" ").append(String.join("; ", eventos)).append(".");
        }
        if (diasFeriado != null) {
            resumo.append(" Feriado simulado em ").append(diasFeriado).append(" dia(s).");
        }
        resumo.append(" Montei ").append(totalGrupos)
                .append(" grupos com 3 opções cada; a quantidade sugerida aparece em cada item.");
        return resumo.toString();
    }

    private Map<String, Object> montarResposta(String cnpj, long idCliente, long codCliente,
                                               String nomeCliente, String perfilMercado,
                                               ClimaService.Clima clima, List<String> eventos,
                                               String modo, String resumo,
                                               List<Map<String, Object>> grupos,
                                               List<Map<String, Object>> recomendados) {
        String cidade = "—".equals(clima.cidade()) ? "Erechim" : clima.cidade();
        String porte = porteDoPerfil(perfilMercado);
        String porteLabel = rotuloPorte(porte);
        String temperatura = String.format(Locale.forLanguageTag("pt-BR"), "%.1f", clima.temperatura());

        Map<String, Object> contexto = new LinkedHashMap<>();
        contexto.put("temperatura", clima.temperatura());
        contexto.put("descricao", clima.descricao());
        contexto.put("cidade", cidade);
        contexto.put("fonte", clima.fonte());
        contexto.put("eventos", eventos);
        contexto.put("modo", modo);
        contexto.put("dadosFicticios", true);
        contexto.put("resumo", perfilMercado + ", " + cidade + ", " + temperatura + " °C");
        contexto.put("estacao", estacaoAtual());
        contexto.put("fimDeSemana", false);

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("cnpj", cnpj);
        resposta.put("idCliente", idCliente);
        resposta.put("codCliente", codCliente);
        resposta.put("nomeCliente", nomeCliente);
        resposta.put("perfilMercado", perfilMercado);
        resposta.put("porte", porte);
        resposta.put("porteLabel", porteLabel);
        resposta.put("novoCliente", false);
        resposta.put("modo", modo);
        resposta.put("contexto", contexto);
        resposta.put("resumoIa", resumo);
        resposta.put("gruposRecomendacao", grupos);
        resposta.put("produtosRecomendados", recomendados);
        resposta.put("catalogoCompleto", catalogoService.listar().stream()
                .map(catalogoService::paraMap).toList());
        resposta.put("aviso", "Dados de demonstração: eventos simulados; clima via OpenWeatherMap "
                + "com fallback local. O catálogo tem 31 SKUs comerciais representativos.");
        return resposta;
    }

    private String porteDoPerfil(String perfilMercado) {
        return switch (perfilMercado) {
            case "Supermercado" -> "GRANDE_DISTRIBUIDOR";
            case "Adega", "Restaurante" -> "BAR_RESTAURANTE";
            default -> "MINIMERCADO";
        };
    }

    private String rotuloPorte(String porte) {
        return switch (porte) {
            case "GRANDE_DISTRIBUIDOR" -> "Grande Distribuidor";
            case "BAR_RESTAURANTE" -> "Bar / Restaurante";
            default -> "Minimercado";
        };
    }

    private String estacaoAtual() {
        LocalDate hoje = LocalDate.now();
        int mmdd = hoje.getMonthValue() * 100 + hoje.getDayOfMonth();
        if (mmdd >= 1221 || mmdd < 321) return "Verão";
        if (mmdd < 621) return "Outono";
        if (mmdd < 923) return "Inverno";
        return "Primavera";
    }
}
