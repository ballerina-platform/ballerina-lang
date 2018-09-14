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
package org.wso2.transport.http.netty.listener.states.listener.http2;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.listener.http2.Http2SourceHandler;
import org.wso2.transport.http.netty.listener.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.wso2.transport.http.netty.listener.states.StateUtil.ILLEGAL_STATE_ERROR;

/**
 * State between start and end of payload read
 */
public class ReceivingEntityBody implements ListenerState {

    private static Logger log = LoggerFactory.getLogger(ReceivingEntityBody.class);
    private final Http2SourceHandler http2SourceHandler;
    private final Http2MessageStateContext http2MessageStateContext;

    ReceivingEntityBody(Http2SourceHandler http2SourceHandler, Http2MessageStateContext http2MessageStateContext) {
        this.http2SourceHandler = http2SourceHandler;
        this.http2MessageStateContext = http2MessageStateContext;
    }

    @Override
    public void readInboundRequestHeaders(Http2HeadersFrame headersFrame) {
        log.warn("readInboundRequestHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void readInboundRequestBody(Http2DataFrame dataFrame) {
        int streamId = dataFrame.getStreamId();
        ByteBuf data = dataFrame.getData();
        HttpCarbonMessage sourceReqCMsg = http2SourceHandler.streamIdRequestMap.get(streamId);

        if (sourceReqCMsg != null) {
            if (dataFrame.isEndOfStream()) {
                sourceReqCMsg.addHttpContent(new DefaultLastHttpContent(data));
                http2SourceHandler.streamIdRequestMap.remove(streamId);
                http2MessageStateContext.setListenerState(new EntityBodyReceived(http2MessageStateContext));
            } else {
                sourceReqCMsg.addHttpContent(new DefaultHttpContent(data));
            }
        } else {
            log.warn("Inconsistent state detected : data has received before headers");
        }
    }

    @Override
    public void writeOutboundResponseHeaders(Http2OutboundRespListener.ResponseWriter responseWriter,
                                             HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        log.warn("writeOutboundResponseHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundResponseBody(Http2OutboundRespListener.ResponseWriter responseWriter,
                                          HttpCarbonMessage outboundResponseMsg, HttpContent httpContent)
            throws Http2Exception {
        http2MessageStateContext.setListenerState(new SendingHeaders());
        http2MessageStateContext.getListenerState()
                .writeOutboundResponseHeaders(responseWriter, outboundResponseMsg, httpContent);
    }
}
