package org.wso2.transport.http.netty.contractimpl;

import io.netty.handler.codec.http2.Http2RemoteFlowController;
import io.netty.handler.codec.http2.Http2Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;

public final class Http2RemoteFlowControlListener implements Http2RemoteFlowController.Listener {
    private static final Logger LOG = LoggerFactory.getLogger(Http2RemoteFlowControlListener.class);
    private Http2ClientChannel http2ClientChannel;

    public Http2RemoteFlowControlListener(Http2ClientChannel http2ClientChannel) {
        this.http2ClientChannel = http2ClientChannel;
    }

    @Override
    public void writabilityChanged(Http2Stream stream) {
        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(stream.id());
        if (outboundMsgHolder == null) {
            return;
        }
        if (http2ClientChannel.getConnection().remote().flowController().isWritable(stream)) {
            LOG.warn("{} stream {} writable.", Thread.currentThread().getName(), stream.id());
            outboundMsgHolder.setStreamWritable(true);
            outboundMsgHolder.getBackPressureObservable().notifyWritable();
        } else {
            LOG.warn("{} stream {} not writable.", Thread.currentThread().getName(), stream.id());
            outboundMsgHolder.setStreamWritable(false);
        }
    }
}
