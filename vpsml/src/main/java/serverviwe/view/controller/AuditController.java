package serverviwe.view.controller;
import lombok.RequiredArgsConstructor;import org.springframework.stereotype.Controller;import org.springframework.ui.Model;import org.springframework.web.bind.annotation.GetMapping;import serverviwe.view.reposiotory.AuditLogRepository;
@Controller @RequiredArgsConstructor public class AuditController {private final AuditLogRepository repo;@GetMapping("/audit") public String audit(Model m){m.addAttribute("logs",repo.findTop100ByOrderByCreatedAtDesc());return "audit";}}
