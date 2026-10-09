package org.example;

public class HierarquiaComercial {
    private String idHierarquia;
    private String descricao;

    public HierarquiaComercial(String idHierarquia, String descricao) {
        this.idHierarquia = idHierarquia;
        this.descricao = descricao;
    }

    public String getIdHierarquia() {
        return idHierarquia;
    }

    public String getDescricao() {
        return descricao;
    }
}