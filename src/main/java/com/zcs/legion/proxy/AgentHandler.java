package com.zcs.legion.proxy;

import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import com.legion.client.common.IRecipientActor;
import com.legion.client.common.RequestDescriptor;
import com.legion.client.common.annotation.ActorProvider;
import com.legion.client.handlers.RecipientHandler;
import com.legion.core.TagMatchers;
import com.legion.core.exception.LegionException;
import com.zcs.legion.api.A;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 升级代理
 * @author lance
 * 2019.3.22 15:46
 */
@Slf4j
@Component
public class AgentHandler implements IRecipientActor<A.BrokerMessage> {
    private AsyncHttpClient httpClient = Dsl.asyncHttpClient();
    @Autowired
    private AgentProperties agentProperties;

    @Override
    @ActorProvider(matcher = TagMatchers.Any, name="agent_handler", model = A.BrokerMessage.class)
    public void reactMessage(RequestDescriptor descriptor, A.BrokerMessage message, RecipientHandler recipientHandler) {

        Map<String, String> tagAgents = agentProperties.getTags();
        if (tagAgents == null) {
            log.warn("tag agents not found.");
            recipientHandler.setFailure(LegionException.valueOf("config failed."));
            return;
        }
        String agentPrefix = tagAgents.get(descriptor.getTag());
        if (agentPrefix == null) {
            log.warn("tag [{}] not found.", descriptor.getTag());
            recipientHandler.setFailure(
                    LegionException.valueOf(String.format("tag [%s] not found", descriptor.getTag())));
            return;
        }
        String url = agentPrefix + descriptor.getRequest().getRequestURI();
        log.debug("prepare post to url: {}", url);
        BoundRequestBuilder postBuilder = httpClient.preparePost(url);
        //http headers
        Map<String, String> headers = descriptor.getRequest().getHeadersMap();
        for (String hk : headers.keySet()) {
            postBuilder.addHeader(hk, headers.get(hk));
        }
        //http body
        postBuilder.setBody(message.getBody());
        postBuilder.execute(new AgentHttpHandler() {
            @Override
            public String onCompleted(Response response) throws Exception {
                log.debug("http response: {}", response);
                Map<String, String> r = Maps.newHashMap();
                for (Map.Entry<String, String> h : response.getHeaders()) {
                    r.put(h.getKey(), h.getValue());
                }

                descriptor.setStatus(r);
                A.BrokerMessage.Builder reply = A.BrokerMessage.newBuilder()
                        .setCode(response.getStatusCode())
                        .setBody(response.getResponseBody());
                recipientHandler.setSuccess(reply.build(), descriptor);

                if(log.isDebugEnabled()){
                    log.debug("http response to reply message: {}", reply);
                }
                return response.getStatusText();
            }
            @Override
            public void onFailed(Throwable cause) {
                recipientHandler.setFailure(LegionException.valueOf(cause));
                log.warn("http response exception: ", cause);
            }
        });
    }
}
