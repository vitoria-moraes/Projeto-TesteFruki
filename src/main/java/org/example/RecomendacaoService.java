package org.example;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecomendacaoService {

    // Simulação de banco em memória populado pelos CSVs ou lista estática para novos cadastros
    private final Map<Long, String> clientesCadastrados = new HashMap<>();

    // Lista de produtos base simulada dos dados carregados
    private final List<RecomendacaoDTO.ProdutoRecomendado> catalogoProdutos = List.of(
            new RecomendacaoDTO.ProdutoRecomendado("P001", "Refrigerante Fruki Guaraná 2L", 98.5, 8.99),
            new RecomendacaoDTO.ProdutoRecomendado("P002", "Refrigerante Fruki Cola 2L", 92.0, 8.49),
            new RecomendacaoDTO.ProdutoRecomendado("P003", "Água Mineral Com Gás 500ml", 88.3, 2.50),
            new RecomendacaoDTO.ProdutoRecomendado("P004", "Água Mineral Sem Gás 500ml", 85.0, 2.25),
            new RecomendacaoDTO.ProdutoRecomendado("P005", "Cítrus Fruki Lata 350ml", 79.4, 3.99)
    );

    public RecomendacaoService() {
        // Populando com o 'L' no final para especificar que são Long
        clientesCadastrados.put(110569L, "Carlos Silva");
        clientesCadastrados.put(45127L, "Mariana Souza");
        clientesCadastrados.put(149062L, "Roberto Dias");
    }

    public RecomendacaoDTO buscarOuValidarCliente(Long codCliente) {
        boolean existe = clientesCadastrados.containsKey(codCliente);
        String nome = existe ? clientesCadastrados.get(codCliente) : null;

        String msg;
        List<RecomendacaoDTO.ProdutoRecomendado> recomendados = new ArrayList<>();

        if (existe) {
            msg = "🤖 *Análise de Perfil concluída!*\nCom base no seu histórico de compras, separamos as melhores oportunidades de reposição com alta afinidade (Scoring):";
            recomendados = catalogoProdutos.stream()
                    .sorted(Comparator.comparingDouble(RecomendacaoDTO.ProdutoRecomendado::getScore).reversed())
                    .collect(Collectors.toList());
        } else {
            msg = "⚠️ *Cliente não encontrado na base de dados.*\nGostaria de realizar um cadastro rápido para aproveitar nossas ofertas com base em scoring? (Responda **SIM** para cadastrar ou digite outro ID).";
        }

        return new RecomendacaoDTO(codCliente, nome, existe, msg, recomendados);
    }

    public RecomendacaoDTO cadastrarClienteExpresso(Long codCliente, String nomeFantasia) {
        clientesCadastrados.put(codCliente, nomeFantasia);
        List<RecomendacaoDTO.ProdutoRecomendado> recomendados = catalogoProdutos;
        String msg = "🎉 *Cadastro realizado com sucesso!*\nBem-vindo à rede Fruki. Aqui estão nossos produtos em destaque com base em scoring para o seu negócio:";
        return new RecomendacaoDTO(codCliente, nomeFantasia, true, msg, recomendados);
    }
}