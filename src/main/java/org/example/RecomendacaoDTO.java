package org.example;

import java.util.List;

public class RecomendacaoDTO {
    private Long codCliente;
    private String mensagemWhatsApp;
    private List<String> produtosRecomendados;

    public RecomendacaoDTO(Long codCliente, String mensagemWhatsApp, List<String> produtosRecomendados) {
        this.codCliente = codCliente;
        this.mensagemWhatsApp = mensagemWhatsApp;
        this.produtosRecomendados = produtosRecomendados;
    }

    public Long getCodCliente() {
        return codCliente;
    }

    public String getMensagemWhatsApp() {
        return mensagemWhatsApp;
    }

    public List<String> getProdutosRecomendados() {
        return produtosRecomendados;
    }
}