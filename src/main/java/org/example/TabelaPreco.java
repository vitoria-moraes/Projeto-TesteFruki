package org.example;

public class TabelaPreco {
    private String idTabelaPreco;
    private String descricao;

    public TabelaPreco(String idTabelaPreco, String descricao) {
        this.idTabelaPreco = idTabelaPreco;
        this.descricao = descricao;
    }

    public String getIdTabelaPreco() {
        return idTabelaPreco;
    }

    public String getDescricao() {
        return descricao;
    }
}