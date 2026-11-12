package serverviwe.view.reposiotory;

import org.springframework.data.jpa.repository.JpaRepository;
import serverviwe.view.model.AuditLog;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop100ByOrderByCreatedAtDesc();
}
