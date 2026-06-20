package core.dto.request;


public class BarberEnrollmentRequestDTO extends UserEnrollmentRequestDTO {
    private final String shopName;

    /**
     * Initializes the barber enrollment request DTO object.
     * @param firstName First name of the barber.
     * @param lastName Last nane of the barber.
     * @param phone The phone number of the barber.
     * @param rawPassword The raw (not hashed) password of the barber.
     * @param shopName The shop name of the barber.
     */
    public BarberEnrollmentRequestDTO(String firstName, String lastName, String phone, String rawPassword, String shopName){
        super(firstName, lastName, phone,rawPassword);
        this.shopName = shopName;
    }

    public String getShopName(){
        return this.shopName;
    }
}
