package org.example;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Motor de recomendação por regras e pontuação (scoring) + validação de acesso e cadastro expresso.
 *
 * score = 35% histórico + 30% perfil do mercado + 25% fatores externos (clima, época, evento) + 10% descoberta
 * Além do ranking, o motor sugere o VOLUME (em caixas) conforme o PORTE do estabelecimento.
 *
 * ATENÇÃO: histórico de compras, clima e porte são FICTÍCIOS (simulados para a demonstração).
 */
@Service
public class RecomendacaoService {

    // ------------------------------------------------------------------ Exceções de negócio

    public static class IdentificadorInvalidoException extends RuntimeException {
        public IdentificadorInvalidoException(String msg) { super(msg); }
    }

    public static class ClienteNaoEncontradoException extends RuntimeException {
        public ClienteNaoEncontradoException(String msg) { super(msg); }
    }

    public static class ConflitoException extends RuntimeException {
        public ConflitoException(String msg) { super(msg); }
    }

    public static class DadosInvalidosException extends RuntimeException {
        public DadosInvalidosException(String msg) { super(msg); }
    }

    // ------------------------------------------------------------------ Constantes

    private static final double PESO_HISTORICO = 0.35;
    private static final double PESO_PERFIL = 0.30;
    private static final double PESO_EXTERNO = 0.25;
    private static final double PESO_DESCOBERTA = 0.10;

    private static final int TOTAL_RECOMENDADOS = 4;
    private static final int JANELA_FERIADO_DIAS = 21;
    private static final double BONUS_EVENTO = 0.3;
    private static final double REFORCO_SAZONAL_VOLUME = 1.5;

    private static final String REFRI = "REFRIGERANTE";
    private static final String SUCO = "SUCO";
    private static final String AGUA = "AGUA";
    private static final String CERVEJA = "CERVEJA";
    private static final String ENERGETICO = "ENERGETICO";

    private static final String MINIMERCADO = "MINIMERCADO";
    private static final String BAR_RESTAURANTE = "BAR_RESTAURANTE";
    private static final String GRANDE_DISTRIBUIDOR = "GRANDE_DISTRIBUIDOR";

    private static final String[] PERFIS = {"Padaria", "Mercadinho", "Supermercado", "Restaurante", "Adega"};

    private static final Map<String, Map<String, Double>> AFINIDADE_PERFIL = Map.of(
            "Padaria", afinidade(0.6, 0.9, 0.6, 0.1, 0.3),
            "Mercadinho", afinidade(0.9, 0.5, 0.6, 0.6, 0.5),
            "Supermercado", afinidade(1.0, 0.7, 0.7, 0.8, 0.6),
            "Restaurante", afinidade(0.8, 0.7, 0.8, 0.7, 0.2),
            "Adega", afinidade(0.5, 0.2, 0.5, 1.0, 0.7)
    );

    /** Porte (fictício) derivado do perfil de mercado. Trocar por dado real quando existir. */
    private static final Map<String, String> PORTE_POR_PERFIL = Map.of(
            "Padaria", MINIMERCADO,
            "Mercadinho", MINIMERCADO,
            "Restaurante", BAR_RESTAURANTE,
            "Adega", BAR_RESTAURANTE,
            "Supermercado", GRANDE_DISTRIBUIDOR
    );

    /** Perfil usado no scoring para clientes que se cadastram informando só o porte. */
    private static final Map<String, String> PERFIL_POR_PORTE = Map.of(
            MINIMERCADO, "Mercadinho",
            BAR_RESTAURANTE, "Restaurante",
            GRANDE_DISTRIBUIDOR, "Supermercado"
    );

    private static final Map<String, String> ROTULO_PORTE = Map.of(
            MINIMERCADO, "Minimercado",
            BAR_RESTAURANTE, "Bar / Restaurante",
            GRANDE_DISTRIBUIDOR, "Grande Distribuidor"
    );

