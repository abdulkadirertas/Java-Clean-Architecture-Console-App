package core.session;

import core.dto.response.AuthResponseDTO;

public class UserSession {
    //singleton instance
    private static UserSession instance;
    private AuthResponseDTO loggedInUser;

    private UserSession(){};

    /**
     * Returns a session instance based on singleton manner.
     * @return The session object
     */
    public static UserSession getInstance(){
        if(instance == null){
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Starts a new user session and stores the authenticated user's credentials.
     * @param userDTO The AuthResponseDTO object containing the session owner's information.
     */
    public void startSession(AuthResponseDTO userDTO){
        this.loggedInUser = userDTO;
    }


    public AuthResponseDTO getLoggedInUser(){
        return loggedInUser;
    }
    public boolean isLoggedIn(){
        return this.loggedInUser !=null;
    }
    public void endSession(){
        this.loggedInUser = null;
    }

}
