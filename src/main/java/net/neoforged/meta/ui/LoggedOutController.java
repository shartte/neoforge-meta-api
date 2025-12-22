package net.neoforged.meta.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoggedOutController {

    @GetMapping("/ui/logout/success")
    public String loggedOut() {
        return "logged-out";
    }

}
