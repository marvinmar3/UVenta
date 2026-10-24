package mx.uv.fiee.uventa.model;

import lombok.Getter;

@Getter
public enum Condicion {
    NUEVO("Nuevo"),
    COMO_NUEVO("Como nuevo"),
    USADO("Usado"),
    DESGASTADO("Con desgaste");

    private final String etiqueta;

    Condicion(String etiqueta) { this.etiqueta = etiqueta; }
}
