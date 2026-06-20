package entity.user.customer;

import entity.appointment.Appointment;
import entity.user.User;

import java.util.ArrayList;
import java.util.List;

public class Customer extends User {

    private final List<Appointment> appointmentHistory = new ArrayList<>();

    private Customer(CustomerBuilder builder){
        super(builder);
    }

    /**
     * Adds an appointment to the appointment history of the customer.
     * @param appointment the appointment to be added.
     */
    public void addAppointment(Appointment appointment){
        appointmentHistory.add(appointment);
    }

    /**
     * Returns the appointment history of the customer.
     * @return A list containing all appointments of the customer.
     */
    public List<Appointment> getAppointmentHistory(){
        return this.appointmentHistory;
    }

    //Static customer builder class
    public static class CustomerBuilder extends UserBuilder {

        /**
         * Constructs and returns a new Customer instance based on the configured properties.
         * * @return The newly constructed Customer instance
         */
        public Customer build(){
            return new Customer(this);
        }
    }

}
