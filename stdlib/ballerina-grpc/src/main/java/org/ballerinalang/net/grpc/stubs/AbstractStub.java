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

package org.ballerinalang.net.grpc.stubs;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.net.grpc.CallOptions;
import org.ballerinalang.net.grpc.ClientCall;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.OutboundMessage;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.ThreadSafe;

import static org.ballerinalang.runtime.Constants.BALLERINA_VERSION;

/**
 * Common base type for stub implementations. Stub configuration is immutable; changing the
 * configuration returns a new stub with updated configuration. Changing the configuration is cheap
 * and may be done before every RPC.
 * <p>
 * <p>Configuration is stored in {@link CallOptions} and is passed to the {@link HttpClientConnector} when
 * performing an RPC.
 *
 * @param <S> the concrete type of this stub.
 * @since 1.0.0
 */
@ThreadSafe
public abstract class AbstractStub<S extends AbstractStub<S>> {

    private static final Logger logger = Logger.getLogger(AbstractStub.class.getName());

    private final HttpClientConnector connector;
    private final CallOptions callOptions;
    private final Struct endpointConfig;

    private static final String CACHE_BALLERINA_VERSION;

    static {
        CACHE_BALLERINA_VERSION = System.getProperty(BALLERINA_VERSION);
    }

    /**
     * Constructor for use by subclasses, with the default {@code CallOptions}.
     *
     * @param connector the channel that this stub will use to do communications
     * @since 1.0.0
     */
    protected AbstractStub(HttpClientConnector connector, Struct endpointConfig) {

        this(connector, endpointConfig, CallOptions.DEFAULT);
    }

    /**
     * Constructor for use by subclasses, with the default {@code CallOptions}.
     *
     * @param connector   the channel that this stub will use to do communications
     * @param callOptions the runtime call options to be applied to every call on this stub
     * @since 1.0.0
     */
    protected AbstractStub(HttpClientConnector connector, Struct endpointConfig, CallOptions callOptions) {

        this.connector = connector;
        this.callOptions = callOptions;
        this.endpointConfig = endpointConfig;
    }

    /**
     * The underlying connector of the stub.
     *
     * @since 1.0.0
     */
    public final HttpClientConnector getConnector() {

        return connector;
    }

    /**
     * Returns remote endpoint config.
     *
     * @since 1.0.0
     */
    public final Struct getEndpointConfig() {

        return endpointConfig;
    }

    /**
     * The {@code CallOptions} of the stub.
     *
     * @since 1.0.0
     */
    public final CallOptions getCallOptions() {

        return callOptions;
    }

    /**
     * Returns a new stub with the given channel for the provided method configurations.
     *
     * @param connector   the connector that this stub will use to do communications
     * @param callOptions the runtime call options to be applied to every call on this stub
     * @since 1.0.0
     */
    protected abstract S build(HttpClientConnector connector, Struct endpointConfig, CallOptions callOptions);

    /**
     * Sets a custom option to be passed to client interceptors on the channel via the CallOptions parameter.
     *
     * @param key   the option being set
     * @param value the value for the key
     * @since 1.0.0
     */
    public final <T> S withOption(CallOptions.Key<T> key, T value) {

        return build(connector, endpointConfig, callOptions.withOption(key, value));
    }

    protected OutboundMessage createOutboundRequest(HttpHeaders httpHeaders) {

        try {
            HTTPCarbonMessage carbonMessage = MessageUtils.createHttpCarbonMessage(true);
            String urlString = getEndpointConfig().getStringField(GrpcConstants.CLIENT_ENDPOINT_URL);
            URL url = new URL(urlString);
            int port = getOutboundReqPort(url);
            String host = url.getHost();
            carbonMessage.setHeader("scheme", url.getProtocol());
            carbonMessage.setHeader("authority", urlString);

            setOutboundReqProperties(carbonMessage, url, port, host);
            setOutboundReqHeaders(carbonMessage, port, host);

            if (httpHeaders != null) {
                for (Map.Entry<String, String> headerEntry : httpHeaders.entries()) {
                    carbonMessage.setHeader(headerEntry.getKey(), headerEntry.getValue());
                }
            }
            return new OutboundMessage(carbonMessage);
        } catch (MalformedURLException e) {
            throw new BallerinaException("Malformed url specified. " + e.getMessage());
        } catch (Throwable t) {
            throw new BallerinaException("Failed to prepare request. " + t.getMessage());
        }
    }

    private int getOutboundReqPort(URL url) {
        int port = 80;
        if (url.getPort() != -1) {
            port = url.getPort();
        } else if (url.getProtocol().equalsIgnoreCase(HttpConstants.PROTOCOL_HTTPS)) {
            port = 443;
        }
        return port;
    }

    private void setOutboundReqHeaders(HTTPCarbonMessage outboundRequest, int port, String host) {
        io.netty.handler.codec.http.HttpHeaders headers = outboundRequest.getHeaders();
        setHostHeader(host, port, headers);
        setOutboundUserAgent(headers);
        removeConnectionHeader(headers);
    }

    private void setOutboundReqProperties(HTTPCarbonMessage outboundRequest, URL url, int port, String host) {
        outboundRequest.setProperty(Constants.HTTP_HOST, host);
        outboundRequest.setProperty(Constants.HTTP_PORT, port);

        String outboundReqPath = url.getPath();
        outboundRequest.setProperty(HttpConstants.TO, outboundReqPath);

        outboundRequest.setProperty(HttpConstants.PROTOCOL, url.getProtocol());
    }

    private void setHostHeader(String host, int port, io.netty.handler.codec.http.HttpHeaders headers) {
        if (port == 80 || port == 443) {
            headers.set(HttpHeaderNames.HOST, host);
        } else {
            headers.set(HttpHeaderNames.HOST, host + ":" + port);
        }
    }

    private void removeConnectionHeader(io.netty.handler.codec.http.HttpHeaders headers) {
        // Remove existing Connection header
        if (headers.contains(HttpHeaderNames.CONNECTION)) {
            headers.remove(HttpHeaderNames.CONNECTION);
        }
    }

    private void setOutboundUserAgent(HttpHeaders headers) {
        String userAgent;
        if (CACHE_BALLERINA_VERSION != null) {
            userAgent = "ballerina/" + CACHE_BALLERINA_VERSION;
        } else {
            userAgent = "ballerina";
        }

        if (!headers.contains(HttpHeaderNames.USER_AGENT)) { // If User-Agent is not already set from program
            headers.set(HttpHeaderNames.USER_AGENT, userAgent);
        }
    }

    /**
     * Cancels a call, and throws the exception.
     *
     * @param t must be a RuntimeException or Error
     */
    public static RuntimeException cancelThrow(ClientCall<?, ?> call, Throwable t) {

        try {
            call.cancel(null, t);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "RuntimeException encountered while closing call", e);
        }
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else if (t instanceof Error) {
            throw (Error) t;
        }
        // should be impossible
        throw new AssertionError(t);
    }
}
