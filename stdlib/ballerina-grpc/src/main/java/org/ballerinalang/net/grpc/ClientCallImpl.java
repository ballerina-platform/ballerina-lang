/*
 * Copyright 2014, gRPC Authors All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.net.grpc;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;

import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;

import java.io.InputStream;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Implementation of {@link ClientCall}.
 *
 * @param <ReqT> Request Message Type.
 * @param <RespT> Response Message Type.
 */
public final class ClientCallImpl<ReqT, RespT> extends ClientCall<ReqT, RespT> {

    private static final Logger log = Logger.getLogger(ClientCallImpl.class.getName());
    private static final String FULL_STREAM_DECOMPRESSION_ENCODINGS = "gzip";

    private final MethodDescriptor<ReqT, RespT> method;
    private final CallOptions callOptions;
    private final boolean unaryRequest;
    private HttpClientConnector connector;
    private final OutboundMessage outboundMessage;
    private final CallableUnitCallback callback;

    private ClientConnectorListener connectorListener;
    private boolean cancelCalled;
    private boolean halfCloseCalled;
    private boolean fullStreamDecompression;
    private DecompressorRegistry decompressorRegistry = DecompressorRegistry.getDefaultInstance();
    private CompressorRegistry compressorRegistry = CompressorRegistry.getDefaultInstance();

    public ClientCallImpl(HttpClientConnector connector, OutboundMessage outboundMessage,  MethodDescriptor<ReqT, RespT>
            method, CallOptions callOptions, CallableUnitCallback callback) {

        this.method = method;
        this.unaryRequest = method.getType() == MethodDescriptor.MethodType.UNARY
                || method.getType() == MethodDescriptor.MethodType.SERVER_STREAMING;
        this.callOptions = callOptions;
        this.connector = connector;
        this.outboundMessage = outboundMessage;
        this.callback = callback;
    }

    ClientCallImpl<ReqT, RespT> setFullStreamDecompression(boolean fullStreamDecompression) {

        this.fullStreamDecompression = fullStreamDecompression;
        return this;
    }

    ClientCallImpl<ReqT, RespT> setDecompressorRegistry(DecompressorRegistry decompressorRegistry) {

        this.decompressorRegistry = decompressorRegistry;
        return this;
    }

    ClientCallImpl<ReqT, RespT> setCompressorRegistry(CompressorRegistry compressorRegistry) {

        this.compressorRegistry = compressorRegistry;
        return this;
    }

    public void prepareHeaders(
            Compressor compressor) {

        outboundMessage.removeHeader("grpc-encoding");
        if (compressor != Codec.Identity.NONE) {
            outboundMessage.setHeader("grpc-encoding", compressor.getMessageEncoding());
        }

        outboundMessage.removeHeader("grpc-accept-encoding");
        String advertisedEncodings = String.join(",", decompressorRegistry.getAdvertisedMessageEncodings());
        if (advertisedEncodings != null) {
            outboundMessage.setHeader("grpc-accept-encoding", advertisedEncodings);
        }

        outboundMessage.removeHeader("content-encoding");
        outboundMessage.removeHeader("accept-encoding");
        if (fullStreamDecompression) {
            outboundMessage.setHeader("accept-encoding", FULL_STREAM_DECOMPRESSION_ENCODINGS);
        }


        outboundMessage.setProperty(Constants.TO, "/" + method.getFullMethodName());
        outboundMessage.setProperty(Constants.HTTP_METHOD, GrpcConstants.HTTP_METHOD);
        outboundMessage.setProperty(Constants.HTTP_VERSION, "2.0");
        outboundMessage.setHeader("content-type", GrpcConstants.CONTENT_TYPE_GRPC);
        outboundMessage.setHeader("te", GrpcConstants.TE_TRAILERS);
//
//        outboundMessage.setProperty(Constants.HTTP_METHOD, Constants.HTTP_POST_METHOD);
    }

    @Override
    public void start(final ClientCall.Listener<RespT> observer) {

        checkState(connectorListener == null, "Already started");
        checkState(!cancelCalled, "call was cancelled");
        checkNotNull(observer, "observer");

        final String compressorName = callOptions.getOption(GrpcConstants.COMPRESSOR_NAME);
        Compressor compressor;
        if (compressorName != null) {
            compressor = compressorRegistry.lookupCompressor(compressorName);
            if (compressor == null) {
                closeObserver(
                        observer,
                        Status.Code.INTERNAL.toStatus().withDescription(
                                String.format("Unable to find compressor by name %s", compressorName)),
                        new DefaultHttpHeaders());

                return;
            }
        } else {
            compressor = Codec.Identity.NONE;
        }
        prepareHeaders(compressor);

        ClientStreamListener clientStreamListener = new ClientStreamListenerImpl(observer);
        connectorListener = new ClientConnectorListener(this, clientStreamListener);

        outboundMessage.framer().setCompressor(compressor);
        connectorListener.setDecompressorRegistry(decompressorRegistry);
        HttpResponseFuture responseFuture = connector.send(outboundMessage.getResponseMessage());
        responseFuture.setHttpConnectorListener(connectorListener);
    }

