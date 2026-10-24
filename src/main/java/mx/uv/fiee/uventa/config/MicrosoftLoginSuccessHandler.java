package mx.uv.fiee.uventa.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import mx.uv.fiee.uventa.model.Usuario;
import mx.uv.fiee.uventa.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MicrosoftLoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Set<String> DOMINIOS_PERMITIDOS = Set.of(
            "uv.mx",
            "estudiantes.uv.mx",
            "egresados.uv.mx");

    private final UsuarioRepository usuarios;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        OidcUser microsoftUser = (OidcUser) authentication.getPrincipal();
        String correo = obtenerCorreo(microsoftUser);

        if (!correoPermitido(correo)) {
            response.sendRedirect("/login?acceso-denegado");
            return;
        }

        Usuario usuario = usuarios.findByCorreoInstitucionalIgnoreCase(correo).orElse(null);
        if (usuario == null || !usuario.isActivo()) {
            response.sendRedirect("/login?usuario-no-registrado");
            return;
        }

        usuario.setCorreoVerificado(true);
        usuarios.save(usuario);
        response.sendRedirect("/catalogo");
    }

    private String obtenerCorreo(OidcUser microsoftUser) {
        String correo = microsoftUser.getEmail();
        if (correo == null || correo.isBlank()) {
            correo = microsoftUser.getPreferredUsername();
        }
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    private boolean correoPermitido(String correo) {
        int separador = correo.lastIndexOf('@');
        if (separador <= 0 || separador == correo.length() - 1) return false;
        return DOMINIOS_PERMITIDOS.contains(correo.substring(separador + 1));
    }
}