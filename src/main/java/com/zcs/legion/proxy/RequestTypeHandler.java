package com.zcs.legion.proxy;

import com.legion.client.common.RequestDescriptor;
import com.legion.client.handlers.RecipientHandler;
import com.zcs.legion.api.A;

/**
 * 服务类型handle
 */
public interface RequestTypeHandler {
    /**
     * 发送请求
     * @param descriptor
     * @param message
     * @param recipientHandler
     */
    void request(RequestDescriptor descriptor, A.BrokerMessage message, RecipientHandler recipientHandler);

}
