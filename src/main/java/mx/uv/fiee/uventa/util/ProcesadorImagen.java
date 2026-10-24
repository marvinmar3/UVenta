package mx.uv.fiee.uventa.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

/*valida que un archivo sea realmente una imagen jpg/png y lo vuelve a guardar como jpeg
    reduce el tamaño, corrige la rotacion de las fotos del celular y elimina los metadatos
    efix, que pueden incluir la ubicacion gps de quien tomó la foto */
public final class ProcesadorImagen {
    public static final int MAX_LADO = 1280;
    private static final long MAX_PIXELES = 40_000_000L;
    private static final float CALIDAD = 0.85f;

    private ProcesadorImagen() {}

    public static byte[] aJpeg(byte[] original) throws ImagenInvalidaException, IOException {
        if (!esJpeg(original) && !esPng(original)) {
            throw new ImagenInvalidaException("Solo se aceptan fotos JPG o PNG.");
        }
        validarDimensiones(original);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(original));
        if (img == null) throw new ImagenInvalidaException("No pudimos leer esa foto. Prueba con otra.");

        BufferedImage rgb = reducirSobreBlanco(img);
        int orientacion = esJpeg(original) ? orientacionExif(original) : 1;
        BufferedImage final_ = orientar(rgb, orientacion);
        return codificar(final_);
    }

    static boolean esJpeg(byte[] b) {
        return b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
    }

    static boolean esPng(byte[] b) {
        return b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
    }

    /** Evita "bombas de descompresión": imágenes diminutas en bytes pero enormes en píxeles. */
    private static void validarDimensiones(byte[] b) throws ImagenInvalidaException, IOException {
        try (ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(b))) {
            var lectores = ImageIO.getImageReaders(in);
            if (!lectores.hasNext()) throw new ImagenInvalidaException("No pudimos leer esa foto. Prueba con otra.");
            ImageReader lector = lectores.next();
            try {
                lector.setInput(in);
                long px = (long) lector.getWidth(0) * lector.getHeight(0);
                if (px > MAX_PIXELES) throw new ImagenInvalidaException("Esa foto es demasiado grande en resolución.");
            } finally {
                lector.dispose();
            }
        }
    }

    private static BufferedImage reducirSobreBlanco(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        double escala = Math.min(1.0, (double) MAX_LADO / Math.max(w, h));
        int nw = Math.max(1, (int) Math.round(w * escala));
        int nh = Math.max(1, (int) Math.round(h * escala));
        BufferedImage dst = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, nw, nh);
            g.drawImage(src, 0, 0, nw, nh, null);
        } finally {
            g.dispose();
        }
        return dst;
    }

    /** Lee la etiqueta de orientación (0x0112) del bloque EXIF de un JPEG; 1 si no hay. */
    static int orientacionExif(byte[] b) {
        int i = 2;
        while (i + 4 < b.length) {
            if ((b[i] & 0xFF) != 0xFF) return 1;
            int marcador = b[i + 1] & 0xFF;
            if (marcador == 0xDA || marcador == 0xD9) return 1;
            int len = u16(b, i + 2, false);
            boolean exif = marcador == 0xE1 && i + 10 < b.length
                    && b[i + 4] == 'E' && b[i + 5] == 'x' && b[i + 6] == 'i' && b[i + 7] == 'f'
                    && b[i + 8] == 0 && b[i + 9] == 0;
            if (exif) {
                int t = i + 10;
                if (t + 8 > b.length) return 1;
                boolean little = b[t] == 'I' && b[t + 1] == 'I';
                int ifd = t + u32(b, t + 4, little);
                int n = u16(b, ifd, little);
                for (int k = 0; k < n; k++) {
                    int e = ifd + 2 + 12 * k;
                    if (e + 12 > b.length) return 1;
                    if (u16(b, e, little) == 0x0112) {
                        int v = u16(b, e + 8, little);
                        return (v >= 1 && v <= 8) ? v : 1;
                    }
                }
                return 1;
            }
            i += 2 + len;
        }
        return 1;
    }

    private static int u16(byte[] b, int i, boolean little) {
        if (i < 0 || i + 2 > b.length) return 0;
        int a = b[i] & 0xFF, c = b[i + 1] & 0xFF;
        return little ? (c << 8) | a : (a << 8) | c;
    }

    private static int u32(byte[] b, int i, boolean little) {
        if (i < 0 || i + 4 > b.length) return 0;
        long v = 0;
        for (int k = 0; k < 4; k++) {
            int x = b[i + (little ? 3 - k : k)] & 0xFF;
            v = (v << 8) | x;
        }
        return v > Integer.MAX_VALUE ? 0 : (int) v;
    }

    /** Aplica la orientación EXIF (1-8) para que la foto se vea derecha sin necesitar metadatos. */
    static BufferedImage orientar(BufferedImage src, int o) {
        if (o <= 1 || o > 8) return src;
        int w = src.getWidth(), h = src.getHeight();
        boolean gira = o >= 5;
        BufferedImage dst = new BufferedImage(gira ? h : w, gira ? w : h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int nx, ny;
                switch (o) {
                    case 2 -> { nx = w - 1 - x; ny = y; }
                    case 3 -> { nx = w - 1 - x; ny = h - 1 - y; }
                    case 4 -> { nx = x; ny = h - 1 - y; }
                    case 5 -> { nx = y; ny = x; }
                    case 6 -> { nx = h - 1 - y; ny = x; }
                    case 7 -> { nx = h - 1 - y; ny = w - 1 - x; }
                    default -> { nx = y; ny = w - 1 - x; }
                }
                dst.setRGB(nx, ny, src.getRGB(x, y));
            }
        }
        return dst;
    }

    private static byte[] codificar(BufferedImage img) throws IOException {
        ImageWriter escritor = ImageIO.getImageWritersByFormatName("jpeg").next();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             var ios = ImageIO.createImageOutputStream(out)) {
            ImageWriteParam p = escritor.getDefaultWriteParam();
            p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            p.setCompressionQuality(CALIDAD);
            escritor.setOutput(ios);
            escritor.write(null, new IIOImage(img, null, null), p);
            ios.flush();
            return out.toByteArray();
        } finally {
            escritor.dispose();
        }
    }

}
