package org.example;

import org.example.RecomendacaoService.ClienteNaoEncontradoException;
import org.example.RecomendacaoService.ConflitoException;
import org.example.RecomendacaoService.DadosInvalidosException;
import org.example.RecomendacaoService.IdentificadorInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Validação de acesso + recomendações + cadastro expresso.
 *
 * GET  /api/recomendacao/{idOuCnpj}   200 = acesso liberado | 400 = formato inválido | 404 = sem cadastro ativo
 *      parâmetros opcionais de demonstração: ?temperatura=35  &fimDeSemana=true  &diasFeriado=3
 * POST /api/cadastro                  { "identificador": "...", "nome": "...", "porte": "MINIMERCADO" }
 */
@RestController
@RequestMapping("/api")
public class RecomendacaoController {

    public record CadastroRequest(String identificador, String nome, String porte) {}

    private final RecomendacaoService recomendacaoService;

    public RecomendacaoController(RecomendacaoService recomendacaoService) {
        this.recomendacaoService = recomendacaoService;
    }

    @GetMapping("/recomendacao/{identificador}")
    public ResponseEntity<Map<String, Object>> consultarCliente(
            @PathVariable String identificador,
            @RequestParam(required = false) Double temperatura,
            @RequestParam(required = false) Integer diasFeriado,
            @RequestParam(required = false) Boolean fimDeSemana) {
        try {
            return ResponseEntity.ok(
                    recomendacaoService.buscarDadosCompletos(identificador, temperatura, diasFeriado, fimDeSemana));
        } catch (IdentificadorInvalidoException e) {
            return erro(HttpStatus.BAD_REQUEST, "IDENTIFICADOR_INVALIDO", e.getMessage());
        } catch (ClienteNaoEncontradoException e) {
            return erro(HttpStatus.NOT_FOUND, "CLIENTE_NAO_ENCONTRADO", e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            return erro(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO", "Não foi possível consultar agora. Tente novamente.");
        }
    }

    @PostMapping("/cadastro")
    public ResponseEntity<Map<String, Object>> cadastrar(@RequestBody CadastroRequest req) {
        try {
            Map<String, Object> criado = recomendacaoService.cadastrarCliente(req.identificador(), req.nome(), req.porte());
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IdentificadorInvalidoException | DadosInvalidosException e) {
            return erro(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", e.getMessage());
        } catch (ConflitoException e) {
            return erro(HttpStatus.CONFLICT, "CADASTRO_EXISTENTE", e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            return erro(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO", "Não foi possível cadastrar agora. Tente novamente.");
        }
    }

    private ResponseEntity<Map<String, Object>> erro(HttpStatus status, String codigo, String mensagem) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("codigo", codigo);
        corpo.put("erro", mensagem);
        return ResponseEntity.status(status).body(corpo);
    }
}