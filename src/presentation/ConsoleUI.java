package presentation;

import core.dto.request.BarberEnrollmentRequestDTO;
import core.dto.request.CustomerEnrollmentRequestDTO;
import core.dto.response.AppointmentResponseDTO;
import core.dto.response.AuthResponseDTO;
import core.dto.response.BarberResponseDTO;
import core.facade.AppointmentSystemFacade;
import entity.appointment.AppointmentState;
import entity.user.UserRole;
import entity.user.barber.ApplicationState;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;


public class ConsoleUI {
    private final Scanner scanner= new Scanner(System.in);
    private final AppointmentSystemFacade facade = new AppointmentSystemFacade();

    public void start(){
        showMainMenu();
    }


    //MENUS

    /**
     * Demonstrates the main menu content and redirects to the other menus.
     */
    private void showMainMenu(){
        while(true) {
            System.out.println("\n--- BARBER APPOINTMENT SYSTEM ---");
            System.out.println("1-Login\n2-Sign up as a Customer\n3-Apply to system as a Barber\n0-Exit");
            String menu = takeInput("Please select an operation.");

            switch (menu){
                case "1":
                    showLoginMenu();
                    break;
                case "2":
                    showCustomerEnrollmentMenu();
                    break;
                case "3":
                    showBarberEnrollmentMenu();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Please select a valid operation.");
                    break;
            }
        }
    }

    /**
     * Demonstrates the admin menu.
     */
    private void showAdminMenu(){
        while(true) {
            System.out.println("\n-- ADMIN MENU --");
            System.out.println("1-View Applications\n2-Remove a user\n0-Logout");

            String op = takeInput("\nPlease select an operation.");
            if (op.equals("1")) {
                System.out.println("\n-- View Applications --");

                while(true) {
                    List<BarberResponseDTO> applicantList = facade.getPendingApplications();
                    if(applicantList.isEmpty()){
                        System.out.println("No pending application found!");
                        break;
                    }else{
                        printBarbersList(applicantList);
                    }

                    String in = takeInput("\nYou can approve or reject any application.\n1-Approve an application\n2-Reject an application\n0-Go back to Admin Menu");
                    if (in.equals("1") || in.equals("2")) {
                        //approve-reject an application
                        try {
                            String pendingBarberId = takeIdInput("\nPlease enter a user id. (0 for admin menu)", 10);
                            //approve the application
                            if(in.equals("1")) {
                                facade.updateApprovalStatus(pendingBarberId, ApplicationState.APPROVED);
                                System.out.println("SUCCESS: Application status successfully updated as APPROVED.\n");
                            }else{
                                //reject the application
                                facade.updateApprovalStatus(pendingBarberId, ApplicationState.REJECTED);
                                System.out.println("SUCCESS: Application status successfully updated as REJECTED.\n");
                            }
                        }catch (IllegalArgumentException e){
                            //barber cannot be found
                            System.out.println("ERROR: " + e.getMessage());
                        }
                    } else if (in.equals("0")) {
                        //go back to admin menu
                        break;
                    } else {
                        System.out.println("Invalid input.");
                    }
                }

            } else if (op.equals("2")) {
                //remove a user
                System.out.println("\n-- Remove User --");
                printAllActiveUsers();
                while(true) {
                    String removedUserId = takeIdInput("\nPlease enter the ID of the user to be removed. (0 for Admin menu)", 10);
                    if (removedUserId.equals("0")) {
                        //go back to admin menu
                        break;
                    }
                    try{
                        facade.removeUser(removedUserId);
                        System.out.println("SUCCESS: The user removed successfully");

                    }catch (IllegalArgumentException e){
                        System.out.println("ERROR: " +e.getMessage());
                    }
                }

            } else if (op.equals("0")) {
                facade.logout();
                System.out.println("Logged out...");
                break;
            } else {
                System.out.println("Please enter a valid operation.");
            }
        }
    }

