# Guia do PO — como usar e apresentar a demo

**Produto:** Pede Fruki B2B  
**O que é:** protótipo do novo canal B2B: o WhatsApp valida o lojista, entrega o
link da vitrine e dispara ofertas contextuais; a plataforma Pede Fruki concentra
a curadoria, o carrinho e o fechamento do pedido.  
**O que não é:** sistema em produção; não consulta o ERP, não grava pedido e não
tem login real.

---

## 1. Antes de começar

Peça para o time de desenvolvimento:

1. Rodar a aplicação (`TodoApplication` no IntelliJ, com JDK 21).
2. Confirmar que a aplicação subiu na porta **8081**.
3. Abrir no navegador:

**http://localhost:8081/chat**

Não precisa instalar nada nem fazer login. A tela simula um WhatsApp Business.

Telas de apoio:

- **http://localhost:8081/promocoes** — os três templates de promoção lado a lado;
- **http://localhost:8081/catalogo** — a plataforma de vitrine e pedido;
- **http://localhost:8081/** — visão geral do novo fluxo.

> Se a porta 8081 estiver ocupada, peça para subir com `--server.port=8082` e use
> `http://localhost:8082/chat`.

---

## 2. Roteiro de 3 minutos

### Passo 1 — “Oi” e identificação

Ao abrir o chat, o cliente já disse **“Oi”**. O assistente explica que vai
validar o CNPJ e levar para a vitrine. Clique em:

> **Demo · Caxias do Sul**

Não precisa digitar. O CNPJ usado é o `11.222.333/0001-44`.

### Passo 2 — CNPJ validado e link personalizado

O assistente:

- confirma o CNPJ e mostra o estabelecimento;
- apresenta cidade, temperatura e perfil;
- envia um **cartão de link personalizado** com o botão
  **“Abrir minha vitrine”**.

**O que explicar aqui:** o WhatsApp não tenta carregar catálogo nem carrinho no
chat. Ele autentica e entrega o link certo. A experiência pesada fica na
plataforma.

### Passo 3 — A vitrine na plataforma

Clique em **“Abrir minha vitrine”** (ou no CTA de um dos templates).

Na plataforma, você verá:

- o cliente já identificado no topo;
- a seção **“Cesta do disparo”** ou **“Sugestões da conversa”** com os itens e
  quantidades sugeridas;
- o catálogo de **31 SKUs** e 6 categorias;
- o carrinho com subtotal, desconto PIX e total.

Ajuste as caixas, escolha a forma de pagamento e clique em **Finalizar pedido**.
O pedido é demonstrativo, mas passa por `POST /api/pedidos`.

### Passo 4 — Disparos contextuais

Volte ao chat. Depois do link, o assistente envia um **exemplo de disparo
contextual** e deixa atalhos:

- **Clima** — “Onda de calor… Estoque agora: Fruki Guaraná e Sucos”;
- **Gre-Nal** — banner de prancha tática com as ofertas do dia;
- **Feriado** — “Prepare seu estoque para o Feriadão”.

Cada template tem banner, texto e botão escuro no padrão WhatsApp. O botão abre
o catálogo com a **cesta recomendada já montada** (`?campanha=...&carrinho=1`).

Para mostrar os três lado a lado, abra **`/promocoes`**. É a tela de apresentação
da UIX: clima, evento regional e feriado em três mockups de celular.

### Passo 5 — Testar outros perfis (opcional)

Use o botão **“Menu”** no topo do chat e escolha outro perfil demo. Cada CNPJ
muda cidade, perfil, recomendação e o gatilho sugerido.

---

## 3. CNPJs de demonstração

| CNPJ | Cidade | Perfil | O que a demo mostra |
|---|---|---|---|
| **11222333000144** | Caxias do Sul | Adega | 3 cervejas + 3 energéticos + 3 águas; disparo de **Gre-Nal** |
| `98765432000110` | Porto Alegre | Supermercado | 3 refrigerantes + 3 cervejas + 3 águas; disparo de **clima** |
| `12345678000190` | Erechim | Mercadinho | 3 refrigerantes + 3 cervejas + 3 águas; disparo de **clima** |
| `55666777000188` | Santa Cruz do Sul | Padaria | 3 sucos + 3 refrigerantes + 3 águas; disparo de **clima** |

- **Qualquer outro CNPJ de 14 dígitos** cai na seleção fixa de 3 produtos
  (Água, Guaraná e Energético), que é o comportamento pedido para o pitch.
- O perfil principal para mostrar o **Gre-Nal** é o **11222333000144**.
- O perfil principal para mostrar o **clima em Porto Alegre** é o
  **98765432000110**.

---

## 4. O que é real e o que é simulado

| Item | Status | O que significa |
|---|---|---|
| Clima (temperatura/cidade) | **Real** | OpenWeatherMap; se a internet falhar, usa 24 °C de fallback |
| CNPJ → cidade/perfil | **Simulado** | Tabela de demonstração; não consulta a base real de CNPJ |
| Validação do CNPJ no chat | **Demo** | Confere o formato e busca o perfil na base mock; não há login real |
| Disparos de clima/jogo/feriado | **Simulados** | A UI e o fluxo existem; o agendamento real da Meta não está conectado |
| Produtos do catálogo | **Curadoria** | 31 SKUs representativos; a base tem 263 códigos anonimizados |
| Preços | **Simulados** | Não são a tabela de preços oficial |
| Eventos (festa política, jogo) | **Simulados** | Estão marcados como “evento simulado” no JSON |
| Recomendação | **Regra de negócio mock** | Não é machine learning; é uma montagem por perfil/clima/evento |
| Pedido/pagamento | **Simulados** | Não persiste, não vai ao ERP, não aplica desconto |
| Integração com a API da Meta | **Não existe** | Protótipo navegável; o envio real de templates entra no projeto real |

