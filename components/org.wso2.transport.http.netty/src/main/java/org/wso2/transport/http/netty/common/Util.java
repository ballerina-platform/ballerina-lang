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

package org.wso2.transport.http.netty.common;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.transport.http.netty.config.Parameter;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Includes utility methods for creating http requests and responses and their related properties.
 */
public class Util {

    private static Logger log = LoggerFactory.getLogger(Util.class);

    private static String getStringValue(HTTPCarbonMessage msg, String key, String defaultValue) {
        String value = (String) msg.getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    private static int getIntValue(HTTPCarbonMessage msg, String key, int defaultValue) {
        Integer value = (Integer) msg.getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    public static HttpResponse createHttpResponse(HTTPCarbonMessage outboundResponseMsg, String inboundReqHttpVersion,
            String serverName, boolean keepAlive) {

        HttpVersion httpVersion = new HttpVersion(Constants.HTTP_VERSION_PREFIX + inboundReqHttpVersion, true);
        HttpResponseStatus httpResponseStatus = getHttpResponseStatus(outboundResponseMsg);
        HttpResponse outboundNettyResponse = new DefaultHttpResponse(httpVersion, httpResponseStatus, false);

        if (!keepAlive && (Float.valueOf(inboundReqHttpVersion) >= Constants.HTTP_1_1)) {
            outboundResponseMsg.setHeader(Constants.HTTP_CONNECTION, Constants.CONNECTION_CLOSE);
        } else {
            outboundResponseMsg.removeHeader(Constants.HTTP_CONNECTION);
        }
        if (keepAlive && (Float.valueOf(inboundReqHttpVersion) < Constants.HTTP_1_1)) {
            outboundResponseMsg.setHeader(Constants.HTTP_CONNECTION, Constants.CONNECTION_KEEP_ALIVE);
        } else {
            outboundResponseMsg.removeHeader(Constants.HTTP_CONNECTION);
        }

        if (outboundResponseMsg.getHeader(Constants.HTTP_SERVER_HEADER) == null) {
            outboundResponseMsg.setHeader(Constants.HTTP_SERVER_HEADER, serverName);
        }

        outboundNettyResponse.headers().add(outboundResponseMsg.getHeaders());

        return outboundNettyResponse;
    }

    private static HttpResponseStatus getHttpResponseStatus(HTTPCarbonMessage msg) {
        int statusCode = Util.getIntValue(msg, Constants.HTTP_STATUS_CODE, 200);
        String reasonPhrase = Util.getStringValue(msg, Constants.HTTP_REASON_PHRASE,
                HttpResponseStatus.valueOf(statusCode).reasonPhrase());
        return new HttpResponseStatus(statusCode, reasonPhrase);
    }

    @SuppressWarnings("unchecked")
    public static HttpRequest createHttpRequest(HTTPCarbonMessage outboundRequestMsg) {
        HttpMethod httpMethod = getHttpMethod(outboundRequestMsg);
        HttpVersion httpVersion = getHttpVersion(outboundRequestMsg);
        String requestPath = getRequestPath(outboundRequestMsg);

        HttpRequest outboundNettyRequest = new DefaultHttpRequest(httpVersion, httpMethod,
                (String) outboundRequestMsg.getProperty(Constants.TO), false);
        outboundNettyRequest.setMethod(httpMethod);
        outboundNettyRequest.setProtocolVersion(httpVersion);
        outboundNettyRequest.setUri(requestPath);

        outboundNettyRequest.headers().add(outboundRequestMsg.getHeaders());

        return outboundNettyRequest;
    }

    private static String getRequestPath(HTTPCarbonMessage outboundRequestMsg) {
        if (outboundRequestMsg.getProperty(Constants.TO) == null) {
            outboundRequestMsg.setProperty(Constants.TO, "");
        }
        return (String) outboundRequestMsg.getProperty(Constants.TO);
    }

    private static HttpVersion getHttpVersion(HTTPCarbonMessage outboundRequestMsg) {
        HttpVersion httpVersion;
        if (null != outboundRequestMsg.getProperty(Constants.HTTP_VERSION)) {
            httpVersion = new HttpVersion(Constants.HTTP_VERSION_PREFIX
                    + outboundRequestMsg.getProperty(Constants.HTTP_VERSION), true);
        } else {
            httpVersion = new HttpVersion(Constants.DEFAULT_VERSION_HTTP_1_1, true);
        }
        return httpVersion;
    }

    private static HttpMethod getHttpMethod(HTTPCarbonMessage outboundRequestMsg) {
        HttpMethod httpMethod;
        if (null != outboundRequestMsg.getProperty(Constants.HTTP_METHOD)) {
            httpMethod = new HttpMethod((String) outboundRequestMsg.getProperty(Constants.HTTP_METHOD));
        } else {
            httpMethod = new HttpMethod(Constants.HTTP_POST_METHOD);
        }
        return httpMethod;
    }

    public static void setupChunkedRequest(HTTPCarbonMessage httpOutboundRequest) {
        httpOutboundRequest.removeHeader(Constants.HTTP_CONTENT_LENGTH);
        setTransferEncodingHeader(httpOutboundRequest);
    }

    public static void setupContentLengthRequest(HTTPCarbonMessage httpOutboundRequest, int contentLength) {
        httpOutboundRequest.removeHeader(Constants.HTTP_TRANSFER_ENCODING);
        if (httpOutboundRequest.getHeader(Constants.HTTP_CONTENT_LENGTH) == null) {
            httpOutboundRequest.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(contentLength));
        }
    }

    private static void setTransferEncodingHeader(HTTPCarbonMessage httpOutboundRequest) {
        if (httpOutboundRequest.getHeader(Constants.HTTP_TRANSFER_ENCODING) == null) {
            httpOutboundRequest.setHeader(Constants.HTTP_TRANSFER_ENCODING, Constants.CHUNKED);
        }
    }

    public static boolean isEntityBodyAllowed(String method) {
        return method.equals(Constants.HTTP_POST_METHOD) || method.equals(Constants.HTTP_PUT_METHOD)
                || method.equals(Constants.HTTP_PATCH_METHOD);
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

    public static SSLConfig getSSLConfigForListener(String certPass, String keyStorePass, String keyStoreFilePath,
            String trustStoreFilePath, String trustStorePass, List<Parameter> parametersList, String verifyClient,
            String sslProtocol, String tlsStoreType) {
        if (certPass == null) {
            certPass = keyStorePass;
        }
        if (keyStoreFilePath == null || keyStorePass == null) {
            throw new IllegalArgumentException("keyStoreFile or keyStorePassword not defined for HTTPS scheme");
        }
        File keyStore = new File(substituteVariables(keyStoreFilePath));
        if (!keyStore.exists()) {
            throw new IllegalArgumentException("KeyStore File " + keyStoreFilePath + " not found");
        }
        SSLConfig sslConfig = new SSLConfig(keyStore, keyStorePass).setCertPass(certPass);
        for (Parameter parameter : parametersList) {
            if (parameter.getName()
                    .equals(Constants.SERVER_SUPPORT_CIPHERS)) {
                sslConfig.setCipherSuites(parameter.getValue());
            } else if (parameter.getName()
                    .equals(Constants.SERVER_SUPPORT_SSL_PROTOCOLS)) {
                sslConfig.setEnableProtocols(parameter.getValue());
            } else if (parameter.getName()
                    .equals(Constants.SERVER_SUPPORTED_SNIMATCHERS)) {
                sslConfig.setSniMatchers(parameter.getValue());
            } else if (parameter.getName()
                    .equals(Constants.SERVER_SUPPORTED_SERVER_NAMES)) {
                sslConfig.setServerNames(parameter.getValue());
            } else if (parameter.getName()
                    .equals(Constants.SERVER_ENABLE_SESSION_CREATION)) {
                sslConfig.setEnableSessionCreation(Boolean.parseBoolean(parameter.getValue()));
            }
        }
        if ("require".equalsIgnoreCase(verifyClient)) {
            sslConfig.setNeedClientAuth(true);
        }

        sslProtocol = sslProtocol != null ? sslProtocol : "TLS";
        sslConfig.setSSLProtocol(sslProtocol);
        tlsStoreType = tlsStoreType != null ? tlsStoreType : "JKS";
        sslConfig.setTLSStoreType(tlsStoreType);

        if (trustStoreFilePath != null) {

            File trustStore = new File(substituteVariables(trustStoreFilePath));

            if (!trustStore.exists()) {
                throw new IllegalArgumentException("trustStore File " + trustStoreFilePath + " not found");
            }
            if (trustStorePass == null) {
                throw new IllegalArgumentException("trustStorePass is not defined for HTTPS scheme");
            }
            sslConfig.setTrustStore(trustStore).setTrustStorePass(trustStorePass);
        }
        return sslConfig;
    }

    public static SSLConfig getSSLConfigForSender(String certPass, String keyStorePass, String keyStoreFilePath,
            String trustStoreFilePath, String trustStorePass, List<Parameter> parametersList, String sslProtocol,
            String tlsStoreType) {

        if (certPass == null) {
            certPass = keyStorePass;
        }
        if (trustStoreFilePath == null || trustStorePass == null) {
            throw new IllegalArgumentException("TrusStoreFile or trustStorePassword not defined for HTTPS scheme");
        }
        SSLConfig sslConfig = new SSLConfig(null, null).setCertPass(null);

        if (keyStoreFilePath != null) {
            File keyStore = new File(substituteVariables(keyStoreFilePath));
            if (!keyStore.exists()) {
                throw new IllegalArgumentException("KeyStore File " + trustStoreFilePath + " not found");
            }
            sslConfig = new SSLConfig(keyStore, keyStorePass).setCertPass(certPass);
        }
        File trustStore = new File(substituteVariables(trustStoreFilePath));

        sslConfig.setTrustStore(trustStore).setTrustStorePass(trustStorePass);
        sslConfig.setClientMode(true);
        sslProtocol = sslProtocol != null ? sslProtocol : "TLS";
        sslConfig.setSSLProtocol(sslProtocol);
        tlsStoreType = tlsStoreType != null ? tlsStoreType : "JKS";
        sslConfig.setTLSStoreType(tlsStoreType);
        if (parametersList != null) {
            for (Parameter parameter : parametersList) {
                String paramName = parameter.getName();
                if (Constants.CLIENT_SUPPORT_CIPHERS.equals(paramName)) {
                    sslConfig.setCipherSuites(parameter.getValue());
                } else if (Constants.CLIENT_SUPPORT_SSL_PROTOCOLS.equals(paramName)) {
                    sslConfig.setEnableProtocols(parameter.getValue());
                } else if (Constants.CLIENT_ENABLE_SESSION_CREATION.equals(paramName)) {
                    sslConfig.setEnableSessionCreation(Boolean.parseBoolean(parameter.getValue()));
                }
            }
        }
        return sslConfig;
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
    private static String substituteVariables(String value) {
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
        outboundResponse.headers().set(Constants.HTTP_CONTENT_LENGTH, 0);
        outboundResponse.headers().set(Constants.HTTP_CONNECTION, Constants.CONNECTION_CLOSE);
        outboundResponse.headers().set(Constants.HTTP_SERVER_HEADER, serverName);
        ChannelFuture outboundRespFuture = ctx.channel().writeAndFlush(outboundResponse);
        outboundRespFuture.addListener(
                (ChannelFutureListener) channelFuture -> log.warn("Failed to send " + status.reasonPhrase()));
        ctx.channel().close();
    }
}
