//formulario de publicar/editar con vista previa, precio segun el tipo y contador de texto
(function(){
    'use strict';
    var input = document.getElementById('fotos');
    if (!input) return;

    var MAX= parseInt(input.dataset.max || '4', '10');
    var MAX_BYTES = 5*1024*1024; // 5 MB
    var TIPOS = ['image/jpeg', 'image/png'];
    var preview = document.getElementById('previews');
    var msj = document.getElementById('msgFotos');
    var existentes = Array.prototype.slice.call(document.querySelectorAll('.uv-thumb__chk'));
    var archivos =[];

    function aviso(texto){
        msg.textContent = texto || '';
        msg.hidden = !texto;
    }

    function capacidad() {
    var quedan = existentes.filter(function (c) { return !c.checked; }).length;
    return Math.max(0, MAX - quedan);
    }

    function sincronizar() {
    // El navegador solo envía lo que está en input.files, así que se reconstruye la lista.
    var dt = new DataTransfer();
    archivos.forEach(function (f) { dt.items.add(f); });
    input.files = dt.files;
    }

    function dibujar() {
        previews.innerHTML = '';
        archivos.forEach(function (f, i) {
        var caja = document.createElement('div');
        caja.className = 'uv-thumb';
        var img = document.createElement('img');
        img.alt = 'Vista previa de ' + f.name;
        img.src = URL.createObjectURL(f);
        img.onload = function () { URL.revokeObjectURL(img.src); };
        var x = document.createElement('button');
        x.type = 'button';
        x.className = 'uv-thumb__remove';
        x.setAttribute('aria-label', 'Quitar ' + f.name);
        x.innerHTML = '<i class="bi bi-x-lg"></i>';
        x.addEventListener('click', function () {
            archivos.splice(i, 1);
            sincronizar();
            dibujar();
            aviso('');
        });
        caja.appendChild(img);
        caja.appendChild(x);
        previews.appendChild(caja);
        });
    }

    input.addEventListener('change', function () {
    var cupo = capacidad();
    var rechazadas = [];
    Array.prototype.slice.call(input.files).forEach(function (f) {
      if (TIPOS.indexOf(f.type) === -1) { rechazadas.push(f.name + ': solo JPG o PNG'); return; }
      if (f.size > MAX_BYTES) { rechazadas.push(f.name + ': pesa más de 5 MB'); return; }
      if (archivos.length >= cupo) { rechazadas.push('Máximo ' + MAX + ' fotos en total'); return; }
      archivos.push(f);
    });
    sincronizar();
    dibujar();
    aviso(rechazadas.length ? 'No se agregó: ' + Array.from(new Set(rechazadas)).join('; ') + '.' : '');
  });

  // Si en la edición se marcan fotos para quitar, hay más espacio; si se desmarcan, menos.
  existentes.forEach(function (c) {
    c.addEventListener('change', function () {
      var cupo = capacidad();
      if (archivos.length > cupo) {
        archivos = archivos.slice(0, cupo);
        sincronizar();
        dibujar();
        aviso('Quitamos las últimas fotos nuevas para respetar el máximo de ' + MAX + '.');
      }
    });
  });

  // Precio solo cuando se vende
  var radios = document.querySelectorAll('input[name="tipo"]');
  var bloquePrecio = document.getElementById('bloquePrecio');
  var descripcion = document.getElementById('descripcion');
  var AYUDA = {
    VENTA: 'Cuenta el estado, qué incluye y cualquier detalle útil.',
    INTERCAMBIO: 'Describe lo que ofreces y qué te gustaría recibir a cambio.',
    DONACION: 'Cuenta qué regalas y cómo quieres entregarlo.'
  };
  function ajustarTipo() {
    var sel = document.querySelector('input[name="tipo"]:checked');
    var tipo = sel ? sel.value : 'VENTA';
    bloquePrecio.hidden = tipo !== 'VENTA';
    descripcion.placeholder = AYUDA[tipo] || '';
  }
  radios.forEach(function (r) { r.addEventListener('change', ajustarTipo); });
  ajustarTipo();

  // Contador de caracteres
  var contador = document.getElementById('contador');
  function contar() { contador.textContent = descripcion.value.length; }
  descripcion.addEventListener('input', contar);
  contar();

  // Evita enviar dos veces el formulario
  var form = document.getElementById('formPublicar');
  var boton = document.getElementById('btnPublicar');
  form.addEventListener('submit', function () {
    boton.disabled = true;
    boton.innerHTML = '<span class="spinner-border spinner-border-sm" aria-hidden="true"></span> Guardando…';
  });

    
})