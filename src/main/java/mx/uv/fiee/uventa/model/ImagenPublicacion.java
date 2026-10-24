package mx.uv.fiee.uventa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "imagen_publicacion")
@Getter @Setter
public class ImagenPublicacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_publicacion")
    private Publicacion publicacion;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private Short orden = 1;
}
