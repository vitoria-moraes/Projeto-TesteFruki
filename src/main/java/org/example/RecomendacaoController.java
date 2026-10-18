package org.example;

import org.example.RecomendacaoService.ClienteInfo;
import org.example.RecomendacaoService.ClienteNaoEncontradoException;
import org.example.RecomendacaoService.ConflitoException;
import org.example.RecomendacaoService.DadosInvalidosException;
import org.example.RecomendacaoService.IdentificadorInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * API de recomendação, cadastro expresso e acesso do Pede Fruki.
 *
 * <p><b>Fluxo do pitch (demo):</b> qualquer CNPJ de 14 dígitos entra na
 * recomendação de demonstração — 3 grupos com 3 opções, quantidade sugerida e
 * motivo (clima + eventos simulados, {@link RecomendacaoDemoService}).</p>
 *
 * <p><b>Rota legada:</b> código numérico do cliente usa o motor de scoring com
 * sazonalidade ({@link RecomendacaoService}) e o clima real via
 * {@link ClimaService} quando o cliente tem latitude/longitude.</p>
 *
 * <p>Também expõe o cadastro expresso ({@code POST /api/cadastro}) e mantém a
 * compatibilidade com os parâmetros de demonstração {@code temperatura},
 * {@code diasFeriado} e {@code fimDeSemana}.</p>
 */
@RestController
@RequestMapping("/api")
public class RecomendacaoController {

    public record CadastroRequest(String identificador, String nome, String porte) {
    }

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
    @GetMapping("/recomendacao")
    public ResponseEntity<?> recomendarPorCnpj(
            @RequestParam("cnpj") String cnpj,
            @RequestParam(required = false) Double temperatura,
            @RequestParam(required = false) Integer diasFeriado) {
        String digitos = somenteDigitos(cnpj);
        if (digitos.length() != 14) {
            return erro(HttpStatus.BAD_REQUEST, "CNPJ_INVALIDO",
                    "CNPJ inválido. Informe os 14 dígitos.");
        }
        if (recomendacaoService.localizarCadastradoPorCnpj(digitos).isPresent()) {
            return comTratamento(() ->
                    recomendacaoService.buscarDadosCompletos(digitos, temperatura, diasFeriado, null));
        }
        return ResponseEntity.ok(demoService.recomendarPorCnpj(digitos, temperatura, diasFeriado));
    }

    /**
     * Rota principal do chat. Aceita CNPJ (14 dígitos) ou código numérico do
     * cliente.
     */
    @GetMapping("/recomendacao/{documento}")
    public ResponseEntity<?> consultarCliente(
            @PathVariable String documento,
            @RequestParam(required = false) Double temperatura,
            @RequestParam(required = false) Integer diasFeriado,
            @RequestParam(required = false) Boolean fimDeSemana) {
        String digitos = somenteDigitos(documento);

        if (digitos.isEmpty()) {
            return erro(HttpStatus.BAD_REQUEST, "IDENTIFICADOR_INVALIDO",
                    "Informe um CNPJ (14 dígitos) ou o código do cliente.");
        }

        // CNPJ: cliente do cadastro expresso tem prioridade; os demais caem na demo do pitch.
        if (digitos.length() == 14) {
            if (recomendacaoService.localizarCadastradoPorCnpj(digitos).isPresent()) {
                return comTratamento(() ->
                        recomendacaoService.buscarDadosCompletos(digitos, temperatura, diasFeriado, fimDeSemana));
            }
            return ResponseEntity.ok(demoService.recomendarPorCnpj(digitos, temperatura, diasFeriado));
        }

        return comTratamento(() -> {
            Double temperaturaEfetiva = temperatura;
            ClimaService.Clima climaReal = null;

            // Sem temperatura simulada, usa o clima real do cliente (quando há coordenadas).
            if (temperaturaEfetiva == null) {
                Optional<Cliente> cliente = recomendacaoService.clientePorCodigo(Long.parseLong(digitos));
                if (cliente.isPresent()) {
                    climaReal = climaService.paraCliente(cliente.get());
                    temperaturaEfetiva = climaReal.temperatura();
                }
            }

            Map<String, Object> dados = recomendacaoService.buscarDadosCompletos(
                    digitos, temperaturaEfetiva, diasFeriado, fimDeSemana);

            if (climaReal != null) {
                if (dados.get("contexto") instanceof Map<?, ?> contexto) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> ctx = (Map<String, Object>) contexto;
                    ctx.put("descricao", climaReal.descricao());
                    ctx.put("cidade", climaReal.cidade());
                    ctx.put("fonte", climaReal.fonte());
                }
                dados.put("fonteClima", climaReal.fonte());
            }
            return dados;
        });
    }

    @PostMapping("/cadastro")
    public ResponseEntity<Map<String, Object>> cadastrar(@RequestBody CadastroRequest req) {
        try {
            Map<String, Object> criado = recomendacaoService.cadastrarCliente(
                    req.identificador(), req.nome(), req.porte());
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IdentificadorInvalidoException | DadosInvalidosException e) {
            return erro(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", e.getMessage());
        } catch (ConflitoException e) {
            return erro(HttpStatus.CONFLICT, "CADASTRO_EXISTENTE", e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            return erro(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                    "Não foi possível cadastrar agora. Tente novamente.");
        }
    }

    private ResponseEntity<Map<String, Object>> comTratamento(Supplier<Map<String, Object>> acao) {
        try {
            return ResponseEntity.ok(acao.get());
        } catch (IdentificadorInvalidoException e) {
            return erro(HttpStatus.BAD_REQUEST, "IDENTIFICADOR_INVALIDO", e.getMessage());
        } catch (ClienteNaoEncontradoException e) {
            return erro(HttpStatus.NOT_FOUND, "CLIENTE_NAO_ENCONTRADO", e.getMessage());
        } catch (NumberFormatException e) {
            return erro(HttpStatus.BAD_REQUEST, "IDENTIFICADOR_INVALIDO", "Código de cliente inválido.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return erro(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                    "Não foi possível consultar agora. Tente novamente.");
        }
    }

    private static String somenteDigitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    private ResponseEntity<Map<String, Object>> erro(HttpStatus status, String codigo, String mensagem) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("codigo", codigo);
        corpo.put("erro", mensagem);
        return ResponseEntity.status(status).body(corpo);
    }
}
