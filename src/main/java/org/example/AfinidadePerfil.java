package org.example;

import java.util.Map;

/*
 * Pontua o quanto um tipo de produto combina com o segmento do cliente.
 * ATENÇÃO: os pesos abaixo são PLACEHOLDERS. Os nomes de segmento no CSV são anonimizados,
 * então ajuste o código do segmento (chave) e os pesos conforme a regra de negócio real.
 * Segmento sem entrada aqui recebe peso 0 para todos os tipos.
 */
public final class AfinidadePerfil {

    private static final Map<String, Map<String, Integer>> PESOS = Map.of(
            "23", Map.of("REFRIGERANTE", 3, "AGUA", 2, "SUCO", 1),
            "16", Map.of("CERVEJA", 3, "ENERGETICO", 2, "REFRIGERANTE", 1),
            "14", Map.of("ENERGETICO", 3, "REFRIGERANTE", 2, "AGUA", 1),
            "21", Map.of("AGUA", 3, "SUCO", 2, "REFRIGERANTE", 1)
    );

    private AfinidadePerfil() {}

    public static int score(String codSegmento, String tipo) {
        return PESOS.getOrDefault(codSegmento, Map.of()).getOrDefault(tipo, 0);
    }
}
