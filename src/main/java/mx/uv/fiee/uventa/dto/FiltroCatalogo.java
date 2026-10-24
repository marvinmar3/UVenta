package mx.uv.fiee.uventa.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import mx.uv.fiee.uventa.model.TipoOferta;

/** Parámetros de búsqueda de la página principal (vienen en la URL). */
@Getter @Setter
public class FiltroCatalogo {
    private String q;
    private Long categoria;
    private TipoOferta tipo;
    private Long carrera;
    private Integer semestre;
    private BigDecimal precioMax;
    private String orden = "recientes";
    private int page;

    public boolean hayFiltros() {
        return (q != null && !q.isBlank()) || categoria != null || tipo != null
                || carrera != null || semestre != null || precioMax != null;
    }
}