package org.example;

import org.example.PedidoService.ItemPedido;
import org.example.PedidoService.PedidoInvalidoException;
import org.example.RecomendacaoService.ClienteNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * POST /api/pedidos
 * { "codCliente": 194643, "pagamento": "PIX", "itens": [ { "codigo": "902", "quantidade": 3 } ] }
 * pagamento: BOLETO | PIX | CARTAO
 */
@RestController
@RequestMapping("/api")
public class PedidoController {

    public record PedidoRequest(Long codCliente, String pagamento, List<ItemPedido> itens) {}

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping("/pedidos")
    public ResponseEntity<Map<String, Object>> criarPedido(@RequestBody PedidoRequest req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(pedidoService.registrarPedido(req.codCliente(), req.itens(), req.pagamento()));
        } catch (ClienteNaoEncontradoException e) {
            return erro(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (PedidoInvalidoException e) {
            return erro(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível transmitir o pedido agora. Tente novamente.");
        }
    }

    private ResponseEntity<Map<String, Object>> erro(HttpStatus status, String mensagem) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("erro", mensagem);
        return ResponseEntity.status(status).body(corpo);
    }
}