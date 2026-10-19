package serverviwe.view.service;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight adaptive anomaly detector for VPS metrics. It maintains a bounded,
 * in-memory history per host and combines threshold, trend and deviation signals.
 * This is an explainable online statistical model, not a pre-trained neural network.
 */
@Service
public class MlAssistantService {
    private static final int HISTORY_LIMIT = 60;
    private final Map<String, Deque<Sample>> history = new ConcurrentHashMap<>();

    public synchronized Map<String, Object> analyze(String host, Map<String, String> raw) {
        String key = (host == null || host.isBlank()) ? "current-server" : host.trim();
        double cpu = number(raw.get("cpu"));
        double ram = number(raw.get("ram"));
        double disk = number(raw.get("disk"));
        double load = number(first(raw.get("load"), "0").split("\\s+")[0]);
        Deque<Sample> samples = history.computeIfAbsent(key, k -> new ArrayDeque<>());
        Sample now = new Sample(Instant.now().toString(), cpu, ram, disk, load);
        samples.addLast(now);
        while (samples.size() > HISTORY_LIMIT) samples.removeFirst();

        List<String> findings = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        int score = 0;
        score += thresholdScore(cpu, 75, 90, "CPU", findings, recommendations);
        score += thresholdScore(ram, 80, 92, "Memory", findings, recommendations);
        score += thresholdScore(disk, 80, 92, "Disk", findings, recommendations);
        if (load >= 4) { score += load >= 8 ? 25 : 12; findings.add("Load average is elevated (" + fmt(load) + ")."); recommendations.add("Inspect top CPU processes and compare load average with the server's CPU core count."); }

        if (samples.size() >= 4) {
            Sample[] arr = samples.toArray(new Sample[0]);
            Sample previous = arr[arr.length - 2];
            if (cpu - previous.cpu >= 15) { score += 12; findings.add("CPU rose by " + fmt(cpu - previous.cpu) + " percentage points since the previous sample."); recommendations.add("Inspect the highest CPU processes and recent application logs."); }
            if (ram - previous.ram >= 12) { score += 12; findings.add("Memory usage is rising quickly."); recommendations.add("Check for memory-heavy processes, leaks and swap activity."); }
            if (disk - previous.disk >= 5) { score += 10; findings.add("Disk usage increased by at least 5 percentage points since the previous sample."); recommendations.add("Find large log files and old temporary files; review before deleting anything."); }
            double avgCpu = arr.length > 1 ? Arrays.stream(arr, 0, arr.length - 1).mapToDouble(s -> s.cpu).average().orElse(cpu) : cpu;
            double avgRam = arr.length > 1 ? Arrays.stream(arr, 0, arr.length - 1).mapToDouble(s -> s.ram).average().orElse(ram) : ram;
            if (arr.length >= 6 && cpu > avgCpu + 2 * stddev(arr, false)) { score += 15; findings.add("CPU is an unusual outlier compared with this session's recent baseline."); recommendations.add("Compare the current process list and application logs with the recent baseline."); }
            if (arr.length >= 6 && ram > avgRam + 2 * stddev(arr, true)) { score += 15; findings.add("Memory is an unusual outlier compared with this session's recent baseline."); recommendations.add("Inspect memory growth and restart only after assessing service impact."); }
        } else {
            findings.add("Learning baseline: " + samples.size() + " sample(s) collected. Trend-based confidence will improve after more checks.");
        }
        score = Math.min(100, score);
        String risk = score >= 60 ? "HIGH" : score >= 30 ? "MEDIUM" : "LOW";
        if (findings.isEmpty()) findings.add("No threshold or trend anomaly detected in the available metrics.");
        if (recommendations.isEmpty()) recommendations.add("Continue monitoring. Collect more samples to establish a more useful per-session baseline.");
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("host", key); result.put("risk", risk); result.put("riskScore", score);
        result.put("sampleCount", samples.size()); result.put("metrics", Map.of("cpuPercent", cpu, "memoryPercent", ram, "diskPercent", disk, "loadAverage", load));
        result.put("findings", findings); result.put("recommendations", recommendations);
        result.put("model", "Adaptive threshold + trend + statistical deviation detector");
        result.put("note", "Risk score is an estimate, not a guarantee of failure. History is in memory and resets when the application restarts.");
        return result;
    }

