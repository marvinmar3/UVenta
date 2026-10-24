package mx.uv.fiee.uventa.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "publicacion")
@Getter @Setter
public class Publicacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_publicacion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoOferta tipo;

    @Column(nullable = false, length = 180)
    private String titulo;

    @Column(nullable = false, length = 4000)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Condicion condicion;

    @Column(name = "punto_entrega")
    private String puntoEntrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoPublicacion estado = EstadoPublicacion.ACTIVA;

    @Column(name = "fecha_publicacion", nullable = false)
    private Instant fechaPublicacion = Instant.now();

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion = Instant.now();

    @Column(length = 120)
    private String materia;

    /** smallint en la base (V3): debe ser Short para que Hibernate valide el tipo. */
    private Short semestre;

    @OneToMany(mappedBy = "publicacion")
    @OrderBy("orden ASC")
    @BatchSize(size = 50)
    private List<ImagenPublicacion> imagenes = new ArrayList<>();
}
