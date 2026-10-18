# Pede Fruki — protótipo B2B

Protótipo Spring Boot do canal de pedidos e recomendação B2B da Fruki.
A entrega atual é focada no **pitch**: a API responde uma requisição válida com
recomendações mockadas e um catálogo comercial de 31 SKUs, sem depender de banco.

## Documentação

- [Relatório de mudanças](docs/RELATORIO-MUDANCAS.md) — o que mudou do estado inicial até aqui.
- [Guia do PO](docs/GUIA-PO.md) — como usar e apresentar a demo.

## Como rodar

Pré-requisitos: **JDK 21** e Maven (ou o Maven embutido no IntelliJ).

```bash
mvn spring-boot:run
```

Aplicação: <http://localhost:8081/chat>

> No terminal desta máquina o `java` do PATH é o Java 8. No IntelliJ, aponte o
> projeto para `C:\Users\Riordan Lucas\.jdks\ms-21.0.12.1` (ou outro JDK 21).

## Duas interfaces

Depois da integração com o commit de sazonalidades/pedidos, o projeto tem duas
telas servidas pelo mesmo backend:

- `/chat` — interface do pitch: 3 grupos × 3 opções, catálogo de 31 SKUs,
  atalho de CNPJ demo e clima real.
- `/chat-remoto` — interface nova (login por ID/CNPJ, cadastro expresso,
  carrinho, cenários de clima e fechamento de pedido via `POST /api/pedidos`).

As duas usam o mesmo catálogo e os mesmos endpoints; a diferença é a
apresentação.

## Fluxo do chat

1. Informe um **CNPJ de 14 dígitos** (ex.: `12345678000190`) ou clique no botão
   **Usar CNPJ demo** que aparece no início do chat.
2. **Opção 1 — Recomendações:** mostra 3 grupos com 3 opções cada, com o motivo
   (clima + eventos simulados) e a quantidade sugerida (3 caixas).
3. **Opção 2 — Catálogo completo:** mostra os 31 SKUs agrupados por categoria,
   independentemente de a opção 1 ter sido usada antes. Itens recomendados
   aparecem com o selo **IA** e a quantidade sugerida.

## API

### Recomendação por CNPJ

```http
GET /api/recomendacao/12345678000190
GET /api/recomendacao?cnpj=12.345.678/0001-90
```

- CNPJ cadastrado na demo → recomendação contextual (3 grupos × 3 itens).
- CNPJ desconhecido → seleção fixa de 3 produtos (Água, Guaraná e Energético),
  que é o comportamento pedido pelo PO.
- Não consulta banco.

Exemplo (resumido):

```json
{
  "cnpj": "12345678000190",
  "modo": "mock-perfil",
  "contexto": {
    "temperatura": 16.7,
    "descricao": "nublado",
    "cidade": "Erechim",
    "fonte": "OpenWeatherMap",
    "eventos": [
      "Festa política na cidade (evento simulado)",
      "Jogo Corinthians x Vitória (evento simulado)"
    ],
    "dadosFicticios": true
  },
  "resumoIa": "Contexto: 16,7 °C em Erechim...",
  "gruposRecomendacao": [
    {
      "titulo": "Refrigerantes para o movimento",
      "motivo": "Festa política ontem + jogo hoje...",
      "quantidadeSugerida": 3,
      "itens": [
        { "codigo": "REFRI-COLA-2L", "descricao": "Fruki Cola 2L", "score": 96 }
      ]
    }
  ]
}
```

### Catálogo completo

```http
GET /api/catalogo
```

Retorna `total`, `categorias` (Águas, Refrigerantes, Sucos, Energéticos,
Cervejas, Tônicas) e `itens`. É a fonte da opção 2 do chat.

### CNPJs de demonstração

```http
GET /api/demo/cnpjs
```

| CNPJ | Cidade | Perfil | Montagem |
|---|---|---|---|
| `12345678000190` | Erechim | Mercadinho | 3 refris + 3 cervejas + 3 águas |
| `98765432000110` | Porto Alegre | Supermercado | 3 refris + 3 cervejas + 3 águas |
| `11222333000144` | Caxias do Sul | Adega | 3 cervejas + 3 energéticos + 3 águas |
| `55666777000188` | Santa Cruz do Sul | Padaria | 3 sucos + 3 refris + 3 águas |

Os eventos são **simulados** e aparecem identificados no JSON. O clima vem da
OpenWeatherMap quando há coordenadas.

### Rota legada — código do cliente

```http
GET /api/recomendacao/{codCliente}
```

Ex.: `GET /api/recomendacao/110569`. Continua usando o motor de scoring atual,
devolve `404` para cliente inexistente e usa o clima real quando o cliente tem
latitude/longitude em `dim_cliente`.

### Cadastro expresso e pedidos (integrados do commit remoto)

```http
POST /api/cadastro
POST /api/pedidos
```

- `POST /api/cadastro` cria um cliente em memória (`identificador`, `nome`,
  `porte`) e passa a aceitá-lo na consulta por CNPJ/código.
- `POST /api/pedidos` valida o cliente e os itens, calcula o total por **caixa**
  no servidor e devolve o número do pedido e a previsão de entrega. Aceita
  `BOLETO`, `PIX` (5% de desconto) e `CARTAO`.
- Nenhum dos dois persiste em banco: é demonstração.

## Clima (OpenWeatherMap)

- Chave em `application.properties` via `OPENWEATHER_API_KEY` (padrão de protótipo).
- Timeout de 1,5 s, cache de 10 min e **fallback** de 24 °C: se a internet falhar
  durante o pitch, a resposta continua de pé (`fonte: "fallback"`).
- Em produção: rotacionar a chave e removê-la do repositório.

## Catálogo

A base original tem 263 códigos, mas muitos são variações de embalagem com nome
e marca anonimizados. Para o chat ficar legível, o catálogo de demonstração tem
**31 SKUs comerciais representativos** com nome, categoria, preço e imagem reais
do repositório. A lista completa pode ser trocada por um de-para oficial depois.

## Estrutura

```
src/main/java/org/example/
  RecomendacaoController.java     -> recomendação (CNPJ/código) + cadastro
  CatalogoController.java         -> GET /api/catalogo
  DemoController.java             -> GET /api/demo/cnpjs
  PedidoController.java           -> POST /api/pedidos
  PedidoService.java              -> fechamento de pedido em memória
  RecomendacaoDemoService.java    -> montagem da recomendação por CNPJ (3x3)
  DemoCnpjDirectory.java          -> CNPJs, cidades, eventos e grupos
  CatalogoService.java            -> catálogo de 31 SKUs (fonte única)
  ClimaService.java               -> OpenWeatherMap + cache + fallback
  Cliente.java                    -> mapeamento de dim_cliente (sem JPA ainda)
  ExcelReaderService.java         -> leitura dos CSVs
  RecomendacaoService.java        -> scoring + sazonalidade (rota legada)
src/main/resources/
  dim_*.csv                       -> bases originais
  templates/chat.html             -> interface do pitch (3 grupos x 3)
  templates/chat-remoto.html      -> interface com login/cadastro/pedidos
  static/images/                  -> fotos de produto e marca
```

## Próximos passos (pós-pitch)

1. Subir PostgreSQL via Docker e mapear `Cliente` como entidade JPA.
2. Criar a tabela fato de compras e substituir o histórico sintético por RFM real.
3. Persistir pedidos e permitir ingestão de novos clientes/produtos/compras.
4. Trocar o catálogo de demonstração pelo catálogo oficial com de-para de SKU.
