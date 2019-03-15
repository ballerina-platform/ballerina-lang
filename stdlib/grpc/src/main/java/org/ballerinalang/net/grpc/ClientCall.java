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
 */
package org.ballerinalang.net.grpc;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;

import org.ballerinalang.net.grpc.exception.StatusRuntimeException;
import org.ballerinalang.net.grpc.stubs.AbstractStub;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;

import java.io.InputStream;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.ballerinalang.net.grpc.GrpcConstants.CONTENT_TYPE_KEY;
import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_ACCEPT_ENCODING;
import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_ENCODING;
import static org.ballerinalang.net.grpc.GrpcConstants.TE_KEY;

/**
 * This class handles a call to a remote method.
 * A call will send zero or more request messages to the server and receive zero or more response messages back.
 *
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 *
 * @since 0.980.0
 */
public final class ClientCall {

    private static final Logger log = Logger.getLogger(ClientCall.class.getName());
    private final MethodDescriptor method;
    private final boolean unaryRequest;
    private HttpClientConnector connector;
    private final OutboundMessage outboundMessage;
    private ClientConnectorListener connectorListener;
    private boolean cancelCalled;
    private boolean halfCloseCalled;
    private DecompressorRegistry decompressorRegistry = DecompressorRegistry.getDefaultInstance();
    private CompressorRegistry compressorRegistry = CompressorRegistry.getDefaultInstance();

    public ClientCall(HttpClientConnector connector, OutboundMessage outboundMessage, MethodDescriptor method) {
        this.method = method;
        this.unaryRequest = method.getType() == MethodDescriptor.MethodType.UNARY
                || method.getType() == MethodDescriptor.MethodType.SERVER_STREAMING;
        this.connector = connector;
        this.outboundMessage = outboundMessage;
    }

    private void prepareHeaders(
            Compressor compressor) {
        outboundMessage.removeHeader(MESSAGE_ENCODING);
        if (compressor != Codec.Identity.NONE) {
            outboundMessage.setHeader(MESSAGE_ENCODING, compressor.getMessageEncoding());
        }
        String advertisedEncodings = String.join(",", decompressorRegistry.getAdvertisedMessageEncodings());
        outboundMessage.setHeader(MESSAGE_ACCEPT_ENCODING, advertisedEncodings);
        outboundMessage.setProperty(Constants.TO, "/" + method.getFullMethodName());
        outboundMessage.setProperty(Constants.HTTP_METHOD, GrpcConstants.HTTP_METHOD);
        outboundMessage.setProperty(Constants.HTTP_VERSION, "2.0");
        outboundMessage.setHeader(CONTENT_TYPE_KEY, GrpcConstants.CONTENT_TYPE_GRPC);
        outboundMessage.setHeader(TE_KEY, GrpcConstants.TE_TRAILERS);
    }

    /**
     * Start a call, using {@code responseListener} for processing response messages.
     *
     * @param observer response listener instance
     */
    public void start(final AbstractStub.Listener observer) {
        if (connectorListener != null) {
            throw new IllegalStateException("Client connection us already setup.");
        }
        if (cancelCalled) {
            throw new IllegalStateException("Client call was cancelled.");
        }
        Compressor compressor;
        String compressorName = outboundMessage.getHeader("grpc-encoding");
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
        ClientStreamListener clientStreamListener = new ClientStreamListener(observer);
        connectorListener = new ClientConnectorListener(clientStreamListener);
        outboundMessage.framer().setCompressor(compressor);
        connectorListener.setDecompressorRegistry(decompressorRegistry);
        HttpResponseFuture responseFuture = connector.send(outboundMessage.getResponseMessage());
        responseFuture.setHttpConnectorListener(connectorListener);
    }

    /**
     * Prevent any further processing for the remote call.
     *
     * @param message error message
     * @param cause Throwable
     */
    public void cancel(String message, Throwable cause) {
        if (message == null && cause == null) {
            cause = new CancellationException("Cancelled without a message or cause");
            log.log(Level.WARNING, "Cancelling without a message or cause is suboptimal", cause);
        }
        if (cancelCalled) {
            return;
        }
        cancelCalled = true;
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

    /**
     * Close the call for request message sending. Incoming response messages are unaffected.
     */
    public void halfClose() {
        if (outboundMessage == null) {
            throw new IllegalStateException("Client call did not start properly.");
        }
        if (cancelCalled) {
            throw new IllegalStateException("Client call was called.");
        }
        if (halfCloseCalled) {
            throw new IllegalStateException("Client call was already closed.");
        }
        halfCloseCalled = true;
        outboundMessage.halfClose();
    }

    /**
     * Send a request message to the server.
     *
     * @param message Request message.
     */
    public void sendMessage(Message message) {
        if (connectorListener == null) {
            throw new IllegalStateException("Connector listener didn't initialize properly.");
        }
        if (cancelCalled) {
            throw new IllegalStateException("Client call was already called.");
        }
        if (halfCloseCalled) {
            throw new IllegalStateException("Client call was already closed.");
        }
        try {
            InputStream resp = method.streamRequest(message);
            outboundMessage.sendMessage(resp);
        } catch (StatusRuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw Status.Code.CANCELLED.toStatus().withCause(ex).withDescription("Failed to stream message")
                    .asRuntimeException();
        }
        // For unary requests, halfClose call should be coming soon.
        if (!unaryRequest) {
            outboundMessage.flush();
        }
    }

    /**
     * Enables per-message compression.
     *
     * @param enabled enable flag
     */
    public void setMessageCompression(boolean enabled) {
        if (outboundMessage == null) {
            throw new IllegalStateException("Client call did not start properly.");
        }
        outboundMessage.setMessageCompression(enabled);
    }

    private void closeObserver(AbstractStub.Listener observer, Status status, HttpHeaders trailers) {
        observer.onClose(status, trailers);
    }

    public boolean isReady() {
        return outboundMessage.isReady();
    }

    /**
     * Client Stream Listener instance.
     */
    public class ClientStreamListener implements StreamListener {

        private final AbstractStub.Listener observer;
        private boolean closed;
        private HttpHeaders responseHeaders;

        ClientStreamListener(AbstractStub.Listener observer) {
            this.observer = observer;
        }

        public void headersRead(final HttpHeaders headers) {
            try {
                if (closed) {
                    return;
                }
                responseHeaders = headers;
                observer.onHeaders(headers);
            } catch (Exception ex) {
                Status status =
                        Status.Code.CANCELLED.toStatus().withCause(ex).withDescription("Failed to read headers");
                close(status, new DefaultHttpHeaders());
            }
        }

        @Override
        public void messagesAvailable(final InputStream message) {
            if (closed) {
                MessageUtils.closeQuietly(message);
                return;
            }
            try {
                Message responseMessage = method.parseResponse(message);
                responseMessage.setHeaders(responseHeaders);
                observer.onMessage(responseMessage);
                message.close();
            } catch (Exception ex) {
                MessageUtils.closeQuietly(message);
                Status status =
                        Status.Code.CANCELLED.toStatus().withCause(ex).withDescription("Failed to read message.");
                close(status, new DefaultHttpHeaders());
            }
        }

        private void close(Status status, HttpHeaders trailers) {
            closed = true;
            closeObserver(observer, status, trailers);
        }

        public void closed(Status status, HttpHeaders trailers) {
            if (closed) {
                return;
            }
            close(status, trailers);
        }
    }
}
