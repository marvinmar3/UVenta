package mx.uv.fiee.uventa.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/** Acepta solo correos institucionales de la UV (ver {@link CorreoUv}). */
@Documented
@Constraint(validatedBy = CorreoInstitucionalValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CorreoInstitucional {
    String message() default "Usa tu correo institucional de la UV (por ejemplo zS12345678@estudiantes.uv.mx).";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
