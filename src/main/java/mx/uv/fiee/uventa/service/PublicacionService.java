package mx.uv.fiee.uventa.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import mx.uv.fiee.uventa.dto.ImagenVista;
import mx.uv.fiee.uventa.dto.PublicacionForm;
import mx.uv.fiee.uventa.dto.TarjetaPublicacion;
import mx.uv.fiee.uventa.model.Categoria;
import mx.uv.fiee.uventa.model.EstadoPublicacion;
import mx.uv.fiee.uventa.model.ImagenPublicacion;
import mx.uv.fiee.uventa.model.Publicacion;
import mx.uv.fiee.uventa.model.TipoOferta;
import mx.uv.fiee.uventa.model.Usuario;
import mx.uv.fiee.uventa.repository.CategoriaRepository;
import mx.uv.fiee.uventa.repository.PublicacionRepository;
import mx.uv.fiee.uventa.repository.UsuarioRepository;
import mx.uv.fiee.uventa.util.PuntosEntrega;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service 
@RequiredArgsConstructor 
public class PublicacionService {
    public static final int MAX_FOTOS = 4;

    private final PublicacionRepository publicaciones;
    private final CategoriaRepository categorias;
    private final UsuarioRepository usuarios;
    private final AlmacenImagenes almacen;


    @Transactional
    public Long crear(String correo, PublicacionForm f, List<MultipartFile> fotos) {
        Usuario autor = usuario(correo);
        Categoria categoria = categoria(f.getCategoriaId());
        List<MultipartFile> nuevas = noVacias(fotos);
        if (nuevas.size() > MAX_FOTOS) {
            throw new PublicacionException("fotos", "Puedes subir máximo " + MAX_FOTOS + " fotos.");
        }

        Publicacion p = new Publicacion();
        p.setUsuario(autor);
        aplicar(p, f, categoria);

        List<String> guardadas = new ArrayList<>();
        try {
            short orden = 1;
            for (MultipartFile mf : nuevas) {
                agregarImagen(p, almacen.guardar(mf), orden++, guardadas);
            }
            publicaciones.save(p);
            return p.getId();
        } catch (RuntimeException e) {
            guardadas.forEach(almacen::borrar);
            throw e;
        }
    }

    @Transactional
    public void actualizar(Long id, String correo, PublicacionForm f,
                           List<MultipartFile> fotos, List<Long> quitar) {
        Publicacion p = propia(id, correo);
        Categoria categoria = categoria(f.getCategoriaId());

        Set<Long> aQuitar = new HashSet<>();
        if (quitar != null) quitar.stream().filter(Objects::nonNull).forEach(aQuitar::add);
        long quedan = p.getImagenes().stream().filter(i -> !aQuitar.contains(i.getId())).count();
        List<MultipartFile> nuevas = noVacias(fotos);
        if (quedan + nuevas.size() > MAX_FOTOS) {
            throw new PublicacionException("fotos", "Una publicación puede tener máximo " + MAX_FOTOS
                    + " fotos. Quita alguna antes de agregar más.");
        }

        aplicar(p, f, categoria);
        p.setFechaActualizacion(Instant.now());

        List<String> guardadas = new ArrayList<>();
        try {
            int max = p.getImagenes().stream().mapToInt(i -> i.getOrden()).max().orElse(0);
            short orden = (short) (max + 1);
            for (MultipartFile mf : nuevas) {
                agregarImagen(p, almacen.guardar(mf), orden++, guardadas);
            }
            List<ImagenPublicacion> removidas = p.getImagenes().stream()
                    .filter(i -> i.getId() != null && aQuitar.contains(i.getId())).toList();
            p.getImagenes().removeAll(removidas);   // orphanRemoval borra las filas
            publicaciones.save(p);
            removidas.forEach(i -> almacen.borrar(i.getUrl()));
        } catch (RuntimeException e) {
            guardadas.forEach(almacen::borrar);
            throw e;
        }
    }

