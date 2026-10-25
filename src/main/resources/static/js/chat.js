// Chat: envío sin recargar, mensajes nuevos cada pocos segundos y ticks de "leído".
(function () {
  'use strict';
  var caja = document.getElementById('mensajes');
  if (!caja) return;

  var base = caja.dataset.base;
  var csrf = caja.dataset.csrf;
  var csrfHeader = caja.dataset.csrfHeader;
  var form = document.getElementById('formMensaje');
  var campo = document.getElementById('textoMensaje');
  var boton = form.querySelector('button[type=submit]');
  var errorBox = document.getElementById('errorChat');
  var ultimo = parseInt(caja.dataset.ultimo || '0', 10);
  var ocupado = false;

  function mostrarError(t) { errorBox.textContent = t || ''; errorBox.hidden = !t; }
  function alFinal() { return caja.scrollHeight - caja.scrollTop - caja.clientHeight < 140; }
  function bajar() { caja.scrollTop = caja.scrollHeight; }

  function ultimoDia() {
    var m = caja.querySelectorAll('.uv-msg');
    return m.length ? m[m.length - 1].dataset.dia : '';
  }

  // Agrega las burbujas que llegan en HTML; ignora las que ya están en pantalla.
  function agregar(html) {
    var t = document.createElement('template');
    t.innerHTML = html.trim();
    var agregados = 0;
    Array.prototype.slice.call(t.content.children).forEach(function (n) {
      if (n.classList.contains('uv-msg') && caja.querySelector('.uv-msg[data-id="' + n.dataset.id + '"]')) return;
      caja.appendChild(n);
      if (n.classList.contains('uv-msg')) {
        agregados++;
        ultimo = Math.max(ultimo, parseInt(n.dataset.id, 10));
      }
    });
    if (agregados) {
      var inicio = document.getElementById('inicioChat');
      if (inicio) inicio.remove();
    }
    return agregados;
  }

  function marcarLeidos(hasta) {
    var h = parseInt(hasta || '0', 10);
    if (!h) return;
    caja.querySelectorAll('.uv-msg[data-propio="true"]').forEach(function (m) {
      if (parseInt(m.dataset.id, 10) <= h) {
        var t = m.querySelector('.uv-msg__ticks');
        if (t && !t.classList.contains('is-leido')) {
          t.classList.remove('bi-check2');
          t.classList.add('bi-check2-all', 'is-leido');
        }
      }
    });
  }

  function aLogin(r) {
    if (r.redirected || r.status === 401 || r.status === 403) { window.location.href = '/login'; return true; }
    return false;
  }

  async function sondear() {
    if (ocupado || document.hidden) return;
    ocupado = true;
    try {
      var r = await fetch(base + '/mensajes?despues=' + ultimo + '&dia=' + encodeURIComponent(ultimoDia()), {
        headers: { 'X-Requested-With': 'fetch' }, credentials: 'same-origin', cache: 'no-store'
      });
      if (aLogin(r) || !r.ok) return;
      marcarLeidos(r.headers.get('X-Leidos'));
      var seguir = alFinal();
      if (agregar(await r.text()) && seguir) bajar();
    } catch (e) { /* sin conexión: se reintenta en el siguiente ciclo */ }
    finally { ocupado = false; }
  }

  async function enviar() {
    var texto = campo.value.trim();
    if (!texto) return;
    boton.disabled = true;
    mostrarError('');
    try {
      var cuerpo = new URLSearchParams({ texto: texto, dia: ultimoDia() });
      var cabeceras = { 'X-Requested-With': 'fetch', 'Content-Type': 'application/x-www-form-urlencoded' };
      cabeceras[csrfHeader] = csrf;
      var r = await fetch(base + '/mensajes', { method: 'POST', headers: cabeceras, body: cuerpo, credentials: 'same-origin' });
      if (aLogin(r)) return;
      if (r.ok) {
        agregar(await r.text());
        campo.value = '';
        ajustarAltura();
        bajar();
      } else if (r.status === 429) {
        mostrarError('Vas muy rápido. Espera un momento antes de enviar otro mensaje.');
      } else if (r.status === 400) {
        mostrarError('Escribe un mensaje de hasta ' + campo.maxLength + ' caracteres.');
      } else {
        mostrarError('No se pudo enviar. Intenta de nuevo.');
      }
    } catch (e) {
      mostrarError('Sin conexión. Revisa tu internet e intenta de nuevo.');
    } finally {
      boton.disabled = false;
      campo.focus();
    }
  }

  function ajustarAltura() {
    campo.style.height = 'auto';
    campo.style.height = Math.min(campo.scrollHeight, 120) + 'px';
  }

  form.addEventListener('submit', function (e) { e.preventDefault(); enviar(); });
  campo.addEventListener('input', ajustarAltura);
  campo.addEventListener('keydown', function (e) {
    if (e.key === 'Enter' && !e.shiftKey && !e.isComposing) { e.preventDefault(); form.requestSubmit(); }
  });

  document.querySelectorAll('.uv-sug').forEach(function (b) {
    b.addEventListener('click', function () {
      campo.value = (campo.value ? campo.value + ' ' : '') + b.dataset.texto;
      ajustarAltura();
      campo.focus();
    });
  });

  document.addEventListener('visibilitychange', function () { if (!document.hidden) sondear(); });
  window.setInterval(sondear, 3000);
  bajar();
})();