package com.zcs.legion.proxy;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "legion")
public class AgentProperties {
    private Map<String, Agent> agents;
    @Data
    public static class Agent{
        private List<String> headerWhiteList;
        private String prefix;
    }
}
