package core.auth;

import core.dto.request.BarberEnrollmentRequestDTO;
import core.dto.request.CustomerEnrollmentRequestDTO;
import core.dto.response.AppointmentResponseDTO;
import core.dto.response.AuthResponseDTO;
import core.dto.response.BarberResponseDTO;
import core.security.IHashStrategy;
import core.session.UserSession;

import entity.appointment.Appointment;
import entity.appointment.AppointmentState;
import entity.user.UserRole;
import entity.user.barber.ApplicationState;
import entity.user.barber.Barber;
import entity.user.customer.Customer;

import infrastructure.common.IRepository;
import infrastructure.user.IUserRepository;
import util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

public class AuthService {
    //Dependencies
    private final IUserRepository<Customer> customerRepository;
    private final IUserRepository<Barber> barberRepository;
    private final IRepository<Appointment> appointmentRepository;
    private final IHashStrategy hashStrategy;
    private final UserSession session;

    /**
     * Initializes the repository dependencies.
     * @param customerRepository The repository contract containing all customers in the system.
     * @param barberRepository The repository contract containing all barbers in the system.
     * @param appointmentRepository The repository contract containing all appointments in the system.
     * @param hashStrategy The hashing strategy which the raw passwords will be hashed based on it (e.g, SHA-256, SHA-512).
     * @param session The session object
     */
    public AuthService(IUserRepository<Customer> customerRepository, IUserRepository<Barber> barberRepository,
                       IRepository<Appointment> appointmentRepository ,IHashStrategy hashStrategy, UserSession session){
        this.customerRepository = customerRepository;
        this.barberRepository = barberRepository;
        this.appointmentRepository = appointmentRepository;
        this.hashStrategy = hashStrategy;
        this.session = session;
    }

    /**
     * Enrolls the new customer to the system.
     * Generates a unique ID and hashes the customer's passwords during enrollment.
     * @param customerRequest The customer request DTO object containing customer information.
     * @throws IllegalArgumentException if the customer has already registered and active
     */
    public void enrollCustomer(CustomerEnrollmentRequestDTO customerRequest){
        Customer existingCustomer = customerRepository.findByPhone(customerRequest.getPhone());
        if(existingCustomer!= null && isPhoneAlreadyRegistered(existingCustomer.getPhone())){
            throw new IllegalArgumentException("The phone number is already exist in the system!");
        }

        //Generate a unique id for the new customer
        String newCustomerId = IdGenerator.generateCustomerId();
        //hash customers password
        String hashedPassword = hashStrategy.hashPassword(customerRequest.getRawPassword());

        //set customers fields
        Customer.CustomerBuilder customerBuilder = new Customer.CustomerBuilder();
        customerBuilder.setUserId(newCustomerId);
        customerBuilder.setFirstName(customerRequest.getFirstName());
        customerBuilder.setLastName(customerRequest.getLastName());
        customerBuilder.setPhone(customerRequest.getPhone());
        customerBuilder.setPassword(hashedPassword);

        //Build the customer
        Customer newCustomer = customerBuilder.build();
        //add to the repository
        customerRepository.add(newCustomer.getUserId(), newCustomer);
        //save changes in the repository
        customerRepository.save();
    }

    /**
     * Enrolls the new barber to the system.
     * Generates a unique ID and hashes the barber's passwords during enrollment.
     * @param barberRequest The barber request DTO object containing barber information.
     * @throws IllegalArgumentException if the barber has already registered and active
     */
    public void enrollBarber(BarberEnrollmentRequestDTO barberRequest){
        Barber existingBarber = barberRepository.findByPhone(barberRequest.getPhone());
        if(existingBarber != null && isPhoneAlreadyRegistered(existingBarber.getPhone())){
            throw new IllegalArgumentException("The phone number is already exist in the system!");
        }

        //Generate a unique id for the new barber
        String newBarberId = IdGenerator.generateBarberId();
        //hash barbers password
        String hashedPassword = hashStrategy.hashPassword(barberRequest.getRawPassword());

        //set barbers fields
        Barber.BarberBuilder barberBuilder = new Barber.BarberBuilder();
        barberBuilder.setUserId(newBarberId);
        barberBuilder.setFirstName(barberRequest.getFirstName());
        barberBuilder.setLastName(barberRequest.getLastName());
        barberBuilder.setPhone(barberRequest.getPhone());
        barberBuilder.setPassword(hashedPassword);
        barberBuilder.setShopName(barberRequest.getShopName());
        //builds the barber
        Barber newBarber = barberBuilder.build();
        //Add to the repository
        barberRepository.add(newBarber.getUserId(), newBarber);
        //save changes in the repository
        barberRepository.save();
    }

