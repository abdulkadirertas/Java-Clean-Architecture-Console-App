package entity.user;


public abstract class User {
    private final String userId;
    private String firstName;
    private String lastName;
    private String phone;
    private String password;
    private boolean isActive;

    protected User(UserBuilder builder){
        this.firstName = builder.firstName;
        this.lastName  =builder.lastName;
        this.phone = builder.phone;
        this.password = builder.password;
        this.isActive = builder.isActive;
        this.userId = builder.userId;
    }

    //Getter Methods
    public String getUserId(){
        return this.userId;
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
    public String getPassword() {
        return this.password;
    }
    public boolean isActive() {
        return this.isActive;
    }

    public void deactivateAccount(){
        this.isActive = false;
    }
    public void activateAccount(){
        this.isActive = true;
    }

    //Static abstract builder class
    public static abstract class UserBuilder {
        protected String userId;
        protected String firstName;
        protected String lastName;
        protected String phone;
        protected String password;
        protected boolean isActive = true; //new users are default active

        //setter methods of the user builder
        public void setUserId(String userId){
            this.userId = userId;
        }
        public void setFirstName(String firstName){
            this.firstName = firstName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        public void setPhone(String phone) {
            this.phone = phone;
        }
        public void setPassword(String password) {
            this.password = password;
        }
        public void setActive(boolean active) {
            isActive = active;
        }


        /**
         * Constructs and returns a new user instance based on the configured properties.
         * * @return The newly constructed user instance
         */
        public abstract User build();
    }
}
