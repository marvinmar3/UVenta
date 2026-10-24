package mx.uv.fiee.uventa.service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import mx.uv.fiee.uventa.dto.DetallePublicacion;
import mx.uv.fiee.uventa.dto.FiltroCatalogo;
import mx.uv.fiee.uventa.dto.TarjetaPublicacion;
import mx.uv.fiee.uventa.model.Carrera;
import mx.uv.fiee.uventa.model.Categoria;
import mx.uv.fiee.uventa.model.EstadoPublicacion;
import mx.uv.fiee.uventa.model.Publicacion;
import mx.uv.fiee.uventa.repository.CarreraRepository;
import mx.uv.fiee.uventa.repository.CategoriaRepository;
import mx.uv.fiee.uventa.repository.PublicacionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private static final int POR_PAGINA = 12;

    private final PublicacionRepository publicaciones;
    private final CategoriaRepository categorias;
    private final CarreraRepository carreras;

    public List<Categoria> categorias() {
        return categorias.findByActivaTrueAndCategoriaPadreIsNullOrderByNombre();
    }

    public List<Carrera> carreras() {
        return carreras.findByActivaTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Page<TarjetaPublicacion> buscar(FiltroCatalogo f) {
        Set<Long> destacadas = new HashSet<>(publicaciones.idsDestacadasVigentes());
        PageRequest pagina = PageRequest.of(Math.max(f.getPage(), 0), POR_PAGINA, orden(f.getOrden()));
        return publicaciones.findAll(especificacion(f), pagina)
                .map(p -> TarjetaPublicacion.de(p, destacadas.contains(p.getId())));
    }

    @Transactional(readOnly = true)
    public List<TarjetaPublicacion> destacadas(int max) {
        List<Long> ids = publicaciones.idsDestacadasVigentes();
        if (ids.isEmpty()) return List.of();
        return publicaciones.findAllById(ids).stream()
                .filter(p -> p.getEstado() == EstadoPublicacion.ACTIVA)
                .sorted(Comparator.comparing(Publicacion::getFechaPublicacion).reversed())
                .limit(max)
                .map(p -> TarjetaPublicacion.de(p, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public DetallePublicacion detalle(Long id) {
        Publicacion p = publicaciones.findById(id)
                .filter(x -> x.getEstado() == EstadoPublicacion.ACTIVA
                          || x.getEstado() == EstadoPublicacion.RESERVADA)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean destacada = publicaciones.idsDestacadasVigentes().contains(id);
        return DetallePublicacion.de(p, destacada);
    }

    private static Sort orden(String orden) {
        if ("precio_asc".equals(orden)) return Sort.by("precio").ascending().and(Sort.by("id").descending());
        if ("precio_desc".equals(orden)) return Sort.by("precio").descending().and(Sort.by("id").descending());
        return Sort.by("fechaPublicacion").descending().and(Sort.by("id").descending());
    }

    private static Specification<Publicacion> especificacion(FiltroCatalogo f) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("estado"), EstadoPublicacion.ACTIVA));

            if (f.getQ() != null && !f.getQ().isBlank()) {
                String like = "%" + escapar(f.getQ().trim().toLowerCase()) + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("titulo")), like, '\\'),
                        cb.like(cb.lower(root.get("descripcion")), like, '\\'),
                        cb.like(cb.lower(root.get("materia")), like, '\\')));
            }
            if (f.getCategoria() != null) {
                ps.add(cb.equal(root.get("categoria").get("id"), f.getCategoria()));
            }
            if (f.getTipo() != null) {
                ps.add(cb.equal(root.get("tipo"), f.getTipo()));
            }
            if (f.getCarrera() != null) {
                ps.add(cb.equal(root.get("usuario").get("carrera").get("id"), f.getCarrera()));
            }
            if (f.getSemestre() != null) {
                ps.add(cb.equal(root.get("semestre"), f.getSemestre().shortValue()));
            }
            if (f.getPrecioMax() != null) {
                ps.add(cb.lessThanOrEqualTo(root.<java.math.BigDecimal>get("precio"), f.getPrecioMax()));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    /** Evita que % y _ escritos por el usuario actúen como comodines. */
    private static String escapar(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