    /**
     * Checks whether the phone number is already used by an active user.
     * @param phone The phone number will be checked.
     * @return true if it is used by another user, otherwise false.
     */
    private boolean isPhoneAlreadyRegistered(String phone){
        for(Customer customer: customerRepository.getAll()){
            if(customer.getPhone().equals(phone) && customer.isActive())return true;
        }
        for(Barber barber: barberRepository.getAll()){
            if(barber.getPhone().equals(phone) && barber.isActive())return true;
        }
        return false;
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
        AuthResponseDTO responseDTO;

        //Admin panel control
        if(phone.equals("admin") && password.equals("123")){
            responseDTO = new AuthResponseDTO("ADMN-1", "admin", UserRole.ADMIN);
            session.startSession(responseDTO);
            return responseDTO;
        }

        //hash raw password
        String hashedPassword = hashStrategy.hashPassword(password);

        //First search in the customers
        Customer customer = customerRepository.findByPhone(phone);
        if(customer != null && customer.isActive()){
            if(customer.getPassword().equals(hashedPassword)){
                responseDTO = new AuthResponseDTO(customer.getUserId(), customer.getFirstName(), UserRole.CUSTOMER);
                session.startSession(responseDTO);
                return responseDTO;
            }
        }

        //The user is not a customer. Search in the barbers
        Barber barber = barberRepository.findByPhone(phone);
        if(barber != null && barber.isActive()){
            if(barber.getPassword().equals(hashedPassword)){
                responseDTO = new AuthResponseDTO(barber.getUserId(), barber.getFirstName(), UserRole.BARBER);
                session.startSession(responseDTO);
                return responseDTO;
            }
        }
        //User cannot be found.
        throw new IllegalArgumentException("User cannot be found. Incorrect phone or password.");
    }

    /**
     * Returns current logged in user
     * @return Logged in user DTO object
     */
    public AuthResponseDTO getLoggedInUser() {
        return session.getLoggedInUser();
    }

    /**
     * Returns the approval status of the barber.
     * @param barberId The ID of the barber whose status will be checked.
     * @return Application status of the barber (e.g, APPROVED, REJECTED, PENDING).
     */
    public ApplicationState getBarbersApplicationStatus(String barberId){
        Barber barber = barberRepository.get(barberId);
        return barber.getApprovalStatus();
    }


    /**
     * Deactivates a user account (soft delete) and ends the session if the user removes their own account.
     * Admin can remove any account without terminating their own session.
     * Active appointments of the removed user are updated as CANCELLED.
     * @param id The user ID whose account will be deactivated.
     * @throws IllegalArgumentException if the user does not exist in the system or the user did not log in.
     */
    public void removeUser(String id){
        //the user which performs remove operation (Customer, barber, admin)
        AuthResponseDTO user = session.getLoggedInUser();
        if(user == null)throw new IllegalArgumentException("No active session found for remove operation.");

        List<Appointment> appointmentListToBeUpdated = new ArrayList<>();
        //A customer or a barber removes his/her account
        //End session after removing
        if(session.isLoggedIn() && id.equals(user.getUserId())){
            if(user.getRole() == UserRole.CUSTOMER){
                Customer customer = customerRepository.get(id);
                appointmentListToBeUpdated = customer.getAppointmentHistory();

                customer.deactivateAccount();
                customerRepository.save();
                session.endSession();
            }else if(user.getRole() == UserRole.BARBER){
                Barber barber = barberRepository.get(user.getUserId());
                appointmentListToBeUpdated = barber.getAppointmentsHistory();

                barber.deactivateAccount();
                barberRepository.save();
                session.endSession();
            }
        }
        //Admin removes the user account
        //The session remains active
        else if(session.isLoggedIn() && user.getRole() == UserRole.ADMIN){
            Customer customer = customerRepository.get(id);
            Barber barber = barberRepository.get(id);
            if(customer != null){
                appointmentListToBeUpdated = customer.getAppointmentHistory();
                customer.deactivateAccount();
                customerRepository.save();
            }else if(barber != null){
                appointmentListToBeUpdated = barber.getAppointmentsHistory();
                barber.deactivateAccount();
                barberRepository.save();
            }else throw new IllegalArgumentException("User cannot be found in the system for the remove operation.");
        }
        if(!appointmentListToBeUpdated.isEmpty()) {
            for (Appointment app : appointmentListToBeUpdated) {
                if (app.getStatus() == AppointmentState.ACTIVE){
                    app.updateAppointmentStatus(AppointmentState.CANCELLED);
                    barberRepository.get(app.getBarberId()).updateSchedule(app.getTimeSlot(),true);
                }
            }
            appointmentRepository.save();
        }
    }


    /**
     * Fetches the all active users in the system and creates an active users list to return.
     * @return A list containing AuthResponseDTO objects.
     */
    public List<AuthResponseDTO> getAllActiveUsers(){
        List<AuthResponseDTO> allUsers = new ArrayList<>();
        for(Customer customer: customerRepository.getAll()){
            if(customer.isActive())allUsers.add(new AuthResponseDTO(customer.getUserId(), customer.getFirstName(), UserRole.CUSTOMER));
        }
        for(Barber barber: barberRepository.getAll()){
            if(barber.isActive())allUsers.add(new AuthResponseDTO(barber.getUserId(), barber.getFirstName(), UserRole.BARBER));
        }
        return allUsers;
    }

    /**
     * Ends the session.
     */
    public void logout(){
        if(session.isLoggedIn()){
            session.endSession();
        }
    }



}
