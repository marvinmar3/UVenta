package mx.uv.fiee.uventa.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import mx.uv.fiee.uventa.dto.PublicacionForm;
import mx.uv.fiee.uventa.dto.TarjetaPublicacion;
import mx.uv.fiee.uventa.model.Condicion;
import mx.uv.fiee.uventa.model.EstadoPublicacion;
import mx.uv.fiee.uventa.model.TipoOferta;
import mx.uv.fiee.uventa.service.CatalogoService;
import mx.uv.fiee.uventa.service.PublicacionException;
import mx.uv.fiee.uventa.service.PublicacionService;
import mx.uv.fiee.uventa.util.PuntosEntrega;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PublicacionController {

    private final PublicacionService servicio;
    private final CatalogoService catalogo;

    // ---------------------------------------------------------------- publicar

    @GetMapping("/publicar")
    public String nueva(Model model) {
        model.addAttribute("form", new PublicacionForm());
        preparar(model, "crear", "/publicar");
        return "publicacion/formulario";
    }

    @PostMapping("/publicar")
    public String crear(@Valid @ModelAttribute("form") PublicacionForm form, BindingResult br,
                        @RequestParam(name = "fotos", required = false) List<MultipartFile> fotos,
                        Principal principal, Model model, RedirectAttributes ra) {
        if (!br.hasErrors()) {
            try {
                Long id = servicio.crear(principal.getName(), form, fotos);
                ra.addFlashAttribute("exito", "¡Listo! Tu publicación ya está en línea.");
                return "redirect:/publicacion/" + id;
            } catch (PublicacionException e) {
                rechazar(br, model, e);
            }
        }
        preparar(model, "crear", "/publicar");
        model.addAttribute("fotosPerdidas", hayArchivos(fotos));
        return "publicacion/formulario";
    }

    // ------------------------------------------------------------------ editar

    @GetMapping("/publicacion/{id}/editar")
    public String editar(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("form", servicio.formularioDe(id, principal.getName()));
        model.addAttribute("imagenes", servicio.imagenesDe(id, principal.getName()));
        preparar(model, "editar", "/publicacion/" + id + "/editar");
        return "publicacion/formulario";
    }

    @PostMapping("/publicacion/{id}/editar")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("form") PublicacionForm form, BindingResult br,
                             @RequestParam(name = "fotos", required = false) List<MultipartFile> fotos,
                             @RequestParam(name = "quitar", required = false) List<Long> quitar,
                             Principal principal, Model model, RedirectAttributes ra) {
        if (!br.hasErrors()) {
            try {
                servicio.actualizar(id, principal.getName(), form, fotos, quitar);
                ra.addFlashAttribute("exito", "Guardamos los cambios de tu publicación.");
                return "redirect:/publicacion/" + id;
            } catch (PublicacionException e) {
                rechazar(br, model, e);
            }
        }
        model.addAttribute("imagenes", servicio.imagenesDe(id, principal.getName()));
        preparar(model, "editar", "/publicacion/" + id + "/editar");
        model.addAttribute("fotosPerdidas", hayArchivos(fotos));
        return "publicacion/formulario";
    }

    // ------------------------------------------------------ mis publicaciones

    @GetMapping("/mis-publicaciones")
    public String mias(@RequestParam(name = "estado", required = false) String estado,
                       Principal principal, Model model) {
        List<TarjetaPublicacion> todas = servicio.misPublicaciones(principal.getName());
        Map<EstadoPublicacion, Long> conteo = todas.stream()
                .collect(Collectors.groupingBy(TarjetaPublicacion::getEstado, Collectors.counting()));

        EstadoPublicacion filtro = filtroDe(estado);
        List<TarjetaPublicacion> filas = filtro == null ? todas
                : todas.stream().filter(t -> t.getEstado() == filtro).toList();

        model.addAttribute("filas", filas);
        model.addAttribute("estadoSel", filtro == null ? "todas" : estado);
        model.addAttribute("total", todas.size());
        model.addAttribute("nActivas", conteo.getOrDefault(EstadoPublicacion.ACTIVA, 0L)
                + conteo.getOrDefault(EstadoPublicacion.RESERVADA, 0L));
        model.addAttribute("nPausadas", conteo.getOrDefault(EstadoPublicacion.PAUSADA, 0L));
        model.addAttribute("nFinalizadas", conteo.getOrDefault(EstadoPublicacion.FINALIZADA, 0L));
        return "publicacion/mis-publicaciones";
    }

    @PostMapping("/publicacion/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam String accion,
                                Principal principal, RedirectAttributes ra) {
        ra.addFlashAttribute("exito", servicio.cambiarEstado(id, principal.getName(), accion));
        return "redirect:/mis-publicaciones";
    }

    // ------------------------------------------------------------------ ayudas

    private void preparar(Model model, String modo, String accion) {
        model.addAttribute("modo", modo);
        model.addAttribute("accion", accion);
        model.addAttribute("categorias", catalogo.categorias());
        model.addAttribute("tipos", TipoOferta.values());
        model.addAttribute("condiciones", Condicion.values());
        model.addAttribute("puntos", PuntosEntrega.LISTA);
        model.addAttribute("maxFotos", PublicacionService.MAX_FOTOS);
    }

    private static void rechazar(BindingResult br, Model model, PublicacionException e) {
        if ("fotos".equals(e.getCampo())) {
            model.addAttribute("errorFotos", e.getMessage());
        } else {
            br.rejectValue(e.getCampo(), "publicacion", e.getMessage());
        }
    }

    private static boolean hayArchivos(List<MultipartFile> fotos) {
        return fotos != null && fotos.stream().anyMatch(f -> f != null && !f.isEmpty());
    }

    private static EstadoPublicacion filtroDe(String estado) {
        if (estado == null) return null;
        return switch (estado) {
            case "activa" -> EstadoPublicacion.ACTIVA;
            case "pausada" -> EstadoPublicacion.PAUSADA;
            case "finalizada" -> EstadoPublicacion.FINALIZADA;
            default -> null;
        };
    }
}