---

## 5. O que falar no pitch (sugestão)

> “Este é o novo desenho do Pede Fruki B2B. O WhatsApp deixa de ser um catálogo
> apertado dentro do chat e passa a fazer o que ele faz de melhor: **engajar,
> validar o CNPJ e chamar o lojista para a ação**.
>
> O cliente manda ‘Oi’, o assistente reconhece o estabelecimento e envia um
> **link personalizado** para a vitrine. Na plataforma, ele vê a curadoria por
> clima, evento e perfil, compara os SKUs, ajusta as caixas e fecha o pedido em
> um ambiente de e-commerce B2B.
>
> O WhatsApp continua ativo com **disparos contextuais**: calor, Gre-Nal,
> feriado. Cada disparo leva um banner, uma mensagem curta e um botão que abre a
> **cesta recomendada já pronta** na plataforma.
>
> Assim reduzimos o atrito do catálogo no chat, ganhamos autonomia em relação às
> políticas e custos da Meta e transformamos o WhatsApp em um gatilho de venda,
> não em um carrinho improvisado.”

---

## 6. Perguntas que podem aparecer

**“Por que tirar o catálogo do WhatsApp?”**  
Porque comparar SKUs, checar estoque e ajustar quantidades exige espaço e
velocidade. O chat fica para validação e engajamento; a plataforma, para a
compra.

**“O WhatsApp deixou de vender?”**  
Não. Ele vira o canal de entrada e de reativação. Cada disparo tem CTA para a
cesta recomendada na plataforma.

**“Como o cliente é reconhecido?”**  
Nesta demo, pelo CNPJ informado no chat. A base é mock; em produção entra a
consulta real ao cadastro/ERP.

**“Por que só 31 produtos?”**  
Porque a base bruta tem 263 códigos, mas muitos são variações de embalagem com
nome e marca anonimizados. Os 31 são os SKUs comerciais legíveis para a demo.

**“Por que a temperatura mudou desde o teste?”**  
O clima é real e tem cache de 10 minutos. Se a internet falhar, o sistema usa
24 °C e continua funcionando.

**“Por que fala de jogo e feriado?”**  
São **eventos simulados** para mostrar como o contexto entra na recomendação e
no disparo. Podem ser trocados por eventos reais.

**“O disparo é enviado de verdade pela Meta?”**  
Não nesta fase. O fluxo, a UIX e o link estão implementados; o agendamento e o
envio via WhatsApp Business API entram na próxima etapa.

**“O pedido foi para o ERP?”**  
Não. Nesta fase, o pedido é uma simulação visual. A gravação e a integração
entram no projeto real.

**“E o fluxo antigo?”**  
Continua em **`/whatsapp`** só para comparação. O fluxo recomendado é `/chat`.

---

## 7. Se algo der errado

| Problema | O que fazer |
|---|---|
| Página não abre | Confirme com o dev se a aplicação está rodando na porta 8081 |
| Botão de perfil demo não responde | Atualize a página (F5) e tente novamente |
| Clima aparece “indisponível” ou 24 °C | É o fallback; a demo continua |
| Catálogo não carrega | Clique novamente; se persistir, peça para o dev checar `/api/catalogo` |
| Cesta não aparece no catálogo | Abra o CTA de um template; ele carrega `?campanha=...&carrinho=1` |
| Imagem não aparece | O sistema troca por um placeholder local; siga a demo |
| Carrinho zerou depois do pagamento | Comportamento esperado: o pedido foi “finalizado” |
| Quer recomeçar | Clique em “Menu” no topo do chat ou atualize a página |

---

## 8. Checklist antes do pitch

- [ ] Aplicação rodando em `http://localhost:8081/chat`.
- [ ] Internet disponível (para o clima real; senão, fallback).
- [ ] Testar o CNPJ `11222333000144` (Caxias) e ver o disparo de Gre-Nal.
- [ ] Testar o CNPJ `98765432000110` (Porto Alegre) e ver o disparo de clima.
- [ ] Abrir `/promocoes` e conferir os três templates.
- [ ] Clicar em um CTA e conferir a cesta já no carrinho.
- [ ] Finalizar um pedido de demonstração no catálogo.
- [ ] Deixar o navegador em tela cheia/zoom confortável.
- [ ] Fechar outras abas e notificações.
- [ ] Ter o roteiro de 3 minutos à mão.

---

## 9. O que ainda não está pronto (e não prometer)

- Envio real de templates pela API da Meta (agendamento, aprovação e custos).
- Consulta real de CNPJ na base da Fruki e autenticação do lojista.
- Catálogo oficial completo e tabela de preços vigente.
- Gravação de pedido, estoque e integração com ERP.
- Recomendação com histórico real de compras (RFM/ML).
- Aplicativo mobile nativo.

Esses itens são os próximos passos do projeto real, depois do pitch.
