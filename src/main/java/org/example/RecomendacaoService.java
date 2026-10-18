package org.example;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Motor de recomendação por regras e pontuação (scoring).
 *
 * score = 35% histórico + 30% perfil do mercado + 25% fatores externos + 10% descoberta de portfólio
 */
@Service
public class RecomendacaoService {

    private static final double PESO_HISTORICO = 0.35;
    private static final double PESO_PERFIL = 0.30;
    private static final double PESO_EXTERNO = 0.25;
    private static final double PESO_DESCOBERTA = 0.10;

    private static final int TOTAL_RECOMENDADOS = 4;
    private static final double TEMPERATURA_PADRAO = 30.0;
    private static final int JANELA_FERIADO_DIAS = 21;

    private static final String REFRI = "REFRIGERANTE";
    private static final String SUCO = "SUCO";
    private static final String AGUA = "AGUA";
    private static final String CERVEJA = "CERVEJA";
    private static final String ENERGETICO = "ENERGETICO";

    private static final String[] PERFIS = {"Padaria", "Mercadinho", "Supermercado", "Restaurante", "Adega"};

    private static final Map<String, Map<String, Double>> AFINIDADE_PERFIL = Map.of(
            "Padaria", afinidade(0.6, 0.9, 0.6, 0.1, 0.3),
            "Mercadinho", afinidade(0.9, 0.5, 0.6, 0.6, 0.5),
            "Supermercado", afinidade(1.0, 0.7, 0.7, 0.8, 0.6),
            "Restaurante", afinidade(0.8, 0.7, 0.8, 0.7, 0.2),
            "Adega", afinidade(0.5, 0.2, 0.5, 1.0, 0.7)
    );

    private static final Map<LocalDate, String> FERIADOS = new TreeMap<>(Map.of(
            LocalDate.of(2026, 11, 2), "Finados",
            LocalDate.of(2026, 11, 15), "Proclamação da República",
            LocalDate.of(2026, 11, 20), "Consciência Negra",
            LocalDate.of(2026, 12, 25), "Natal",
            LocalDate.of(2027, 1, 1), "Ano Novo"
    ));

    private static final Map<String, Double> PRECOS = Map.of(
            "Fruki Cola 2L", 7.80,
            "Fruki Laranja 2L", 7.20,
            "Fruki Uva 2L", 7.20,
            "Fruki Guaraná 2L", 7.50,
            "Energético Traz Elev 473ml", 6.90,
            "Água da Pedra 500ml", 2.00,
            "Cerveja Bellavista Puro Malte", 5.50,
            "Suco Frut Fresh 1L", 4.80
    );

    private final ExcelReaderService excelReaderService;

    private List<Cliente> clientesCache;
    private List<ItemCatalogo> catalogoCache;

    public RecomendacaoService(ExcelReaderService excelReaderService) {
        this.excelReaderService = excelReaderService;
    }

    private record ItemCatalogo(String codigo, String nome, String categoria, double preco, String imagem) {}

    private record Contexto(double temperatura, String feriado, Integer diasParaFeriado) {}

    public Map<String, Object> buscarDadosCompletos(Long codCliente) {
        return buscarDadosCompletos(codCliente, null, null);
    }

    /**
     * Busca o cliente pelo código ERP ou pelo id interno, sem criar cliente fictício.
     * Usado pelo controller para distinguir "existe" de "não existe" (404 real).
     */
    public Cliente buscarCliente(Long codCliente) {
        if (codCliente == null) return null;
        return carregarClientes().stream()
                .filter(Objects::nonNull)
                .filter(c -> codCliente.equals(c.getCodCliente()) || codCliente.equals(c.getIdCliente()))
                .findFirst()
                .orElse(null);
    }