    /**
     * Demonstrates the customer menu.
     */
    private void showCustomerMenu(){
        while(true){
            System.out.println("\n-- Customer Menu --");
            System.out.println("1-Book an Appointment\n2-View Active Appointments\n3-View Appointment History\n4-Cancel Appointment\n5-Remove My Account\n0-Logout");

            String op = takeInput("Please select a menu.");
            if(op.equals("1")){
                bookAnAppointmentMenu();
            }else if(op.equals("2")){
                printActiveAppointmentDetails();
            }else if(op.equals("3")){
                printCustomersAppointmentHistory();
            }else if(op.equals("4")) {
                showCancelAppointmentMenu();
            }else if(op.equals("5")){
                try {
                    String isRemoved =removeUserAccount(facade.getLoggedInUser().getUserId());
                    if(isRemoved.equals("1"))break; //Account was removed. Go back to main menu
                }catch (IllegalArgumentException e){
                    System.out.println("ERROR: " + e.getMessage());
                }

            }else if(op.equals("0")){
                //Logout. Redirecting to main menu
                System.out.println("Logged out...");
                facade.logout();
                break;
            }else{
                System.out.println("Please enter a valid input.");
            }
        }
    }

    /**
     * Demonstrates the barber menu.
     */
    private void showBarberMenu(){
        AuthResponseDTO barber = facade.getLoggedInUser();
        ApplicationState status = facade.getBarbersApplicationStatus(barber.getUserId());

        if(status == ApplicationState.PENDING){
            System.out.println("\nInfo: Your application is currently UNDER REVIEW (PENDING). Please check back later.");
            facade.logout();
            return;
        }
        if(status == ApplicationState.REJECTED){
            System.out.println("\nInfo: Your application has been REJECTED by the Admin.\nYour account has been removed.");
            facade.removeUser(barber.getUserId());
            facade.logout();
            return;
        }

        while(true){
            System.out.println("\n-- Barber Menu --");
            System.out.println("1-View Active Appointments\n2-Cancel An Appointment\n3-View Schedule\n4-Remove My Account\n0-Logout");
            String op = takeInput("Please select a menu.");
            if(op.equals("0")){
                System.out.println("Logged out...");
                facade.logout();
                return;
            }
            if(op.equals("1")){
                //View active appointments
                printActiveAppointmentDetails();
            }else if(op.equals("2")) {
                //cancel an appointment
                showCancelAppointmentMenu();
            }else if(op.equals("3")) {
                String barberId = facade.getLoggedInUser().getUserId();
                Map<LocalDateTime, Boolean> schedule = facade.getBarbersScheduleDTO(barberId).getSchedule();
                printSchedule(schedule);
            }else if(op.equals("4")){
                try{
                    removeUserAccount(facade.getLoggedInUser().getUserId());
                    System.out.println("\nYour account successfully REMOVED.");
                    facade.logout();
                    break;
                }catch (IllegalArgumentException e){
                    System.out.println("ERROR: " + e.getMessage());
                }
            }else{
                System.out.println("Please select a valid menu.");
            }
        }
    }


    //USE CASES
    /**
     * Demonstrates the customer enrollment menu. Takes necessary account information and enrolls to the system.
     */
    private void showCustomerEnrollmentMenu(){
        String firstName="", lastName="", phone="", rawPassword="";


        while(true){
            System.out.println("\n-- Sign Up as Customer --");

            firstName = takeNameInput("\nPlease enter your first name. (0 for main menu)");
            if(firstName.equals("0")){
                System.out.println("Enrollment cancelled. Redirecting to main menu.");
                break;
            }

            lastName = takeNameInput("\nPlease enter your last name. (0 for main menu)");
            if(lastName.equals("0")){
                System.out.println("Enrollment cancelled. Redirecting to main menu.");
                break;
            }

            phone = takePhoneInput();
            if(phone.equals("0")){
                System.out.println("Enrollment cancelled. Redirecting to main menu.");
                break;
            }

            rawPassword = takeNewPasswordInput();
            if(rawPassword.equals("0")){
                System.out.println("Enrollment cancelled. Redirecting to main menu.");
                break;
            }

            String personalInfo = "\nPlease confirm your personal information.\nFirst Name: " + firstName + "\nLast Name: " + lastName + "\nPhone Number: " + phone + "\n";
            String confirmInfo = confirmInformation(personalInfo);
            if(confirmInfo.equals("0")){
                System.out.println("Enrollment cancelled. Redirecting to main menu.");
                break;
            }else if(confirmInfo.equals("1")){
                try {
                    CustomerEnrollmentRequestDTO customerRequest = new CustomerEnrollmentRequestDTO(firstName, lastName, phone, rawPassword);
                    facade.enrollCustomer(customerRequest);
                    System.out.println("\nSUCCESS! You can sign in with your new account.");
                    System.out.println("Redirecting to main menu.");
                    break;
                }catch (IllegalArgumentException e){
                    System.out.println("\nERROR! " + e.getMessage());
                    System.out.println("Please sign up with another phone number.");
                }
            }
        }

    }

