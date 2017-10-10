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

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.util.MessageUtils;
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
import org.ballerinalang.runtime.message.BallerinaMessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.MessageDataSource;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.carbon.transport.http.netty.message.HttpMessageDataStreamer;

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

/**
 * Utility class providing utility methods.
 */
public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    private static final String TRANSPORT_MESSAGE = "transport_message";

    public static BValue[] addHeader(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct requestStruct = ((BStruct) abstractNativeFunction.getRefArgument(context, 0));
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));

        String headerName = abstractNativeFunction.getStringArgument(context, 0);
        String headerValue = abstractNativeFunction.getStringArgument(context, 1);

        HttpHeaders httpHeaders = httpCarbonMessage.getHeaders();
        httpHeaders.add(headerName, headerValue);

        if (log.isDebugEnabled()) {
            log.debug("Add " + headerName + " to header with value: " + headerValue);
        }

        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] clone(Context context, AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        if (log.isDebugEnabled()) {
            log.debug("Invoke message clone.");
        }
        BStruct requestStruct = ((BStruct) abstractNativeFunction.getRefArgument(context, 0));
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));

        BStruct clonedRequestStruct = new BStruct(requestStruct.getType());
        HTTPCarbonMessage clonedHttpRequest = createHttpCarbonMessage(httpCarbonMessage);
        HttpUtil.addCarbonMsg(clonedRequestStruct, clonedHttpRequest);
        return abstractNativeFunction.getBValues(clonedRequestStruct);
    }

    public static BValue[] getBinaryPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BBlob result;
        try {
            BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
            HTTPCarbonMessage httpCarbonMessage = HttpUtil
                    .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));

            if (httpCarbonMessage.isAlreadyRead()) {
                result = new BBlob((byte[]) httpCarbonMessage.getMessageDataSource().getDataObject());
            } else {
                result = new BBlob(toByteArray(new HttpMessageDataStreamer(httpCarbonMessage).getInputStream()));
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
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));

        String headerName = abstractNativeFunction.getStringArgument(context, 0);
        String headerValue = httpCarbonMessage.getHeader(headerName);

