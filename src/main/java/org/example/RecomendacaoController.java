package org.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * API de recomendação.
 *
 * <p><b>Rota do pitch (mock):</b> {@code GET /api/recomendacao/{cnpj}} — aceita
 * qualquer CNPJ de 14 dígitos e devolve os 3 produtos fixos definidos com o PO.
 * Não consulta banco.</p>
 *
 * <p><b>Rota legada (continua funcionando):</b> {@code GET /api/recomendacao/{codCliente}}
 * — para os IDs numéricos usados no chat e nos testes (ex.: 110569, 149062).
 * Nessa rota o clima real do cliente é usado quando há latitude/longitude.</p>
 */
@RestController
@RequestMapping("/api/recomendacao")
public class RecomendacaoController {

    private final RecomendacaoService recomendacaoService;
    private final RecomendacaoDemoService demoService;
    private final ClimaService climaService;

    public RecomendacaoController(RecomendacaoService recomendacaoService,
                                  RecomendacaoDemoService demoService,
                                  ClimaService climaService) {
        this.recomendacaoService = recomendacaoService;
        this.demoService = demoService;
        this.climaService = climaService;
    }

    /**
     * Variante para CNPJ formatado (com pontos, barra e hífen), que não cabe em
     * um único segmento de URL. Ex.: {@code /api/recomendacao?cnpj=12.345.678/0001-90}
     */
    @GetMapping
    public ResponseEntity<?> recomendarPorCnpj(@RequestParam("cnpj") String cnpj) {
        String digitos = cnpj == null ? "" : cnpj.replaceAll("\\D", "");
        if (digitos.length() != 14) {
            return erro(400, "CNPJ inválido. Informe os 14 dígitos.");
        }
        return ResponseEntity.ok(demoService.recomendarPorCnpj(digitos));
    }

    @GetMapping("/{documento}")
    public ResponseEntity<?> recomendar(@PathVariable String documento) {
        String digitos = documento == null ? "" : documento.replaceAll("\\D", "");

        if (digitos.isEmpty()) {
            return erro(400, "Informe um CNPJ (14 dígitos) ou o código do cliente.");
        }

        // Qualquer CNPJ de 14 dígitos cai no mock do pitch.
        if (digitos.length() == 14) {
            return ResponseEntity.ok(demoService.recomendarPorCnpj(digitos));
        }
        // CNPJ incompleto ou documento grande demais: erro claro em vez de 500.
        if (digitos.length() > 9) {
            return erro(400, "Documento inválido. Use um CNPJ com 14 dígitos ou o código do cliente.");
        }

        long codCliente;
        try {
            codCliente = Long.parseLong(digitos);
        } catch (NumberFormatException ex) {
            return erro(400, "Código de cliente inválido.");
        }

        if (codCliente <= 0) {
            return erro(400, "ID inválido.");
        }

        Cliente cliente = recomendacaoService.buscarCliente(codCliente);
        if (cliente == null) {
            return erro(404, "Cliente não encontrado.");
        }

        ClimaService.Clima clima = climaService.paraCliente(cliente);
        Map<String, Object> dados = recomendacaoService.buscarDadosCompletos(
                codCliente, clima.temperatura(), null);

        if (dados.get("contexto") instanceof Map<?, ?> contexto) {
            @SuppressWarnings("unchecked")
            Map<String, Object> ctx = (Map<String, Object>) contexto;
            ctx.put("descricao", clima.descricao());
            ctx.put("cidade", clima.cidade());
            ctx.put("fonte", clima.fonte());
        }
        dados.put("fonteClima", clima.fonte());

        return ResponseEntity.ok(dados);
    }

    private ResponseEntity<Map<String, Object>> erro(int status, String mensagem) {
        return ResponseEntity.status(status).body(Map.<String, Object>of("erro", mensagem));
    }
}
