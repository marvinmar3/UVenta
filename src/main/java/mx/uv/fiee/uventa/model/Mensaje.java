package mx.uv.fiee.uventa.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "mensaje")
public class Mensaje {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "id_conversacion")
    private Conversacion conversacion;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "id_emisor")
    private Usuario emisor;

    @Column(nullable = false, length = 1000)
    private String contenido;

    @Column(name = "fecha_envio", nullable = false)
    private Instant fechaEnvio = Instant.now();

    @Column(nullable = false)
    private boolean leido;
}
