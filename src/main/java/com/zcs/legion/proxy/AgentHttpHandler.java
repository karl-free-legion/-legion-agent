package com.zcs.legion.proxy;

import org.asynchttpclient.AsyncCompletionHandler;

public abstract class AgentHttpHandler extends AsyncCompletionHandler<String> {
    public abstract void onFailed(Throwable cause);

    @Override
    public void onThrowable(Throwable t) {
        super.onThrowable(t);
        onFailed(t);
    }
}
