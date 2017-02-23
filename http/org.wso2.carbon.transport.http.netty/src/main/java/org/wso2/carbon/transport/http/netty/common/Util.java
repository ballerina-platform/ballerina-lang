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

package org.wso2.carbon.transport.http.netty.common;

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.wso2.carbon.kernel.utils.Utils;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.Headers;
import org.wso2.carbon.messaging.MessageDataSource;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.carbon.transport.http.netty.config.Parameter;
import org.wso2.carbon.transport.http.netty.listener.RequestDataHolder;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Includes utility methods for creating http requests and responses and their related properties.
 */
public class Util {

    private static final String DEFAULT_HTTP_METHOD_POST = "POST";
    private static final String DEFAULT_VERSION_HTTP_1_1 = "HTTP/1.1";

    public static Headers getHeaders(HttpMessage message) {
        List<Header> headers = new LinkedList<>();
        if (message.headers() != null) {
            for (Map.Entry<String, String> k : message.headers().entries()) {
                headers.add(new Header(k.getKey(), k.getValue()));
            }
        }
        return new Headers(headers);
    }

    public static void setHeaders(HttpMessage message, Headers headers) {
        HttpHeaders httpHeaders = message.headers();
        for (Header header : headers.getAll()) {
            httpHeaders.add(header.getName(), header.getValue());
        }
    }

