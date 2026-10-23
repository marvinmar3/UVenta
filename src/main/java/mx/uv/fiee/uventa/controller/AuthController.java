package mx.uv.fiee.uventa.controller;


import mx.uv.fiee.uventa.dto.RegistroForm;
import mx.uv.fiee.uventa.model.Usuario;
import mx.uv.fiee.uventa.repository.CarreraRepository;
import mx.uv.fiee.uventa.service.RegistroException;
import mx.uv.fiee.uventa.service.UsuarioService;
import mx.uv.fiee.uventa.service.VerificacionService;
import mx.uv.fiee.uventa.service.VerificacionService.Resultado;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor 
public class AuthController {
    private static final String SESION_CORREO = "correo_pendiente";

    private final UsuarioService usuarios;
    private final VerificacionService verificacion;
    private final CarreraRepository carreras;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping ("/registro")
    public String registroForm(Model model){
        model.addAttribute("form", new RegistroForm());
        model.addAttribute("carreras", carreras.findByActivaTrueOrderByNombreAsc());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("form") RegistroForm form, BindingResult br,
                            Model model, HttpSession session) {
        if (!br.hasErrors() && !form.getPassword().equals(form.getConfirmarPassword())) {
            br.rejectValue("confirmarPassword", "noCoincide", "Las contraseñas no coinciden.");
        }
        if (!br.hasErrors()) {
            try {
                Usuario u = usuarios.registrar(form);
                session.setAttribute(SESION_CORREO, u.getCorreoInstitucional());
                return "redirect:/verificar";
            } catch (RegistroException e) {
                br.rejectValue(e.getCampo(), "registro", e.getMessage());
            }
        }
        model.addAttribute("carreras", carreras.findByActivaTrueOrderByNombreAsc());
        return "auth/registro";
    }

    @GetMapping("/verificar")
    public String verificar(HttpSession session, Model model) {
        model.addAttribute("correo", session.getAttribute(SESION_CORREO));
        return "auth/verificar";
    }

    @PostMapping("/verificar")
    public String verificarCodigo(@RequestParam String correo, @RequestParam String codigo,
                                  HttpSession session, Model model) {
        Resultado r = verificacion.verificar(correo, codigo);
        if (r == Resultado.OK) {
            session.removeAttribute(SESION_CORREO);
            return "redirect:/login?verificado";
        }
        model.addAttribute("correo", correo);
        model.addAttribute("error", switch (r) {
            case INCORRECTO -> "El código no es correcto. Revísalo e intenta de nuevo.";
            case EXPIRADO -> "El código venció. Pide uno nuevo.";
            case BLOQUEADO -> "Demasiados intentos. Pide un código nuevo.";
            case OK -> "";
        });
        return "auth/verificar";
    }

    @PostMapping("/reenviar")
    public String reenviar(@RequestParam String correo, HttpSession session, RedirectAttributes ra) {
        verificacion.reenviar(correo);
        session.setAttribute(SESION_CORREO, correo.trim());
        ra.addFlashAttribute("info",
                "Si ese correo está pendiente de verificar, te enviamos un código nuevo.");
        return "redirect:/verificar";
    }

}
