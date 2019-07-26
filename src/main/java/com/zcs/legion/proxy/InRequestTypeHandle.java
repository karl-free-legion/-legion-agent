package com.zcs.legion.proxy;

import com.legion.client.common.RequestDescriptor;
import com.legion.client.handlers.RecipientHandler;
import com.legion.core.exception.LegionException;
import com.zcs.legion.api.A;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("IN")
@Slf4j
public class InRequestTypeHandle extends AbstractRequestTypeHandler {
    @Autowired
    private AgentProperties agentProperties;
    @Override
    public void request(RequestDescriptor descriptor, A.BrokerMessage message, RecipientHandler recipientHandler) {

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
        dealPostResult(url,descriptor,message,recipientHandler);
    }
}