    @Override
    public void request(int numMessages) {

        checkState(connectorListener != null, "Not started");
        checkArgument(numMessages >= 0, "Number requested must be non-negative");
        connectorListener.request(numMessages);
    }

    @Override
    public void cancel(@Nullable String message, @Nullable Throwable cause) {

        if (message == null && cause == null) {
            cause = new CancellationException("Cancelled without a message or cause");
            log.log(Level.WARNING, "Cancelling without a message or cause is suboptimal", cause);
        }
        if (cancelCalled) {
            return;
        }
        cancelCalled = true;
        // Cancel is called in exception handling cases, so it may be the case that the
        // stream was never successfully created or start has never been called.
        if (outboundMessage != null) {
            Status status = Status.Code.CANCELLED.toStatus();
            if (message != null) {
                status = status.withDescription(message);
            } else {
                status = status.withDescription("Call cancelled without message");
            }
            if (cause != null) {
                status = status.withCause(cause);
            }
            outboundMessage.complete(status, new DefaultHttpHeaders());
        }
    }

    @Override
    public void halfClose() {

        checkState(outboundMessage != null, "Not started");
        checkState(!cancelCalled, "call was cancelled");
        checkState(!halfCloseCalled, "call already half-closed");
        halfCloseCalled = true;
        outboundMessage.halfClose();
    }

    @Override
    public void sendMessage(ReqT message) {

        checkState(connectorListener != null, "Not started");
        checkState(!cancelCalled, "call was cancelled");
        checkState(!halfCloseCalled, "call was half-closed");
        try {
            InputStream resp = method.streamRequest(message);
            outboundMessage.sendMessage(resp);

        } catch (RuntimeException e) {
            throw Status.Code.CANCELLED.toStatus().withCause(e).withDescription("Failed to stream message")
                    .asRuntimeException();
        } catch (Error e) {
            throw Status.Code.CANCELLED.toStatus().withDescription("Client sendMessage() failed with Error")
                    .asRuntimeException();
        }
        // For unary requests, we don't flush since we know that halfClose should be coming soon. This
        // allows us to piggy-back the END_STREAM=true on the last message frame without opening the
        // possibility of broken applications forgetting to call halfClose without noticing.
        if (!unaryRequest) {
            outboundMessage.flush();
        }
    }

    @Override
    public void setMessageCompression(boolean enabled) {

        checkState(outboundMessage != null, "Not started");
        outboundMessage.setMessageCompression(enabled);
    }

    @Override
    public CallableUnitCallback getBallerinaCallback() {

        return callback;
    }

    private void closeObserver(Listener<RespT> observer, Status status, HttpHeaders trailers) {

        observer.onClose(status, trailers);
    }

    private class ClientStreamListenerImpl implements ClientStreamListener {

        private final Listener<RespT> observer;
        private boolean closed;

        public ClientStreamListenerImpl(Listener<RespT> observer) {

            this.observer = checkNotNull(observer, "observer");
        }

        @Override
        public void headersRead(final HttpHeaders headers) {

            try {
                if (closed) {
                    return;
                }
                observer.onHeaders(headers);
            } catch (Throwable t) {
                Status status =
                        Status.Code.CANCELLED.toStatus().withCause(t).withDescription("Failed to read headers");
                close(status, new DefaultHttpHeaders());
            }

        }

        @Override
        public void messagesAvailable(final MessageProducer producer) {

            if (closed) {
                MessageUtils.closeQuietly(producer);
                return;
            }

            InputStream message;
            try {
                while ((message = producer.next()) != null) {
                    try {
                        observer.onMessage(method.parseResponse(message));
                    } catch (Throwable t) {
                        MessageUtils.closeQuietly(message);
                        throw t;
                    }
                    message.close();
                }
            } catch (Throwable t) {
                MessageUtils.closeQuietly(producer);
                Status status =
                        Status.Code.CANCELLED.toStatus().withCause(t).withDescription("Failed to read message.");
                close(status, new DefaultHttpHeaders());
            }
        }

        /**
         * Must be called from application thread.
         */
        private void close(Status status, HttpHeaders trailers) {

            closed = true;
            closeObserver(observer, status, trailers);
        }

        @Override
        public void closed(Status status, HttpHeaders trailers) {

            if (closed) {
                // We intentionally don't keep the status or metadata from the server.
                return;
            }
            close(status, trailers);
        }

        @Override
        public void onReady() {

            try {
                observer.onReady();
            } catch (Throwable t) {
                Status status =
                        Status.Code.CANCELLED.toStatus().withCause(t).withDescription("Failed to call onReady.");
                close(status, new DefaultHttpHeaders());
            }

        }
    }
}
