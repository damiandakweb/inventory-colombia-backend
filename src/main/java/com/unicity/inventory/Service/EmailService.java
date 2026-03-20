package com.unicity.inventory.Service;

public interface EmailService {

    /**
     * Envía un correo electrónico de texto simple de forma asíncrona.
     * Su única responsabilidad es enviar un mensaje, no construirlo.
     *
     * @param to      La dirección de correo del destinatario.
     * @param subject El asunto del correo.
     * @param text    El cuerpo completo del correo.
     */
    void sendSimpleEmail(String to, String subject, String text);

}
