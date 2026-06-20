package entity.appointment;

import java.time.LocalDateTime;

public class Appointment {
    //Appointment fields
    private final String appointmentID;
    private String customerId;
    private String barberId;
    private LocalDateTime timeSlot;
    private AppointmentState status;

    private Appointment(AppointmentBuilder builder){
        this.appointmentID = builder.appointmentID;
        this.customerId = builder.customerId;
        this.barberId = builder.barberId;
        this.timeSlot = builder.timeSlot;
        this.status = builder.status;
    }

    //GETTER METHODS
    public String getAppointmentID() {
        return appointmentID;
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

    public void updateAppointmentStatus(AppointmentState status){
        this.status = status;
    }

    //Static appointment builder class
    public static class AppointmentBuilder{
        protected String appointmentID;
        protected String customerId;
        protected String barberId;
        protected LocalDateTime timeSlot;
        protected AppointmentState status;

        /**
         * Constructs and returns a new Appointment instance based on the configured properties.
         * * @return The newly constructed Appointment instance
         */
        public Appointment build(){
            return new Appointment(this);
        }
        //Builder Setter Methods
        public void setAppointmentID(String appointmentID) {
            this.appointmentID = appointmentID;
        }
        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }
        public void setBarberId(String barberId) {
            this.barberId = barberId;
        }
        public void setTimeSlot(LocalDateTime timeSlot) {
            this.timeSlot = timeSlot;
        }
        public void setStatus(AppointmentState status) {
            this.status = status;
        }
    }
}
