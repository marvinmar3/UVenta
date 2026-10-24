package mx.uv.fiee.uventa.model;

import lombok.Getter;

/** Tipo de publicación. etiqueta/icono/clase se usan en las vistas. */
@Getter
public enum TipoOferta {
    VENTA("Venta", "bi-tag", "venta"),
    INTERCAMBIO("Intercambio", "bi-arrow-left-right", "intercambio"),
    DONACION("Donación", "bi-gift", "donacion");

    private final String etiqueta;
    private final String icono;
    private final String clase;

    TipoOferta(String etiqueta, String icono, String clase) {
        this.etiqueta = etiqueta;
        this.icono = icono;
        this.clase = clase;
    }
}
