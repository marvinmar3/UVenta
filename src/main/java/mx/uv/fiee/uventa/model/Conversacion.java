package mx.uv.fiee.uventa.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/** Una conversación entre quien publica y un interesado, ligada a una publicación. */
@Entity
@Table(name = "conversacion")
@Getter @Setter
public class Conversacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conversacion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_publicacion")
    private Publicacion publicacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_interesado")
    private Usuario interesado;

    @Column(name = "fecha_inicio", nullable = false, updatable = false)
    private Instant fechaInicio = Instant.now();
}