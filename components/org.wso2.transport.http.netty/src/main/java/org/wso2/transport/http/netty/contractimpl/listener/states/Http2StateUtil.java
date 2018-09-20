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

package org.wso2.transport.http.netty.contractimpl.listener.states;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.listener.http2.Http2SourceHandler;
import org.wso2.transport.http.netty.message.DefaultListener;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonRequest;
import org.wso2.transport.http.netty.message.PooledDataStreamerFactory;

import java.net.InetSocketAddress;

import static org.wso2.transport.http.netty.contract.Constants.PROMISED_STREAM_REJECTED_ERROR;

/**
 * HTTP/2 utility functions for states.
 */
public class Http2StateUtil {

    private static final Logger LOG = LoggerFactory.getLogger(Http2StateUtil.class);

    /**
     * Notifies the registered listeners which listen for the incoming carbon messages.
     *
     * @param http2SourceHandler the instance which handle the incoming request
     * @param httpRequestMsg     the http request message
     * @param streamId           the id of the stream
     */
    public static void notifyRequestListener(Http2SourceHandler http2SourceHandler, HttpCarbonMessage httpRequestMsg,
                                             int streamId) {
        if (http2SourceHandler.getServerConnectorFuture() != null) {
            try {
                ServerConnectorFuture outboundRespFuture = httpRequestMsg.getHttpResponseFuture();
                outboundRespFuture.setHttpConnectorListener(new Http2OutboundRespListener(
                        http2SourceHandler.getServerChannelInitializer(), httpRequestMsg,
                        http2SourceHandler.getChannelHandlerContext(), http2SourceHandler.getConnection(),
                        http2SourceHandler.getEncoder(), streamId, http2SourceHandler.getServerName(),
                        http2SourceHandler.getRemoteAddress()));
                http2SourceHandler.getServerConnectorFuture().notifyHttpListener(httpRequestMsg);
            } catch (Exception e) {
                LOG.error("Error while notifying listeners", e);
            }
        } else {
            LOG.error("Cannot find registered listener to forward the message");
        }
    }

    /**
     * Creates a {@code HttpCarbonRequest} from HttpRequest.
     *
     * @param httpRequest the HTTPRequest message
     * @param ctx         the channel handler context
     * @param interfaceId listener interface id
     * @return the CarbonRequest Message created from given HttpRequest
     */
    public static HttpCarbonRequest setupCarbonRequest(HttpRequest httpRequest, ChannelHandlerContext ctx,
                                                       String interfaceId) {
        HttpCarbonRequest sourceReqCMsg = new HttpCarbonRequest(httpRequest, new DefaultListener(ctx));
        sourceReqCMsg.setProperty(Constants.POOLED_BYTE_BUFFER_FACTORY, new PooledDataStreamerFactory(ctx.alloc()));
        sourceReqCMsg.setProperty(Constants.CHNL_HNDLR_CTX, ctx);
        HttpVersion protocolVersion = httpRequest.protocolVersion();
        sourceReqCMsg.setProperty(Constants.HTTP_VERSION,
                protocolVersion.majorVersion() + "." + protocolVersion.minorVersion());
        sourceReqCMsg.setProperty(Constants.HTTP_METHOD, httpRequest.method().name());

        InetSocketAddress localAddress = null;
        //This check was added because in case of netty embedded channel, this could be of type 'EmbeddedSocketAddress'.
        if (ctx.channel().localAddress() instanceof InetSocketAddress) {
            localAddress = (InetSocketAddress) ctx.channel().localAddress();
        }
        sourceReqCMsg.setProperty(Constants.LOCAL_ADDRESS, localAddress);
        sourceReqCMsg.setProperty(Constants.LISTENER_PORT, localAddress != null ? localAddress.getPort() : null);
        sourceReqCMsg.setProperty(Constants.LISTENER_INTERFACE_ID, interfaceId);
        sourceReqCMsg.setProperty(Constants.PROTOCOL, Constants.HTTP_SCHEME);
        String uri = httpRequest.uri();
        sourceReqCMsg.setProperty(Constants.REQUEST_URL, uri);
        sourceReqCMsg.setProperty(Constants.TO, uri);
        return sourceReqCMsg;
    }

