package mx.uv.fiee.uventa.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import mx.uv.fiee.uventa.model.Condicion;
import mx.uv.fiee.uventa.model.ImagenPublicacion;
import mx.uv.fiee.uventa.model.Publicacion;
import mx.uv.fiee.uventa.model.TipoOferta;
import mx.uv.fiee.uventa.model.Usuario;
import mx.uv.fiee.uventa.util.Formato;

@Getter @Builder
public class DetallePublicacion {
    private final Long id;
    private final String titulo;
    private final String descripcion;
    private final TipoOferta tipo;
    private final BigDecimal precio;
    private final Condicion condicion; 
    private final String categoria;
    private final String categoriaIcono;
    private final int tono;
    private final String materia;
    private final Short semestre;
    private final String puntoEntrega;
    private final String vendedor;
    private final String iniciales;
    private final String carreraVendedor;
    private final Instant fecha;
    private final List<String> imagenes;
    private final boolean destacada;

    public String getPrecioTexto() { return Formato.precioTexto(tipo, precio); }
    public String getHace() { return Formato.hace(fecha); }

    public static DetallePublicacion de(Publicacion p, boolean destacada) {
        Usuario u = p.getUsuario();
        return DetallePublicacion.builder()
                .id(p.getId())
                .titulo(p.getTitulo())
                .descripcion(p.getDescripcion())
                .tipo(p.getTipo())
                .precio(p.getPrecio())
                .condicion(p.getCondicion())
                .categoria(p.getCategoria().getNombre())
                .categoriaIcono(p.getCategoria().getIcono())
                .tono((int) (p.getCategoria().getId() % 6))
                .materia(p.getMateria())
                .semestre(p.getSemestre())
                .puntoEntrega(p.getPuntoEntrega())
                .vendedor(Formato.nombreCorto(u.getNombre(), u.getApellidos()))
                .iniciales(Formato.iniciales(u.getNombre(), u.getApellidos()))
                .carreraVendedor(u.getCarrera() == null ? null : u.getCarrera().getNombre())
                .fecha(p.getFechaPublicacion())
                .imagenes(p.getImagenes().stream().map(ImagenPublicacion::getUrl).toList())
                .destacada(destacada)
                .build();
    }
    
}
