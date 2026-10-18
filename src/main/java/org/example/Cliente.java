package org.example;

import java.time.LocalDate;

/**
 * Mapeamento da tabela {@code dim_cliente} (base original da Fruki).
 *
 * <p>Esta classe ainda não é uma entidade JPA: o protótipo do pitch não sobe banco.
 * Quando o PostgreSQL entrar (pós-pitch), basta adicionar {@code @Entity},
 * {@code @Table(name = "dim_cliente")} e as anotações de coluna. Os nomes dos
 * campos já seguem as colunas reais do CSV.</p>
 *
 * <p>Observação: {@code nome} não existe na base original (os nomes estão
 * anonimizados). Ele é derivado de {@code cod_cliente} apenas para exibição.</p>
 */
public class Cliente {

    // Colunas de dim_cliente.csv, na ordem do arquivo.
    private Long idCliente;                 // id_cliente
    private Long codCliente;                // COD_CLIENTE
    private Long idHierarquia;              // id_hierarquia
    private String codCadeia;               // cod_cadeia
    private String codSegmento;             // cod_segmento
    private String codSubsegmento;          // COD_SUBSEGMENTO
    private String codSituacaoCredito;      // COD_SITUACAO_CREDITO
    private String idTabelaPreco;           // ID_TABELA_PRECO
    private String codGrupoEconomico;       // COD_GRUPO_ECONOMICO
    private String estado;                  // ESTADO
    private String situacaoCliente;         // situacao_cliente
    private String situacaoCredito;         // SITUACAO_CREDITO
    private LocalDate dataImplantacao;      // DATA_IMPLANTACAO
    private String endereco;                // ENDERECO
    private String bairro;                  // bairro
    private String cidade;                  // CIDADE
    private String cep;                     // CEP
    private String cadeia;                  // cadeia
    private String segmento;                // segmento
    private String subsegmento;             // SUBSEGMENTO
    private String tabelaPreco;             // TABELA_PRECO
    private String grupoEconomicoDescricao; // GRUPO_ECONOMICO_DESCRICAO
    private Double latitude;                // LATITUDE
    private Double longitude;               // LONGITUDE

    // Campo de negócio: não existe na base recebida, mas existirá no cadastro real.
    private String cnpj;

    // Nome de exibição derivado (não é coluna).
    private String nome;

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

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }

    public Long getCodCliente() { return codCliente; }
    public void setCodCliente(Long codCliente) { this.codCliente = codCliente; }

    public Long getIdHierarquia() { return idHierarquia; }
    public void setIdHierarquia(Long idHierarquia) { this.idHierarquia = idHierarquia; }

    public String getCodCadeia() { return codCadeia; }
    public void setCodCadeia(String codCadeia) { this.codCadeia = codCadeia; }

    public String getCodSegmento() { return codSegmento; }
    public void setCodSegmento(String codSegmento) { this.codSegmento = codSegmento; }

    public String getCodSubsegmento() { return codSubsegmento; }
    public void setCodSubsegmento(String codSubsegmento) { this.codSubsegmento = codSubsegmento; }

    public String getCodSituacaoCredito() { return codSituacaoCredito; }
    public void setCodSituacaoCredito(String codSituacaoCredito) { this.codSituacaoCredito = codSituacaoCredito; }

    public String getIdTabelaPreco() { return idTabelaPreco; }
    public void setIdTabelaPreco(String idTabelaPreco) { this.idTabelaPreco = idTabelaPreco; }

    public String getCodGrupoEconomico() { return codGrupoEconomico; }
    public void setCodGrupoEconomico(String codGrupoEconomico) { this.codGrupoEconomico = codGrupoEconomico; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getSituacaoCliente() { return situacaoCliente; }
    public void setSituacaoCliente(String situacaoCliente) { this.situacaoCliente = situacaoCliente; }

    public String getSituacaoCredito() { return situacaoCredito; }
    public void setSituacaoCredito(String situacaoCredito) { this.situacaoCredito = situacaoCredito; }

    public LocalDate getDataImplantacao() { return dataImplantacao; }
    public void setDataImplantacao(LocalDate dataImplantacao) { this.dataImplantacao = dataImplantacao; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getCadeia() { return cadeia; }
    public void setCadeia(String cadeia) { this.cadeia = cadeia; }

    public String getSegmento() { return segmento; }
    public void setSegmento(String segmento) { this.segmento = segmento; }

    public String getSubsegmento() { return subsegmento; }
    public void setSubsegmento(String subsegmento) { this.subsegmento = subsegmento; }

    public String getTabelaPreco() { return tabelaPreco; }
    public void setTabelaPreco(String tabelaPreco) { this.tabelaPreco = tabelaPreco; }

    public String getGrupoEconomicoDescricao() { return grupoEconomicoDescricao; }
    public void setGrupoEconomicoDescricao(String grupoEconomicoDescricao) { this.grupoEconomicoDescricao = grupoEconomicoDescricao; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
