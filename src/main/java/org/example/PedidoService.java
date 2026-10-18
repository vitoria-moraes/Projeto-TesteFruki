package org.example;

import org.example.RecomendacaoService.ItemCatalogo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fechamento de pedido (simulado): valida cliente e itens, calcula totais no servidor
 * (quantidade de caixas x preço da caixa), aplica a condição de pagamento e gera o número do pedido.
 * Os pedidos ficam em memória durante a demonstração.
 */
@Service
public class PedidoService {

    public static class PedidoInvalidoException extends RuntimeException {
        public PedidoInvalidoException(String msg) { super(msg); }
    }

    /** Item recebido do front: código do produto e quantidade de caixas. */
    public record ItemPedido(String codigo, Integer quantidade) {}

    private static final BigDecimal DESCONTO_PIX = new BigDecimal("0.05");

    private final RecomendacaoService recomendacaoService;
    private final AtomicLong sequencia = new AtomicLong(1000);
    private final Map<String, Map<String, Object>> pedidos = new ConcurrentHashMap<>();

    public PedidoService(RecomendacaoService recomendacaoService) {
        this.recomendacaoService = recomendacaoService;
    }

    public Map<String, Object> registrarPedido(Long codCliente, List<ItemPedido> itens, String pagamento) {
        if (codCliente == null || recomendacaoService.localizarPorCodigo(codCliente).isEmpty()) {
            throw new RecomendacaoService.ClienteNaoEncontradoException("Cliente não encontrado ou sem cadastro ativo.");
        }
        if (itens == null || itens.isEmpty()) {
            throw new PedidoInvalidoException("O carrinho está vazio.");
        }

        String condicao;
        switch (pagamento == null ? "" : pagamento.toUpperCase()) {
            case "BOLETO" -> condicao = "Boleto Faturado B2B (30 dias)";
            case "PIX" -> condicao = "PIX à vista (5% de desconto)";
            case "CARTAO" -> condicao = "Cartão Corporativo";
            default -> throw new PedidoInvalidoException("Escolha uma condição de pagamento válida.");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<Map<String, Object>> linhas = new ArrayList<>();
        for (ItemPedido it : itens) {
            if (it == null || it.codigo() == null || it.quantidade() == null || it.quantidade() <= 0) {
                throw new PedidoInvalidoException("Item inválido no pedido.");
            }
            ItemCatalogo produto = recomendacaoService.produtoPorCodigo(it.codigo())
                    .orElseThrow(() -> new PedidoInvalidoException("Produto " + it.codigo() + " não encontrado no catálogo."));

            BigDecimal precoCaixa = BigDecimal.valueOf(produto.precoCaixa()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal sub = precoCaixa.multiply(BigDecimal.valueOf(it.quantidade()));
            subtotal = subtotal.add(sub);

            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("codigo", produto.codigo());
            linha.put("descricao", produto.nome());
            linha.put("quantidade", it.quantidade());
            linha.put("precoCaixa", precoCaixa);
            linha.put("subtotal", sub);
            linhas.add(linha);
        }

        BigDecimal desconto = "PIX".equalsIgnoreCase(pagamento)
                ? subtotal.multiply(DESCONTO_PIX).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);
        BigDecimal total = subtotal.subtract(desconto);

        String numero = "PF-" + LocalDate.now().getYear() + "-" + String.format("%06d", sequencia.incrementAndGet());

        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("numeroPedido", numero);
        pedido.put("codCliente", codCliente);
        pedido.put("condicaoPagamento", condicao);
        pedido.put("itens", linhas);
        pedido.put("subtotal", subtotal);
        pedido.put("desconto", desconto);
        pedido.put("total", total);
        pedido.put("previsaoEntrega", proximoDiaUtil().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        pedidos.put(numero, pedido);
        return pedido;
    }

    private LocalDate proximoDiaUtil() {
        LocalDate d = LocalDate.now().plusDays(1);
        while (d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY) {
            d = d.plusDays(1);
        }
        return d;
    }
}