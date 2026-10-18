package org.example;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Catálogo comercial usado na demonstração.
 *
 * <p>A base original tem 263 códigos, mas boa parte é variação de embalagem e
 * está com nome/marca anonimizados. Para o chat ficar legível, este catálogo
 * reúne 31 SKUs representativos com nome, categoria, preço e imagem reais do
 * repositório. A lista é a mesma para todos os clientes; o que muda por CNPJ é
 * a recomendação (quantidade, motivo e ordem).</p>
 */
@Service
public class CatalogoService {

    public record ProdutoDemo(String codigo, String descricao, String categoria,
                              double preco, String imagemUrl, String embalagem) {
    }

    private static final List<String> ORDEM_CATEGORIAS = List.of(
            "AGUA", "REFRIGERANTE", "SUCO", "ENERGETICO", "CERVEJA", "TONICA");

    private static final Map<String, String> NOMES_CATEGORIAS = Map.of(
            "AGUA", "Águas",
            "REFRIGERANTE", "Refrigerantes",
            "SUCO", "Sucos",
            "ENERGETICO", "Energéticos",
            "CERVEJA", "Cervejas",
            "TONICA", "Tônicas");

    private static final List<ProdutoDemo> PRODUTOS = List.of(
            // Águas
            p("AGUA-500", "Água da Pedra 500ml sem gás", "AGUA", 2.00,
                    "/images/Agua fruki/fruki-500ml-agua-s-gas-gotas.webp", "Caixa 12un"),
            p("AGUA-500-GAS", "Água da Pedra 500ml com gás", "AGUA", 2.20,
                    "/images/Agua fruki/fruki-500ml-agua-c-gas-gotas.webp", "Caixa 12un"),
            p("AGUA-1.5", "Água da Pedra 1,5L", "AGUA", 3.50,
                    "/images/Agua fruki/fruki_pet_1-5l_regular_frontal.webp", "Caixa 6un"),
            p("AGUA-2", "Água da Pedra 2L", "AGUA", 4.00,
                    "/images/Agua fruki/fruki_pet_2l_regular_frontal.webp", "Caixa 6un"),
            p("AGUA-5", "Água da Pedra 5L", "AGUA", 6.00,
                    "/images/Agua fruki/fruki-5l-agua-s-gas-gotas.webp", "Unidade"),
            p("AGUA-350-GAS", "Água da Pedra 350ml com gás", "AGUA", 1.80,
                    "/images/Agua fruki/fruki-350ml-agua-c-gas-gotas.webp", "Caixa 12un"),

            // Refrigerantes
            p("REFRI-COLA-2L", "Fruki Cola 2L", "REFRIGERANTE", 7.80,
                    "/images/Refri fruki/cola-2l.webp", "Caixa 6un"),
            p("REFRI-COLA-600", "Fruki Cola 600ml", "REFRIGERANTE", 4.50,
                    "/images/Refri fruki/cola-600.webp", "Caixa 12un"),
            p("REFRI-GUARANA-2L", "Fruki Guaraná 2L", "REFRIGERANTE", 7.50,
                    "/images/Refri fruki/guarana1-5l.webp", "Caixa 6un"),
            p("REFRI-LARANJA-2L", "Fruki Laranja 2L", "REFRIGERANTE", 7.20,
                    "/images/Refri fruki/laranja-let-2l.webp", "Caixa 6un"),
            p("REFRI-UVA-2L", "Fruki Uva 2L", "REFRIGERANTE", 7.20,
                    "/images/Refri fruki/mockup-2l-uva-final.webp", "Caixa 6un"),
            p("REFRI-COLA-ZERO-2L", "Fruki Cola Zero 2L", "REFRIGERANTE", 7.90,
                    "/images/Refri fruki/cola-zero-2l.webp", "Caixa 6un"),

            // Sucos
            p("SUCO-LARANJA-1L", "Suco Frut Fresh Laranja 1L", "SUCO", 4.80,
                    "/images/Sucos fruki/_suco_tetrapack_1l_frente_laranja.webp", "Caixa 12un"),
            p("SUCO-UVA-1L", "Suco de Uva 1L (vidro)", "SUCO", 5.20,
                    "/images/Sucos fruki/_suco_vidro_1l_frente_uva.webp", "Caixa 6un"),
            p("SUCO-MACA-1L", "Suco de Maçã 1L", "SUCO", 5.20,
                    "/images/Sucos fruki/_suco_tetrapack_1l_frente_maca.webp", "Caixa 12un"),
            p("SUCO-LARANJA-200", "Suco Frut Fresh Laranja 200ml", "SUCO", 1.50,
                    "/images/Sucos fruki/_suco_tetrapack_200ml_frente_laranja.webp", "Caixa 27un"),
            p("SUCO-UVA-200", "Suco de Uva 200ml", "SUCO", 1.50,
                    "/images/Sucos fruki/_suco_tetrapack_200ml_frente_uva.webp", "Caixa 27un"),

            // Energéticos
            p("ENERG-ORIGINAL-473", "Energético Traz Elev Original 473ml", "ENERGETICO", 6.90,
                    "/images/Energetico fruki/eleve_473_original.webp", "Caixa 12un"),
            p("ENERG-TROPICAL-473", "Energético Traz Elev Frutas Tropicais 473ml", "ENERGETICO", 7.20,
                    "/images/Energetico fruki/eleve_473_frutas-tropicais.webp", "Caixa 12un"),
            p("ENERG-ORIGINAL-269", "Energético Traz Elev Original 269ml", "ENERGETICO", 4.50,
                    "/images/Energetico fruki/elev-269ml-original.webp", "Caixa 12un"),
            p("ENERG-TROPICAL-269", "Energético Traz Elev Frutas Tropicais 269ml", "ENERGETICO", 4.50,
                    "/images/Energetico fruki/elev-269ml-frutas-tropicais.webp", "Caixa 12un"),
            p("ENERG-MACA-473", "Energético Traz Elev Maçã Verde 473ml", "ENERGETICO", 7.20,
                    "/images/Energetico fruki/eleve_473_maca_verde.webp", "Caixa 12un"),

            // Cervejas
            p("CERVEJA-PILSEN-473", "Cerveja Bellavista Pilsen 473ml", "CERVEJA", 5.50,
                    "/images/Cervejas fruki/bellavista-pilsen-473.webp", "Caixa 12un"),
            p("CERVEJA-IPA-350", "Cerveja Bellavista IPA 350ml", "CERVEJA", 6.50,
                    "/images/Cervejas fruki/bellavista-ipa-350.webp", "Caixa 12un"),
            p("CERVEJA-PREMIUM", "Cerveja Bellavista Premium", "CERVEJA", 5.90,
                    "/images/Cervejas fruki/bellavista-premium.webp", "Caixa 12un"),
            p("CERVEJA-SESSION-350", "Cerveja Bellavista Session IPA 350ml", "CERVEJA", 6.90,
                    "/images/Cervejas fruki/bellavista-session-ipa-350.webp", "Caixa 12un"),
            p("CERVEJA-ZERO-350", "Cerveja Bellavista Zero 350ml", "CERVEJA", 5.90,
                    "/images/Cervejas fruki/bellavista-zero-350.webp", "Caixa 12un"),

            // Tônicas
            p("TONICA-350", "Tônica Fruki 350ml", "TONICA", 3.50,
                    "/images/Tonicas fruki/tonica__350ml.webp", "Caixa 12un"),
            p("TONICA-ZERO-350", "Tônica Fruki Zero 350ml", "TONICA", 3.50,
                    "/images/Tonicas fruki/tonica_zero_lata-1-.webp", "Caixa 12un"),
            p("TONICA-1500", "Tônica Fruki 1,5L", "TONICA", 7.90,
                    "/images/Tonicas fruki/tonica_1500ml.webp", "Caixa 6un"),
            p("TONICA-CITRUS-350", "Tônica Citrus 350ml", "TONICA", 3.80,
                    "/images/Tonicas fruki/citrus_350ml.png2.webp", "Caixa 12un")
    );

