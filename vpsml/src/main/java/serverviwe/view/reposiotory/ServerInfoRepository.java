package serverviwe.view.reposiotory;

import org.springframework.data.jpa.repository.JpaRepository;
import serverviwe.view.model.ServerInfo;
import java.util.Optional;

public interface ServerInfoRepository extends JpaRepository<ServerInfo, Long> {
    Optional<ServerInfo> findByServerIpAndSshPort(String serverIp, int sshPort);
}
