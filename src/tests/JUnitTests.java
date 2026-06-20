package tests;

import core.appointment.AppointmentService;
import core.auth.AuthService;
import core.dto.response.AuthResponseDTO;
import core.security.IHashStrategy;
import core.security.Sha256HashStrategy;
import core.session.UserSession;
import entity.appointment.Appointment;
import entity.appointment.AppointmentState;
import entity.user.UserRole;
import entity.user.barber.Barber;
import infrastructure.appointment.AppointmentRepository;
import infrastructure.common.IRepository;
import infrastructure.user.BarberRepository;
import infrastructure.user.CustomerRepository;
import org.junit.jupiter.api.Test;


import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class JUnitTests {

    /**
     * Tests the 'Book an Appointment' use case when the selected schedule is occupied
     */
    @Test
    public void testBookAppointment_WhenSlotIsOccupied_ShouldThrowException() {

        BarberRepository barberRepo = new BarberRepository("src/tests/testBarbers.csv");
        CustomerRepository custRepo = new CustomerRepository("src/tests/testCustomers.csv");
        IRepository<Appointment> appRepo = new AppointmentRepository("src/tests/testApps.csv", barberRepo, custRepo);
        AppointmentService appService = new AppointmentService(custRepo, barberRepo, appRepo);

        String barberId = "b-1";
        LocalDateTime targetSlot = LocalDateTime.of(2026, 6, 1, 14, 0); // 01.06.2026 14:00

        Barber.BarberBuilder barberBuilder = new Barber.BarberBuilder();
        barberBuilder.setUserId("b-1");
        barberBuilder.setFirstName("ahmet");
        barberBuilder.setLastName("yilmaz");
        barberBuilder.setShopName("barber shop");
        Barber barber = barberBuilder.build();
        barber.getSchedule().put(targetSlot, false); // slot occupied
        barberRepo.add(barber.getUserId(), barber);


        // 2. ACT & ASSERT
        //Should throw illegal arg. exception
        assertThrows(IllegalArgumentException.class, () -> {
            appService.bookAnAppointment("c-1", barberId, targetSlot);
        }, "ERROR: Exception was not thrown!!");
    }

    /**
     * Tests the cascaded remove operation. When a user is removed from the system, the related appointments
     * must be updated as CANCELLED.
     */
    @Test
    public void testRemoveUser_WhenBarberIsRemoved_ShouldCancelActiveAppointments() {
        // 1. ARRANGE
        BarberRepository barberRepo = new BarberRepository("src/tests/testBarbers.csv");
        CustomerRepository custRepo = new CustomerRepository("src/tests/testCustomers.csv");
        IRepository<Appointment> appRepo = new AppointmentRepository("src/tests/testApps.csv", barberRepo, custRepo);

        IHashStrategy hashStrategy = new Sha256HashStrategy();
        UserSession session = UserSession.getInstance();
        AuthService authService = new AuthService(custRepo, barberRepo, appRepo, hashStrategy,session);

        AuthResponseDTO authResponseDTO = new AuthResponseDTO("a1", "admin", UserRole.ADMIN);
        //simulate admin session
        session.startSession(authResponseDTO);

        String barberId = "b-555";
        Barber.BarberBuilder barberBuilder = new Barber.BarberBuilder();
        barberBuilder.setUserId(barberId);
        barberBuilder.setFirstName("Hakan");
        barberBuilder.setLastName("Kaya");
        barberBuilder.setShopName("Star Barber");
        Barber barber = barberBuilder.build();

        Appointment.AppointmentBuilder appointmentBuilder = new Appointment.AppointmentBuilder();
        appointmentBuilder.setAppointmentID("app-999");
        appointmentBuilder.setCustomerId("c-5");
        appointmentBuilder.setBarberId(barberId);
        appointmentBuilder.setTimeSlot(LocalDateTime.now().plusDays(1));
        appointmentBuilder.setStatus(AppointmentState.ACTIVE);

        Appointment activeApp = appointmentBuilder.build();
        barber.getAppointmentsHistory().add(activeApp); // add to barber's history

        barberRepo.add(barber.getUserId(), barber);
        appRepo.add(activeApp.getAppointmentID(), activeApp);

        // 2. ACT Admin removes the barber
        authService.removeUser(barberId);

        // 3. ASSERT
        // Was the barber deactivated?
        assertFalse(barber.isActive(), "ERROR: Account was not deactivated!");

        // Was the appointment state updated as CANCELLED
        assertEquals(AppointmentState.CANCELLED, activeApp.getStatus(), "ERROR: The active appointment was not cancelled.");
    }

}

