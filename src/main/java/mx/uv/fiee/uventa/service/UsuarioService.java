package mx.uv.fiee.uventa.service;

import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import mx.uv.fiee.uventa.dto.RegistroForm;
import mx.uv.fiee.uventa.model.Carrera;
import mx.uv.fiee.uventa.model.TipoUsuario;
import mx.uv.fiee.uventa.model.Usuario;
import mx.uv.fiee.uventa.repository.CarreraRepository;
import mx.uv.fiee.uventa.repository.UsuarioRepository;
import mx.uv.fiee.uventa.validation.CorreoUv;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    /** Primer año que se ofrece en la lista de año de egreso. */
    public static final int ANIO_MIN_EGRESO = 1990;

    private final UsuarioRepository usuarios;
    private final CarreraRepository carreras;
    private final PasswordEncoder encoder;
    private final VerificacionService verificacion;

    /** Del año actual hacia atrás; se calcula en cada consulta, así nunca queda desactualizado. */
    public List<Integer> aniosEgreso() {
        return IntStream.rangeClosed(ANIO_MIN_EGRESO, Year.now().getValue())
                .boxed().sorted(Comparator.reverseOrder()).toList();
    }

    @Transactional
    public Usuario registrar(RegistroForm f) {
        String correo = f.getCorreo().trim().toLowerCase();
        CorreoUv.Info info = CorreoUv.analizar(correo)
                .orElseThrow(() -> new RegistroException("correo", "Usa tu correo institucional de la UV."));

        if (usuarios.existsByCorreoInstitucionalIgnoreCase(correo)) {
            throw new RegistroException("correo",
                    "Ese correo ya tiene una cuenta. Si aún no la verificas, usa “Verificar correo”.");
        }

        Usuario u = new Usuario();
        u.setNombre(f.getNombre().trim());
        u.setApellidos(f.getApellidos().trim());
        u.setCorreoInstitucional(correo);
        u.setPasswordHash(encoder.encode(f.getPassword()));

        switch (info.tipo()) {
            case ESTUDIANTE -> {
                if (usuarios.existsByMatricula(info.matricula())) {
                    throw new RegistroException("correo", "Esa matrícula ya tiene una cuenta.");
                }
                u.setTipoUsuario(TipoUsuario.ESTUDIANTE);
                u.setMatricula(info.matricula());
                u.setCarrera(carreraObligatoria(f.getIdCarrera()));
            }
            case EGRESADO -> {
                Integer anio = f.getAnioEgreso();
                if (anio == null || anio < ANIO_MIN_EGRESO || anio > Year.now().getValue()) {
                    throw new RegistroException("anioEgreso", "Elige tu año de egreso.");
                }
                u.setTipoUsuario(TipoUsuario.EGRESADO);
                u.setAnioEgreso(anio);
                u.setCarrera(carreraObligatoria(f.getIdCarrera()));
            }
            case PERSONAL -> u.setTipoUsuario(TipoUsuario.PERSONAL);
        }

        usuarios.save(u);
        verificacion.enviarNuevo(u);
        return u;
    }

    private Carrera carreraObligatoria(Long id) {
        if (id == null) throw new RegistroException("idCarrera", "Elige tu carrera.");
        return carreras.findById(id)
                .filter(Carrera::isActiva)
                .orElseThrow(() -> new RegistroException("idCarrera", "Elige una carrera de la lista."));
    }
}
