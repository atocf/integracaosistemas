package br.com.atocf.pedido.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Integração entre sistemas de pedidos")
                        .description("Sistema que receba um arquivo desnormalizado via API REST e emcaminha o mesmo para normalização")
                        .version("1.0"));
    }
}