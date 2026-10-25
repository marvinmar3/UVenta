package mx.uv.fiee.uventa.controller;

import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mx.uv.fiee.uventa.dto.ConversacionResumen;
import mx.uv.fiee.uventa.dto.MensajeVista;
import mx.uv.fiee.uventa.service.ChatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private static final List<String> SUGERENCIAS = List.of(
            "¿Aún está disponible?",
            "¿Podría darme más detalles?",
            "¿Acepta cambio por otro artículo?",
            "¿Podría darme un mejor precio?",
            "¿Podría enviarme más fotos?"
    );

    private final ChatService chat;

    /** Botón "Contactar al vendedor" de una publicación. */
    @PostMapping("/publicacion/{id}/contactar")
    public String contactar(@PathVariable Long id, Principal principal) {
        return "redirect:/chat/" + chat.iniciar(id, principal.getName());
    }

    @GetMapping("/chat")
    public String bandeja(@RequestParam(name = "rol", required = false) String rol,
                          Principal principal, Model model) {
        cargarBandeja(model, principal, rol, null);
        return "chat/bandeja";
    }

    @GetMapping("/chat/{id}")
    public String conversacion(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("v", chat.abrir(id, principal.getName()));
        model.addAttribute("sugerencias", SUGERENCIAS);
        model.addAttribute("maxCaracteres", ChatService.MAX_CARACTERES);
        model.addAttribute("motivos", ChatService.MOTIVOS_REPORTE);
        cargarBandeja(model, principal, null, id);
        return "chat/conversacion";
    }

    /** Sondeo: devuelve solo los mensajes nuevos, ya armados como HTML. */
    @GetMapping("/chat/{id}/mensajes")
    public String nuevos(@PathVariable Long id,
                         @RequestParam(name = "despues", defaultValue = "0") long despues,
                         @RequestParam(name = "dia", defaultValue = "") String dia,
                         Principal principal, Model model, HttpServletResponse respuesta) {
        ChatService.Nuevos r = chat.nuevos(id, principal.getName(), despues);
        respuesta.setHeader("X-Leidos", String.valueOf(r.leidoHasta()));
        respuesta.setHeader("Cache-Control", "no-store");
        model.addAttribute("mensajes", r.mensajes());
        model.addAttribute("diaPrevio", dia);
        return "chat/fragmentos :: burbujas";
    }

    /** Envío desde JavaScript: responde con la burbuja del mensaje enviado. */
    @PostMapping(path = "/chat/{id}/mensajes", headers = "X-Requested-With=fetch")
    public String enviarConJs(@PathVariable Long id, @RequestParam String texto,
                              @RequestParam(name = "dia", defaultValue = "") String dia,
                              Principal principal, Model model, HttpServletResponse respuesta) {
        MensajeVista m = chat.enviar(id, principal.getName(), texto);
        respuesta.setHeader("Cache-Control", "no-store");
        model.addAttribute("mensajes", List.of(m));
        model.addAttribute("diaPrevio", dia);
        return "chat/fragmentos :: burbujas";
    }

    /** Envío normal (si JavaScript está desactivado). */
    @PostMapping("/chat/{id}/mensajes")
    public String enviarSinJs(@PathVariable Long id, @RequestParam String texto,
                              Principal principal, RedirectAttributes ra) {
        try {
            chat.enviar(id, principal.getName(), texto);
        } catch (ResponseStatusException e) {
            ra.addFlashAttribute("errorChat", "No se pudo enviar. Escribe un mensaje de hasta "
                    + ChatService.MAX_CARACTERES + " caracteres.");
        }
        return "redirect:/chat/" + id;
    }

    @PostMapping("/chat/{id}/reportar")
    public String reportar(@PathVariable Long id,
                           @RequestParam String motivo,
                           @RequestParam(name = "detalle", defaultValue = "") String detalle,
                           Principal principal, RedirectAttributes ra) {
        boolean nuevo = chat.reportar(id, principal.getName(), motivo, detalle);
        ra.addFlashAttribute("avisoChat", nuevo
                ? "Gracias. Recibimos tu reporte y lo revisaremos."
                : "Ya habías reportado esta publicación. Seguimos revisándola.");
        return "redirect:/chat/" + id;
    }

    private void cargarBandeja(Model model, Principal principal, String rol, Long activa) {
        List<ConversacionResumen> todas = chat.bandeja(principal.getName());
        List<ConversacionResumen> lista = switch (rol == null ? "" : rol) {
            case "compras" -> todas.stream().filter(c -> !c.isSoyVendedor()).toList();
            case "ventas" -> todas.stream().filter(ConversacionResumen::isSoyVendedor).toList();
            default -> todas;
        };
        model.addAttribute("bandeja", lista);
        model.addAttribute("rolSel", rol == null ? "todas" : rol);
        model.addAttribute("activa", activa);
    }
}
