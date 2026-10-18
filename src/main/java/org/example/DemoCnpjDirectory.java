package org.example;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Diretório de CNPJs de demonstração.
 *
 * <p>Cada CNPJ aponta para um cliente/perfil/cidade diferentes e para uma
 * "montagem" de recomendação diferente. CNPJ desconhecido cai no mock padrão
 * de 3 produtos (o que o PO pediu para o pitch).</p>
 *
 * <p>Os eventos são <b>simulados</b> e ficam explícitos no JSON para não
 * confundir com dados reais da Fruki.</p>
 */
@Service
public class DemoCnpjDirectory {

    public record GrupoDemo(String titulo, String motivo, int quantidadeSugerida, List<String> codigos) {
    }

    public record PerfilDemo(String cnpj, long idCliente, long codCliente, String nome,
                             String cidade, double latitude, double longitude, String perfilMercado,
                             List<String> eventos, List<GrupoDemo> grupos) {
    }

    private static final Map<String, PerfilDemo> PERFIS = criarPerfis();

    private static Map<String, PerfilDemo> criarPerfis() {
        Map<String, PerfilDemo> perfis = new LinkedHashMap<>();

        perfis.put("12345678000190", new PerfilDemo(
                "12345678000190", 149062L, 239516L, "Cliente Demo — Erechim/RS",
                "Erechim", -27.618954, -52.268404, "Mercadinho",
                List.of("Festa política na cidade (evento simulado)",
                        "Jogo Corinthians x Vitória (evento simulado)"),
                List.of(
                        g("Refrigerantes para o movimento",
                                "Festa política ontem + jogo hoje: o bairro recebe mais gente. "
                                        + "3 opções para não faltar.",
                                3, "REFRI-COLA-2L", "REFRI-GUARANA-2L", "REFRI-LARANJA-2L"),
                        g("Cervejas para o jogo",
                                "Corinthians x Vitória hoje à noite: cerveja gelada é o item de maior giro.",
                                3, "CERVEJA-PILSEN-473", "CERVEJA-IPA-350", "CERVEJA-PREMIUM"),
                        g("Águas para hidratar",
                                "Evento e jogo aumentam o consumo de água; 3 opções para o time.",
                                3, "AGUA-500", "AGUA-1.5", "AGUA-2")
                )));

        perfis.put("98765432000110", new PerfilDemo(
                "98765432000110", 110569L, 173621L, "Cliente Demo — Porto Alegre/RS",
                "Porto Alegre", -30.0346, -51.2177, "Supermercado",
                List.of("Festa política na cidade (evento simulado)",
                        "Jogo Corinthians x Vitória (evento simulado)"),
                List.of(
                        g("Refrigerantes para o evento",
                                "Festa política + jogo: movimento alto. Refrigerante gelado é o primeiro a sair.",
                                3, "REFRI-COLA-2L", "REFRI-COLA-600", "REFRI-GUARANA-2L"),
                        g("Cervejas para o jogo",
                                "Corinthians x Vitória hoje: 3 rótulos para o público do bar.",
                                3, "CERVEJA-PILSEN-473", "CERVEJA-SESSION-350", "CERVEJA-ZERO-350"),
                        g("Águas para hidratar",
                                "Hidratação para o time e para o público do evento.",
                                3, "AGUA-500", "AGUA-500-GAS", "AGUA-1.5")
                )));

        perfis.put("11222333000144", new PerfilDemo(
                "11222333000144", 45127L, 46273L, "Cliente Demo — Caxias do Sul/RS",
                "Caxias do Sul", -29.1678, -51.1794, "Adega",
                List.of("Jogo Internacional x Grêmio (evento simulado)"),
                List.of(
                        g("Cervejas para o jogo",
                                "Internacional x Grêmio hoje: 3 rótulos para o público da adega.",
                                3, "CERVEJA-PILSEN-473", "CERVEJA-IPA-350", "CERVEJA-SESSION-350"),
                        g("Energéticos para a noite",
                                "Jogo à noite pede energético para quem vai até o fim.",
                                3, "ENERG-ORIGINAL-473", "ENERG-TROPICAL-473", "ENERG-ORIGINAL-269"),
                        g("Águas para hidratar",
                                "Água para intercalar com a cerveja e hidratar o time.",
                                3, "AGUA-500", "AGUA-1.5", "AGUA-2")
                )));

        perfis.put("55666777000188", new PerfilDemo(
                "55666777000188", 81065L, 81065L, "Cliente Demo — Santa Cruz do Sul/RS",
                "Santa Cruz do Sul", -29.7184, -52.4259, "Padaria",
                List.of("Festa da cidade (evento simulado)"),
                List.of(
                        g("Sucos para a família",
                                "Festa da cidade: famílias circulando, sucos para o público infantil.",
                                3, "SUCO-LARANJA-1L", "SUCO-UVA-1L", "SUCO-MACA-1L"),
                        g("Refrigerantes para o evento",
                                "Festa da cidade aumenta o consumo de refrigerante.",
                                3, "REFRI-GUARANA-2L", "REFRI-LARANJA-2L", "REFRI-UVA-2L"),
                        g("Águas para hidratar",
                                "Dia de festa ao ar livre pede água.",
                                3, "AGUA-500", "AGUA-1.5", "AGUA-5")
                )));

        return java.util.Collections.unmodifiableMap(perfis);
    }

    private static GrupoDemo g(String titulo, String motivo, int quantidade, String... codigos) {
        return new GrupoDemo(titulo, motivo, quantidade, List.of(codigos));
    }

    public Optional<PerfilDemo> buscar(String cnpj) {
        return Optional.ofNullable(PERFIS.get(cnpj));
    }

    public List<PerfilDemo> listar() {
        return List.copyOf(PERFIS.values());
    }
}
