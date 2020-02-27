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

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.InputStream;
import java.util.Map;

import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_ENCODING;
import static org.ballerinalang.net.grpc.GrpcConstants.TO_HEADER;

/**
 * Class that represents an gRPC inbound message.
 *
 * @since 0.980.0
 */
public class InboundMessage {

    private final HttpCarbonMessage httpCarbonMessage;

    InboundMessage(HttpCarbonMessage httpCarbonMessage) {
        this.httpCarbonMessage = httpCarbonMessage;
    }

    /**
     * @return true if the request does not have body content.
     */
    public boolean isEmpty() {
        return httpCarbonMessage.isEmpty();
    }

    /**
     * @return map of headers of the HTTP request.
     */
    public HttpHeaders getHeaders() {
        return httpCarbonMessage.getHeaders();
    }

    /**
     * Get an HTTP header of the HTTP request.
     *
     * @param key name of the header
     * @return value of the header
     */
    public String getHeader(String key) {
        return httpCarbonMessage.getHeader(key);
    }

    /**
     * Set a property in the underlining Carbon Message.
     *
     * @param key property key
     * @return value of the property key
     */
    public Object getProperty(String key) {
        return httpCarbonMessage.getProperty(key);
    }

    /**
     * @return property map of the underlining CarbonMessage.
     */
    public Map<String, Object> getProperties() {
        return httpCarbonMessage.getProperties();
    }

    /**
     * Set a property in the underlining Carbon Message.
     *
     * @param key   property key
     * @param value property value
     */
    public void setProperty(String key, Object value) {
        httpCarbonMessage.setProperty(key, value);
    }

    /**
     * @return URL of the request.
     */
    public String getPath() {
        return (String) httpCarbonMessage.getProperty(TO_HEADER);
    }

    public int getStatus() {
        return  httpCarbonMessage.getHttpStatusCode();
    }

    /**
     * @return HTTP method of the request.
     */
    public String getHttpMethod() {
        return httpCarbonMessage.getHttpMethod();
    }

    /**
     * Get underlying HttpCarbonMessage.
     *
     * @return HttpCarbonMessage instance of the InboundMessage
     */
    HttpCarbonMessage getHttpCarbonMessage() {
        return httpCarbonMessage;
    }

    /**
     * Method use to send the response to the caller.
     *
     * @param carbonMessage OutboundMessage message
     * @return true if no errors found, else otherwise
     * @throws ServerConnectorException server connector exception.
     */
    public boolean respond(HttpCarbonMessage carbonMessage) throws ServerConnectorException {
        HttpResponseFuture statusFuture = httpCarbonMessage.respond(carbonMessage);
        return statusFuture.getStatus().getCause() == null;
    }

    public Decompressor getMessageDecompressor() {
        String contentEncodingHeader = httpCarbonMessage.getHeader(MESSAGE_ENCODING);
        if (contentEncodingHeader != null) {
            httpCarbonMessage.removeHeader(HttpHeaderNames.CONTENT_ENCODING.toString());
            return DecompressorRegistry.getDefaultInstance().lookupDecompressor(contentEncodingHeader);
        }
        return null;
    }

    /**
     * Inbound Message state listener.
     *
     * <p>
     * Referenced from grpc-java implementation.
     *
     */
    public abstract static class InboundStateListener implements MessageDeframer.Listener {

        private MessageDeframer deframer;

        InboundStateListener(int maxMessageSize) {
            deframer = new MessageDeframer(
                    this,
                    Codec.Identity.NONE,
                    maxMessageSize);
        }

        /**
         * Override this method to provide a stream listener.
         *
         * @return stream listener
         */
        protected abstract StreamListener listener();

        public void messagesAvailable(InputStream inputStream) {
            listener().messagesAvailable(inputStream);
        }

        /**
         * Closes the deframer and frees any resources. After this method is called, additional calls
         * will have no effect.
         * <p>
         * <p>When {@code stopDelivery} is false, the deframer will wait to close until any already
         * queued messages have been delivered.
         * <p>
         *
         * @param stopDelivery interrupt pending deliveries and close immediately
         */
        final void closeDeframer(boolean stopDelivery) {
            if (stopDelivery) {
                deframer.close();
            } else {
                deframer.closeWhenComplete();
            }
        }

        /**
         * Called to parse a received frame and attempt delivery of any completed messages. Must be
         * called from the transport thread.
         */
        final void deframe(final HttpContent httpContent) {
            try {
                deframer.deframe(httpContent);
            } catch (Exception ex) {
                deframeFailed(ex);
            }
        }

        protected final void setDecompressor(Decompressor decompressor) {
            deframer.setDecompressor(decompressor);
        }

    }

}
