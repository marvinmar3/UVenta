package mx.uv.fiee.uventa.validation;


import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interpreta un correo institucional de la UV: valida el dominio y, si es de estudiante,
 * obtiene la matrícula (la cuenta estudiantil es "z" + matrícula, p. ej. zS21012345@estudiantes.uv.mx).
 */
public final class CorreoUv {

    public enum Tipo { ESTUDIANTE, EGRESADO, PERSONAL }

    public record Info(Tipo tipo, String matricula) {}

    public static final String DOMINIO_ESTUDIANTE = "estudiantes.uv.mx";
    /** OJO: sin confirmar que la UV use este dominio para egresados. Verifícalo antes de depender de él. */
    public static final String DOMINIO_EGRESADO = "egresados.uv.mx";
    public static final String DOMINIO_PERSONAL = "uv.mx";

    private static final Pattern PARTE_LOCAL = Pattern.compile("^[a-z0-9._+-]+$");
    private static final Pattern CUENTA_ESTUDIANTE = Pattern.compile("^z([a-z]?\\d{6,9})$");

    private CorreoUv() {}

    public static Optional<Info> analizar(String correo) {
        if (correo == null) return Optional.empty();
        String c = correo.trim().toLowerCase(Locale.ROOT);
        int at = c.indexOf('@');
        if (at <= 0 || at != c.lastIndexOf('@')) return Optional.empty();

        String local = c.substring(0, at);
        String dominio = c.substring(at + 1);
        if (!PARTE_LOCAL.matcher(local).matches()) return Optional.empty();

        return switch (dominio) {
            case DOMINIO_ESTUDIANTE -> {
                Matcher m = CUENTA_ESTUDIANTE.matcher(local);
                yield m.matches()
                        ? Optional.of(new Info(Tipo.ESTUDIANTE, m.group(1).toUpperCase(Locale.ROOT)))
                        : Optional.empty();
            }
            case DOMINIO_EGRESADO -> Optional.of(new Info(Tipo.EGRESADO, null));
            case DOMINIO_PERSONAL -> Optional.of(new Info(Tipo.PERSONAL, null));
            default -> Optional.empty();
        };
    }
}
