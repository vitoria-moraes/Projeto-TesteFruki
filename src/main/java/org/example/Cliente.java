package org.example;

public class Cliente {
    private Long codCliente;   // COD_CLIENTE (código do cliente)
    private Long idCliente;    // id_cliente (id interno da base)
    private String nome;
    private String codSegmento;
    private String cnpj;

    // Construtor vazio obrigatório para instanciação direta
    public Cliente() {
        this.cnpj = "";
    }

    public Cliente(Long codCliente, Long idCliente, String nome, String codSegmento) {
        this.codCliente = codCliente;
        this.idCliente = idCliente;
        this.nome = nome;
        this.codSegmento = codSegmento;
        this.cnpj = "";
    }

    public Cliente(Long codCliente, String nome, String codSegmento, String cnpj) {
        this.codCliente = codCliente;
        this.idCliente = codCliente;
        this.nome = nome;
        this.codSegmento = codSegmento;
        this.cnpj = cnpj;
    }

    // Getters
    public Long getCodCliente() { return codCliente; }
    public Long getIdCliente() { return idCliente; }
    public String getNome() { return nome; }
    public String getCodSegmento() { return codSegmento; }
    public String getCnpj() { return cnpj; }

    // Setters
    public void setCodCliente(Long codCliente) { this.codCliente = codCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    public void setNome(String nome) { this.nome = nome; }
    public void setCodSegmento(String codSegmento) { this.codSegmento = codSegmento; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
}