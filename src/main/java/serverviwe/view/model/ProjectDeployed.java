package serverviwe.view.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity @Table(name = "project_deployed")
public class ProjectDeployed {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "server_info_id")
    private ServerInfo serverInfo;
    @Column(nullable = false, length = 150)
    private String projectName;
    @Column(length = 500)
    private String projectPath;
    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.UNDEPLOYED;
    public enum ProjectStatus { DEPLOYED, UNDEPLOYED }
}
