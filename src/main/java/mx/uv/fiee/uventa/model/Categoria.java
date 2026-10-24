package mx.uv.fiee.uventa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table (name = "categoria")
@Getter @Setter
public class Categoria {
    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "id_categoria")
    private Long id;

    @Column (nullable = false, unique = true, length = 100)
    private String nombre;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "id_categoria_padre")
    private Categoria categoriaPadre;

    private String descripcion;

    @Column(nullable = false)
    private boolean activa = true;

    /** Clase de Bootstrap Icons, p. ej. "bi-book" (columna agregada en V5). */
    @Column(nullable = false, length = 40)
    private String icono = "bi-grid";
}
