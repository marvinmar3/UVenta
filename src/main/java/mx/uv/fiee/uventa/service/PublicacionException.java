package mx.uv.fiee.uventa.service;

/*Error de negocio al publicar; "campo" dice donde mostrarlo ("fotos" si es de las imagenes) */
public class PublicacionException extends RuntimeException {
    private final String campo;

    public PublicacionException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() { return campo; }
    
}
