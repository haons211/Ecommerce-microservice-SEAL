package com.haons.microservice.gatewayservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConditionalOnProperty(name = "security.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "security")
public class SecurityProps {
    private String[] skipUrls;
}
