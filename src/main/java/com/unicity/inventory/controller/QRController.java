package com.unicity.inventory.controller;

import com.unicity.inventory.Service.QRServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QRController {

    private final QRServiceImpl qrService;

    // Inyecta la URL del frontend desde el archivo de propiedades
    @Value("${app.frontend.url}")
    private String frontendUrl;

    @GetMapping(value = "/{activoId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQRCode(@PathVariable("activoId") Long activoId) {
        try {
            // 1. Construye la URL completa que apuntará al detalle del activo
            String urlParaQR = frontendUrl + "/activos/" + activoId;

            // 2. Llama al servicio para generar la imagen del QR
            byte[] qrImage = qrService.generateQRCodeImage(urlParaQR, 250, 250);

            // 3. Devuelve la imagen como respuesta
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrImage);

        } catch (Exception e) {
            // En caso de error, puedes devolver una respuesta de error o un QR de "error"
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
