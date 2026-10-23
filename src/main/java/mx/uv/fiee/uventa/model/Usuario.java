package mx.uv.fiee.uventa.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "usuario")
@Getter @Setter
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String apellidos;

    @Column(name = "correo_institucional", nullable = false)
    private String correoInstitucional;

    @Column(name = "correo_verificado", nullable = false)
    private boolean correoVerificado;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false, length = 15)
    private TipoUsuario tipoUsuario;

    @Column(length = 30)
    private String matricula;

    @Column(name = "anio_egreso")
    private Integer anioEgreso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_carrera")
    private Carrera carrera;

    @Column(length = 25)
    private String telefono;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Rol rol = Rol.USUARIO;

    @Column(nullable = false)
    private boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private Instant fechaRegistro;
}
