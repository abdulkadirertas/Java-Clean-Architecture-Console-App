package util;
import java.util.UUID;

public class IdGenerator {
    private IdGenerator() {} //prevent creating an instance

    public static String generateCustomerId() {
        return "c-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String generateBarberId() {
        return "b-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String generateAppointmentId() {
        return "app-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
