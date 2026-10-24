package mx.uv.fiee.uventa.config;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import mx.uv.fiee.uventa.service.AlmacenImagenes;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AlmacenImagenes almacen;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Los nombres de archivo son únicos (UUID), así que se pueden guardar en caché sin riesgo.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(almacen.getRaizUri())
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePrivate());
    }
}
