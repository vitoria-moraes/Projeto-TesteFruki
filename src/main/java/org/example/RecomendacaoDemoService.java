package org.example;

import org.springframework.stereotype.Service;

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
 * opção "ver catálogo inteiro" sem depender da recomendação.</p>
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
        Optional<DemoCnpjDirectory.PerfilDemo> perfil = directory.buscar(cnpj);
        return perfil.map(p -> montarPerfil(cnpj, p)).orElseGet(() -> montarPadrao(cnpj));
    }

    private Map<String, Object> montarPerfil(String cnpj, DemoCnpjDirectory.PerfilDemo perfil) {
        ClimaService.Clima clima = climaService.paraCoordenada(
                perfil.latitude(), perfil.longitude(), perfil.cidade());

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

        String resumo = montarResumo(clima, perfil.cidade(), perfil.eventos(), grupos.size());
        return montarResposta(cnpj, perfil.idCliente(), perfil.codCliente(), perfil.nome(),
                perfil.perfilMercado(), clima, perfil.eventos(), "mock-perfil",
                resumo, grupos, recomendados);
    }

    private Map<String, Object> montarPadrao(String cnpj) {
        ClimaService.Clima clima = climaService.paraDemo();
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
        item.put("grupo", grupo);
        item.put("motivo", motivo);
        item.put("motivos", List.of(motivo));
        item.put("score", score);
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
                                List<String> eventos, int totalGrupos) {
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

        Map<String, Object> contexto = new LinkedHashMap<>();
        contexto.put("temperatura", clima.temperatura());
        contexto.put("descricao", clima.descricao());
        contexto.put("cidade", cidade);
        contexto.put("fonte", clima.fonte());
        contexto.put("eventos", eventos);
        contexto.put("modo", modo);
        contexto.put("dadosFicticios", true);

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("cnpj", cnpj);
        resposta.put("idCliente", idCliente);
        resposta.put("codCliente", codCliente);
        resposta.put("nomeCliente", nomeCliente);
        resposta.put("perfilMercado", perfilMercado);
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
}
