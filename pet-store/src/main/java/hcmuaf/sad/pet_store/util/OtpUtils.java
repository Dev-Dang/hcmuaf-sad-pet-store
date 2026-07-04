package hcmuaf.sad.pet_store.util;

import hcmuaf.sad.pet_store.model.policy.OtpPolicy;

import java.security.SecureRandom;

public class OtpUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    private OtpUtils() {
    }

    public static String generate() {
        int upperBound = (int) Math.pow(10, OtpPolicy.OTP_LENGTH);
        return String.format("%0" + OtpPolicy.OTP_LENGTH + "d", RANDOM.nextInt(upperBound));
    }

    public static boolean verify(String otp, String hash) {
        return PasswordUtils.verify(otp, hash);
    }
}
