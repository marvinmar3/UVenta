package mx.uv.fiee.uventa.repository;

import java.util.List;
import java.util.Optional;
import mx.uv.fiee.uventa.model.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversacionRepository extends JpaRepository<Conversacion, Long> {
    Optional<Conversacion> findByPublicacionIdAndInteresadoId(Long publicacionId, Long interesadoId);
    /*Conversaciones donde el usuario es el interesado o el dueño de la publicacion */
    @Query("SELECT c FROM Conversacion c "
            + "WHERE c.interesado.id = :usuarioId OR c.publicacion.usuario.id = :usuarioId")
    List<Conversacion> delUsuario(@Param("usuarioId") Long usuarioId);
}