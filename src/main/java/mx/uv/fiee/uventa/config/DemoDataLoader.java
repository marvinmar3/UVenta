package mx.uv.fiee.uventa.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.uv.fiee.uventa.model.*;
import mx.uv.fiee.uventa.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga publicaciones de ejemplo para ver el diseño de la página principal.
 * Solo corre con el perfil "demo" y únicamente si todavía no hay publicaciones.
 * Los vendedores de ejemplo están inactivos: nadie puede iniciar sesión con ellos.
 */
@Component
@Profile("demo")
@Slf4j
@RequiredArgsConstructor
public class DemoDataLoader implements ApplicationRunner {

    private final UsuarioRepository usuarios;
    private final CarreraRepository carreras;
    private final CategoriaRepository categorias;
    private final PublicacionRepository publicaciones;
    private final PasswordEncoder encoder;

    @PersistenceContext
    private EntityManager em;

    private int contador = 0;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (publicaciones.count() > 0) {
            log.info("[DEMO] Ya hay publicaciones; no se cargan datos de ejemplo.");
            return;
        }
        List<Carrera> cs = carreras.findByActivaTrueOrderByNombreAsc();
        if (cs.isEmpty()) {
            log.warn("[DEMO] No hay carreras en la base; se omiten los datos de ejemplo.");
            return;
        }
        Usuario ana = vendedor("Ana", "López Cruz", "demo.ana", "S00000001", cs.get(0));
        Usuario diego = vendedor("Diego", "Ramírez Soto", "demo.diego", "S00000002", cs.get(cs.size() > 1 ? 1 : 0));
        Usuario sofia = vendedor("Sofía", "García Mora", "demo.sofia", "S00000003", cs.get(cs.size() > 2 ? 2 : 0));
        Usuario ivan = vendedor("Iván", "Pérez Luna", "demo.ivan", "S00000004", cs.get(0));

        final String LIB = "Libros y material académico", ELE = "Electrónica y tecnología",
                ROP = "Ropa y accesorios", ALI = "Alimentos", SER = "Servicios estudiantiles",
                HOG = "Hogar y artículos generales";

        item(ana, LIB, TipoOferta.VENTA, "Circuitos eléctricos, Dorf (10.ª ed.)",
                "Buen estado. Algunos capítulos subrayados a lápiz.", 220, Condicion.USADO, "Circuitos eléctricos", 3, 1, true);
        item(diego, ELE, TipoOferta.DONACION, "Kit de resistencias y protoboard",
                "Resistencias de varios valores y una protoboard de 830 puntos. Lo regalo a quien lo necesite.", 0, Condicion.COMO_NUEVO, "Electrónica analógica", 2, 2, false);
        item(sofia, ELE, TipoOferta.VENTA, "Arduino UNO con cables y sensores",
                "Incluye cable USB, jumpers y 3 sensores. Funciona perfecto.", 350, Condicion.USADO, "Microcontroladores", 5, 3, true);
        item(ana, LIB, TipoOferta.VENTA, "Calculadora científica Casio fx-991",
                "Casi sin uso. Incluye tapa.", 180, Condicion.COMO_NUEVO, "Cálculo diferencial", 1, 4, false);
        item(ivan, ROP, TipoOferta.INTERCAMBIO, "Bata de laboratorio, talla M",
                "La cambio por una talla L.", 0, Condicion.USADO, "Laboratorio de física", 2, 5, false);
        item(diego, ALI, TipoOferta.VENTA, "Pastel de zanahoria por rebanada",
                "Hecho el mismo día. Entrega en cafetería entre clases.", 25, Condicion.NUEVO, null, null, 0, true);
        item(sofia, LIB, TipoOferta.VENTA, "Apuntes de Estructura de datos",
                "Apuntes completos del semestre, a computadora.", 40, Condicion.USADO, "Estructura de datos", 3, 6, false);
        item(ivan, SER, TipoOferta.VENTA, "Asesoría de programación en Python",
                "Sesiones de 1 hora para principiantes.", 80, Condicion.NUEVO, "Programación", 1, 2, false);
        item(ana, ALI, TipoOferta.VENTA, "Fresas con crema (vaso grande)",
                "Preparadas al momento.", 35, Condicion.NUEVO, null, null, 1, false);
        item(diego, HOG, TipoOferta.VENTA, "Lámpara de escritorio LED",
                "Luz cálida y fría, con brazo flexible.", 90, Condicion.COMO_NUEVO, null, null, 7, false);
        item(sofia, ELE, TipoOferta.VENTA, "Multímetro digital",
                "Mide voltaje, corriente y resistencia. Con puntas nuevas.", 150, Condicion.USADO, "Instrumentación", 4, 8, false);
        item(ivan, ROP, TipoOferta.VENTA, "Mochila para laptop de 15 pulgadas",
                "Con compartimento acolchado y puerto USB.", 200, Condicion.USADO, null, null, 9, false);
        item(ana, LIB, TipoOferta.DONACION, "Sistemas digitales, Tocci",
                "Lo dono a quien lo vaya a usar este semestre.", 0, Condicion.USADO, "Sistemas digitales", 4, 10, false);
        item(diego, SER, TipoOferta.VENTA, "Tutorías de Cálculo integral",
                "Resolución de ejercicios y preparación de exámenes.", 100, Condicion.NUEVO, "Cálculo integral", 2, 3, false);
        item(sofia, ELE, TipoOferta.INTERCAMBIO, "Teclado mecánico compacto",
                "Lo cambio por un mouse inalámbrico.", 0, Condicion.USADO, null, null, 4, false);
        item(ivan, ALI, TipoOferta.VENTA, "Brownies caseros (caja de 6)",
                "Con nuez. Pedidos con un día de anticipación.", 60, Condicion.NUEVO, null, null, 0, false);

