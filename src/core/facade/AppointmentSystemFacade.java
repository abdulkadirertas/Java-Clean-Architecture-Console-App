package core.facade;

import core.application.ApplicationService;
import core.appointment.AppointmentService;
import core.auth.AuthService;
import core.dto.request.BarberEnrollmentRequestDTO;
import core.dto.request.CustomerEnrollmentRequestDTO;
import core.dto.response.AppointmentResponseDTO;
import core.dto.response.AuthResponseDTO;
import core.dto.response.BarberResponseDTO;
import core.dto.response.ScheduleResponseDTO;
import core.security.IHashStrategy;
import core.security.Sha256HashStrategy;
import core.session.UserSession;
import entity.appointment.AppointmentState;
import entity.user.barber.ApplicationState;
import infrastructure.common.RepositoryFactory;

import java.time.LocalDateTime;
import java.util.List;

public class AppointmentSystemFacade {
    private final AuthService authService;
    private final AppointmentService appointmentService;
    private final ApplicationService applicationService;

    //Initialize services.
    public AppointmentSystemFacade(){
        IHashStrategy hashStrategy = new Sha256HashStrategy();
        this.authService = new AuthService(RepositoryFactory.getCustomerRepository(), RepositoryFactory.getBarberRepository(),RepositoryFactory.getAppointmentRepository() ,hashStrategy, UserSession.getInstance());
        this.appointmentService = new AppointmentService(RepositoryFactory.getCustomerRepository(), RepositoryFactory.getBarberRepository(), RepositoryFactory.getAppointmentRepository());
        this.applicationService = new ApplicationService(RepositoryFactory.getBarberRepository());
    }

    /**
     * Enrolls the new customer to the system.
     * Generates a unique ID and hashes the customer's passwords during enrollment.
     * @param customerRequest The customer request DTO object containing customer information.
     * @throws IllegalArgumentException if the customer has already registered and active
     */
    public void enrollCustomer(CustomerEnrollmentRequestDTO customerRequest){
        authService.enrollCustomer(customerRequest);
    }

    /**
     * Enrolls the new barber to the system.
     * Generates a unique ID and hashes the barber's passwords during enrollment.
     * @param barberRequest The barber request DTO object containing barber information.
     * @throws IllegalArgumentException if the barber has already registered and active
     */
    public void enrollBarber(BarberEnrollmentRequestDTO barberRequest){
        authService.enrollBarber(barberRequest);
    }

    /**
     * Tries to log in to the system by validating the phone number and the hashed password.
     * Opens a user session if the credentials are correct.
     * @param phone The phone number of the user requesting to log in.
     * @param password The raw password of the user requesting to log in.
     * @return A DTO object containing the information of the user.
     * @throws IllegalArgumentException if the user cannot be found in the system.
     */
    public AuthResponseDTO login(String phone, String password){
        return authService.login(phone, password);
    }

    /**
     * Returns current logged in user
     * @return Logged in user DTO object
     */
    public AuthResponseDTO getLoggedInUser() {
        return authService.getLoggedInUser();
    }

    /**
     * Returns the approval status of the barber.
     * @param barberId The ID of the barber whose status will be checked.
     * @return Application status of the barber (e.g, APPROVED, REJECTED, PENDING).
     */
    public ApplicationState getBarbersApplicationStatus(String barberId){
        return authService.getBarbersApplicationStatus(barberId);
    }

    /**
     * Returns the all active users in the system.
     * @return A list containing AuthResponseDTO objects.
     */
    public List<AuthResponseDTO> getAllActiveUsers(){
        return authService.getAllActiveUsers();
    }

    /**
     * Deactivates a user account (soft delete) and ends the session if the user removes their own account.
     * Admin can remove any account without terminating their own session.
     * Active appointments of the removed user are updated as CANCELLED.
     * @param id The user ID whose account will be deactivated.
     * @throws IllegalArgumentException if the user does not exist in the system or the user did not log in.
     */
    public void removeUser(String id){
        authService.removeUser(id);
    }

    /**
     * Ends the session.
     */
    public void logout(){
        authService.logout();
    }


    //Appointment service use cases

    /**
     * Creates an appointment, links it to the related customer,
     * and updates the time slot of the related barber to occupied.
     * @param customerId The customer ID related to the appointment.
     * @param barberId The Barber ID related to the appointment
     * @param timeSlot The time slot of the appointment
     * @throws IllegalArgumentException if the customer or barber cannot be found in the system.
     */
    public void bookAnAppointment(String customerId, String barberId, LocalDateTime timeSlot){
        appointmentService.bookAnAppointment(customerId, barberId, timeSlot);
    }

    /**
     * Returns the list of the appointment history of the customer.
     * @param customerId The id of the customer whose appointment history will be returned.
     * @return A list containing AppointmentResponseDTO objects.
     * @throws IllegalArgumentException if the customer cannot be found in the system.
     */
    public List<AppointmentResponseDTO> getCustomersAppointmentHistory(String customerId){
        return appointmentService.getCustomersAppointmentHistory(customerId);
    }

    /**
     * Returns the list of the appointment history of the barber.
     * @param barberId The id of the barber whose appointment history will be returned.
     * @return A list containing AppointmentResponseDTO objects.
     * @throws IllegalArgumentException if the barber cannot be found in the system.
     */
    public List<AppointmentResponseDTO> getBarbersAppointmentHistory(String barberId){
        return appointmentService.getBarbersAppointmentHistory(barberId);
    }

    /**
     * Updates the state of the appointment and syncs the barbers schedule's accordingly.
     * @param appointmentId The appointment ID whose status will be updated.
     * @param status New appointment status (e.g, ACTIVE, PAST, CANCELLED)
     * @throws IllegalArgumentException if the related appointment, customer or barber cannot be found in the system.
     */
    public void updateAppointmentStatus(String appointmentId, AppointmentState status){
        appointmentService.updateAppointmentStatus(appointmentId, status);
    }

    /**
     * Filters the active and approved barbers and returns the list of their DTO objects.
     * @return A list containing active and approved barbers DTO objects.
     */
    public List<BarberResponseDTO> getActiveBarbers(){
        return appointmentService.getActiveBarbers();
    }

    /**
     * Returns the schedule DTO object of the related barber.
     * @param barberId The barber ID whose schedule will be returned
     * @return A DTO object containing a copy of the barbers schedule.
     * @throws IllegalArgumentException if the related barber instance or its schedule cannot be found in the system.
     */
    public ScheduleResponseDTO getBarbersScheduleDTO(String barberId){
        return appointmentService.getBarbersSchedule(barberId);
    }

    //Application service use cases
    /**
     * Filters the pending applications and returns a list of their DTO object.
     * @return A list containing pending barber DTO objects.
     */
    public List<BarberResponseDTO> getPendingApplications(){
        return applicationService.getPendingApplications();
    }

    /**
     * Updates the approval status of the barber.
     * @param barberId The barber ID whose status will be updated.
     * @param status New application status (e.g, APPROVED, REJECTED, PENDING).
     * @throws IllegalArgumentException if the related barber cannot be found in the system.
     */
    public void updateApprovalStatus(String barberId, ApplicationState status){
        applicationService.updateApprovalStatus(barberId, status);
    }
}
