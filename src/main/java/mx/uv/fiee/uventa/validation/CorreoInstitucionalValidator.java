package mx.uv.fiee.uventa.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CorreoInstitucionalValidator implements ConstraintValidator<CorreoInstitucional, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext ctx) {
        // Vacío lo revisa @NotBlank; aquí solo se valida el correo.
        return valor == null || valor.isBlank() || CorreoUv.analizar(valor).isPresent();
    }
}