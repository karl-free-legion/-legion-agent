package com.zcs.legion.proxy.web;


import com.google.protobuf.ByteString;
import com.legion.client.common.LegionConnector;
import com.legion.client.handlers.SenderHandler;
import com.legion.client.handlers.SenderHandlerFactory;
import com.legion.core.api.X;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
public class TestController {
    private final Counter REQUEST_TOTAL = Metrics.counter(" http_req_total", "Legion-Gateway", "reg_node_total");
    @Autowired
    private LegionConnector legionConnector;

    /**
     * 消息转发处理
     * @param type      消息类型, M/P
     * @param groupId   GroupID
     * @param tag       标签
     * @param body      消息体
     * @return          返回结果
     */
    @RequestMapping(value = "/{type}/{groupId}/{tag}", method = RequestMethod.POST)
    public String  dispatch(@PathVariable String type, @PathVariable String groupId,
                            @PathVariable String tag, @RequestBody String body,
                            HttpServletRequest request) {
        REQUEST_TOTAL.increment();

        if(log.isDebugEnabled()){
            log.info("===>RequestURI: {}/{}/{}, body: {}", type, groupId, tag, body);
        }
        X.XAgentRequest.Builder agentRequest = X.XAgentRequest.newBuilder()
                .setRequest("/sdk/user/msglog");
        Enumeration<String>  headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String headerName = headerNames.nextElement();
            agentRequest.putHeaders(headerName, request.getHeader(headerName));
        }
        agentRequest.setBody(ByteString.copyFromUtf8(body));

        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        SenderHandler<X.XAgentResponse> handler = SenderHandlerFactory.create(success->{
            //handler success
            log.info("response success: {}", success);
            completableFuture.complete(success.toString());
        }, fail->{
            //handler
            log.info("response failed. {}, {}", fail.getCode(), fail.getMessage());
            completableFuture.complete(String.format("response failed, code={}, msg={}", fail.getCode(), fail.getMessage()));
        });

        log.info("===> start message.");
        legionConnector.sendMessage(tag, agentRequest.build(), handler, X.XAgentResponse.class);
        try {
            String m = completableFuture.get(1, TimeUnit.MINUTES);
            log.info("completable future completed. m={}", m);
            return m;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return "timeout";
    }
}
