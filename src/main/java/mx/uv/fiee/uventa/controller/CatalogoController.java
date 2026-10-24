package mx.uv.fiee.uventa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import mx.uv.fiee.uventa.dto.FiltroCatalogo;
import mx.uv.fiee.uventa.model.TipoOferta;
import mx.uv.fiee.uventa.service.CatalogoService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Controller 
@RequiredArgsConstructor 
public class CatalogoController {

    private final CatalogoService catalogo;

    /* pagina principal: busqueda, catalogo, destacados y listado con filtros */
    
    @GetMapping ("/")
    public String home(@ModelAttribute("f") FiltroCatalogo f, BindingResult ignorarErrores, Model model){
        boolean filtrado = f.hayFiltros();
        model.addAttribute("page",catalogo.buscar(f));
        model.addAttribute("categorias", catalogo.categorias());
        model.addAttribute("carreras", catalogo.carreras());
        model.addAttribute("tipos", TipoOferta.values());
        model.addAttribute("destacadas", filtrado ? List.of() : catalogo.destacadas(8));
        model.addAttribute("filtrado", filtrado);
        model.addAttribute("base", "/?" + consulta(f));
        return "home";
    }

    @GetMapping ("/catalogo")
     public String rutaAnterior(){
        return "redirect:/"; 
    }

    @GetMapping("/publicacion/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("p", catalogo.detalle(id));
        return "publicacion/detalle";
    }

    /** Parte fija de la URL para la paginación: "q=abc&categoria=2&" (vacía si no hay filtros). */
    private static String consulta(FiltroCatalogo f) {
        UriComponentsBuilder b = UriComponentsBuilder.newInstance();
        if (f.getQ() != null && !f.getQ().isBlank()) b.queryParam("q", f.getQ().trim());
        if (f.getCategoria() != null) b.queryParam("categoria", f.getCategoria());
        if (f.getTipo() != null) b.queryParam("tipo", f.getTipo().name());
        if (f.getCarrera() != null) b.queryParam("carrera", f.getCarrera());
        if (f.getSemestre() != null) b.queryParam("semestre", f.getSemestre());
        if (f.getPrecioMax() != null) b.queryParam("precioMax", f.getPrecioMax().toPlainString());
        if (f.getOrden() != null && !"recientes".equals(f.getOrden())) b.queryParam("orden", f.getOrden());
        String q = b.build().encode().getQuery();
        return q == null ? "" : q + "&";
    }
    
}
