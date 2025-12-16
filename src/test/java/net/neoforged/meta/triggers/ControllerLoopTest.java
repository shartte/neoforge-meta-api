package net.neoforged.meta.triggers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControllerLoopTest {

    @Test
    @Timeout(value = 10)
    void testTriggerService() {
        ControllerLoop controllerLoop;
        try (var context = createControllerContext()) {
            controllerLoop = context.getBean(ControllerLoop.class);
            assertTrue(controllerLoop.isRunning());
        }
        assertFalse(controllerLoop.isRunning());
    }

    private static ConfigurableApplicationContext createControllerContext() {
        var app = new SpringApplicationBuilder(TriggerConfiguration.class)
                .bannerMode(Banner.Mode.OFF)
                .web(WebApplicationType.NONE)
                .build();
        return app.run();
    }
}