package serverviwe.view.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import serverviwe.view.service.MlAssistantService;
import serverviwe.view.service.ServerMonitorService;
import java.util.Map;

@Controller
public class MlAssistantController {
    private final MlAssistantService assistant;
    private final ServerMonitorService monitor;
    public MlAssistantController(MlAssistantService assistant, ServerMonitorService monitor) { this.assistant = assistant; this.monitor = monitor; }

    @GetMapping("/ml-assistant")
    public String page(HttpSession session, Model model) {
        if (session.getAttribute("host") == null) return "redirect:/connecttovps";
        model.addAttribute("host", session.getAttribute("host"));
        return "ml-assistant";
    }

    @PostMapping("/api/ml/assistant")
    @ResponseBody
    public Map<String,Object> chat(@RequestBody ChatRequest request, HttpSession session) throws Exception {
        String host = (String) session.getAttribute("host");
        if (host == null || host.isBlank()) throw new IllegalStateException("Connect to a VPS before using the ML assistant.");
        Integer port = (Integer) session.getAttribute("port");
        String username = (String) session.getAttribute("username");
        String password = (String) session.getAttribute("password");
        Map<String,String> stats = monitor.getServerStats(host, port == null ? 22 : port, username, password);
        return assistant.chat(host, request == null ? "" : request.message(), stats);
    }

    @GetMapping("/api/ml/analysis")
    @ResponseBody
    public Map<String,Object> analysis(HttpSession session) throws Exception {
        String host = (String) session.getAttribute("host");
        if (host == null || host.isBlank()) throw new IllegalStateException("Connect to a VPS first.");
        Integer port = (Integer) session.getAttribute("port");
        Map<String,String> stats = monitor.getServerStats(host, port == null ? 22 : port, (String)session.getAttribute("username"), (String)session.getAttribute("password"));
        return assistant.chat(host, "diagnose server and predict failure risk", stats).get("analysis") instanceof Map<?,?> m ? (Map<String,Object>)m : Map.of();
    }

    public record ChatRequest(String message) {}
}
