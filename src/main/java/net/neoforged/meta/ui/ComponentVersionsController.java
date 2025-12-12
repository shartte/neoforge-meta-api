package net.neoforged.meta.ui;

import net.neoforged.meta.db.SoftwareComponentVersionDao;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ComponentVersionsController {
    private final SoftwareComponentVersionDao dao;

    public ComponentVersionsController(SoftwareComponentVersionDao dao) {
        this.dao = dao;
    }


    @GetMapping("/ui/component-versions")
    @Transactional(readOnly = true)
    public String componentVersions(Model model) {
        var versions = dao.findAll(Sort.by(Sort.Direction.DESC, "released"));
        model.addAttribute("versions", versions);

        return "component-versions";
    }
}