    public Map<String, Object> buscarDadosCompletos(Long codCliente, Double temperaturaSimulada, Integer diasFeriadoSimulado) {
        List<Cliente> clientes = new ArrayList<>();
        try {
            clientes = carregarClientes();
        } catch (Exception e) {
            // Ignora falhas de leitura do CSV em runtime
        }

        Cliente cliente = null;
        if (clientes != null && !clientes.isEmpty()) {
            cliente = clientes.stream()
                    .filter(c -> c != null && (
                            (c.getCodCliente() != null && c.getCodCliente().equals(codCliente)) ||
                                    (c.getIdCliente() != null && c.getIdCliente().equals(codCliente))))
                    .findFirst()
                    .orElse(null);
        }

        // Garante cliente padrão para nunca falhar o fluxo de conversação
        if (cliente == null) {
            cliente = new Cliente();
            cliente.setCodCliente(codCliente != null ? codCliente : 110569L);
            cliente.setIdCliente(codCliente != null ? codCliente : 110569L);
            cliente.setNome("Estabelecimento Parceiro #" + (codCliente != null ? codCliente : "110569"));
            cliente.setCodSegmento("23");
        }

        List<ItemCatalogo> catalogo = carregarCatalogo();
        String perfil = perfilDoMercado(cliente);
        Contexto contexto = montarContexto(temperaturaSimulada, diasFeriadoSimulado);

        Random rnd = new Random(codCliente != null ? codCliente : 1L);

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (ItemCatalogo item : catalogo) {
            boolean comprou = rnd.nextDouble() > 0.35;
            int qtd90dias = comprou ? 1 + rnd.nextInt(12) : 0;
            int diasUltimaCompra = 2 + rnd.nextInt(40);

            List<String> motivos = new ArrayList<>();

            double histScore = Math.min(1.0, qtd90dias / 10.0);
            if (qtd90dias > 0 && diasUltimaCompra >= 14) {
                histScore = Math.min(1.0, histScore + 0.2);
                motivos.add("Já faz " + diasUltimaCompra + " dias desde o último pedido deste item");
            } else if (qtd90dias >= 6) {
                motivos.add("Item de alta saída no seu histórico");
            }

            double perfilScore = AFINIDADE_PERFIL.get(perfil).getOrDefault(item.categoria(), 0.5);
            if (perfilScore >= 0.8) {
                motivos.add("Forte saída em " + perfil.toLowerCase());
            }

            double externoScore = scoreClima(item.categoria(), contexto.temperatura());
            if (contexto.temperatura() >= 28 && externoScore >= 0.85) {
                motivos.add("Calor de " + Math.round(contexto.temperatura()) + "°C aumenta a procura");
            }
            if (contexto.diasParaFeriado() != null && (item.categoria().equals(CERVEJA) || item.categoria().equals(REFRI))) {
                externoScore = Math.min(1.0, externoScore + 0.3);
                motivos.add(contexto.feriado() + " em " + contexto.diasParaFeriado() + " dias");
            }

            double descobertaScore = qtd90dias == 0 ? 1.0 : 0.0;
            if (qtd90dias == 0) {
                motivos.add("Novidade no seu mix: você ainda não compra este item");
            }

            double score = (PESO_HISTORICO * histScore)
                    + (PESO_PERFIL * perfilScore)
                    + (PESO_EXTERNO * externoScore)
                    + (PESO_DESCOBERTA * descobertaScore);

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("codigo", item.codigo());
            map.put("descricao", item.nome());
            map.put("categoria", item.categoria());
            map.put("preco", item.preco());
            map.put("imagemUrl", item.imagem());
            map.put("score", Math.round(score * 100));
            map.put("motivos", motivos);
            map.put("motivo", motivos.stream().limit(2).collect(Collectors.joining(" • ")));
            ranking.add(map);
        }

        ranking.sort((a, b) -> Long.compare((Long) b.get("score"), (Long) a.get("score")));

        List<Map<String, Object>> recomendados = ranking.stream().limit(TOTAL_RECOMENDADOS).collect(Collectors.toList());

        Map<String, Object> contextoMap = new LinkedHashMap<>();
        contextoMap.put("temperatura", contexto.temperatura());
        contextoMap.put("feriado", contexto.feriado());
        contextoMap.put("diasParaFeriado", contexto.diasParaFeriado());
        contextoMap.put("dadosFicticios", true);

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("codCliente", cliente.getCodCliente() != null ? cliente.getCodCliente() : codCliente);
        resposta.put("nomeCliente", cliente.getNome() != null && !cliente.getNome().isEmpty() ? cliente.getNome() : "Estabelecimento Parceiro");
        resposta.put("perfilMercado", perfil);
        resposta.put("contexto", contextoMap);
        resposta.put("produtosRecomendados", recomendados);
        resposta.put("catalogoCompleto", catalogo.stream().map(this::itemParaMap).collect(Collectors.toList()));
        return resposta;
    }

