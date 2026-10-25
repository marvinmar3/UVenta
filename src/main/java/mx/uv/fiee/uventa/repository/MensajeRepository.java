package mx.uv.fiee.uventa.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import mx.uv.fiee.uventa.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findTop200ByConversacionIdOrderByIdDesc(Long conversacionId);

    List<Mensaje> findTop100ByConversacionIdAndIdGreaterThanOrderByIdAsc(Long conversacionId, Long despuesDe);

    Optional<Mensaje> findFirstByConversacionIdOrderByIdDesc(Long conversacionId);

    long countByConversacionIdAndLeidoFalseAndEmisorIdNot(Long conversacionId, Long usuarioId);

    long countByEmisorIdAndFechaEnvioAfter(Long emisorId, Instant desde);

    /** Total de mensajes sin leer que le han escrito al usuario en todas sus conversaciones. */
    @Query("select count(m) from Mensaje m where m.leido = false and m.emisor.id <> :u "
         + "and (m.conversacion.interesado.id = :u or m.conversacion.publicacion.usuario.id = :u)")
    long sinLeer(@Param("u") Long usuarioId);

    /** Marca como leídos los mensajes que le llegaron al usuario en esa conversación. */
    @Modifying
    @Query("update Mensaje m set m.leido = true where m.conversacion.id = :c and m.emisor.id <> :u and m.leido = false")
    int marcarLeidos(@Param("c") Long conversacionId, @Param("u") Long usuarioId);

    /** Mayor id de un mensaje del usuario que la otra persona ya leyó (0 si ninguno). */
    @Query("select coalesce(max(m.id), 0) from Mensaje m where m.conversacion.id = :c and m.emisor.id = :u and m.leido = true")
    Long maxIdLeidoDe(@Param("c") Long conversacionId, @Param("u") Long usuarioId);
}