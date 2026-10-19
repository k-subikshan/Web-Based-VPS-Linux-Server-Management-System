package serverviwe.view.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serverviwe.view.model.AuditLog;
import serverviwe.view.reposiotory.AuditLogRepository;
import java.time.LocalDateTime;

@Service @RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository repository;
    public void log(String action, String host, String username, String details) {
        AuditLog l = new AuditLog(); l.setAction(action); l.setHost(host); l.setUsername(username); l.setDetails(details); l.setCreatedAt(LocalDateTime.now()); repository.save(l);
    }
}
