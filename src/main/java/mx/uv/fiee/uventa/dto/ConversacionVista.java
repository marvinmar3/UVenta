package mx.uv.fiee.uventa.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/** Todo lo que necesita la pantalla de una conversación. */
@Getter @Builder
public class ConversacionVista {
    private final Long id;
    private final Long publicacionId;
    private final String publicacionTitulo;
    private final String publicacionPrecio;
    private final String publicacionImagen;
    private final String categoriaIcono;
    private final int tono;
    private final boolean publicacionDisponible;
    private final String otroNombre;
    private final String otroIniciales;
    private final boolean soyVendedor;
    private final List<MensajeVista> mensajes;
    private final long ultimoId;
}