    /**
     * Write HTTP2 headers to outbound response.
     *
     * @param ctx                      the channel handler context
     * @param encoder                  the HTTP2 connection encoder
     * @param outboundRespStatusFuture the future of outbound response write operation
     * @param streamId                 the id of the stream
     * @param http2Headers             the Http2Headers received over a HTTP/2 stream
     * @param endStream                is this the end of stream
     * @throws Http2Exception throws if a protocol-related error occurred
     */
    public static void writeHttp2Headers(ChannelHandlerContext ctx, Http2ConnectionEncoder encoder,
                                         HttpResponseFuture outboundRespStatusFuture, int streamId,
                                         Http2Headers http2Headers, boolean endStream) throws Http2Exception {
        ChannelFuture channelFuture = encoder.writeHeaders(
                ctx, streamId, http2Headers, 0, endStream, ctx.newPromise());
        encoder.flowController().writePendingBytes();
        ctx.flush();
        Util.addResponseWriteFailureListener(outboundRespStatusFuture, channelFuture);
    }

    /**
     * Write HTTP2 promise.
     *
     * @param pushPromise              HTTP/2 promise message
     * @param ctx                      the channel handler context
     * @param conn                     HTTP2 connection
     * @param encoder                  the HTTP2 connection encoder
     * @param inboundRequestMsg        request message received from the client
     * @param outboundRespStatusFuture the future of outbound response write operation
     * @param originalStreamId         the original id of the stream
     * @throws Http2Exception throws if a protocol-related error occurred
     */
    public static void writeHttp2Promise(Http2PushPromise pushPromise, ChannelHandlerContext ctx, Http2Connection conn,
                                         Http2ConnectionEncoder encoder, HttpCarbonMessage inboundRequestMsg,
                                         HttpResponseFuture outboundRespStatusFuture, int originalStreamId)
            throws Http2Exception {
        int promisedStreamId = getNextStreamId(conn);
        // Update streamIds
        pushPromise.setPromisedStreamId(promisedStreamId);
        pushPromise.setStreamId(originalStreamId);
        // Construct http request
        HttpRequest httpRequest = pushPromise.getHttpRequest();
        httpRequest.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), Constants.HTTP_SCHEME);
        // A push promise is a server initiated request, hence it should contain request headers
        Http2Headers http2Headers =
                HttpConversionUtil.toHttp2Headers(httpRequest, true);
        // Write the push promise to the wire
        ChannelFuture channelFuture = encoder.writePushPromise(
                ctx, originalStreamId, promisedStreamId, http2Headers, 0, ctx.newPromise());
        encoder.flowController().writePendingBytes();
        ctx.flush();
        Util.checkForResponseWriteStatus(inboundRequestMsg, outboundRespStatusFuture, channelFuture);
    }

    private static synchronized int getNextStreamId(Http2Connection conn) {
        return conn.local().incrementAndGetNextStreamId();
    }

    /**
     * Validate the state of promised stream with the original stream id and given stream id.
     *
     * @param originalStreamId  the original id of the stream
     * @param streamId          the id of the stream to be validated
     * @param conn              HTTP2 connection
     * @param inboundRequestMsg request message received from the client
     * @throws Http2Exception throws if stream id is not valid for given connection
     */
    public static void validatePromisedStreamState(int originalStreamId, int streamId, Http2Connection conn,
                                                   HttpCarbonMessage inboundRequestMsg) throws Http2Exception {
        if (streamId == originalStreamId) { // Not a promised stream, no need to validate
            return;
        }
        if (!isValidStreamId(streamId, conn)) {
            inboundRequestMsg.getHttpOutboundRespStatusFuture().
                    notifyHttpListener(new ServerConnectorException(PROMISED_STREAM_REJECTED_ERROR));
            throw new Http2Exception(Http2Error.REFUSED_STREAM, PROMISED_STREAM_REJECTED_ERROR);
        }
    }

    /**
     * Check for the validity of stream id.
     *
     * @param streamId the id of the stream
     * @param conn     HTTP2 connection
     * @return whether the stream id is valid or not
     */
    public static boolean isValidStreamId(int streamId, Http2Connection conn) {
        return conn.stream(streamId) != null;
    }
}
