# Guia do PO — como usar e apresentar a demo

**Produto:** Pede Fruki B2B  
**O que é:** protótipo de chat comercial que recomenda produtos, mostra catálogo
e simula um pedido B2B.  
**O que não é:** sistema em produção; não consulta o ERP, não grava pedido e não
tem login.

---

## 1. Antes de começar

Peça para o time de desenvolvimento:

1. Rodar a aplicação (`TodoApplication` no IntelliJ, com JDK 21).
2. Confirmar que a aplicação subiu na porta **8081**.
3. Abrir no navegador:

**http://localhost:8081/chat**

Não precisa instalar nada nem fazer login. A tela simula um WhatsApp Business.

> Se a página não abrir, veja a seção “Se algo der errado” no final.

---

## 2. Roteiro de 3 minutos

### Passo 1 — Identificação

Ao abrir, o chat pergunta o CNPJ. Logo abaixo aparece o botão:

> **🏬 Usar CNPJ demo 12.345.678/0001-90 (Erechim/RS)**

**Clique no botão.** Não precisa digitar.

O sistema responde com:

- nome do estabelecimento demo;
- cidade e temperatura (clima real);
- menu com 4 opções.

### Passo 2 — Opção 1: recomendações da IA

Clique em **1 — Ver Recomendações Personalizadas**.

Você verá:

1. Um resumo do contexto:
   > “Contexto: 7,3 °C em Erechim. Frio: o foco é o evento e o jogo. Festa
   > política na cidade (evento simulado); Jogo Corinthians x Vitória (evento
   > simulado). Montei 3 grupos com 3 opções cada…”
2. **3 grupos** de recomendação:
   - Refrigerantes para o movimento;
   - Cervejas para o jogo;
   - Águas para hidratar.
3. Em cada grupo, **3 produtos** com:
   - nome, imagem, preço e embalagem;
   - **“Sugestão IA: 3 caixas”**;
   - botões **+ / –** para montar o pedido.
4. Um botão **“Ver catálogo completo”**.

**O que explicar aqui:** a recomendação não é só “produto X”; ela conecta
**clima + evento + perfil do estabelecimento** e sugere **quantidade**.

### Passo 3 — Opção 2: catálogo completo

Clique em **2 — Ver Catálogo Completo de Produtos** (ou no botão do passo
anterior).

Você verá:

- **31 SKUs** no total;
- 6 blocos: **Águas, Refrigerantes, Sucos, Energéticos, Cervejas e Tônicas**;
- os itens recomendados pela IA com selo **IA** e a quantidade sugerida;
- o restante do catálogo disponível para compra.

**Importante:** o catálogo funciona **mesmo depois** de ver a recomendação.
Essa era uma correção importante: antes, a opção 2 “herdava” a lista da IA.

### Passo 4 — Carrinho e pedido

1. Adicione itens com **+**.
2. Clique em **📦 Ver Carrinho & Fechar Pedido**.
3. O chat mostra o resumo, quantidade e valor total.
4. Escolha uma forma de pagamento.
5. O sistema mostra “Pedido transmitido com sucesso”.

**Atenção:** esse pedido é **demonstrativo**. Ele não é gravado no banco, não
vai para o ERP e o desconto de PIX não é aplicado. Serve para mostrar a
experiência.

### Passo 5 — Testar outros CNPJs (opcional)

Volte ao menu e digite outro CNPJ da tabela abaixo. Cada um muda a cidade, o
perfil e a montagem da IA.

---

## 3. CNPJs de demonstração

| CNPJ | Cidade | Perfil | O que a IA mostra |
|---|---|---|---|
| **12345678000190** | Erechim | Mercadinho | 3 refrigerantes + 3 cervejas + 3 águas |
| `98765432000110` | Porto Alegre | Supermercado | 3 refrigerantes + 3 cervejas + 3 águas (mix diferente) |
| `11222333000144` | Caxias do Sul | Adega | 3 cervejas + 3 energéticos + 3 águas |
| `55666777000188` | Santa Cruz do Sul | Padaria | 3 sucos + 3 refrigerantes + 3 águas |

- **Qualquer outro CNPJ de 14 dígitos** cai na seleção fixa de 3 produtos
  (Água, Guaraná e Energético), que é o comportamento pedido para o pitch.
- O CNPJ principal da apresentação é o **12345678000190**.

---

## 4. O que é real e o que é simulado

