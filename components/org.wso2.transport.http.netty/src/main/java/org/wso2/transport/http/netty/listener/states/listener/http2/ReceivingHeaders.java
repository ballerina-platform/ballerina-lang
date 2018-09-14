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

import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener.ResponseWriter;
import org.wso2.transport.http.netty.listener.http2.Http2SourceHandler;
import org.wso2.transport.http.netty.listener.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * State between start and end of inbound request headers read.
 */
public class ReceivingHeaders implements ListenerState {

    private static final Logger LOG = LoggerFactory.getLogger(ReceivingHeaders.class);

    private final Http2SourceHandler http2SourceHandler;
    private final Http2MessageStateContext http2MessageStateContext;

    public ReceivingHeaders(Http2SourceHandler http2SourceHandler, Http2MessageStateContext http2MessageStateContext) {
        this.http2SourceHandler = http2SourceHandler;
        this.http2MessageStateContext = http2MessageStateContext;
    }

    @Override
    public void readInboundRequestHeaders(Http2HeadersFrame headersFrame) throws Http2Exception {
        int streamId = headersFrame.getStreamId();
        if (headersFrame.isEndOfStream()) {
            // Retrieve HTTP request and add last http content with trailer headers.
            HttpCarbonMessage sourceReqCMsg = http2SourceHandler.streamIdRequestMap.get(streamId);
            if (sourceReqCMsg != null) {
                http2SourceHandler.readTrailerHeaders(streamId, headersFrame.getHeaders(), sourceReqCMsg);
                http2SourceHandler.streamIdRequestMap.remove(streamId);
            } else if (headersFrame.getHeaders().contains(Constants.HTTP2_METHOD)) {
                // if the header frame is an initial header frame and also it has endOfStream
                sourceReqCMsg = http2SourceHandler.setupHttp2CarbonMsg(headersFrame.getHeaders(), streamId);
                // Add empty last http content if no data frames available in the http request
                sourceReqCMsg.addHttpContent(new DefaultLastHttpContent());
                http2SourceHandler.notifyRequestListener(sourceReqCMsg, streamId);
            }
        } else {
            // Construct new HTTP Request
            HttpCarbonMessage sourceReqCMsg =
                    http2SourceHandler.setupHttp2CarbonMsg(headersFrame.getHeaders(), streamId);
            sourceReqCMsg.setHttp2MessageStateContext(http2MessageStateContext);
            http2SourceHandler.streamIdRequestMap.put(streamId, sourceReqCMsg);   // storing to add HttpContent later
            http2SourceHandler.notifyRequestListener(sourceReqCMsg, streamId);
        }
    }

    @Override
    public void readInboundRequestBody(Http2DataFrame dataFrame) throws ServerConnectorException {
        http2MessageStateContext.setListenerState(
                new ReceivingEntityBody(http2SourceHandler, http2MessageStateContext));
        http2MessageStateContext.getListenerState().readInboundRequestBody(dataFrame);
    }

    @Override
    public void writeOutboundResponseHeaders(ResponseWriter responseWriter, HttpCarbonMessage outboundResponseMsg,
                                             HttpContent httpContent) {
        LOG.warn("writeOutboundResponseHeaders is not a dependant action of this state");
    }

    @Override
    public void writeOutboundResponseBody(ResponseWriter responseWriter, HttpCarbonMessage outboundResponseMsg,
                                          HttpContent httpContent) throws Http2Exception {
        // When receiving headers, if payload is not consumed by the server, this method is invoked if server is
        // going to send the response back.
        http2MessageStateContext.setListenerState(new SendingHeaders());
        http2MessageStateContext.getListenerState()
                .writeOutboundResponseHeaders(responseWriter, outboundResponseMsg, httpContent);
    }
}
