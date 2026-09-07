package com.ecocity.esg.adapter.in.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ecoCityOpenApi() {
        return new OpenAPI().info(new Info()
                .title("EcoCity ESG API")
                .description("API de gestão ambiental, social e de governança para cidades inteligentes. Inclui consumo de energia, coleta de resíduos, emissões de carbono, diversidade e licenças ambientais.")
                .version("1.0.0")
                .contact(new Contact().name("Grupo 31").email("juniordomingos1980@gmail.com")));
    }
}
