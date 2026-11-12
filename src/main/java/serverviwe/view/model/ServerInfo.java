package serverviwe.view.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "server_info", indexes = @Index(name = "idx_server_ip_port", columnList = "serverIp,sshPort", unique = true))
public class ServerInfo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 255)
    private String serverIp;
    @Column(nullable = false, length = 100)
    private String serverName;
    @Column(nullable = false)
    private int sshPort = 22;
    @Column(nullable = false, length = 100)
    private String username = "root";
}
