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

package org.ballerinalang.net.netty.contractimpl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import org.ballerinalang.net.netty.contract.Constants;
import org.ballerinalang.net.netty.contract.HttpConnectorListener;
import org.ballerinalang.net.netty.contract.HttpResponseFuture;
import org.ballerinalang.net.netty.contractimpl.listener.states.http2.EntityBodyReceived;
import org.ballerinalang.net.netty.contractimpl.listener.states.http2.SendingHeaders;
import org.ballerinalang.net.netty.message.BackPressureObservable;
import org.ballerinalang.net.netty.message.DefaultBackPressureListener;
import org.ballerinalang.net.netty.message.DefaultBackPressureObservable;
import org.ballerinalang.net.netty.message.DefaultListener;
import org.ballerinalang.net.netty.message.Http2InboundContentListener;
import org.ballerinalang.net.netty.message.Http2PassthroughBackPressureListener;
import org.ballerinalang.net.netty.message.Http2PushPromise;
import org.ballerinalang.net.netty.message.HttpCarbonMessage;
import org.ballerinalang.net.netty.message.Listener;
import org.ballerinalang.net.netty.message.PassthroughBackPressureListener;
import org.ballerinalang.net.netty.message.ServerRemoteFlowControlListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ballerinalang.net.netty.contract.exceptions.ServerConnectorException;
import org.ballerinalang.net.netty.contractimpl.common.states.Http2MessageStateContext;
import org.ballerinalang.net.netty.contractimpl.listener.HttpServerChannelInitializer;
import org.ballerinalang.net.netty.contractimpl.listener.http2.Http2ServerChannel;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.ballerinalang.net.netty.contractimpl.common.states.Http2StateUtil.isValidStreamId;

/**
 * {@code Http2OutboundRespListener} is responsible for listening for outbound response messages
 * and delivering them to the client.
 */
