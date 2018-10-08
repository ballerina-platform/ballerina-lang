/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.transport.http.netty.contractimpl.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.ConfigurationException;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contractimpl.common.ssl.SSLConfig;
import org.wso2.transport.http.netty.contractimpl.common.ssl.SSLHandlerFactory;
import org.wso2.transport.http.netty.contractimpl.listener.SourceHandler;
import org.wso2.transport.http.netty.contractimpl.sender.CertificateValidationHandler;
import org.wso2.transport.http.netty.contractimpl.sender.OCSPStaplingHandler;
import org.wso2.transport.http.netty.message.DefaultListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonRequest;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;
import org.wso2.transport.http.netty.message.Listener;
import org.wso2.transport.http.netty.message.PooledDataStreamerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;

import static org.wso2.transport.http.netty.contract.Constants.COLON;
import static org.wso2.transport.http.netty.contract.Constants.HEADER_VAL_100_CONTINUE;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_HOST;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_PORT;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_SCHEME;
import static org.wso2.transport.http.netty.contract.Constants.IS_PROXY_ENABLED;
import static org.wso2.transport.http.netty.contract.Constants.PROTOCOL;
import static org.wso2.transport.http.netty.contract.Constants
        .REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_HEADERS;
import static org.wso2.transport.http.netty.contract.Constants.TO;
import static org.wso2.transport.http.netty.contract.Constants.URL_AUTHORITY;

/**
 * Includes utility methods for creating http requests and responses and their related properties.
 */
public class Util {

