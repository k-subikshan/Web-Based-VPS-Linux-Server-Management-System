package serverviwe.view.reposiotory;

import org.springframework.data.jpa.repository.JpaRepository;
import serverviwe.view.model.ProjectDeployed;
import java.util.List;

public interface ProjectDeployedRepository extends JpaRepository<ProjectDeployed, Long> {
    List<ProjectDeployed> findByServerInfoId(Long serverId);
}
