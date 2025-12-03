package org.example.devmarketbackend.global.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Shop API")
                    .version("1.0.0")
                    .description("Shop API 명세서")
            )
            .addSecurityItem(SecurityRequirement().addList("Authorization"))
            .schemaRequirement(
                "Authorization",
                SecurityScheme()
                    .name("Authorization")
                    .bearerFormat("JWT")
                    .scheme("bearer")
                    .type(SecurityScheme.Type.HTTP)
                    .`in`(SecurityScheme.In.HEADER)
                    .description("Access Token을 입력하세요.")
            )
    }

    @Bean
    fun allGroup(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("All")
            .pathsToMatch("/**")
            .build()
    }
}

