package org.example;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelReaderService {

    public List<Cliente> lerClientesDoCsv() {
        List<Cliente> clientes = new ArrayList<>();
        try {
            var resource = getClass().getClassLoader().getResourceAsStream("dim_cliente.csv");
            if (resource == null) return clientes;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                String linha;
                boolean primeiraLinha = true;
                while ((linha = reader.readLine()) != null) {
                    if (primeiraLinha) { primeiraLinha = false; continue; }
                    String[] partes = linha.contains(";") ? linha.split(";", -1) : linha.split(",", -1);
                    if (partes.length < 5) continue;

                    try {
                        Cliente cliente = new Cliente();
                        cliente.setIdCliente(parseLong(campo(partes, 0)));
                        cliente.setCodCliente(parseLong(campo(partes, 1)));
                        if (cliente.getCodCliente() == null) cliente.setCodCliente(cliente.getIdCliente());
                        if (cliente.getIdCliente() == null) cliente.setIdCliente(cliente.getCodCliente());
                        if (cliente.getCodCliente() == null) continue;

                        cliente.setIdHierarquia(parseLong(campo(partes, 2)));
                        cliente.setCodCadeia(campo(partes, 3));
                        cliente.setCodSegmento(campo(partes, 4));
                        cliente.setCodSubsegmento(campo(partes, 5));
                        cliente.setCodSituacaoCredito(campo(partes, 6));
                        cliente.setIdTabelaPreco(campo(partes, 7));
                        cliente.setCodGrupoEconomico(campo(partes, 8));
                        cliente.setEstado(campo(partes, 9));
                        cliente.setSituacaoCliente(campo(partes, 10));
                        cliente.setSituacaoCredito(campo(partes, 11));
                        cliente.setDataImplantacao(parseData(campo(partes, 12)));
                        cliente.setEndereco(campo(partes, 13));
                        cliente.setBairro(campo(partes, 14));
                        cliente.setCidade(campo(partes, 15));
                        cliente.setCep(campo(partes, 16));
                        cliente.setCadeia(campo(partes, 17));
                        cliente.setSegmento(campo(partes, 18));
                        cliente.setSubsegmento(campo(partes, 19));
                        cliente.setTabelaPreco(campo(partes, 20));
                        cliente.setGrupoEconomicoDescricao(campo(partes, 21));
                        cliente.setLatitude(parseDouble(campo(partes, 22)));
                        cliente.setLongitude(parseDouble(campo(partes, 23)));
                        cliente.setNome("Cliente " + cliente.getCodCliente());

                        clientes.add(cliente);
                    } catch (Exception e) { /* Ignora linhas pontuais inválidas */ }
                }
                System.out.println("✅ Total de clientes carregados: " + clientes.size());
                if (!clientes.isEmpty()) {
                    System.out.println("💡 Exemplo de IDs de clientes válidos na base: " +
                            clientes.stream().limit(5).map(c -> c.getCodCliente().toString()).toList());
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao ler clientes: " + e.getMessage());
        }
        return clientes;
    }

    private static String campo(String[] partes, int indice) {
        if (indice >= partes.length || partes[indice] == null) return "";
        return partes[indice].trim().replaceAll("\"", "");
    }

    private static Long parseLong(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Long.parseLong(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseDouble(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return Double.parseDouble(valor.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static java.time.LocalDate parseData(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return java.time.LocalDate.parse(valor.trim());
        } catch (Exception e) {
            return null;
        }
    }

    public List<Produto> lerProdutosDoCsv() {
        List<Produto> produtos = new ArrayList<>();
        try {
            var resource = getClass().getClassLoader().getResourceAsStream("dim_item.csv");
            if (resource == null) return produtos;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                String linha;
                boolean primeiraLinha = true;
                while ((linha = reader.readLine()) != null) {
                    if (primeiraLinha) { primeiraLinha = false; continue; }
                    // Colunas de dim_item.csv: 4 = COD_ITEM, 12 = ORIGEM (tipo), 22 = COD_DESC_FAMILIA
                    String[] partes = linha.contains(";") ? linha.split(";", -1) : linha.split(",", -1);
                    if (partes.length >= 23) {
                        String codItem = partes[4].trim().replaceAll("\"", "");
                        String tipo = partes[12].trim().replaceAll("\"", "");
                        String familia = partes[22].trim().replaceAll("\"", "");
                        if (tipo.isEmpty()) continue; // sem tipo não dá para recomendar
                        produtos.add(new Produto(codItem, tipo, familia + " (cód. " + codItem + ")"));
                    }
                }
                System.out.println("✅ Total de produtos carregados: " + produtos.size());
            }
        } catch (Exception e) {
            System.out.println("Erro ao ler produtos: " + e.getMessage());
        }
        return produtos;
    }

    public List<HierarquiaComercial> lerHierarquiaDoCsv() {
        List<HierarquiaComercial> lista = new ArrayList<>();
        try {
            var resource = getClass().getClassLoader().getResourceAsStream("dim_hierarquia_comercial.csv");
            if (resource == null) return lista;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                String linha;
                boolean primeiraLinha = true;
                while ((linha = reader.readLine()) != null) {
                    if (primeiraLinha) { primeiraLinha = false; continue; }
                    String[] partes = linha.contains(";") ? linha.split(";") : linha.split(",");
                    if (partes.length >= 2) {
                        lista.add(new HierarquiaComercial(partes[0].trim(), partes[1].trim()));
                    }
                }
                System.out.println("✅ Total de registos de Hierarquia Comercial carregados: " + lista.size());
            }
        } catch (Exception e) {
            System.out.println("Erro ao ler hierarquia: " + e.getMessage());
        }
        return lista;
    }

    public List<TabelaPreco> lerTabelaPrecoDoCsv() {
        List<TabelaPreco> lista = new ArrayList<>();
        try {
            var resource = getClass().getClassLoader().getResourceAsStream("dim_tabela_preco.csv");
            if (resource == null) return lista;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                String linha;
                boolean primeiraLinha = true;
                while ((linha = reader.readLine()) != null) {
                    if (primeiraLinha) { primeiraLinha = false; continue; }
                    String[] partes = linha.contains(";") ? linha.split(";") : linha.split(",");
                    if (partes.length >= 2) {
                        lista.add(new TabelaPreco(partes[0].trim(), partes[1].trim()));
                    }
                }
                System.out.println("✅ Total de registos de Tabela de Preço carregados: " + lista.size());
            }
        } catch (Exception e) {
            System.out.println("Erro ao ler tabela de preço: " + e.getMessage());
        }
        return lista;
    }
}