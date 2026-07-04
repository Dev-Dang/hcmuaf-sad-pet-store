package hcmuaf.sad.pet_store.model.policy;

public class OtpPolicy {
    public static final int OTP_LENGTH = 6;
    public static final int MAX_ATTEMPT = 5;
    public static final int MAX_RESEND = 5;
    public static final int OTP_TTL_MINUTES = 10;
    public static final int CHALLENGE_TTL_MINUTES = 30;
    public static final int RESEND_COOLDOWN_SECONDS = 60;

    private OtpPolicy() {
    }
}