    /**
     * Demonstrates the barber enrollment (application) menu. Takes necessary account information and enrolls to the system.
     */
    private void showBarberEnrollmentMenu(){
        String firstName="", lastName="", phone="", shopName="", rawPassword="";

        while(true){
            System.out.println("\n-- Apply as Barber --");

            firstName = takeNameInput("\nPlease enter your first name. (0 for main menu)");
            if(firstName.equals("0")){
                System.out.println("Application cancelled. Redirecting to main menu.");
                break;
            }

            lastName = takeNameInput("\nPlease enter your last name. (0 for main menu)");
            if(lastName.equals("0")){
                System.out.println("Application cancelled. Redirecting to main menu.");
                break;
            }

            phone = takePhoneInput();
            if(phone.equals("0")){
                System.out.println("Application cancelled. Redirecting to main menu.");
                break;
            }

            shopName = takeShopNameInput();
            if(shopName.equals("0")){
                System.out.println("Application cancelled. Redirecting to main menu.");
                break;
            }

            rawPassword = takeNewPasswordInput();
            if(rawPassword.equals("0")){
                System.out.println("Application cancelled. Redirecting to main menu.");
                break;
            }

            String personalInfo = "\nPlease confirm your personal information.\nFirst Name: " + firstName + "\nLast Name: " + lastName + "\nPhone Number: " + phone +"\nShop Name: " +shopName +"\n";
            String confirmInfo = confirmInformation(personalInfo);
            if(confirmInfo.equals("0")){
                System.out.println("Application cancelled. Redirecting to main menu.");
                break;
            }else if(confirmInfo.equals("1")){
                try {
                    BarberEnrollmentRequestDTO barberRequest = new BarberEnrollmentRequestDTO(firstName, lastName, phone, rawPassword, shopName);
                    facade.enrollBarber(barberRequest);
                    System.out.println("\nSUCCESS! Application is received. Your application status: PENDING\nYou can view your application status when you log in the system.");
                    System.out.println("Redirecting to main menu.");
                    break;
                }catch (IllegalArgumentException e){
                    System.out.println("\nERROR! " + e.getMessage());
                    System.out.println("Please sign up with another phone number.");
                }
            }
        }
    }

    /**
     * Demonstrates the login menu. Takes phone and password information from the user and tries to log in.
     */
    private void showLoginMenu(){
        String phone="", rawPassword="";
        while(true){
            System.out.println("\n-- Login --");

            phone = takePhoneInput();
            if(phone.equals("0")){
                System.out.println("Login cancelled. Redirecting to main menu.");
                break;
            }

            rawPassword = takeInput("Please enter your password. (0 for main menu)");
            if(rawPassword.equals("0")){
                System.out.println("Login cancelled. Redirecting to main menu.");
                break;
            }

            try{
                AuthResponseDTO user = facade.login(phone,rawPassword);
                System.out.println("SUCCESS: Login is successful. Welcome " + user.getFirstName());

                //Redirect the user to the related menus
                if(user.getRole() == UserRole.ADMIN){
                    showAdminMenu();
                    break;
                }
                else if(user.getRole() == UserRole.CUSTOMER){
                    showCustomerMenu();
                    break;
                }
                else if(user.getRole() == UserRole.BARBER){
                    showBarberMenu();
                    break;
                }

            }catch (IllegalArgumentException e){
                System.out.println("ERROR: " +e.getMessage());
            }
        }
    }

