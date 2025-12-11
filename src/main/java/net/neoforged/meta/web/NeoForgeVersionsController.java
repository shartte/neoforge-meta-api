package net.neoforged.meta.web;

import net.neoforged.meta.maven.NeoForgeVersionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller("uiNeoForgeVersionsController")
public class NeoForgeVersionsController {
    private final NeoForgeVersionService service;

    public NeoForgeVersionsController(NeoForgeVersionService service) {
        this.service = service;
    }

    @GetMapping("/ui/neoforge-versions")
    @Transactional(readOnly = true)
    public String versions(Model model) {
        model.addAttribute("versions", service.getVersions());
        return "neoforge-versions";
    }

    @GetMapping("/ui/neoforge-versions/version/{version}")
    @Transactional(readOnly = true)
    public String versionDetail(@PathVariable String version, Model model) {
        var neoForgeVersion = service.getVersion(version);
        if (neoForgeVersion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("version", neoForgeVersion);

        return "component-version-detail";
    }
}
