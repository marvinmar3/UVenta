package mx.uv.fiee.uventa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import mx.uv.fiee.uventa.validation.CorreoInstitucional;

/**
 * El tipo de cuenta y la matrícula ya no se piden: se deducen del correo institucional.
 * Carrera y año de egreso son condicionales y los valida UsuarioService.
 */
@Getter @Setter
public class RegistroForm {
    @NotBlank(message = "Escribe tu nombre.")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "Escribe tus apellidos.")
    @Size(max = 150)
    private String apellidos;

    @NotBlank(message = "Escribe tu correo institucional.")
    @CorreoInstitucional
    private String correo;

    @NotBlank(message = "Crea una contraseña.")
    @Size(min = 8, max = 72, message = "La contraseña debe tener al menos 8 caracteres y no más de 72.")
    private String password;

    @NotBlank(message = "Repite tu contraseña.")
    private String confirmarPassword;

    private Integer anioEgreso;

    private Long idCarrera;
}
