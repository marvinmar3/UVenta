package mx.uv.fiee.uventa.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import mx.uv.fiee.uventa.model.TipoUsuario;
import mx.uv.fiee.uventa.validation.CorreoInstitucional;

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

    @NotNull(message = "Elige una opción.")
    private TipoUsuario tipoUsuario = TipoUsuario.ESTUDIANTE;

    @Size(max = 30)
    private String matricula;

    @Min(value = 1950, message = "Año no válido.")
    @Max(value = 2100, message = "Año no válido.")
    private Integer anioEgreso;

    @NotNull(message = "Elige tu carrera.")
    private Long idCarrera;
}