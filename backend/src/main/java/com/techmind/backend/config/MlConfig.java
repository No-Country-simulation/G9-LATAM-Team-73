package com.techmind.backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({MlProperties.class, OciLanguageProperties.class})
public class MlConfig {
}
