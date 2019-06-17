package org.wso2.transport.http.netty.contractimpl.listener.http2;

import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

public class InboundMessageHolder {
    private HttpCarbonMessage inboundMsg;
    private HttpCarbonResponse response;
    private Http2ServerChannel http2ServerChannel;
    private long lastReadWriteTime;

    public InboundMessageHolder(HttpCarbonMessage inboundMsg) {
        this.inboundMsg = inboundMsg;
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
        return inboundMsg;
    }
}
