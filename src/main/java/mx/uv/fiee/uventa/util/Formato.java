package mx.uv.fiee.uventa.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mx.uv.fiee.uventa.model.TipoOferta;

/** Textos que se muestran en las tarjetas y en el detalle. */
public final class Formato {

    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("d 'de' MMM", Locale.forLanguageTag("es-MX"));

    private Formato() {}

    public static String dinero(BigDecimal v) {
        if (v == null) return "$0";
        boolean entero = v.stripTrailingZeros().scale() <= 0;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(entero ? 0 : 2);
        nf.setMaximumFractionDigits(entero ? 0 : 2);
        return "$" + nf.format(v);
    }

    public static String precioTexto(TipoOferta tipo, BigDecimal precio) {
        return switch (tipo) {
            case VENTA -> dinero(precio);
            case DONACION -> "Gratis";
            case INTERCAMBIO -> "Intercambio";
        };
    }

    /** "Cálculo I · Sem. 1", o null si no hay materia ni semestre. */
    public static String meta(String materia, Short semestre) {
        List<String> partes = new ArrayList<>();
        if (materia != null && !materia.isBlank()) partes.add(materia.trim());
        if (semestre != null) partes.add("Sem. " + semestre);
        return partes.isEmpty() ? null : String.join(" · ", partes);
    }

    public static String hace(Instant t) {
        Duration d = Duration.between(t, Instant.now());
        long min = d.toMinutes();
        if (min < 1) return "ahora";
        if (min < 60) return "hace " + min + " min";
        long h = d.toHours();
        if (h < 24) return "hace " + h + " h";
        long dias = d.toDays();
        if (dias == 1) return "ayer";
        if (dias < 7) return "hace " + dias + " días";
        return t.atZone(ZoneId.systemDefault()).format(FECHA);
    }

    /** Muestra el primer nombre y la inicial del primer apellido: "Ana L." */
    public static String nombreCorto(String nombre, String apellidos) {
        String primero = nombre == null ? "" : nombre.trim().split("\\s+")[0];
        String inicial = (apellidos == null || apellidos.isBlank()) ? "" : " " + apellidos.trim().charAt(0) + ".";
        return primero + inicial;
    }

    public static String iniciales(String nombre, String apellidos) {
        String a = (nombre == null || nombre.isBlank()) ? "" : nombre.trim().substring(0, 1);
        String b = (apellidos == null || apellidos.isBlank()) ? "" : apellidos.trim().substring(0, 1);
        return (a + b).toUpperCase(Locale.ROOT);
    }
}
