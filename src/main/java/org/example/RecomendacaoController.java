package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RecomendacaoController {

    @Autowired
    private RecomendacaoService recomendacaoService;

    @GetMapping("/recomendacao/{id}")
    public ResponseEntity<RecomendacaoDTO> consultarCliente(@PathVariable Long id) {
        RecomendacaoDTO dto = recomendacaoService.buscarOuValidarCliente(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/cadastro-rapido")
    public ResponseEntity<RecomendacaoDTO> cadastroRapido(@RequestParam Long codCliente, @RequestParam String nome) {
        RecomendacaoDTO dto = recomendacaoService.cadastrarClienteExpresso(codCliente, nome);
        return ResponseEntity.ok(dto);
    }
}