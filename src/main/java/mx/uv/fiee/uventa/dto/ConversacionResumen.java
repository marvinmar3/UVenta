package mx.uv.fiee.uventa.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import mx.uv.fiee.uventa.util.Formato;

@Getter @Builder
public class ConversacionResumen {
    private final Long id;
    private final String otroNombre;
    private final String otroIniciales;
    private final String titulo;
    private final String categoriaIcono;
    private final int tono;
    private final String ultimoTexto;
    private final Instant fecha;
    private final boolean soyVendedor;
    private final long sinLeer;

    public String getHace() { return Formato.hace(fecha); }
}
