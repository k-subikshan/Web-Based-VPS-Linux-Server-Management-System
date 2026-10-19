package serverviwe.view.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter
@Entity @Table(name = "audit_log", indexes = @Index(name = "idx_audit_created", columnList = "createdAt"))
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String action;
    @Column(length = 255)
    private String host;
    @Column(length = 100)
    private String username;
    @Column(length = 2000)
    private String details;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
