package com.unicity.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    // Inyectamos el email de "username" desde application.properties
    // para usarlo como remitente por defecto si es necesario.
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            // ✅ ¡LA SOLUCIÓN! Aquí establecemos el remitente que el destinatario verá.
            // Usaremos el del grupo, pero podrías cambiarlo si quisieras.
            message.setFrom("solicitudes_col@unicity.com");

            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            System.out.println("Correo enviado exitosamente a: " + to + " desde: " + message.getFrom());

        } catch (Exception e) {
            System.err.println("Error al enviar correo a " + to + ": " + e.getMessage());
        }
    }
}