| Item | Status | O que significa |
|---|---|---|
| Clima (temperatura/cidade) | **Real** | OpenWeatherMap; se a internet falhar, usa 24 °C de fallback |
| CNPJ → cidade/perfil | **Simulado** | Tabela de demonstração; não consulta a base real de CNPJ |
| Produtos do catálogo | **Curadoria** | 31 SKUs representativos; a base tem 263 códigos anonimizados |
| Preços | **Simulados** | Não são a tabela de preços oficial |
| Eventos (festa política, jogo) | **Simulados** | Estão marcados como “evento simulado” no JSON |
| Recomendação | **Regra de negócio mock** | Não é machine learning; é uma montagem por perfil/clima/evento |
| Pedido/pagamento | **Simulados** | Não persiste, não vai ao ERP, não aplica desconto |
| Login | **Não existe** | Protótipo aberto; autenticação entra no projeto real |

---

## 5. O que falar no pitch (sugestão)

> “Este é o Pede Fruki B2B. Vou usar um CNPJ real de Erechim para mostrar a
> experiência. O sistema reconhece o estabelecimento e cruza três informações:
> clima da região, eventos locais e perfil do negócio.
>
> Na recomendação, ele não entrega só uma lista: entrega **3 grupos com 3 opções
> cada**, com o **motivo** e uma **quantidade sugerida**. Por exemplo: mais
> refrigerante porque tem evento e jogo na cidade; cerveja para o público do
> jogo; água para hidratar.
>
> No catálogo completo, o cliente vê os 31 SKUs e os itens recomendados aparecem
> com selo IA. Ele pode ajustar a quantidade e fechar o pedido.
>
> Nesta fase, o pedido é demonstrativo; a persistência e a integração com o ERP
> entram na próxima etapa.”

---

## 6. Perguntas que podem aparecer

**“Por que só 31 produtos?”**  
Porque a base bruta tem 263 códigos, mas muitos são variações de embalagem com
nome e marca anonimizados. Os 31 são os SKUs comerciais legíveis para a demo. O
catálogo oficial entra depois.

**“Por que a temperatura mudou desde o teste?”**  
O clima é real e tem cache de 10 minutos. Se a internet falhar, o sistema usa
24 °C e continua funcionando.

**“Por que fala de festa política e jogo?”**  
São eventos **simulados** para mostrar como o contexto entra na recomendação.
Podem ser trocados por eventos reais ou por outro cenário.

**“Por que 3 caixas?”**  
É uma sugestão de quantidade da demo. Pode ser ajustada por produto/grupo.

**“O pedido foi para o ERP?”**  
Não. Nesta fase, o pedido é uma simulação visual. A gravação e a integração
entram no projeto real.

**“Dá para usar outro CNPJ?”**  
Sim. Os quatro CNPJs da tabela têm perfis diferentes; qualquer outro CNPJ de
14 dígitos retorna a seleção fixa de 3 produtos.

**“Por que o PIX não deu desconto?”**  
Porque o pagamento é demonstrativo; o desconto não é aplicado nesta versão.

---

## 7. Se algo der errado

| Problema | O que fazer |
|---|---|
| Página não abre | Confirme com o dev se a aplicação está rodando na porta 8081 |
| Botão do CNPJ não responde | Atualize a página (F5) e tente novamente |
| Clima aparece “indisponível” ou 24 °C | É o fallback; a demo continua |
| Catálogo não carrega | Clique novamente; se persistir, peça para o dev checar `/api/catalogo` |
| Imagem não aparece | O sistema troca por um placeholder local; siga a demo |
| Carrinho zerou depois do pagamento | Comportamento esperado: o pedido foi “finalizado” |
| Quer recomeçar | Clique em “Menu” no topo ou atualize a página |

---

## 8. Checklist antes do pitch

- [ ] Aplicação rodando em `http://localhost:8081/chat`.
- [ ] Internet disponível (para o clima real; senão, fallback).
- [ ] Testar o CNPJ `12345678000190` uma vez.
- [ ] Testar opção 1 → opção 2 (catálogo completo).
- [ ] Testar adicionar item e abrir o carrinho.
- [ ] Deixar o navegador em tela cheia/zoom confortável.
- [ ] Fechar outras abas e notificações.
- [ ] Ter o roteiro de 3 minutos à mão.

---

## 9. O que ainda não está pronto (e não prometer)

- Consulta real de CNPJ na base da Fruki.
- Catálogo oficial completo e tabela de preços vigente.
- Gravação de pedido, estoque e integração com ERP.
- Login/autenticação do cliente.
- Recomendação com histórico real de compras (RFM/ML).
- Aplicativo mobile nativo.

Esses itens são os próximos passos do projeto real, depois do pitch.