//        if (headerValue == null) {
//            TODO: should NOT handle error for null headers, need to return `ballerina null`
//            ErrorHandler.handleUndefineHeader(headerName);
//        }
        return abstractNativeFunction.getBValues(new BString(headerValue));
    }

    public static BValue[] getJsonPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BJSON result = null;
        try {
            // Accessing First Parameter Value.
            BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
            HTTPCarbonMessage httpCarbonMessage = HttpUtil
                    .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));

            if (httpCarbonMessage.isAlreadyRead()) {
                MessageDataSource payload = httpCarbonMessage.getMessageDataSource();
                if (payload instanceof BJSON) {
                    result = (BJSON) payload;
                } else {
                    // else, build the JSON from the string representation of the payload.
                    result = new BJSON(httpCarbonMessage.getMessageDataSource().getMessageAsString());
                }
            } else {
                result = new BJSON(new HttpMessageDataStreamer(httpCarbonMessage).getInputStream());
                httpCarbonMessage.setMessageDataSource(result);
                result.setOutputStream(new HttpMessageDataStreamer(httpCarbonMessage).getOutputStream());
                httpCarbonMessage.setAlreadyRead(true);
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving json payload from message: " + e.getMessage());
        }
        // Setting output value.
        return abstractNativeFunction.getBValues(result);
    }

    public static BValue[] getProperty(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));
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
            BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
            HTTPCarbonMessage httpCarbonMessage = HttpUtil
                    .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));
            if (httpCarbonMessage.isAlreadyRead()) {
                result = new BString(httpCarbonMessage.getMessageDataSource().getMessageAsString());
            } else {
                if (httpCarbonMessage.isEmpty() && httpCarbonMessage.isEndOfMsgAdded()) {
                    return abstractNativeFunction.getBValues(new BString(""));
                }
                String payload = MessageUtils.getStringFromInputStream(new HttpMessageDataStreamer(httpCarbonMessage)
                        .getInputStream());
                result = new BString(payload);
                httpCarbonMessage.setMessageDataSource(new StringDataSource(payload));
                httpCarbonMessage.setAlreadyRead(true);
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
        BXML result = null;
        try {
            // Accessing First Parameter Value.
//            BMessage msg = (BMessage) abstractNativeFunction.getRefArgument(context, 0);
            BStruct struct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
            HTTPCarbonMessage httpCarbonMessage = HttpUtil
                    .getCarbonMsg(struct, HttpUtil.createHttpCarbonMessage(isRequest));

            if (httpCarbonMessage.isAlreadyRead()) {
                MessageDataSource payload = httpCarbonMessage.getMessageDataSource();
                if (payload instanceof BXML) {
                    // if the payload is already xml, return it as it is.
                    result = (BXML) payload;
                } else {
                    // else, build the xml from the string representation of the payload.
                    result = XMLUtils.parse(httpCarbonMessage.getMessageDataSource().getMessageAsString());
                }
            } else {
                if (httpCarbonMessage.isEmpty()) {
                    throw new BallerinaException("empty content");
                }
                result = XMLUtils.parse(new HttpMessageDataStreamer(httpCarbonMessage).getInputStream());
                httpCarbonMessage.setMessageDataSource(result);
                result.setOutputStream(new HttpMessageDataStreamer(httpCarbonMessage).getOutputStream());
                httpCarbonMessage.setAlreadyRead(true);
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving XML payload from message: " + e.getMessage());
        }
        // Setting output value.
        return abstractNativeFunction.getBValues(result);
    }

    private static HTTPCarbonMessage createHttpCarbonMessage(HTTPCarbonMessage httpCarbonMessage) {
        HTTPCarbonMessage clonedHttpCarbonMessage;
        if (httpCarbonMessage.getMessageDataSource() != null &&
                httpCarbonMessage.getMessageDataSource() instanceof BallerinaMessageDataSource) {
            // Clone the headers and the properties map of this message
            clonedHttpCarbonMessage = httpCarbonMessage.cloneCarbonMessageWithOutData();
            clonedHttpCarbonMessage.setMessageDataSource(((BallerinaMessageDataSource) httpCarbonMessage
                    .getMessageDataSource()).clone());
        } else {
            clonedHttpCarbonMessage = httpCarbonMessage.cloneCarbonMessageWithData();
        }
        return clonedHttpCarbonMessage;
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
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        httpCarbonMessage.getHeaders().clear();
        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] removeHeader(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        String headerName = abstractNativeFunction.getStringArgument(context, 0);

        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        httpCarbonMessage.removeHeader(headerName);
        if (log.isDebugEnabled()) {
            log.debug("Remove header:" + headerName);
        }
        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setHeader(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        String headerName = abstractNativeFunction.getStringArgument(context, 0);
        String headerValue = abstractNativeFunction.getStringArgument(context, 1);

        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        httpCarbonMessage.setHeader(headerName, headerValue);

        if (log.isDebugEnabled()) {
            log.debug("Set " + headerName + " header with value: " + headerValue);
        }
        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setJsonPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        BJSON payload = (BJSON) abstractNativeFunction.getRefArgument(context, 1);

        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        httpCarbonMessage.setMessageDataSource(payload);
        payload.setOutputStream(new HttpMessageDataStreamer(httpCarbonMessage).getOutputStream());
        httpCarbonMessage.setAlreadyRead(true);
        httpCarbonMessage.setHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setProperty(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        String propertyName = abstractNativeFunction.getStringArgument(context, 0);
        String propertyValue = abstractNativeFunction.getStringArgument(context, 1);

        if (propertyName != null && propertyValue != null) {
            HTTPCarbonMessage httpCarbonMessage = HttpUtil
                    .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));
            httpCarbonMessage.setProperty(propertyName, propertyValue);
        }
        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setStringPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));

        String payload = abstractNativeFunction.getStringArgument(context, 0);
        StringDataSource stringDataSource = new StringDataSource(payload
                , new HttpMessageDataStreamer(httpCarbonMessage).getOutputStream());
        httpCarbonMessage.setMessageDataSource(stringDataSource);
        httpCarbonMessage.setAlreadyRead(true);
        httpCarbonMessage.setHeader(Constants.CONTENT_TYPE, Constants.TEXT_PLAIN);
        if (log.isDebugEnabled()) {
            log.debug("Setting new payload: " + payload);
        }
        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setXMLPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        BXML payload = (BXML) abstractNativeFunction.getRefArgument(context, 1);

        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        httpCarbonMessage.setMessageDataSource(payload);
        httpCarbonMessage.setHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_XML);
        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] getContentLength(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        int contentLength = -1;
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));

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
            BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
            HTTPCarbonMessage httpCarbonMessage = HttpUtil
                    .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(isRequest));
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
    public static void startPendingHttpConnectors() throws BallerinaConnectorException {
        try {
            // Starting up HTTP Server connectors
            HttpConnectionManager.getInstance().startPendingHTTPConnectors();
        } catch (ServerConnectorException e) {
            throw new BallerinaConnectorException(e);
        }
    }

    public static void handleResponse(HTTPCarbonMessage requestMsg, HTTPCarbonMessage responseMsg) {
        try {
            requestMsg.respond(responseMsg);
        } catch (org.wso2.carbon.transport.http.netty.contract.ServerConnectorException e) {
            throw new BallerinaConnectorException("Error occurred during response", e);
        }
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
            // TODO If you put just "", then we got a NPE. Need to find why
            handleResponse(requestMessage, createErrorMessage("  ", statusCode));
        }
    }

    public static HTTPCarbonMessage createErrorMessage(String payload, int statusCode) {

        HTTPCarbonMessage response = HttpUtil.createHttpCarbonMessage(false);
        StringDataSource stringDataSource = new StringDataSource(payload
                , new HttpMessageDataStreamer(response).getOutputStream());
        response.setMessageDataSource(stringDataSource);
        response.setAlreadyRead(true);
        byte[] errorMessageBytes = payload.getBytes(Charset.defaultCharset());

        HttpHeaders httpHeaders = response.getHeaders();
        httpHeaders.set(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_CONNECTION,
                org.wso2.carbon.transport.http.netty.common.Constants.CONNECTION_KEEP_ALIVE);
        httpHeaders.set(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_CONTENT_TYPE,
                org.wso2.carbon.transport.http.netty.common.Constants.TEXT_PLAIN);
        httpHeaders.set(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_CONTENT_LENGTH,
                (String.valueOf(errorMessageBytes.length)));

        response.setProperty(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_STATUS_CODE, statusCode);
        response.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);
        return response;
    }

    public static HTTPCarbonMessage getCarbonMsg(BStruct struct, HTTPCarbonMessage defaultMsg) {
        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) struct
                .getNativeData(TRANSPORT_MESSAGE);
        if (httpCarbonMessage != null) {
            return httpCarbonMessage;
        }
        addCarbonMsg(struct, defaultMsg);
        return defaultMsg;
    }

    public static void addCarbonMsg(BStruct struct, HTTPCarbonMessage httpCarbonMessage) {
        struct.addNativeData(TRANSPORT_MESSAGE, httpCarbonMessage);
    }


    /**
     * Extract the listener configurations from the config annotation.
     *
     * @param annotationInfo configuration annotation info.
     * @return the set of {@link ListenerConfiguration} which were extracted from config annotation.
     */
    public static Set<ListenerConfiguration> getDefaultOrDynamicListenerConfig(Annotation annotationInfo) {
        Map<String, Map<String, String>> listenerProp = buildListerProperties(annotationInfo);

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
    private static Map<String, Map<String, String>> buildListerProperties(Annotation configInfo) {
        if (configInfo == null) {
            return null;
        }
        //key - listenerId, value - listener config property map
        Map<String, Map<String, String>> listenerConfMap = new HashMap<>();


        AnnAttrValue hostAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_HOST);
        AnnAttrValue portAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_PORT);

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

        if (portAttrVal != null && portAttrVal.getIntValue() > 0) {
            Map<String, String> httpPropMap = new HashMap<>();
            httpPropMap.put(Constants.ANN_CONFIG_ATTR_PORT, Long.toString(portAttrVal.getIntValue()));
            httpPropMap.put(Constants.ANN_CONFIG_ATTR_SCHEME, Constants.PROTOCOL_HTTP);
            if (hostAttrVal != null && hostAttrVal.getStringValue() != null) {
                httpPropMap.put(Constants.ANN_CONFIG_ATTR_HOST, hostAttrVal.getStringValue());
            } else {
                httpPropMap.put(Constants.ANN_CONFIG_ATTR_HOST, Constants.HTTP_DEFAULT_HOST);
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
}
