package mx.uv.fiee.uventa.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice 
public class ErroresWeb {
    /* Maneja los errores globales de la aplicación */
    /*si se sube alfo demaciado pesao, regresa al mismo formulario con un aviso */
    @ExceptionHandler (MaxUploadSizeExceededException.class)
    public String archivoMuyGrande(HttpServletRequest req) {
        return "redirect:" + req.getRequestURI() + "?error=tamano";
    }
}