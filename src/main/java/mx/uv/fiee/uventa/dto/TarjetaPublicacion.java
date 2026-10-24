package mx.uv.fiee.uventa.dto;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import mx.uv.fiee.uventa.model.Publicacion;
import mx.uv.fiee.uventa.model.TipoOferta;
import mx.uv.fiee.uventa.model.Usuario;
import mx.uv.fiee.uventa.util.Formato;

/** Datos ya listos para pintar una tarjeta (evita cargar entidades en la vista). */
@Getter @Builder
public class TarjetaPublicacion {
    private final Long id;
    private final String titulo;
    private final TipoOferta tipo;
    private final BigDecimal precio;
    private final String categoria;
    private final String categoriaIcono;
    private final int tono;
    private final String materia;
    private final Short semestre;
    private final String vendedor;
    private final String iniciales;
    private final Instant fecha;
    private final String imagenUrl;
    private final boolean destacada;

    public String getPrecioTexto() { return Formato.precioTexto(tipo, precio); }
    public String getMeta() { return Formato.meta(materia, semestre); }
    public String getHace() { return Formato.hace(fecha); }

    public static TarjetaPublicacion de(Publicacion p, boolean destacada) {
        Usuario u = p.getUsuario();
        return TarjetaPublicacion.builder()
                .id(p.getId())
                .titulo(p.getTitulo())
                .tipo(p.getTipo())
                .precio(p.getPrecio())
                .categoria(p.getCategoria().getNombre())
                .categoriaIcono(p.getCategoria().getIcono())
                .tono((int) (p.getCategoria().getId() % 6))
                .materia(p.getMateria())
                .semestre(p.getSemestre())
                .vendedor(Formato.nombreCorto(u.getNombre(), u.getApellidos()))
                .iniciales(Formato.iniciales(u.getNombre(), u.getApellidos()))
                .fecha(p.getFechaPublicacion())
                .imagenUrl(p.getImagenes().isEmpty() ? null : p.getImagenes().get(0).getUrl())
                .destacada(destacada)
                .build();
    }
}