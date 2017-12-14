/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.net.http.session.Session;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contractimpl.HttpResponseStatusFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.net.http.Constants.MESSAGE_DATA_SOURCE;

/**
 * Utility class providing utility methods.
 */
public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    private static final String METHOD_ACCESSED = "isMethodAccessed";
    private static final String IO_EXCEPTION_OCCURED = "I/O exception occurred";

    public static BValue[] addHeader(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct httpMessageStruct = ((BStruct) abstractNativeFunction.getRefArgument(context, 0));
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));

        String headerName = abstractNativeFunction.getStringArgument(context, 0);
        String headerValue = abstractNativeFunction.getStringArgument(context, 1);

        HttpHeaders httpHeaders = httpCarbonMessage.getHeaders();
        httpHeaders.add(headerName, headerValue);

        if (log.isDebugEnabled()) {
            log.debug("Add " + headerName + " to header with value: " + headerValue);
        }

        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] getBinaryPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BBlob result;
        try {
            BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
            HTTPCarbonMessage httpCarbonMessage = HttpUtil
                    .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));

            if (httpMessageStruct.getNativeData(MESSAGE_DATA_SOURCE) != null) {
                result = (BBlob) httpMessageStruct.getNativeData(MESSAGE_DATA_SOURCE);
            } else {
                result = new BBlob(toByteArray(new HttpMessageDataStreamer(httpCarbonMessage).getInputStream()));
                httpMessageStruct.addNativeData(MESSAGE_DATA_SOURCE, result);
            }
            if (log.isDebugEnabled()) {
                log.debug("Payload in String:" + result.stringValue());
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving string payload from message: " + e.getMessage());
        }
        return abstractNativeFunction.getBValues(result);
    }

    public static BValue[] getHeader(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));

        String headerName = abstractNativeFunction.getStringArgument(context, 0);
        String headerValue = httpCarbonMessage.getHeader(headerName);

        return abstractNativeFunction.getBValues(new BString(headerValue));
    }

    public static BValue[] getJsonPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BJSON result = null;
        try {
            // Accessing First Parameter Value.
            BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
            HTTPCarbonMessage httpCarbonMessage = HttpUtil
                    .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));

            MessageDataSource payload = HttpUtil.getMessageDataSource(httpMessageStruct);
            if (payload != null) {
                if (payload instanceof BJSON) {
                    result = (BJSON) payload;
                } else {
                    // else, build the JSON from the string representation of the payload.
                    result = new BJSON(payload.getMessageAsString());
                }
            } else {
                result = new BJSON(new HttpMessageDataStreamer(httpCarbonMessage).getInputStream());
                result.setOutputStream(new HttpMessageDataStreamer(httpCarbonMessage).getOutputStream());

                addMessageDataSource(httpMessageStruct, result);
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving json payload from message: " + e.getMessage());
        }
        // Setting output value.
        return abstractNativeFunction.getBValues(result);
    }

    public static BValue[] getProperty(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        String propertyName = abstractNativeFunction.getStringArgument(context, 0);

        Object propertyValue = httpCarbonMessage.getProperty(propertyName);

        if (propertyValue == null) {
            return AbstractNativeFunction.VOID_RETURN;
        }

        if (propertyValue instanceof String) {
            return abstractNativeFunction.getBValues(new BString((String) propertyValue));
        } else {
            throw new BallerinaException("Property value is of unknown type : " + propertyValue.getClass().getName());
        }
    }

    public static BValue[] getStringPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BString result;
        try {
            BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
            MessageDataSource messageDataSource = HttpUtil.getMessageDataSource(httpMessageStruct);
            if (messageDataSource != null) {
                result = new BString(messageDataSource.getMessageAsString());
            } else {
                HTTPCarbonMessage httpCarbonMessage = HttpUtil
                        .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
                if (httpCarbonMessage.isEmpty() && httpCarbonMessage.isEndOfMsgAdded()) {
                    return abstractNativeFunction.getBValues(new BString(""));
                }
                String payload = StringUtils.getStringFromInputStream(new HttpMessageDataStreamer(httpCarbonMessage)
                        .getInputStream());
                result = new BString(payload);

                addMessageDataSource(httpMessageStruct, new StringDataSource(payload));
            }
            if (log.isDebugEnabled()) {
                log.debug("Payload in String:" + result.stringValue());
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving string payload from message: " + e.getMessage());
        }
        return abstractNativeFunction.getBValues(result);
    }

    public static BValue[] getXMLPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BXML result;
        try {
            BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);

            MessageDataSource messageDataSource = HttpUtil.getMessageDataSource(httpMessageStruct);
            if (messageDataSource != null) {
                if (messageDataSource instanceof BXML) {
                    // if the payload is already xml, return it as it is.
                    result = (BXML) messageDataSource;
                } else {
                    // else, build the xml from the string representation of the payload.
                    result = XMLUtils.parse(messageDataSource.getMessageAsString());
                }
            } else {
                HTTPCarbonMessage httpCarbonMessage = HttpUtil
                        .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
                result = XMLUtils.parse(new HttpMessageDataStreamer(httpCarbonMessage).getInputStream());

                result.setOutputStream(new HttpMessageDataStreamer(httpCarbonMessage).getOutputStream());
                addMessageDataSource(httpMessageStruct, result);
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving XML payload from message: " + e.getMessage());
        }
        // Setting output value.
        return abstractNativeFunction.getBValues(result);
    }

    private static byte[] toByteArray(InputStream input) throws IOException {
        byte[] buffer = new byte[4096];
        int n1;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (; -1 != (n1 = input.read(buffer)); ) {
            output.write(buffer, 0, n1);
        }
        byte[] bytes = output.toByteArray();
        output.close();
        return bytes;
    }

    public static BValue[] removeAllHeaders(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        httpCarbonMessage.getHeaders().clear();
        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] removeHeader(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        String headerName = abstractNativeFunction.getStringArgument(context, 0);

        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        httpCarbonMessage.removeHeader(headerName);
        if (log.isDebugEnabled()) {
            log.debug("Remove header:" + headerName);
        }
        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setHeader(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        String headerName = abstractNativeFunction.getStringArgument(context, 0);
        String headerValue = abstractNativeFunction.getStringArgument(context, 1);

        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        httpCarbonMessage.setHeader(headerName, headerValue);

        if (log.isDebugEnabled()) {
            log.debug("Set " + headerName + " header with value: " + headerValue);
        }
        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setJsonPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        BJSON payload = (BJSON) abstractNativeFunction.getRefArgument(context, 1);

        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));

        httpCarbonMessage.waitAndReleaseAllEntities();

        payload.setOutputStream(new HttpMessageDataStreamer(httpCarbonMessage).getOutputStream());
        addMessageDataSource(httpMessageStruct, payload);

        httpCarbonMessage.setHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);

        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setProperty(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        String propertyName = abstractNativeFunction.getStringArgument(context, 0);
        String propertyValue = abstractNativeFunction.getStringArgument(context, 1);

        if (propertyName != null && propertyValue != null) {
            HTTPCarbonMessage httpCarbonMessage = HttpUtil
                    .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
            httpCarbonMessage.setProperty(propertyName, propertyValue);
        }
        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setStringPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));

        httpCarbonMessage.waitAndReleaseAllEntities();

        String payload = abstractNativeFunction.getStringArgument(context, 0);
        StringDataSource stringDataSource = new StringDataSource(payload
                , new HttpMessageDataStreamer(httpCarbonMessage).getOutputStream());

        addMessageDataSource(httpMessageStruct, stringDataSource);

        httpCarbonMessage.setHeader(Constants.CONTENT_TYPE, Constants.TEXT_PLAIN);
        if (log.isDebugEnabled()) {
            log.debug("Setting new payload: " + payload);
        }
        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setXMLPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        BXML payload = (BXML) abstractNativeFunction.getRefArgument(context, 1);

        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));

        httpCarbonMessage.waitAndReleaseAllEntities();

        payload.setOutputStream(new HttpMessageDataStreamer(httpCarbonMessage).getOutputStream());

        addMessageDataSource(httpMessageStruct, payload);

        httpCarbonMessage.setHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_XML);

        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] getContentLength(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        int contentLength = -1;
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));

        String lengthStr = httpCarbonMessage.getHeader(Constants.HTTP_CONTENT_LENGTH);
        try {
            contentLength = Integer.parseInt(lengthStr);
        } catch (NumberFormatException e) {
            throw new BallerinaException("Invalid content length");
        }
        return abstractNativeFunction.getBValues(new BInteger(contentLength));
    }

    public static BValue[] setContentLength(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        try {
            BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
            HTTPCarbonMessage httpCarbonMessage = HttpUtil
                    .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
            long contentLength = abstractNativeFunction.getIntArgument(context, 0);
            httpCarbonMessage.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(contentLength));
        } catch (ClassCastException e) {
            throw new BallerinaException("Invalid message or Content-Length");
        }
        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BMap<String, BValue> getParamMap(String payload) throws UnsupportedEncodingException {
        BMap<String, BValue> params = new BMap<>();
        String[] entries = payload.split("&");
        for (String entry : entries) {
            int index = entry.indexOf('=');
            if (index != -1) {
                String name = entry.substring(0, index).trim();
                String value = URLDecoder.decode(entry.substring(index + 1).trim(), "UTF-8");
                if (value.matches("")) {
                    params.put(name, new BString(""));
                    continue;
                }
                params.put(name, new BString(value));
            }
        }
        return params;
    }

    /**
     * Helper method to start pending http server connectors.
     *
     * @throws BallerinaConnectorException
     */
    public static void startPendingHttpConnectors(BallerinaHttpServerConnector httpServerConnector)
            throws BallerinaConnectorException {
        try {
            // Starting up HTTP Server connectors
            HttpConnectionManager.getInstance().startPendingHTTPConnectors(httpServerConnector);
        } catch (ServerConnectorException e) {
            throw new BallerinaConnectorException(e);
        }
    }

    public static BValue[] prepareResponseAndSend(Context context, AbstractNativeFunction abstractNativeFunction
            , HTTPCarbonMessage requestMessage, HTTPCarbonMessage responseMessage,
            MessageDataSource messageDataSource) {
        addHTTPSessionAndCorsHeaders(requestMessage, responseMessage);
        HttpResponseStatusFuture statusFuture = handleResponse(requestMessage, responseMessage);
        if (messageDataSource != null) {
            messageDataSource.serializeData();
        }
        try {
            statusFuture = statusFuture.sync();
        } catch (InterruptedException e) {
            throw new BallerinaException("interrupted sync: " + e.getMessage());
        }
        if (statusFuture.getStatus().getCause() != null) {
            return abstractNativeFunction.getBValues(getServerConnectorError(context
                    , statusFuture.getStatus().getCause()));
        }
        return abstractNativeFunction.VOID_RETURN;
    }

    public static void addHTTPSessionAndCorsHeaders(HTTPCarbonMessage requestMsg, HTTPCarbonMessage responseMsg) {
        Session session = (Session) requestMsg.getProperty(Constants.HTTP_SESSION);
        if (session != null) {
            session.generateSessionHeader(responseMsg);
        }
        //Process CORS if exists.
        if (requestMsg.getHeader(Constants.ORIGIN) != null) {
            CorsHeaderGenerator.process(requestMsg, responseMsg, true);
        }
    }

    public static HttpResponseStatusFuture handleResponse(HTTPCarbonMessage requestMsg, HTTPCarbonMessage responseMsg) {
        HttpResponseStatusFuture responseFuture;
        try {
            responseFuture = requestMsg.respond(responseMsg);
        } catch (org.wso2.transport.http.netty.contract.ServerConnectorException e) {
            throw new BallerinaConnectorException("Error occurred during response", e);
        }
        return responseFuture;
    }

    public static void handleFailure(HTTPCarbonMessage requestMessage, BallerinaConnectorException ex) {
        Object carbonStatusCode = requestMessage.getProperty(Constants.HTTP_STATUS_CODE);
        int statusCode = (carbonStatusCode == null) ? 500 : Integer.parseInt(carbonStatusCode.toString());
        String errorMsg = ex.getMessage();
        log.error(errorMsg);
        ErrorHandlerUtils.printError(ex);
        if (statusCode == 404) {
            handleResponse(requestMessage, createErrorMessage(errorMsg, statusCode));
        } else {
            handleResponse(requestMessage, createErrorMessage("", statusCode));
        }
    }

    public static HTTPCarbonMessage createErrorMessage(String payload, int statusCode) {
        HTTPCarbonMessage response = HttpUtil.createHttpCarbonMessage(false);
        response.waitAndReleaseAllEntities();
        response.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(payload.getBytes())));
        setHttpStatusCodes(payload, statusCode, response);

        return response;
    }

    private static void setHttpStatusCodes(String payload, int statusCode, HTTPCarbonMessage response) {
        HttpHeaders httpHeaders = response.getHeaders();
        httpHeaders.set(org.wso2.transport.http.netty.common.Constants.HTTP_CONTENT_TYPE,
                org.wso2.transport.http.netty.common.Constants.TEXT_PLAIN);

        byte[] errorMessageBytes = payload.getBytes(Charset.defaultCharset());
        httpHeaders.set(org.wso2.transport.http.netty.common.Constants.HTTP_CONTENT_LENGTH,
                (String.valueOf(errorMessageBytes.length)));

        response.setProperty(org.wso2.transport.http.netty.common.Constants.HTTP_STATUS_CODE, statusCode);
    }

    public static BStruct getServerConnectorError(Context context, Throwable throwable) {
        PackageInfo sessionPackageInfo = context.getProgramFile()
                .getPackageInfo(Constants.PROTOCOL_PACKAGE_HTTP);
        StructInfo sessionStructInfo = sessionPackageInfo.getStructInfo(Constants.HTTP_CONNECTOR_ERROR);
        BStruct httpConnectorError = new BStruct(sessionStructInfo.getType());
        if (throwable.getMessage() == null) {
            httpConnectorError.setStringField(0, IO_EXCEPTION_OCCURED);
        } else {
            httpConnectorError.setStringField(0, throwable.getMessage());
        }
        return httpConnectorError;
    }

    public static HTTPCarbonMessage getCarbonMsg(BStruct struct, HTTPCarbonMessage defaultMsg) {
        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) struct
                .getNativeData(Constants.TRANSPORT_MESSAGE);
        if (httpCarbonMessage != null) {
            return httpCarbonMessage;
        }
        addCarbonMsg(struct, defaultMsg);
        return defaultMsg;
    }

    public static void addCarbonMsg(BStruct struct, HTTPCarbonMessage httpCarbonMessage) {
        struct.addNativeData(Constants.TRANSPORT_MESSAGE, httpCarbonMessage);
    }

    public static void addMessageDataSource(BStruct struct, MessageDataSource messageDataSource) {
        struct.addNativeData(MESSAGE_DATA_SOURCE, messageDataSource);
    }

    public static void addRequestResponseFlag(BStruct request, BStruct response) {
        request.addNativeData(Constants.INBOUND_REQUEST, true);
        response.addNativeData(Constants.OUTBOUND_RESPONSE, true);
    }

    /**
     * Extract the listener configurations from the config annotation.
     *
     * @param annotationInfo configuration annotation info.
     * @return the set of {@link ListenerConfiguration} which were extracted from config annotation.
     */
    public static Set<ListenerConfiguration> getDefaultOrDynamicListenerConfig(Annotation annotationInfo) {
        Map<String, Map<String, String>> listenerProp = buildListenerProperties(annotationInfo);

        Set<ListenerConfiguration> listenerConfigurationSet;
        if (listenerProp == null || listenerProp.isEmpty()) {
            listenerConfigurationSet =
                    HttpConnectionManager.getInstance().getDefaultListenerConfiugrationSet();
        } else {
            listenerConfigurationSet = getListenerConfigurationsFrom(listenerProp);
        }
        return listenerConfigurationSet;
    }

    private static String getListenerInterface(Map<String, String> parameters) {
        String host = parameters.get("host") != null ? parameters.get("host") : "0.0.0.0";
        int port = Integer.parseInt(parameters.get("port"));
        return host + ":" + port;
    }

    /**
     * Method to build map of listener property maps given the service annotation attachment.
     * This will first look for the port property and if present then it will get other properties,
     * and create the property map.
     *
     * @param configInfo In which listener configurations are specified.
     * @return listenerConfMap      With required properties
     */
    private static Map<String, Map<String, String>> buildListenerProperties(Annotation configInfo) {
        if (configInfo == null) {
            return null;
        }
        //key - listenerId, value - listener config property map
        Map<String, Map<String, String>> listenerConfMap = new HashMap<>();


        AnnAttrValue hostAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_HOST);
        AnnAttrValue portAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_PORT);
        AnnAttrValue keepAliveAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_KEEP_ALIVE);

        // Retrieve secure port from either http of ws configuration annotation.
        AnnAttrValue httpsPortAttrVal;
        if (configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_HTTPS_PORT) == null) {
            httpsPortAttrVal =
                    configInfo.getAnnAttrValue(org.ballerinalang.net.ws.Constants.ANN_CONFIG_ATTR_WSS_PORT);
        } else {
            httpsPortAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_HTTPS_PORT);
        }

        AnnAttrValue keyStoreFileAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_KEY_STORE_FILE);
        AnnAttrValue keyStorePasswordAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_KEY_STORE_PASS);
        AnnAttrValue certPasswordAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_CERT_PASS);
        AnnAttrValue trustStoreFileAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_TRUST_STORE_FILE);
        AnnAttrValue trustStorePasswordAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_TRUST_STORE_PASS);
        AnnAttrValue sslVerifyClientAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_SSL_VERIFY_CLIENT);
        AnnAttrValue sslEnabledProtocolsAttrVal = configInfo
                .getAnnAttrValue(Constants.ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS);
        AnnAttrValue ciphersAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_CIPHERS);
        AnnAttrValue sslProtocolAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_SSL_PROTOCOL);

        if (portAttrVal != null && portAttrVal.getIntValue() > 0) {
            Map<String, String> httpPropMap = new HashMap<>();
            httpPropMap.put(Constants.ANN_CONFIG_ATTR_PORT, Long.toString(portAttrVal.getIntValue()));
            httpPropMap.put(Constants.ANN_CONFIG_ATTR_SCHEME, Constants.PROTOCOL_HTTP);
            if (hostAttrVal != null && hostAttrVal.getStringValue() != null) {
                httpPropMap.put(Constants.ANN_CONFIG_ATTR_HOST, hostAttrVal.getStringValue());
            } else {
                httpPropMap.put(Constants.ANN_CONFIG_ATTR_HOST, Constants.HTTP_DEFAULT_HOST);
            }
            if (keepAliveAttrVal != null) {
                httpPropMap.put(Constants.ANN_CONFIG_ATTR_KEEP_ALIVE,
                                String.valueOf(keepAliveAttrVal.getBooleanValue()));
            } else {
                httpPropMap.put(Constants.ANN_CONFIG_ATTR_KEEP_ALIVE, Boolean.TRUE.toString());
            }
            listenerConfMap.put(buildInterfaceName(httpPropMap), httpPropMap);
        }

        if (httpsPortAttrVal != null && httpsPortAttrVal.getIntValue() > 0) {
            Map<String, String> httpsPropMap = new HashMap<>();
            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_PORT, Long.toString(httpsPortAttrVal.getIntValue()));
            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_SCHEME, Constants.PROTOCOL_HTTPS);
            if (hostAttrVal != null && hostAttrVal.getStringValue() != null) {
                httpsPropMap.put(Constants.ANN_CONFIG_ATTR_HOST, hostAttrVal.getStringValue());
            } else {
                httpsPropMap.put(Constants.ANN_CONFIG_ATTR_HOST, Constants.HTTP_DEFAULT_HOST);
            }
            if (keyStoreFileAttrVal == null || keyStoreFileAttrVal.getStringValue() == null) {
                //TODO get from language pack, and add location
                throw new BallerinaConnectorException("Keystore location must be provided for secure connection");
            }
            if (keyStorePasswordAttrVal == null || keyStorePasswordAttrVal.getStringValue() == null) {
                //TODO get from language pack, and add location
                throw new BallerinaConnectorException("Keystore password value must be provided for secure connection");
            }
            if (certPasswordAttrVal == null || certPasswordAttrVal.getStringValue() == null) {
                //TODO get from language pack, and add location
                throw new BallerinaConnectorException(
                        "Certificate password value must be provided for secure connection");
            }
            if ((trustStoreFileAttrVal == null || trustStoreFileAttrVal.getStringValue() == null)
                    && sslVerifyClientAttrVal != null) {
                //TODO get from language pack, and add location
                throw new BallerinaException("Truststore location must be provided to enable Mutual SSL");
            }
            if ((trustStorePasswordAttrVal == null || trustStorePasswordAttrVal.getStringValue() == null)
                    && sslVerifyClientAttrVal != null) {
                //TODO get from language pack, and add location
                throw new BallerinaException("Truststore password value must be provided to enable Mutual SSL");
            }

            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_TLS_STORE_TYPE, Constants.PKCS_STORE_TYPE);
            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_KEY_STORE_FILE, keyStoreFileAttrVal.getStringValue());
            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_KEY_STORE_PASS, keyStorePasswordAttrVal.getStringValue());
            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_CERT_PASS, certPasswordAttrVal.getStringValue());
            if (sslVerifyClientAttrVal != null) {
                httpsPropMap.put(Constants.ANN_CONFIG_ATTR_SSL_VERIFY_CLIENT, sslVerifyClientAttrVal.getStringValue());
            }
            if (trustStoreFileAttrVal != null) {
                httpsPropMap.put(Constants.ANN_CONFIG_ATTR_TRUST_STORE_FILE, trustStoreFileAttrVal.getStringValue());
            }
            if (trustStorePasswordAttrVal != null) {
                httpsPropMap
                        .put(Constants.ANN_CONFIG_ATTR_TRUST_STORE_PASS, trustStorePasswordAttrVal.getStringValue());
            }
            if (sslEnabledProtocolsAttrVal != null) {
                httpsPropMap.put(Constants.ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS,
                        sslEnabledProtocolsAttrVal.getStringValue());
            }
            if (ciphersAttrVal != null) {
                httpsPropMap.put(Constants.ANN_CONFIG_ATTR_CIPHERS, ciphersAttrVal.getStringValue());
            }
            if (sslProtocolAttrVal != null) {
                httpsPropMap.put(Constants.ANN_CONFIG_ATTR_SSL_PROTOCOL, sslProtocolAttrVal.getStringValue());
            }
            listenerConfMap.put(buildInterfaceName(httpsPropMap), httpsPropMap);
        }
        return listenerConfMap;
    }

    /**
     * Build interface name using schema and port.
     *
     * @param propMap which has schema and port
     * @return interfaceName
     */
    private static String buildInterfaceName(Map<String, String> propMap) {
        StringBuilder iName = new StringBuilder();
        iName.append(propMap.get(Constants.ANN_CONFIG_ATTR_SCHEME));
        iName.append("_");
        iName.append(propMap.get(Constants.ANN_CONFIG_ATTR_HOST));
        iName.append("_");
        iName.append(propMap.get(Constants.ANN_CONFIG_ATTR_PORT));
        return iName.toString();
    }

    private static Set<ListenerConfiguration> getListenerConfigurationsFrom(
            Map<String, Map<String, String>> listenerProp) {
        Set<ListenerConfiguration> listenerConfigurationSet = new HashSet<>();
        for (Map.Entry<String, Map<String, String>> entry : listenerProp.entrySet()) {
            Map<String, String> propMap = entry.getValue();
            String entryListenerInterface = getListenerInterface(propMap);
            ListenerConfiguration listenerConfiguration = HTTPConnectorUtil
                    .buildListenerConfig(entryListenerInterface, propMap);
            listenerConfigurationSet.add(listenerConfiguration);
        }
        return listenerConfigurationSet;
    }

    public static HTTPCarbonMessage createHttpCarbonMessage(boolean isRequest) {
        HTTPCarbonMessage httpCarbonMessage;
        if (isRequest) {
            httpCarbonMessage = new HTTPCarbonMessage(
                    new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, ""));
            httpCarbonMessage.setEndOfMsgAdded(true);
        } else {
            httpCarbonMessage = new HTTPCarbonMessage(
                    new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
            httpCarbonMessage.setEndOfMsgAdded(true);
        }
        return httpCarbonMessage;
    }

    public static String sanitizeUri(String uri) {
        if (uri.startsWith("/")) {
            return uri;
        }
        return "/".concat(uri);
    }

    public static void checkFunctionValidity(BStruct bStruct) {
        methodInvocationCheck(bStruct);
        outboundResponseStructCheck(bStruct);
    }

    public static void methodInvocationCheck(BStruct bStruct) {
        if (bStruct.getNativeData(METHOD_ACCESSED) != null) {
            throw new IllegalStateException("illegal function invocation");
        }
        bStruct.addNativeData(METHOD_ACCESSED, true);
    }

    public static void outboundResponseStructCheck(BStruct bStruct) {
        if (bStruct.getNativeData(Constants.OUTBOUND_RESPONSE) == null) {
            throw new BallerinaException("operation not allowed");
        }
    }

    public static MessageDataSource getMessageDataSource(BStruct httpMsgStruct) {
        return (MessageDataSource) httpMsgStruct.getNativeData(MESSAGE_DATA_SOURCE);
    }
}
