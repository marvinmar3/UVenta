//formulario de publicar/editar con vista previa, precio segun el tipo y contador de texto
(function(){
    'use strict';
    var input = document.getElementById('fotos');
    if (!input) return;

    var MAX= parseInt(input.dataset.max || '4', '10');
    var MAX_BYTES = 5*1024*1024; // 5 MB
    var TIPOS = ['image/jpeg', 'image/png'];
    var previews = document.getElementById('previews');
    var msg = document.getElementById('msgFotos');
    var existentes = Array.prototype.slice.call(document.querySelectorAll('.uv-thumb__chk'));
    var archivos =[];
    var convirtiendo = false;

    function esHeic(archivo) {
    return /image\/hei[cf]/i.test(archivo.type)
        || /\.(heic|heif)$/i.test(archivo.name);
}

    async function convertirHeic(archivo) {
      if (!esHeic(archivo)) {
          return archivo;
      }

      if (typeof heic2any !== 'function') {
          throw new Error('No se pudo cargar el conversor HEIC.');
      }

      var resultado = await heic2any({
          blob: archivo,
          toType: 'image/jpeg',
          quality: 0.85
      });

      var blob = Array.isArray(resultado) ? resultado[0] : resultado;
      var nombre = archivo.name.replace(/\.(heic|heif)$/i, '.jpg');

      return new File([blob], nombre, {
          type: 'image/jpeg',
          lastModified: Date.now()
      });
    }


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

    input.addEventListener('change', async function () {
      if (convirtiendo) {
          return;
      }

      convirtiendo = true;
      input.disabled = true;

      var cupo = capacidad();
      var rechazadas = [];

      try {
          var seleccionadas = Array.prototype.slice.call(input.files);

          for (var i = 0; i < seleccionadas.length; i++) {
              if (archivos.length >= cupo) {
                  rechazadas.push('Máximo ' + MAX + ' fotos en total');
                  break;
              }

              var original = seleccionadas[i];
              var archivo;

              try {
                  archivo = await convertirHeic(original);
              } catch (error) {
                  rechazadas.push(original.name + ': no se pudo convertir');
                  continue;
              }

              if (TIPOS.indexOf(archivo.type) === -1) {
                  rechazadas.push(original.name + ': solo JPG, PNG o HEIC');
                  continue;
              }

              if (archivo.size > MAX_BYTES) {
                  rechazadas.push(original.name + ': supera los 5 MB después de convertir');
                  continue;
              }

              archivos.push(archivo);
          }

          sincronizar();
          dibujar();

          aviso(
              rechazadas.length
                  ? 'No se agregó: ' + Array.from(new Set(rechazadas)).join('; ') + '.'
                  : ''
          );
      } finally {
          convirtiendo = false;
          input.disabled = false;
      }
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
  form.addEventListener('submit', function (evento) {
    if (convirtiendo) {
        evento.preventDefault();
        aviso('Espera a que terminen de convertir las imágenes.');
        return;
    }

    boton.disabled = true;
    boton.innerHTML =
        '<span class="spinner-border spinner-border-sm" aria-hidden="true"></span> Guardando…';
  });

    
})