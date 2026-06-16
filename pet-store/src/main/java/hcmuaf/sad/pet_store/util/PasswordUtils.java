package hcmuaf.sad.pet_store.util;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class PasswordUtils {
    private static final Argon2PasswordEncoder encoder =
            Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public static String hash(String raw) {
        return encoder.encode(raw);
    }

    public static boolean verify(String raw, String hash) {
        return encoder.matches(raw, hash);
    }
}
