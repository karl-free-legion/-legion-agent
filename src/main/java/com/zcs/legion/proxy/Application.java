package com.zcs.legion.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

/**
 * Legion-Gateway
 * @author lance
 * @since 2019.2.23 16:41
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"com.zcs.legion.proxy", "com.legion.client"})
public class Application {

    @Autowired
    private AgentProperties agentProperties;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void init(){
        log.info("agent config: {}", agentProperties);
    }
}
