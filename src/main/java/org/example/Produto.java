package org.example;

// Produto.java
public class Produto {
    private String codItem;
    private String tipo; // Ex: REFRIGERANTE, ENERGETICO, CERVEJA
    private String descricao;

    public Produto(String codItem, String tipo, String descricao) {
        this.codItem = codItem;
        this.tipo = tipo;
        this.descricao = descricao;
    }

    public String getCodItem() { return codItem; }
    public String getTipo() { return tipo; }
    public String getDescricao() { return descricao; }
}
