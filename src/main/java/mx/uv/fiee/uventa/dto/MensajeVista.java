package mx.uv.fiee.uventa.dto;

import lombok.Getter;
import lombok.Builder;

@Getter @Builder 
public class MensajeVista {
    private final Long id;
    private final String texto;
    private final boolean propio;
    private final String hora;
    private final String dia;
    private final boolean leido;
}