    /** Multiplicador do volume de caixas por porte. */
    private static final Map<String, Double> FATOR_PORTE = Map.of(
            MINIMERCADO, 1.0,
            BAR_RESTAURANTE, 3.0,
            GRANDE_DISTRIBUIDOR, 8.0
    );

    /** Caixas-base sugeridas por categoria para um minimercado. */
    private static final Map<String, Integer> CAIXAS_BASE = Map.of(
            REFRI, 1, AGUA, 2, CERVEJA, 1, SUCO, 1, ENERGETICO, 1
    );

    private static final Map<String, Integer> UNIDADES_POR_CAIXA = Map.of(
            REFRI, 6, AGUA, 12, CERVEJA, 12, SUCO, 12, ENERGETICO, 12
    );

    private static final Map<LocalDate, String> FERIADOS = new TreeMap<>(Map.of(
            LocalDate.of(2026, 11, 2), "Finados",
            LocalDate.of(2026, 11, 15), "Proclamação da República",
            LocalDate.of(2026, 11, 20), "Consciência Negra",
            LocalDate.of(2026, 12, 25), "Natal",
            LocalDate.of(2027, 1, 1), "Ano Novo"
    ));

    /** Preço FICTÍCIO por unidade. */
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

    // ------------------------------------------------------------------ Tipos auxiliares

    public record ItemCatalogo(String codigo, String nome, String categoria, double precoUnitario,
                               int unidadesPorCaixa, String imagem) {
        /** Preço da caixa (é assim que o B2B vende). */
        public double precoCaixa() {
            return Math.round(precoUnitario * unidadesPorCaixa * 100.0) / 100.0;
        }
    }

    public record ClienteInfo(Long codCliente, String nome, String perfil, String porte, boolean novo) {}

    private record Contexto(double temperatura, String feriado, Integer diasParaFeriado,
                            boolean fimDeSemana, String estacao) {
        boolean periodoQuente() { return temperatura >= 28; }
        boolean evento() { return fimDeSemana || diasParaFeriado != null; }
    }

    // ------------------------------------------------------------------ Estado

    private final ExcelReaderService excelReaderService;
    private final CatalogoService catalogoService;

    private List<Cliente> clientesCache;
    private List<ItemCatalogo> catalogoCache;

    // Clientes cadastrados pelo cadastro expresso (ficam em memória durante a demonstração)
    private final List<Cliente> cadastrados = new CopyOnWriteArrayList<>();
    private final Map<Long, String> portePorCadastro = new ConcurrentHashMap<>();
    private long proximoCodigoCadastro = 900001L;

    public RecomendacaoService(ExcelReaderService excelReaderService, CatalogoService catalogoService) {
        this.excelReaderService = excelReaderService;
        this.catalogoService = catalogoService;
    }

    // ------------------------------------------------------------------ Validação de acesso

    /**
     * Valida ID (COD_CLIENTE ou id_cliente da base) ou CNPJ.
     * Lança IdentificadorInvalidoException se o formato for inválido.
     */
    public Optional<ClienteInfo> localizarCliente(String identificador) {
        String digitos = somenteDigitos(identificador);
        validarFormato(digitos);

        if (digitos.length() == 14) {
            // A base original não tem CNPJ: só encontra clientes do cadastro expresso
            return cadastrados.stream()
                    .filter(c -> digitos.equals(c.getCnpj()))
                    .findFirst()
                    .map(this::infoDeCadastrado);
        }
        return localizarPorCodigo(Long.parseLong(digitos));
    }

    public Optional<ClienteInfo> localizarPorCodigo(Long codigo) {
        List<Cliente> csv = carregarClientes();
        Cliente achado = csv.stream().filter(c -> codigo.equals(c.getCodCliente())).findFirst()
                .orElseGet(() -> csv.stream().filter(c -> codigo.equals(c.getIdCliente())).findFirst().orElse(null));
        if (achado != null) {
            return Optional.of(infoDeCsv(achado));
        }
        return cadastrados.stream()
                .filter(c -> codigo.equals(c.getCodCliente()))
                .findFirst()
                .map(this::infoDeCadastrado);
    }

