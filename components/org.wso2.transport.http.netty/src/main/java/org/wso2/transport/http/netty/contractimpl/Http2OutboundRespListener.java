/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.contractimpl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * Get executed when HTTP/2 response is available.
 */
public class Http2OutboundRespListener implements HttpConnectorListener {

    private ChannelHandlerContext channelHandlerContext;
    private Http2ConnectionEncoder encoder;
    private int streamId;
    private boolean isHeaderWritten = false;

    public Http2OutboundRespListener(ChannelHandlerContext channelHandlerContext, Http2ConnectionEncoder encoder,
                                     int streamId) {
        this.channelHandlerContext = channelHandlerContext;
        this.encoder = encoder;
        this.streamId = streamId;
    }

    @Override
    public void onMessage(HTTPCarbonMessage outboundResponseMsg) {
        channelHandlerContext.channel().eventLoop().execute(() -> {

            outboundResponseMsg.getHttpContentAsync().setMessageListener(
                    httpContent -> channelHandlerContext.channel().eventLoop().execute(() -> {
                        writeOutboundResponse(outboundResponseMsg, httpContent);
                    }));
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }

    private void writeOutboundResponse(HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent) {

        if (!isHeaderWritten) {
            writeHeaders(outboundResponseMsg);
        }

        if (Util.isLastHttpContent(httpContent)) {
            writeData(httpContent, true);
        } else {
            writeData(httpContent, false);
        }
    }

    private void writeHeaders(HTTPCarbonMessage outboundResponseMsg) {
        Http2Headers http2Headers = new DefaultHttp2Headers().status(OK.codeAsText());
        outboundResponseMsg.getHeaders().entries().forEach(
                header -> http2Headers.set(header.getKey().toLowerCase(), header.getValue()));

        isHeaderWritten = true;
        encoder.writeHeaders(channelHandlerContext, streamId, http2Headers, 0, false,
                             channelHandlerContext.newPromise());
        try {
            encoder.flowController().writePendingBytes();
        } catch (Http2Exception e) {
        }
        channelHandlerContext.flush();

    }

    private void writeData(HttpContent httpContent, boolean endStream) {
        encoder.writeData(channelHandlerContext, streamId, httpContent.content().retain(),
                          0, endStream, channelHandlerContext.newPromise());
        try {
            encoder.flowController().writePendingBytes();
        } catch (Http2Exception e) {
        }
        channelHandlerContext.flush();

    }

}
