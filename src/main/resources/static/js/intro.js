// Presentación animada de UVenta: cada elemento con data-s aparece en ese segundo
// y desaparece en data-e. La línea de tiempo sigue a la voz en off; si el navegador
// bloquea el sonido, avanza en silencio con un reloj propio.
(function () {
  'use strict';
  var stage = document.getElementById('stage');
  if (!stage) return;

  var DURACION = 52.3;      // segundos de la voz en off
  var FINAL = 51.5;         // cuadro final: logo y lema
  var reducir = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  var btnSonido = document.getElementById('snd');
  var btnOmitir = document.getElementById('skip');
  var btnRepetir = document.getElementById('replay');
  var barra = document.getElementById('prog');
  var audio = new Audio(stage.dataset.audio);
  audio.preload = 'auto';

  var items = Array.prototype.map.call(stage.querySelectorAll('[data-s]'), function (e) {
    return { e: e, s: parseFloat(e.dataset.s), x: e.dataset.e ? parseFloat(e.dataset.e) : 1e9, on: null };
  });

  // ?t=12 congela la animación en ese segundo (útil para revisar el diseño).
  var congelado = (location.search.match(/[?&]t=([\d.]+)/) || [])[1];

  var t = 0;                // segundo actual
  var t0 = performance.now();
  var conSonido = false;    // true mientras el audio manda sobre el reloj
  var terminado = false;
  var enPantalla = true;

  function pintar(seg) {
    for (var i = 0; i < items.length; i++) {
      var o = items[i];
      var on = seg >= o.s && seg < o.x;
      if (on !== o.on) { o.on = on; o.e.classList.toggle('in', on); }
    }
    barra.style.transform = 'scaleX(' + Math.min(seg / DURACION, 1) + ')';
  }

  function etiquetaSonido(activo) {
    btnSonido.setAttribute('aria-pressed', activo ? 'true' : 'false');
    btnSonido.querySelector('span').textContent = activo ? 'Silenciar' : 'Activar sonido';
    var ic = btnSonido.querySelector('i');
    ic.className = 'bi ' + (activo ? 'bi-volume-up-fill' : 'bi-volume-mute-fill');
    if (activo) btnSonido.classList.remove('pulse');
  }

  function sinSonido() {
    audio.pause();
    conSonido = false;
    t0 = performance.now() - t * 1000;
    etiquetaSonido(false);
  }

  function activarSonido(quiere) {
    if (!quiere) { sinSonido(); return; }
    audio.currentTime = Math.min(t, DURACION);
    audio.play().then(function () { conSonido = true; etiquetaSonido(true); })
               .catch(function () { btnSonido.classList.add('pulse'); });
  }

  function terminar(irAContenido) {
    if (terminado) return;
    terminado = true;
    audio.pause();
    conSonido = false;
    t = FINAL;
    pintar(FINAL);
    barra.style.transform = 'scaleX(1)';
    btnSonido.hidden = true;
    btnOmitir.hidden = true;
    btnRepetir.hidden = false;
    if (irAContenido) {
      var destino = document.getElementById('como');
      if (destino) destino.scrollIntoView({ behavior: reducir ? 'auto' : 'smooth' });
    }
  }

  function repetir() {
    terminado = false;
    t = 0;
    t0 = performance.now();
    audio.currentTime = 0;
    btnRepetir.hidden = true;
    btnSonido.hidden = false;
    btnOmitir.hidden = false;
    etiquetaSonido(false);
    pintar(0);
    activarSonido(true);
    requestAnimationFrame(tick);
  }

  function tick() {
    if (terminado) return;
    if (enPantalla && !document.hidden) {
      if (congelado !== undefined) t = parseFloat(congelado);
      else t = conSonido ? audio.currentTime : (performance.now() - t0) / 1000;
      pintar(t);
      if (t >= DURACION + 0.3 || (conSonido && audio.ended)) { terminar(false); return; }
    }
    requestAnimationFrame(tick);
  }

  // Pausa la presentación (y la voz) cuando no se ve; la retoma al volver.
  function revisarVisibilidad(visible) {
    if (visible === enPantalla) return;
    enPantalla = visible;
    if (terminado) return;
    if (!visible) { if (conSonido) audio.pause(); return; }
    if (conSonido) {
      audio.play().catch(sinSonido);
    } else {
      t0 = performance.now() - t * 1000;
    }
  }
  if ('IntersectionObserver' in window) {
    new IntersectionObserver(function (es) {
      revisarVisibilidad(es[0].isIntersecting && !document.hidden);
    }, { threshold: 0.25 }).observe(stage);
  }
  document.addEventListener('visibilitychange', function () {
    revisarVisibilidad(!document.hidden);
  });

  btnSonido.addEventListener('click', function () { activarSonido(!conSonido); });
  btnOmitir.addEventListener('click', function () { terminar(true); });
  btnRepetir.addEventListener('click', repetir);
  audio.addEventListener('error', function () { btnSonido.hidden = true; });

  // Sin animaciones: se muestra directamente el cuadro final.
  if (reducir) {
    terminar(false);
    btnRepetir.hidden = true;
    return;
  }

  // Intenta arrancar con sonido; si el navegador lo impide, sigue en silencio.
  etiquetaSonido(false);
  if (congelado === undefined) {
    audio.play().then(function () { conSonido = true; t0 = performance.now(); etiquetaSonido(true); })
               .catch(function () { btnSonido.classList.add('pulse'); });
  }
  requestAnimationFrame(tick);
})();