    /**
     * Procura apenas entre os CNPJs do cadastro expresso (memória), sem validar
     * o dígito verificador. O controller usa isso para decidir entre o motor
     * real e a demo por CNPJ sem lançar exceção de formato.
     */
    public Optional<ClienteInfo> localizarCadastradoPorCnpj(String cnpj) {
        String digitos = somenteDigitos(cnpj);
        if (digitos.length() != 14) return Optional.empty();
        return cadastrados.stream()
                .filter(c -> digitos.equals(c.getCnpj()))
                .findFirst()
                .map(this::infoDeCadastrado);
    }

    /** Cliente completo (com latitude/longitude), usado para buscar o clima real. */
    public Optional<Cliente> clientePorCodigo(Long codigo) {
        if (codigo == null) return Optional.empty();
        List<Cliente> csv = carregarClientes();
        Optional<Cliente> achado = csv.stream().filter(c -> codigo.equals(c.getCodCliente())).findFirst();
        if (achado.isPresent()) return achado;
        achado = csv.stream().filter(c -> codigo.equals(c.getIdCliente())).findFirst();
        if (achado.isPresent()) return achado;
        return cadastrados.stream().filter(c -> codigo.equals(c.getCodCliente())).findFirst();
    }

    // ------------------------------------------------------------------ Cadastro expresso

    public synchronized Map<String, Object> cadastrarCliente(String identificador, String nome, String porte) {
        String digitos = somenteDigitos(identificador);
        validarFormato(digitos);

        if (nome == null || nome.trim().length() < 3) {
            throw new DadosInvalidosException("Informe a razão social do estabelecimento (mínimo de 3 caracteres).");
        }
        String porteNorm = porte == null ? "" : porte.trim().toUpperCase();
        if (!FATOR_PORTE.containsKey(porteNorm)) {
            throw new DadosInvalidosException("Porte inválido. Use MINIMERCADO, BAR_RESTAURANTE ou GRANDE_DISTRIBUIDOR.");
        }
        if (localizarCliente(digitos).isPresent()) {
            throw new ConflitoException("Já existe um cadastro ativo para este ID/CNPJ.");
        }

        Long codigo;
        String cnpj = "";
        if (digitos.length() == 14) {
            cnpj = digitos;
            codigo = proximoCodigoCadastro++;
        } else {
            codigo = Long.parseLong(digitos);
        }

        cadastrados.add(new Cliente(codigo, nome.trim(), "", cnpj));
        portePorCadastro.put(codigo, porteNorm);

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("codCliente", codigo);
        resposta.put("nomeCliente", nome.trim());
        resposta.put("porte", porteNorm);
        resposta.put("porteLabel", ROTULO_PORTE.get(porteNorm));
        return resposta;
    }

    // ------------------------------------------------------------------ Recomendação

    public Map<String, Object> buscarDadosCompletos(String identificador) {
        return buscarDadosCompletos(identificador, null, null, null);
    }

