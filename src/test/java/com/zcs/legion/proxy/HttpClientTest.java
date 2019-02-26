package com.zcs.legion.proxy;

import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class HttpClientTest {

    @Test
    @Ignore
    public void testPost() throws Exception{
        CountDownLatch latch = new CountDownLatch(1);
        String prefix = "http://172.18.90.118:61614/tsm";
        Dsl.asyncHttpClient().preparePost(prefix+"/sdk/user/msglog")
                .setHeader("myHeader", "hello, world!")
                .execute(new AgentHttpHandler() {
                    @Override
                    public void onFailed(Throwable cause) {
                        log.info("http failed. ", cause);
                        latch.countDown();
                    }

                    @Override
                    public String onCompleted(Response response) throws Exception {
                        log.info("http success.response:{}", response);
                        latch.countDown();
                        return null;
                    }
                });
        latch.await();
    }
}
