package mx.uv.fiee.uventa.config;

import mx.uv.fiee.uventa.model.Usuario;
import mx.uv.fiee.uventa.repository.UsuarioRepository;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Deja el nombre del usuario en sesión disponible en todas las vistas (barra superior). */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final UsuarioRepository usuarios;

    @ModelAttribute("nombreUsuario")
    public String nombreUsuario(Principal principal) {
        if (principal == null) return null;
        return usuarios.findByCorreoInstitucionalIgnoreCase(principal.getName())
                .map(Usuario::getNombre).orElse(null);
    }
}
