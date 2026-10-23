package mx.uv.fiee.uventa.service;

import mx.uv.fiee.uventa.dto.RegistroForm;
import mx.uv.fiee.uventa.model.Carrera;
import mx.uv.fiee.uventa.model.TipoUsuario;
import mx.uv.fiee.uventa.model.Usuario;
import mx.uv.fiee.uventa.repository.CarreraRepository;
import mx.uv.fiee.uventa.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final CarreraRepository carreras;
    private final PasswordEncoder encoder;
    private final VerificacionService verificacion;

    @Transactional
    public Usuario registrar(RegistroForm f) {
        String correo = f.getCorreo().trim().toLowerCase();
        if (usuarios.existsByCorreoInstitucionalIgnoreCase(correo)) {
            throw new RegistroException("correo",
                    "Ese correo ya tiene una cuenta. Si aún no la verificas, usa “Verificar correo”.");
        }
        Carrera carrera = carreras.findById(f.getIdCarrera())
                .filter(Carrera::isActiva)
                .orElseThrow(() -> new RegistroException("idCarrera", "Elige una carrera de la lista."));

        Usuario u = new Usuario();
        u.setNombre(f.getNombre().trim());
        u.setApellidos(f.getApellidos().trim());
        u.setCorreoInstitucional(correo);
        u.setPasswordHash(encoder.encode(f.getPassword()));
        u.setTipoUsuario(f.getTipoUsuario());
        u.setCarrera(carrera);

        if (f.getTipoUsuario() == TipoUsuario.ESTUDIANTE) {
            String mat = f.getMatricula() == null ? "" : f.getMatricula().trim().toUpperCase();
            if (mat.isEmpty()) throw new RegistroException("matricula", "Escribe tu matrícula.");
            if (usuarios.existsByMatricula(mat)) {
                throw new RegistroException("matricula", "Esa matrícula ya está registrada.");
            }
            u.setMatricula(mat);
        } else {
            if (f.getAnioEgreso() == null) {
                throw new RegistroException("anioEgreso", "Indica tu año de egreso.");
            }
            u.setAnioEgreso(f.getAnioEgreso());
        }

        usuarios.save(u);
        verificacion.enviarNuevo(u);
        return u;
    }
}
