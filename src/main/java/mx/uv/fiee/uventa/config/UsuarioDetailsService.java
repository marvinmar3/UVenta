package mx.uv.fiee.uventa.config;

import mx.uv.fiee.uventa.model.Usuario;
import mx.uv.fiee.uventa.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    @Override
    public UserDetails loadUserByUsername(String correo) {
        Usuario u = usuarios.findByCorreoInstitucionalIgnoreCase(correo.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.withUsername(u.getCorreoInstitucional())
                .password(u.getPasswordHash())
                .roles(u.getRol().name())
                // Sin correo verificado (o cuenta inactiva) no se puede iniciar sesión.
                .disabled(!u.isCorreoVerificado() || !u.isActivo())
                .build();
    }
}
