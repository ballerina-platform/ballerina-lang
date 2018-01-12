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
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.TypeTags;
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
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contractimpl.HttpResponseStatusFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HTTPConnectorUtil;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ballerinalang.mime.util.Constants.CONTENT_TYPE;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS_INDEX;
import static org.ballerinalang.mime.util.Constants.HEADER_VALUE_STRUCT;
import static org.ballerinalang.mime.util.Constants.IS_ENTITY_BODY_PRESENT;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.PRIMARY_TYPE_INDEX;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.Constants.SIZE_INDEX;
import static org.ballerinalang.mime.util.Constants.SUBTYPE_INDEX;
import static org.ballerinalang.net.http.Constants.ENTITY_INDEX;
import static org.ballerinalang.net.http.Constants.HTTP_MESSAGE_INDEX;
import static org.ballerinalang.net.http.Constants.MESSAGE_OUTPUT_STREAM;

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

        OutputStream messageOutputStream = new HttpMessageDataStreamer(httpCarbonMessage).getOutputStream();
        BStruct entity = (BStruct) abstractNativeFunction.getRefArgument(context, ENTITY_INDEX);
        String baseType = getContentType(entity);
        boolean isBodyAvailable;

        if (baseType != null) {
            switch (baseType) {
                case org.ballerinalang.mime.util.Constants.TEXT_PLAIN:
                    isBodyAvailable = MimeUtil.isTextBodyPresent(entity);
                    break;
                case org.ballerinalang.mime.util.Constants.APPLICATION_JSON:
                    isBodyAvailable = MimeUtil.isJsonBodyPresent(entity);
                    break;
                case org.ballerinalang.mime.util.Constants.APPLICATION_XML:
                    isBodyAvailable = MimeUtil.isXmlBodyPresent(entity);
                    break;
                default:
                    isBodyAvailable = MimeUtil.isBinaryBodyPresent(entity);
                    break;
            }
        } else {
            baseType = org.ballerinalang.mime.util.Constants.OCTET_STREAM;
            isBodyAvailable = MimeUtil.isBinaryBodyPresent(entity);
        }
        HttpUtil.setHeaderToStruct(context, entity, CONTENT_TYPE, baseType);
        httpMessageStruct.addNativeData(MESSAGE_ENTITY, entity);
        if (isBodyAvailable) {
            httpMessageStruct.addNativeData(IS_ENTITY_BODY_PRESENT, true);
        }
        addMessageOutputStream(httpMessageStruct, messageOutputStream);
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
        boolean isEntityBodyAvailable = false;
        if (httpMessageStruct.getNativeData(IS_ENTITY_BODY_PRESENT) != null) {
            isEntityBodyAvailable = (Boolean) httpMessageStruct.getNativeData(IS_ENTITY_BODY_PRESENT);
        }
        if (entity != null && isEntityBodyRequired && !isEntityBodyAvailable) {
            populateEntityBody(context, httpMessageStruct, entity, isRequest);
        }
        if (entity == null) {
            entity = ConnectorUtils
                    .createAndGetStruct(context, org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME,
                            org.ballerinalang.mime.util.Constants.ENTITY);
            entity.setRefField(ENTITY_HEADERS_INDEX, new BMap<>());
            httpMessageStruct.addNativeData(MESSAGE_ENTITY, entity);
            httpMessageStruct.addNativeData(IS_ENTITY_BODY_PRESENT, false);
        }
        return abstractNativeFunction.getBValues(entity);
    }

    /**
     * Populate the entity with the entity body.
     *
     * @param context           Represent ballerina context
     * @param httpMessageStruct Represent ballerina request/response message
     * @param entity            Represent a ballerina entity
     * @param isRequest         boolean representing whether the message is a request or a response
     */
    private static void populateEntityBody(Context context, BStruct httpMessageStruct, BStruct entity,
            boolean isRequest) {
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        HttpMessageDataStreamer httpMessageDataStreamer = new HttpMessageDataStreamer(httpCarbonMessage);
        InputStream inputStream = httpMessageDataStreamer.getInputStream();
        String baseType = getContentType(entity);
        long contentLength = entity.getIntField(SIZE_INDEX);
        if (baseType != null) {
            switch (baseType) {
                case org.ballerinalang.mime.util.Constants.TEXT_PLAIN:
                case org.ballerinalang.mime.util.Constants.APPLICATION_FORM:
                    MimeUtil.readAndSetStringPayload(context, entity, inputStream, contentLength);
                    break;
                case org.ballerinalang.mime.util.Constants.APPLICATION_JSON:
                    MimeUtil.readAndSetJsonPayload(context, entity, inputStream, contentLength);
                    break;
                case org.ballerinalang.mime.util.Constants.APPLICATION_XML:
                    MimeUtil.readAndSetXmlPayload(context, entity, inputStream, contentLength);
                    break;
                default:
                    MimeUtil.readAndSetBinaryPayload(context, entity, inputStream, contentLength);
                    break;
            }
        } else {
            MimeUtil.readAndSetBinaryPayload(context, entity, inputStream, contentLength);
        }
        httpMessageStruct.addNativeData(MESSAGE_ENTITY, entity);
        httpMessageStruct.addNativeData(IS_ENTITY_BODY_PRESENT, true);
        OutputStream messageOutputStream = httpMessageDataStreamer.getOutputStream();
        HttpUtil.addMessageOutputStream(httpMessageStruct, messageOutputStream);
    }

    private static String getContentType(BStruct entity) {
        if (entity.getRefField(MEDIA_TYPE_INDEX) != null) {
            BStruct mediaType = (BStruct) entity.getRefField(MEDIA_TYPE_INDEX);
            if (mediaType != null) {
                return mediaType.getStringField(PRIMARY_TYPE_INDEX) + "/" + mediaType.getStringField(SUBTYPE_INDEX);
            }
        }
        return null;
    }

    public static void addMessageOutputStream(BStruct struct, OutputStream messageOutputStream) {
        struct.addNativeData(MESSAGE_OUTPUT_STREAM, messageOutputStream);
    }

    public static void closeMessageOutputStream(BStruct httpMsgStruct) {
        OutputStream messageOutputStream = (OutputStream) httpMsgStruct.getNativeData(MESSAGE_OUTPUT_STREAM);
        try {
            if (messageOutputStream != null) {
                messageOutputStream.close();
            }
        } catch (IOException e) {
            log.error("Couldn't close message output stream", e);
        }
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
        MessageDataSource outboundMessageSource = readMessageDataSource(httpMessageStruct);
        HttpResponseStatusFuture outboundResponseStatusFuture = sendOutboundResponse(requestMessage, responseMessage);

        if (outboundMessageSource != null) {
            outboundMessageSource.serializeData();
            HttpUtil.closeMessageOutputStream(httpMessageStruct);
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

    /**
     * Extract entity body from the request/response message and construct 'MessageDataSource' with the extracted
     * content.
     *
     * @param httpMessageStruct Represent request/response struct
     * @return Newly created 'MessageDataSource' from the entity body
     */
    public static MessageDataSource readMessageDataSource(BStruct httpMessageStruct) {
        boolean isEntityBodyAvailable = (Boolean) httpMessageStruct.getNativeData(IS_ENTITY_BODY_PRESENT);

        if (isEntityBodyAvailable) {
            BStruct entity = (BStruct) httpMessageStruct.getNativeData(MESSAGE_ENTITY);
            OutputStream messageOutputStream = (OutputStream) httpMessageStruct.getNativeData(MESSAGE_OUTPUT_STREAM);
            String baseType = getContentType(entity);
            if (baseType != null) {
                switch (baseType) {
                    case org.ballerinalang.mime.util.Constants.TEXT_PLAIN:
                        String textPayload = MimeUtil.getTextPayload(entity);
                        return new StringDataSource(textPayload, messageOutputStream);
                    case org.ballerinalang.mime.util.Constants.APPLICATION_JSON:
                        BJSON jsonPayload = MimeUtil.getJsonPayload(entity);
                        if (jsonPayload != null) {
                            jsonPayload.setOutputStream(messageOutputStream);
                            return jsonPayload;
                        }
                        break;
                    case org.ballerinalang.mime.util.Constants.APPLICATION_XML:
                        BXML xmlPayload = MimeUtil.getXmlPayload(entity);
                        if (xmlPayload != null) {
                            xmlPayload.setOutputStream(messageOutputStream);
                            return xmlPayload;
                        }
                        break;
                    default:
                        byte[] binaryPayload = MimeUtil.getBinaryPayload(entity);
                        return new BlobDataSource(binaryPayload, messageOutputStream);
                }
            } else {
                byte[] binaryPayload = MimeUtil.getBinaryPayload(entity);
                return new BlobDataSource(binaryPayload, messageOutputStream);
            }
        }
        return null;
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
        if (statusCode == 404) {
            sendOutboundResponse(requestMessage, createErrorMessage(errorMsg, statusCode));
        } else {
            sendOutboundResponse(requestMessage, createErrorMessage("", statusCode));
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

    public static void populateConnection(BStruct request, HTTPCarbonMessage cMsg) {
        request.addNativeData(Constants.TRANSPORT_MESSAGE, cMsg);
        request.setStringField(Constants.CONNECTION_HOST_INDEX,
                ((InetSocketAddress) cMsg.getProperty(Constants.LOCAL_ADDRESS)).getHostName());
        request.setIntField(Constants.CONNECTION_PORT_INDEX, (Integer) cMsg.getProperty(Constants.LISTENER_PORT));
    }

    public static void populateInboundRequest(BStruct request, BStruct entity, BStruct mediaType,
            HTTPCarbonMessage cMsg) {
        request.addNativeData(Constants.TRANSPORT_MESSAGE, cMsg);
        request.addNativeData(Constants.INBOUND_REQUEST, true);
        request.setStringField(Constants.REQUEST_PATH_INDEX, (String) cMsg.getProperty(Constants.REQUEST_URL));
        request.setStringField(Constants.REQUEST_METHOD_INDEX, (String) cMsg.getProperty(Constants.HTTP_METHOD));
        request.setStringField(Constants.REQUEST_VERSION_INDEX, (String) cMsg.getProperty(Constants.HTTP_VERSION));
        Map<String, String> resourceArgValues = (Map<String, String>) cMsg.getProperty(Constants.RESOURCE_ARGS);
        request.setStringField(Constants.REQUEST_REST_URI_POSTFIX_INDEX,
                                resourceArgValues.get(Constants.REST_URI_POSTFIX));

        if (cMsg.getHeader(Constants.USER_AGENT_HEADER) != null) {
            request.setStringField(Constants.REQUEST_USER_AGENT_INDEX, cMsg.getHeader(Constants.USER_AGENT_HEADER));
            cMsg.removeHeader(Constants.USER_AGENT_HEADER);
        }
        populateEntity(entity, mediaType, cMsg);
        request.addNativeData(MESSAGE_ENTITY, entity);
        request.addNativeData(IS_ENTITY_BODY_PRESENT, false);
    }

    /**
     * Populate inbound response with headers and entity.
     *
     * @param response  Ballerina struct to represent response
     * @param entity    Entity of the response
     * @param mediaType Content type of the response
     * @param cMsg      Represent carbon message.
     */
    public static void populateInboundResponse(BStruct response, BStruct entity, BStruct mediaType, HTTPCarbonMessage
            cMsg) {
        response.addNativeData(Constants.TRANSPORT_MESSAGE, cMsg);
        int statusCode = (Integer) cMsg.getProperty(Constants.HTTP_STATUS_CODE);
        response.setIntField(Constants.RESPONSE_STATUS_CODE_INDEX, statusCode);
        response.setStringField(Constants.RESPONSE_REASON_PHRASE_INDEX,
                HttpResponseStatus.valueOf(statusCode).reasonPhrase());

        if (cMsg.getHeader(Constants.SERVER_HEADER) != null) {
            response.setStringField(Constants.RESPONSE_SERVER_INDEX, cMsg.getHeader(Constants.SERVER_HEADER));
            cMsg.removeHeader(Constants.SERVER_HEADER);
        }
        populateEntity(entity, mediaType, cMsg);
        response.addNativeData(MESSAGE_ENTITY, entity);
        response.addNativeData(IS_ENTITY_BODY_PRESENT, false);
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
        String lengthStr = cMsg.getHeader(Constants.HTTP_CONTENT_LENGTH);
        try {
            contentLength = lengthStr != null ? Integer.parseInt(lengthStr) : contentLength;
            MimeUtil.setContentLength(entity, contentLength);
        } catch (NumberFormatException e) {
            throw new BallerinaException("Invalid content length");
        }
        entity.setRefField(ENTITY_HEADERS_INDEX, prepareHeaderMap(cMsg.getHeaders(), new BMap<>()));
    }

    @SuppressWarnings("unchecked")
    public static void populateOutboundRequest(BStruct message, HTTPCarbonMessage reqMsg) {
        setHeadersToTransportMessage(reqMsg, message);
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

    public static void setHeadersToTransportMessage(HTTPCarbonMessage outboundRequest, BStruct messageStruct) {
        BStruct entityStruct = (BStruct) messageStruct.getNativeData(MESSAGE_ENTITY);
        outboundRequest.getHeaders().clear();
        HttpHeaders removedHeaders = messageStruct.getType().getName().equals(Constants.REQUEST) ?
                getRequestStructHeaders(messageStruct) : getResponseStructHeaders(messageStruct);

        BMap<String, BValue> headers = getEntityStructHeaders(entityStruct, removedHeaders);
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
    private static BMap<String, BValue> getEntityStructHeaders(BStruct struct, HttpHeaders removedHeaders) {
        BMap<String, BValue> headers = (BMap) struct.getRefField(ENTITY_HEADERS_INDEX);
        if (headers == null) {
            return null;
        }
        return prepareHeaderMap(removedHeaders, headers);
    }

    @SuppressWarnings("unchecked")
    private static HttpHeaders getRequestStructHeaders(BStruct struct) {
        HttpHeaders removedHeaders = new DefaultHttpHeaders();
        if (!struct.getStringField(Constants.REQUEST_USER_AGENT_INDEX).isEmpty()) {
            removedHeaders.add(Constants.USER_AGENT_HEADER, struct.getStringField(Constants.REQUEST_USER_AGENT_INDEX));
        }
        return removedHeaders;
    }

    @SuppressWarnings("unchecked")
    private static HttpHeaders getResponseStructHeaders(BStruct struct) {
        HttpHeaders removedHeaders = new DefaultHttpHeaders();
        if (!struct.getStringField(Constants.RESPONSE_SERVER_INDEX).isEmpty()) {
            removedHeaders.add(Constants.SERVER_HEADER, struct.getStringField(Constants.RESPONSE_SERVER_INDEX));
        }
        return removedHeaders;
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
                PROTOCOL_PACKAGE_MIME, HEADER_VALUE_STRUCT).getType() : headerValueStructType;
        BMap<String, BValue> headerMap = struct.getRefField(ENTITY_HEADERS_INDEX) != null ?
                (BMap) struct.getRefField(ENTITY_HEADERS_INDEX) : new BMap<>();
        struct.setRefField(ENTITY_HEADERS_INDEX, prepareHeaderMap(new DefaultHttpHeaders().add(key, value), headerMap));
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

    public static void checkFunctionValidity(BStruct bStruct, HTTPCarbonMessage reqMsg) {
        serverConnectionStructCheck(reqMsg);
        methodInvocationCheck(bStruct, reqMsg);
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
        return Constants.HEADER_VAL_100_CONTINUE.equalsIgnoreCase(reqMsg.getHeader(Constants.EXPECT_HEADER));
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
