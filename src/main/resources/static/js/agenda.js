/* =====================================================================
   Pede Fruki — agenda contextual
   ---------------------------------------------------------------------
   Monta os próximos sete dias a partir de hoje, um evento por dia, e
   renderiza o template de WhatsApp do dia selecionado. O CTA de cada dia
   aponta para /catalogo?campanha=<id>&carrinho=1.
   ===================================================================== */
(function () {
  'use strict';

  var CAMPANHAS = window.PF_CAMPANHAS || {};
  var AGENDA = window.PF_AGENDA || [];

  var DIAS = ['dom', 'seg', 'ter', 'qua', 'qui', 'sex', 'sáb'];
  var DIAS_LONGOS = ['domingo', 'segunda-feira', 'terça-feira', 'quarta-feira',
    'quinta-feira', 'sexta-feira', 'sábado'];
  var MESES = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun',
    'jul', 'ago', 'set', 'out', 'nov', 'dez'];
  var MESES_LONGOS = ['janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho',
    'julho', 'agosto', 'setembro', 'outubro', 'novembro', 'dezembro'];

  var produtos = {};
  var catalogoCarregado = false;
  var dias = [];
  var selecionado = 0;

  var $ = function (id) { return document.getElementById(id); };
  var esc = function (s) {
    return String(s == null ? '' : s).replace(/[&<>"']/g, function (c) {
      return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
    });
  };
  var brl = function (v) {
    return Number(v || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  };
  var capitalize = function (s) { return s ? s.charAt(0).toUpperCase() + s.slice(1) : s; };

  function hoje() {
    var d = new Date();
    d.setHours(0, 0, 0, 0);
    return d;
  }

  function somarDias(data, n) {
    var d = new Date(data.getTime());
    d.setDate(d.getDate() + n);
    return d;
  }

  function curta(data) {
    return String(data.getDate()).padStart(2, '0') + '/' + String(data.getMonth() + 1).padStart(2, '0');
  }

  function longa(data) {
    return DIAS_LONGOS[data.getDay()] + ', ' + data.getDate() + ' de ' + MESES_LONGOS[data.getMonth()];
  }

  function rotuloDia(data, indice) {
    if (indice === 0) return 'Hoje';
    if (indice === 1) return 'Amanhã';
    return DIAS[data.getDay()] + ', ' + curta(data);
  }

  function periodo(inicio, fim) {
    var mesmoMes = inicio.getMonth() === fim.getMonth() && inicio.getFullYear() === fim.getFullYear();
    if (mesmoMes) {
      return inicio.getDate() + '–' + fim.getDate() + ' de ' + MESES_LONGOS[fim.getMonth()];
    }
    return inicio.getDate() + ' de ' + MESES_LONGOS[inicio.getMonth()] + ' – ' +
      fim.getDate() + ' de ' + MESES_LONGOS[fim.getMonth()];
  }

  function montarDias() {
    var base = hoje();
    var lista = [];
    for (var i = 0; i < AGENDA.length; i++) {
      var item = AGENDA[i];
      var camp = CAMPANHAS[item.campanha];
      if (!camp) continue;
      lista.push({
        data: somarDias(base, item.offset == null ? i : item.offset),
        campanha: camp,
        evento: item.evento,
        tipo: item.tipo,
        manchete: item.manchete,
        janela: item.janela,
        cidade: item.cidade,
        publico: item.publico,
        destaque: item.destaque,
        porque: item.porque
      });
    }
    return lista;
  }

  function renderSemana() {
    var alvo = $('week');
    if (!alvo) return;
    alvo.innerHTML = dias.map(function (d, i) {
      var data = d.data;
      var selecionadoAttr = i === selecionado ? 'true' : 'false';
      return '' +
        '<button type="button" class="day' +
        (i === selecionado ? ' is-selected' : '') +
        (i === 0 ? ' is-today' : '') +
        '" data-index="' + i + '" data-tema="' + esc(d.campanha.tema) + '"' +
        ' role="tab" aria-selected="' + selecionadoAttr + '"' +
        ' aria-label="' + esc(d.evento + ', ' + longa(data)) + '">' +
        '<span class="day-week">' + DIAS[data.getDay()] + (i === 0 ? ' · hoje' : '') + '</span>' +
        '<span class="day-date"><b>' + data.getDate() + '</b><small>' + MESES[data.getMonth()] + '</small></span>' +
        '<span class="day-event">' + esc(d.evento) + '</span>' +
        '<span class="day-type"><i></i>' + esc(d.tipo) + '</span>' +
        '</button>';
    }).join('');

    var inicio = dias.length ? dias[0].data : hoje();
    var fim = dias.length ? dias[dias.length - 1].data : hoje();
    $('weekRange').textContent = periodo(inicio, fim);
  }

  function bannerHtml(camp) {
    if (camp.tema === 'clima') {
      return '' +
        '<div class="pf-banner pf-banner--clima" role="img" aria-label="Bebidas Fruki sob o sol de uma onda de calor">' +
        '<span class="pf-sun" aria-hidden="true"></span>' +
        '<span class="pf-heatline pf-heatline--a" aria-hidden="true"></span>' +
        '<span class="pf-heatline pf-heatline--b" aria-hidden="true"></span>' +
        '<span class="pf-banner__tag">' + esc(camp.bannerTag || 'Onda de calor') + '</span>' +
        '<img class="pf-pack pf-clima-pack--1" src="/images/Refri fruki/guarana1-5l.webp" alt="">' +
        '<img class="pf-pack pf-clima-pack--2" src="/images/Sucos fruki/_suco_tetrapack_1l_frente_laranja.webp" alt="">' +
        '<img class="pf-pack pf-clima-pack--3" src="/images/Agua fruki/fruki-500ml-agua-c-gas-gotas.webp" alt="">' +
        '</div>';
    }

    if (camp.tema === 'grenal') {
      return '' +
        '<div class="pf-banner pf-banner--grenal" role="img" aria-label="Prancha tática de futebol com o clássico Gre-Nal">' +
        '<svg class="pf-pitch" viewBox="0 0 300 128" preserveAspectRatio="none" aria-hidden="true">' +
        '<g fill="none" stroke="rgba(255,255,255,.24)" stroke-width="1">' +
        '<rect x="10" y="10" width="280" height="108"/>' +
        '<line x1="150" y1="10" x2="150" y2="118"/>' +
        '<circle cx="150" cy="64" r="17"/>' +
        '<rect x="10" y="37" width="27" height="54"/>' +
        '<rect x="263" y="37" width="27" height="54"/>' +
        '</g>' +
        '<path d="M62 92C90 76 111 86 128 63" fill="none" stroke="rgba(217,245,107,.55)" stroke-width="1.4" stroke-dasharray="4 4"/>' +
        '<path d="m124 66 9-4-4 10z" fill="rgba(217,245,107,.7)"/>' +
        '</svg>' +
        '<span class="pf-tactic" style="left:20%;top:64%">×</span>' +
        '<span class="pf-tactic pf-tactic--o" style="left:38%;top:48%"></span>' +
        '<span class="pf-tactic" style="left:72%;top:34%">×</span>' +
        '<span class="pf-tactic pf-tactic--o" style="left:80%;top:69%"></span>' +
        '<span class="pf-grenal-badge">Clássico · 16h</span>' +
        '<div class="pf-grenal-title">GRE<span>-</span>NAL</div>' +
        '<div class="pf-grenal-sub">GRÊMIO × INTERNACIONAL</div>' +
        '</div>';
    }

    if (camp.tema === 'feriado') {
      return '' +
        '<div class="pf-banner pf-banner--feriado" role="img" aria-label="Produtos Fruki com confetes para o feriadão">' +
        '<span class="pf-feriado-tag">FERIADÃO</span>' +
        '<i class="pf-confetti" style="--x:64%;--y:13%;--r:28deg;--c:#0a7d5a" aria-hidden="true"></i>' +
        '<i class="pf-confetti" style="--x:76%;--y:27%;--r:-18deg;--c:#c44b2d" aria-hidden="true"></i>' +
        '<i class="pf-confetti round" style="--x:88%;--y:14%;--r:0deg;--c:#e7a32c" aria-hidden="true"></i>' +
        '<i class="pf-confetti" style="--x:52%;--y:9%;--r:52deg;--c:#ffffff" aria-hidden="true"></i>' +
        '<i class="pf-confetti round" style="--x:70%;--y:43%;--r:0deg;--c:#0a7d5a" aria-hidden="true"></i>' +
        '<i class="pf-confetti" style="--x:93%;--y:39%;--r:12deg;--c:#c44b2d" aria-hidden="true"></i>' +
        '<i class="pf-confetti" style="--x:44%;--y:24%;--r:-32deg;--c:#e7a32c" aria-hidden="true"></i>' +
        '<img class="pf-pack pf-feriado-pack--1" src="/images/Refri fruki/cola-2l.webp" alt="">' +
        '<img class="pf-pack pf-feriado-pack--2" src="/images/Refri fruki/guarana1-5l.webp" alt="">' +
        '<img class="pf-pack pf-feriado-pack--3" src="/images/Sucos fruki/_suco_tetrapack_1l_frente_laranja.webp" alt="">' +
        '</div>';
    }

    if (camp.tema === 'esporte') {
      return '' +
        '<div class="pf-banner pf-banner--selecao" role="img" aria-label="Campo de futebol e produtos Fruki para o jogo do Brasil">' +
        '<span class="pf-banner__tag">' + esc(camp.bannerTag || 'Jogo do Brasil') + '</span>' +
        '<span class="pf-selecao-pitch" aria-hidden="true"></span>' +
        '<span class="pf-selecao-circle" aria-hidden="true"></span>' +
        '<span class="pf-selecao-title">BRASIL</span>' +
        '<span class="pf-selecao-sub">RUMO AO HEXA</span>' +
        '<img class="pf-pack pf-selecao-pack--1" src="/images/Cervejas fruki/bellavista-pilsen-473.webp" alt="">' +
        '<img class="pf-pack pf-selecao-pack--2" src="/images/Energetico fruki/eleve_473_original.webp" alt="">' +
        '<img class="pf-pack pf-selecao-pack--3" src="/images/Agua fruki/fruki-500ml-agua-s-gas-gotas.webp" alt="">' +
        '</div>';
    }

    if (camp.tema === 'festa') {
      return '' +
        '<div class="pf-banner pf-banner--festa" role="img" aria-label="Uvas e produtos Fruki para a Festa da Uva">' +
        '<span class="pf-banner__tag">' + esc(camp.bannerTag || 'Festa da Uva') + '</span>' +
        '<span class="pf-grapes" aria-hidden="true"></span>' +
        '<span class="pf-festa-title">FESTA<br>DA UVA</span>' +
        '<span class="pf-festa-sub">CAXIAS DO SUL</span>' +
        '<img class="pf-pack pf-festa-pack--1" src="/images/Sucos fruki/_suco_vidro_1l_frente_uva.webp" alt="">' +
        '<img class="pf-pack pf-festa-pack--2" src="/images/Sucos fruki/_suco_tetrapack_1l_frente_maca.webp" alt="">' +
        '<img class="pf-pack pf-festa-pack--3" src="/images/Refri fruki/mockup-2l-uva-final.webp" alt="">' +
        '</div>';
    }

    if (camp.tema === 'infantil') {
      return '' +
        '<div class="pf-banner pf-banner--infantil" role="img" aria-label="Balões e produtos Fruki para o Dia das Crianças">' +
        '<span class="pf-banner__tag">' + esc(camp.bannerTag || 'Dia das Crianças') + '</span>' +
        '<span class="pf-balloon" style="--c:#0a7d5a;left:61%;top:12%" aria-hidden="true"></span>' +
        '<span class="pf-balloon" style="--c:#e26d8a;left:76%;top:7%" aria-hidden="true"></span>' +
        '<span class="pf-balloon" style="--c:#f2b23c;left:89%;top:16%" aria-hidden="true"></span>' +
        '<span class="pf-infantil-title">FESTA!</span>' +
        '<span class="pf-infantil-sub">PARA A CRIANÇADA</span>' +
        '<img class="pf-pack pf-infantil-pack--1" src="/images/Sucos fruki/_suco_tetrapack_200ml_frente_laranja.webp" alt="">' +
        '<img class="pf-pack pf-infantil-pack--2" src="/images/Sucos fruki/_suco_tetrapack_200ml_frente_uva.webp" alt="">' +
        '<img class="pf-pack pf-infantil-pack--3" src="/images/Refri fruki/cola-600.webp" alt="">' +
        '</div>';
    }

    return '' +
      '<div class="pf-banner pf-banner--frio" role="img" aria-label="Produtos Fruki para uma frente fria">' +
      '<span class="pf-banner__tag">' + esc(camp.bannerTag || 'Frente fria') + '</span>' +
      '<span class="pf-snow pf-snow--a" aria-hidden="true"></span>' +
      '<span class="pf-snow pf-snow--b" aria-hidden="true"></span>' +
      '<span class="pf-snow pf-snow--c" aria-hidden="true"></span>' +
      '<span class="pf-frio-title">FRENTE<br>FRIA</span>' +
      '<span class="pf-frio-sub">HAPPY HOUR EM CASA</span>' +
      '<img class="pf-pack pf-frio-pack--1" src="/images/Sucos fruki/_suco_vidro_1l_frente_uva.webp" alt="">' +
      '<img class="pf-pack pf-frio-pack--2" src="/images/Sucos fruki/_suco_tetrapack_1l_frente_maca.webp" alt="">' +
      '<img class="pf-pack pf-frio-pack--3" src="/images/Tonicas fruki/tonica_1500ml.webp" alt="">' +
      '</div>';
  }

  function promoHtml(camp, d) {
    var texto = esc(window.PF.textoCampanha(camp, d.cidade)).replace(/\n/g, '<br>');
    var url = window.PF.urlCampanha(camp.id);
    return '' +
      '<div class="pf-promo">' +
      bannerHtml(camp) +
      '<div class="pf-promo__body">' +
      '<p class="pf-promo__text">' + texto + '</p>' +
      '</div>' +
      '<a class="pf-promo__cta" href="' + esc(url) + '">' +
      esc(camp.botao) +
      '<svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M3 8h9M8.5 4.5 12 8l-3.5 3.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>' +
      '</a>' +
      '</div>';
  }

  function renderCesta(camp) {
    var itens = (camp.itens || []).map(function (i) {
      var p = produtos[i.codigo] || null;
      var q = Math.max(1, Number(i.quantidade) || 1);
      return {
        codigo: i.codigo,
        quantidade: q,
        descricao: p ? p.descricao : i.codigo,
        imagem: p ? p.imagemUrl : '/images/brand/produto-placeholder.svg',
        preco: p ? Number(p.preco) || 0 : null
      };
    });

    var caixas = itens.reduce(function (s, i) { return s + i.quantidade; }, 0);
    var total = itens.reduce(function (s, i) { return s + (i.preco || 0) * i.quantidade; }, 0);

    $('basketList').innerHTML = itens.map(function (i) {
      return '' +
        '<li class="basket-item">' +
        '<img src="' + esc(i.imagem) + '" alt="" loading="lazy" onerror="this.onerror=null;this.src=\'/images/brand/produto-placeholder.svg\'">' +
        '<span class="basket-item-main"><b>' + esc(i.descricao) + '</b><small>' + i.quantidade + ' cx · ' + esc(i.codigo) + '</small></span>' +
        '<span class="basket-item-price">' + (i.preco == null ? '—' : brl(i.preco * i.quantidade)) + '</span>' +
        '</li>';
    }).join('');

    $('basketMeta').textContent = caixas + ' caixas · ' + itens.length + ' SKUs';
    $('basketTotal').textContent = catalogoCarregado ? brl(total) : '—';
  }

  function renderDetalhe() {
    if (!dias.length) return;
    var d = dias[selecionado];
    var camp = d.campanha;

    $('previewKicker').textContent = 'Gatilho • ' + d.tipo + ' · ' +
      (selecionado === 0 ? 'Hoje, ' + curta(d.data) : longa(d.data));
    $('previewTitle').textContent = d.manchete;
    $('waDay').textContent = rotuloDia(d.data, selecionado);
    $('waMessage').innerHTML = promoHtml(camp, d);

    $('infoType').textContent = 'Gatilho • ' + d.tipo;
    $('infoEvent').textContent = d.evento;
    $('infoHighlight').textContent = d.destaque;
    $('infoReason').textContent = d.porque;
    $('infoWindow').textContent = d.janela + ' · ' + rotuloDia(d.data, selecionado);
    $('infoAudience').textContent = d.publico;

    $('cta').href = window.PF.urlCampanha(camp.id);
    renderCesta(camp);
  }

  function renderTudo() {
    renderSemana();
    renderDetalhe();
  }

  function selecionar(indice) {
    if (indice < 0 || indice >= dias.length) return;
    selecionado = indice;
    renderSemana();
    renderDetalhe();
    var telefone = document.querySelector('.day-preview');
    if (telefone && window.matchMedia('(max-width: 900px)').matches) {
      telefone.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  function ligarEventos() {
    $('week').addEventListener('click', function (e) {
      var botao = e.target.closest ? e.target.closest('.day') : null;
      if (!botao) return;
      selecionar(Number(botao.getAttribute('data-index')));
    });

    $('week').addEventListener('keydown', function (e) {
      if (e.key !== 'ArrowRight' && e.key !== 'ArrowLeft') return;
      e.preventDefault();
      selecionar(selecionado + (e.key === 'ArrowRight' ? 1 : -1));
      var atual = $('week').querySelector('.day[aria-selected="true"]');
      if (atual) atual.focus();
    });
  }

  async function carregarCatalogo() {
    try {
      var r = await fetch('/api/catalogo');
      if (!r.ok) throw new Error('catálogo indisponível');
      var data = await r.json();
      (data.itens || []).forEach(function (p) { produtos[p.codigo] = p; });
      catalogoCarregado = true;
    } catch (e) {
      catalogoCarregado = false;
    }
    if (dias.length) renderCesta(dias[selecionado].campanha);
  }

  function init() {
    dias = montarDias();
    if (!dias.length) return;
    ligarEventos();
    renderTudo();
    carregarCatalogo();
  }

  document.addEventListener('DOMContentLoaded', init);
})();
