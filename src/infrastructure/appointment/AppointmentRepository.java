package infrastructure.appointment;

import entity.appointment.Appointment;
import entity.appointment.AppointmentState;
import entity.user.barber.Barber;
import entity.user.customer.Customer;
import infrastructure.common.IRepository;
import infrastructure.user.BarberRepository;
import infrastructure.user.CustomerRepository;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentRepository implements IRepository<Appointment> {
    private final String appointmentsFilePath;
    private final Map<String, Appointment> appointmentMap = new HashMap<>();

    //Dependencies
    private final BarberRepository barberRepository;
    private final CustomerRepository customerRepository;

    public AppointmentRepository(String appointmentsFilePath, BarberRepository barberRepository, CustomerRepository customerRepository){
        this.appointmentsFilePath = appointmentsFilePath;
        this.barberRepository = barberRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Adds a new appointment to the repository
     * @param id The unique identifier of the appointment
     * @param appointment The appointment instance to be added
     */
    public void add(String id, Appointment appointment){
        appointmentMap.put(id, appointment);
    }

    /**
     * Returns the appointment corresponding to given id.
     * @param id The unique identifier of the appointment to be retrieved.
     * @return The appointment associated with the specified ID, or null if not found
     */
    public Appointment get(String id){
        return appointmentMap.get(id);
    }

    /**
     * Fetches all entities from the repository and returns them as a list.
     * @return A list containing all appointments
     */
    public List<Appointment> getAll(){
        return new ArrayList<>(appointmentMap.values());
    }

    /**
     * Loads the appointment data from  the persistent storage and initializes the repository state.
     * Checks the timeslots of the appointments and updates their status if the appointment is past.
     * Adds the appointments to the appointment history of the corresponding customers.
     * Updates the time slots of the corresponding barbers if the appointment status is active.
     */
    public void load(){
        try(BufferedReader br = new BufferedReader(new FileReader(this.appointmentsFilePath))) {

            String[] temp;

            String line = br.readLine(); //skip first line  (titles)
            while ((line = br.readLine()) != null) {
                Appointment.AppointmentBuilder appointmentBuilder = new Appointment.AppointmentBuilder(); //create an appointment builder

                //content of temp array: appointmentId;customerId;barberId;timeSlot;status
                temp = line.split(";");

                //Set appointment fields using builder
                appointmentBuilder.setAppointmentID(temp[0]);
                appointmentBuilder.setCustomerId(temp[1]);
                appointmentBuilder.setBarberId(temp[2]);

                LocalDateTime timeSlot = LocalDateTime.parse(temp[3]);
                AppointmentState status = AppointmentState.valueOf(temp[4]);

                //Update appointment status if its time is past
                if(timeSlot.isBefore(LocalDateTime.now()) && status == AppointmentState.ACTIVE){
                    status = AppointmentState.PAST;
                }
                appointmentBuilder.setTimeSlot(timeSlot);
                appointmentBuilder.setStatus(status);

                //build appointment
                Appointment appointment = appointmentBuilder.build();

                //Retrieve the customer who owns the appointment
                Customer customer = customerRepository.get(appointment.getCustomerId());
                //customer may be removed mistakenly (orphan record)
                if(customer != null) {
                    //add to the customers appointment history
                    customer.addAppointment(appointment);
                }

                Barber barber = barberRepository.get(appointment.getBarberId());
                if(barber != null){
                    barber.addAppointment(appointment);
                }
                //Time slot of the barber schedule must be occupied If the appointment is active
                if(appointment.getStatus() == AppointmentState.ACTIVE){
                    //barber may be removed mistakenly (orphan record) so, do null check
                    if(barber != null && barber.getSchedule().containsKey(appointment.getTimeSlot())) {
                        barber.updateSchedule(timeSlot, false);
                    }
                }
                //add to appointments map
                appointmentMap.put(appointment.getAppointmentID(), appointment);
            }
        }catch (FileNotFoundException e){
            System.out.println("File not found. "+ e.getMessage());
        }catch (IOException e){
            System.out.println("Error occurred while reading the file: " + this.appointmentsFilePath +" " + e.getMessage());
        }
    }

    /**
     * Saves all currently existing appointments to the data source
     */
    public void save(){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(this.appointmentsFilePath))){
            //write the titles first
            bw.write("appointmentId;customerId;barberId;timeSlot;status");
            bw.newLine();

            for(Appointment appointment: appointmentMap.values()){
                //csv line: appointmentId;customerId;barberId;timeSlot;status
                String line = String.join(";", appointment.getAppointmentID(), appointment.getCustomerId(), appointment.getBarberId(), String.valueOf(appointment.getTimeSlot()), String.valueOf(appointment.getStatus()));
                bw.write(line);
                bw.newLine();
            }
        }catch (IOException e){
            System.out.println("Error occurred while writing to the file: " + this.appointmentsFilePath + " " + e.getMessage());
        }
    }

}
