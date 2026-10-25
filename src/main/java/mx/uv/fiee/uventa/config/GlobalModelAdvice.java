package mx.uv.fiee.uventa.config;

import mx.uv.fiee.uventa.model.Usuario;
import mx.uv.fiee.uventa.repository.UsuarioRepository;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import mx.uv.fiee.uventa.service.ChatService;
import org.springframework.ui.Model;


/** Deja el nombre del usuario en sesión disponible en todas las vistas (barra superior). */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final UsuarioRepository usuarios;
    private final ChatService chat;

    @ModelAttribute
    public void datosDeSesion(Principal principal, Model model) {
        if (principal == null) return;
        usuarios.findByCorreoInstitucionalIgnoreCase(principal.getName()).ifPresent(u -> {
            model.addAttribute("nombreUsuario", u.getNombre());
            model.addAttribute("mensajesSinLeer", chat.sinLeer(u.getId()));
        });
    }
}

