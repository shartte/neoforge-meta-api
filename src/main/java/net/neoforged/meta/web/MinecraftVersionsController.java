package net.neoforged.meta.web;

import net.neoforged.meta.db.MinecraftVersionDao;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller("uiVersionsController")
public class MinecraftVersionsController {
    private final MinecraftVersionDao minecraftVersionDao;

    public MinecraftVersionsController(MinecraftVersionDao minecraftVersionDao) {
        this.minecraftVersionDao = minecraftVersionDao;
    }

    @GetMapping("/ui/minecraft-versions")
    @Transactional(readOnly = true)
    public String versions(Model model) {
        var minecraftVersions = minecraftVersionDao.findAll(Sort.by(Sort.Direction.DESC, "released"));
        model.addAttribute("minecraftVersions", minecraftVersions);

        return "minecraft-versions";
    }

    @GetMapping("/ui/minecraft-versions/version/{version}")
    @Transactional(readOnly = true)
    public String versionDetail(@PathVariable String version, Model model) {
        var minecraftVersion = minecraftVersionDao.getByVersion(version);
        if (minecraftVersion == null) {
            return "error/404";
        }

        model.addAttribute("version", minecraftVersion);

        return "minecraft-version-detail";
    }

    @PostMapping("/ui/minecraft-versions/version/{version}/reimport")
    @Transactional
    public String reimportVersion(@PathVariable String version, RedirectAttributes redirect) {
        minecraftVersionDao.setReimportForVersion(version);
        redirect.addFlashAttribute("successMessage", "Reimport has been flagged");
        return "redirect:/ui/minecraft-versions/version/{version}";
    }
}
