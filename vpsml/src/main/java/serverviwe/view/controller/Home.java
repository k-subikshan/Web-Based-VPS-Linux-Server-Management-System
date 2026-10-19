package serverviwe.view.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Home {
    @GetMapping({"/", "/dashboard"})
    public String home(HttpSession session) {
        return session.getAttribute("host") == null ? "redirect:/connecttovps" : "dashboard";
    }
}
