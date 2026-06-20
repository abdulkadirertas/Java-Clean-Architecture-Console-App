package infrastructure.common;

import entity.appointment.Appointment;
import entity.user.barber.Barber;
import entity.user.customer.Customer;
import infrastructure.appointment.AppointmentRepository;
import infrastructure.user.BarberRepository;
import infrastructure.user.CustomerRepository;
import infrastructure.user.IUserRepository;

public class RepositoryFactory {
    private final static AppointmentRepository appointmentRepository;
    private final static BarberRepository barberRepository;
    private final static CustomerRepository customerRepository;

    //Load operation must be performed one times
    static {
        barberRepository = new BarberRepository("barbers.csv");
        customerRepository = new CustomerRepository("customers.csv");
        appointmentRepository = new AppointmentRepository("appointments.csv", barberRepository, customerRepository);

        barberRepository.load();
        customerRepository.load();
        //! appointments must be loaded last
        appointmentRepository.load();
    }

    //prevent creating an instance of the factory
    private RepositoryFactory(){}


    //Factory getter methods
    public static IUserRepository<Customer> getCustomerRepository(){
        return customerRepository;
    }

    public static IUserRepository<Barber> getBarberRepository(){
        return barberRepository;
    }

    public static IRepository<Appointment> getAppointmentRepository(){
        return appointmentRepository;
    }
}
