/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.LastHttpContent;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpClientConnectorListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.nio.charset.Charset;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.net.grpc.GrpcConstants.DEFAULT_MAX_MESSAGE_SIZE;
import static org.ballerinalang.net.grpc.MessageUtils.readAsString;

/**
 * Client Connector Listener.
 */
public class ClientConnectorListener implements HttpClientConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(ClientConnectorListener.class);
    ClientCall call;
    private Status transportError;
    private HttpHeaders transportErrorMetadata;
    private boolean headersReceived;
    private InboundStateListener stateListener;

    ClientConnectorListener(ClientCall call, ClientCall.ClientStreamListener streamListener) {

        this.call = call;
        this.stateListener = new InboundStateListener(DEFAULT_MAX_MESSAGE_SIZE, streamListener);
    }

    public final void setDecompressorRegistry(DecompressorRegistry decompressorRegistry) {

        stateListener.setDecompressorRegistry(decompressorRegistry);
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpMessage) {

        InboundMessage inboundMessage = new InboundMessage(httpMessage);

        if (isValid(inboundMessage)) {
            stateListener.inboundHeadersReceived(inboundMessage.getHeaders());
        }

        final Executor wrappedExecutor = ThreadPoolFactory.getInstance().getWorkerExecutor();
        wrappedExecutor.execute(() -> {

            try {
                HttpContent httpContent = inboundMessage.getHttpCarbonMessage().getHttpContent();
                while (true) {
                    if (httpContent == null) {
                        break;
                    }
                    if (transportError != null) {
                        // Referenced from grpc-java, when transport error exists. we collect more details about the
                        // error by augmenting the description.
                        transportError = transportError.augmentDescription(
                                "MESSAGE DATA: " + readAsString(httpContent, Charset.forName("UTF-8")));
                        // Release content as we are not going to process it.
                        httpContent.release();
                        // Report transport error.
                        if ((transportError.getDescription() != null && transportError.getDescription().length() >
                                1000) || (httpContent instanceof LastHttpContent)) {
                            stateListener.transportReportStatus(transportError, false, transportErrorMetadata);
                            break;
                        }
                    } else {
                        stateListener.inboundDataReceived(httpContent);
                        // Exit the loop at the end of the content
                        if (httpContent instanceof LastHttpContent) {
                            LastHttpContent lastHttpContent = (LastHttpContent) httpContent;
                            if (lastHttpContent.trailingHeaders().isEmpty()) {
                                // This is a protocol violation as we expect to receive trailer headers with Last Http
                                // content.
                                transportError = Status.Code.INTERNAL.toStatus().withDescription("Received unexpected" +
                                        " EOS on DATA frame from server.");
                                transportErrorMetadata = new DefaultHttpHeaders();
                                stateListener.transportReportStatus(transportError, false, transportErrorMetadata);
                            } else {
                                // Read Trailer header to get gRPC response status.
                                transportTrailersReceived(lastHttpContent.trailingHeaders());
                            }
                            break;
                        }
                    }
                    httpContent = inboundMessage.getHttpCarbonMessage().getHttpContent();
                }
            } catch (RuntimeException | Error e) {

                if (transportError != null) {
                    // Already received a transport error so just augment it.
                    transportError = transportError.augmentDescription(e.getMessage());
                } else {
                    transportError = Status.fromThrowable(e);
                }
                stateListener.transportReportStatus(transportError, false, transportErrorMetadata);
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {
        if (transportError != null) {
            // Already received a transport error so just augment it.
            transportError = transportError.augmentDescription(throwable.getMessage());
        } else {
            transportError = Status.fromThrowable(throwable);
        }
        stateListener.transportReportStatus(transportError, false, transportErrorMetadata);
    }

    /**
     * Called by subclasses whenever {@code Headers} are received from the transport.
     *
     * @param inboundMessage the incoming message.
     */
    protected boolean isValid(InboundMessage inboundMessage) {

        HttpHeaders headers = inboundMessage.getHeaders();
        if (headers == null) {
            transportError = Status.Code.INTERNAL.toStatus().withDescription("Message headers is null");
            return false;
        }

        if (transportError != null) {
            // Already received a transport error so just augment it.
            transportError = transportError.augmentDescription("headers: " + headers);
            return false;
        }
        try {
            if (headersReceived) {
                transportError = Status.Code.INTERNAL.toStatus().withDescription("Received headers twice");
                return false;
            }
            Integer httpStatus = inboundMessage.getStatus();
            if (httpStatus >= 100 && httpStatus < 200) {
                // Ignore the headers. See RFC 7540 §8.1
                // 1xx (Informational): The request was received, continuing process
                return false;
            }
            headersReceived = true;

            transportError = validateInitialMetadata(inboundMessage);
            return transportError == null;

        } finally {
            if (transportError != null) {
                // Note we don't immediately report the transport error, instead we wait for more data on
                // the stream so we can accumulate more detail into the error before reporting it.
                transportErrorMetadata = headers;
            }
        }
    }

    private Status validateInitialMetadata(InboundMessage inboundMessage) {

        String contentType = inboundMessage.getHeaders().get("content-type");
        if (!MessageUtils.isGrpcContentType(contentType)) {
            return MessageUtils.httpStatusToGrpcStatus(inboundMessage.getStatus())
                    .augmentDescription("invalid content-type: " + contentType);
        }
        return null;
    }

    /**
     * Called by subclasses for the terminal trailer metadata on a stream.
     *
     * @param trailers the received terminal trailer metadata
     */
    protected void transportTrailersReceived(HttpHeaders trailers) {

        if (transportError != null) {
            transportError = transportError.augmentDescription("trailers: " + trailers);
            stateListener.transportReportStatus(transportError, false, transportErrorMetadata);
        } else {
            Status status = statusFromTrailers(trailers);
            stateListener.transportReportStatus(status, false, trailers);
        }
    }

    /**
     * Extract the response status from trailers.
     */
    private Status statusFromTrailers(HttpHeaders trailers) {

        String statusString = trailers.get("grpc-status");
        Status status = null;
        if (statusString != null) {
            Pattern statusCodePattern = Pattern.compile("Status\\{ code (.*?),");
            Matcher m = statusCodePattern.matcher(statusString);
            while (m.find()) {
                String sCode = m.group(1);
                Status.Code code = Status.Code.valueOf(sCode);
                status = Status.fromCode(code);
            }
        }
        if (status != null) {
            return status.withDescription(trailers.get("grpc-message"));
        } else {
            return Status.Code.UNKNOWN.toStatus().withDescription("missing GRPC status in response");
        }
    }

    private static class InboundStateListener extends InboundMessage.InboundStateListener {

        final ClientCall.ClientStreamListener listener;
        private DecompressorRegistry decompressorRegistry = DecompressorRegistry.getDefaultInstance();

        private boolean deframerClosed = false;
        private Runnable deframerClosedTask;

        private boolean statusReported;
        private boolean listenerClosed;

        protected InboundStateListener(int maxMessageSize, ClientCall.ClientStreamListener listener) {

            super(maxMessageSize);
            this.listener = listener;
        }

        private void setDecompressorRegistry(DecompressorRegistry decompressorRegistry) {

            this.decompressorRegistry = decompressorRegistry;
        }

        @Override
        protected ClientCall.ClientStreamListener listener() {

            return listener;
        }

        @Override
        public void deframerClosed(boolean hasPartialMessage) {

            deframerClosed = true;
            if (deframerClosedTask != null) {
                deframerClosedTask.run();
                deframerClosedTask = null;
            }
        }

        /**
         * Called by transport implementations when they receive headers.
         *
         * @param headers the parsed headers
         */
        protected void inboundHeadersReceived(HttpHeaders headers) {

            String streamEncoding = headers.get("content-encoding");
            if (streamEncoding != null) {
                deframeFailed(
                        Status.Code.INTERNAL.toStatus()
                                .withDescription(
                                        String.format("Full stream decompressor for %s is not supported",
                                                streamEncoding)).asRuntimeException());
                return;
            }

            String messageEncoding = headers.get("grpc-encoding");
            if (messageEncoding != null) {
                Decompressor decompressor = decompressorRegistry.lookupDecompressor(messageEncoding);
                if (decompressor == null) {
                    deframeFailed(
                            Status.Code.INTERNAL.toStatus()
                                    .withDescription(String.format("Can't find decompressor for %s", messageEncoding))
                                    .asRuntimeException());
                    return;
                } else if (decompressor != Codec.Identity.NONE) {
                    setDecompressor(decompressor);
                }
            }

            listener().headersRead(headers);
        }

        /**
         * Processes the contents of a received data frame from the server.
         *
         * @param httpContent the received data frame. Its ownership is transferred to this method.
         */
        public void inboundDataReceived(HttpContent httpContent) {

            try {
                deframe(httpContent);
            } finally {
                if (httpContent.refCnt() != 0) {
                    httpContent.release();
                }
            }
        }

        public final void transportReportStatus(final Status status, boolean stopDelivery,
                                                final HttpHeaders trailers) {

            if (statusReported && !stopDelivery) {
                return;
            }
            statusReported = true;

            if (deframerClosed) {
                deframerClosedTask = null;
                closeListener(status, trailers);
            } else {
                deframerClosedTask = () -> closeListener(status, trailers);
                closeDeframer(stopDelivery);
            }
        }

        /**
         * Closes the listener if not previously closed.
         *
         * @throws IllegalStateException if the call has not yet been started.
         */
        private void closeListener(Status status, HttpHeaders trailers) {

            if (!listenerClosed) {
                listenerClosed = true;
                listener().closed(status, trailers);
            }
        }

        @Override
        public void deframeFailed(Throwable cause) {
            transportReportStatus(Status.fromThrowable(cause), true, new DefaultHttpHeaders());;
        }
    }

}
