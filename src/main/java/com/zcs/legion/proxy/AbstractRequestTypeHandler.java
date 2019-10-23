package com.zcs.legion.proxy;


import com.google.common.collect.Maps;
import com.legion.client.common.RequestDescriptor;
import com.legion.client.handlers.RecipientHandler;
import com.legion.core.exception.LegionException;
import com.zcs.legion.api.A;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;

import java.util.Map;
@Slf4j
public abstract class AbstractRequestTypeHandler implements RequestTypeHandler {
    static AsyncHttpClient httpClient = Dsl.asyncHttpClient();
    /**
     * 处理请求结果
     * @param descriptor
     * @param message
     * @param recipientHandler
     */
    static void dealPostResult(String url,RequestDescriptor descriptor, A.BrokerMessage message, RecipientHandler recipientHandler){
        BoundRequestBuilder postBuilder = httpClient.preparePost(url);
        //http headers
        Map<String, String> headers = descriptor.getRequest().getHeadersMap();
        for (String hk : headers.keySet()) {
            postBuilder.addHeader(hk, headers.get(hk));
        }
        //http body
        postBuilder.setBody(message.getBody());
        dealResult(postBuilder,descriptor,recipientHandler);
    }
    /**
     * 处理请求结果
     * @param descriptor
     * @param recipientHandler
     */
    static void dealGetResult(String url,RequestDescriptor descriptor, RecipientHandler recipientHandler){
        BoundRequestBuilder getBuilder = httpClient.prepareGet(url);
        //http headers
        Map<String, String> headers = descriptor.getRequest().getHeadersMap();
        for (String hk : headers.keySet()) {
            getBuilder.addHeader(hk, headers.get(hk));
        }
        dealResult(getBuilder,descriptor,recipientHandler);
    }

    /**
     * 处理返回结果
     * @param builder
     * @param descriptor
     * @param recipientHandler
     */
    static void dealResult(BoundRequestBuilder builder,RequestDescriptor descriptor, RecipientHandler recipientHandler){
        //http body
        builder.execute(new AgentHttpHandler() {
            @Override
            public String onCompleted(Response response) throws Exception {
                log.debug("http response: {}", response);
                Map<String, String> r = Maps.newHashMap();
                for (Map.Entry<String, String> h : response.getHeaders()) {
                    r.put(h.getKey(), h.getValue());
                    descriptor.putExtensionParam(h.getKey(), h.getValue());
                }

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
