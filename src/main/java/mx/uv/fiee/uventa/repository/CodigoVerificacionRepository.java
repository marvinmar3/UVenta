package mx.uv.fiee.uventa.repository;

import mx.uv.fiee.uventa.model.CodigoVerificacion;
import java.util.Optional;
import mx.uv.fiee.uventa.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodigoVerificacionRepository extends JpaRepository<CodigoVerificacion, Long> {
    Optional<CodigoVerificacion> findFirstByUsuarioAndFechaUsoIsNullOrderByFechaCreacionDesc(Usuario usuario);
    
}
