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
import io.netty.handler.codec.http.DefaultHttpHeaders;
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
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.net.http.session.Session;
import org.ballerinalang.runtime.message.BlobDataSource;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.Parameter;
import org.wso2.transport.http.netty.contractimpl.HttpResponseStatusFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ballerinalang.net.http.Constants.MESSAGE_DATA_SOURCE;

/**
 * Utility class providing utility methods.
 */
public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    private static final String METHOD_ACCESSED = "isMethodAccessed";
    private static final String IO_EXCEPTION_OCCURED = "I/O exception occurred";
    private static BStructType headerValueStructType;

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

    public static BValue[] getHeader(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));

        String headerName = abstractNativeFunction.getStringArgument(context, 0);
        String headerValue = httpCarbonMessage.getHeader(headerName);

        return abstractNativeFunction.getBValues(new BString(headerValue));
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

    public static BValue[] setBinaryPayload(Context context, AbstractNativeFunction nativeFunction, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) nativeFunction.getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil.getCarbonMsg(httpMessageStruct,
                HttpUtil.createHttpCarbonMessage(isRequest));

        httpCarbonMessage.waitAndReleaseAllEntities();

        byte[] payload = nativeFunction.getBlobArgument(context, 0);
        BlobDataSource blobDataSource = new BlobDataSource(payload);
        addMessageDataSource(httpMessageStruct, blobDataSource);

        httpCarbonMessage.setHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);

        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setJsonPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));

        httpCarbonMessage.waitAndReleaseAllEntities();

        BJSON payload = (BJSON) abstractNativeFunction.getRefArgument(context, 1);
        addMessageDataSource(httpMessageStruct, payload);

        HttpUtil.setHeaderToStruct(context, httpMessageStruct, Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);

        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setStringPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));

        httpCarbonMessage.waitAndReleaseAllEntities();

        String payload = abstractNativeFunction.getStringArgument(context, 0);
        StringDataSource stringDataSource = new StringDataSource(payload);
        addMessageDataSource(httpMessageStruct, stringDataSource);

        HttpUtil.setHeaderToStruct(context, httpMessageStruct, Constants.CONTENT_TYPE, Constants.TEXT_PLAIN);
        if (log.isDebugEnabled()) {
            log.debug("Setting new payload: " + payload);
        }
        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] setXMLPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);

        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));

        httpCarbonMessage.waitAndReleaseAllEntities();

        BXML payload = (BXML) abstractNativeFunction.getRefArgument(context, 1);
        addMessageDataSource(httpMessageStruct, payload);

        HttpUtil.setHeaderToStruct(context, httpMessageStruct, Constants.CONTENT_TYPE, Constants.APPLICATION_XML);

        return AbstractNativeFunction.VOID_RETURN;
    }

    public static BValue[] getBinaryPayload(Context context,
            AbstractNativeFunction abstractNativeFunction, boolean isRequest) {
        BlobDataSource result;
        try {
            BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
            HTTPCarbonMessage httpCarbonMessage = HttpUtil
                    .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));

            if (httpMessageStruct.getNativeData(MESSAGE_DATA_SOURCE) != null) {
                result = (BlobDataSource) httpMessageStruct.getNativeData(MESSAGE_DATA_SOURCE);
            } else {
                HttpMessageDataStreamer httpMessageDataStreamer = new HttpMessageDataStreamer(httpCarbonMessage);
                result = new BlobDataSource(toByteArray(httpMessageDataStreamer.getInputStream()));
                HttpUtil.addMessageDataSource(httpMessageStruct, result);
            }
            if (log.isDebugEnabled()) {
                log.debug("String representation of the payload:" + result.getMessageAsString());
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving string payload from message: " + e.getMessage());
        }
        return abstractNativeFunction.getBValues(new BBlob(result.getValue()));
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
                HttpMessageDataStreamer httpMessageDataStreamer = new HttpMessageDataStreamer(httpCarbonMessage);
                result = new BJSON(httpMessageDataStreamer.getInputStream());
                addMessageDataSource(httpMessageStruct, result);
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving json payload from message: " + e.getMessage());
        }
        // Setting output value.
        return abstractNativeFunction.getBValues(result);
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
                HttpMessageDataStreamer httpMessageDataStreamer = new HttpMessageDataStreamer(httpCarbonMessage);
                String payload = StringUtils.getStringFromInputStream(httpMessageDataStreamer.getInputStream());
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
                HttpMessageDataStreamer httpMessageDataStreamer = new HttpMessageDataStreamer(httpCarbonMessage);
                result = XMLUtils.parse(httpMessageDataStreamer.getInputStream());
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

    public static void addMessageDataSource(BStruct struct, MessageDataSource messageDataSource) {
        struct.addNativeData(MESSAGE_DATA_SOURCE, messageDataSource);
    }

    public static MessageDataSource getMessageDataSource(BStruct httpMsgStruct) {
        return (MessageDataSource) httpMsgStruct.getNativeData(MESSAGE_DATA_SOURCE);
    }

    public static void closeMessageOutputStream(OutputStream messageOutputStream) {
        try {
            if (messageOutputStream != null) {
                messageOutputStream.close();
            }
        } catch (IOException e) {
            log.error("Couldn't close message output stream", e);
        }
    }

    public static BValue[] getContentLength(Context context, AbstractNativeFunction abstractNativeFunction) {
        int contentLength = -1;
        BStruct requestStruct = (BStruct) abstractNativeFunction.getRefArgument(context, 0);
        String lengthStr = HttpUtil.getHeaderFromStruct(requestStruct, Constants.HTTP_CONTENT_LENGTH);
        try {
            contentLength = Integer.parseInt(lengthStr);
        } catch (NumberFormatException e) {
            throw new BallerinaException("Invalid content length");
        }
        return abstractNativeFunction.getBValues(new BInteger(contentLength));
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
            , HTTPCarbonMessage requestMessage, HTTPCarbonMessage responseMessage, BStruct httpMessageStruct) {
        addHTTPSessionAndCorsHeaders(requestMessage, responseMessage);

        MessageDataSource outboundMessageSource = HttpUtil.getMessageDataSource(httpMessageStruct);
        HttpResponseStatusFuture outboundResponseStatusFuture = sendOutboundResponse(requestMessage, responseMessage);
        if (outboundMessageSource != null) {
            OutputStream messageOutputStream = new HttpMessageDataStreamer(responseMessage).getOutputStream();
            outboundMessageSource.serializeData(messageOutputStream);
            HttpUtil.closeMessageOutputStream(messageOutputStream);
        }

        try {
            outboundResponseStatusFuture = outboundResponseStatusFuture.sync();
        } catch (InterruptedException e) {
            throw new BallerinaException("interrupted sync: " + e.getMessage());
        }
        if (outboundResponseStatusFuture.getStatus().getCause() != null) {
            return abstractNativeFunction.getBValues(getServerConnectorError(context
                    , outboundResponseStatusFuture.getStatus().getCause()));
        }
        return abstractNativeFunction.VOID_RETURN;
    }

    public static BStruct createSessionStruct(Context context, Session session) {
        BStruct sessionStruct = ConnectorUtils
                .createAndGetStruct(context, Constants.PROTOCOL_PACKAGE_HTTP, Constants.SESSION);
        //Add session to the struct as a native data
        sessionStruct.addNativeData(Constants.HTTP_SESSION, session);
        return sessionStruct;
    }

    public static String getSessionID(String cookieHeader) {
        return Arrays.stream(cookieHeader.split(";"))
                .filter(cookie -> cookie.trim().startsWith(Constants.SESSION_ID))
                .findFirst().get().trim().substring(Constants.SESSION_ID.length());
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

    public static HttpResponseStatusFuture sendOutboundResponse(HTTPCarbonMessage requestMsg,
            HTTPCarbonMessage responseMsg) {
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
        sendOutboundResponse(requestMessage, createErrorMessage(errorMsg, statusCode));
    }

    public static HTTPCarbonMessage createErrorMessage(String payload, int statusCode) {
        HTTPCarbonMessage response = HttpUtil.createHttpCarbonMessage(false);
        response.waitAndReleaseAllEntities();
        if (payload != null) {
            payload = lowerCaseTheFirstLetter(payload);
            response.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(payload.getBytes())));
        }
        setHttpStatusCodes(statusCode, response);

        return response;
    }

    private static String lowerCaseTheFirstLetter(String payload) {
        char[] characters = payload.toCharArray();
        characters[0] = Character.toLowerCase(characters[0]);
        payload = new String(characters);
        return payload;
    }

    private static void setHttpStatusCodes(int statusCode, HTTPCarbonMessage response) {
        HttpHeaders httpHeaders = response.getHeaders();
        httpHeaders.set(org.wso2.transport.http.netty.common.Constants.HTTP_CONTENT_TYPE,
                        org.wso2.transport.http.netty.common.Constants.TEXT_PLAIN);

        response.setProperty(org.wso2.transport.http.netty.common.Constants.HTTP_STATUS_CODE, statusCode);
    }

    public static BStruct getServerConnectorError(Context context, Throwable throwable) {
        PackageInfo httpPackageInfo = context.getProgramFile()
                .getPackageInfo(Constants.PROTOCOL_PACKAGE_HTTP);
        StructInfo errorStructInfo = httpPackageInfo.getStructInfo(Constants.HTTP_CONNECTOR_ERROR);
        BStruct httpConnectorError = new BStruct(errorStructInfo.getType());
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

    public static void setHeaderValueStructType(BStruct struct) {
        headerValueStructType = struct.getType();
    }

    public static void populateInboundRequest(BStruct inboundRequestStruct, HTTPCarbonMessage inboundRequestMsg) {
        inboundRequestStruct.addNativeData(Constants.TRANSPORT_MESSAGE, inboundRequestMsg);
        inboundRequestStruct.addNativeData(Constants.INBOUND_REQUEST, true);

        enrichWithInboundRequestInfo(inboundRequestStruct, inboundRequestMsg);
        enrichWithInboundRequestHeaders(inboundRequestStruct, inboundRequestMsg);
    }

    private static void enrichWithInboundRequestHeaders(BStruct inboundRequestStruct,
            HTTPCarbonMessage inboundRequestMsg) {
        if (inboundRequestMsg.getHeader(Constants.USER_AGENT_HEADER) != null) {
            inboundRequestStruct.setStringField(Constants.REQUEST_USER_AGENT_INDEX,
                    inboundRequestMsg.getHeader(Constants.USER_AGENT_HEADER));
            inboundRequestMsg.removeHeader(Constants.USER_AGENT_HEADER);
        }
        inboundRequestStruct.setRefField(Constants.REQUEST_HEADERS_INDEX,
                prepareHeaderMap(inboundRequestMsg.getHeaders(), new BMap<>()));
    }

    private static void enrichWithInboundRequestInfo(BStruct inboundRequestStruct,
            HTTPCarbonMessage inboundRequestMsg) {
        inboundRequestStruct.setStringField(Constants.REQUEST_PATH_INDEX,
                (String) inboundRequestMsg.getProperty(Constants.REQUEST_URL));
        inboundRequestStruct.setStringField(Constants.REQUEST_HOST_INDEX,
                ((InetSocketAddress) inboundRequestMsg.getProperty(Constants.LOCAL_ADDRESS)).getHostName());
        inboundRequestStruct.setIntField(Constants.REQUEST_PORT_INDEX,
                (Integer) inboundRequestMsg.getProperty(Constants.LISTENER_PORT));
        inboundRequestStruct.setStringField(Constants.REQUEST_METHOD_INDEX,
                (String) inboundRequestMsg.getProperty(Constants.HTTP_METHOD));
        inboundRequestStruct.setStringField(Constants.REQUEST_VERSION_INDEX,
                (String) inboundRequestMsg.getProperty(Constants.HTTP_VERSION));
        Map<String, String> resourceArgValues =
                (Map<String, String>) inboundRequestMsg.getProperty(Constants.RESOURCE_ARGS);
        inboundRequestStruct.setStringField(Constants.REQUEST_REST_URI_POSTFIX_INDEX,
                resourceArgValues.get(Constants.REST_URI_POSTFIX));
    }

    public static void populateInboundResponse(BStruct response, HTTPCarbonMessage cMsg) {
        response.addNativeData(Constants.TRANSPORT_MESSAGE, cMsg);
        int statusCode = (Integer) cMsg.getProperty(Constants.HTTP_STATUS_CODE);
        response.setIntField(Constants.RESPONSE_STATUS_CODE_INDEX, statusCode);
        response.setStringField(Constants.RESPONSE_REASON_PHRASE_INDEX,
                HttpResponseStatus.valueOf(statusCode).reasonPhrase());

        if (cMsg.getHeader(Constants.SERVER_HEADER) != null) {
            response.setStringField(Constants.RESPONSE_SERVER_INDEX, cMsg.getHeader(Constants.SERVER_HEADER));
            cMsg.removeHeader(Constants.SERVER_HEADER);
        }
        response.setRefField(Constants.RESPONSE_HEADERS_INDEX,
                prepareHeaderMap(cMsg.getHeaders(), new BMap<>()));
    }

    @SuppressWarnings("unchecked")
    public static void populateOutboundRequest(BStruct request, HTTPCarbonMessage reqMsg) {
        setHeadersToTransportMessage(reqMsg, request);
    }

    public static void populateOutboundResponse(BStruct response, HTTPCarbonMessage resMsg, HTTPCarbonMessage reqMsg) {
        response.addNativeData(Constants.TRANSPORT_MESSAGE, resMsg);
        response.addNativeData(Constants.INBOUND_REQUEST_MESSAGE, reqMsg);
        response.addNativeData(Constants.OUTBOUND_RESPONSE, true);
        response.setRefField(Constants.RESPONSE_HEADERS_INDEX, new BMap<>());
    }

    private static BMap<String, BValue> prepareHeaderMap(HttpHeaders headers, BMap<String, BValue> headerMap) {
        Map<String, ArrayList> headerStructHolder = new HashMap<>();
        for (Map.Entry<String, String> headerEntry : headers) {
            String headerKey = headerEntry.getKey().trim();
            String headerValue = headerEntry.getValue().trim();
            //Get the list of HeaderStruct for a given key
            ArrayList<BStruct> headerValueList = headerStructHolder.get(headerKey) != null ?
                    headerStructHolder.get(headerKey) : new ArrayList<>();
            if (headerValue.contains(",")) {
                List<String> valueList = Arrays.stream(headerValue.split(",")).map(String::trim)
                        .collect(Collectors.toList());
                for (String value : valueList) {
                    populateHeaderStruct(headerValueList, value);
                }
            } else {
                populateHeaderStruct(headerValueList, headerValue);
            }
            headerStructHolder.put(headerKey, headerValueList);
        }
        //create BMap of BRefValueArray
        for (Map.Entry<String, ArrayList> structHolder : headerStructHolder.entrySet()) {
            headerMap.put(structHolder.getKey(), new BRefValueArray((BRefType[]) structHolder.getValue()
                    .toArray(new BRefType[0]), new BArrayType(headerValueStructType)));
        }
        return headerMap;
    }

    @SuppressWarnings("unchecked")
    private static void populateHeaderStruct(ArrayList headerValueList, String value) {
        if (value.contains(";")) {
            headerValueList.add(populateWithHeaderValueAndParams(new BStruct(headerValueStructType), value));
        } else {
            headerValueList.add(populateWithHeaderValue(new BStruct(headerValueStructType), value));
        }
    }

    private static BStruct populateWithHeaderValueAndParams(BStruct headerStruct, String headerValue) {
        String value = headerValue.substring(0, headerValue.indexOf(";")).trim();
        List<String> paramList = Arrays.stream(headerValue.substring(headerValue.indexOf(";") + 1)
                .split(";")).map(String::trim).collect(Collectors.toList());
        headerStruct.setStringField(Constants.HEADER_VALUE_INDEX, value);
        headerStruct.setRefField(Constants.HEADER_PARAM_INDEX, createParamBMap(paramList));
        return headerStruct;
    }

    private static BStruct populateWithHeaderValue(BStruct headerStruct, String headerValue) {
        headerStruct.setStringField(0, headerValue.trim());
        return headerStruct;
    }

    private static BMap<String, BValue> createParamBMap(List<String> paramList) {
        BMap<String, BValue> paramMap = new BMap<>();
        for (String param : paramList) {
            if (param.contains("=")) {
                String[] keyValuePair = param.split("=");
                paramMap.put(keyValuePair[0].trim(), new BString(keyValuePair[1].trim()));
            } else {
                //handle when parameter value is optional
                paramMap.put(param.trim(), null);
            }
        }
        return paramMap;
    }

    /**
     * Set headers of request/response struct to the transport message.
     *
     * @param outboundRequest transport Http carbon message.
     * @param struct req/resp struct.
     */
    public static void setHeadersToTransportMessage(HTTPCarbonMessage outboundRequest, BStruct struct) {
        outboundRequest.getHeaders().clear();
        BMap<String, BValue> headers = struct.getType().getName().equals(Constants.REQUEST) ?
                getRequestStructHeaders(struct) : getResponseStructHeaders(struct);
        if (headers == null) {
            return;
        }
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            String headerValue = buildHeaderValue(headers, key);
            outboundRequest.setHeader(key, headerValue);
        }
    }

    @SuppressWarnings("unchecked")
    private static BMap<String, BValue> getRequestStructHeaders(BStruct struct) {
        BMap<String, BValue> headers = (BMap) struct.getRefField(Constants.REQUEST_HEADERS_INDEX);
        if (headers == null) {
            return null;
        }
        HttpHeaders removedHeaders = new DefaultHttpHeaders();
        if (!struct.getStringField(Constants.REQUEST_USER_AGENT_INDEX).equals("")) {
            removedHeaders.add(Constants.USER_AGENT_HEADER, struct.getStringField(Constants.REQUEST_USER_AGENT_INDEX));
        }
        return prepareHeaderMap(removedHeaders, headers);
    }

    @SuppressWarnings("unchecked")
    private static BMap<String, BValue> getResponseStructHeaders(BStruct struct) {
        BMap<String, BValue> headers = (BMap) struct.getRefField(Constants.RESPONSE_HEADERS_INDEX);
        if (headers == null) {
            return null;
        }
        HttpHeaders removedHeaders = new DefaultHttpHeaders();
        if (struct.getNativeData(Constants.OUTBOUND_RESPONSE) == null
                && !struct.getStringField(Constants.RESPONSE_SERVER_INDEX).equals("")) {
            removedHeaders.add(Constants.SERVER_HEADER, struct.getStringField(Constants.RESPONSE_SERVER_INDEX));
        }
        return prepareHeaderMap(removedHeaders, headers);
    }

    private static String buildHeaderValue(BMap<String, BValue> headers, String key) {
        StringBuilder headerValue = new StringBuilder();
        if (headers.get(key).getType().getTag() != TypeTags.ARRAY_TAG) {
            throw new BallerinaException("expects an array as header value for header : " + key);
        }
        BRefValueArray headerValues = (BRefValueArray) headers.get(key);
        for (int index = 0; index < headerValues.size(); index++) {
            //TODO remove this check when map supports exact type
            if (headerValues.get(index).getType().getTag() == TypeTags.STRUCT_TAG) {
                BStruct headerStruct = (BStruct) headerValues.get(index);
                String value = headerStruct.getStringField(Constants.HEADER_VALUE_INDEX);
                headerValue.append(index > 0 ? "," + value : value);
                BMap paramMap = (BMap) headerStruct.getRefField(Constants.HEADER_PARAM_INDEX);
                headerValue = paramMap != null ? concatParams(headerValue, paramMap) : headerValue;
            } else if (headerValues.get(index).getType().getTag() == TypeTags.MAP_TAG) {
                BMap headerMap = (BMap) headerValues.get(index);
                String value = headerMap.get(Constants.HEADER_VALUE).stringValue();
                headerValue.append(index > 0 ? "," + value : value);
                BMap paramMap = (BMap) headerMap.get(Constants.HEADER_PARAM);
                headerValue = paramMap != null ? concatParams(headerValue, paramMap) : headerValue;
            } else {
                throw new BallerinaException("invalid header assignment for key : " + key);
            }
        }
        return headerValue.toString();
    }

    @SuppressWarnings("unchecked")
    private static StringBuilder concatParams(StringBuilder headerValue, BMap paramMap) {
        Set<String> paramKeys = paramMap.keySet();
        for (String paramKey : paramKeys) {
            String paramValue = paramMap.get(paramKey) != null ? paramMap.get(paramKey).stringValue() : null;
            headerValue.append(paramValue == null ? ";" + paramKey : ";" + paramKey + "=" + paramValue);
        }
        return headerValue;
    }

    private static void setHeaderToStruct(Context context, BStruct struct, String key, String value) {
        headerValueStructType = headerValueStructType == null ? ConnectorUtils.createAndGetStruct(context,
                Constants.HTTP_PACKAGE_PATH, Constants.HEADER_VALUE_STRUCT).getType() : headerValueStructType;
        int headersIndex = struct.getType().getName().equals(Constants.REQUEST) ? Constants.REQUEST_HEADERS_INDEX :
                Constants.RESPONSE_HEADERS_INDEX;
        BMap<String, BValue> headerMap = struct.getRefField(headersIndex) != null ?
                (BMap) struct.getRefField(headersIndex) : new BMap<>();
        struct.setRefField(headersIndex, prepareHeaderMap(new DefaultHttpHeaders().add(key, value), headerMap));
    }

    @SuppressWarnings("unchecked")
    private static String getHeaderFromStruct(BStruct struct, String key) {
        int headersIndex = struct.getType().getName().equals(Constants.REQUEST) ? Constants.REQUEST_HEADERS_INDEX :
                Constants.RESPONSE_HEADERS_INDEX;
        return struct.getRefField(headersIndex) != null ?
                buildHeaderValue((BMap) struct.getRefField(headersIndex), key) : null;
    }

    /**
     * Extract the listener configurations from the config annotation.
     *
     * @param annotationInfo configuration annotation info.
     * @return the set of {@link ListenerConfiguration} which were extracted from config annotation.
     */
    public static Set<ListenerConfiguration> getDefaultOrDynamicListenerConfig(Annotation annotationInfo) {

        if (annotationInfo == null) {
            return HttpConnectionManager.getInstance().getDefaultListenerConfiugrationSet();
        }

        //key - listenerId, value - listener config property map
        Set<ListenerConfiguration> listenerConfSet = new HashSet<>();

        extractBasicConfig(annotationInfo, listenerConfSet);
        extractHttpsConfig(annotationInfo, listenerConfSet);

        if (listenerConfSet.isEmpty()) {
            listenerConfSet = HttpConnectionManager.getInstance().getDefaultListenerConfiugrationSet();
        }

        return listenerConfSet;
    }

    private static String getListenerInterface(String host, int port) {
        host = host != null ? host : "0.0.0.0";
        return host + ":" + port;
    }

    private static void extractBasicConfig(Annotation configInfo, Set<ListenerConfiguration> listenerConfSet) {
        AnnAttrValue hostAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_HOST);
        AnnAttrValue portAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_PORT);
        AnnAttrValue keepAliveAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_KEEP_ALIVE);
        AnnAttrValue transferEncoding = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_TRANSFER_ENCODING);
        AnnAttrValue chunking = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_CHUNKING);

        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        if (portAttrVal != null && portAttrVal.getIntValue() > 0) {
            listenerConfiguration.setPort(Math.toIntExact(portAttrVal.getIntValue()));

            listenerConfiguration.setScheme(Constants.PROTOCOL_HTTP);
            if (hostAttrVal != null && hostAttrVal.getStringValue() != null) {
                listenerConfiguration.setHost(hostAttrVal.getStringValue());
            } else {
                listenerConfiguration.setHost(Constants.HTTP_DEFAULT_HOST);
            }

            if (keepAliveAttrVal != null) {
                listenerConfiguration.setKeepAlive(keepAliveAttrVal.getBooleanValue());
            } else {
                listenerConfiguration.setKeepAlive(Boolean.TRUE);
            }

            // For the moment we don't have to pass it down to transport as we only support
            // chunking. Once we start supporting gzip, deflate, etc, we need to parse down the config.
            if (transferEncoding != null && !Constants.ANN_CONFIG_ATTR_CHUNKING
                    .equalsIgnoreCase(transferEncoding.getStringValue())) {
                throw new BallerinaConnectorException("Unsupported configuration found for Transfer-Encoding : "
                        + transferEncoding.getStringValue());
            }

            if (chunking != null) {
                ChunkConfig chunkConfig = getChunkConfig(chunking.getStringValue());
                listenerConfiguration.setChunkConfig(chunkConfig);
            } else {
                listenerConfiguration.setChunkConfig(ChunkConfig.AUTO);
            }

            listenerConfiguration
                    .setId(getListenerInterface(listenerConfiguration.getHost(), listenerConfiguration.getPort()));
            listenerConfSet.add(listenerConfiguration);
        }
    }

    public static ChunkConfig getChunkConfig(String chunking) {
        ChunkConfig chunkConfig;
        if (Constants.CHUNKING_AUTO.equalsIgnoreCase(chunking)) {
            chunkConfig = ChunkConfig.AUTO;
        } else if (Constants.CHUNKING_ALWAYS.equalsIgnoreCase(chunking)) {
            chunkConfig = ChunkConfig.ALWAYS;
        } else if (Constants.CHUNKING_NEVER.equalsIgnoreCase(chunking)) {
            chunkConfig = ChunkConfig.NEVER;
        } else {
            throw new BallerinaConnectorException("Invalid configuration found for Transfer-Encoding : " + chunking);
        }
        return chunkConfig;
    }

    private static void extractHttpsConfig(Annotation configInfo, Set<ListenerConfiguration> listenerConfSet) {
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
        AnnAttrValue hostAttrVal = configInfo.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_HOST);

        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        if (httpsPortAttrVal != null && httpsPortAttrVal.getIntValue() > 0) {
            listenerConfiguration.setPort(Math.toIntExact(httpsPortAttrVal.getIntValue()));
            listenerConfiguration.setScheme(Constants.PROTOCOL_HTTPS);

            if (hostAttrVal != null && hostAttrVal.getStringValue() != null) {
                listenerConfiguration.setHost(hostAttrVal.getStringValue());
            } else {
                listenerConfiguration.setHost(Constants.HTTP_DEFAULT_HOST);
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

            listenerConfiguration.setTLSStoreType(Constants.PKCS_STORE_TYPE);
            listenerConfiguration.setKeyStoreFile(keyStoreFileAttrVal.getStringValue());
            listenerConfiguration.setKeyStorePass(keyStorePasswordAttrVal.getStringValue());
            listenerConfiguration.setCertPass(certPasswordAttrVal.getStringValue());

            if (sslVerifyClientAttrVal != null) {
                listenerConfiguration.setVerifyClient(sslVerifyClientAttrVal.getStringValue());
            }
            if (trustStoreFileAttrVal != null) {
                listenerConfiguration.setTrustStoreFile(trustStoreFileAttrVal.getStringValue());
            }
            if (trustStorePasswordAttrVal != null) {
                listenerConfiguration.setTrustStorePass(trustStorePasswordAttrVal.getStringValue());
            }

            List<Parameter> serverParams = new ArrayList<>();
            Parameter serverCiphers;
            if (sslEnabledProtocolsAttrVal != null && sslEnabledProtocolsAttrVal.getStringValue() != null) {
                serverCiphers = new Parameter(Constants.ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS,
                        sslEnabledProtocolsAttrVal.getStringValue());
                serverParams.add(serverCiphers);
            }

            if (ciphersAttrVal != null && ciphersAttrVal.getStringValue() != null) {
                serverCiphers = new Parameter(Constants.ANN_CONFIG_ATTR_CIPHERS, ciphersAttrVal.getStringValue());
                serverParams.add(serverCiphers);
            }

            if (!serverParams.isEmpty()) {
                listenerConfiguration.setParameters(serverParams);
            }

            if (sslProtocolAttrVal != null) {
                listenerConfiguration.setSSLProtocol(sslProtocolAttrVal.getStringValue());
            }

            listenerConfiguration
                    .setId(getListenerInterface(listenerConfiguration.getHost(), listenerConfiguration.getPort()));
            listenerConfSet.add(listenerConfiguration);
        }
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

    public static void checkFunctionValidity(BStruct bStruct, HTTPCarbonMessage httpMsg) {
        methodInvocationCheck(bStruct, httpMsg);
        outboundResponseStructCheck(bStruct);
    }

    private static void methodInvocationCheck(BStruct bStruct, HTTPCarbonMessage httpMsg) {
        if (bStruct.getNativeData(METHOD_ACCESSED) != null || httpMsg == null) {
            throw new IllegalStateException("illegal function invocation");
        }

        if (!is100ContinueRequest(httpMsg)) {
            bStruct.addNativeData(METHOD_ACCESSED, true);
        }
    }

    private static void outboundResponseStructCheck(BStruct bStruct) {
        if (bStruct.getNativeData(Constants.OUTBOUND_RESPONSE) == null) {
            throw new BallerinaException("operation not allowed");
        }
    }

    private static boolean is100ContinueRequest(HTTPCarbonMessage httpMsg) {
        return Constants.HEADER_VAL_100_CONTINUE.equalsIgnoreCase(httpMsg.getHeader(Constants.EXPECT_HEADER));
    }

    public static Annotation getServiceConfigAnnotation(Service service, String pkgPath) {
        List<Annotation> annotationList = service.getAnnotationList(pkgPath, Constants.ANN_NAME_CONFIG);

        if (annotationList == null) {
            return null;
        }

        if (annotationList.size() > 1) {
            throw new BallerinaException(
                    "multiple service configuration annotations found in service: " + service.getName());
        }

        return annotationList.isEmpty() ? null : annotationList.get(0);
    }

    public static Annotation getResourceConfigAnnotation(Resource resource, String pkgPath) {
        List<Annotation> annotationList = resource.getAnnotationList(pkgPath, Constants.ANN_NAME_RESOURCE_CONFIG);

        if (annotationList == null) {
            return null;
        }

        if (annotationList.size() > 1) {
            throw new BallerinaException(
                    "multiple resource configuration annotations found in resource: " +
                            resource.getServiceName() + "." + resource.getName());
        }

        return annotationList.isEmpty() ? null : annotationList.get(0);
    }
}
