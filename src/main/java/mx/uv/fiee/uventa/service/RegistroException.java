package mx.uv.fiee.uventa.service;

//error de negocio asociado a un campo del formulario de registro
public class RegistroException extends RuntimeException {
    private final String campo;

    public RegistroException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
    
}
