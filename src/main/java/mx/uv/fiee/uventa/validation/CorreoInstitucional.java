package mx.uv.fiee.uventa.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

// acepta solo correos @egresados.uv.mx o @estudiantes.uv.mx
@Documented
@Constraint(validatedBy = CorreoInstitucionalValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CorreoInstitucional {
    String message() default "Usa tu correo institucional (@egresados.mx o @estudiantes.uv.mx)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
