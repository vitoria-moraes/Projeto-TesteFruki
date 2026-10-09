package org.example;

public class Cliente {
    private Long codCliente;
    private String nome;
    private String codSegmento;
    private String cnpj;

    // Construtor com 4 argumentos
    public Cliente(Long codCliente, String nome, String codSegmento, String cnpj) {
        this.codCliente = codCliente;
        this.nome = nome;
        this.codSegmento = codSegmento;
        this.cnpj = cnpj;
    }

    // Construtor alternativo com 3 argumentos (para compatibilidade caso algum sítio ainda use)
    public Cliente(Long codCliente, String nome, String codSegmento) {
        this.codCliente = codCliente;
        this.nome = nome;
        this.codSegmento = codSegmento;
        this.cnpj = "";
    }

    public Long getCodCliente() { return codCliente; }
    public String getNome() { return nome; }
    public String getCodSegmento() { return codSegmento; }
    public String getCnpj() { return cnpj; }
}