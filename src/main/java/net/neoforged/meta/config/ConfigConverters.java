package net.neoforged.meta.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigConverters {
    @Bean
    @ConfigurationPropertiesBinding
    static VersionRangeConverter versionRangeConverter() {
        return new VersionRangeConverter();
    }
}
