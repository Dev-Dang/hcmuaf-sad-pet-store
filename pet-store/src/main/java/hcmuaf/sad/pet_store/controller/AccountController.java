package hcmuaf.sad.pet_store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

    @GetMapping("/account")
    public String accountMenu() {
        return "account/account-menu";
    }
}
