# API Java V1 | Pede Fruki (B2B)

API REST em Spring Boot que recebe a consulta do chat (WhatsApp simulado), valida o estabelecimento, devolve o ranking de produtos e recebe o pedido.
URL base (padrão do Spring Boot): `http://localhost:8080`

| # | Método e rota | Para que serve | Respostas |
|---|---|---|---|
| 1 | `GET /api/status` | Confirma que a API está no ar | 200 |
| 2 | `GET /api/recomendacao/{idOuCnpj}` | Valida o ID/CNPJ e devolve o ranking de produtos, o porte, o contexto e o catálogo | 200, 400, 404 |
| 3 | `POST /api/cadastro` | Cadastro expresso de um estabelecimento novo | 201, 400, 409 |
| 4 | `POST /api/pedidos` | Fecha o pedido (calcula totais, aplica pagamento, gera o número) | 201, 400, 404 |

Parâmetros opcionais de demonstração no endpoint 2: `temperatura` (ex.: 35), `fimDeSemana` (true/false) e `diasFeriado` (ex.: 3). Eles forçam o clima e a época usados pelo motor.

## 1. GET /api/status

```
GET /api/status
```
```json
{
  "status": "ok",
  "api": "Pede Fruki - Recomendação B2B",
  "versao": "v1",
  "produtosNoCatalogo": 6,
  "horario": "2026-10-19T00:02:02"
}
```

## 2. GET /api/recomendacao/{idOuCnpj}

Aceita o `COD_CLIENTE`, o `id_cliente` da base original ou um CNPJ (com ou sem pontuação). Só clientes **ativos** são liberados.

```
GET /api/recomendacao/194643?temperatura=35&fimDeSemana=false
```
Resposta **200** (resumida; o campo `produtosRecomendados` já vem ordenado do maior para o menor score):
```json
{
  "codCliente": 194643,
  "nomeCliente": "Cliente 194643",
  "perfilMercado": "Adega",
  "porte": "BAR_RESTAURANTE",
  "porteLabel": "Bar / Restaurante",
  "novoCliente": false,
  "contexto": {
    "temperatura": 35.0,
    "estacao": "Primavera",
    "fimDeSemana": false,
    "feriado": "Finados",
    "diasParaFeriado": 14,
    "resumo": "Primavera, 35°C, Finados em 14 dias",
    "dadosFicticios": true
  },
  "produtosRecomendados": [
    {
      "codigo": "905",
      "descricao": "Cerveja Bellavista Puro Malte",
      "categoria": "CERVEJA",
      "preco": 66.0,
      "precoUnitario": 5.5,
      "unidadesPorCaixa": 12,
      "embalagem": "Cx c/ 12 un",
      "imagemUrl": "/images/Cervejas fruki/bellavista-premium.webp",
      "score": 80,
      "motivos": [
        "Item de alta saída no seu histórico",
        "Forte saída em adega",
        "Calor de 35°C aumenta a procura",
        "Finados em 14 dias"
      ],
      "motivo": "Item de alta saída no seu histórico • Forte saída em adega",
      "qtdSugeridaCaixas": 5,
      "justificativaVolume": "Volume ajustado ao porte (Bar / Restaurante) com reforço sazonal"
    },
    "... (mais 3 produtos, ordenados por score)"
  ],
  "catalogoCompleto": [
    {
      "codigo": "901",
      "descricao": "Fruki Guaraná 2L",
      "categoria": "REFRIGERANTE",
      "preco": 45.0,
      "precoUnitario": 7.5,
      "unidadesPorCaixa": 6,
      "embalagem": "Cx c/ 6 un",
      "imagemUrl": "/images/Refri fruki/guarana1-5l.webp"
    },
    "... (demais produtos do catálogo)"
  ]
}
```

Erros:

| Situação | Status | Corpo |
|---|---|---|
| ID inexistente ou inativo (ex.: `999999999`) | 404 | `{"codigo": "CLIENTE_NAO_ENCONTRADO", "erro": "Não encontramos um cadastro ativo para o ID/CNPJ informado."}` |
| Formato inválido (ex.: CNPJ com dígitos errados) | 400 | `{"codigo": "IDENTIFICADOR_INVALIDO", "erro": "Esse CNPJ não parece válido. Confira os 14 dígitos e tente novamente."}` |

## 3. POST /api/cadastro

```
POST /api/cadastro
Content-Type: application/json

{ "identificador": "11.222.333/0001-81", "nome": "Bar do Zé", "porte": "BAR_RESTAURANTE" }
```
`porte`: `MINIMERCADO`, `BAR_RESTAURANTE` ou `GRANDE_DISTRIBUIDOR`. Resposta **201**:
```json
{
  "codCliente": 900001,
  "nomeCliente": "Bar do Zé",
  "porte": "BAR_RESTAURANTE",
  "porteLabel": "Bar / Restaurante"
}
```
Depois do cadastro, o mesmo CNPJ já é aceito no endpoint 2. Se o identificador já existir, a resposta é **409**.

## 4. POST /api/pedidos

```
POST /api/pedidos
Content-Type: application/json

{ "codCliente": 194643, "pagamento": "PIX",
   "itens": [ { "codigo": "902", "quantidade": 3 }, { "codigo": "903", "quantidade": 2 } ] }
```
`pagamento`: `BOLETO` (30 dias), `PIX` (5% de desconto) ou `CARTAO`. A quantidade é em **caixas**, e os totais são calculados no servidor. Resposta **201**:
```json
{
  "numeroPedido": "PF-2026-001001",
  "codCliente": 194643,
  "condicaoPagamento": "PIX à vista (5% de desconto)",
  "itens": [
    {
      "codigo": "902",
      "descricao": "Fruki Cola 2L",
      "quantidade": 3,
      "precoCaixa": 46.8,
      "subtotal": 140.4
    },
    {
      "codigo": "903",
      "descricao": "Água da Pedra 500ml",
      "quantidade": 2,
      "precoCaixa": 24.0,
      "subtotal": 48.0
    }
  ],
  "subtotal": 188.4,
  "desconto": 9.42,
  "total": 178.98,
  "previsaoEntrega": "20/10/2026"
}
```

## Como a consulta flui

```
chat.html  ──GET /api/recomendacao/{id}──►  RecomendacaoController
                                                  │
                                    RecomendacaoService (valida ID/CNPJ,
                                    scoring: histórico + perfil + clima/época,
                                    volume por porte)
                                                  │
chat.html  ◄────────── JSON do ranking ───────────┘
chat.html  ──POST /api/pedidos──►  PedidoController ──► PedidoService ──► número do pedido
```

Observações: histórico de compras, clima e porte são **fictícios**. Cadastros e pedidos ficam em memória e somem quando a aplicação reinicia.
