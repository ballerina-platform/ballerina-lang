/*
 * Copyright 2015, gRPC Authors All rights reserved.
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
import org.wso2.transport.http.netty.contract.ServerConnectorException;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_ENCODING;

final class ServerCallImpl<ReqT, RespT> extends ServerCall<ReqT, RespT> {

    private static final Logger log = Logger.getLogger(ServerCallImpl.class.getName());

    /**
     * The accepted message encodings (i.e. compression) that can be used in the stream.
     */
    private static final String MESSAGE_ACCEPT_ENCODING = "grpc-accept-encoding";

    private static final String TOO_MANY_RESPONSES = "Too many responses";
    private static final String MISSING_RESPONSE = "Completed without a response";

    private final InboundMessage inboundMessage;
    private final OutboundMessage outboundMessage;
    private final MethodDescriptor<ReqT, RespT> method;

    // state
    private volatile boolean cancelled;
    private boolean sendHeadersCalled;
    private boolean closeCalled;
    private boolean messageSent;
    private Compressor compressor;
    private final String messageAcceptEncoding;

    private DecompressorRegistry decompressorRegistry;
    private CompressorRegistry compressorRegistry;

    ServerCallImpl(InboundMessage inboundMessage, OutboundMessage outboundMessage, MethodDescriptor<ReqT, RespT>
            method, DecompressorRegistry decompressorRegistry, CompressorRegistry compressorRegistry) {

        this.inboundMessage = inboundMessage;
        this.outboundMessage = outboundMessage;
        this.method = method;
        this.decompressorRegistry = decompressorRegistry;
        this.compressorRegistry = compressorRegistry;
        this.messageAcceptEncoding = inboundMessage.getHeader(MESSAGE_ACCEPT_ENCODING);
    }

    @Override
    public void sendHeaders() {

        if (sendHeadersCalled) {
            throw new RuntimeException("sendHeaders has already been called");
        }

        if (closeCalled) {
            throw new RuntimeException("call is closed");
        }

        outboundMessage.removeHeader(MESSAGE_ENCODING);
        if (compressor == null) {
            compressor = Codec.Identity.NONE;
        } else {
            if (messageAcceptEncoding != null) {
                List<String> acceptEncodings = Arrays.stream(messageAcceptEncoding.split("\\s*,\\s*"))
                        .map(mediaType -> mediaType.split("\\s*;\\s*")[0])
                        .collect(Collectors.toList());
                if (!acceptEncodings.contains(compressor.getMessageEncoding())) {
                    // resort to using no compression.
                    compressor = Codec.Identity.NONE;
                }
            } else {
                compressor = Codec.Identity.NONE;
            }
        }

        // Always put compressor, even if it's identity.
        outboundMessage.setHeader(MESSAGE_ENCODING, compressor.getMessageEncoding());
        outboundMessage.framer().setCompressor(compressor);

        outboundMessage.removeHeader(MESSAGE_ACCEPT_ENCODING);
        String advertisedEncodings = String.join(",", decompressorRegistry.getAdvertisedMessageEncodings());

        if (advertisedEncodings != null) {
            outboundMessage.setHeader(MESSAGE_ACCEPT_ENCODING, advertisedEncodings);
        }

        // Don't check if sendMessage has been called, since it requires that sendHeaders was already
        // called.
        try {
            inboundMessage.respond(outboundMessage.getResponseMessage());
        } catch (ServerConnectorException e) {
            throw new RuntimeException("Error while sending the response.", e);
        }
        sendHeadersCalled = true;
    }

    @Override
    public void sendMessage(RespT message) {

        if (!sendHeadersCalled) {
            throw new RuntimeException("sendHeaders has not been called");
        }

        if (closeCalled) {
            throw new RuntimeException("call is closed");
        }

        if (method.getType().serverSendsOneMessage() && messageSent) {
            internalClose(Status.Code.INTERNAL.toStatus().withDescription(TOO_MANY_RESPONSES));
            return;
        }

        messageSent = true;
        try {
            InputStream resp = method.streamResponse(message);
            outboundMessage.sendMessage(resp);
        } catch (RuntimeException e) {
            close(Status.fromThrowable(e), new DefaultHttpHeaders());
        } catch (Error e) {
            close(
                    Status.Code.CANCELLED.toStatus().withDescription("Server sendMessage() failed with Error"), new
                            DefaultHttpHeaders());
            throw e;
        }
    }

    @Override
    public void setCompression(String compressorName) {
        // Added here to give a better error message.
        checkState(!sendHeadersCalled, "sendHeaders has been called");

        compressor = compressorRegistry.lookupCompressor(compressorName);
        checkArgument(compressor != null, "Unable to find compressor by name %s", compressorName);
    }

    @Override
    public void setMessageCompression(boolean enable) {
        //check this.
    }

    @Override
    public boolean isReady() {

        return outboundMessage.isReady();
    }

    @Override
    public void close(Status status, HttpHeaders trailers) {

        if (closeCalled) {
            throw new RuntimeException("call already closed");
        }
        closeCalled = true;

        if (status.isOk() && method.getType().serverSendsOneMessage() && !messageSent) {
            internalClose(Status.Code.INTERNAL.toStatus().withDescription(MISSING_RESPONSE));
            return;
        }
//        try {
        //inboundMessage.respond(outboundMessage.getResponseMessage());
        outboundMessage.complete(status, trailers);
//        } catch (ServerConnectorException e) {
//            throw new RuntimeException("Error while sending the response.", e);
//        }
    }

    @Override
    public boolean isCancelled() {

        return cancelled;
    }

    ServerStreamListener newServerStreamListener(ServerCall.Listener<ReqT> listener) {

        return new ServerStreamListenerImpl<>(this, listener);
    }

    @Override
    public String getAuthority() {
        //check this.
        return "";
    }

    @Override
    public MethodDescriptor<ReqT, RespT> getMethodDescriptor() {

        return method;
    }

    /**
     * Close the {@link OutboundMessage} because an internal error occurred. Allow the application to
     * run until completion, but silently ignore interactions with the {@link OutboundMessage} from now
     * on.
     */
    private void internalClose(Status internalError) {

        outboundMessage.complete(internalError, new DefaultHttpHeaders());
    }

    /**
     * All of these callbacks are assumed to called on an application thread, and the caller is
     * responsible for handling thrown exceptions.
     */
    static final class ServerStreamListenerImpl<ReqT> implements ServerStreamListener {

        private final ServerCallImpl<ReqT, ?> call;
        private final ServerCall.Listener<ReqT> listener;

        public ServerStreamListenerImpl(
                ServerCallImpl<ReqT, ?> call, ServerCall.Listener<ReqT> listener) {

            this.call = call;
            this.listener = listener;
        }

        @Override
        public void messagesAvailable(final InputStream message) {

            if (call.cancelled) {
                MessageUtils.closeQuietly(message);
                return;
            }

            try {
                while (message != null && message.available() > 0) {
                    try {
                        listener.onMessage(call.method.parseRequest(message));
                    } catch (Throwable t) {
                        MessageUtils.closeQuietly(message);
                        throw t;
                    }
                    message.close();
                }
            } catch (Throwable t) {
                MessageUtils.closeQuietly(message);
                throw new RuntimeException(t);
            }
        }

        @Override
        public void halfClosed() {

            if (call.cancelled) {
                return;
            }

            listener.onHalfClose();
        }

        @Override
        public void closed(Status status) {

            if (status.isOk()) {
                listener.onComplete();
            } else {
                call.cancelled = true;
                listener.onCancel();
            }
        }

        @Override
        public void onReady() {

            if (call.cancelled) {
                return;
            }
            listener.onReady();
        }
    }
}
