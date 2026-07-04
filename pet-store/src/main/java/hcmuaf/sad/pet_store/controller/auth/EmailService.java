package hcmuaf.sad.pet_store.controller.auth;

public interface EmailService {
    void sendOtp(String email, String otp);
}
