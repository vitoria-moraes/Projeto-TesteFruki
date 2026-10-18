# Relatório de mudanças — do estado inicial ao protótipo atual

**Projeto:** Pede Fruki B2B  
**Data:** 18/10/2026  
**Objetivo:** registrar o que mudou no protótipo desde o estado inicial, com foco no
que foi entregue para o pitch e no que ainda é mock.

> **Importante:** este trabalho foi commitado e integrado ao commit remoto
> `5b556b5` (sazonalidades no motor + pedidos) por meio de um merge local. O
> último commit antes desta entrega era `46e5b94 Implementando catalogo`.

---

## 1. Resumo executivo

O protótipo saiu de um chat que só funcionava com código numérico e um catálogo
de 6 itens para uma demonstração com:

- entrada por **CNPJ** (14 dígitos) ou código de cliente;
- **4 perfis de CNPJ** com cidades, eventos e montagens diferentes;
- recomendação em **3 grupos × 3 opções**, com **quantidade sugerida** e o
  **motivo** (clima + eventos simulados);
- **catálogo completo de 31 SKUs** em 6 categorias, independente da recomendação;
- **clima real** via OpenWeatherMap, com timeout, cache e fallback;
- correção de bugs visuais (imagem do energético, avatar, placeholder externo);
- README e documentação para o time.

A decisão de **não usar PostgreSQL/Docker/JPA** foi mantida, conforme orientação
do PO para o pitch. A rota antiga por código de cliente continua funcionando.

---

## 2. Antes × depois

| Área | Antes | Agora |
|---|---|---|
| Identificação | Código numérico (ID) | CNPJ de 14 dígitos ou código numérico |
| Recomendação | 4 itens gerados com histórico aleatório | Demo: 3 grupos × 3 itens, com motivo e quantidade; legado: 4 itens |
| Catálogo | 3 itens no mock / 6 no motor antigo | 31 SKUs comerciais, 6 categorias, independente da IA |
| Clima | 30 °C fixo | OpenWeatherMap real, cache 10 min, fallback 24 °C |
| Perfil de mercado | Hash do código do segmento | Perfil explícito por CNPJ demo (Mercadinho, Supermercado, Adega, Padaria) |
| Preço | Fixo no código | Catálogo demo com preço por SKU (ainda não é a tabela real) |
| ID inexistente | Retornava 200 com cliente fictício | Retorna 404 |
| Imagem do energético | Caminho errado (`elev_...`) | Corrigido (`eleve_...`) |
| Avatar/perfil | `<div>` com texto “PF” | Logo SVG local (`static/images/brand/`) |
| Fallback de imagem | `via.placeholder.com` (externo) | SVG local (`produto-placeholder.svg`) |
| Modelo de cliente | 5 campos | 24 colunas de `dim_cliente` + nome/cnpj (sem JPA) |
| Erros no chat | “Problema de conexão” genérico | Mensagem específica (400/404/erro da API) |
| Segurança do chat | `innerHTML` com texto do usuário | Texto escapado antes de renderizar |
| Documentação | Nenhuma | `README.md` + `docs/` |
| Testes | Nenhum | Validação manual de API, JS e imagens (sem testes automatizados) |

---

## 3. O que foi construído

### 3.1 API de recomendação por CNPJ

**Rota principal:**

```http
GET /api/recomendacao/12345678000190
GET /api/recomendacao?cnpj=12.345.678/0001-90
```

Comportamento:

- **CNPJ de 14 dígitos cadastrado na demo** → recomendação contextual
  (`modo: "mock-perfil"`), com 3 grupos × 3 itens.
- **CNPJ desconhecido** → seleção fixa de 3 produtos
  (`modo: "mock-padrao"`): Água da Pedra 500ml, Fruki Guaraná 2L e
  Energético Traz Elev 473ml. É o comportamento pedido pelo PO.
- **Código numérico** (ex.: `110569`) → rota legada, com o motor de scoring
  atual e clima real quando o cliente tem latitude/longitude.
- **Documento inválido** → 400; **cliente inexistente** → 404.

A variante com `?cnpj=` existe porque um CNPJ formatado tem `/`, que não cabe em
um único segmento de URL.

