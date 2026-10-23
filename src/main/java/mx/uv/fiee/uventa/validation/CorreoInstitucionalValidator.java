package mx.uv.fiee.uventa.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class CorreoInstitucionalValidator implements ConstraintValidator<CorreoInstitucional, String> {
    private static final Pattern UV = Pattern.compile("^[a-zA-Z0-9._%+-]+@(egresados\\.uv\\.mx|estudiantes\\.uv\\.mx)$");

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext ctx) {
        // Vacío lo revisa @NotBlank; aquí solo se valida el dominio.
        return valor == null || valor.isBlank() || UV.matcher(valor.trim()).matches();
    }
    
}
