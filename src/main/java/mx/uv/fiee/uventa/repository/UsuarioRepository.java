package mx.uv.fiee.uventa.repository;

import mx.uv.fiee.uventa.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreoInstitucionalIgnoreCase(String correo);
    boolean existsByCorreoInstitucionalIgnoreCase(String correo);
    boolean existsByMatricula(String matricula);
}
