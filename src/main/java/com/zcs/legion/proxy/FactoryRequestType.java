package com.zcs.legion.proxy;

import com.legion.client.common.RequestDescriptor;
import com.legion.client.handlers.RecipientHandler;
import com.zcs.legion.api.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
@Component
public class FactoryRequestType {
    private Map<String,RequestTypeHandler> map;
    @Autowired
    public FactoryRequestType(Map<String,RequestTypeHandler> map){
        this.map = map;
    }
    /**
     * 发送请求
     * @param descriptor
     * @param message
     * @param recipientHandler
     */
    void request(RequestDescriptor descriptor, A.BrokerMessage message, RecipientHandler recipientHandler){
        map.get(message.getRequestType().toString()).request(descriptor,message,recipientHandler);
    }
}