    /** Devuelve el mensaje que se muestra al usuario. */
    @Transactional
    public String cambiarEstado(Long id, String correo, String accion) {
        Publicacion p = propia(id, correo);
        EstadoPublicacion actual = p.getEstado();
        switch (accion) {
            case "pausar" -> {
                exigir(actual == EstadoPublicacion.ACTIVA || actual == EstadoPublicacion.RESERVADA);
                p.setEstado(EstadoPublicacion.PAUSADA);
                return "Publicación pausada. Ya no aparece en el catálogo.";
            }
            case "activar" -> {
                exigir(actual == EstadoPublicacion.PAUSADA || actual == EstadoPublicacion.FINALIZADA);
                p.setEstado(EstadoPublicacion.ACTIVA);
                p.setFechaActualizacion(Instant.now());
                return "Tu publicación está activa otra vez.";
            }
            case "finalizar" -> {
                exigir(actual != EstadoPublicacion.FINALIZADA);
                p.setEstado(EstadoPublicacion.FINALIZADA);
                return "¡Listo! Marcamos tu publicación como finalizada.";
            }
            case "eliminar" -> {
                p.setEstado(EstadoPublicacion.ELIMINADA);
                p.getImagenes().forEach(i -> almacen.borrar(i.getUrl()));
                p.getImagenes().clear();
                return "Publicación eliminada.";
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    // ---------------------------------------------------------------- consultas

    @Transactional(readOnly = true)
    public List<TarjetaPublicacion> misPublicaciones(String correo) {
        Usuario u = usuario(correo);
        Set<Long> destacadas = new HashSet<>(publicaciones.idsDestacadasVigentes());
        return publicaciones
                .findByUsuarioIdAndEstadoNotOrderByFechaPublicacionDesc(u.getId(), EstadoPublicacion.ELIMINADA)
                .stream().map(p -> TarjetaPublicacion.de(p, destacadas.contains(p.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public PublicacionForm formularioDe(Long id, String correo) {
        return PublicacionForm.de(propia(id, correo));
    }

    @Transactional(readOnly = true)
    public List<ImagenVista> imagenesDe(Long id, String correo) {
        return propia(id, correo).getImagenes().stream()
                .map(i -> new ImagenVista(i.getId(), i.getUrl())).toList();
    }

    // ------------------------------------------------------------------ ayudas

    private void aplicar(Publicacion p, PublicacionForm f, Categoria categoria) {
        TipoOferta tipo = f.getTipo();
        BigDecimal precio = BigDecimal.ZERO;
        if (tipo == TipoOferta.VENTA) {
            if (f.getPrecio() == null || f.getPrecio().signum() <= 0) {
                throw new PublicacionException("precio", "Escribe un precio mayor a $0.");
            }
            precio = f.getPrecio();
        }
        String punto = limpiar(f.getPuntoEntrega());
        if (punto != null && !PuntosEntrega.LISTA.contains(punto)) {
            throw new PublicacionException("puntoEntrega", "Elige un punto de entrega de la lista.");
        }
        p.setTipo(tipo);
        p.setTitulo(f.getTitulo().trim());
        p.setDescripcion(f.getDescripcion().trim());
        p.setCategoria(categoria);
        p.setCondicion(f.getCondicion());
        p.setPrecio(precio);
        p.setMateria(limpiar(f.getMateria()));
        p.setSemestre(f.getSemestre() == null ? null : f.getSemestre().shortValue());
        p.setPuntoEntrega(punto);
    }

    private void agregarImagen(Publicacion p, String url, short orden, List<String> guardadas) {
        guardadas.add(url);
        ImagenPublicacion im = new ImagenPublicacion();
        im.setPublicacion(p);
        im.setUrl(url);
        im.setOrden(orden);
        p.getImagenes().add(im);
    }

    private Publicacion propia(Long id, String correo) {
        return publicaciones.findById(id)
                .filter(p -> p.getEstado() != EstadoPublicacion.ELIMINADA)
                .filter(p -> p.getUsuario().getCorreoInstitucional().equalsIgnoreCase(correo))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private Usuario usuario(String correo) {
        return usuarios.findByCorreoInstitucionalIgnoreCase(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
    }

    private Categoria categoria(Long id) {
        return categorias.findById(id)
                .filter(Categoria::isActiva)
                .orElseThrow(() -> new PublicacionException("categoriaId", "Elige una categoría de la lista."));
    }

    private static List<MultipartFile> noVacias(List<MultipartFile> fotos) {
        return fotos == null ? List.of() : fotos.stream().filter(f -> f != null && !f.isEmpty()).toList();
    }

    private static String limpiar(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static void exigir(boolean condicion) {
        if (!condicion) throw new ResponseStatusException(HttpStatus.CONFLICT);
    }

}
