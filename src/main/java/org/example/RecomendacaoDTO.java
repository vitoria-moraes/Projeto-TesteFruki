package org.example;

import java.util.List;

public class RecomendacaoDTO {
    private Long codCliente;
    private String nomeCliente;
    private boolean cadastrado;
    private String mensagemWhatsApp;
    private List<ProdutoRecomendado> produtosRecomendados;

    // Construtores, Getters e Setters
    public RecomendacaoDTO() {}

    public RecomendacaoDTO(Long codCliente, String nomeCliente, boolean cadastrado, String mensagemWhatsApp, List<ProdutoRecomendado> produtosRecomendados) {
        this.codCliente = codCliente;
        this.nomeCliente = nomeCliente;
        this.cadastrado = cadastrado;
        this.mensagemWhatsApp = mensagemWhatsApp;
        this.produtosRecomendados = produtosRecomendados;
    }

    public Long getCodCliente() { return codCliente; }
    public void setCodCliente(Long codCliente) { this.codCliente = codCliente; }
    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
    public boolean isCadastrado() { return cadastrado; }
    public void setCadastrado(boolean cadastrado) { this.cadastrado = cadastrado; }
    public String getMensagemWhatsApp() { return mensagemWhatsApp; }
    public void setMensagemWhatsApp(String mensagemWhatsApp) { this.mensagemWhatsApp = mensagemWhatsApp; }
    public List<ProdutoRecomendado> getProdutosRecomendados() { return produtosRecomendados; }
    public void setProdutosRecomendados(List<ProdutoRecomendado> produtosRecomendados) { this.produtosRecomendados = produtosRecomendados; }

    public static class ProdutoRecomendado {
        private String idProduto;
        private String descricao;
        private double score;
        private double precoUnitario;

        public ProdutoRecomendado(String idProduto, String descricao, double score, double precoUnitario) {
            this.idProduto = idProduto;
            this.descricao = descricao;
            this.score = score;
            this.precoUnitario = precoUnitario;
        }

        public String getIdProduto() { return idProduto; }
        public String getDescricao() { return descricao; }
        public double getScore() { return score; }
        public double getPrecoUnitario() { return precoUnitario; }
    }
}