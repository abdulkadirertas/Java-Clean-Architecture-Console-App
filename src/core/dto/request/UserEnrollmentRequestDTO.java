package core.dto.request;

public abstract class UserEnrollmentRequestDTO {
    private final String firstName;
    private final String lastName;
    private final String phone;
    private final String rawPassword;

    /**
     * Initializes the user enrollment request DTO object.
     * @param firstName First name of the user.
     * @param lastName Last nane of the user.
     * @param phone The phone number of the user.
     * @param rawPassword The raw (not hashed) password of the user.
     */
    public UserEnrollmentRequestDTO(String firstName, String lastName, String phone, String rawPassword){
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.rawPassword = rawPassword;
    }

    //Getter Methods
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getPhone() {
        return phone;
    }
    public String getRawPassword() {
        return rawPassword;
    }
}
