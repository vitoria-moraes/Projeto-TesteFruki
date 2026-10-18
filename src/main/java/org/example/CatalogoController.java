package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Catálogo completo usado pelo chat. É independente da recomendação: a opção 2
 * sempre busca esta lista, mesmo que o cliente tenha visto a opção 1 antes.
 */
@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    public Map<String, Object> listar() {
        return catalogoService.catalogoAgrupado();
    }
}
