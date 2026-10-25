package mx.uv.fiee.uventa.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mx.uv.fiee.uventa.dto.ConversacionResumen;
import mx.uv.fiee.uventa.dto.ConversacionVista;
import mx.uv.fiee.uventa.dto.MensajeVista;
import mx.uv.fiee.uventa.model.Conversacion;
import mx.uv.fiee.uventa.model.EstadoPublicacion;
import mx.uv.fiee.uventa.model.Mensaje;
import mx.uv.fiee.uventa.model.Publicacion;
import mx.uv.fiee.uventa.model.Reporte;
import mx.uv.fiee.uventa.model.Usuario;
import mx.uv.fiee.uventa.repository.ConversacionRepository;
import mx.uv.fiee.uventa.repository.MensajeRepository;
import mx.uv.fiee.uventa.repository.PublicacionRepository;
import mx.uv.fiee.uventa.repository.ReporteRepository;
import mx.uv.fiee.uventa.repository.UsuarioRepository;
import mx.uv.fiee.uventa.util.Formato;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ChatService {

    public static final int MAX_CARACTERES = 1000;
    private static final int MAX_POR_MINUTO = 15;
    private static final int MAX_BANDEJA = 100;

    /** Mensajes nuevos y el id más alto de los míos que la otra persona ya leyó. */
    public record Nuevos(List<MensajeVista> mensajes, long leidoHasta) {}

    private final ConversacionRepository conversaciones;
    private final MensajeRepository mensajes;
    private final PublicacionRepository publicaciones;
    private final UsuarioRepository usuarios;
    private final ReporteRepository reportes;

    // --------------------------------------------------------------- iniciar

    /** Abre (o recupera) la conversación del usuario con el dueño de una publicación. */
    @Transactional
    public Long iniciar(Long publicacionId, String correo) {
        Usuario u = usuario(correo);
        Publicacion p = publicaciones.findById(publicacionId)
                .filter(x -> x.getEstado() == EstadoPublicacion.ACTIVA || x.getEstado() == EstadoPublicacion.RESERVADA)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (p.getUsuario().getId().equals(u.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes escribirte a ti mismo.");
        }
        return conversaciones.findByPublicacionIdAndInteresadoId(p.getId(), u.getId())
                .orElseGet(() -> {
                    Conversacion c = new Conversacion();
                    c.setPublicacion(p);
                    c.setInteresado(u);
                    return conversaciones.save(c);
                }).getId();
    }

    // --------------------------------------------------------------- bandeja

    @Transactional(readOnly = true)
    public List<ConversacionResumen> bandeja(String correo) {
        Usuario u = usuario(correo);
        List<ConversacionResumen> lista = new ArrayList<>();
        for (Conversacion c : conversaciones.delUsuario(u.getId()).stream().limit(MAX_BANDEJA).toList()) {
            Publicacion p = c.getPublicacion();
            boolean soyVendedor = p.getUsuario().getId().equals(u.getId());
            Usuario otro = soyVendedor ? c.getInteresado() : p.getUsuario();
            Mensaje ultimo = mensajes.findFirstByConversacionIdOrderByIdDesc(c.getId()).orElse(null);
            String texto = ultimo == null ? "Escribe el primer mensaje"
                    : (ultimo.getEmisor().getId().equals(u.getId()) ? "Tú: " : "") + recortar(ultimo.getContenido(), 70);
            lista.add(ConversacionResumen.builder()
                    .id(c.getId())
                    .otroNombre(Formato.nombreCorto(otro.getNombre(), otro.getApellidos()))
                    .otroIniciales(Formato.iniciales(otro.getNombre(), otro.getApellidos()))
                    .titulo(p.getTitulo())
                    .categoriaIcono(p.getCategoria().getIcono())
                    .tono((int) (p.getCategoria().getId() % 6))
                    .ultimoTexto(texto)
                    .fecha(ultimo != null ? ultimo.getFechaEnvio() : c.getFechaInicio())
                    .soyVendedor(soyVendedor)
                    .sinLeer(mensajes.countByConversacionIdAndLeidoFalseAndEmisorIdNot(c.getId(), u.getId()))
                    .build());
        }
        lista.sort(Comparator.comparing(ConversacionResumen::getFecha).reversed());
        return lista;
    }

    @Transactional(readOnly = true)
    public long sinLeer(Long usuarioId) {
        return mensajes.sinLeer(usuarioId);
    }

    // ----------------------------------------------------------- conversación

    /** Abre la conversación y marca como leído lo que le habían escrito. */
    @Transactional
    public ConversacionVista abrir(Long id, String correo) {
        Usuario u = usuario(correo);
        Conversacion c = participante(id, u);
        mensajes.marcarLeidos(c.getId(), u.getId());

        List<Mensaje> ms = new ArrayList<>(mensajes.findTop200ByConversacionIdOrderByIdDesc(c.getId()));
        Collections.reverse(ms);
        List<MensajeVista> vistas = ms.stream().map(m -> vista(m, u.getId())).toList();

        Publicacion p = c.getPublicacion();
        boolean soyVendedor = p.getUsuario().getId().equals(u.getId());
        Usuario otro = soyVendedor ? c.getInteresado() : p.getUsuario();
        return ConversacionVista.builder()
                .id(c.getId())
                .publicacionId(p.getId())
                .publicacionTitulo(p.getTitulo())
                .publicacionPrecio(Formato.precioTexto(p.getTipo(), p.getPrecio()))
                .publicacionImagen(p.getImagenes().isEmpty() ? null : p.getImagenes().get(0).getUrl())
                .categoriaIcono(p.getCategoria().getIcono())
                .tono((int) (p.getCategoria().getId() % 6))
                .publicacionDisponible(p.getEstado() == EstadoPublicacion.ACTIVA
                        || p.getEstado() == EstadoPublicacion.RESERVADA || soyVendedor)
                .otroNombre(Formato.nombreCorto(otro.getNombre(), otro.getApellidos()))
                .otroIniciales(Formato.iniciales(otro.getNombre(), otro.getApellidos()))
                .soyVendedor(soyVendedor)
                .mensajes(vistas)
                .ultimoId(vistas.isEmpty() ? 0 : vistas.get(vistas.size() - 1).getId())
                .build();
    }

    /** Mensajes posteriores a "despues"; sirve para actualizar la pantalla sin recargarla. */
    @Transactional
    public Nuevos nuevos(Long id, String correo, long despues) {
        Usuario u = usuario(correo);
        Conversacion c = participante(id, u);
        List<Mensaje> ms = mensajes.findTop100ByConversacionIdAndIdGreaterThanOrderByIdAsc(c.getId(), despues);
        if (ms.stream().anyMatch(m -> !m.getEmisor().getId().equals(u.getId()) && !m.isLeido())) {
            mensajes.marcarLeidos(c.getId(), u.getId());
        }
        long leidoHasta = mensajes.maxIdLeidoDe(c.getId(), u.getId());
        return new Nuevos(ms.stream().map(m -> vista(m, u.getId())).toList(), leidoHasta);
    }

    @Transactional
    public MensajeVista enviar(Long id, String correo, String texto) {
        Usuario u = usuario(correo);
        Conversacion c = participante(id, u);

        String limpio = texto == null ? "" : texto.strip();
        if (limpio.isEmpty() || limpio.length() > MAX_CARACTERES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mensaje vacío o demasiado largo.");
        }
        long recientes = mensajes.countByEmisorIdAndFechaEnvioAfter(u.getId(), Instant.now().minus(1, ChronoUnit.MINUTES));
        if (recientes >= MAX_POR_MINUTO) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Vas muy rápido.");
        }

        Mensaje m = new Mensaje();
        m.setConversacion(c);
        m.setEmisor(u);
        m.setContenido(limpio);
        return vista(mensajes.save(m), u.getId());
    }

    // --------------------------------------------------------------- reportar

    public static final List<String> MOTIVOS_REPORTE = List.of(
            "Posible fraude o estafa",
            "Producto distinto al publicado",
            "Contenido inapropiado u ofensivo",
            "Acoso o mensajes molestos",
            "Otro motivo");

    /** Registra un reporte sobre la publicación de la conversación. Devuelve false si ya lo había hecho. */
    @Transactional
    public boolean reportar(Long id, String correo, String motivo, String detalle) {
        Usuario u = usuario(correo);
        Conversacion c = participante(id, u);
        if (!MOTIVOS_REPORTE.contains(motivo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Motivo no válido.");
        }
        Publicacion p = c.getPublicacion();
        if (reportes.existsByReportanteIdAndPublicacionIdAndEstado(u.getId(), p.getId(), "ABIERTO")) {
            return false;
        }
        Reporte r = new Reporte();
        r.setReportante(u);
        r.setPublicacion(p);
        r.setMotivo(motivo);
        String d = detalle == null ? "" : detalle.strip();
        r.setDescripcion(d.isEmpty() ? null : d.substring(0, Math.min(d.length(), 500)));
        reportes.save(r);
        return true;
    }

    // ----------------------------------------------------------------- ayudas

    /** Solo el interesado y el dueño de la publicación pueden ver la conversación. */
    private Conversacion participante(Long id, Usuario u) {
        Conversacion c = conversaciones.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean participa = c.getInteresado().getId().equals(u.getId())
                || c.getPublicacion().getUsuario().getId().equals(u.getId());
        if (!participa) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return c;
    }

    private MensajeVista vista(Mensaje m, Long usuarioId) {
        return MensajeVista.builder()
                .id(m.getId())
                .texto(m.getContenido())
                .propio(m.getEmisor().getId().equals(usuarioId))
                .hora(Formato.hora(m.getFechaEnvio()))
                .dia(Formato.dia(m.getFechaEnvio()))
                .leido(m.isLeido())
                .build();
    }

    private Usuario usuario(String correo) {
        return usuarios.findByCorreoInstitucionalIgnoreCase(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
    }

    private static String recortar(String s, int max) {
        String plano = s.replaceAll("\\s+", " ").strip();
        return plano.length() <= max ? plano : plano.substring(0, max - 1) + "…";
    }
}