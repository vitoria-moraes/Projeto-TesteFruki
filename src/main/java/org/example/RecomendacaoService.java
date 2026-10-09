package org.example;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecomendacaoService {

    private final ExcelReaderService readerService;

    public RecomendacaoService(ExcelReaderService readerService) {
        this.readerService = readerService;
    }

    public RecomendacaoDTO gerarRecomendacaoParaCliente(Long codCliente) {
        List<Cliente> clientes = readerService.lerClientesDoCsv();
        List<Produto> produtos = readerService.lerProdutosDoCsv();

        Cliente clienteEncontrado = clientes.stream()
                .filter(c -> c.getCodCliente().equals(codCliente))
                .findFirst()
                .orElse(null);

        if (clienteEncontrado == null) {
            return new RecomendacaoDTO(codCliente, "Cliente não encontrado na base de dados.", List.of());
        }

        List<String> sugestoesProdutos = produtos.stream()
                .limit(3)
                .map(Produto::getDescricao)
                .collect(Collectors.toList());

        String mensagemWhatsApp = "Olá! Notámos que o seu estabelecimento (" + clienteEncontrado.getCodSegmento() +
                ") pode beneficiar dos seguintes produtos em destaque:\n" +
                String.join("\n", sugestoesProdutos.stream().map(p -> "• " + p).toList()) +
                "\nGostaria de adicionar algum destes itens ao seu pedido?";

        return new RecomendacaoDTO(codCliente, mensagemWhatsApp, sugestoesProdutos);
    }
}