package com.freightfox.githubsearcher.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FreightFox - GitHub Repository Searcher API")
                        .description("RESTful microservice to search repositories using GitHub REST API, persist with upsert semantics, and retrieve with dynamic filtering and sorting.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Onkar Kandekar")
                                .email("onkarkandekar05@gmail.com")
                                .url("https://linkedin.com/in/Onkar-Kandekar"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .servers(List.of(
                        new Server().url("/").description("Current Server Context")
                ));
    }
}
