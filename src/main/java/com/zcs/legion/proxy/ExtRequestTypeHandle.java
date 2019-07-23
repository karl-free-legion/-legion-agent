package com.zcs.legion.proxy;

import com.legion.client.common.RequestDescriptor;
import com.legion.client.handlers.RecipientHandler;
import com.zcs.legion.api.A;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("EXT")
@Slf4j
public class ExtRequestTypeHandle extends AbstractRequestTypeHandler {
    static Map<String,SendMessageHandle> messageHandleMap;
    static {
        messageHandleMap = new HashMap<>();
        messageHandleMap.put(HttpMethod.POST.toString(),new PostSendMessageHandle());
        messageHandleMap.put(HttpMethod.GET.toString(),new GetSendMessageHandle());
    }
    /**
     * 发送请求
     * @param descriptor
     * @param message
     * @param recipientHandler
     */
    @Override
    public void request(RequestDescriptor descriptor, A.BrokerMessage message, RecipientHandler recipientHandler) {
        messageHandleMap.get(message.getHttpMethod()).sendMessage(descriptor,message,recipientHandler);
    }

    /**
     * 发送消息的handle
     */
    interface SendMessageHandle{
        /**
         * 发送消息
         * @param descriptor
         * @param message
         * @param recipientHandler
         */
        void sendMessage(RequestDescriptor descriptor, A.BrokerMessage message, RecipientHandler recipientHandler);
    }

    /**
     * 通过post 发送消息的handle
     */
    static class PostSendMessageHandle implements SendMessageHandle{
        @Override
        public void sendMessage(RequestDescriptor descriptor, A.BrokerMessage message, RecipientHandler recipientHandler) {
            dealPostResult(message.getRequestUrl(),descriptor,message,recipientHandler);
        }
    }

    /**
     * 通过get 发送消息的handle
     */
    static class GetSendMessageHandle implements SendMessageHandle{
        @Override
        public void sendMessage(RequestDescriptor descriptor, A.BrokerMessage message, RecipientHandler recipientHandler) {
            dealGetResult(message.getRequestUrl(),descriptor,recipientHandler);
        }
    }
}
