package mx.uv.fiee.uventa.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Marcadores para las secciones que aún no existen, para que ningún enlace dé 404. */
@Controller
public class ProximamenteController {

    private static final Map<String, String[]> SECCIONES = Map.of(
        "/favoritos", new String[]{"Favoritos", "bi-heart",
            "Guarda lo que te interesa para encontrarlo después."},
        "/cuenta", new String[]{"Mi cuenta", "bi-person-circle",
            "Aquí verás y editarás tu perfil."});

    @GetMapping({"/favoritos", "/cuenta"})
    public String seccion(HttpServletRequest req, Model model) {
        String[] s = SECCIONES.get(req.getRequestURI());
        model.addAttribute("titulo", s[0]);
        model.addAttribute("icono", s[1]);
        model.addAttribute("texto", s[2]);
        return "proximamente";
    }
}
