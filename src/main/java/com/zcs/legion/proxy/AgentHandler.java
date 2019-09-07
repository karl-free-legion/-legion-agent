package com.zcs.legion.proxy;

import com.legion.client.common.IRecipientActor;
import com.legion.client.common.RequestDescriptor;
import com.legion.client.common.annotation.ActorProvider;
import com.legion.client.handlers.RecipientHandler;
import com.legion.core.TagMatchers;
import com.zcs.legion.api.A;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 升级代理
 * @author lance
 * 2019.3.22 15:46
 */
@Slf4j
@Component
public class AgentHandler implements IRecipientActor<A.BrokerMessage> {
    @Autowired
    private FactoryRequestType factoryRequestType;
    @Override
    @ActorProvider(matcher = TagMatchers.Any, name="agent_handler", model = A.BrokerMessage.class)
    public void reactMessage(RequestDescriptor descriptor, A.BrokerMessage message, RecipientHandler recipientHandler) {
        factoryRequestType.request(descriptor,message,recipientHandler);
    }
}