### 3.2 Perfis de CNPJ demo

| CNPJ | Cidade | Perfil | Montagem |
|---|---|---|---|
| `12345678000190` | Erechim | Mercadinho | 3 refrigerantes + 3 cervejas + 3 águas |
| `98765432000110` | Porto Alegre | Supermercado | 3 refrigerantes + 3 cervejas + 3 águas |
| `11222333000144` | Caxias do Sul | Adega | 3 cervejas + 3 energéticos + 3 águas |
| `55666777000188` | Santa Cruz do Sul | Padaria | 3 sucos + 3 refrigerantes + 3 águas |

- Cada perfil tem coordenadas próprias e consulta o clima da cidade.
- Os eventos são **simulados** e vêm identificados no JSON como
  `"evento simulado"`.
- `GET /api/demo/cnpjs` lista os perfis para teste.

### 3.3 Recomendação contextual (demo)

Cada grupo tem:

- `titulo` (ex.: “Cervejas para o jogo”);
- `motivo` (clima + evento + perfil);
- `quantidadeSugerida` (3);
- `itens` (3 produtos, cada um com `quantidadeSugerida`, `motivo`, `score`,
  `imagemUrl`, `preco`, `embalagem`).

O resumo (`resumoIa`) é montado dinamicamente:

- calor (≥ 28 °C): “Calor: água e refrigerante ganham peso”;
- frio (≤ 12 °C): “Frio: o foco é o evento e o jogo”;
- ameno: “Tempo ameno: foco no evento e no jogo”.

### 3.4 Catálogo completo

**Rota:** `GET /api/catalogo`

- **31 SKUs** comerciais, com nome, categoria, preço, embalagem e imagem.
- **6 categorias:** Águas (6), Refrigerantes (6), Sucos (5), Energéticos (5),
  Cervejas (5), Tônicas (4).
- A opção 2 do chat busca essa rota **sempre**, mesmo depois da opção 1.
- Itens que estão na recomendação recebem selo **IA** e a quantidade sugerida.

**Por que 31 e não 263?** A base bruta tem 263 códigos, mas muitos são variações
de embalagem com nome/marca anonimizados. Os 31 são os SKUs representativos com
nome, imagem e preço legíveis. A lista bruta pode ser exposta depois, mas não é
recomendada para o pitch.

### 3.5 Clima real

- OpenWeatherMap, endpoint `data/2.5/weather`.
- **Timeout:** 1,5 s.
- **Cache:** 10 min por coordenada.
- **Fallback:** 24 °C com `fonte: "fallback"` se a internet/chave falharem.
- Chave configurável por `OPENWEATHER_API_KEY`; o valor padrão está no
  `application.properties` apenas para o protótipo.
- A rota legada usa latitude/longitude de `dim_cliente` quando existem.

### 3.6 Modelo de cliente

`Cliente.java` passou a mapear as 24 colunas de `dim_cliente`:

- `id_cliente`, `COD_CLIENTE`, `id_hierarquia`, `cod_cadeia`, `cod_segmento`,
  `COD_SUBSEGMENTO`, `COD_SITUACAO_CREDITO`, `ID_TABELA_PRECO`,
  `COD_GRUPO_ECONOMICO`, `ESTADO`, `situacao_cliente`, `SITUACAO_CREDITO`,
  `DATA_IMPLANTACAO`, endereço/bairro/cidade/CEP, cadeia/segmento/subsegmento,
  tabela de preço, grupo econômico, latitude e longitude.
- `nome` e `cnpj` são campos de apoio (não existem na base original).
- **Ainda não é entidade JPA.** O Javadoc explica como anotar quando o banco entrar.

### 3.7 Correções no chat

- Entrada aceita CNPJ com ou sem máscara (o front remove não dígitos).
- Exibe cidade, temperatura e descrição do clima na boas-vindas.
- Atalho **“Usar CNPJ demo 12.345.678/0001-90 (Erechim/RS)”**.
- Texto do usuário é escapado antes de ir para a tela.
- Menu passou a comparar a opção exata (antes “100 caixas” caía na opção 1).
- Estado de `PAGAMENTO` não chama mais `finalizarPedido()` inexistente.
- Quantidade do carrinho atualiza **todas** as ocorrências do mesmo produto
  (recomendação + catálogo), sem IDs duplicados.
