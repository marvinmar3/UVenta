package mx.uv.fiee.uventa.model;

import lombok.Getter;

/** Tipo de publicación. etiqueta/icono/clase se usan en las vistas. */
@Getter
public enum TipoOferta {
    VENTA("Venta", "bi-tag", "venta", "Vender", "Pon el precio que quieras", "Marcar como vendida"),
    INTERCAMBIO("Intercambio", "bi-arrow-left-right", "intercambio","Intercambiar", "Cambia por algo que necesites", "Marcar como intercambiada"),
    DONACION("Donación", "bi-gift", "donacion","Donar", "Regalo a un compañero", "Marcar como entregada");

    private final String etiqueta;
    private final String icono;
    private final String clase;
    private final String verbo;
    private final String ayuda;
    private final String finalizador;

    TipoOferta(String etiqueta, String icono, String clase, String verbo, String ayuda, String finalizador) {
        this.etiqueta = etiqueta;
        this.icono = icono;
        this.clase = clase;
        this.verbo = verbo;
        this.ayuda = ayuda;
        this.finalizador = finalizador;
    }
}