    private double scoreClima(String categoria, double temperatura) {
        if (temperatura >= 28) {
            return switch (categoria) {
                case AGUA -> 1.0;
                case REFRI -> 0.9;
                case CERVEJA -> 0.85;
                case SUCO -> 0.8;
                default -> 0.6;
            };
        }
        if (temperatura <= 16) {
            return switch (categoria) {
                case AGUA -> 0.3;
                case SUCO -> 0.6;
                default -> 0.5;
            };
        }
        return 0.5;
    }

    private String perfilDoMercado(Cliente cliente) {
        String seg = cliente.getCodSegmento() != null ? cliente.getCodSegmento() : "23";
        return PERFIS[Math.abs(seg.hashCode()) % PERFIS.length];
    }

    private Contexto montarContexto(Double temperaturaSimulada, Integer diasFeriadoSimulado) {
        double temp = temperaturaSimulada != null ? temperaturaSimulada : TEMPERATURA_PADRAO;

        if (diasFeriadoSimulado != null) {
            return new Contexto(temp, "Feriado", diasFeriadoSimulado);
        }

        LocalDate hoje = LocalDate.now();
        for (Map.Entry<LocalDate, String> f : FERIADOS.entrySet()) {
            long dias = ChronoUnit.DAYS.between(hoje, f.getKey());
            if (dias >= 0 && dias <= JANELA_FERIADO_DIAS) {
                return new Contexto(temp, f.getValue(), (int) dias);
            }
        }
        return new Contexto(temp, null, null);
    }

    private synchronized List<Cliente> carregarClientes() {
        if (clientesCache == null) {
            clientesCache = excelReaderService.lerClientesDoCsv();
        }
        return clientesCache;
    }

    private synchronized List<ItemCatalogo> carregarCatalogo() {
        if (catalogoCache != null) return catalogoCache;

        Map<String, ItemCatalogo> porNome = new LinkedHashMap<>();
        for (Produto p : excelReaderService.lerProdutosDoCsv()) {
            String nome = obterNomeComercialReal(p);
            if (nome.equals("Bebida Fruki B2B")) continue;
            porNome.putIfAbsent(nome, new ItemCatalogo(
                    p.getCodItem(), nome, categoriaDoNome(nome),
                    PRECOS.getOrDefault(nome, 7.50), obterCaminhoImagem(p)));
        }

        if (porNome.size() < TOTAL_RECOMENDADOS) {
            porNome.putIfAbsent("Fruki Guaraná 2L", new ItemCatalogo("901", "Fruki Guaraná 2L", REFRI, 7.50, "/images/Refri fruki/guarana1-5l.webp"));
            porNome.putIfAbsent("Fruki Cola 2L", new ItemCatalogo("902", "Fruki Cola 2L", REFRI, 7.80, "/images/Refri fruki/cola-2l.webp"));
            porNome.putIfAbsent("Água da Pedra 500ml", new ItemCatalogo("903", "Água da Pedra 500ml", AGUA, 2.00, "/images/Agua fruki/fruki-500ml-agua-s-gas-gotas.webp"));
            porNome.putIfAbsent("Energético Traz Elev 473ml", new ItemCatalogo("904", "Energético Traz Elev 473ml", ENERGETICO, 6.90, "/images/Energetico fruki/eleve_473_original.webp"));
            porNome.putIfAbsent("Cerveja Bellavista Puro Malte", new ItemCatalogo("905", "Cerveja Bellavista Puro Malte", CERVEJA, 5.50, "/images/Cervejas fruki/bellavista-premium.webp"));
            porNome.putIfAbsent("Suco Frut Fresh 1L", new ItemCatalogo("906", "Suco Frut Fresh 1L", SUCO, 4.80, "/images/Sucos fruki/_suco_tetrapack_1l_frente_laranja.webp"));
        }

        catalogoCache = new ArrayList<>(porNome.values());
        return catalogoCache;
    }

