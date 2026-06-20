package entity.user.barber;

import entity.appointment.Appointment;
import entity.user.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class Barber extends User{
    private String shopName;
    private ApplicationState approvalStatus = ApplicationState.PENDING; //default
    private final List<Appointment> appointmentsHistory= new ArrayList<>();
    private final Map<LocalDateTime, Boolean> schedule =  new TreeMap<>();

    private Barber(BarberBuilder builder){
        super(builder);
        this.shopName = builder.shopName;
        this.approvalStatus = builder.approvalStatus;
    }

    //Getter Methods
    public String getShopName(){
        return this.shopName;
    }
    public ApplicationState getApprovalStatus(){
        return this.approvalStatus;
    }
    public Map<LocalDateTime, Boolean> getSchedule(){
        return this.schedule;
    }
    public List<Appointment> getAppointmentsHistory(){return this.appointmentsHistory;}

    /**
     * Returns the availability of the time slot
     * @param timeSlot specified time slot
     * @return true if the time slot exists and available, false if it does not exist or occupied.
     */
    public boolean isAvailable(LocalDateTime timeSlot){
        return this.schedule != null && this.schedule.getOrDefault(timeSlot, false);
    }

    public void updateApprovalStatus(ApplicationState status){
        this.approvalStatus = status;
    }

    /**
     * Adds an appointment to the barber's appointment history.
     * @param appointment The appointment to be added to the history list.
     */
    public void addAppointment(Appointment appointment){
        this.appointmentsHistory.add(appointment);
    }

    /**
     * Generates a live schedule for next 6 days except for sunday
     */
    public void generateFreshSchedule() {
        LocalDate date = LocalDate.now();
        int daysAdded = 0;

        while (daysAdded < 6) {
            if (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                for (int hour = 10; hour <= 22; hour++) {
                    LocalDateTime slot = LocalDateTime.of(date, LocalTime.of(hour, 0));
                    this.schedule.put(slot, true); // true = free
                }
                daysAdded++;
            }
            date = date.plusDays(1);
        }
    }

    /**
     * Updates the specified time slot in the barber schedule.
     * @param timeSlot time slot to be updated.
     * @param isAvailable false if the slot is occupied, true if available
     */
    public void updateSchedule(LocalDateTime timeSlot, boolean isAvailable){
        schedule.put(timeSlot, isAvailable);
    }

    //static builder class
    public static class BarberBuilder extends UserBuilder{
        private String shopName;
        private ApplicationState approvalStatus = ApplicationState.PENDING; //default

        //Extra (does not exist in the user builder) setter methods for the barber builder
        public void setShopName(String shopName){
            this.shopName = shopName;
        }
        public void setApprovalStatus(ApplicationState status){
            this.approvalStatus = status;
        }

        /**
         * Constructs and returns a new Barber instance based on the configured properties.
         * * @return The newly constructed Barber instance
         */
        public Barber build(){
            return new Barber(this);
        }
    }
}
