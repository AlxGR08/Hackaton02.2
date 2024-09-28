package dbp.hackathon.Ticket;

import dbp.hackathon.Estudiante.Estudiante;
import dbp.hackathon.Estudiante.EstudianteRepository;
import dbp.hackathon.Funcion.Funcion;
import dbp.hackathon.Funcion.FuncionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private FuncionRepository funcionRepository;

    @Autowired
    private QRService qrService;  // Inyectamos el servicio de QR

    @Autowired
    private MailService mailService;  // Inyectamos el servicio de correo

    public Ticket createTicket(Long estudianteId, Long funcionId, Integer cantidad) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId).orElse(null);
        Funcion funcion = funcionRepository.findById(funcionId).orElse(null);
        if (estudiante == null || funcion == null) {
            throw new IllegalStateException("Estudiante or Funcion not found!");
        }

        Ticket ticket = new Ticket();
        ticket.setEstudiante(estudiante);
        ticket.setFuncion(funcion);
        ticket.setCantidad(cantidad);
        ticket.setEstado(Estado.VENDIDO);
        ticket.setFechaCompra(LocalDateTime.now());

        // Generar el código QR usando el servicio de QR
        String qrCodeUrl = qrService.generarQR(ticket.getId().toString());  // Generamos el QR basado en el ID del ticket
        ticket.setQr(qrCodeUrl);  // Guardamos el URL del QR en el ticket

        // Guardar el ticket en la base de datos
        Ticket savedTicket = ticketRepository.save(ticket);

        // Enviar correo con el ticket y el QR
        String emailBody = generarPlantillaCorreo(savedTicket, qrCodeUrl);
        mailService.enviarCorreo(estudiante.getEmail(), "Confirmación de compra", emailBody);

        return savedTicket;
    }

    // Método auxiliar para generar el cuerpo del correo
    private String generarPlantillaCorreo(Ticket ticket, String qrCodeUrl) {
        return "<h1>¡Gracias por tu compra!</h1>" +
                "<p>Nombre de la función: " + ticket.getFuncion().getNombre() + "</p>" +
                "<p>Fecha de la función: " + ticket.getFuncion().getFecha() + "</p>" +
                "<p>Cantidad de entradas: " + ticket.getCantidad() + "</p>" +
                "<p>Código QR: <img src='" + qrCodeUrl + "' /></p>" +
                "<p>¡No olvides llevar tu código QR impreso o en tu dispositivo móvil para poder ingresar a la función!</p>";
    }


    public Ticket findById(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        ticketRepository.deleteById(id);
    }

    public Iterable<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    public Iterable<Ticket> findByEstudianteId(Long estudianteId) {
        return ticketRepository.findByEstudianteId(estudianteId);
    }

    public void changeState(Long id) {
        Ticket ticket = ticketRepository.findById(id).orElse(null);
        if (ticket == null) {
            throw new IllegalStateException("Ticket no encontrado");
        }
        if (ticket.getEstado() == Estado.CANJEADO) {
            throw new IllegalStateException("El ticket ya ha sido canjeado");
        }

        // Cambiar el estado a CANJEADO
        ticket.setEstado(Estado.CANJEADO);
        ticketRepository.save(ticket);

        // Enviar correo de confirmación de canje
        String emailBody = "Tu ticket para la función " + ticket.getFuncion().getNombre() + " ha sido canjeado exitosamente.";
        mailService.enviarCorreo(ticket.getEstudiante().getEmail(), "Confirmación de canjeo", emailBody);
    }

}