    /**
     * @param temperaturaSimulada opcional: força a temperatura (demonstração)
     * @param diasFeriadoSimulado opcional: força um feriado daqui a N dias (demonstração)
     * @param fimDeSemanaSimulado opcional: força (ou desliga) o efeito de fim de semana/evento (demonstração)
     */
    public Map<String, Object> buscarDadosCompletos(String identificador, Double temperaturaSimulada,
                                                    Integer diasFeriadoSimulado, Boolean fimDeSemanaSimulado) {
        ClienteInfo cliente = localizarCliente(identificador)
                .orElseThrow(() -> new ClienteNaoEncontradoException(
                        "Não encontramos um cadastro ativo para o ID/CNPJ informado."));

        List<ItemCatalogo> catalogo = carregarCatalogo();
        Contexto ctx = montarContexto(temperaturaSimulada, diasFeriadoSimulado, fimDeSemanaSimulado);

        // Histórico fictício e determinístico por cliente (cliente novo não tem histórico)
        Random rnd = new Random(cliente.codCliente());

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (ItemCatalogo item : catalogo) {
            boolean comprou = rnd.nextDouble() > 0.35;           // sempre sorteia: mantém o resultado estável
            int qtd90dias = comprou ? 1 + rnd.nextInt(12) : 0;
            int diasUltimaCompra = 2 + rnd.nextInt(40);
            if (cliente.novo()) qtd90dias = 0;

            String cat = item.categoria();
            List<String> motivos = new ArrayList<>();

            // 1) Histórico
            double histScore = Math.min(1.0, qtd90dias / 10.0);
            if (qtd90dias > 0 && diasUltimaCompra >= 14) {
                histScore = Math.min(1.0, histScore + 0.2);
                motivos.add("Já faz " + diasUltimaCompra + " dias desde o último pedido deste item");
            } else if (qtd90dias >= 6) {
                motivos.add("Item de alta saída no seu histórico");
            }

            // 2) Perfil do mercado
            double perfilScore = AFINIDADE_PERFIL.get(cliente.perfil()).getOrDefault(cat, 0.5);
            if (perfilScore >= 0.8) {
                motivos.add("Forte saída em " + cliente.perfil().toLowerCase());
            }

            // 3) Fatores externos: clima/época + evento (fim de semana ou feriado)
            double externoScore = scoreClima(cat, ctx.temperatura());
            if (ctx.periodoQuente() && externoScore >= 0.85) {
                motivos.add("Calor de " + Math.round(ctx.temperatura()) + "°C aumenta a procura");
            }
            boolean familiaOuCerveja = cat.equals(REFRI) || cat.equals(CERVEJA);
            if (ctx.evento() && familiaOuCerveja) {
                externoScore = Math.min(1.0, externoScore + BONUS_EVENTO);
                if (ctx.diasParaFeriado() != null) {
                    motivos.add(ctx.feriado() + " em " + ctx.diasParaFeriado() + " dias");
                } else {
                    motivos.add("Fim de semana: maior giro de " + (cat.equals(REFRI) ? "refrigerante família" : "cerveja"));
                }
            }

            // 4) Descoberta de portfólio
            double descobertaScore = qtd90dias == 0 ? 1.0 : 0.0;
            if (qtd90dias == 0) {
                motivos.add("Novidade no seu mix: você ainda não compra este item");
            }

            double score = PESO_HISTORICO * histScore + PESO_PERFIL * perfilScore
                    + PESO_EXTERNO * externoScore + PESO_DESCOBERTA * descobertaScore;

            // Volume sugerido em caixas: base da categoria x porte x reforço sazonal
            boolean reforco = (ctx.periodoQuente() && (cat.equals(AGUA) || cat.equals(ENERGETICO)))
                    || (ctx.evento() && familiaOuCerveja);
            int caixas = (int) Math.ceil(CAIXAS_BASE.getOrDefault(cat, 1)
                    * FATOR_PORTE.get(cliente.porte())
                    * (reforco ? REFORCO_SAZONAL_VOLUME : 1.0));

            Map<String, Object> map = itemParaMap(item);
            map.put("score", Math.round(score * 100));
            map.put("motivos", motivos);
            map.put("motivo", motivos.stream().limit(2).collect(Collectors.joining(" • ")));
            map.put("qtdSugeridaCaixas", caixas);
            map.put("justificativaVolume", "Volume ajustado ao porte (" + ROTULO_PORTE.get(cliente.porte()) + ")"
                    + (reforco ? " com reforço sazonal" : ""));
            ranking.add(map);
        }

        ranking.sort((a, b) -> {
            int c = Long.compare((Long) b.get("score"), (Long) a.get("score"));
            return c != 0 ? c : ((String) a.get("descricao")).compareTo((String) b.get("descricao"));
        });
        List<Map<String, Object>> recomendados = ranking.stream().limit(TOTAL_RECOMENDADOS).collect(Collectors.toList());

        Map<String, Object> contextoMap = new LinkedHashMap<>();
        contextoMap.put("temperatura", ctx.temperatura());
        contextoMap.put("estacao", ctx.estacao());
        contextoMap.put("fimDeSemana", ctx.fimDeSemana());
        contextoMap.put("feriado", ctx.feriado());
        contextoMap.put("diasParaFeriado", ctx.diasParaFeriado());
        contextoMap.put("resumo", resumoContexto(ctx));
        contextoMap.put("dadosFicticios", true);

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("codCliente", cliente.codCliente());
        resposta.put("nomeCliente", cliente.nome());
        resposta.put("perfilMercado", cliente.perfil());
        resposta.put("porte", cliente.porte());
        resposta.put("porteLabel", ROTULO_PORTE.get(cliente.porte()));
        resposta.put("novoCliente", cliente.novo());
        resposta.put("contexto", contextoMap);
        resposta.put("produtosRecomendados", recomendados);
        resposta.put("catalogoCompleto", catalogo.stream().map(this::itemParaMap).collect(Collectors.toList()));
        return resposta;
    }

