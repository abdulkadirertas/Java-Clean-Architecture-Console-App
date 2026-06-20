package core.dto.response;

public class BarberResponseDTO {
    private final String barberId;
    private final String firstName;
    private final String lastName;
    private final String phone;
    private final String shopName;

    /**
     * Initializes the barber response DTO object.
     * @param barberId The ID of the barber.
     * @param firstName First name of the barber.
     * @param lastName Last name of the barber.
     * @param phone The phone number of the barber.
     * @param shopName The shop name of the barber.
     */
    public BarberResponseDTO(String barberId, String firstName, String lastName, String phone, String shopName){
        this.barberId = barberId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.shopName = shopName;
    }

    public String getBarberId() {
        return this.barberId;
    }
    public String getFirstName() {
        return this.firstName;
    }
    public String getLastName() {
        return this.lastName;
    }
    public String getPhone() {
        return this.phone;
    }
    public String getShopName() {
        return this.shopName;
    }
}
