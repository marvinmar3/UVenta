package mx.uv.fiee.uventa.repository;

import mx.uv.fiee.uventa.model.Carrera;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;



public interface CarreraRepository extends JpaRepository<Carrera, Long> {
    List<Carrera> findByActivaTrueOrderByNombreAsc();
}
