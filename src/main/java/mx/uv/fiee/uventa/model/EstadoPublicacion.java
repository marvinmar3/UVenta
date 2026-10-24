package mx.uv.fiee.uventa.model;

import lombok.Getter;

@Getter
public enum EstadoPublicacion { 
    ACTIVA("Activa", "activa"),
    RESERVADA("Reservada", "reservada"),
    FINALIZADA("Finalizada", "finalizada"),
    PAUSADA("Pausada", "pausada"),
    ELIMINADA("Eliminada", "eliminada"); 
    
    private final String etiqueta;
    private final String clase;

    EstadoPublicacion(String etiqueta, String clase) {
        this.etiqueta = etiqueta;
        this.clase = clase;
    }
}
