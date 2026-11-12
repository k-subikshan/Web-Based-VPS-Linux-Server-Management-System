package serverviwe.view.service;

import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ServerMonitorService {
    private final SSHService ssh;
    public ServerMonitorService(SSHService ssh) { this.ssh = ssh; }

    public Map<String,String> getServerStats(String host, int port, String user, String password) throws Exception {
        Map<String,String> stats = new LinkedHashMap<>();
        String cpu = ssh.execute(host, port, user, password, "LC_ALL=C top -bn1 | awk '/Cpu\\(s\\)/ {print 100-$8}'").stdout().trim();
        String ram = ssh.execute(host, port, user, password, "free -m | awk '/^Mem:/ {if ($2>0) printf \"%.1f\", ($3/$2)*100}'").stdout().trim();
        String disk = ssh.execute(host, port, user, password, "df -P / | awk 'NR==2 {gsub(/%/,\"\",$5); print $5}'").stdout().trim();
        String uptime = ssh.execute(host, port, user, password, "uptime -p 2>/dev/null || uptime").stdout().trim();
        String load = ssh.execute(host, port, user, password, "cut -d' ' -f1-3 /proc/loadavg").stdout().trim();
        String network = ssh.execute(host, port, user, password, "awk 'NR>2 {rx+=$2; tx+=$10} END {printf \"%.2f\", (rx+tx)/1024/1024}' /proc/net/dev").stdout().trim();
        stats.put("cpu", safe(cpu)); stats.put("ram", safe(ram)); stats.put("disk", safe(disk)); stats.put("network", safe(network)); stats.put("uptime", uptime); stats.put("load", load); stats.put("status", "connected");
        return stats;
    }
    private String safe(String s) { try { double d=Double.parseDouble(s); return String.format("%.1f", Math.max(0, Math.min(100,d))); } catch(Exception e){ return "0.0"; } }
}
