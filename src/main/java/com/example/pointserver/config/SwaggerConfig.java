package com.example.pointserver.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "포인트서버 API 명세서",
                description = "포인트서버 API 명세서",
                version = "v1"))
@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi pointOpenApi() {
        String[] paths = {"/point/v1/**"};

        return GroupedOpenApi.builder()
                .group("포인트서버 API v1")
                .pathsToMatch(paths)
                .build();
    }
}
