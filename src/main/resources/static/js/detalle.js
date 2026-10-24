// Cambia la foto grande al tocar una miniatura.
document.querySelectorAll('.uv-gallery__thumb').forEach(function (b) {
  b.addEventListener('click', function () {
    var principal = document.getElementById('fotoPrincipal');
    if (principal) principal.src = b.dataset.src;
  });
});