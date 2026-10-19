package serverviwe.view.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import serverviwe.view.service.ServerMonitorService;
import serverviwe.view.service.ServiceMonitor;
import java.util.Map;

@Controller @RequiredArgsConstructor
public class ServerMonitorController {
    private final ServerMonitorService monitor; private final ServiceMonitor services;
    @GetMapping("/server-monitor") public String page(HttpSession s,Model m){try{String h=(String)s.getAttribute("host");int p=(Integer)s.getAttribute("port");String u=(String)s.getAttribute("username"),pw=(String)s.getAttribute("password"); if(h==null) return "redirect:/connecttovps"; m.addAllAttributes(monitor.getServerStats(h,p,u,pw));var serviceMap=services.getServices(h,p,u,pw); m.addAllAttributes(serviceMap); m.addAttribute("services",serviceMap);m.addAttribute("host",h);m.addAttribute("port",p);m.addAttribute("username",u);m.addAttribute("path",s.getAttribute("path"));return "server-monitor";}catch(Exception e){m.addAttribute("error",e.getMessage());return "server-monitor";}}
    @GetMapping("/api/monitor") @ResponseBody public Map<String,String> api(HttpSession s)throws Exception{return monitor.getServerStats((String)s.getAttribute("host"),(Integer)s.getAttribute("port"),(String)s.getAttribute("username"),(String)s.getAttribute("password"));}
}