public class Http2OutboundRespListener implements HttpConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(Http2OutboundRespListener.class);

    private Http2MessageStateContext http2MessageStateContext;
    private org.ballerinalang.net.netty.message.HttpCarbonMessage inboundRequestMsg;
    private org.ballerinalang.net.netty.message.HttpCarbonMessage outboundResponseMsg;
    private ChannelHandlerContext ctx;
    private Http2ConnectionEncoder encoder;
    private int originalStreamId;   // stream id of the request received from the client
    private Http2Connection conn;
    private String serverName;
    private org.ballerinalang.net.netty.contract.HttpResponseFuture outboundRespStatusFuture;
    private HttpServerChannelInitializer serverChannelInitializer;
    private Calendar inboundRequestArrivalTime;
    private String remoteAddress = "-";
    private org.ballerinalang.net.netty.message.ServerRemoteFlowControlListener remoteFlowControlListener;
    private ResponseWriter defaultResponseWriter;
    private Http2ServerChannel http2ServerChannel;

    public Http2OutboundRespListener(HttpServerChannelInitializer serverChannelInitializer,
                                     org.ballerinalang.net.netty.message.HttpCarbonMessage inboundRequestMsg, ChannelHandlerContext ctx,
                                     Http2Connection conn, Http2ConnectionEncoder encoder, int streamId,
                                     String serverName, String remoteAddress,
                                     ServerRemoteFlowControlListener remoteFlowControlListener,
                                     Http2ServerChannel http2ServerChannel) {
        this.serverChannelInitializer = serverChannelInitializer;
        this.inboundRequestMsg = inboundRequestMsg;
        this.ctx = ctx;
        this.conn = conn;
        this.encoder = encoder;
        this.originalStreamId = streamId;
        this.serverName = serverName;
        if (remoteAddress != null) {
            this.remoteAddress = remoteAddress;
        }
        outboundRespStatusFuture = inboundRequestMsg.getHttpOutboundRespStatusFuture();
        inboundRequestArrivalTime = Calendar.getInstance();
        http2MessageStateContext = inboundRequestMsg.getHttp2MessageStateContext();
        this.remoteFlowControlListener = remoteFlowControlListener;
        this.http2ServerChannel = http2ServerChannel;
    }

    @Override
    public void onMessage(org.ballerinalang.net.netty.message.HttpCarbonMessage outboundResponseMsg) {
        this.outboundResponseMsg = outboundResponseMsg;
        writeMessage(outboundResponseMsg, originalStreamId, true);
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.error("Couldn't send the outbound response", throwable);
    }

    @Override
    public void onPushPromise(org.ballerinalang.net.netty.message.Http2PushPromise pushPromise) {
        writePromise(pushPromise);
    }

    @Override
    public void onPushResponse(int promiseId, org.ballerinalang.net.netty.message.HttpCarbonMessage outboundResponseMsg) {
        //TODO:Add HTTP/2 server timeout handler for the push response stream
        if (isValidStreamId(promiseId, conn)) {
            //TODO:Call dataEventListener.onStreamInit with the promiseId
            writeMessage(outboundResponseMsg, promiseId, false);
        } else {
            inboundRequestMsg.getHttpOutboundRespStatusFuture()
                    .notifyHttpListener(new ServerConnectorException(Constants.PROMISED_STREAM_REJECTED_ERROR));
        }
    }

    private void writePromise(Http2PushPromise pushPromise) {
        ctx.channel().eventLoop().execute(() -> {
            try {
                if (http2MessageStateContext == null) {
                    http2MessageStateContext = new Http2MessageStateContext();
                    http2MessageStateContext.setListenerState(new EntityBodyReceived(http2MessageStateContext));
                }
                http2MessageStateContext.getListenerState().writeOutboundPromise(this, pushPromise);
            } catch (Http2Exception ex) {
                LOG.error("Failed to send push promise : " + ex.getMessage(), ex);
                inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(ex);
            }
        });
    }

    private void writeMessage(org.ballerinalang.net.netty.message.HttpCarbonMessage outboundResponseMsg, int streamId, boolean backOffEnabled) {
        ResponseWriter writer = new ResponseWriter(streamId);
        if (backOffEnabled) {
            remoteFlowControlListener.addResponseWriter(writer);
            defaultResponseWriter = writer;
        }
        setBackPressureListener(outboundResponseMsg, writer);
        setContentEncoding(outboundResponseMsg);
        outboundResponseMsg.getHttpContentAsync().setMessageListener(httpContent -> {
            checkStreamUnwritability(writer);
            ctx.channel().eventLoop().execute(() -> {
                try {
                    writer.writeOutboundResponse(outboundResponseMsg, httpContent);
                } catch (Http2Exception ex) {
                    LOG.error("Failed to send the outbound response : " + ex.getMessage(), ex);
                    inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(ex);
                }
            });
        });
    }

    private void setContentEncoding(org.ballerinalang.net.netty.message.HttpCarbonMessage outboundResponseMsg) {
        String contentEncoding = outboundResponseMsg.getHeader(HttpHeaderNames.CONTENT_ENCODING.toString());
        //This means compression AUTO case; With NEVER(identity) and ALWAYS, content-encoding will always have a value.
        if (contentEncoding == null) {
            String acceptEncoding = inboundRequestMsg.getHeader(HttpHeaderNames.ACCEPT_ENCODING.toString());
            if (acceptEncoding != null) {
                String targetContentEncoding = determineScheme(acceptEncoding);
                if (targetContentEncoding != null) {
                    outboundResponseMsg.setHeader(HttpHeaderNames.CONTENT_ENCODING.toString(), targetContentEncoding);
                }
            }
        }
    }

    /***
     * This function is to determine one encoding scheme from the request's `accept-encoding` header. The logic to
     * determine the scheme is similar to the logic used in Netty's `determineWrapper()` function in the
     * `HttpContentCompressor` class which is used to do the same, when doing the HTTP1.1 compression.
     *
     * @param acceptEncoding `accept-encoding` header value
     * @return the chosen encoding scheme
     */
    private String determineScheme(String acceptEncoding) {
        float starQ = -1.0f;
        float gzipQ = -1.0f;
        float deflateQ = -1.0f;
        for (String encoding : acceptEncoding.split(",")) {
            float qValue = 1.0f;
            int equalsPos = encoding.indexOf('=');
            if (equalsPos != -1) {
                try {
                    qValue = Float.parseFloat(encoding.substring(equalsPos + 1));
                } catch (NumberFormatException e) {
                    // Ignore encoding
                    qValue = 0.0f;
                }
            }
            if (encoding.contains("*")) {
                starQ = qValue;
            } else if (encoding.contains(Constants.ENCODING_GZIP) && qValue > gzipQ) {
                gzipQ = qValue;
            } else if (encoding.contains(Constants.ENCODING_DEFLATE) && qValue > deflateQ) {
                deflateQ = qValue;
            } else {
                LOG.debug("Server does not support the requested encoding scheme/s");
            }
        }
        if (gzipQ > 0.0f || deflateQ > 0.0f) {
            if (gzipQ >= deflateQ) {
                return Constants.ENCODING_GZIP;
            } else {
                return Constants.ENCODING_DEFLATE;
            }
        }
        if (starQ > 0.0f) {
            if (gzipQ == -1.0f) {
                return Constants.ENCODING_GZIP;
            }
            if (deflateQ == -1.0f) {
                return Constants.ENCODING_DEFLATE;
            }
        }
        return null;
    }

    /**
     * Responsible for writing HTTP/2 outbound response to the caller.
     */
    public class ResponseWriter {
        private int streamId;
        private AtomicBoolean streamWritable = new AtomicBoolean(true);
        private final org.ballerinalang.net.netty.message.BackPressureObservable
                backPressureObservable = new DefaultBackPressureObservable();

        ResponseWriter(int streamId) {
            this.streamId = streamId;
        }

        private void writeOutboundResponse(org.ballerinalang.net.netty.message.HttpCarbonMessage outboundResponseMsg, HttpContent httpContent)
            throws Http2Exception {
            if (http2MessageStateContext == null) {
                http2MessageStateContext = new Http2MessageStateContext();
                http2MessageStateContext.setListenerState(
                    new SendingHeaders(Http2OutboundRespListener.this, http2MessageStateContext));
            }
            http2MessageStateContext.getListenerState().
                writeOutboundResponseBody(Http2OutboundRespListener.this, outboundResponseMsg,
                                          httpContent, streamId);
        }

        public int getStreamId() {
            return streamId;
        }

        public void setStreamWritable(boolean streamWritable) {
            this.streamWritable.set(streamWritable);
        }

        boolean isStreamWritable() {
            return streamWritable.get();
        }

        public BackPressureObservable getBackPressureObservable() {
            return backPressureObservable;
        }
    }

    private void setBackPressureListener(org.ballerinalang.net.netty.message.HttpCarbonMessage outboundResponseMsg, ResponseWriter writer) {
        if (outboundResponseMsg.isPassthrough()) {
            setPassthroughBackOffListener(outboundResponseMsg, writer);
        } else {
            writer.getBackPressureObservable().setListener(new DefaultBackPressureListener());
        }
    }

    /**
     * Passthrough backoff scenarios involved here are (response HTTP/2-HTTP/2) and (response HTTP/1.1-HTTP/2).
     *
     * @param outboundResponseMsg outbound response message
     * @param writer              HTTP/2 response writer
     */
    private void setPassthroughBackOffListener(org.ballerinalang.net.netty.message.HttpCarbonMessage outboundResponseMsg, ResponseWriter writer) {
        Listener inboundListener = outboundResponseMsg.getListener();
        if (inboundListener instanceof org.ballerinalang.net.netty.message.Http2InboundContentListener) {
            writer.getBackPressureObservable().setListener(
                new Http2PassthroughBackPressureListener((Http2InboundContentListener) inboundListener));
        } else if (inboundListener instanceof DefaultListener) {
            writer.getBackPressureObservable().setListener(
                new PassthroughBackPressureListener(outboundResponseMsg.getTargetContext()));
        }
    }

    private void checkStreamUnwritability(ResponseWriter writer) {
        if (!writer.isStreamWritable()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("In thread {}. Stream is not writable.", Thread.currentThread().getName());
            }
            writer.getBackPressureObservable().notifyUnWritable();
        }
    }

    public void resetStream(ChannelHandlerContext ctx, int streamId, Http2Error http2Error) throws Http2Exception {
        encoder.writeRstStream(ctx, streamId, http2Error.code(), ctx.newPromise());
        encoder.flowController().writePendingBytes();
        http2ServerChannel.getDataEventListeners()
                .forEach(dataEventListener -> dataEventListener.onStreamReset(streamId));
        ctx.flush();
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return ctx;
    }

    public Http2ConnectionEncoder getEncoder() {
        return encoder;
    }

    public HttpResponseFuture getOutboundRespStatusFuture() {
        return outboundRespStatusFuture;
    }

    public HttpServerChannelInitializer getServerChannelInitializer() {
        return serverChannelInitializer;
    }

    public org.ballerinalang.net.netty.message.HttpCarbonMessage getInboundRequestMsg() {
        return inboundRequestMsg;
    }

    public HttpCarbonMessage getOutboundResponseMsg() {
        return outboundResponseMsg;
    }

    public Http2Connection getConnection() {
        return conn;
    }

    public Calendar getInboundRequestArrivalTime() {
        return inboundRequestArrivalTime;
    }

    public int getOriginalStreamId() {
        return originalStreamId;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getServerName() {
        return serverName;
    }

    public void removeDefaultResponseWriter() {
        remoteFlowControlListener.removeResponseWriter(defaultResponseWriter);
    }

    public void removeBackPressureListener() {
        defaultResponseWriter.getBackPressureObservable().removeListener();
    }

    public Http2ServerChannel getHttp2ServerChannel() {
        return http2ServerChannel;
    }
}
