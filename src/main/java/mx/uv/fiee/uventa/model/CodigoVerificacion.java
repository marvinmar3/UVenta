package mx.uv.fiee.uventa.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity 
@Table (name = "codigo_verificacion")
@Getter @Setter
public class CodigoVerificacion {
    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "id_codigo")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "id_usuario")
    private Usuario usuario;

    @Column (name="codigo_hash", nullable = false, updatable = false)
    private String codigoHash;

    @CreationTimestamp 
    @Column (name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column (name = "fecha_expiracion", nullable = false)
    private Instant fechaExpiracion;

    @Column(name="fecha_uso")
    private Instant fechaUso;

    @Column(nullable = false)
    private int intentos;

}
