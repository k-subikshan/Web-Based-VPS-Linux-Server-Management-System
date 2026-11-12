package serverviwe.view.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import serverviwe.view.service.AuditService;
import serverviwe.view.service.SSHService;

@Controller @RequiredArgsConstructor
public class TerminalController {
    private final SSHService ssh; private final AuditService audit;
    @GetMapping("/run_command") public String terminal(HttpSession s,Model m){m.addAttribute("path",s.getAttribute("path")); return "run_command";}
    @PostMapping("/run_command") public String run(@RequestParam String command,HttpSession s,Model m){
        try{String cmd=command==null?"":command.trim(); validate(cmd); String path=SSHService.normalizePath((String)s.getAttribute("path")); String full="cd -- "+SSHService.shellQuote(path)+" && "+cmd; SSHService.CommandResult r=ssh.execute((String)s.getAttribute("host"),(Integer)s.getAttribute("port"),(String)s.getAttribute("username"),(String)s.getAttribute("password"),full); m.addAttribute("output",r.combined());m.addAttribute("command",cmd);m.addAttribute("path",path); audit.log("COMMAND",(String)s.getAttribute("host"),(String)s.getAttribute("username"),cmd); }catch(Exception e){m.addAttribute("output","ERROR: "+e.getMessage());m.addAttribute("command",command);m.addAttribute("path",s.getAttribute("path"));} return "run_command";
    }
    private void validate(String c){if(c.isBlank())throw new IllegalArgumentException("Command is empty"); if(c.length()>2000)throw new IllegalArgumentException("Command is too long"); String lower=c.toLowerCase(); String[] blocked={" rm -"," rm ","mkfs","shutdown","reboot","poweroff","halt","dd if="," :(){","fork bomb","passwd ","useradd ","userdel ","chmod 777","chown -r","wget ","curl ","nc ","ncat ","socat ","base64 -d"}; for(String b:blocked)if(lower.contains(b))throw new IllegalArgumentException("This command is blocked by the safety policy."); if(lower.matches(".*(^|[;&|])\\s*sudo(\\s|$).*"))throw new IllegalArgumentException("sudo is not allowed from the web terminal.");}
}
