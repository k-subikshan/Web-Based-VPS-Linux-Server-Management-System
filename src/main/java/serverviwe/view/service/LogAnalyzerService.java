package serverviwe.view.service;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class LogAnalyzerService {
    private final SSHService ssh;
    public LogAnalyzerService(SSHService ssh){this.ssh=ssh;}
    public String getLogs(String host,int port,String user,String password,String source,int lines) throws Exception {
        int n=Math.max(10,Math.min(lines,500));
        String command=switch(source){
            case "nginx" -> "tail -n " + n + " /var/log/nginx/error.log 2>&1";
            case "auth" -> "journalctl -u ssh -n " + n + " --no-pager 2>&1";
            case "mysql" -> "journalctl -u mysql -n " + n + " --no-pager 2>&1";
            case "mariadb" -> "journalctl -u mariadb -n " + n + " --no-pager 2>&1";
            case "gunicorn" -> "journalctl -u gunicorn -n " + n + " --no-pager 2>&1";
            case "springboot" -> "journalctl -u springboot -n " + n + " --no-pager 2>&1";
            default -> throw new IllegalArgumentException("Unknown log source");
        };
        return ssh.execute(host,port,user,password,command).combined();
    }
    public Map<String,String> summary(String host,int port,String user,String password) throws Exception {
        String out=ssh.execute(host,port,user,password,"journalctl -p warning..alert -n 100 --no-pager 2>&1").combined();
        long errors=out.lines().filter(x->x.matches(".*(?i)(error|fail|critical|denied).*" )).count();
        return Map.of("warningLines",String.valueOf(out.lines().count()),"errorLikeLines",String.valueOf(errors));
    }
}
