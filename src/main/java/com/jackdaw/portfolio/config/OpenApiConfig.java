package com.jackdaw.portfolio.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info =
                @Info(
                        title = "Portfolio API",
                        version = "1.0",
                        description =
                                "Web endpoints of Pratham Thapa Magar's portfolio site: "
                                        + "the server-rendered homepage and the public contact-form submission.",
                        contact =
                                @Contact(
                                        name = "Pratham Thapa Magar",
                                        email = "johnthapa13@gmail.com")))
@Configuration
public class OpenApiConfig {}