- Avatar e banner usam logo SVG local.
- Imagem quebrada do energético corrigida.
- Fallback de imagem local, sem `via.placeholder.com`.
- `white-space` ajustado para os cards não ficarem espaçados.
- `meta viewport` para telas menores.

### 3.8 Infraestrutura e documentação

- `pom.xml`: `-parameters` explícito no compilador.
- `README.md` com fluxo, endpoints, CNPJs e como rodar.
- `docs/RELATORIO-MUDANCAS.md` (este arquivo) e `docs/GUIA-PO.md`.

---

## 4. Endpoints

| Método | Rota | Para quê |
|---|---|---|
| GET | `/chat` | Interface do chat |
| GET | `/api/recomendacao/{cnpj}` | Recomendação por CNPJ (14 dígitos) |
| GET | `/api/recomendacao?cnpj=...` | CNPJ formatado |
| GET | `/api/recomendacao/{codCliente}` | Rota legada por código numérico |
| GET | `/api/catalogo` | Catálogo completo (31 SKUs) |
| GET | `/api/demo/cnpjs` | Lista os CNPJs de demonstração |

### Formato da resposta (demo)

```json
{
  "cnpj": "12345678000190",
  "idCliente": 149062,
  "codCliente": 239516,
  "nomeCliente": "Cliente Demo — Erechim/RS",
  "perfilMercado": "Mercadinho",
  "modo": "mock-perfil",
  "contexto": {
    "temperatura": 7.3,
    "descricao": "algumas nuvens",
    "cidade": "Erechim",
    "fonte": "OpenWeatherMap",
    "eventos": [
      "Festa política na cidade (evento simulado)",
      "Jogo Corinthians x Vitória (evento simulado)"
    ],
    "dadosFicticios": true
  },
  "resumoIa": "Contexto: 7,3 °C em Erechim. Frio: o foco é o evento e o jogo...",
  "gruposRecomendacao": [
    {
      "titulo": "Refrigerantes para o movimento",
      "motivo": "Frio de 7,3 °C. Festa política ontem + jogo hoje...",
      "quantidadeSugerida": 3,
      "itens": [
        {
          "codigo": "REFRI-COLA-2L",
          "descricao": "Fruki Cola 2L",
          "quantidadeSugerida": 3,
          "score": 96
        }
      ]
    }
  ]
}
```

> A resposta real também inclui `produtosRecomendados` (lista achatada dos 9
> itens) e `catalogoCompleto` (os 31 SKUs), usados pelo chat e por integrações.

---

## 5. Arquivos alterados/criados

### Novos

| Arquivo | Papel |
|---|---|
| `src/main/java/org/example/CatalogoService.java` | Catálogo de 31 SKUs |
| `src/main/java/org/example/CatalogoController.java` | `GET /api/catalogo` |
| `src/main/java/org/example/DemoCnpjDirectory.java` | Perfis de CNPJ, eventos e grupos |
| `src/main/java/org/example/DemoController.java` | `GET /api/demo/cnpjs` |
| `src/main/java/org/example/RecomendacaoDemoService.java` | Montagem da recomendação por CNPJ |
| `src/main/java/org/example/ClimaService.java` | OpenWeatherMap + cache + fallback |
| `src/main/java/org/example/OpenWeatherResponse.java` | DTO da API de clima |
| `src/main/resources/static/images/brand/logo-fruki.svg` | Avatar/logo local |
| `src/main/resources/static/images/brand/produto-placeholder.svg` | Fallback de imagem |
| `README.md` | Documentação de uso |
| `docs/RELATORIO-MUDANCAS.md` | Este relatório |
| `docs/GUIA-PO.md` | Guia do PO |

### Modificados

