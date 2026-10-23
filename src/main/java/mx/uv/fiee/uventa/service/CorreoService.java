package mx.uv.fiee.uventa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorreoService {
    private final ObjectProvider<JavaMailSender> mail;

    @Value("${spring.mail.username:no-reply@uventa.local}")
    private String remitente;

    public void enviarCodigo(String para, String nombre, String codigo, long minutos){
        JavaMailSender sender = mail.getIfAvailable();
        if(sender == null) {
            //modo desarrollo : sin smtp configurado , el codigo se muestra en consola
            log.warn("[Desarrollo] smtp no configurado, el código de verificación para {} es: {}", para, codigo);
            return;
        }
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setFrom(remitente);
            m.setTo(para);
            m.setSubject("Tu código de verificación de UVenta");
            m.setText("Hola " + nombre + ",\n\nTu código es: " + codigo
                    + "\nVence en " + minutos + " minutos.\n\nSi no creaste una cuenta en UVenta, ignora este mensaje.");
            sender.send(m);
        } catch (MailException e) {
            log.error("No se pudo enviar el código a {}", para, e);
        }
    }
}
