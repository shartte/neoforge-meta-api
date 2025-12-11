package net.neoforged.meta.web;

import net.neoforged.meta.db.BrokenSoftwareComponentVersionDao;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class BrokenVersionsController {
    private final BrokenSoftwareComponentVersionDao dao;

    public BrokenVersionsController(BrokenSoftwareComponentVersionDao dao) {
        this.dao = dao;
    }

    @GetMapping("/ui/broken-versions")
    @Transactional(readOnly = true)
    public String brokenVersions(Model model) {
        var versions = dao.findAll(Sort.by(Sort.Direction.DESC, "lastAttempt"));
        model.addAttribute("versions", versions);

        return "broken-versions";
    }

    @GetMapping("/ui/broken-versions/version/{groupId}/{artifactId}/{version}")
    @Transactional(readOnly = true)
    public String brokenVersions(@PathVariable("groupId") String groupId,
                                 @PathVariable("artifactId") String artifactId,
                                 @PathVariable("version") String versionId,
                                 Model model) {
        var version = dao.findByGAV(groupId, artifactId, versionId);
        if (version == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("version", version);
        model.addAttribute("errors", List.copyOf(version.getErrors()));

        return "broken-version";
    }
}
