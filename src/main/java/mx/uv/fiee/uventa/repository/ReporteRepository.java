package mx.uv.fiee.uventa.repository;

import mx.uv.fiee.uventa.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    /** Evita que la misma persona reporte varias veces lo mismo mientras siga abierto. */
    boolean existsByReportanteIdAndPublicacionIdAndEstado(Long reportanteId, Long publicacionId, String estado);
}