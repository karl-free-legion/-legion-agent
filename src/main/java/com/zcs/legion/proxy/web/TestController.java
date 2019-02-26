package com.zcs.legion.proxy.web;


import com.google.protobuf.ByteString;
import com.legion.client.common.LegionConnector;
import com.legion.client.handlers.SenderHandler;
import com.legion.client.handlers.SenderHandlerFactory;
import com.legion.core.api.X;
import com.zcs.legion.proxy.AgentProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;
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

    @Autowired
    private AgentProperties agentProperties;

    @RequestMapping(value = "/tags", method = RequestMethod.GET)
    public Map<String, String> queryTags(){
        return agentProperties.getTags();
    }
    @RequestMapping(value="/tags", method = RequestMethod.POST)
    public Map<String, String> updateTags(@RequestBody Map<String, String> tags){
        if(tags!=null){
            for(String k : tags.keySet()){
                agentProperties.getTags().put(k, tags.get(k));
            }
        }
        return agentProperties.getTags();
    }

    /**
     * 消息转发处理
     * @param groupId   GroupID
     * @param tag       标签
     * @param body      消息体
     * @return          返回结果
     */
    @RequestMapping(value = "/m/{groupId}/{tag}", method = RequestMethod.POST)
    public String  tagProxy(@PathVariable String groupId,
                            @PathVariable String tag, @RequestBody String body,
                            HttpServletRequest request) {
        REQUEST_TOTAL.increment();

        if(log.isDebugEnabled()){
            log.info("===>RequestURI: /m/{}/{}, body: {}", groupId, tag, body);
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
            completableFuture.complete(String.format("response failed, code=%d, msg=%s", fail.getCode(), fail.getMessage()));
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
