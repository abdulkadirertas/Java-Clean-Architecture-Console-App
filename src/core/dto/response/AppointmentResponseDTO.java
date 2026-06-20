package core.dto.response;

import entity.appointment.AppointmentState;
import java.time.LocalDateTime;

public class AppointmentResponseDTO {
    private final String appointmentId;
    private final String customerId;
    private final String customerFullName;
    private final String barberId;
    private final String barberFullName;
    private final String barbersShopName;
    private final LocalDateTime timeSlot;
    private final AppointmentState status;

    public AppointmentResponseDTO(String appointmentId, String customerId,String customerFullName, String barberId,String barberFullName, String barbersShopName, LocalDateTime timeSlot, AppointmentState status){
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.customerFullName = customerFullName;
        this.barberId = barberId;
        this.barberFullName = barberFullName;
        this.barbersShopName = barbersShopName;
        this.timeSlot = timeSlot;
        this.status = status;
    }

    //Getter methods
    public String getAppointmentId() {
        return appointmentId;
    }
    public String getCustomerId() {
        return customerId;
    }
    public String getBarberId() {
        return barberId;
    }
    public LocalDateTime getTimeSlot() {
        return timeSlot;
    }
    public AppointmentState getStatus() {
        return status;
    }
    public String getBarberFullName() {
        return barberFullName;
    }
    public String getBarbersShopName() {
        return barbersShopName;
    }
    public String getCustomerFullName() {
        return customerFullName;
    }
}
