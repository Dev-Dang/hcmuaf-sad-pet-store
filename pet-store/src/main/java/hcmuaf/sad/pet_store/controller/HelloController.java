package hcmuaf.sad.pet_store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "redirect:/products";
    }

    @GetMapping("/hello")
    public String helloPage() {
        return "redirect:/products";
    }
}
