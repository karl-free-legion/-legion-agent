package com.zcs.legion.proxy;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "legion.agent")
public class AgentProperties {
    private Map<String, String> tags;
}
