package com.zcs.legion.proxy;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.legion.client.api.sample.T;
import com.legion.client.common.ActorProvider;
import com.legion.client.common.IRecipientActor;
import com.legion.client.common.RequestDescriptor;
import com.legion.client.common.TagMatchers;
import com.legion.client.exception.UnsupportedTagException;
import com.legion.client.handlers.RecipientHandler;
import com.legion.core.LegionException;
import com.legion.core.XHelper;
import com.legion.core.api.X;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.asynchttpclient.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
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

        Map<String, AgentProperties.Agent> tagAgents = agentProperties.getAgents();
        if (tagAgents == null) {
            log.warn("tag agents not found.");
            recipientHandler.setFailure(LegionException.valueOf("config failed."));
            return;
        }
        AgentProperties.Agent agent = tagAgents.get(descriptor.getTag());
        if (agent == null) {
            log.warn("tag [{}] not found.", descriptor.getTag());
            recipientHandler.setFailure(
                    LegionException.valueOf(String.format("tag [%s] not found", descriptor.getTag())));
            return;
        }
        String url = agent.getPrefix()+msg.getRequest();
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
                    if (agent.getHeaderWhiteList().contains(h.getKey())) {
                        r.putHeaders(h.getKey(), h.getValue());
                    }
                }
                r.setStatus(response.getStatusCode())
                        .setBody(ByteString.copyFrom(response.getResponseBodyAsByteBuffer()));
                recipientHandler.setSuccess(r.build().toByteString());
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
