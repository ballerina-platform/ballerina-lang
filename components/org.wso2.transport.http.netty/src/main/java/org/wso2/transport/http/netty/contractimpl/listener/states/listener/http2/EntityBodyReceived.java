/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.contractimpl.listener.states.listener.http2;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.listener.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.wso2.transport.http.netty.contractimpl.listener.states.Http2StateUtil.writeHttp2Promise;

/**
 * State between end of inbound request payload read and start of outbound response or push response headers write.
 */
public class EntityBodyReceived implements ListenerState {

    private static final Logger LOG = LoggerFactory.getLogger(EntityBodyReceived.class);

    private final Http2MessageStateContext http2MessageStateContext;

    public EntityBodyReceived(Http2MessageStateContext http2MessageStateContext) {
        this.http2MessageStateContext = http2MessageStateContext;
    }

    @Override
    public void readInboundRequestHeaders(Http2HeadersFrame headersFrame) {
        LOG.warn("readInboundRequestHeaders is not a dependant action of this state");
    }

    @Override
    public void readInboundRequestBody(Http2DataFrame dataFrame) {
        LOG.warn("readInboundRequestBody is not a dependant action of this state");
    }

    @Override
    public void writeOutboundResponseHeaders(Http2OutboundRespListener http2OutboundRespListener,
                                             HttpCarbonMessage outboundResponseMsg, HttpContent httpContent,
                                             int streamId) {
        LOG.warn("writeOutboundResponseHeaders is not a dependant action of this state");
    }

    @Override
    public void writeOutboundResponseBody(Http2OutboundRespListener http2OutboundRespListener,
                                          HttpCarbonMessage outboundResponseMsg, HttpContent httpContent, int streamId)
            throws Http2Exception {
        // When the initial frames of the response is to be sent.
        http2MessageStateContext.setListenerState(
                new SendingHeaders(http2OutboundRespListener, http2MessageStateContext));
        http2MessageStateContext.getListenerState()
                .writeOutboundResponseHeaders(http2OutboundRespListener, outboundResponseMsg, httpContent, streamId);
    }

    @Override
    public void writeOutboundPromise(Http2OutboundRespListener http2OutboundRespListener,
                                     Http2PushPromise pushPromise) throws Http2Exception {
        writeHttp2Promise(pushPromise, http2OutboundRespListener.getChannelHandlerContext(),
                http2OutboundRespListener.getConnection(), http2OutboundRespListener.getEncoder(),
                http2OutboundRespListener.getInboundRequestMsg(),
                http2OutboundRespListener.getInboundRequestMsg().getHttpOutboundRespStatusFuture(),
                http2OutboundRespListener.getOriginalStreamId());
    }
}
