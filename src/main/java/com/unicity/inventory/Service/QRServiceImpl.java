package com.unicity.inventory.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QRServiceImpl {

    /**
     * Genera una imagen de código QR a partir de un texto.
     *
     * @param text El texto a codificar en el QR (será una URL).
     * @param width El ancho de la imagen en píxeles.
     * @param height La altura de la imagen en píxeles.
     * @return Un array de bytes que representa la imagen PNG del QR.
     * @throws WriterException Si ocurre un error al codificar el QR.
     * @throws IOException Si ocurre un error al escribir la imagen en memoria.
     */
    public byte[] generateQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        // Escribe la matriz de bits en un flujo de bytes en memoria (formato PNG)
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

        // Devuelve el array de bytes de la imagen generada
        return pngOutputStream.toByteArray();
    }
}
