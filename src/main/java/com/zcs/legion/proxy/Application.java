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

    @Getter
    @Autowired
    private LegionConnector connector;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void init(){
        log.info("agent config: {}", agentProperties);
    }

    @Bean
    public CommandLineRunner runner(){
        return args ->
                Executors.newSingleThreadScheduledExecutor().schedule(()->{
                    String tag = "query";
                    X.XAgentRequest request = X.XAgentRequest.newBuilder()
                            .setRequest("/sdk/user/msglog")
                            .putHeaders("Content-Type", "application/json")
                            .putHeaders("X-APP-AUTHORIZATION", "test auth")
                            .putHeaders("X-APP-SIGN", "sign....")
                            .setBody(ByteString.copyFromUtf8("{\"hello\":1}"))
                            .build();
                    CompletableFuture<String> completableFuture = new CompletableFuture<>();
                    SenderHandler<X.XAgentResponse> handler = SenderHandlerFactory.create(success->{
                        //handler success
                        log.info("response success: {}", success);
                    }, fail->{
                        //handler
                        log.info("response failed. {}, {}", fail.getCode(), fail.getMessage());
                    });

                    log.info("===> start message.");
                    connector.sendMessage(tag, request, handler, X.XAgentResponse.class);
                    try {
                        String m = completableFuture.get(5, TimeUnit.SECONDS);
                        log.info("completable future completed. m={}", m);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                }, 5, TimeUnit.SECONDS);
    }
}
