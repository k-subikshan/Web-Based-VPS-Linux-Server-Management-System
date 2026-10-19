package serverviwe.view.controller;
import jakarta.servlet.http.HttpSession; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.web.bind.annotation.*; import serverviwe.view.service.SSLMonitorService; import serverviwe.view.service.WebsiteMonitorService;
@Controller @RequiredArgsConstructor public class WebsiteController {private final WebsiteMonitorService web;private final SSLMonitorService ssl;
@GetMapping("/website-monitor") public String page(){return "website-monitor";}
@PostMapping("/website-monitor/check") public String check(@RequestParam String website,Model m){try{m.addAttribute("website",website);m.addAttribute("result",web.check(website));if(website.startsWith("https://"))m.addAttribute("sslDays",ssl.getDaysRemaining(website));}catch(Exception e){m.addAttribute("error",e.getMessage());m.addAttribute("website",website);}return "website-monitor";}}
