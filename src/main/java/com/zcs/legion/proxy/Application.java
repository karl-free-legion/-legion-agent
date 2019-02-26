package com.zcs.legion.proxy;

import com.google.protobuf.ByteString;
import com.legion.client.api.sample.T;
import com.legion.client.common.LegionConnector;
import com.legion.client.handlers.SenderHandler;
import com.legion.client.handlers.SenderHandlerFactory;
import com.legion.core.api.X;
import com.legion.net.NetConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

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
