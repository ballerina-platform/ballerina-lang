/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.transport.http.netty.message;

import io.netty.handler.codec.http2.Http2RemoteFlowController;
import io.netty.handler.codec.http2.Http2Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;

/**
 * Http/2 remote flow control listener for client. Each HTTP/2 connection has a single remote flow control listener.
 */
public final class ClientRemoteFlowControlListener implements Http2RemoteFlowController.Listener {
    private static final Logger LOG = LoggerFactory.getLogger(ClientRemoteFlowControlListener.class);
    private Http2ClientChannel http2ClientChannel;
    private Http2RemoteFlowController http2RemoteFlowController;

    public ClientRemoteFlowControlListener(Http2ClientChannel http2ClientChannel) {
        this.http2ClientChannel = http2ClientChannel;
        this.http2RemoteFlowController = http2ClientChannel.getConnection().remote().flowController();
    }

    @Override
    public void writabilityChanged(Http2Stream stream) {
        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(stream.id());
        //By the time the writability changed event is triggered for closed stream, it might have already been
        //removed from the h2 client channel map hence nullability of outboundMsgHolder must be handled here.
        if (outboundMsgHolder == null) {
            return;
        }
        //Netty flow controller methods should only be called from an I/O thread.
        if (http2RemoteFlowController.isWritable(stream)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("In thread {}. Stream {} is writable. State {} ", Thread.currentThread().getName(),
                          stream.id(), stream.state());
            }
            outboundMsgHolder.setStreamWritable(true);
            outboundMsgHolder.getBackPressureObservable().notifyWritable();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("In thread {}. Stream {} is not writable. State {} ", Thread.currentThread().getName(),
                          stream.id(), stream.state());
            }
            outboundMsgHolder.setStreamWritable(false);
        }
    }
}
