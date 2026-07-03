package hcmuaf.sad.pet_store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/error-page")
    public String error(Model model) {
        model.addAttribute("errorMessage", "Có lỗi xảy ra. Vui lòng thử lại.");
        return "error/generic";
    }
}
