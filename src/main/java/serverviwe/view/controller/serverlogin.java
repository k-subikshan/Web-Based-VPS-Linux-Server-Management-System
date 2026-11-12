package serverviwe.view.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import serverviwe.view.layer.vpsloginform;
import serverviwe.view.model.ServerInfo;
import serverviwe.view.reposiotory.ServerInfoRepository;
import serverviwe.view.service.AuditService;
import serverviwe.view.service.SSHService;

@Controller @RequiredArgsConstructor
public class serverlogin {
    private final SSHService ssh;
    private final ServerInfoRepository servers;
    private final AuditService audit;

    @GetMapping("/connecttovps")
    public String connectvps(Model model) { if(!model.containsAttribute("vpsloginform")) model.addAttribute("vpsloginform",new vpsloginform()); return "vpslogin"; }

    @PostMapping("/connect")
    public String connect(@Valid @ModelAttribute("vpsloginform") vpsloginform form, BindingResult errors, HttpSession session, Model model) {
        form.setHost(form.getHost()==null?null:form.getHost().trim()); form.setUsername(form.getUsername()==null?null:form.getUsername().trim()); form.setPath(SSHService.normalizePath(form.getPath()));
        if(errors.hasErrors()){ model.addAttribute("error","Please correct the highlighted fields."); return "vpslogin"; }
        try {
            SSHService.CommandResult test=ssh.execute(form.getHost(),form.getPort(),form.getUsername(),form.getPassword(),"printf connected");
            if(test.exitCode()!=0) throw new IllegalStateException(test.combined());
            session.setAttribute("host",form.getHost()); session.setAttribute("port",form.getPort()); session.setAttribute("username",form.getUsername()); session.setAttribute("password",form.getPassword()); session.setAttribute("path",form.getPath());
            servers.findByServerIpAndSshPort(form.getHost(),form.getPort()).ifPresentOrElse(s->{s.setUsername(form.getUsername());s.setServerName(form.getHost());servers.save(s);},()->{ServerInfo s=new ServerInfo();s.setServerIp(form.getHost());s.setServerName(form.getHost());s.setSshPort(form.getPort());s.setUsername(form.getUsername());servers.save(s);});
            audit.log("SSH_CONNECT",form.getHost(),form.getUsername(),"Successful SSH connection");
            return "redirect:/dashboard";
        } catch(Exception e){ model.addAttribute("error",friendly(e)); return "vpslogin"; }
    }

    @PostMapping("/disconnect")
    public String disconnect(HttpSession session){ audit.log("SSH_DISCONNECT",(String)session.getAttribute("host"),(String)session.getAttribute("username"),"User disconnected"); session.invalidate(); return "redirect:/connecttovps"; }
    @GetMapping("/logout") public String logout(HttpSession session){ session.invalidate(); return "redirect:/connecttovps"; }
    private String friendly(Exception e){ String m=e.getMessage(); if(m==null||m.isBlank()) return "Unable to connect to the VPS."; if(m.contains("UnknownHost")) return "Host/IP is invalid or contains unexpected characters. Check the host value."; if(m.contains("Connection refused")) return "SSH connection was refused. Verify that SSH is running and the port is open."; if(m.toLowerCase().contains("auth fail")) return "SSH authentication failed. Check username/password."; return m; }
}
