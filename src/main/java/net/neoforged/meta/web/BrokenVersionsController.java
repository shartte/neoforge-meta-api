package net.neoforged.meta.web;

import net.neoforged.meta.db.BrokenSoftwareComponentVersionDao;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
}
