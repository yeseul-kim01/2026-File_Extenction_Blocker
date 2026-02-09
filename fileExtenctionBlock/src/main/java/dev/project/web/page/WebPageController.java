package dev.project.web.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/policy")
    public String policy() {
        return "policy";
    }
}