    // ------------------------------------------------------------------ Catálogo (usado também pelos pedidos)

    public List<ItemCatalogo> catalogo() {
        return carregarCatalogo();
    }

    public Optional<ItemCatalogo> produtoPorCodigo(String codigo) {
        return carregarCatalogo().stream().filter(i -> i.codigo().equals(codigo)).findFirst();
    }

    // ------------------------------------------------------------------ Regras internas

    private double scoreClima(String categoria, double temperatura) {
        if (temperatura >= 28) {   // período quente: água e energético lideram
            return switch (categoria) {
                case AGUA -> 1.0;
                case ENERGETICO -> 0.95;
                case REFRI, CERVEJA -> 0.85;
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

    private Contexto montarContexto(Double temperaturaSimulada, Integer diasFeriadoSimulado, Boolean fimDeSemanaSimulado) {
        LocalDate hoje = LocalDate.now();
        String estacao = estacaoDo(hoje);
        double temp = temperaturaSimulada != null ? temperaturaSimulada : temperaturaPadrao(estacao);
        boolean fds = fimDeSemanaSimulado != null ? fimDeSemanaSimulado : ehFimDeSemana(hoje);

        if (diasFeriadoSimulado != null) {
            return new Contexto(temp, "Feriado", diasFeriadoSimulado, fds, estacao);
        }
        for (Map.Entry<LocalDate, String> f : FERIADOS.entrySet()) {
            long dias = ChronoUnit.DAYS.between(hoje, f.getKey());
            if (dias >= 0 && dias <= JANELA_FERIADO_DIAS) {
                return new Contexto(temp, f.getValue(), (int) dias, fds, estacao);
            }
        }
        return new Contexto(temp, null, null, fds, estacao);
    }

    /** Estações do hemisfério sul. */
    private static String estacaoDo(LocalDate d) {
        int mmdd = d.getMonthValue() * 100 + d.getDayOfMonth();
        if (mmdd >= 1221 || mmdd < 321) return "Verão";
        if (mmdd < 621) return "Outono";
        if (mmdd < 923) return "Inverno";
        return "Primavera";
    }

    /** Temperatura FICTÍCIA típica de cada estação (°C). */
    private static double temperaturaPadrao(String estacao) {
        return switch (estacao) {
            case "Verão" -> 32.0;
            case "Outono" -> 22.0;
            case "Inverno" -> 14.0;
            default -> 25.0;
        };
    }

    private static boolean ehFimDeSemana(LocalDate d) {
        DayOfWeek dia = d.getDayOfWeek();
        return dia == DayOfWeek.FRIDAY || dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY;
    }

    private static String resumoContexto(Contexto c) {
        StringBuilder sb = new StringBuilder(c.estacao()).append(", ").append(Math.round(c.temperatura())).append("°C");
        if (c.fimDeSemana()) sb.append(", fim de semana");
        if (c.diasParaFeriado() != null) sb.append(", ").append(c.feriado()).append(" em ").append(c.diasParaFeriado()).append(" dias");
        return sb.toString();
    }

    private ClienteInfo infoDeCsv(Cliente c) {
        String perfil = perfilDoMercado(c);
        return new ClienteInfo(c.getCodCliente(), c.getNome(), perfil, PORTE_POR_PERFIL.get(perfil), false);
    }

    private ClienteInfo infoDeCadastrado(Cliente c) {
        String porte = portePorCadastro.getOrDefault(c.getCodCliente(), MINIMERCADO);
        return new ClienteInfo(c.getCodCliente(), c.getNome(), PERFIL_POR_PORTE.get(porte), porte, true);
    }

    private String perfilDoMercado(Cliente cliente) {
        String seg = cliente.getCodSegmento() != null ? cliente.getCodSegmento() : "";
        return PERFIS[Math.abs(seg.hashCode()) % PERFIS.length];
    }

    // ------------------------------------------------------------------ Formato do ID / CNPJ

    private static String somenteDigitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    private static void validarFormato(String digitos) {
        if (digitos.isEmpty()) {
            throw new IdentificadorInvalidoException("Informe o ID ou o CNPJ usando apenas números.");
        }
        if (digitos.length() == 14) {
            if (!cnpjValido(digitos)) {
                throw new IdentificadorInvalidoException("Esse CNPJ não parece válido. Confira os 14 dígitos e tente novamente.");
            }
        } else if (digitos.length() > 12) {
            throw new IdentificadorInvalidoException("O CNPJ deve ter 14 dígitos. Para ID de cliente, use até 12 números.");
        }
    }

    static boolean cnpjValido(String c) {
        if (c.length() != 14 || c.chars().distinct().count() == 1) return false;
        int[] p1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] p2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        return digitoVerificador(c, p1) == c.charAt(12) - '0' && digitoVerificador(c, p2) == c.charAt(13) - '0';
    }

    private static int digitoVerificador(String c, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) soma += (c.charAt(i) - '0') * pesos[i];
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    // ------------------------------------------------------------------ Dados (CSV)

    private synchronized List<Cliente> carregarClientes() {
        if (clientesCache == null) {
            clientesCache = excelReaderService.lerClientesDoCsv();
        }
        return clientesCache;
    }

    /**
     * Catálogo único do protótipo: os 31 SKUs comerciais de {@link CatalogoService}.
     * Assim o motor de scoring, a demo por CNPJ e o fechamento de pedido falam a
     * mesma língua (código, preço de caixa e imagem).
     */
    private synchronized List<ItemCatalogo> carregarCatalogo() {
        if (catalogoCache != null) return catalogoCache;

        List<ItemCatalogo> itens = new ArrayList<>();
        for (CatalogoService.ProdutoDemo produto : catalogoService.listar()) {
            itens.add(new ItemCatalogo(
                    produto.codigo(),
                    produto.descricao(),
                    produto.categoria(),
                    produto.preco(),
                    produto.unidadesPorCaixa(),
                    produto.imagemUrl()));
        }
        catalogoCache = itens;
        return catalogoCache;
    }

    private void padrao(Map<String, ItemCatalogo> mapa, String cod, String nome, String cat, String imagem) {
        mapa.putIfAbsent(nome, new ItemCatalogo(cod, nome, cat, PRECOS.getOrDefault(nome, 7.50),
                UNIDADES_POR_CAIXA.get(cat), imagem));
    }

    private Map<String, Object> itemParaMap(ItemCatalogo i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("codigo", i.codigo());
        m.put("descricao", i.nome());
        m.put("categoria", i.categoria());
        m.put("preco", i.precoCaixa());               // preço da CAIXA (unidade de venda)
        m.put("precoUnitario", i.precoUnitario());
        m.put("unidadesPorCaixa", i.unidadesPorCaixa());
        m.put("embalagem", "Cx c/ " + i.unidadesPorCaixa() + " un");
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