package hcmuaf.sad.pet_store.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ResetPasswordController {

    @GetMapping("/auth/reset-password")
    public String showForm() {
        // TODO: UC-4
        throw new UnsupportedOperationException("TODO: UC-4");
    }

    @PostMapping("/auth/reset-password")
    public String requestReset() {
        // TODO: UC-4
        throw new UnsupportedOperationException("TODO: UC-4");
    }
}
