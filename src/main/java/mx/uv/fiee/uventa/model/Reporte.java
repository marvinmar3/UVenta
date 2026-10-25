package mx.uv.fiee.uventa.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/** Aviso de una persona sobre una publicación (fraude, contenido indebido, etc.). */
@Entity
@Table(name = "reporte")
@Getter @Setter
public class Reporte {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reportante")
    private Usuario reportante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_publicacion")
    private Publicacion publicacion;

    @Column(nullable = false, length = 100)
    private String motivo;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, length = 15)
    private String estado = "ABIERTO";

    @Column(nullable = false, updatable = false)
    private Instant fecha = Instant.now();
}
