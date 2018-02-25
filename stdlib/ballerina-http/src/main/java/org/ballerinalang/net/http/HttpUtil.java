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
import io.netty.handler.codec.http.HttpHeaderNames;
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
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDecoder;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.net.http.session.Session;
import org.ballerinalang.net.ws.WebSocketConstants;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.Parameter;
import org.wso2.transport.http.netty.config.RequestSizeValidationConfig;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.bre.bvm.BLangVMErrors.PACKAGE_BUILTIN;
import static org.ballerinalang.bre.bvm.BLangVMErrors.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.mime.util.Constants.CONTENT_TYPE;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS_INDEX;
import static org.ballerinalang.mime.util.Constants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.MULTIPART_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.Constants.NO_CONTENT_LENGTH_FOUND;
import static org.ballerinalang.mime.util.Constants.OCTET_STREAM;
import static org.ballerinalang.net.http.HttpConstants.ENTITY_INDEX;
import static org.ballerinalang.net.http.HttpConstants.HTTP_MESSAGE_INDEX;

/**
 * Utility class providing utility methods.
 */
public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    private static final String METHOD_ACCESSED = "isMethodAccessed";
    private static final String IO_EXCEPTION_OCCURED = "I/O exception occurred";

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

    /**
     * Set the given entity to request or response message.
     *
     * @param context                Ballerina context
     * @param abstractNativeFunction Reference to abstract native ballerina function
     * @param isRequest              boolean representing whether the message is a request or a response
     * @return void return
     */
    public static BValue[] setEntity(Context context, AbstractNativeFunction abstractNativeFunction,
                                     boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, HTTP_MESSAGE_INDEX);

        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        httpCarbonMessage.waitAndReleaseAllEntities();
        BStruct entity = (BStruct) abstractNativeFunction.getRefArgument(context, ENTITY_INDEX);
        String baseType = MimeUtil.getContentType(entity);
        if (baseType == null) {
            baseType = OCTET_STREAM;
        }
        HttpUtil.setHeaderToEntity(entity, CONTENT_TYPE, baseType);
        httpMessageStruct.addNativeData(MESSAGE_ENTITY, entity);
        httpMessageStruct.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, EntityBodyHandler
                .checkEntityBodyAvailability(entity));
        return AbstractNativeFunction.VOID_RETURN;
    }

    /**
     * Get the entity from request or response.
     *
     * @param context                Ballerina context
     * @param abstractNativeFunction Reference to abstract native ballerina function
     * @param isRequest              boolean representing whether the message is a request or a response
     * @param isEntityBodyRequired   boolean representing whether the entity body is required
     * @return Entity of the request or response
     */
    public static BValue[] getEntity(Context context, AbstractNativeFunction abstractNativeFunction, boolean isRequest,
                                     boolean isEntityBodyRequired) {
        BStruct httpMessageStruct = (BStruct) abstractNativeFunction.getRefArgument(context, HTTP_MESSAGE_INDEX);
        BStruct entity = (BStruct) httpMessageStruct.getNativeData(MESSAGE_ENTITY);
        boolean isByteChannelAlreadySet = false;

        if (httpMessageStruct.getNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET) != null) {
            isByteChannelAlreadySet = (Boolean) httpMessageStruct.getNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET);
        }
        if (entity != null && isEntityBodyRequired && !isByteChannelAlreadySet) {
            populateEntityBody(context, httpMessageStruct, entity, isRequest);
        }
        if (entity == null) {
            entity = createNewEntity(context, httpMessageStruct);
        }
        return abstractNativeFunction.getBValues(entity);
    }

    /**
     * Populate entity with the relevant body content.
     *
     * @param context           Represent ballerina context
     * @param httpMessageStruct Represent ballerina request/response
     * @param entity            Represent an entity
     * @param isRequest         boolean representing whether the message is a request or a response
     */
    protected static void populateEntityBody(Context context, BStruct httpMessageStruct, BStruct entity,
                                             boolean isRequest) {
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        HttpMessageDataStreamer httpMessageDataStreamer = new HttpMessageDataStreamer(httpCarbonMessage);
        String contentType = httpCarbonMessage.getHeader(CONTENT_TYPE);
        if (isRequest && MimeUtil.isNotNullAndEmpty(contentType) && contentType.startsWith(MULTIPART_AS_PRIMARY_TYPE)
                && context != null) {
            MultipartDecoder.parseBody(context, entity, contentType, httpMessageDataStreamer.getInputStream());
        } else {
            int contentLength = NO_CONTENT_LENGTH_FOUND;
            String lengthStr = httpCarbonMessage.getHeader(HttpConstants.HTTP_CONTENT_LENGTH);
            try {
                contentLength = lengthStr != null ? Integer.parseInt(lengthStr) : contentLength;
                if (contentLength == NO_CONTENT_LENGTH_FOUND) {
                    contentLength = httpCarbonMessage.getFullMessageLength();
                }
                MimeUtil.setContentLength(entity, contentLength);
            } catch (NumberFormatException e) {
                throw new BallerinaException("Invalid content length");
            }
            EntityBodyHandler.setDiscreteMediaTypeBodyContent(entity, httpMessageDataStreamer
                    .getInputStream());
        }
        httpMessageStruct.addNativeData(MESSAGE_ENTITY, entity);
        httpMessageStruct.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
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

    public static void prepareOutboundResponse(Context context, HTTPCarbonMessage inboundRequestMsg,
                                               HTTPCarbonMessage outboundResponseMsg, BStruct outboundResponseStruct) {

        HttpUtil.checkEntityAvailability(context, outboundResponseStruct);

        HttpUtil.addHTTPSessionAndCorsHeaders(context, inboundRequestMsg, outboundResponseMsg);
        setKeepAliveAndCompressionHeaders(context, outboundResponseMsg);
        HttpUtil.enrichOutboundMessage(outboundResponseMsg, outboundResponseStruct);
    }

    public static BStruct createSessionStruct(Context context, Session session) {
        BStruct sessionStruct = ConnectorUtils
                .createAndGetStruct(context, HttpConstants.PROTOCOL_PACKAGE_HTTP, HttpConstants.SESSION);
        //Add session to the struct as a native data
        sessionStruct.addNativeData(HttpConstants.HTTP_SESSION, session);
        return sessionStruct;
    }

    public static String getSessionID(String cookieHeader) {
        return Arrays.stream(cookieHeader.split(";"))
                .filter(cookie -> cookie.trim().startsWith(HttpConstants.SESSION_ID))
                .findFirst().get().trim().substring(HttpConstants.SESSION_ID.length());
    }

    public static void addHTTPSessionAndCorsHeaders(Context context, HTTPCarbonMessage requestMsg,
                                                    HTTPCarbonMessage responseMsg) {
        Session session = (Session) requestMsg.getProperty(HttpConstants.HTTP_SESSION);
        if (session != null) {
            boolean isSecureRequest = false;
            AnnAttachmentInfo configAnn = context.getServiceInfo().getAnnotationAttachmentInfo(
                    HttpConstants.PROTOCOL_PACKAGE_HTTP, HttpConstants.ANN_NAME_CONFIG);
            if (configAnn != null) {
                AnnAttributeValue httpsPortAttrVal = configAnn
                        .getAttributeValue(HttpConstants.ANN_CONFIG_ATTR_HTTPS_PORT);
                if (httpsPortAttrVal != null) {
                    Integer listenerPort = (Integer) requestMsg.getProperty(HttpConstants.LISTENER_PORT);
                    if (listenerPort != null && httpsPortAttrVal.getIntValue() == listenerPort) {
                        isSecureRequest = true;
                    }
                }
            }
            session.generateSessionHeader(responseMsg, isSecureRequest);
        }
        //Process CORS if exists.
        if (requestMsg.getHeader(HttpConstants.ORIGIN) != null) {
            CorsHeaderGenerator.process(requestMsg, responseMsg, true);
        }
    }

    public static HttpResponseFuture sendOutboundResponse(HTTPCarbonMessage requestMsg,
                                                          HTTPCarbonMessage responseMsg) {
        HttpResponseFuture responseFuture;
        try {
            responseFuture = requestMsg.respond(responseMsg);
        } catch (org.wso2.transport.http.netty.contract.ServerConnectorException e) {
            throw new BallerinaConnectorException("Error occurred during response", e);
        }
        return responseFuture;
    }

    public static void handleFailure(HTTPCarbonMessage requestMessage, BallerinaConnectorException ex) {
        Object carbonStatusCode = requestMessage.getProperty(HttpConstants.HTTP_STATUS_CODE);
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
        } else {
            response.addHttpContent(new DefaultLastHttpContent());
        }
        setHttpStatusCodes(statusCode, response);

        return response;
    }

    private static String lowerCaseTheFirstLetter(String payload) {
        if (!payload.isEmpty()) {
            char[] characters = payload.toCharArray();
            characters[0] = Character.toLowerCase(characters[0]);
            payload = new String(characters);
        }
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
                .getPackageInfo(HttpConstants.PROTOCOL_PACKAGE_HTTP);
        StructInfo errorStructInfo = httpPackageInfo.getStructInfo(HttpConstants.HTTP_CONNECTOR_ERROR);
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
                .getNativeData(HttpConstants.TRANSPORT_MESSAGE);
        if (httpCarbonMessage != null) {
            return httpCarbonMessage;
        }
        addCarbonMsg(struct, defaultMsg);
        return defaultMsg;
    }

    public static void addCarbonMsg(BStruct struct, HTTPCarbonMessage httpCarbonMessage) {
        struct.addNativeData(HttpConstants.TRANSPORT_MESSAGE, httpCarbonMessage);
    }

    public static void populateInboundRequest(BStruct inboundRequestStruct, BStruct entity, BStruct mediaType,
                                              HTTPCarbonMessage inboundRequestMsg) {
        inboundRequestStruct.addNativeData(HttpConstants.TRANSPORT_MESSAGE, inboundRequestMsg);
        inboundRequestStruct.addNativeData(HttpConstants.IN_REQUEST, true);

        enrichWithInboundRequestInfo(inboundRequestStruct, inboundRequestMsg);
        enrichWithInboundRequestHeaders(inboundRequestStruct, inboundRequestMsg);

        populateEntity(entity, mediaType, inboundRequestMsg);
        inboundRequestStruct.addNativeData(MESSAGE_ENTITY, entity);
        inboundRequestStruct.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, false);
    }

    private static void enrichWithInboundRequestHeaders(BStruct inboundRequestStruct,
                                                        HTTPCarbonMessage inboundRequestMsg) {
        if (inboundRequestMsg.getHeader(HttpConstants.USER_AGENT_HEADER) != null) {
            inboundRequestStruct.setStringField(HttpConstants.IN_REQUEST_USER_AGENT_INDEX,
                    inboundRequestMsg.getHeader(HttpConstants.USER_AGENT_HEADER));
            inboundRequestMsg.removeHeader(HttpConstants.USER_AGENT_HEADER);
        }
    }

    private static void enrichWithInboundRequestInfo(BStruct inboundRequestStruct,
                                                     HTTPCarbonMessage inboundRequestMsg) {
        inboundRequestStruct.setStringField(HttpConstants.IN_REQUEST_RAW_PATH_INDEX,
                (String) inboundRequestMsg.getProperty(HttpConstants.REQUEST_URL));
        inboundRequestStruct.setStringField(HttpConstants.IN_REQUEST_METHOD_INDEX,
                (String) inboundRequestMsg.getProperty(HttpConstants.HTTP_METHOD));
        inboundRequestStruct.setStringField(HttpConstants.IN_REQUEST_VERSION_INDEX,
                (String) inboundRequestMsg.getProperty(HttpConstants.HTTP_VERSION));
        Map<String, String> resourceArgValues =
                (Map<String, String>) inboundRequestMsg.getProperty(HttpConstants.RESOURCE_ARGS);
        inboundRequestStruct.setStringField(HttpConstants.IN_REQUEST_EXTRA_PATH_INFO_INDEX,
                resourceArgValues.get(HttpConstants.EXTRA_PATH_INFO));
    }

    public static void enrichConnectionInfo(BStruct connection, HTTPCarbonMessage cMsg) {
        connection.addNativeData(HttpConstants.TRANSPORT_MESSAGE, cMsg);
        connection.setStringField(HttpConstants.CONNECTION_HOST_INDEX,
                ((InetSocketAddress) cMsg.getProperty(HttpConstants.LOCAL_ADDRESS)).getHostName());
        connection.setIntField(HttpConstants.CONNECTION_PORT_INDEX,
                (Integer) cMsg.getProperty(HttpConstants.LISTENER_PORT));
    }

    /**
     * Populate inbound response with headers and entity.
     *
     * @param inboundResponse    Ballerina struct to represent response
     * @param entity             Entity of the response
     * @param mediaType          Content type of the response
     * @param inboundResponseMsg Represent carbon message.
     */
    public static void populateInboundResponse(BStruct inboundResponse, BStruct entity, BStruct mediaType,
                                               HTTPCarbonMessage inboundResponseMsg) {
        inboundResponse.addNativeData(HttpConstants.TRANSPORT_MESSAGE, inboundResponseMsg);
        int statusCode = (Integer) inboundResponseMsg.getProperty(HttpConstants.HTTP_STATUS_CODE);
        inboundResponse.setIntField(HttpConstants.IN_RESPONSE_STATUS_CODE_INDEX, statusCode);
        inboundResponse.setStringField(HttpConstants.IN_RESPONSE_REASON_PHRASE_INDEX,
                HttpResponseStatus.valueOf(statusCode).reasonPhrase());

        if (inboundResponseMsg.getHeader(HttpConstants.SERVER_HEADER) != null) {
            inboundResponse.setStringField(HttpConstants.IN_RESPONSE_SERVER_INDEX,
                    inboundResponseMsg.getHeader(HttpConstants.SERVER_HEADER));
            inboundResponseMsg.removeHeader(HttpConstants.SERVER_HEADER);
        }
        populateEntity(entity, mediaType, inboundResponseMsg);
        inboundResponse.addNativeData(MESSAGE_ENTITY, entity);
        inboundResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, false);
    }

    /**
     * Populate entity with headers, content-type and content-length.
     *
     * @param entity    Represent an entity struct
     * @param mediaType mediaType struct that needs to be set to the entity
     * @param cMsg      Represent a carbon message
     */
    private static void populateEntity(BStruct entity, BStruct mediaType, HTTPCarbonMessage cMsg) {
        String contentType = cMsg.getHeader(CONTENT_TYPE);
        MimeUtil.setContentType(mediaType, entity, contentType);
        int contentLength = -1;
        String lengthStr = cMsg.getHeader(HttpConstants.HTTP_CONTENT_LENGTH);
        try {
            contentLength = lengthStr != null ? Integer.parseInt(lengthStr) : contentLength;
            MimeUtil.setContentLength(entity, contentLength);
        } catch (NumberFormatException e) {
            throw new BallerinaException("Invalid content length");
        }
        entity.setRefField(ENTITY_HEADERS_INDEX, prepareEntityHeaderMap(cMsg.getHeaders(), new BMap<>()));
    }

    private static BMap<String, BValue> prepareEntityHeaderMap(HttpHeaders headers, BMap<String, BValue> headerMap) {
        for (Map.Entry<String, String> header : headers.entries()) {
            if (headerMap.keySet().contains(header.getKey())) {
                BStringArray valueArray = (BStringArray) headerMap.get(header.getKey());
                valueArray.add(valueArray.size(), header.getValue());
            } else {
                BStringArray valueArray = new BStringArray(new String[]{header.getValue()});
                headerMap.put(header.getKey(), valueArray);
            }
        }
        return headerMap;
    }

    /**
     * Set headers and properties of request/response struct to the outbound transport message.
     *
     * @param outboundMsg    transport Http carbon message.
     * @param outboundStruct req/resp struct.
     */
    public static void enrichOutboundMessage(HTTPCarbonMessage outboundMsg, BStruct outboundStruct) {
        setHeadersToTransportMessage(outboundMsg, outboundStruct);
        setPropertiesToTransportMessage(outboundMsg, outboundStruct);
    }

    @SuppressWarnings("unchecked")
    private static void setHeadersToTransportMessage(HTTPCarbonMessage outboundMsg, BStruct struct) {
        BStruct entityStruct = (BStruct) struct.getNativeData(MESSAGE_ENTITY);
        HttpHeaders transportHeaders = outboundMsg.getHeaders();
        if (isInboundRequestStruct(struct) || isInboundResponseStruct(struct)) {
            addRemovedPropertiesBackToHeadersMap(struct, transportHeaders);
            return;
        }
        BMap<String, BValue> entityHeaders = (BMap) entityStruct.getRefField(ENTITY_HEADERS_INDEX);
        if (entityHeaders == null) {
            return;
        }
        Set<String> keys = entityHeaders.keySet();
        for (String key : keys) {
            BStringArray headerValues = (BStringArray) entityHeaders.get(key);
            for (int i = 0; i < headerValues.size(); i++) {
                transportHeaders.add(key, headerValues.get(i));
            }
        }
    }

    private static boolean isInboundRequestStruct(BStruct struct) {
        return struct.getType().getName().equals(HttpConstants.IN_REQUEST);
    }

    private static boolean isInboundResponseStruct(BStruct struct) {
        return struct.getType().getName().equals(HttpConstants.IN_RESPONSE);
    }

    private static boolean isOutboundResponseStruct(BStruct struct) {
        return struct.getType().getName().equals(HttpConstants.OUT_RESPONSE);
    }

    private static void addRemovedPropertiesBackToHeadersMap(BStruct struct, HttpHeaders transportHeaders) {
        if (isInboundRequestStruct(struct)) {
            if (!struct.getStringField(HttpConstants.IN_REQUEST_USER_AGENT_INDEX).isEmpty()) {
                transportHeaders.add(HttpConstants.USER_AGENT_HEADER,
                        struct.getStringField(HttpConstants.IN_REQUEST_USER_AGENT_INDEX));
            }
        } else {
            if (!struct.getStringField(HttpConstants.IN_RESPONSE_SERVER_INDEX).isEmpty()) {
                transportHeaders.add(HttpConstants.SERVER_HEADER,
                        struct.getStringField(HttpConstants.IN_RESPONSE_SERVER_INDEX));
            }
        }
    }

    private static void setPropertiesToTransportMessage(HTTPCarbonMessage outboundResponseMsg, BStruct struct) {
        if (isOutboundResponseStruct(struct)) {
            if (struct.getIntField(HttpConstants.OUT_RESPONSE_STATUS_CODE_INDEX) != 0) {
                outboundResponseMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, getIntValue(
                        struct.getIntField(HttpConstants.OUT_RESPONSE_STATUS_CODE_INDEX)));
            }
            if (!struct.getStringField(HttpConstants.OUT_RESPONSE_REASON_PHRASE_INDEX).isEmpty()) {
                outboundResponseMsg.setProperty(HttpConstants.HTTP_REASON_PHRASE,
                        struct.getStringField(HttpConstants.OUT_RESPONSE_REASON_PHRASE_INDEX));
            }
        }
    }

    private static void setHeaderToEntity(BStruct struct, String key, String value) {
        BMap<String, BValue> headerMap = struct.getRefField(ENTITY_HEADERS_INDEX) != null ?
                (BMap) struct.getRefField(ENTITY_HEADERS_INDEX) : new BMap<>();
        struct.setRefField(ENTITY_HEADERS_INDEX,
                prepareEntityHeaderMap(new DefaultHttpHeaders().add(key, value), headerMap));
    }

    /**
     * Check the existence of entity. Set new entity of not present.
     *
     * @param context ballerina context.
     * @param struct  request/response struct.
     */
    public static void checkEntityAvailability(Context context, BStruct struct) {
        BStruct entity = (BStruct) struct.getNativeData(MESSAGE_ENTITY);
        if (entity == null) {
            HttpUtil.createNewEntity(context, struct);
        }
    }

    /**
     * Set new entity to in/out request/response struct.
     *
     * @param context ballerina context.
     * @param struct  request/response struct.
     */
    public static BStruct createNewEntity(Context context, BStruct struct) {
        BStruct entity = ConnectorUtils.createAndGetStruct(context
                , org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME
                , org.ballerinalang.mime.util.Constants.ENTITY);
        entity.setRefField(ENTITY_HEADERS_INDEX, new BMap<>());
        struct.addNativeData(MESSAGE_ENTITY, entity);
        struct.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, false);
        return entity;
    }

    /**
     * Set connection Keep-Alive and content-encoding headers to transport message.
     *
     * @param context         ballerina context.
     * @param outboundMessage transport message.
     */
    public static void setKeepAliveAndCompressionHeaders(Context context, HTTPCarbonMessage outboundMessage) {
        AnnAttachmentInfo configAnn = context.getServiceInfo().getAnnotationAttachmentInfo(
                HttpConstants.PROTOCOL_PACKAGE_HTTP, HttpConstants.ANN_NAME_CONFIG);
        if (configAnn != null) {
            AnnAttributeValue keepAliveAttrVal = configAnn.getAttributeValue(HttpConstants.ANN_CONFIG_ATTR_KEEP_ALIVE);

            if (keepAliveAttrVal != null && !keepAliveAttrVal.getBooleanValue()) {
                outboundMessage.setHeader(HttpConstants.CONNECTION_HEADER, HttpConstants.HEADER_VAL_CONNECTION_CLOSE);
            } else {
                // default behaviour: keepAlive = true
                outboundMessage.setHeader(HttpConstants.CONNECTION_HEADER,
                        HttpConstants.HEADER_VAL_CONNECTION_KEEP_ALIVE);
            }
            AnnAttributeValue compressionEnabled = configAnn.getAttributeValue(
                    HttpConstants.ANN_CONFIG_ATTR_COMPRESSION_ENABLED);
            if (compressionEnabled != null && !compressionEnabled.getBooleanValue()) {
                outboundMessage.setHeader(HttpHeaderNames.CONTENT_ENCODING.toString(),
                                          Constants.HTTP_TRANSFER_ENCODING_IDENTITY);
            }
        } else {
            // default behaviour: keepAlive = true
            outboundMessage.setHeader(HttpConstants.CONNECTION_HEADER, HttpConstants.HEADER_VAL_CONNECTION_KEEP_ALIVE);
        }
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
        AnnAttrValue hostAttrVal = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_HOST);
        AnnAttrValue portAttrVal = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_PORT);
        AnnAttrValue keepAliveAttrVal = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_KEEP_ALIVE);
        AnnAttrValue transferEncoding = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_TRANSFER_ENCODING);
        AnnAttrValue chunking = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_CHUNKING);
        AnnAttrValue maxUriLength = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_MAXIMUM_URL_LENGTH);
        AnnAttrValue maxHeaderSize = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_MAXIMUM_HEADER_SIZE);
        AnnAttrValue maxEntityBodySize = configInfo.getAnnAttrValue(
                HttpConstants.ANN_CONFIG_ATTR_MAXIMUM_ENTITY_BODY_SIZE);

        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        if (portAttrVal != null && portAttrVal.getIntValue() > 0) {
            listenerConfiguration.setPort(Math.toIntExact(portAttrVal.getIntValue()));

            listenerConfiguration.setScheme(HttpConstants.PROTOCOL_HTTP);
            if (hostAttrVal != null && hostAttrVal.getStringValue() != null) {
                listenerConfiguration.setHost(hostAttrVal.getStringValue());
            } else {
                listenerConfiguration.setHost(HttpConstants.HTTP_DEFAULT_HOST);
            }

            if (keepAliveAttrVal != null) {
                listenerConfiguration.setKeepAlive(keepAliveAttrVal.getBooleanValue());
            } else {
                listenerConfiguration.setKeepAlive(Boolean.TRUE);
            }

            // For the moment we don't have to pass it down to transport as we only support
            // chunking. Once we start supporting gzip, deflate, etc, we need to parse down the config.
            if (transferEncoding != null && !HttpConstants.ANN_CONFIG_ATTR_CHUNKING
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

            RequestSizeValidationConfig requestSizeValidationConfig =
                    listenerConfiguration.getRequestSizeValidationConfig();
            if (maxUriLength != null) {
                if (maxUriLength.getIntValue() > 0) {
                    requestSizeValidationConfig.setMaxUriLength(Math.toIntExact(maxUriLength.getIntValue()));
                } else {
                    throw new BallerinaConnectorException("Invalid configuration found for maxUriLength : "
                            + maxUriLength.getIntValue());
                }
            }
            if (maxHeaderSize != null) {
                if (maxHeaderSize.getIntValue() > 0) {
                    requestSizeValidationConfig.setMaxHeaderSize(Math.toIntExact(maxHeaderSize.getIntValue()));
                } else {
                    throw new BallerinaConnectorException("Invalid configuration found for maxHeaderSize : "
                            + maxHeaderSize.getIntValue());
                }
            }
            if (maxEntityBodySize != null) {
                if (maxEntityBodySize.getIntValue() > 0) {
                    requestSizeValidationConfig.setMaxEntityBodySize(Math.toIntExact(maxEntityBodySize.getIntValue()));
                } else {
                    throw new BallerinaConnectorException("Invalid configuration found for maxEntityBodySize : "
                            + maxEntityBodySize.getIntValue());
                }
            }

            listenerConfiguration
                    .setId(getListenerInterface(listenerConfiguration.getHost(), listenerConfiguration.getPort()));
            listenerConfSet.add(listenerConfiguration);
        }
    }

    public static ChunkConfig getChunkConfig(String chunking) {
        ChunkConfig chunkConfig;
        if (HttpConstants.CHUNKING_AUTO.equalsIgnoreCase(chunking)) {
            chunkConfig = ChunkConfig.AUTO;
        } else if (HttpConstants.CHUNKING_ALWAYS.equalsIgnoreCase(chunking)) {
            chunkConfig = ChunkConfig.ALWAYS;
        } else if (HttpConstants.CHUNKING_NEVER.equalsIgnoreCase(chunking)) {
            chunkConfig = ChunkConfig.NEVER;
        } else {
            throw new BallerinaConnectorException("Invalid configuration found for Transfer-Encoding : " + chunking);
        }
        return chunkConfig;
    }

    private static void extractHttpsConfig(Annotation configInfo, Set<ListenerConfiguration> listenerConfSet) {
        // Retrieve secure port from either http of ws configuration annotation.
        AnnAttrValue httpsPortAttrVal;
        if (configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_HTTPS_PORT) == null) {
            httpsPortAttrVal =
                    configInfo.getAnnAttrValue(WebSocketConstants.ANN_CONFIG_ATTR_WSS_PORT);
        } else {
            httpsPortAttrVal = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_HTTPS_PORT);
        }

        AnnAttrValue keyStoreFileAttrVal = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_KEY_STORE_FILE);
        AnnAttrValue keyStorePasswordAttrVal = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_KEY_STORE_PASS);
        AnnAttrValue certPasswordAttrVal = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_CERT_PASS);
        AnnAttrValue trustStoreFileAttrVal = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_TRUST_STORE_FILE);
        AnnAttrValue trustStorePasswordAttrVal = configInfo.getAnnAttrValue(
                HttpConstants.ANN_CONFIG_ATTR_TRUST_STORE_PASS);
        AnnAttrValue sslVerifyClientAttrVal = configInfo.getAnnAttrValue(
                HttpConstants.ANN_CONFIG_ATTR_SSL_VERIFY_CLIENT);
        AnnAttrValue sslEnabledProtocolsAttrVal = configInfo
                .getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS);
        AnnAttrValue ciphersAttrVal = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_CIPHERS);
        AnnAttrValue sslProtocolAttrVal = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_SSL_PROTOCOL);
        AnnAttrValue hostAttrVal = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_HOST);
        AnnAttrValue certificateValidationEnabledAttrValue = configInfo
                .getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_VALIDATE_CERT_ENABLED);
        AnnAttrValue cacheSizeAttrValue = configInfo.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_CACHE_SIZE);
        AnnAttrValue cacheValidityPeriodAttrValue = configInfo
                .getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_CACHE_VALIDITY_PERIOD);

        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        if (httpsPortAttrVal != null && httpsPortAttrVal.getIntValue() > 0) {
            listenerConfiguration.setPort(Math.toIntExact(httpsPortAttrVal.getIntValue()));
            listenerConfiguration.setScheme(HttpConstants.PROTOCOL_HTTPS);

            if (hostAttrVal != null && hostAttrVal.getStringValue() != null) {
                listenerConfiguration.setHost(hostAttrVal.getStringValue());
            } else {
                listenerConfiguration.setHost(HttpConstants.HTTP_DEFAULT_HOST);
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

            listenerConfiguration.setTLSStoreType(HttpConstants.PKCS_STORE_TYPE);
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
            if (certificateValidationEnabledAttrValue != null && certificateValidationEnabledAttrValue
                    .getBooleanValue()) {
                listenerConfiguration.setValidateCertEnabled(certificateValidationEnabledAttrValue.getBooleanValue());
                if (cacheSizeAttrValue != null) {
                    listenerConfiguration.setCacheSize((int) cacheSizeAttrValue.getIntValue());
                }
                if (cacheValidityPeriodAttrValue != null) {
                    listenerConfiguration.setCacheValidityPeriod((int) cacheValidityPeriodAttrValue.getIntValue());
                }
            }
            List<Parameter> serverParams = new ArrayList<>();
            Parameter serverCiphers;
            if (sslEnabledProtocolsAttrVal != null && sslEnabledProtocolsAttrVal.getStringValue() != null) {
                serverCiphers = new Parameter(HttpConstants.ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS,
                        sslEnabledProtocolsAttrVal.getStringValue());
                serverParams.add(serverCiphers);
            }

            if (ciphersAttrVal != null && ciphersAttrVal.getStringValue() != null) {
                serverCiphers = new Parameter(HttpConstants.ANN_CONFIG_ATTR_CIPHERS, ciphersAttrVal.getStringValue());
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

    public static void checkFunctionValidity(BStruct connectionStruct, HTTPCarbonMessage reqMsg) {
        serverConnectionStructCheck(reqMsg);
        methodInvocationCheck(connectionStruct, reqMsg);
    }

    private static void methodInvocationCheck(BStruct bStruct, HTTPCarbonMessage reqMsg) {
        if (bStruct.getNativeData(METHOD_ACCESSED) != null || reqMsg == null) {
            throw new IllegalStateException("illegal function invocation");
        }

        if (!is100ContinueRequest(reqMsg)) {
            bStruct.addNativeData(METHOD_ACCESSED, true);
        }
    }

    private static void serverConnectionStructCheck(HTTPCarbonMessage reqMsg) {
        if (reqMsg == null) {
            throw new BallerinaException("operation not allowed:invalid Connection variable");
        }
    }

    private static boolean is100ContinueRequest(HTTPCarbonMessage reqMsg) {
        return HttpConstants.HEADER_VAL_100_CONTINUE.equalsIgnoreCase(reqMsg.getHeader(HttpConstants.EXPECT_HEADER));
    }

    public static Annotation getServiceConfigAnnotation(Service service, String pkgPath) {
        List<Annotation> annotationList = service.getAnnotationList(pkgPath, HttpConstants.ANN_NAME_CONFIG);

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
        List<Annotation> annotationList = resource.getAnnotationList(pkgPath, HttpConstants.ANN_NAME_RESOURCE_CONFIG);

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

    private static int getIntValue(long val) {
        int intVal = (int) val;

        if (intVal != val) {
            throw new IllegalArgumentException("invalid argument: " + val);
        }

        return intVal;
    }

    /**
     * Extract generic error message.
     *
     * @param context Represent ballerina context.
     * @param errMsg  Error message.
     * @return Generic error message.
     */
    public static BStruct getGenericError(Context context, String errMsg) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(PACKAGE_BUILTIN);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);

        BStruct genericError = new BStruct(errorStructInfo.getType());
        genericError.setStringField(0, errMsg);
        return genericError;
    }
}