| Arquivo | Mudança |
|---|---|
| `src/main/java/org/example/RecomendacaoController.java` | CNPJ, query param, 404 real, clima |
| `src/main/java/org/example/Cliente.java` | 24 colunas de `dim_cliente` |
| `src/main/java/org/example/ExcelReaderService.java` | Leitura completa do cliente + lat/long |
| `src/main/java/org/example/RecomendacaoService.java` | `buscarCliente`, correção da imagem |
| `src/main/resources/application.properties` | Configuração do clima |
| `src/main/resources/templates/chat.html` | Catálogo independente, grupos, atalho CNPJ, correções |
| `pom.xml` | `-parameters` |

---

## 6. Decisões e limitações (o que ainda é mock)

1. **Sem banco de dados.** Nada de PostgreSQL/Docker/JPA nesta entrega, por
   orientação do PO. `Cliente` está mapeado, mas não persistido.
2. **Catálogo de 31 SKUs.** É uma curadoria comercial; a base tem 263 códigos
   anonimizados. Não é o catálogo oficial.
3. **Preços de demonstração.** Os valores do catálogo são mock; a tabela
   `dim_tabela_preco` ainda não está integrada (e tem 374 mil linhas, com
   validade vencida e `PRECO_MINIMO > PRECO_VENDA` em parte dos registros).
4. **Eventos simulados.** “Festa política” e “Corinthians x Vitória” estão no
   JSON como simulação; não são notícias reais.
5. **Clima real, mas com cache.** A temperatura pode mudar entre o teste e o
   pitch; se a internet falhar, cai no fallback de 24 °C.
6. **Rota legada ainda usa histórico aleatório.** O motor antigo
   (`RecomendacaoService`) não foi substituído por RFM real; a demo por CNPJ é
   que tem a montagem contextual.
7. **Pedido/pagamento não persistem.** O carrinho é em memória JS; “pedido
   transmitido” é uma mensagem de demonstração. O desconto de PIX não é aplicado.
8. **Sem autenticação.** Qualquer pessoa com um código/CNPJ acessa a demo. No
   projeto real, isso precisa de login.
9. **Sem testes automatizados.** A validação foi manual (API, JS, imagens).
10. **Imagens pesadas.** O pacote tem ~96 MB de imagens; não foi otimizado.

---

## 7. Validação realizada

| Verificação | Resultado |
|---|---|
| Compilação com JDK 21 | OK |
| Aplicação sobe | OK (porta 8081 no uso normal) |
| `GET /chat` | 200 |
| CNPJ `12345678000190` | 200, 3 grupos × 3 itens, quantidade 3 |
| CNPJ desconhecido | 200, 3 produtos, `mock-padrao` |
| Código `999` | 404 |
| Documento inválido | 400 |
| `GET /api/catalogo` | 31 itens / 6 categorias |
| 31 imagens do catálogo | 31/31 com HTTP 200 |
| Sintaxe do JavaScript | OK (`node --check`) |
| CNPJs demo | 4 perfis listados |

> O clique no navegador não foi automatizado; a validação foi por API e sintaxe.
> O fluxo visual deve ser conferido no chat.

---

## 8. Riscos e atenção

- **Chave da OpenWeatherMap no repositório.** Foi exposta no chat; rotacionar
  depois do pitch e usar variável de ambiente.
- **Sem testes automatizados.** Qualquer mudança futura pode quebrar o fluxo.
- **Catálogo não oficial.** Não prometer que os 31 SKUs são o catálogo real.
- **Preços não são da tabela real.** Não usar para cobrança.
- **Eventos simulados.** Deixar claro no pitch para não parecer notícia real.
- **Imagens grandes.** Se o deploy for em rede lenta, comprimir antes.
- **Dois arquivos de `.idea` apareceram staged** (provavelmente pela IDE); não
  foram commitados.

---

## 9. Próximos passos (pós-pitch)

1. Subir PostgreSQL via Docker e mapear `Cliente` como entidade JPA.
2. Criar a tabela fato de compras e substituir o histórico sintético por RFM.
3. Persistir pedidos e permitir ingestão de clientes/produtos/compras.
4. Integrar o catálogo oficial e a tabela de preços vigente.
5. Adicionar autenticação e autorização por cliente.
6. Criar testes automatizados (JUnit/MockMvc) para API e recomendação.
7. Otimizar as imagens (redimensionar/comprimir) e o tempo de startup.
8. Remover a chave do repositório e configurar segredo no ambiente.
