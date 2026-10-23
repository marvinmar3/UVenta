package mx.uv.fiee.uventa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller 
public class CatalogoController {
    
    @GetMapping ("/")
    public String raiz(){
       return "redirect:/catalogo";
    }

    @GetMapping ("/catalogo")
     public String index(){
        return "catalogo/index"; //marcador / cat real es el sig modulo
    }
    
}