    private Map<String, Object> itemParaMap(ItemCatalogo i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("codigo", i.codigo());
        m.put("descricao", i.nome());
        m.put("categoria", i.categoria());
        m.put("preco", i.preco());
        m.put("imagemUrl", i.imagem());
        return m;
    }

    private String categoriaDoNome(String nome) {
        String n = nome.toUpperCase();
        if (n.contains("ENERG")) return ENERGETICO;
        if (n.contains("ÁGUA") || n.contains("AGUA")) return AGUA;
        if (n.contains("CERVEJA")) return CERVEJA;
        if (n.contains("SUCO")) return SUCO;
        return REFRI;
    }

    private static Map<String, Double> afinidade(double refri, double suco, double agua, double cerveja, double energetico) {
        return Map.of(REFRI, refri, SUCO, suco, AGUA, agua, CERVEJA, cerveja, ENERGETICO, energetico);
    }

    private String obterNomeComercialReal(Produto p) {
        String descOriginal = p.getDescricao() != null ? p.getDescricao().toUpperCase() : "";

        if (descOriginal.contains("REFRIGERANTE")) {
            if (descOriginal.contains("COLA")) return "Fruki Cola 2L";
            if (descOriginal.contains("LARANJA")) return "Fruki Laranja 2L";
            if (descOriginal.contains("UVA")) return "Fruki Uva 2L";
            return "Fruki Guaraná 2L";
        } else if (descOriginal.contains("ENERGETICO") || descOriginal.contains("ENERGÉTICO")) {
            return "Energético Traz Elev 473ml";
        } else if (descOriginal.contains("AGUA") || descOriginal.contains("ÁGUA")) {
            return "Água da Pedra 500ml";
        } else if (descOriginal.contains("CERVEJA")) {
            return "Cerveja Bellavista Puro Malte";
        } else if (descOriginal.contains("SUCO")) {
            return "Suco Frut Fresh 1L";
        }

        return "Bebida Fruki B2B";
    }

    private String obterCaminhoImagem(Produto p) {
        String descOriginal = p.getDescricao() != null ? p.getDescricao().toUpperCase() : "";

        if (descOriginal.contains("REFRIGERANTE")) {
            if (descOriginal.contains("COLA")) return "/images/Refri fruki/cola-2l.webp";
            if (descOriginal.contains("LARANJA")) return "/images/Refri fruki/laranja-let-2l.webp";
            if (descOriginal.contains("UVA")) return "/images/Refri fruki/mockup-2l-uva-final.webp";
            return "/images/Refri fruki/guarana1-5l.webp";
        } else if (descOriginal.contains("ENERGETICO") || descOriginal.contains("ENERGÉTICO")) {
            return "/images/Energetico fruki/eleve_473_original.webp";
        } else if (descOriginal.contains("AGUA") || descOriginal.contains("ÁGUA")) {
            return "/images/Agua fruki/fruki-500ml-agua-s-gas-gotas.webp";
        } else if (descOriginal.contains("CERVEJA")) {
            return "/images/Cervejas fruki/bellavista-premium.webp";
        } else if (descOriginal.contains("SUCO")) {
            return "/images/Sucos fruki/_suco_tetrapack_1l_frente_laranja.webp";
        }

        return "/images/Refri fruki/cola-2l.webp";
    }
}