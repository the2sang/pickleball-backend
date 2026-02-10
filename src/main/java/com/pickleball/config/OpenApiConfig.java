package com.pickleball.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot 4.0 API Documentation")
                        .description("Spring Boot 4.0 기반의 프로젝트 API 문서입니다.")
                        .version("4.0.0"));
    }
}
