package core.security;

public interface IHashStrategy {
    /**
     * Hashes the given raw password to enhance the security.
     * @param rawPassword The raw password to be hashed.
     * @return The hashed password.
     */
    String hashPassword(String rawPassword);
}
