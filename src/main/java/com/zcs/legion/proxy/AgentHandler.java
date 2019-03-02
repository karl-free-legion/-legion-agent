package com.zcs.legion.proxy;

import com.google.protobuf.ByteString;
import com.legion.client.common.ActorProvider;
import com.legion.client.common.IRecipientActor;
import com.legion.client.common.RequestDescriptor;
import com.legion.client.common.TagMatchers;
import com.legion.client.handlers.RecipientHandler;
import com.legion.core.api.X;
import com.legion.core.exception.LegionException;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class AgentHandler implements IRecipientActor<X.XAgentRequest> {
    AsyncHttpClient httpClient = Dsl.asyncHttpClient();
    @Autowired
    private AgentProperties agentProperties;
    @Override
    @ActorProvider(matcher = TagMatchers.Any, name="agent_handler", model = X.XAgentRequest.class)
    public void reactMessage(RequestDescriptor descriptor, X.XAgentRequest msg, RecipientHandler recipientHandler) {

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
        String url = agentPrefix + msg.getRequest();
        log.debug("prepare post to url: {}", url);
        BoundRequestBuilder postBuilder = httpClient.preparePost(url);
        //http headers
        Map<String, String> headers = msg.getHeadersMap();
        for (String hk : headers.keySet()) {
            postBuilder.addHeader(hk, headers.get(hk));
        }
        //http body
        postBuilder.setBody(msg.getBody().toByteArray());
        //post
        postBuilder.execute(new AgentHttpHandler() {
            @Override
            public String onCompleted(Response response) throws Exception {
                log.debug("http response: {}", response);
                X.XAgentResponse.Builder r = X.XAgentResponse.newBuilder();
                for (Map.Entry<String, String> h : response.getHeaders()) {
                    r.putHeaders(h.getKey(), h.getValue());
                }
                r.setStatus(response.getStatusCode())
                        .setBody(ByteString.copyFrom(response.getResponseBodyAsByteBuffer()));
                recipientHandler.setSuccess(r.build());
                log.debug("http response to reply message: {}", r);
                return response.getStatusText();
            }
            @Override
            public void onFailed(Throwable cause) {
                recipientHandler.setFailure(LegionException.valueOf(cause));
                log.debug("http response exception: ", cause);
            }
        });
    }
}
