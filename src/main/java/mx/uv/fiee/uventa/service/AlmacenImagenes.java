package mx.uv.fiee.uventa.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import mx.uv.fiee.uventa.util.ImagenInvalidaException;
import mx.uv.fiee.uventa.util.ProcesadorImagen;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/*guarda las fotos de las publicacioens en una carpeta del servidor y las sirve en /uploads */
@Service 
public class AlmacenImagenes {
    public static final Long MAX_BYTES = 5L * 1024 * 1024L; // 5 MB
    private static final String PREFIJO = "/uploads/";

    private final Path raiz;

    public AlmacenImagenes(@Value("${uventa.uploads.dir:uploads}") String carpeta)throws IOException {
        this.raiz = Path.of(carpeta).toAbsolutePath().normalize();
        Files.createDirectories(raiz);
    }

    /*Ubicacion de la carpeta como uri terminada en / para el manejador de recursos */
    public String getRaizUri() {
        String u = raiz.toUri().toString();
        return u.endsWith("/") ? u : u + "/";
    }

    /*valida, reduce y guarda la foto. devuelve la url publica */
    public String guardar(MultipartFile archivo){
        if (archivo.isEmpty()) throw new ImagenInvalidaException("Archivo vacío");
        if (archivo.getSize() > MAX_BYTES) {
            throw new PublicacionException("fotos", "Cada foto debe pesar máximo 5 MB.");
        }
        try {
            byte[] jpg = ProcesadorImagen.aJpeg(archivo.getBytes());
            String nombre = UUID.randomUUID() + ".jpg";
            Files.write(raiz.resolve(nombre), jpg);
            return PREFIJO + nombre;
        } catch (ImagenInvalidaException e) {
            throw new PublicacionException("fotos", e.getMessage());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Borra el archivo de una foto; ignora URLs que no sean de esta carpeta. */
    public void borrar(String url) {
        if (url == null || !url.startsWith(PREFIJO)) return;
        String nombre = Path.of(url.substring(PREFIJO.length())).getFileName().toString();
        try {
            Files.deleteIfExists(raiz.resolve(nombre));
        } catch (IOException ignorado) {
            // Un archivo huérfano no debe tumbar la operación.
        }
    }
}
