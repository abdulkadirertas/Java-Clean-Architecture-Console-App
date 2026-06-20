package core.dto.response;

import entity.user.UserRole;

public class AuthResponseDTO {
    private final String userId;
    private final String firstName;
    private final UserRole role;

    /**
     * Initializes the authorization DTO object.
     * @param userId The ID of the user.
     * @param firstName First name of the user.
     * @param role The role of the user logged in (e.g, ADMIN, CUSTOMER, BARBER).
     */
    public AuthResponseDTO(String userId, String firstName, UserRole role){
        this.userId = userId;
        this.firstName = firstName;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }
    public String getFirstName() {
        return firstName;
    }
    public UserRole getRole() {
        return role;
    }
}
