package core.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class Sha256HashStrategy implements IHashStrategy {

    /**
     * Hashes the raw password based on SHA-256 cryptographic algorithm.
     * @param rawPassword The raw password to be hashed.
     * @return The hashed password represented as a hexadecimal string.
     * @throws RuntimeException if the SHA-256 digest algorithm is not found.
     */
    public String hashPassword(String rawPassword){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] digest = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);

        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException("Error: No Such a Digest Algorithm in the System. ", e);
        }
    }
}
