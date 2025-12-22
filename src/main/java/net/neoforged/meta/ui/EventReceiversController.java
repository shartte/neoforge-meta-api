package net.neoforged.meta.ui;

import jakarta.persistence.EntityManager;
import net.neoforged.meta.db.event.Event;
import net.neoforged.meta.triggers.EventReceiversService;
import org.hibernate.query.spi.SqmQuery;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.Instant;

@Controller
public class EventReceiversController {
    private final EventReceiversService service;
    private final EntityManager entityManager;

    public EventReceiversController(EventReceiversService service, EntityManager entityManager) {
        this.service = service;
        this.entityManager = entityManager;
    }

    @GetMapping("/ui/event-receivers")
    public String eventReceivers(Model model) {
        model.addAttribute("eventReceivers", service.getSummary());
        return "event-receivers";
    }

    @GetMapping(value = "/ui/event-receivers/receiver/{receiverId}")
    public String eventReceiverDetails(@PathVariable String receiverId, Model model) {
        var receiver = service.getReceiver(receiverId);
        if (receiver == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("receiver", receiver);
        model.addAttribute("state", service.getState(receiverId));

        // Try to provide a select for the events
        var cb = entityManager.getCriteriaBuilder();
        var q = cb.createQuery(Event.class);
        var predicate = receiver.getEventFilter().toPredicate(q.from(Event.class), cb);
        if (predicate != null) {
            q.where(predicate);
        }
        var sqmQuery = entityManager.createQuery(q).unwrap(SqmQuery.class);
        model.addAttribute("eventFilterSql", sqmQuery.getSqmStatement().toHqlString());

        return "event-receiver";
    }

    @PostMapping(value = "/ui/event-receivers/receiver/{receiverId}", params = "action=pause")
    public String pauseReceiver(@PathVariable String receiverId,
                                @RequestParam(required = false) @Nullable Duration pauseFor,
                                @RequestParam(defaultValue = "false") boolean fromDetails,
                                RedirectAttributes redirectAttributes) {

        Instant until = null;
        if (pauseFor != null) {
            until = Instant.now().plus(pauseFor);
        }

        service.pauseReceiver(receiverId, until);

        redirectAttributes.addFlashAttribute("successMessage", "Paused event receiver '" + receiverId + "'");

        if (fromDetails) {
            redirectAttributes.addAttribute("receiverId", receiverId);
            return "redirect:/ui/event-receivers/receiver/{receiverId}";
        } else {
            return "redirect:/ui/event-receivers";
        }
    }

    @PostMapping(value = "/ui/event-receivers/receiver/{receiverId}", params = "action=resume")
    public String resumeReceiver(@PathVariable String receiverId, @RequestParam(defaultValue = "false") boolean fromDetails, RedirectAttributes redirectAttributes) {
        service.resumeReceiver(receiverId);

        redirectAttributes.addFlashAttribute("successMessage", "Resumed event receiver '" + receiverId + "'");

        if (fromDetails) {
            redirectAttributes.addAttribute("receiverId", receiverId);
            return "redirect:/ui/event-receivers/receiver/{receiverId}";
        } else {
            return "redirect:/ui/event-receivers";
        }
    }
}
