package core.appointment;

import core.dto.response.AppointmentResponseDTO;
import core.dto.response.BarberResponseDTO;
import core.dto.response.ScheduleResponseDTO;
import entity.appointment.Appointment;
import entity.appointment.AppointmentState;
import entity.user.barber.ApplicationState;
import entity.user.barber.Barber;
import entity.user.customer.Customer;
import infrastructure.common.IRepository;
import infrastructure.user.IUserRepository;
import util.IdGenerator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppointmentService {
    //Dependencies
    private final IUserRepository<Customer> customerRepository;
    private final IUserRepository<Barber> barberRepository;
    private final IRepository<Appointment> appointmentRepository;

    /**
     * Initializes the repository dependencies.
     * @param customerRepository The repository contract containing all customers in the system.
     * @param barberRepository The repository contract containing all barbers in the system.
     * @param appointmentRepository The repository contract containing all appointments in the system.
     */
    public AppointmentService(IUserRepository<Customer> customerRepository, IUserRepository<Barber> barberRepository, IRepository<Appointment> appointmentRepository){
        this.customerRepository = customerRepository;
        this.barberRepository = barberRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Creates an appointment, links it to the related customer,
     * and updates the time slot of the related barber to occupied.
     * @param customerId The customer ID related to the appointment.
     * @param barberId The Barber ID related to the appointment
     * @param timeSlot The time slot of the appointment
     * @throws IllegalArgumentException if the customer or barber cannot be found in the system.
     */
    public void bookAnAppointment(String customerId, String barberId, LocalDateTime timeSlot){
        Barber barber = barberRepository.get(barberId);
        Customer customer = customerRepository.get(customerId);

        if(barber == null || customer == null){
            throw new IllegalArgumentException("The barber or the customer does not exist in the system");
        }
        //if the barber is available at this time slot
        if(barber.isAvailable(timeSlot)){
            Appointment.AppointmentBuilder builder = new Appointment.AppointmentBuilder();
            //set appointment fields
            builder.setAppointmentID(IdGenerator.generateAppointmentId());
            builder.setCustomerId(customerId);
            builder.setBarberId(barberId);
            builder.setTimeSlot(timeSlot);
            builder.setStatus(AppointmentState.ACTIVE);
            //build the appointment
            Appointment appointment = builder.build();

            appointmentRepository.add(appointment.getAppointmentID(), appointment); //add to appointment repository
            customer.addAppointment(appointment); //add to customers appointment history
            barber.updateSchedule(timeSlot, false); //update barbers schedule (occupied)
            barber.addAppointment(appointment);

            appointmentRepository.save(); //Save changes
        }

    }

    /**
     * Updates the state of the appointment and syncs the barbers schedule's accordingly.
     * @param appointmentId The appointment ID whose status will be updated.
     * @param status New appointment status (e.g, ACTIVE, PAST, CANCELLED)
     * @throws IllegalArgumentException if the related appointment, customer or barber cannot be found in the system.
     */
    public void updateAppointmentStatus(String appointmentId, AppointmentState status){
        Appointment appointment = appointmentRepository.get(appointmentId);
        if(appointment == null)throw new IllegalArgumentException("Appointment not found.");

        Customer customer = customerRepository.get(appointment.getCustomerId());
        Barber barber = barberRepository.get(appointment.getBarberId());
        if(customer == null || barber == null)throw new IllegalArgumentException("Customer or Barber missing for the appointment.");

        appointment.updateAppointmentStatus(status);


        //If the appointment is updated as active, update barbers time slot also (false=occupied)
        //if the appointment is cancelled or past, free the time slot
        barber.updateSchedule(appointment.getTimeSlot(), status != AppointmentState.ACTIVE);
        appointmentRepository.save(); //save changes
    }

    /**
     * Returns the list of the appointment history of the customer.
     * @param customerId The id of the customer whose appointment history will be returned.
     * @return A list containing AppointmentResponseDTO objects.
     * @throws IllegalArgumentException if the customer cannot be found in the system.
     */
    public List<AppointmentResponseDTO> getCustomersAppointmentHistory(String customerId){
        List<AppointmentResponseDTO> appList = new ArrayList<>();
        Customer customer = customerRepository.get(customerId);
        if(customer == null)throw new IllegalArgumentException("Customer cannot be found in the system.");

        for(Appointment app: customer.getAppointmentHistory()){
            Barber barber = barberRepository.get(app.getBarberId());
            String barberFullName = (barber != null) ? barber.getFirstName() + " " + barber.getLastName() : "Unknown barber";
            String barbersShopName = (barber != null) ? barber.getShopName() : "Unknown shop.";
            String customerFullName = customer.getFirstName() + " " + customer.getLastName();

            AppointmentResponseDTO appDTO = new AppointmentResponseDTO(app.getAppointmentID(), app.getCustomerId(),customerFullName , app.getBarberId(),barberFullName, barbersShopName, app.getTimeSlot(), app.getStatus());
            appList.add(appDTO);
        }
        return appList;
    }

    /**
     * Returns the list of the appointment history of the barber.
     * @param barberId The id of the barber whose appointment history will be returned.
     * @return A list containing AppointmentResponseDTO objects.
     * @throws IllegalArgumentException if the barber cannot be found in the system.
     */
    public List<AppointmentResponseDTO> getBarbersAppointmentHistory(String barberId){
        List<AppointmentResponseDTO> appList = new ArrayList<>();
        Barber barber = barberRepository.get(barberId);
        if(barber == null)throw new IllegalArgumentException("Barber cannot be found in the system.");

        for(Appointment app: barber.getAppointmentsHistory()){
            Customer customer  = customerRepository.get(app.getCustomerId());
            String customerFullName = (customer!= null) ? customer.getFirstName() + " " + customer.getLastName() : "Unknown Customer";
            String barberFullName = barber.getFirstName() + " " + barber.getLastName();
            AppointmentResponseDTO appDTO = new AppointmentResponseDTO(app.getAppointmentID(), app.getCustomerId(),customerFullName , app.getBarberId(),barberFullName, barber.getShopName(), app.getTimeSlot(), app.getStatus());
            appList.add(appDTO);
        }
        return appList;
    }


    /**
     * Filters the active and approved barbers and returns the list of their DTO objects.
     * @return A list containing active and approved barbers DTO objects.
     */
    public List<BarberResponseDTO> getActiveBarbers(){
        List<BarberResponseDTO> activeBarbers = new ArrayList<>();
        for(Barber barber: barberRepository.getAll()){
            if(barber.isActive() && barber.getApprovalStatus() == ApplicationState.APPROVED){
                BarberResponseDTO barberDTO = new BarberResponseDTO(barber.getUserId(), barber.getFirstName(), barber.getLastName(), barber.getPhone(), barber.getShopName());
                activeBarbers.add(barberDTO);
            }
        }
        return activeBarbers;
    }

    /**
     * Returns the schedule DTO object of the related barber.
     * @param barberId The barber ID whose schedule will be returned
     * @return A DTO object containing a copy of the barbers schedule.
     * @throws IllegalArgumentException if the related barber instance or its schedule cannot be found in the system.
     */
    public ScheduleResponseDTO getBarbersSchedule(String barberId){
        Barber barber = barberRepository.get(barberId);
        if(barber == null)throw new IllegalArgumentException("Barber cannot be found while getting the schedule");

        Map<LocalDateTime, Boolean> schedule = barber.getSchedule();
        if(schedule == null) throw new IllegalArgumentException("Barber's schedule cannot be found.");

        return new ScheduleResponseDTO(schedule);
    }



}