    public static String getStringValue(CarbonMessage msg, String key, String defaultValue) {
        String value = (String) msg.getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    public static int getIntValue(CarbonMessage msg, String key, int defaultValue) {
        Integer value = (Integer) msg.getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    public static HttpResponse createHttpResponse(CarbonMessage msg) {
        return createHttpResponse(msg, false);
    }

    @SuppressWarnings("unchecked")
    public static HttpResponse createHttpResponse(CarbonMessage msg, boolean connectionCloseAfterResponse) {
        HttpVersion httpVersion = new HttpVersion(Util.getStringValue(msg, Constants.HTTP_VERSION, HTTP_1_1.text()),
                true);

        int statusCode = Util.getIntValue(msg, Constants.HTTP_STATUS_CODE, 200);

        String reasonPhrase = Util.getStringValue(msg, Constants.HTTP_REASON_PHRASE,
                HttpResponseStatus.valueOf(statusCode).reasonPhrase());

        HttpResponseStatus httpResponseStatus = new HttpResponseStatus(statusCode, reasonPhrase);

        DefaultHttpResponse outgoingResponse = new DefaultHttpResponse(httpVersion, httpResponseStatus, false);

        Headers headers = msg.getHeaders();
        if (connectionCloseAfterResponse) {
            headers.set(Constants.HTTP_CONNECTION, Constants.CONNECTION_CLOSE);
        }

        Util.setHeaders(outgoingResponse, headers);

        return outgoingResponse;
    }

    @SuppressWarnings("unchecked")
    public static HttpRequest createHttpRequest(CarbonMessage msg) {
        HttpMethod httpMethod;
        if (null != msg.getProperty(Constants.HTTP_METHOD)) {
            httpMethod = new HttpMethod((String) msg.getProperty(Constants.HTTP_METHOD));
        } else {
            httpMethod = new HttpMethod(DEFAULT_HTTP_METHOD_POST);
        }
        HttpVersion httpVersion;
        if (null != msg.getProperty(Constants.HTTP_VERSION)) {
            httpVersion = new HttpVersion((String) msg.getProperty(Constants.HTTP_VERSION), true);
        } else {
            httpVersion = new HttpVersion(DEFAULT_VERSION_HTTP_1_1, true);
        }
        if ((String) msg.getProperty(Constants.TO) == null) {
            msg.setProperty(Constants.TO, "/");
        }
        HttpRequest outgoingRequest = new DefaultHttpRequest(httpVersion, httpMethod,
                (String) msg.getProperty(Constants.TO), false);
        Headers headers = msg.getHeaders();
        Util.setHeaders(outgoingRequest, headers);
        return outgoingRequest;
    }

    /**
     * Prepare request message with Transfer-Encoding/Content-Length
     *
     * @param cMsg CarbonMessage
     */
    public static void setupTransferEncodingForRequest(CarbonMessage cMsg) {
        if (cMsg.getHeader(Constants.HTTP_TRANSFER_ENCODING) != null) {
            cMsg.removeHeader(Constants.HTTP_CONTENT_LENGTH);
        } else if (cMsg.isAlreadyRead() || (cMsg.getHeader(Constants.HTTP_CONTENT_LENGTH) == null && !cMsg.isEmpty())) {
            int contentLength = cMsg.getFullMessageLength();
            if (contentLength > 0) {
                cMsg.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(contentLength));
            }
        }
    }

    /**
     * Prepare response message with Transfer-Encoding/Content-Length
     *
     * @param cMsg CarbonMessage
     */
    public static void setupTransferEncodingForResponse(CarbonMessage cMsg, RequestDataHolder requestDataHolder) {

        // 1. Remove Transfer-Encoding and Content-Length as per rfc7230#section-3.3.1
        int statusCode = Util.getIntValue(cMsg, Constants.HTTP_STATUS_CODE, 200);
        String httpMethod = requestDataHolder.getHttpMethod();
        if (statusCode == 204 ||
            statusCode >= 100 && statusCode < 200 ||
            (HttpMethod.CONNECT.name().equals(httpMethod) && statusCode >= 200 && statusCode < 300)) {
            cMsg.removeHeader(Constants.HTTP_TRANSFER_ENCODING);
            cMsg.removeHeader(Constants.HTTP_CONTENT_LENGTH);
            return;
        }

        // 2. Check for transfer encoding header is set in the request
        // As per RFC 2616, Section 4.4, Content-Length must be ignored if Transfer-Encoding header
        // is present and its value not equal to 'identity'
        String requestTransferEncodingHeader = requestDataHolder.getTransferEncodingHeader();
        if (requestTransferEncodingHeader != null &&
            !Constants.HTTP_TRANSFER_ENCODING_IDENTITY.equalsIgnoreCase(requestTransferEncodingHeader)) {
            cMsg.setHeader(Constants.HTTP_TRANSFER_ENCODING, requestTransferEncodingHeader);
            cMsg.removeHeader(Constants.HTTP_CONTENT_LENGTH);
            return;
        }

        // 3. Check for request Content-Length header
        String requestContentLength = requestDataHolder.getContentLengthHeader();
        if (requestContentLength != null &&
            (cMsg.isAlreadyRead() || (cMsg.getHeader(Constants.HTTP_CONTENT_LENGTH) == null && !cMsg.isEmpty()))) {
            int contentLength = cMsg.getFullMessageLength();
            if (contentLength > 0) {
                cMsg.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(contentLength));
            }
            cMsg.removeHeader(Constants.HTTP_TRANSFER_ENCODING);
            return;
        }

        // 4. If request doesn't have Transfer-Encoding or Content-Length header look for response properties
        if (cMsg.getHeader(Constants.HTTP_TRANSFER_ENCODING) != null) {
            cMsg.getHeaders().remove(Constants.HTTP_CONTENT_LENGTH);  // remove Content-Length if present
        } else if (cMsg.isAlreadyRead() || (cMsg.getHeader(Constants.HTTP_CONTENT_LENGTH) == null && !cMsg.isEmpty())) {
            int contentLength = cMsg.getFullMessageLength();
            if (contentLength > 0) {
                cMsg.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(contentLength));
            }
        }

    }

    /**
     * Prepare built message to transfer through the wire.
     * This will populate the message content from the DataSource into the output stream of the carbon message
     *
     * @param cMsg Carbon Message
     */
    public static void prepareBuiltMessageForTransfer(CarbonMessage cMsg) {
        if (cMsg.isAlreadyRead()) {
            MessageDataSource messageDataSource = cMsg.getMessageDataSource();
            if (messageDataSource != null) {
                messageDataSource.serializeData();
                cMsg.setEndOfMsgAdded(true);
            }
        }
    }

    public static SSLConfig getSSLConfigForListener(String certPass, String keyStorePass, String keyStoreFilePath,
            String trustStoreFilePath, String trustStorePass, List<Parameter> parametersList) {
        if (certPass == null) {
            certPass = keyStorePass;
        }
        if (keyStoreFilePath == null || keyStorePass == null) {
            throw new IllegalArgumentException("keyStoreFile or keyStorePass not defined for HTTPS scheme");
        }
        File keyStore = new File(Utils.substituteVariables(keyStoreFilePath));
        if (!keyStore.exists()) {
            throw new IllegalArgumentException("KeyStore File " + keyStoreFilePath + " not found");
        }
        SSLConfig sslConfig = new SSLConfig(keyStore, keyStorePass).setCertPass(certPass);
        for (Parameter parameter : parametersList) {
            if (parameter.getName()
                    .equals(Constants.SERVER_SUPPORT_CIPHERS)) {
                sslConfig.setCipherSuites(parameter.getValue());

            } else if (parameter.getName()
                    .equals(Constants.SERVER_SUPPORT_HTTPS_PROTOCOLS)) {
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
            } else if (parameter.getName()
                    .equals(Constants.SSL_VERIFY_CLIENT)) {
                sslConfig.setNeedClientAuth(Boolean.parseBoolean(parameter.getValue()));
            }
        }
        if (trustStoreFilePath != null) {

            File trustStore = new File(Utils.substituteVariables(trustStoreFilePath));

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
            String trustStoreFilePath, String trustStorePass, List<Parameter> parametersList) {

        if (certPass == null) {
            certPass = keyStorePass;
        }
        if (trustStoreFilePath == null || trustStorePass == null) {
            throw new IllegalArgumentException("TrusStoreFile or trustStorePass not defined for HTTPS scheme");
        }
        SSLConfig sslConfig = new SSLConfig(null, null).setCertPass(null);

        if (keyStoreFilePath != null) {
            File keyStore = new File(Utils.substituteVariables(keyStoreFilePath));
            if (!keyStore.exists()) {
                throw new IllegalArgumentException("KeyStore File " + trustStoreFilePath + " not found");
            }
            sslConfig = new SSLConfig(keyStore, keyStorePass).setCertPass(certPass);
        }
        File trustStore = new File(Utils.substituteVariables(trustStoreFilePath));

        sslConfig.setTrustStore(trustStore).setTrustStorePass(trustStorePass);
        sslConfig.setClientMode(true);
        if (parametersList != null) {
            for (Parameter parameter : parametersList) {
                String paramName = parameter.getName();
                if (Constants.CLIENT_SUPPORT_CIPHERS.equals(paramName)) {
                    sslConfig.setCipherSuites(parameter.getValue());
                } else if (Constants.CLIENT_SUPPORT_HTTPS_PROTOCOLS.equals(paramName)) {
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
    public static String getStringProperty(
            Map<String, Object> properties, String key, String defaultVal) {

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
    public static Boolean getBooleanProperty(
            Map<String, Object> properties, String key, boolean defaultVal) {

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
    public static Long getLongProperty(
            Map<String, Object> properties, String key, long defaultVal) {

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

}