    private static ProdutoDemo p(String codigo, String descricao, String categoria,
                                 double preco, String imagemUrl, String embalagem) {
        return new ProdutoDemo(codigo, descricao, categoria, preco, imagemUrl, embalagem);
    }

    public List<ProdutoDemo> listar() {
        return PRODUTOS;
    }

    public Optional<ProdutoDemo> porCodigo(String codigo) {
        if (codigo == null) return Optional.empty();
        return PRODUTOS.stream().filter(p -> p.codigo().equals(codigo)).findFirst();
    }

    public Map<String, Object> paraMap(ProdutoDemo produto) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("codigo", produto.codigo());
        item.put("descricao", produto.descricao());
        item.put("categoria", produto.categoria());
        item.put("preco", produto.preco());
        item.put("imagemUrl", produto.imagemUrl());
        item.put("embalagem", produto.embalagem());
        return item;
    }

    /** Resposta de GET /api/catalogo: lista completa + agrupamento por categoria. */
    public Map<String, Object> catalogoAgrupado() {
        List<Map<String, Object>> categorias = new ArrayList<>();
        for (String categoria : ORDEM_CATEGORIAS) {
            List<Map<String, Object>> itens = PRODUTOS.stream()
                    .filter(p -> p.categoria().equals(categoria))
                    .map(this::paraMap)
                    .toList();
            if (itens.isEmpty()) continue;

            Map<String, Object> grupo = new LinkedHashMap<>();
            grupo.put("categoria", categoria);
            grupo.put("nome", NOMES_CATEGORIAS.getOrDefault(categoria, categoria));
            grupo.put("itens", itens);
            categorias.add(grupo);
        }

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("total", PRODUTOS.size());
        resposta.put("categorias", categorias);
        resposta.put("itens", PRODUTOS.stream().map(this::paraMap).toList());
        return resposta;
    }

    public String nomeCategoria(String categoria) {
        return NOMES_CATEGORIAS.getOrDefault(categoria, categoria);
    }

    public String formatarPreco(double valor) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%.2f", valor);
    }
}