        log.info("[DEMO] Se cargaron {} publicaciones de ejemplo.", contador);
    }

    private Usuario vendedor(String nombre, String apellidos, String alias, String matricula, Carrera carrera) {
        String correo = alias + "@estudiantes.uv.mx";
        return usuarios.findByCorreoInstitucionalIgnoreCase(correo).orElseGet(() -> {
            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setApellidos(apellidos);
            u.setCorreoInstitucional(correo);
            u.setCorreoVerificado(true);
            u.setPasswordHash(encoder.encode(UUID.randomUUID().toString()));
            u.setTipoUsuario(TipoUsuario.ESTUDIANTE);
            u.setMatricula(matricula);
            u.setCarrera(carrera);
            u.setActivo(false); // cuenta de ejemplo: no puede iniciar sesión
            return usuarios.save(u);
        });
    }

    private void item(Usuario vendedor, String categoria, TipoOferta tipo, String titulo, String descripcion,
                      int precio, Condicion condicion, String materia, Integer semestre, int diasAtras,
                      boolean destacada) {
        Categoria cat = categorias.findAll().stream()
                .filter(c -> c.getNombre().equals(categoria)).findFirst().orElse(null);
        if (cat == null) {
            log.warn("[DEMO] No existe la categoría '{}'; se omite '{}'.", categoria, titulo);
            return;
        }
        Publicacion p = new Publicacion();
        p.setUsuario(vendedor);
        p.setCategoria(cat);
        p.setTipo(tipo);
        p.setTitulo(titulo);
        p.setDescripcion(descripcion);
        p.setPrecio(BigDecimal.valueOf(precio));
        p.setCondicion(condicion);
        p.setMateria(materia);
        p.setSemestre(semestre == null ? null : semestre.shortValue());
        p.setPuntoEntrega(tipo == TipoOferta.VENTA && categoria.equals("Alimentos")
                ? "Cafetería" : "Biblioteca");
        Instant fecha = Instant.now().minus(Duration.ofDays(diasAtras)).minus(Duration.ofMinutes(7L * contador));
        p.setFechaPublicacion(fecha);
        p.setFechaActualizacion(fecha);
        publicaciones.save(p);
        contador++;

        if (destacada) {
            em.createNativeQuery("INSERT INTO destacado (id_publicacion, id_plan, fecha_inicio, fecha_fin, estado) "
                    + "SELECT CAST(?1 AS bigint), id_plan, now(), now() + interval '7 days', 'ACTIVO' "
                    + "FROM plan_destacado ORDER BY id_plan LIMIT 1")
                .setParameter(1, p.getId())
                .executeUpdate();
        }
    }
}
