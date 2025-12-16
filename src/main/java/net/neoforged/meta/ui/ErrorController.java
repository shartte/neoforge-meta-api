package net.neoforged.meta.ui;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webmvc.autoconfigure.error.BasicErrorController;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;

/**
 * This subclass only exists so that {@link UiController} contributes its model attributes.
 */
@Controller
@ConditionalOnWebApplication
public class ErrorController extends BasicErrorController {
    public ErrorController(ErrorAttributes errorAttributes, WebProperties webProperties) {
        super(errorAttributes, webProperties.getError());
    }
}
