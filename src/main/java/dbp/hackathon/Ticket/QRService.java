package dbp.hackathon.Ticket;

import org.springframework.stereotype.Service;

@Service
public class QRService {

    // Genera el enlace del QR usando la API de goQR.me
    public String generarQR(String data) {
        // La URL de la API de goQR.me para generar el QR
        return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + data;
    }
}
