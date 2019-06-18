package org.wso2.transport.http.netty.contractimpl.listener.http2;

import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

public class InboundMessageHolder {
    private HttpCarbonMessage inboundMsgOrPushResponse;
    private HttpCarbonResponse response;
    private Http2ServerChannel http2ServerChannel;
    private long lastReadWriteTime;
    private Http2OutboundRespListener http2OutboundRespListener;

    public InboundMessageHolder(HttpCarbonMessage inboundMsgOrPushResponse) {
        this.inboundMsgOrPushResponse = inboundMsgOrPushResponse;
    }

    /**
     * Gets last read or write operation execution time.
     *
     * @return the last read or write operation execution time
     */
    long getLastReadWriteTime() {
        return lastReadWriteTime;
    }

    /**
     * Sets the last read or write operation execution time.
     *
     * @param lastReadWriteTime the last read or write operation execution time
     */
    void setLastReadWriteTime(long lastReadWriteTime) {
        this.lastReadWriteTime = lastReadWriteTime;
    }

    public HttpCarbonMessage getInboundMessage() {
        return inboundMsgOrPushResponse;
    }

    public Http2OutboundRespListener getHttp2OutboundRespListener() {
        return http2OutboundRespListener;
    }

    public void setHttp2OutboundRespListener(
            Http2OutboundRespListener http2OutboundRespListener) {
        this.http2OutboundRespListener = http2OutboundRespListener;
    }
}
