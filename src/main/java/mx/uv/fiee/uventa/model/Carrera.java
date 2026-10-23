package mx.uv.fiee.uventa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table (name = "carrera")
@Getter @Setter
public class Carrera {
    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "id_carrera")
    private Long id;

    @Column (nullable = false, unique = true, length = 120)
    private String nombre;

    @Column (nullable = false)
    private boolean activa = true;
}
