package hcmuaf.sad.pet_store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("/hello")
    public String helloPage() {
        return "index";  // mapping đến file index.html trong thư mục resources/templates
    }
}
