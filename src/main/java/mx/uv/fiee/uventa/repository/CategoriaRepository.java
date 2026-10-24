package mx.uv.fiee.uventa.repository;

import java.util.List;
import mx.uv.fiee.uventa.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByActivaTrueAndCategoriaPadreIsNullOrderByNombre();
}