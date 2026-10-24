package mx.uv.fiee.uventa.repository;

import java.util.List;
import mx.uv.fiee.uventa.model.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PublicacionRepository
        extends JpaRepository<Publicacion, Long>, JpaSpecificationExecutor<Publicacion> {

    /** Ids de publicaciones con un destacado vigente (pagado y sin vencer). */
    @Query(value = "SELECT DISTINCT d.id_publicacion FROM destacado d "
            + "WHERE d.estado = 'ACTIVO' AND d.fecha_fin > now()", nativeQuery = true)
    List<Long> idsDestacadasVigentes();
}
