package serverviwe.view.service;

import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ServiceMonitor {
    private final SSHService ssh;
    public ServiceMonitor(SSHService ssh) { this.ssh=ssh; }
    public Map<String,String> getServices(String host,int port,String user,String password) throws Exception {
        Map<String,String> result=new LinkedHashMap<>();
        for(String s: new String[]{"nginx","mysql","mariadb","docker","gunicorn","springboot","apache2"}) {
            try { result.put(s, ssh.execute(host,port,user,password,"systemctl is-active " + SSHService.shellQuote(s) + " 2>/dev/null || true").stdout().trim()); }
            catch(Exception e){ result.put(s,"unknown"); }
        }
        return result;
    }
    public String action(String host,int port,String user,String password,String service,String action) throws Exception {
        if(!service.matches("[A-Za-z0-9_.@-]{1,80}")) throw new IllegalArgumentException("Invalid service name");
        if(!action.matches("start|stop|restart|status")) throw new IllegalArgumentException("Invalid service action");
        // Service control intentionally uses systemctl directly; sudo policy on the VPS determines permission.
        return ssh.execute(host,port,user,password,"systemctl " + action + " " + service + " 2>&1").combined();
    }
}
