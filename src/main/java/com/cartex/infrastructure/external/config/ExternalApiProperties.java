package com.cartex.infrastructure.external.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "external.api")
public class ExternalApiProperties {

    private String authUrl;
    private String reportsBaseUrl;
}
