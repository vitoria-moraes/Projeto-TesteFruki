package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ajuda a equipe a lembrar quais CNPJs têm montagens diferentes na demo.
 * Não é usado pelo chat; serve para teste/apresentação.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final DemoCnpjDirectory directory;

    public DemoController(DemoCnpjDirectory directory) {
        this.directory = directory;
    }

    @GetMapping("/cnpjs")
    public List<Map<String, Object>> listarCnpjs() {
        return directory.listar().stream().map(perfil -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("cnpj", perfil.cnpj());
            item.put("nome", perfil.nome());
            item.put("cidade", perfil.cidade());
            item.put("perfilMercado", perfil.perfilMercado());
            item.put("eventos", perfil.eventos());
            item.put("grupos", perfil.grupos().size());
            return item;
        }).toList();
    }
}
