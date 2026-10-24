package mx.uv.fiee.uventa.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import mx.uv.fiee.uventa.model.Condicion;
import mx.uv.fiee.uventa.model.Publicacion;
import mx.uv.fiee.uventa.model.TipoOferta;

/*datos del formulario de publicar y editar (las fotos viajan aparte) */
@Getter @Setter 
public class PublicacionForm {
    
    @NotNull(message = "Elige qué quieres hacer.")
    private TipoOferta tipo = TipoOferta.VENTA;

    @NotBlank(message = "Escribe el titulo.")
    @Size(max = 180, message = "El titulo no puede tener más de {max} caracteres.")
    private String titulo;

    @NotNull (message = "Elige la categoría.")
    private Long categoriaId;

    @NotBlank(message="Describe lo que publicas.")
    @Size(max=2000, message="Máximo {max} caracteres.")
    private String descripcion;

    @NotNull (message= "Elige la condición del artículo.")
    private Condicion condicion = Condicion.USADO;

    @DecimalMin (value="0", message="El precio no puede ser negativo.")
    @Digits(integer=5, fraction =2, message="El precio no puede tener más de {integer} dígitos y {fraction} decimales.")
    private BigDecimal precio;

    @Size(max=120, message="Máximo {max} caracteres.")
    private String materia;

    @Min(value=1, message="Semestre inválido.")
    @Max(value=10, message="Semestre inválido.")
    private Integer semestre;

    @Size (max=255)
    private String puntoEntrega = "Biblioteca";

    public static PublicacionForm de(Publicacion p) {
        PublicacionForm f = new PublicacionForm();
        f.setTipo(p.getTipo());
        f.setTitulo(p.getTitulo());
        f.setCategoriaId(p.getCategoria().getId());
        f.setDescripcion(p.getDescripcion());
        f.setCondicion(p.getCondicion() == null ? Condicion.USADO : p.getCondicion());
        f.setPrecio(p.getPrecio());
        f.setMateria(p.getMateria());
        f.setSemestre(p.getSemestre() == null ? null : p.getSemestre().intValue());
        f.setPuntoEntrega(p.getPuntoEntrega());
        return f;
    }
}
