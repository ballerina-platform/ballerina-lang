package org.wso2.transport.http.netty.contractimpl.sender.http2;

import io.netty.handler.codec.http.HttpContent;

/**
 * Represents HTTP2 request content.
 */
public class Http2Content {
    private final HttpContent httpContent;
    private final OutboundMsgHolder outboundMsgHolder;

    Http2Content(HttpContent httpContent, OutboundMsgHolder outboundMsgHolder) {
        this.httpContent = httpContent;
        this.outboundMsgHolder = outboundMsgHolder;
    }

    public HttpContent getHttpContent() {
        return httpContent;
    }

    public OutboundMsgHolder getOutboundMsgHolder() {
        return outboundMsgHolder;
    }
}