    public Map<String,Object> chat(String host, String message, Map<String,String> stats) {
        Map<String,Object> analysis = analyze(host, stats);
        String q = message == null ? "" : message.toLowerCase(Locale.ROOT);
        StringBuilder answer = new StringBuilder();
        if (containsAny(q, "predict", "failure", "fail", "risk", "health", "diagnos", "slow", "problem", "issue", "analy")) {
            answer.append("ML-assisted server assessment for ").append(analysis.get("host")).append(": risk ").append(analysis.get("risk")).append(" (").append(analysis.get("riskScore")).append("/100).\n\n");
            answer.append("Findings:\n"); for (Object f : (List<?>)analysis.get("findings")) answer.append("• ").append(f).append("\n");
            answer.append("\nRecommended next steps:\n"); for (Object r : (List<?>)analysis.get("recommendations")) answer.append("• ").append(r).append("\n");
        } else if (containsAny(q, "cpu", "ram", "memory", "disk", "load", "metric", "usage")) {
            Map<?,?> metrics = (Map<?,?>)analysis.get("metrics");
            answer.append("Current server metrics:\n• CPU: ").append(metrics.get("cpuPercent")).append("%\n• Memory: ").append(metrics.get("memoryPercent")).append("%\n• Disk: ").append(metrics.get("diskPercent")).append("%\n• Load average: ").append(metrics.get("loadAverage")).append("\n\n");
            answer.append("Current risk estimate: ").append(analysis.get("risk")).append(" (").append(analysis.get("riskScore")).append("/100). Ask me to diagnose the server for the detailed findings.");
        } else if (containsAny(q, "help", "what can", "capabilit", "commands")) {
            answer.append("I can analyze current CPU, memory, disk and load metrics; flag unusual changes; estimate operational risk; and suggest safe troubleshooting steps. Try: 'diagnose my server', 'predict failure risk', 'show CPU and RAM', or 'why is my server slow?'. I do not run or restart commands from this assistant.");
        } else {
            answer.append("I can help assess VPS health using the live metrics available to this application. Try asking 'diagnose my server', 'predict failure risk', or 'show CPU, RAM and disk usage'.");
        }
        Map<String,Object> response = new LinkedHashMap<>(); response.put("answer", answer.toString()); response.put("analysis", analysis); return response;
    }

    private int thresholdScore(double value, double warning, double critical, String label, List<String> findings, List<String> recommendations) {
        if (value >= critical) { findings.add(label + " usage is critical at " + fmt(value) + "%. "); recommendations.add("Investigate " + label.toLowerCase(Locale.ROOT) + " pressure immediately and check recent logs before changing services."); return 25; }
        if (value >= warning) { findings.add(label + " usage is high at " + fmt(value) + "%. "); recommendations.add("Monitor " + label.toLowerCase(Locale.ROOT) + " usage and identify resource-heavy processes."); return 12; }
        return 0;
    }
    private double stddev(Sample[] samples, boolean memory) { double[] vals = Arrays.stream(samples, 0, samples.length - 1).mapToDouble(s -> memory ? s.ram : s.cpu).toArray(); double mean = Arrays.stream(vals).average().orElse(0); return Math.sqrt(Arrays.stream(vals).map(v -> (v-mean)*(v-mean)).average().orElse(0)); }
    private static boolean containsAny(String s, String... terms) { for (String t:terms) if (s.contains(t)) return true; return false; }
    private static String first(String a, String b) { return a == null || a.isBlank() ? b : a; }
    private static double number(String s) { try { return Math.max(0, Double.parseDouble(s == null ? "0" : s.trim().replace("%", ""))); } catch(Exception e) { return 0; } }
    private static String fmt(double n) { return String.format(Locale.ROOT, "%.1f", n); }
    private record Sample(String time, double cpu, double ram, double disk, double load) {}
}
