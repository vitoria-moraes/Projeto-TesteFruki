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

            // Colunas: 0=id_cliente, 1=COD_CLIENTE, 4=cod_segmento, 10=situacao_cliente
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                String linha;
                boolean primeiraLinha = true;
                while ((linha = reader.readLine()) != null) {
                    if (primeiraLinha) { primeiraLinha = false; continue; }
                    String[] partes = linha.contains(";") ? linha.split(";", -1) : linha.split(",", -1);
                    if (partes.length >= 11) {
                        try {
                            String situacao = partes[10].trim().replaceAll("\"", "");
                            if (!situacao.equalsIgnoreCase("Ativo")) continue; // só clientes com cadastro ativo

                            Long idCliente = Long.parseLong(partes[0].trim().replaceAll("\"", ""));
                            Long codCliente = Long.parseLong(partes[1].trim().replaceAll("\"", ""));
                            String codSegmento = partes[4].trim().replaceAll("\"", "");

                            clientes.add(new Cliente(codCliente, idCliente, "Cliente " + codCliente, codSegmento));
                        } catch (Exception e) { /* Ignora erros pontuais */ }
                    }
                }
                System.out.println("✅ Total de clientes ATIVOS carregados: " + clientes.size());
                if (!clientes.isEmpty()) {
                    System.out.println("💡 Exemplos de clientes ativos (COD_CLIENTE / id_cliente): " +
                            clientes.stream().limit(5).map(c -> c.getCodCliente() + " / " + c.getIdCliente()).toList());
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao ler clientes: " + e.getMessage());
        }
        return clientes;
    }

    public List<Produto> lerProdutosDoCsv() {
        List<Produto> lista = new ArrayList<>();
        try {
            var resource = getClass().getClassLoader().getResourceAsStream("dim_item.csv");
            if (resource == null) return lista;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                String linha;
                boolean primeiraLinha = true;
                while ((linha = reader.readLine()) != null) {
                    if (primeiraLinha) { primeiraLinha = false; continue; }
                    String[] partes = linha.contains(";") ? linha.split(";") : linha.split(",");
                    if (partes.length >= 2) {
                        String codItem = partes[0].trim().replaceAll("\"", "");
                        String descricao = partes[1].trim().replaceAll("\"", "");
                        lista.add(new Produto(codItem, "Geral", descricao));
                    }
                }
                System.out.println("✅ Total de produtos carregados: " + lista.size());
            }
        } catch (Exception e) {
            System.out.println("Erro ao ler produtos: " + e.getMessage());
        }
        return lista;
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