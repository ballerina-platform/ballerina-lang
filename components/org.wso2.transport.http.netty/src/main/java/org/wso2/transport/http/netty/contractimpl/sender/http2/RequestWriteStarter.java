package org.wso2.transport.http.netty.contractimpl.sender.http2;

/**
 * Starts the request content writing.
 */
public class RequestWriteStarter {

    private final OutboundMsgHolder outboundMsgHolder;
    private final Http2ClientChannel http2ClientChannel;

    public RequestWriteStarter(OutboundMsgHolder outboundMsgHolder, Http2ClientChannel http2ClientChannel) {
        this.outboundMsgHolder = outboundMsgHolder;
        this.http2ClientChannel = http2ClientChannel;
    }

    public void startWritingContent() {
        // Write Content
        outboundMsgHolder.setFirstContentWritten(false);
        outboundMsgHolder.getRequest().getHttpContentAsync().setMessageListener((httpContent) -> {
            http2ClientChannel.getChannel().eventLoop().execute(() -> {
                Http2Content http2Content = new Http2Content(httpContent, outboundMsgHolder);
                http2ClientChannel.getChannel().write(http2Content);
            });
        });
    }
}
