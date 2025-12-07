package com.basilios.basilios.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // Use originPatterns quando allowCredentials=true (o Spring responde com o origin exato)
                        .allowedOriginPatterns(
                                "http://localhost:5173",
                                "http://127.0.0.1:5173",
                                "http://localhost:3000"
                        )
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin")
                        .exposedHeaders("Authorization")
                        .allowCredentials(true);
            }
        };
    }
}
