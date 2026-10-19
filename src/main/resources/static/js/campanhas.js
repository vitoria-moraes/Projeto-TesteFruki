/* =====================================================================
   Pede Fruki — gatilhos contextuais do WhatsApp
   ---------------------------------------------------------------------
   Fonte única dos templates de disparo usados no chat, na agenda e no
   catálogo. Cada campanha tem banner, copy, CTA e cesta recomendada; o CTA
   abre /catalogo?campanha=<id>&carrinho=1 com o carrinho pré-montado.

   A agenda usa PF_AGENDA para distribuir um evento diferente em cada um
   dos próximos sete dias. As datas são calculadas no navegador; os eventos
   são simulados para a demonstração.
   ===================================================================== */
(function () {
  'use strict';

  var CAMPANHAS = {
    clima: {
      id: 'clima',
      gatilho: 'CLIMA',
      rotulo: 'Gatilho · Clima',
      titulo: 'Promoção por clima',
      tema: 'clima',
      texto: 'Onda de calor em {cidade}!\nEstoque agora: Fruki Guaraná e Sucos.\nToque para ver oferta.',
      tituloCatalogo: 'Onda de calor em {cidade}',
      descricao: 'Guaraná, sucos e água para o giro do calor. A cesta já vem montada — ajuste as caixas e finalize.',
      botao: 'Ver oferta no catálogo',
      bannerTag: 'Onda de calor',
      clienteDemo: { codCliente: 173621, nomeCliente: 'Cliente Demo — Porto Alegre/RS', cnpj: '98765432000110' },
      itens: [
        { codigo: 'REFRI-GUARANA-2L', quantidade: 4, motivo: 'Calor: guaraná é o item de maior giro.' },
        { codigo: 'SUCO-LARANJA-1L', quantidade: 3, motivo: 'Sucos para hidratar e variar a vitrine.' },
        { codigo: 'AGUA-500', quantidade: 4, motivo: 'Água para o pico de calor.' }
      ]
    },
    grenal: {
      id: 'grenal',
      gatilho: 'EVENTO REGIONAL',
      rotulo: 'Gatilho · Evento regional',
      titulo: 'Dia de Gre-Nal',
      tema: 'grenal',
      texto: 'O grande jogo é hoje! Garanta as ofertas exclusivas para o dia de Gre-Nal. Toque aqui.',
      tituloCatalogo: 'Dia de Gre-Nal',
      descricao: 'Cervejas, energético e água para o jogo. A cesta já vem montada — ajuste as caixas e finalize.',
      botao: 'Ver ofertas do dia',
      bannerTag: 'Gre-Nal',
      clienteDemo: { codCliente: 46273, nomeCliente: 'Cliente Demo — Caxias do Sul/RS', cnpj: '11222333000144' },
      itens: [
        { codigo: 'CERVEJA-PILSEN-473', quantidade: 4, motivo: 'Cerveja gelada para o clássico.' },
        { codigo: 'CERVEJA-IPA-350', quantidade: 3, motivo: 'Rótulo extra para o público do bar.' },
        { codigo: 'ENERG-ORIGINAL-473', quantidade: 3, motivo: 'Energético para a noite de jogo.' },
        { codigo: 'AGUA-500', quantidade: 3, motivo: 'Hidratação para o time.' }
      ]
    },
    feriado: {
      id: 'feriado',
      gatilho: 'FERIADO',
      rotulo: 'Gatilho · Feriado',
      titulo: 'Prepare o feriadão',
      tema: 'feriado',
      texto: 'Prepare seu estoque para o Feriadão!\nConfira nossa curadoria de produtos.\nToque para comprar.',
      tituloCatalogo: 'Prepare o feriadão',
      descricao: 'Mix de refrigerantes, sucos e água para o feriado. A cesta já vem montada — ajuste as caixas e finalize.',
      botao: 'Abrir catálogo e comprar',
      bannerTag: 'Feriadão',
      clienteDemo: { codCliente: 239516, nomeCliente: 'Cliente Demo — Erechim/RS', cnpj: '12345678000190' },
      itens: [
        { codigo: 'REFRI-COLA-2L', quantidade: 3, motivo: 'Refrigerante para o feriado.' },
        { codigo: 'REFRI-GUARANA-2L', quantidade: 3, motivo: 'O clássico que não pode faltar.' },
        { codigo: 'SUCO-UVA-1L', quantidade: 2, motivo: 'Suco para a mesa da família.' },
        { codigo: 'AGUA-1.5', quantidade: 2, motivo: 'Água para o passeio.' }
      ]
    },
    selecao: {
      id: 'selecao',
      gatilho: 'EVENTO ESPORTIVO',
      rotulo: 'Gatilho · Evento esportivo',
      titulo: 'Jogo do Brasil',
      tema: 'esporte',
      texto: 'Jogo do Brasil é hoje!\nReforce cerveja, energético e água para o balcão.\nToque para ver oferta.',
      tituloCatalogo: 'Jogo do Brasil',
      descricao: 'Cerveja, energético e água para o jogo da seleção. A cesta já vem montada — ajuste as caixas e finalize.',
      botao: 'Ver oferta do jogo',
      bannerTag: 'Jogo do Brasil',
      clienteDemo: { codCliente: 46273, nomeCliente: 'Cliente Demo — Caxias do Sul/RS', cnpj: '11222333000144' },
      itens: [
        { codigo: 'CERVEJA-PILSEN-473', quantidade: 4, motivo: 'Cerveja para o jogo da seleção.' },
        { codigo: 'ENERG-ORIGINAL-473', quantidade: 3, motivo: 'Energético para acompanhar a partida.' },
        { codigo: 'AGUA-500', quantidade: 3, motivo: 'Água para hidratar a torcida.' },
        { codigo: 'REFRI-GUARANA-2L', quantidade: 2, motivo: 'Refrigerante para quem não bebe álcool.' }
      ]
    },
    'festa-uva': {
      id: 'festa-uva',
      gatilho: 'EVENTO REGIONAL',
      rotulo: 'Gatilho · Evento regional',
      titulo: 'Festa da Uva',
      tema: 'festa',
      texto: 'Festa da Uva em {cidade}!\nSucos e refrigerantes para o movimento da festa.\nToque para ver oferta.',
      tituloCatalogo: 'Festa da Uva em {cidade}',
      descricao: 'Sucos de uva e maçã, refrigerante de uva e água para o público da festa. A cesta já vem montada — ajuste as caixas e finalize.',
      botao: 'Ver oferta da festa',
      bannerTag: 'Festa da Uva',
      clienteDemo: { codCliente: 46273, nomeCliente: 'Cliente Demo — Caxias do Sul/RS', cnpj: '11222333000144' },
      itens: [
        { codigo: 'SUCO-UVA-1L', quantidade: 4, motivo: 'Suco de uva é a cara da festa.' },
        { codigo: 'SUCO-MACA-1L', quantidade: 2, motivo: 'Alternativa para variar a vitrine.' },
        { codigo: 'REFRI-UVA-2L', quantidade: 3, motivo: 'Refrigerante de uva para o público da festa.' },
        { codigo: 'AGUA-500', quantidade: 2, motivo: 'Água para o passeio.' }
      ]
    },
    criancas: {
      id: 'criancas',
      gatilho: 'DATA COMEMORATIVA',
      rotulo: 'Gatilho · Data comemorativa',
      titulo: 'Dia das Crianças',
      tema: 'infantil',
      texto: 'Dia das Crianças chegando!\nSucos e refris para as festas do bairro.\nToque para ver oferta.',
      tituloCatalogo: 'Dia das Crianças',
      descricao: 'Sucos em caixinha, refrigerante e água para festas escolares e do bairro. A cesta já vem montada — ajuste as caixas e finalize.',
      botao: 'Ver oferta das crianças',
      bannerTag: 'Dia das Crianças',
      clienteDemo: { codCliente: 239516, nomeCliente: 'Cliente Demo — Erechim/RS', cnpj: '12345678000190' },
      itens: [
        { codigo: 'SUCO-LARANJA-200', quantidade: 4, motivo: 'Caixinha individual para as festas.' },
        { codigo: 'SUCO-UVA-200', quantidade: 3, motivo: 'Suco de uva para variar.' },
        { codigo: 'REFRI-COLA-600', quantidade: 2, motivo: 'Refrigerante para a mesa.' },
        { codigo: 'AGUA-500', quantidade: 2, motivo: 'Água para as crianças.' }
      ]
    },
    frio: {
      id: 'frio',
      gatilho: 'CLIMA',
      rotulo: 'Gatilho · Clima',
      titulo: 'Frente fria',
      tema: 'frio',
      texto: 'Frente fria em {cidade}!\nReforce sucos e tônicas para o happy hour em casa.\nToque para ver oferta.',
      tituloCatalogo: 'Frente fria em {cidade}',
      descricao: 'Sucos, tônicas e cervejas premium para o happy hour em casa. A cesta já vem montada — ajuste as caixas e finalize.',
      botao: 'Ver oferta do frio',
      bannerTag: 'Frente fria',
      clienteDemo: { codCliente: 173621, nomeCliente: 'Cliente Demo — Porto Alegre/RS', cnpj: '98765432000110' },
      itens: [
        { codigo: 'SUCO-UVA-1L', quantidade: 3, motivo: 'Suco de uva para a mesa no frio.' },
        { codigo: 'SUCO-MACA-1L', quantidade: 2, motivo: 'Suco de maçã para variar.' },
        { codigo: 'TONICA-1500', quantidade: 2, motivo: 'Tônica para os drinks em casa.' },
        { codigo: 'CERVEJA-PREMIUM', quantidade: 2, motivo: 'Rótulo premium para o happy hour.' }
      ]
    }
  };

  /* ---------------------------------------------------------------------
     Agenda demonstrativa: um evento por dia nos próximos sete dias.
     O offset 0 é hoje. Datas, eventos e contextos são simulados.
     --------------------------------------------------------------------- */
  var AGENDA = [
    {
      offset: 0,
      campanha: 'clima',
      evento: 'Onda de calor',
      tipo: 'Clima',
      manchete: 'Promoção por clima.',
      janela: '08:30',
      cidade: 'Porto Alegre',
      publico: 'Mercados e adegas de Porto Alegre',
      destaque: 'Máx. 33 °C · sol forte',
      porque: 'Temperatura acima da média na região. Guaraná, sucos e água são os itens de maior giro no balcão.'
    },
    {
      offset: 1,
      campanha: 'grenal',
      evento: 'Gre-Nal',
      tipo: 'Evento regional',
      manchete: 'Dia de Gre-Nal.',
      janela: '10:00',
      cidade: 'Porto Alegre',
      publico: 'Bares, adegas e mercados do RS',
      destaque: 'Clássico · 16h',
      porque: 'O clássico movimenta bares e adegas. Cerveja gelada, energético e água entram na cesta do dia.'
    },
    {
      offset: 2,
      campanha: 'feriado',
      evento: 'Feriadão',
      tipo: 'Feriado',
      manchete: 'Prepare o feriadão.',
      janela: '08:00',
      cidade: 'Erechim',
      publico: 'Mercadinhos e conveniências',
      destaque: 'Feriado na quinta',
      porque: 'Feriado prolongado muda a rotina de compras. O lojista antecipa o estoque de refris, sucos e água.'
    },
    {
      offset: 3,
      campanha: 'selecao',
      evento: 'Jogo do Brasil',
      tipo: 'Evento esportivo',
      manchete: 'Jogo do Brasil.',
      janela: '16:30',
      cidade: 'Caxias do Sul',
      publico: 'Bares e conveniências',
      destaque: 'Seleção em campo · 20h',
      porque: 'Jogo da seleção à noite aquece o consumo em bares e conveniências. Cerveja, energético e água lideram.'
    },
    {
      offset: 4,
      campanha: 'festa-uva',
      evento: 'Festa da Uva',
      tipo: 'Evento regional',
      manchete: 'Festa da Uva.',
      janela: '09:00',
      cidade: 'Caxias do Sul',
      publico: 'Comércio e adegas de Caxias do Sul',
      destaque: 'Caxias do Sul · temporada',
      porque: 'A festa atrai turistas e aumenta o fluxo no comércio local. Sucos e refris de uva entram na vitrine.'
    },
    {
      offset: 5,
      campanha: 'criancas',
      evento: 'Dia das Crianças',
      tipo: 'Data comemorativa',
      manchete: 'Dia das Crianças.',
      janela: '08:30',
      cidade: 'Erechim',
      publico: 'Padarias, mercados e escolas',
      destaque: 'Festas e escolas',
      porque: 'A data comemorativa aumenta a procura por sucos e refris para festas, escolas e presentes.'
    },
    {
      offset: 6,
      campanha: 'frio',
      evento: 'Frente fria',
      tipo: 'Clima',
      manchete: 'Frente fria.',
      janela: '07:30',
      cidade: 'Porto Alegre',
      publico: 'Mercados e adegas de Porto Alegre',
      destaque: 'Mín. 9 °C',
      porque: 'A queda de temperatura desloca o consumo para o happy hour em casa. Sucos, tônicas e cervejas premium ganham espaço.'
    }
  ];

  var PRINCIPAIS = ['clima', 'grenal', 'feriado'];

  function texto(campanha, cidade) {
    if (!campanha) return '';
    return String(campanha.texto || '').replace('{cidade}', cidade || 'Porto Alegre');
  }

  function tituloCatalogo(campanha, cidade) {
    if (!campanha) return '';
    return String(campanha.tituloCatalogo || campanha.titulo || '').replace('{cidade}', cidade || 'Porto Alegre');
  }

  function url(campanha, comCarrinho) {
    var id = typeof campanha === 'string' ? campanha : (campanha && campanha.id);
    if (!id) return '/catalogo';
    return '/catalogo?campanha=' + encodeURIComponent(id) + (comCarrinho === false ? '' : '&carrinho=1');
  }

  function daUrl() {
    try {
      var id = new URLSearchParams(window.location.search).get('campanha');
      return id && CAMPANHAS[id] ? CAMPANHAS[id] : null;
    } catch (e) {
      return null;
    }
  }

  function ehCampanha(valor) {
    return !!(valor && CAMPANHAS[valor]);
  }

  window.PF_CAMPANHAS = CAMPANHAS;
  window.PF_AGENDA = AGENDA;
  window.PF_CAMPANHAS_PRINCIPAIS = PRINCIPAIS;
  window.PF = window.PF || {};
  window.PF.campanha = function (id) { return CAMPANHAS[id] || null; };
  window.PF.campanhaDaUrl = daUrl;
  window.PF.ehCampanha = ehCampanha;
  window.PF.textoCampanha = texto;
  window.PF.tituloCampanha = tituloCatalogo;
  window.PF.urlCampanha = url;
})();
