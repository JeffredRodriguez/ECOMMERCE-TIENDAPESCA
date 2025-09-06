package com.tiendapesca.APItiendapesca.Security;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import java.util.Properties;

@Configuration
public class EnvConfig {

    private final ConfigurableEnvironment environment;

    // Cambiar de Environment a ConfigurableEnvironment
    public EnvConfig(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Bean
    public Dotenv dotenv() {
        return Dotenv.configure()
                .ignoreIfMissing()
                .load();
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        Dotenv dotenv = dotenv();
        Properties properties = new Properties();

        // Cargar todas las variables del .env a las propiedades de Spring
        dotenv.entries().forEach(entry -> {
            properties.put(entry.getKey(), entry.getValue());
        });

        // Agregar las propiedades al entorno de Spring
        environment.getPropertySources().addFirst(
            new PropertiesPropertySource("dotenvProperties", properties)
        );
    }
}