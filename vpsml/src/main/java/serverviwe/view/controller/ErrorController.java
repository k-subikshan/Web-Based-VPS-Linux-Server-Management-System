package serverviwe.view.controller;
import org.springframework.stereotype.Controller;import org.springframework.web.bind.annotation.GetMapping;
@Controller public class ErrorController { @GetMapping("/error-page") public String error(){return "error";} }
