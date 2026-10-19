package serverviwe.view.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serverviwe.view.model.ProjectDeployed;
import serverviwe.view.reposiotory.ProjectDeployedRepository;
import java.util.List;

@Service @RequiredArgsConstructor
public class ProjectDeployedService {
    private final ProjectDeployedRepository repository;
    public List<ProjectDeployed> list(Long serverId){ return serverId==null?repository.findAll():repository.findByServerInfoId(serverId); }
    public ProjectDeployed save(ProjectDeployed p){ return repository.save(p); }
    public void delete(Long id){ repository.deleteById(id); }
}
