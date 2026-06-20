package core.dto.request;

public class CustomerEnrollmentRequestDTO extends UserEnrollmentRequestDTO {

    /**
     * Initializes the customer enrollment request DTO object.
     * @param firstName First name of the customer.
     * @param lastName Last nane of the customer.
     * @param phone The phone number of the customer.
     * @param rawPassword The raw (not hashed) password of the customer.
     */
    public CustomerEnrollmentRequestDTO(String firstName, String lastName, String phone, String rawPassword){
        super(firstName,lastName,phone,rawPassword);
    }
}
