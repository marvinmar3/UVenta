package mx.uv.fiee.uventa.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mx.uv.fiee.uventa.model.CodigoVerificacion;
import mx.uv.fiee.uventa.model.Usuario;
import mx.uv.fiee.uventa.repository.CodigoVerificacionRepository;
import mx.uv.fiee.uventa.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class VerificacionService {
    public enum Resultado { OK, INCORRECTO, EXPIRADO, BLOQUEADO }

    private static final int MAX_INTENTOS = 5;
    private static final long SEGUNDOS_ENTRE_ENVIOS = 60;

    private final CodigoVerificacionRepository codigos;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final CorreoService correo;
    private final SecureRandom random = new SecureRandom();

    @Value("${uventa.verificacion.minutos:15}")
    private long minutos;

    @Transactional
    public void enviarNuevo(Usuario u) {
        String codigo = String.format("%06d", random.nextInt(1_000_000));
        CodigoVerificacion c = new CodigoVerificacion();
        c.setUsuario(u);
        c.setCodigoHash(encoder.encode(codigo));
        c.setFechaExpiracion(Instant.now().plus(minutos, ChronoUnit.MINUTES));
        codigos.save(c);
        correo.enviarCodigo(u.getCorreoInstitucional(), u.getNombre(), codigo, minutos);
    }

    /** Responde igual exista o no el correo, para no revelar qué cuentas existen. */
    @Transactional
    public void reenviar(String correoInstitucional) {
        usuarios.findByCorreoInstitucionalIgnoreCase(correoInstitucional.trim())
                .filter(u -> !u.isCorreoVerificado())
                .ifPresent(u -> {
                    boolean reciente = codigos
                            .findFirstByUsuarioAndFechaUsoIsNullOrderByFechaCreacionDesc(u)
                            .map(c -> c.getFechaCreacion().isAfter(Instant.now().minusSeconds(SEGUNDOS_ENTRE_ENVIOS)))
                            .orElse(false);
                    if (!reciente) enviarNuevo(u);
                });
    }

    @Transactional
    public Resultado verificar(String correoInstitucional, String codigo) {
        Optional<Usuario> ou = usuarios.findByCorreoInstitucionalIgnoreCase(correoInstitucional.trim());
        if (ou.isEmpty()) return Resultado.INCORRECTO;
        Usuario u = ou.get();
        if (u.isCorreoVerificado()) return Resultado.OK;

        Optional<CodigoVerificacion> oc =
                codigos.findFirstByUsuarioAndFechaUsoIsNullOrderByFechaCreacionDesc(u);
        if (oc.isEmpty()) return Resultado.EXPIRADO;
        CodigoVerificacion c = oc.get();

        if (c.getIntentos() >= MAX_INTENTOS) return Resultado.BLOQUEADO;
        if (c.getFechaExpiracion().isBefore(Instant.now())) return Resultado.EXPIRADO;

        c.setIntentos(c.getIntentos() + 1);
        if (!encoder.matches(codigo.trim(), c.getCodigoHash())) return Resultado.INCORRECTO;

        c.setFechaUso(Instant.now());
        u.setCorreoVerificado(true);
        return Resultado.OK;
    }
}