    /**
     * Demonstrates the appointment cancellation menu.
     */
    private void showCancelAppointmentMenu(){

        while(true) {
            System.out.println("\n- Cancel an Appointment -");
            AuthResponseDTO currentUser = facade.getLoggedInUser();
            String currentUserId = currentUser.getUserId();
            List<AppointmentResponseDTO> appointmentHistory;

            if(currentUser.getRole() == UserRole.CUSTOMER){
                appointmentHistory = facade.getCustomersAppointmentHistory(currentUserId);
            }else{
                appointmentHistory = facade.getBarbersAppointmentHistory(currentUserId);
            }

            List<AppointmentResponseDTO> activeAppointments = new ArrayList<>();
            for(AppointmentResponseDTO app: appointmentHistory){
                if(app.getStatus() == AppointmentState.ACTIVE)activeAppointments.add(app);
            }

            //No active appointment exists
            if(activeAppointments.isEmpty()){
                System.out.println("No active appointment to cancel.");
                return;
            }
            activeAppointments.sort(Comparator.comparing(AppointmentResponseDTO::getTimeSlot));
            //printActiveAppointments method can be used but, it contains same active appointments query also. So, to prevent double fetch from the repository, print is performed here.
            System.out.println("\n- Active Appointments -");
            for(AppointmentResponseDTO app : activeAppointments) {
                printAppointment(app);
            }

            String appointmentId = takeIdInput("\nPlease enter the ID of the appointment you want to cancel. (0 for Menu)", 12);
            if (appointmentId.equals("0")) return;

            boolean isOwnedByUser = false;
            AppointmentResponseDTO cancelledAppointment= null;
            for(AppointmentResponseDTO app: activeAppointments){
                if(app.getAppointmentId().equals(appointmentId)){
                    isOwnedByUser=true;
                    cancelledAppointment = app;
                    break;
                }
            }

            if(!isOwnedByUser){
                System.out.println("Invalid Appointment ID. Please enter an ID from your active appointments list.");
                continue;
            }

            printAppointment(cancelledAppointment);
            String confirmation = confirmInformation("\nAre you sure you want to CANCEL this appointment?");
            if(confirmation.equals("0")){
                System.out.println("Cancellation aborted.");
                return;
            }else if(confirmation.equals("1")){
                try {
                    facade.updateAppointmentStatus(appointmentId, AppointmentState.CANCELLED);
                    System.out.println("SUCCESS: Appointment cancelled successfully.");
                    return;
                } catch (IllegalArgumentException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }else if(confirmation.equals("2")){
                System.out.println("Cancellation aborted. Try with another ID.");
            }
        }
    }

    /**
     * Removes the user from the system.
     * @param id The ID of the user to be removed.
     * @return 1 if the account was removed successfully, 0 if the user cancels the operation.
     */
    private String removeUserAccount(String id){
        while(true){
            String confirm = takeInput("Are you sure to REMOVE your account? 1-Yes 0-No");

            if(confirm.equals("1")){
                facade.removeUser(id);
                System.out.println("\nYour account successfully REMOVED.");
                facade.logout();
                return "1";
            }else if(confirm.equals("0")){
                System.out.println("Removal operation cancelled.");
                return "0";
            }else{
                System.out.println("Please enter a valid input.");
            }
        }
    }

    /**
     * Demonstrates the appointment booking menu.
     */
    private void bookAnAppointmentMenu(){
        AuthResponseDTO currentCustomer = facade.getLoggedInUser();
        System.out.println("\n- Book An Appointment -");
        List<BarberResponseDTO> activeBarbers = facade.getActiveBarbers();
        if(activeBarbers.isEmpty()){
            System.out.println("\nNo active barber found.");
            return;
        }


        while(true){
            printBarbersList(activeBarbers);
            String barberIndexInput = takeInput("Please enter the index of the barber. (0 for Customer Menu)");
            if(barberIndexInput.equals("0"))return;

            if(barberIndexInput.matches("\\d+")){
                int barberIndex = Integer.parseInt(barberIndexInput);
                if(barberIndex>0 && barberIndex<=activeBarbers.size()){

                    BarberResponseDTO selectedBarber = activeBarbers.get(barberIndex-1);
                    String selectedBarberId = selectedBarber.getBarberId();
                    Map<LocalDateTime, Boolean>  selectedBarberSchedule = facade.getBarbersScheduleDTO(selectedBarberId).getSchedule();
                    printSchedule(selectedBarberSchedule);

                    while(true) {
                        LocalDateTime selectedTimeSlot = takeTimeSlotInput();
                        if (selectedTimeSlot == null) return;

                        if (!selectedBarberSchedule.containsKey(selectedTimeSlot)) {
                            System.out.println("ERROR: Selected time is not in the schedule! Please select a valid date from the list.");
                            continue;
                        }
                        boolean isAvailable = selectedBarberSchedule.get(selectedTimeSlot);
                        if (!isAvailable) {
                            System.out.println("Selected slot is occupied. Please select an available slot");
                        } else {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                            String formattedDate = selectedTimeSlot.format(formatter);

                            String appDetails = "\nPlease confirm your appointment details." +
                                    "\nAppointment Details:" + "\nDate: " + formattedDate
                                    + "\nBarber Details:\nBarber Name:" + selectedBarber.getFirstName() + " " + selectedBarber.getLastName()
                                    + "\nShop Name: " + selectedBarber.getShopName() + "\n";

                            String confirmation = confirmInformation(appDetails);
                            if (confirmation.equals("1")) {
                                //confirmed by the user
                                try {
                                    facade.bookAnAppointment(currentCustomer.getUserId(), selectedBarberId, selectedTimeSlot);
                                    System.out.println("SUCCESS: Appointment is created successfully.\nYou can view your appointment details in the 'View Active Appointments' menu.");
                                    return;
                                } catch (IllegalArgumentException e) {
                                    System.out.println("ERROR: " + e.getMessage());
                                }

                            } else if (confirmation.equals("0")) {
                                //go back to menu
                                System.out.println("Operation cancelled.");
                                return;
                            }else if(confirmation.equals("2"))break;
                            //if confirmation is equal to "2" (edit), starts from the beginning of the loop
                        }
                    }

                }else{
                    System.out.printf("Please enter a valid index value. Valid interval is--> min: 1, max: %d\n",activeBarbers.size());
                }
            }else{
                System.out.println("The input must be an integer. Please enter an index only ");
            }
        }
    }


    //INPUT METHODS

    /**
     * Prints the given prompt and takes an input from the user.
     * @param prompt The prompt message to be printed before the taking input.
     * @return String input.
     */
    private String takeInput(String prompt){
        System.out.print(prompt + "\nInput: ");
        return scanner.nextLine().trim();
    }

    /**
     * Takes a name input from the user.
     * @param prompt The prompt message to be printed before the taking input.
     * @return String name input. 0 for main menu.
     */
    private String takeNameInput(String prompt){
        while(true) {
            String name = takeInput(prompt);
            if(name.equals("0")){
                return "0";
            }

            if(name.isEmpty() || name.length()>30){
                System.out.println("Please enter a valid input. Name cannot be empty or more than thirty characters.");
            }else{
                return name;
            }
        }
    }

    /**
     * Takes a phone number input from the user.
     * @return String phone number input.
     */
    private String takePhoneInput(){
        while(true){
            String phone = takeInput("\nPlease enter your phone number. (0 for main menu)");
            if(phone.equals("0"))return "0";

            if(phone.length()<5 || phone.length()>15){
                System.out.println("Please enter a valid input. The phone number must be between 5-15 characters long.");
            }else return phone;
        }
    }

    /**
     * Takes a new password input two times. Checks the both passwords whether they matches or not.
     * @return String new password input.
     */
    private String takeNewPasswordInput(){
        while(true){
            String second;
            String rawPassword = takeInput("\nPlease create your password. (0 for main menu)");
            if(rawPassword.equals("0"))return "0";

            if(rawPassword.length()<4 || rawPassword.length()>20){
                System.out.println("Password must be minimum 4, maximum 20 characters.\n");
            }else{
                second = takeInput("\nPlease enter your password again. (0 for main menu)");
                if(second.equals("0"))return "0";

                if(rawPassword.equals(second))return rawPassword;
                else{
                    System.out.println("Entered passwords does not match. Please try again.");
                }
            }
        }
    }

    /**
     * Takes a new shop name input.
     * @return String shop name input.
     */
    private String takeShopNameInput(){
        while(true){
            String shopName = takeNameInput("\nPlease enter your shop's name. (0 for main menu)");
            if(shopName.equals("0"))return "0";

            if(shopName.isEmpty() || shopName.length()>50){
                System.out.println("Please enter a valid input. Shop name cannot be empty and more than fifty characters.");
            }else return shopName;
        }
    }

    /**
     * Takes a time slot input from the user in the specified format.
     * @return LocalDateTime time slot input.
     */
    private LocalDateTime takeTimeSlotInput(){
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        while(true) {
            String selectedSlot = takeInput("Please enter the appointment date and hour. (0 for Customer menu)\n(Format: GG.AA.YYYY-SS, Example: 20.05.2026-11)");
            if (selectedSlot.equals("0")) return null;

            try {
                String[] parts = selectedSlot.split("-");
                if (parts.length != 2) {
                    System.out.println("Please enter in the correct format");
                    continue;
                }


                LocalDate date = LocalDate.parse(parts[0].trim(), dateFormatter);
                int hour = Integer.parseInt(parts[1].trim());

                if (hour < 10 || hour > 21) {
                    System.out.println("Please select an appointment in the working hours (10.00-22.00).");
                    continue;
                }

                return LocalDateTime.of(date, LocalTime.of(hour, 0));

            } catch (DateTimeParseException | NumberFormatException e) {
                System.out.println("Invalid time or hour format.");
            }
        }
    }

    /**
     * Takes an ID input from the user. ID input can be used to cancel an appointment or to remove a user.
     * @param prompt The prompt message to be printed before taking input.
     * @param idLength The expected ID length (e.g, for appointments:12, for users:10).
     * @return String ID input
     */
    private String takeIdInput(String prompt, int idLength){
        while(true) {
            String id = takeInput(prompt);
            if (id.equals("0")) return "0";
            if(id.length() != idLength){
                System.out.println("Invalid id format.");
            }else{
                return id;
            }
        }
    }

    /**
     * Prompts the given message and ask users for confirmation.
     * @param prompt The prompt message to be printed before the taking input.
     * @return Returns following string values:
     * 1- if operation is confirmed by the user.
     * 2- if the user wants to edit the operation.
     * 0- if the user cancels the operation.
     */
    private String confirmInformation(String prompt){
        while(true) {
            System.out.printf(prompt);
            String in = takeInput("\n1-Confirm\n2-Edit (Starting from the beginning)\n0-Cancel and go back to menu");

            if (in.equals("0") || in.equals("1") || in.equals("2")) {
                return in;
            }
            else {
                System.out.println("Please enter a valid information. (0 for main menu)");
            }
        }
    }


    //PRINT METHODS (Also contains some USE CASES)

    /**
     * Prints the given barber list.
     * @param barberList A list containing BarberResponseDTO objects.
     */
    private void printBarbersList(List<BarberResponseDTO> barberList){
        int i=1;
        for(BarberResponseDTO barber: barberList){
            System.out.printf("%d) -Barber ID: %-15s  - Barber Name: %-15s  - Shop Name: %-15s  - Phone: %-15s%n%n",i, barber.getBarberId(),(barber.getFirstName() +" " +barber.getLastName()), barber.getShopName() ,barber.getPhone());
            i++;
        }
    }

    /**
     * Prints the given barber schedule.
     * @param schedule The schedule map containing time slots and their availabilities.
     */
    private void printSchedule(Map<LocalDateTime, Boolean> schedule){

        // Format the slots
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy EEEE", Locale.ENGLISH);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        //Linked list to keep in sorted order
        Map<LocalDate, List<String>> dailySlots = new LinkedHashMap<>();

        LocalDateTime now = LocalDateTime.now();


        for (Map.Entry<LocalDateTime, Boolean> entry : schedule.entrySet()) {
            if(entry.getKey().isBefore(now))continue; //skip past slots

            LocalDate date = entry.getKey().toLocalDate();
            String time = entry.getKey().format(timeFormatter);
            String status = entry.getValue() ? "Available" : "Occupied";

            dailySlots.computeIfAbsent(date, k -> new ArrayList<>()).add(time + " - " + status);
        }

        List<LocalDate> dates = new ArrayList<>(dailySlots.keySet());

        System.out.println("\n- BARBER SCHEDULE -\n");

        // Print dates
        for (LocalDate date : dates) {
            //Left alignment
            System.out.printf("%-26s", date.format(dateFormatter));
        }
        System.out.println();


        for (int i = 0; i < dates.size(); i++) {
            System.out.printf("%-26s", "------------------");
        }
        System.out.println();

        //Print hours
        int maxSlots = 13; //13 slots exist

        for (int row = 0; row < maxSlots; row++) {
            for (LocalDate date : dates) {
                List<String> slots = dailySlots.get(date);
                if (slots != null && row < slots.size()) {
                    System.out.printf("%-26s", slots.get(row));
                } else {
                    System.out.printf("%-26s", "");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Prints the active appointments
     */
    private void printActiveAppointmentDetails(){
        AuthResponseDTO currentUser = facade.getLoggedInUser();

        List<AppointmentResponseDTO> appointments;
        if(currentUser.getRole() == UserRole.CUSTOMER){
            appointments = facade.getCustomersAppointmentHistory(currentUser.getUserId());
        }else{
            appointments = facade.getBarbersAppointmentHistory(currentUser.getUserId());
        }

        List<AppointmentResponseDTO> activeAppointments = new ArrayList<>();

        for(AppointmentResponseDTO app: appointments){
            if(app.getStatus() == AppointmentState.ACTIVE)activeAppointments.add(app);
        }

        if(activeAppointments.isEmpty()){
            System.out.println("\nNo active appointment is found.");
            return;
        }
        activeAppointments.sort(Comparator.comparing(AppointmentResponseDTO::getTimeSlot));
        System.out.println("\n-Active Appointments-");
        for(AppointmentResponseDTO app: activeAppointments){
            printAppointment(app);
        }
    }

    /**
     * Prints the customer's appointment history.
     */
    private void printCustomersAppointmentHistory() {
        List<AppointmentResponseDTO> appointments = facade.getCustomersAppointmentHistory(facade.getLoggedInUser().getUserId());
        List<AppointmentResponseDTO> pastAppointments = new ArrayList<>();
        List<AppointmentResponseDTO> cancelledAppointments = new ArrayList<>();

        for (AppointmentResponseDTO app : appointments) {
            if (app.getStatus() == AppointmentState.PAST) pastAppointments.add(app);
            else if (app.getStatus() == AppointmentState.CANCELLED) cancelledAppointments.add(app);
        }

        pastAppointments.sort(Comparator.comparing(AppointmentResponseDTO::getTimeSlot).reversed());
        cancelledAppointments.sort(Comparator.comparing(AppointmentResponseDTO::getTimeSlot).reversed());

        if (pastAppointments.isEmpty()) {
            System.out.println("\nNo past appointment found.");
        } else{
            System.out.println("\n- Past Appointments -");
            for (AppointmentResponseDTO app : pastAppointments) {
            printAppointment(app);
            }
        }

        if(cancelledAppointments.isEmpty()){
            System.out.println("\nNo cancelled appointment found");
        }else {
            System.out.println("\n- Cancelled Appointments -");
            for (AppointmentResponseDTO app : cancelledAppointments) {
                printAppointment(app);
            }
        }
    }

    /**
     * Prints the given appointment details.
     * @param appointment The AppointmentResponseDTO object containing appointment details.
     */
    private void printAppointment(AppointmentResponseDTO appointment){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String formattedDate = appointment.getTimeSlot().format(formatter);
        System.out.printf(
                """
                        
                        %-20s : %s
                        %-20s : %s
                        %-20s : %s
                        %-20s : %s
                        %-20s : %s
                        %-20s : %s
                        %-20s : %s
                        """,

                "Appointment ID", appointment.getAppointmentId(),
                "Customer Name", appointment.getCustomerFullName(),
                "Barber Id", appointment.getBarberId(),
                "Barber Name", appointment.getBarberFullName(),
                "Shop Name", appointment.getBarbersShopName(),
                "Appointment Date", formattedDate,
                "Appointment Status", appointment.getStatus()
        );
        System.out.println("--------------------------------------------------");
    }

    /**
     * Prints all active users in the system.
     */
    private void printAllActiveUsers(){
        List<AuthResponseDTO> allUsers = facade.getAllActiveUsers();
        for(AuthResponseDTO user: allUsers){
            String role = (user.getRole() == UserRole.CUSTOMER)? "Customer":"Barber";
            System.out.printf("""
                          %-20s: %s
                          %-20s: %s
                          %-20s: %s
                          """,
                    "User ID" ,user.getUserId(),
                    "User First Name", user.getFirstName(),
                    "User Role", role
            );
            System.out.println("--------------------");
        }
    }



}