    private Util() {
        //Hides implicit public constructor.
    }

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    private static String getStringValue(HttpCarbonMessage msg, String key, String defaultValue) {
        String value = (String) msg.getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    private static int getIntValue(HttpCarbonMessage msg, String key, int defaultValue) {
        Integer value = (Integer) msg.getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    public static HttpResponse createHttpResponse(HttpCarbonMessage outboundResponseMsg, String inboundReqHttpVersion,
                                                  String serverName, boolean keepAlive) {

        HttpVersion httpVersion = new HttpVersion(Constants.HTTP_VERSION_PREFIX + inboundReqHttpVersion, true);
        HttpResponseStatus httpResponseStatus = getHttpResponseStatus(outboundResponseMsg);
        HttpResponse outboundNettyResponse = new DefaultHttpResponse(httpVersion, httpResponseStatus, false);

        setOutboundRespHeaders(outboundResponseMsg, inboundReqHttpVersion, serverName, keepAlive,
                outboundNettyResponse);

        return outboundNettyResponse;
    }

    public static HttpResponse createFullHttpResponse(HttpCarbonMessage outboundResponseMsg,
            String inboundReqHttpVersion, String serverName, boolean keepAlive, ByteBuf fullContent) {

        HttpVersion httpVersion = new HttpVersion(Constants.HTTP_VERSION_PREFIX + inboundReqHttpVersion, true);
        HttpResponseStatus httpResponseStatus = getHttpResponseStatus(outboundResponseMsg);
        HttpResponse outboundNettyResponse =
                new DefaultFullHttpResponse(httpVersion, httpResponseStatus, fullContent, false);

        setOutboundRespHeaders(outboundResponseMsg, inboundReqHttpVersion, serverName, keepAlive,
                outboundNettyResponse);

        return outboundNettyResponse;
    }

    private static void setOutboundRespHeaders(HttpCarbonMessage outboundResponseMsg, String inboundReqHttpVersion,
                                               String serverName, boolean keepAlive,
                                               HttpResponse outboundNettyResponse) {
        if (!keepAlive && (Float.valueOf(inboundReqHttpVersion) >= Constants.HTTP_1_1)) {
            outboundResponseMsg.setHeader(HttpHeaderNames.CONNECTION.toString(), Constants.CONNECTION_CLOSE);
        } else if (keepAlive && (Float.valueOf(inboundReqHttpVersion) < Constants.HTTP_1_1)) {
            outboundResponseMsg.setHeader(HttpHeaderNames.CONNECTION.toString(), Constants.CONNECTION_KEEP_ALIVE);
        } else {
            outboundResponseMsg.removeHeader(HttpHeaderNames.CONNECTION.toString());
        }

        if (outboundResponseMsg.getHeader(HttpHeaderNames.SERVER.toString()) == null) {
            outboundResponseMsg.setHeader(HttpHeaderNames.SERVER.toString(), serverName);
        }

        if (outboundResponseMsg.getHeader(HttpHeaderNames.DATE.toString()) == null) {
            outboundResponseMsg.setHeader(HttpHeaderNames.DATE.toString(),
                                          ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        }

        outboundNettyResponse.headers().add(outboundResponseMsg.getHeaders());
    }

    public static HttpResponseStatus getHttpResponseStatus(HttpCarbonMessage msg) {
        int statusCode = Util.getIntValue(msg, Constants.HTTP_STATUS_CODE, 200);
        String reasonPhrase = Util.getStringValue(msg, Constants.HTTP_REASON_PHRASE,
                HttpResponseStatus.valueOf(statusCode).reasonPhrase());
        return new HttpResponseStatus(statusCode, reasonPhrase);
    }

    @SuppressWarnings("unchecked")
    public static HttpRequest createHttpRequest(HttpCarbonMessage outboundRequestMsg) {
        HttpMethod httpMethod = getHttpMethod(outboundRequestMsg);
        HttpVersion httpVersion = getHttpVersion(outboundRequestMsg);
        String requestPath = getRequestPath(outboundRequestMsg);
        HttpRequest outboundNettyRequest = new DefaultHttpRequest(httpVersion, httpMethod,
                (String) outboundRequestMsg.getProperty(TO), false);
        outboundNettyRequest.setMethod(httpMethod);
        outboundNettyRequest.setProtocolVersion(httpVersion);
        outboundNettyRequest.setUri(requestPath);
        outboundNettyRequest.headers().add(outboundRequestMsg.getHeaders());

        return outboundNettyRequest;
    }

    private static String getRequestPath(HttpCarbonMessage outboundRequestMsg) {
        if (outboundRequestMsg.getProperty(TO) == null) {
            outboundRequestMsg.setProperty(TO, "");
        }
        // Return absolute url if proxy is enabled
        if (outboundRequestMsg.getProperty(IS_PROXY_ENABLED) != null && (boolean) outboundRequestMsg
                .getProperty(IS_PROXY_ENABLED) && outboundRequestMsg.getProperty(PROTOCOL).equals(HTTP_SCHEME)) {
            return outboundRequestMsg.getProperty(PROTOCOL) + URL_AUTHORITY
                    + outboundRequestMsg.getProperty(HTTP_HOST) + COLON
                    + outboundRequestMsg.getProperty(HTTP_PORT)
                    + outboundRequestMsg.getProperty(TO);
        }
        return (String) outboundRequestMsg.getProperty(TO);
    }

    private static HttpVersion getHttpVersion(HttpCarbonMessage outboundRequestMsg) {
        HttpVersion httpVersion;
        if (null != outboundRequestMsg.getProperty(Constants.HTTP_VERSION)) {
            httpVersion = new HttpVersion(Constants.HTTP_VERSION_PREFIX
                    + outboundRequestMsg.getProperty(Constants.HTTP_VERSION), true);
        } else {
            httpVersion = new HttpVersion(Constants.DEFAULT_VERSION_HTTP_1_1, true);
        }
        return httpVersion;
    }

    private static HttpMethod getHttpMethod(HttpCarbonMessage outboundRequestMsg) {
        HttpMethod httpMethod;
        if (null != outboundRequestMsg.getProperty(Constants.HTTP_METHOD)) {
            httpMethod = new HttpMethod((String) outboundRequestMsg.getProperty(Constants.HTTP_METHOD));
        } else {
            httpMethod = new HttpMethod(Constants.HTTP_POST_METHOD);
        }
        return httpMethod;
    }

    public static void setupChunkedRequest(HttpCarbonMessage httpOutboundRequest) {
        httpOutboundRequest.removeHeader(HttpHeaderNames.CONTENT_LENGTH.toString());
        setTransferEncodingHeader(httpOutboundRequest);
    }

    /**
     * Creates a {@link HttpRequest} using a {@link Http2Headers} received over a particular HTTP/2 stream.
     *
     * @param http2Headers the Http2Headers received over a HTTP/2 stream
     * @param streamId     the stream id
     * @return the HttpRequest formed using the HttpHeaders
     * @throws Http2Exception if an error occurs while converting headers from HTTP/2 to HTTP
     */
    public static HttpRequest createHttpRequestFromHttp2Headers(Http2Headers http2Headers, int streamId)
            throws Http2Exception {
        String method = Constants.HTTP_GET_METHOD;
        if (http2Headers.method() != null) {
            method = http2Headers.getAndRemove(Constants.HTTP2_METHOD).toString();
        }
        String path = Constants.DEFAULT_BASE_PATH;
        if (http2Headers.path() != null) {
            path = http2Headers.getAndRemove(Constants.HTTP2_PATH).toString();
        }
        // Remove PseudoHeaderNames from headers
        http2Headers.getAndRemove(Constants.HTTP2_AUTHORITY);
        http2Headers.getAndRemove(Constants.HTTP2_SCHEME);
        HttpVersion version = new HttpVersion(Constants.HTTP_VERSION_2_0, true);

        // Construct new HTTP Carbon Request
        HttpRequest httpRequest = new DefaultHttpRequest(version, HttpMethod.valueOf(method), path);
        // Convert Http2Headers to HttpHeaders
        HttpConversionUtil.addHttp2ToHttpHeaders(
                streamId, http2Headers, httpRequest.headers(), version, false, true);
        return httpRequest;
    }

    public static void setupContentLengthRequest(HttpCarbonMessage httpOutboundRequest, long contentLength) {
        httpOutboundRequest.removeHeader(HttpHeaderNames.TRANSFER_ENCODING.toString());
        httpOutboundRequest.removeHeader(HttpHeaderNames.CONTENT_LENGTH.toString());
        if (httpOutboundRequest.getHeader(HttpHeaderNames.CONTENT_LENGTH.toString()) == null) {
            httpOutboundRequest.setHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(contentLength));
        }
    }

    private static void setTransferEncodingHeader(HttpCarbonMessage httpOutboundRequest) {
        if (httpOutboundRequest.getHeader(HttpHeaderNames.TRANSFER_ENCODING.toString()) == null) {
            httpOutboundRequest.setHeader(HttpHeaderNames.TRANSFER_ENCODING.toString(), Constants.CHUNKED);
        }
    }

    public static boolean isEntityBodyAllowed(String method) {
        return method.equals(Constants.HTTP_POST_METHOD) || method.equals(Constants.HTTP_PUT_METHOD)
                || method.equals(Constants.HTTP_PATCH_METHOD) || method.equals(Constants.HTTP_DELETE_METHOD);
    }

    /**
     * Returns the status of chunking compatibility with http version.
     *
     * @param httpVersion http version string.
     * @return  boolean value of status.
     */
    public static boolean isVersionCompatibleForChunking(String httpVersion) {
        return Float.valueOf(httpVersion) >= Constants.HTTP_1_1;
    }

    /**
     * Returns whether to enforce chunking on HTTP 1.0 requests.
     *
     * @param chunkConfig Chunking configuration.
     * @param httpVersion http version string.
     * @return true if chunking should be enforced else false.
     */
    public static boolean shouldEnforceChunkingforHttpOneZero(ChunkConfig chunkConfig, String httpVersion) {
        return chunkConfig == ChunkConfig.ALWAYS && Float.valueOf(httpVersion) >= Constants.HTTP_1_0;
    }

    /**
     * Configure outbound HTTP pipeline for SSL configuration.
     *
     * @param socketChannel Socket channel of outbound connection
     * @param sslConfig {@link SSLConfig}
     * @param host host of the connection
     * @param port port of the connection
     * @throws SSLException if any error occurs in the SSL connection
     */
    public static void configureHttpPipelineForSSL(SocketChannel socketChannel, String host, int port,
            SSLConfig sslConfig) throws SSLException {
        LOG.debug("adding ssl handler");
        ChannelPipeline pipeline = socketChannel.pipeline();
        SSLHandlerFactory sslHandlerFactory = new SSLHandlerFactory(sslConfig);
        if (sslConfig.isOcspStaplingEnabled()) {
            sslHandlerFactory.createSSLContextFromKeystores();
            ReferenceCountedOpenSslContext referenceCountedOpenSslContext = sslHandlerFactory
                    .buildClientReferenceCountedOpenSslContext();

            if (referenceCountedOpenSslContext != null) {
                SslHandler sslHandler = referenceCountedOpenSslContext.newHandler(socketChannel.alloc());
                ReferenceCountedOpenSslEngine engine = (ReferenceCountedOpenSslEngine) sslHandler.engine();
                socketChannel.pipeline().addLast(sslHandler);
                socketChannel.pipeline().addLast(new OCSPStaplingHandler(engine));
            }
        } else {
            SSLEngine sslEngine;
            if (sslConfig.getTrustStore() != null) {
                sslHandlerFactory.createSSLContextFromKeystores();
                sslEngine = instantiateAndConfigSSL(sslConfig, host, port, sslConfig.isHostNameVerificationEnabled(),
                        sslHandlerFactory);
            } else {
                sslEngine = getSslEngineForCerts(socketChannel, host, port, sslConfig, sslHandlerFactory);
            }
            pipeline.addLast(Constants.SSL_HANDLER, new SslHandler(sslEngine));
            if (sslConfig.isValidateCertEnabled()) {
                pipeline.addLast(Constants.HTTP_CERT_VALIDATION_HANDLER, new CertificateValidationHandler(
                        sslEngine, sslConfig.getCacheValidityPeriod(), sslConfig.getCacheSize()));
            }
        }
    }

    private static SSLEngine getSslEngineForCerts(SocketChannel socketChannel, String host, int port,
            SSLConfig sslConfig, SSLHandlerFactory sslHandlerFactory) throws SSLException {
        SslContext sslContext = sslHandlerFactory.createHttpTLSContextForClient();
        SslHandler sslHandler = sslContext.newHandler(socketChannel.alloc(), host, port);
        SSLEngine sslEngine = sslHandler.engine();
        sslHandlerFactory.addCommonConfigs(sslEngine);
        sslHandlerFactory.setSNIServerNames(sslEngine, host);
        if (sslConfig.isHostNameVerificationEnabled()) {
            setHostNameVerfication(sslEngine);
        }
        return sslEngine;
    }

    /**
     * Set configurations to create ssl engine.
     *
     * @param sslConfig ssl related configurations
     * @param host host of the connection
     * @param port port of the connection
     * @param hostNameVerificationEnabled true if host name verification is enabled
     * @param sslHandlerFactory an instance of sslHandlerFactory
     * @return ssl engine
     */
    private static SSLEngine instantiateAndConfigSSL(SSLConfig sslConfig, String host, int port,
            boolean hostNameVerificationEnabled, SSLHandlerFactory sslHandlerFactory) {
        // set the pipeline factory, which creates the pipeline for each newly created channels
        SSLEngine sslEngine = null;
        if (sslConfig != null) {
            sslEngine = sslHandlerFactory.buildClientSSLEngine(host, port);
            sslEngine.setUseClientMode(true);
            sslHandlerFactory.setSNIServerNames(sslEngine, host);
            if (hostNameVerificationEnabled) {
                sslHandlerFactory.setHostNameVerfication(sslEngine);
            }
        }
        return sslEngine;
    }

    /**
     * Get integer type property value from a property map.
     * <p>
     * If {@code properties} is null or property value is null, default value is returned
     *
     * @param properties map of properties
     * @param key        property name
     * @param defaultVal default value of the property
     * @return integer value of the property,
     */
    public static int getIntProperty(Map<String, Object> properties, String key, int defaultVal) {

        if (properties == null) {
            return defaultVal;
        }

        Object propertyVal = properties.get(key);

        if (propertyVal == null) {
            return defaultVal;
        }

        if (!(propertyVal instanceof Integer)) {
            throw new IllegalArgumentException("Property : " + key + " must be an integer");
        }

        return (Integer) propertyVal;
    }


    /**
     * Get string type property value from a property map.
     * <p>
     * If {@code properties} is null or property value is null, default value is returned
     *
     * @param properties map of properties
     * @param key        property name
     * @param defaultVal default value of the property
     * @return integer value of the property,
     */
    public static String getStringProperty(Map<String, Object> properties, String key, String defaultVal) {

        if (properties == null) {
            return defaultVal;
        }

        Object propertyVal = properties.get(key);

        if (propertyVal == null) {
            return defaultVal;
        }

        if (!(propertyVal instanceof String)) {
            throw new IllegalArgumentException("Property : " + key + " must be a string");
        }

        return (String) propertyVal;
    }


    /**
     * Get boolean type property value from a property map.
     * <p>
     * If {@code properties} is null or property value is null, default value is returned
     *
     * @param properties map of properties
     * @param key        property name
     * @param defaultVal default value of the property
     * @return integer value of the property,
     */
    public static Boolean getBooleanProperty(Map<String, Object> properties, String key, boolean defaultVal) {

        if (properties == null) {
            return defaultVal;
        }

        Object propertyVal = properties.get(key);

        if (propertyVal == null) {
            return defaultVal;
        }

        if (!(propertyVal instanceof Boolean)) {
            throw new IllegalArgumentException("Property : " + key + " must be a boolean");
        }

        return (Boolean) propertyVal;
    }

    /**
     * Get long type property value from a property map.
     * <p>
     * If {@code properties} is null or property value is null, default value is returned
     *
     * @param properties map of properties
     * @param key        property name
     * @param defaultVal default value of the property
     * @return integer value of the property,
     */
    public static Long getLongProperty(Map<String, Object> properties, String key, long defaultVal) {

        if (properties == null) {
            return defaultVal;
        }

        Object propertyVal = properties.get(key);

        if (propertyVal == null) {
            return defaultVal;
        }

        if (!(propertyVal instanceof Long)) {
            throw new IllegalArgumentException("Property : " + key + " must be a long");
        }

        return (Long) propertyVal;
    }

    //TODO Below code segment is directly copied from kernel. Once kernel Utils been moved as a separate dependency
    //Need to remove below part and use that.
    private static final Pattern varPattern = Pattern.compile("\\$\\{([^}]*)}");

    /**
     * Replace system property holders in the property values.
     * e.g. Replace ${carbon.home} with value of the carbon.home system property.
     *
     * @param value string value to substitute
     * @return String substituted string
     */
    public static String substituteVariables(String value) {
        Matcher matcher = varPattern.matcher(value);
        boolean found = matcher.find();
        if (!found) {
            return value;
        }
        StringBuffer sb = new StringBuffer();
        do {
            String sysPropKey = matcher.group(1);
            String sysPropValue = getSystemVariableValue(sysPropKey, null);
            if (sysPropValue == null || sysPropValue.length() == 0) {
                throw new RuntimeException("System property " + sysPropKey + " is not specified");
            }
            // Due to reported bug under CARBON-14746
            sysPropValue = sysPropValue.replace("\\", "\\\\");
            matcher.appendReplacement(sb, sysPropValue);
        } while (matcher.find());
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * A utility which allows reading variables from the environment or System properties.
     * If the variable in available in the environment as well as a System property, the System property takes
     * precedence.
     *
     * @param variableName System/environment variable name
     * @param defaultValue default value to be returned if the specified system variable is not specified.
     * @return value of the system/environment variable
     */
    private static String getSystemVariableValue(String variableName, String defaultValue) {
        String value;
        if (System.getProperty(variableName) != null) {
            value = System.getProperty(variableName);
        } else if (System.getenv(variableName) != null) {
            value = System.getenv(variableName);
        } else {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Create ID for server connector.
     *
     * @param host host of the channel.
     * @param port port of the channel.
     * @return constructed ID for server connector.
     */
    public static String createServerConnectorID(String host, int port) {
        return host + ":" + port;
    }

    /**
     * Reset channel attributes.
     *
     * @param ctx Channel handler context
     */
    public static void resetChannelAttributes(ChannelHandlerContext ctx) {
        ctx.channel().attr(Constants.RESPONSE_FUTURE_OF_ORIGINAL_CHANNEL).set(null);
        ctx.channel().attr(Constants.ORIGINAL_REQUEST).set(null);
        ctx.channel().attr(Constants.REDIRECT_COUNT).set(null);
        ctx.channel().attr(Constants.RESOLVED_REQUESTED_URI_ATTR).set(null);
        ctx.channel().attr(Constants.ORIGINAL_CHANNEL_START_TIME).set(null);
        ctx.channel().attr(Constants.ORIGINAL_CHANNEL_TIMEOUT).set(null);
    }

    /**
     * Check if a given content is last httpContent.
     *
     * @param httpContent new content.
     * @return true or false.
     */
    public static boolean isLastHttpContent(HttpContent httpContent) {
        return httpContent instanceof LastHttpContent;
    }

    /**
     * Send back no entity body response and close the connection. This function is mostly used
     * when we send back error messages.
     *
     * @param ctx connection
     * @param status response status
     * @param httpVersion of the response
     * @param serverName server name
     */
    public static void sendAndCloseNoEntityBodyResp(ChannelHandlerContext ctx, HttpResponseStatus status,
            HttpVersion httpVersion, String serverName) {
        HttpResponse outboundResponse = new DefaultHttpResponse(httpVersion, status);
        outboundResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        outboundResponse.headers().set(HttpHeaderNames.CONNECTION.toString(), Constants.CONNECTION_CLOSE);
        outboundResponse.headers().set(HttpHeaderNames.SERVER.toString(), serverName);
        ChannelFuture outboundRespFuture = ctx.channel().writeAndFlush(outboundResponse);
        outboundRespFuture.addListener(
                (ChannelFutureListener) channelFuture -> LOG.warn("Failed to send {}", status.reasonPhrase()));
        ctx.channel().close();
    }

    /**
     * Checks for status of the response write operation.
     *
     * @param inboundRequestMsg        request message received from the client
     * @param outboundRespStatusFuture the future of outbound response write operation
     * @param channelFuture            the channel future related to response write operation
     */
    public static void checkForResponseWriteStatus(HttpCarbonMessage inboundRequestMsg,
                                           HttpResponseFuture outboundRespStatusFuture, ChannelFuture channelFuture) {
        channelFuture.addListener(writeOperationPromise -> {
            Throwable throwable = writeOperationPromise.cause();
            if (throwable != null) {
                if (throwable instanceof ClosedChannelException) {
                    throwable = new IOException(REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_HEADERS);
                }
                outboundRespStatusFuture.notifyHttpListener(throwable);
            } else {
                outboundRespStatusFuture.notifyHttpListener(inboundRequestMsg);
            }
        });
    }

    /**
     * Adds a listener to notify the outbound response future if an error occurs while writing the response message.
     *
     * @param outboundRespStatusFuture the future of outbound response write operation
     * @param channelFuture            the channel future related to response write operation
     */
    public static void addResponseWriteFailureListener(HttpResponseFuture outboundRespStatusFuture,
                                                       ChannelFuture channelFuture) {
        channelFuture.addListener(writeOperationPromise -> {
            Throwable throwable = writeOperationPromise.cause();
            if (throwable != null) {
                if (throwable instanceof ClosedChannelException) {
                    throwable = new IOException(REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_HEADERS);
                }
                outboundRespStatusFuture.notifyHttpListener(throwable);
            }
        });
    }

    /**
     * Creates HTTP carbon message.
     *
     * @param httpMessage HTTP message
     * @param ctx Channel handler context
     * @return HttpCarbonMessage
     */
    public static HttpCarbonMessage createHTTPCarbonMessage(HttpMessage httpMessage, ChannelHandlerContext ctx) {
        Listener contentListener = new DefaultListener(ctx);
        return new HttpCarbonMessage(httpMessage, contentListener);
    }

    /**
     * Removes handlers from the pipeline if they are present.
     *
     * @param pipeline     the channel pipeline
     * @param handlerNames names of the handlers to be removed
     */
    public static void safelyRemoveHandlers(ChannelPipeline pipeline, String... handlerNames) {
        for (String name : handlerNames) {
            if (pipeline.get(name) != null) {
                pipeline.remove(name);
            } else {
                LOG.debug("Trying to remove not engaged {} handler from the pipeline", name);
            }
        }
    }

    /**
     * Create a HttpCarbonMessage using the netty inbound http request.
     * @param httpRequestHeaders of inbound request
     * @param ctx of the inbound request
     * @param sourceHandler instance which handled the particular request
     * @return HttpCarbon message
     */
    public static HttpCarbonMessage createInboundReqCarbonMsg(HttpRequest httpRequestHeaders,
            ChannelHandlerContext ctx, SourceHandler sourceHandler) {

        HttpCarbonMessage inboundRequestMsg =
                new HttpCarbonRequest(httpRequestHeaders, new DefaultListener(ctx));
        inboundRequestMsg.setProperty(Constants.POOLED_BYTE_BUFFER_FACTORY, new PooledDataStreamerFactory(ctx.alloc()));

        inboundRequestMsg.setProperty(Constants.CHNL_HNDLR_CTX, ctx);
        inboundRequestMsg.setProperty(Constants.SRC_HANDLER, sourceHandler);
        HttpVersion protocolVersion = httpRequestHeaders.protocolVersion();
        inboundRequestMsg.setProperty(Constants.HTTP_VERSION,
                protocolVersion.majorVersion() + "." + protocolVersion.minorVersion());
        inboundRequestMsg.setProperty(Constants.HTTP_METHOD, httpRequestHeaders.method().name());
        InetSocketAddress localAddress = null;

        //This check was added because in case of netty embedded channel, this could be of type 'EmbeddedSocketAddress'.
        if (ctx.channel().localAddress() instanceof InetSocketAddress) {
            localAddress = (InetSocketAddress) ctx.channel().localAddress();
        }
        inboundRequestMsg.setProperty(Constants.LISTENER_PORT, localAddress != null ? localAddress.getPort() : null);
        inboundRequestMsg.setProperty(Constants.LISTENER_INTERFACE_ID, sourceHandler.getInterfaceId());
        inboundRequestMsg.setProperty(Constants.PROTOCOL, Constants.HTTP_SCHEME);

        boolean isSecuredConnection = false;
        if (ctx.channel().pipeline().get(Constants.SSL_HANDLER) != null) {
            isSecuredConnection = true;
        }
        inboundRequestMsg.setProperty(Constants.IS_SECURED_CONNECTION, isSecuredConnection);

        inboundRequestMsg.setProperty(Constants.LOCAL_ADDRESS, ctx.channel().localAddress());
        inboundRequestMsg.setProperty(Constants.REMOTE_ADDRESS, sourceHandler.getRemoteAddress());
        inboundRequestMsg.setProperty(Constants.REQUEST_URL, httpRequestHeaders.uri());
        inboundRequestMsg.setProperty(Constants.TO, httpRequestHeaders.uri());

        return inboundRequestMsg;
    }

    /**
     * Create a HttpCarbonMessage using the netty inbound response message.
     * @param ctx of the inbound response message
     * @param httpResponseHeaders of the inbound response message
     * @param outboundRequestMsg is the correlated outbound request message
     * @return HttpCarbon message
     */
    public static HttpCarbonMessage createInboundRespCarbonMsg(ChannelHandlerContext ctx,
                                                               HttpResponse httpResponseHeaders,
                                                               HttpCarbonMessage outboundRequestMsg) {
        HttpCarbonMessage inboundResponseMsg = new HttpCarbonResponse(httpResponseHeaders, new DefaultListener(ctx));
        inboundResponseMsg.setProperty(Constants.POOLED_BYTE_BUFFER_FACTORY,
                new PooledDataStreamerFactory(ctx.alloc()));

        inboundResponseMsg.setProperty(Constants.DIRECTION, Constants.DIRECTION_RESPONSE);
        inboundResponseMsg.setProperty(Constants.HTTP_STATUS_CODE, httpResponseHeaders.status().code());

        //copy required properties for service chaining from incoming carbon message to the response carbon message
        //copy shared worker pool
        inboundResponseMsg.setProperty(Constants.EXECUTOR_WORKER_POOL, outboundRequestMsg
                .getProperty(Constants.EXECUTOR_WORKER_POOL));

        return inboundResponseMsg;
    }

    /**
     * Check whether a connection should alive or not.
     * @param keepAliveConfig of the connection
     * @param outboundRequestMsg of this particular transaction
     * @return true if the connection should be kept alive
     * @throws ConfigurationException for invalid configurations
     */
    public static boolean isKeepAlive(KeepAliveConfig keepAliveConfig, HttpCarbonMessage outboundRequestMsg)
            throws ConfigurationException {
        switch (keepAliveConfig) {
        case AUTO:
            return Float.valueOf((String) outboundRequestMsg.getProperty(Constants.HTTP_VERSION)) > Constants.HTTP_1_0;
        case ALWAYS:
            return true;
        case NEVER:
            return false;
        default:
            // The execution will never reach here. In case execution reach here means it should be an invalid value
            // for keep-alive configurations.
            throw new ConfigurationException("Invalid keep-alive configuration value : "
                    + keepAliveConfig.toString());
        }
    }

    /**
     * Check whether a particular request is expecting continue.
     * @param inboundRequestMsg in question
     * @return true if the request expects 100-continue response
     */
    public static boolean is100ContinueRequest(HttpCarbonMessage inboundRequestMsg) {
        return HEADER_VAL_100_CONTINUE.equalsIgnoreCase(
                inboundRequestMsg.getHeader(HttpHeaderNames.EXPECT.toString()));
    }

    /**
     * Decide whether the connection should be kept open.
     *
     * @param keepAliveConfig         Represents keepalive configuration
     * @param requestConnectionHeader Connection header of the request
     * @param httpVersion             Represents HTTP version
     * @return A boolean indicating whether to keep the connection open or not
     */
    public static boolean isKeepAliveConnection(KeepAliveConfig keepAliveConfig, String requestConnectionHeader,
                                                String httpVersion) {
        if (keepAliveConfig == null || keepAliveConfig == KeepAliveConfig.AUTO) {
            if (Float.valueOf(httpVersion) <= Constants.HTTP_1_0) {
                return requestConnectionHeader != null && requestConnectionHeader
                        .equalsIgnoreCase(Constants.CONNECTION_KEEP_ALIVE);
            } else {
                return requestConnectionHeader == null || !requestConnectionHeader
                        .equalsIgnoreCase(Constants.CONNECTION_CLOSE);
            }
        } else {
            return keepAliveConfig == KeepAliveConfig.ALWAYS;
        }
    }

    /**
     * Disable host name verification if it is set to false by the user.
     *
     * @param sslEngine ssl engine
     */
    public static void setHostNameVerfication(SSLEngine sslEngine) {
        SSLParameters sslParams = sslEngine.getSSLParameters();
        sslParams.setEndpointIdentificationAlgorithm(Constants.HTTPS_SCHEME);
        sslEngine.setSSLParameters(sslParams);
    }

    /**
     * Use this method to get the {@link OutboundThrottlingHandler} in the pipeline. This requires a
     * {@link ChannelHandlerContext} of a handler in the pipeline.
     *
     * @param channelContext the channelContext which will be used to obtain the {@link OutboundThrottlingHandler}
     *                       in the pipeline.
     */
    public static OutboundThrottlingHandler getOutBoundThrottlingHandler(ChannelHandlerContext channelContext) {
        return (OutboundThrottlingHandler) channelContext.pipeline().get(Constants.OUTBOUND_THROTTLING_HANDLER);

    }
}
