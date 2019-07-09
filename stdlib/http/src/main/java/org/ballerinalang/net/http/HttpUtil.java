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
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.bre.Context;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.JSONGenerator;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.StreamingJsonValue;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityWrapper;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDataSource;
import org.ballerinalang.mime.util.MultipartDecoder;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.caching.RequestCacheControlObj;
import org.ballerinalang.net.http.caching.ResponseCacheControlObj;
import org.ballerinalang.net.http.session.Session;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.transactions.TransactionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contract.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.config.ForwardedExtensionConfig;
import org.wso2.transport.http.netty.contract.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;
import org.wso2.transport.http.netty.contract.config.Parameter;
import org.wso2.transport.http.netty.contract.config.ProxyServerConfiguration;
import org.wso2.transport.http.netty.contract.config.RequestSizeValidationConfig;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.config.SslConfiguration;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.PoolConfiguration;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpHeaderNames.CACHE_CONTROL;
import static org.ballerinalang.mime.util.EntityBodyHandler.checkEntityBodyAvailability;
import static org.ballerinalang.mime.util.MimeConstants.BOUNDARY;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.MimeConstants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.OCTET_STREAM;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.MimeConstants.REQUEST_ENTITY_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.RESPONSE_ENTITY_FIELD;
import static org.ballerinalang.net.http.HttpConstants.ALWAYS;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_COMPRESSION_CONTENT_TYPES;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_COMPRESSION_ENABLE;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.AUTO;
import static org.ballerinalang.net.http.HttpConstants.COLON;
import static org.ballerinalang.net.http.HttpConstants.CONNECTION_MANAGER;
import static org.ballerinalang.net.http.HttpConstants.CONNECTION_POOLING_MAX_ACTIVE_STREAMS_PER_CONNECTION;
import static org.ballerinalang.net.http.HttpConstants.ENABLED_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_CERTIFICATE;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_HANDSHAKE_TIMEOUT;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_KEY;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_KEY_PASSWORD;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_KEY_STORE;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_OCSP_STAPLING;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_SESSION_TIMEOUT;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_TRUST_CERTIFICATES;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_TRUST_STORE;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_VALIDATE_CERT;
import static org.ballerinalang.net.http.HttpConstants.FILE_PATH;
import static org.ballerinalang.net.http.HttpConstants.HTTP_ERROR_CODE;
import static org.ballerinalang.net.http.HttpConstants.HTTP_ERROR_MESSAGE;
import static org.ballerinalang.net.http.HttpConstants.HTTP_ERROR_RECORD;
import static org.ballerinalang.net.http.HttpConstants.MUTUAL_SSL_HANDSHAKE_RECORD;
import static org.ballerinalang.net.http.HttpConstants.NEVER;
import static org.ballerinalang.net.http.HttpConstants.PASSWORD;
import static org.ballerinalang.net.http.HttpConstants.PKCS_STORE_TYPE;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTPS;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;
import static org.ballerinalang.net.http.HttpConstants.REQUEST_CACHE_CONTROL;
import static org.ballerinalang.net.http.HttpConstants.REQUEST_CACHE_CONTROL_FIELD;
import static org.ballerinalang.net.http.HttpConstants.REQUEST_MUTUAL_SSL_HANDSHAKE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.REQUEST_MUTUAL_SSL_HANDSHAKE_STATUS;
import static org.ballerinalang.net.http.HttpConstants.RESOLVED_REQUESTED_URI;
import static org.ballerinalang.net.http.HttpConstants.RESOLVED_REQUESTED_URI_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_CACHE_CONTROL;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_CACHE_CONTROL_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_REASON_PHRASE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_STATUS_CODE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.SSL_CONFIG_ENABLE_SESSION_CREATION;
import static org.ballerinalang.net.http.HttpConstants.SSL_CONFIG_SSL_VERIFY_CLIENT;
import static org.ballerinalang.net.http.HttpConstants.SSL_ENABLED_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.SSL_PROTOCOL_VERSION;
import static org.ballerinalang.net.http.HttpConstants.TRANSPORT_MESSAGE;
import static org.ballerinalang.net.http.nativeimpl.pipelining.PipeliningHandler.sendPipelinedResponse;
import static org.ballerinalang.runtime.Constants.BALLERINA_VERSION;
import static org.wso2.transport.http.netty.contract.Constants.ENCODING_GZIP;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_TRANSFER_ENCODING_IDENTITY;

/**
 * Utility class providing utility methods.
 */
public class HttpUtil {

    public static final boolean TRUE = true;
    public static final boolean FALSE = false;

    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    private static final String METHOD_ACCESSED = "isMethodAccessed";
    private static final String IO_EXCEPTION_OCCURED = "I/O exception occurred";
    private static final String CHUNKING_CONFIG = "chunking_config";

//    public static Object[] getProperty(Context context, boolean isRequest) {
//        BMap<String, BValue> httpMessageStruct = (BMap<String, BValue>) context.getRefArgument(0);
//        HttpCarbonMessage httpCarbonMessage = HttpUtil
//                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
//        String propertyName = context.getStringArgument(0);
//
//        Object propertyValue = httpCarbonMessage.getProperty(propertyName);
//
//        if (propertyValue == null) {
//            return new BValue[0];
//        }
//
//        if (propertyValue instanceof String) {
//            return new BValue[] { new BString((String) propertyValue) };
//        } else {
//            throw new BallerinaException("Property value is of unknown type : " + propertyValue.getClass().getName());
//        }
//    }

//    public static void setProperty(Context context, boolean isRequest) {
//        BMap<String, BValue> httpMessageStruct = (BMap<String, BValue>) context.getRefArgument(0);
//        String propertyName = context.getStringArgument(0);
//        String propertyValue = context.getStringArgument(1);
//
//        if (propertyName != null && propertyValue != null) {
//            HttpCarbonMessage httpCarbonMessage = HttpUtil
//                    .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
//            httpCarbonMessage.setProperty(propertyName, propertyValue);
//        }
//    }

    /**
     * Set new entity to in/out request/response struct.
     *
     * @param httpMessageStruct request/response struct.
     * @return created entity.
     */
    public static ObjectValue createNewEntity(ObjectValue httpMessageStruct) {
        ObjectValue entity = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_MIME, ENTITY);
        HttpCarbonMessage httpCarbonMessage = HttpUtil.getCarbonMsg(httpMessageStruct,
                HttpUtil.createHttpCarbonMessage(isRequest(httpMessageStruct)));
        entity.addNativeData(ENTITY_HEADERS, httpCarbonMessage.getHeaders());
        entity.addNativeData(ENTITY_BYTE_CHANNEL, null);
        httpMessageStruct.set(isRequest(httpMessageStruct) ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD
                , entity);
        httpMessageStruct.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, false);
        return entity;
    }

    /**
     * Set the given entity to request or response message.
     *
     * @param messageObj Represent ballerina request/response
     * @param entityObj  Represent an entity
     * @param isRequest  boolean representing whether the message is a request or a response
     */
    public static void setEntity(ObjectValue messageObj, ObjectValue entityObj, boolean isRequest) {
        HttpCarbonMessage httpCarbonMessage = HttpUtil.getCarbonMsg(messageObj,
                HttpUtil.createHttpCarbonMessage(isRequest));
        String contentType = MimeUtil.getContentTypeWithParameters(entityObj);
        if (checkEntityBodyAvailability(entityObj)) {
            httpCarbonMessage.waitAndReleaseAllEntities();
            if (contentType == null) {
                contentType = OCTET_STREAM;
            }
            HeaderUtil.setHeaderToEntity(entityObj, HttpHeaderNames.CONTENT_TYPE.toString(), contentType);
        }
        messageObj.set(isRequest ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD, entityObj);
        messageObj.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, checkEntityBodyAvailability(entityObj));
    }

    /**
     * Get the entity from request or response.
     *
     * @param messageObj         Ballerina context
     * @param isRequest          boolean representing whether the message is a request or a response
     * @param entityBodyRequired boolean representing whether the entity body is required
     * @return Entity of the request or response
     */
    public static ObjectValue getEntity(ObjectValue messageObj, boolean isRequest, boolean entityBodyRequired) {
        ObjectValue entity = (ObjectValue) messageObj.get(isRequest ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD);
        boolean byteChannelAlreadySet = false;

        if (messageObj.getNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET) != null) {
            byteChannelAlreadySet = (Boolean) messageObj.getNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET);
        }
        if (entityBodyRequired && !byteChannelAlreadySet) {
            populateEntityBody(messageObj, entity, isRequest, false);
        }
        return entity;
    }

    /**
     * Populate entity with the relevant body content.
     *
     * @param messageObj Represent ballerina request/response
     * @param entityObj     Represent an entity
     * @param request    boolean representing whether the message is a request or a response
     * @param streaming  boolean representing whether the entity requires byte channel or message as native data
     */
    public static void populateEntityBody(ObjectValue messageObj, ObjectValue entityObj, boolean request,
                                          boolean streaming) {
        HttpCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(messageObj, HttpUtil.createHttpCarbonMessage(request));
        String contentType = httpCarbonMessage.getHeader(HttpHeaderNames.CONTENT_TYPE.toString());
        //TODO check following condition related to streaming
        if (MimeUtil.isNotNullAndEmpty(contentType) && contentType.startsWith(MULTIPART_AS_PRIMARY_TYPE)
                && !streaming) {
            MultipartDecoder.parseBody(entityObj, contentType,
                                       new HttpMessageDataStreamer(httpCarbonMessage).getInputStream());
        } else {
            long contentLength = MimeUtil.extractContentLength(httpCarbonMessage);
            if (contentLength > 0) {
                if (streaming) {
                    entityObj.addNativeData(ENTITY_BYTE_CHANNEL, new EntityWrapper(
                            new EntityBodyChannel(new HttpMessageDataStreamer(httpCarbonMessage).getInputStream())));
                } else {
                    entityObj.addNativeData(TRANSPORT_MESSAGE, httpCarbonMessage);
                }
            }
        }
        messageObj.set(request ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD, entityObj);
        messageObj.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
    }

    public static ObjectValue extractEntity(ObjectValue request) {
        Object isEntityBodyAvailable = request.getNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET);
        if (isEntityBodyAvailable == null || !((Boolean) isEntityBodyAvailable)) {
            return null;
        }
        return (ObjectValue) request.get(isRequest(request) ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD);
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

    public static void prepareOutboundResponse(ObjectValue connectionObj, HttpCarbonMessage inboundRequestMsg,
                                               HttpCarbonMessage outboundResponseMsg,
                                               ObjectValue outboundResponseObj) {
        HttpUtil.checkEntityAvailability(outboundResponseObj);
        HttpUtil.addHTTPSessionAndCorsHeaders(inboundRequestMsg, outboundResponseMsg);
        HttpUtil.enrichOutboundMessage(outboundResponseMsg, outboundResponseObj);
        HttpService httpService = (HttpService) connectionObj.getNativeData(HttpConstants.HTTP_SERVICE);
        HttpUtil.setCompressionHeaders(httpService.getCompressionConfig(), inboundRequestMsg, outboundResponseMsg);
        HttpUtil.setChunkingHeader(httpService.getChunkingConfig(), outboundResponseMsg);
    }

    public static BMap<String, BValue> createSessionStruct(Context context, Session session) {
        BMap<String, BValue> sessionStruct = ConnectorUtils
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

    private static void addHTTPSessionAndCorsHeaders(HttpCarbonMessage requestMsg, HttpCarbonMessage responseMsg) {
        //TODO Remove once service session LC is introduced
//        Session session = (Session) requestMsg.getProperty(HttpConstants.HTTP_SESSION);
//        if (session != null) {
//            boolean isSecureRequest = false;
//            AnnAttachmentInfo configAnn = context.getServiceInfo().getAnnotationAttachmentInfo(
//                    HttpConstants.PROTOCOL_PACKAGE_HTTP, HttpConstants.ANN_NAME_CONFIG);
//            if (configAnn != null) {
//                AnnAttributeValue httpsPortAttrVal = configAnn
//                        .getAttributeValue(HttpConstants.ANN_CONFIG_ATTR_HTTPS_PORT);
//                if (httpsPortAttrVal != null) {
//                    Integer listenerPort = (Integer) requestMsg.getProperty(HttpConstants.LISTENER_PORT);
//                    if (listenerPort != null && httpsPortAttrVal.getIntValue() == listenerPort) {
//                        isSecureRequest = true;
//                    }
//                }
//            }
//            session.generateSessionHeader(responseMsg, isSecureRequest);
//        }
        //Process CORS if exists.
        if (requestMsg.getHeader(HttpHeaderNames.ORIGIN.toString()) != null) {
            CorsHeaderGenerator.process(requestMsg, responseMsg, true);
        }
    }

    /**
     * This method should never be called directly to send out responses for ballerina HTTP 1.1. Use
     * PipeliningHandler's sendPipelinedResponse() method instead.
     *
     * @param requestMsg  Represent the request message
     * @param responseMsg Represent the corresponding response
     * @return HttpResponseFuture that represent the future results
     */
    public static HttpResponseFuture sendOutboundResponse(HttpCarbonMessage requestMsg,
                                                          HttpCarbonMessage responseMsg) {
        HttpResponseFuture responseFuture;
        try {
            responseFuture = requestMsg.respond(responseMsg);
        } catch (org.wso2.transport.http.netty.contract.ServerConnectorException e) {
            throw new BallerinaConnectorException("Error occurred during response", e);
        }
        return responseFuture;
    }

    /**
     * Sends an HTTP/2 Server Push message back to the client.
     *
     * @param requestMsg   the request message associated to the server push response
     * @param pushResponse the server push message
     * @param pushPromise  the push promise associated with the server push
     * @return the future to get notifications of the operation asynchronously
     */
    public static HttpResponseFuture pushResponse(HttpCarbonMessage requestMsg, HttpCarbonMessage pushResponse,
                                                  Http2PushPromise pushPromise) {
        HttpResponseFuture responseFuture;
        try {
            responseFuture = requestMsg.pushResponse(pushResponse, pushPromise);
        } catch (org.wso2.transport.http.netty.contract.ServerConnectorException e) {
            throw new BallerinaConnectorException("Error occurred while sending a server push message", e);
        }
        return responseFuture;
    }

    /**
     * Sends an HTTP/2 Push Promise message back to the client.
     *
     * @param requestMsg  the request message associated to the push promise
     * @param pushPromise the push promise message
     * @return the future to get notifications of the operation asynchronously
     */
    public static HttpResponseFuture pushPromise(HttpCarbonMessage requestMsg, Http2PushPromise pushPromise) {
        HttpResponseFuture responseFuture;
        try {
            responseFuture = requestMsg.pushPromise(pushPromise);
        } catch (org.wso2.transport.http.netty.contract.ServerConnectorException e) {
            throw new BallerinaConnectorException("Error occurred during response", e);
        }
        return responseFuture;
    }

    public static void handleFailure(HttpCarbonMessage requestMessage, BallerinaConnectorException ex) {
        String errorMsg = ex.getMessage();
        int statusCode = getStatusCode(requestMessage, errorMsg);
        sendPipelinedResponse(requestMessage, createErrorMessage(errorMsg, statusCode));
    }

    static void handleFailure(HttpCarbonMessage requestMessage, ErrorValue error) {
        String errorMsg = getErrorMessage(error);
        int statusCode = getStatusCode(requestMessage, errorMsg);
        //TODO Need a method to get printable stacktrace
//        ErrorHandlerUtils.printError("error: " + BLangVMErrors.getPrintableStackTrace(error));
        ErrorHandlerUtils.printError("error: " + error.toString());
        sendPipelinedResponse(requestMessage, createErrorMessage(errorMsg, statusCode));
    }

    private static String getErrorMessage(ErrorValue error) {
        String errorMsg = error.getReason();
        //TODO Test whether error.getDetails() can be casted to (MapValue)
        MapValue errorDetails = (MapValue) error.getDetails();
        if (!errorDetails.isEmpty()) {
            errorMsg = errorMsg.concat(COLON + errorDetails.get(HTTP_ERROR_MESSAGE));
        }
        return errorMsg;
    }

    public static int getStatusCode(HttpCarbonMessage requestMessage, String errorMsg) {
        Integer carbonStatusCode = requestMessage.getHttpStatusCode();
        if (carbonStatusCode == null) {
            //log only the internal server errors
            log.error(errorMsg);
            return HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
        }
        return carbonStatusCode;
    }

    public static HttpCarbonMessage createErrorMessage(String payload, int statusCode) {
        HttpCarbonMessage response = HttpUtil.createHttpCarbonMessage(false);
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

    private static void setHttpStatusCodes(int statusCode, HttpCarbonMessage response) {
        HttpHeaders httpHeaders = response.getHeaders();
        httpHeaders.set(HttpHeaderNames.CONTENT_TYPE, org.wso2.transport.http.netty.contract.Constants.TEXT_PLAIN);

        response.setHttpStatusCode(statusCode);
    }

    /**
     * Get error struct.
     *
     * @param errMsg  Error message
     * @return Error struct
     */
    public static ErrorValue getError(String errMsg) {
        MapValue<String, Object> httpErrorRecord = createHTTPErrorRecord();
        httpErrorRecord.put(HTTP_ERROR_MESSAGE, errMsg);
        return BallerinaErrors.createError(HTTP_ERROR_CODE, httpErrorRecord);
    }

    private static MapValue<String, Object> createHTTPErrorRecord() {
        return BallerinaValues.createRecordValue(PROTOCOL_PACKAGE_HTTP, HTTP_ERROR_RECORD);
    }

    /**
     * Get error struct from throwable.
     *
     * @param throwable Throwable representing the error.
     * @return Error struct
     */
    public static ErrorValue getError(Throwable throwable) {
        if (throwable.getMessage() == null) {
            return getError(IO_EXCEPTION_OCCURED);
        } else {
            return getError(throwable.getMessage());
        }
    }

    //TODO Remove after migration : implemented using bvm values/types
    public static HttpCarbonMessage getCarbonMsg(BMap<String, BValue> struct, HttpCarbonMessage defaultMsg) {
        HttpCarbonMessage httpCarbonMessage = (HttpCarbonMessage) struct.getNativeData(TRANSPORT_MESSAGE);
        if (httpCarbonMessage != null) {
            return httpCarbonMessage;
        }
        addCarbonMsg(struct, defaultMsg);
        return defaultMsg;
    }

    public static HttpCarbonMessage getCarbonMsg(ObjectValue struct, HttpCarbonMessage defaultMsg) {
        HttpCarbonMessage httpCarbonMessage = (HttpCarbonMessage) struct.getNativeData(TRANSPORT_MESSAGE);
        if (httpCarbonMessage != null) {
            return httpCarbonMessage;
        }
        addCarbonMsg(struct, defaultMsg);
        return defaultMsg;
    }

    /**
     * Gets the {@code Http2PushPromise} represented by the PushPromise struct.
     *
     * @param pushPromiseStruct  the push promise struct
     * @param defaultPushPromise the Http2PushPromise to use if the struct does not have native data of a push promise
     * @return the {@code Http2PushPromise} represented by the PushPromise struct
     */
    public static Http2PushPromise getPushPromise(ObjectValue pushPromiseStruct,
                                                  Http2PushPromise defaultPushPromise) {
        Http2PushPromise pushPromise =
                (Http2PushPromise) pushPromiseStruct.getNativeData(HttpConstants.TRANSPORT_PUSH_PROMISE);
        if (pushPromise != null) {
            return pushPromise;
        }
        pushPromiseStruct.addNativeData(HttpConstants.TRANSPORT_PUSH_PROMISE, defaultPushPromise);
        return defaultPushPromise;
    }

    /**
     * Populates the push promise struct from native {@code Http2PushPromise}.
     *  @param pushPromiseStruct the push promise struct
     * @param pushPromise the native Http2PushPromise
     */
    public static void populatePushPromiseStruct(ObjectValue pushPromiseStruct,
                                                 Http2PushPromise pushPromise) {
        pushPromiseStruct.addNativeData(HttpConstants.TRANSPORT_PUSH_PROMISE, pushPromise);
        pushPromiseStruct.set(HttpConstants.PUSH_PROMISE_PATH_FIELD, pushPromise.getPath());
        pushPromiseStruct.set(HttpConstants.PUSH_PROMISE_METHOD_FIELD, pushPromise.getMethod());
    }

    /**
     * Creates native {@code Http2PushPromise} from PushPromise struct.
     *
     * @param pushPromiseObj the PushPromise struct
     * @return the populated the native {@code Http2PushPromise}
     */
    public static Http2PushPromise createHttpPushPromise(ObjectValue pushPromiseObj) {
        String method = pushPromiseObj.get(HttpConstants.PUSH_PROMISE_METHOD_FIELD).toString();
        if (method == null || method.isEmpty()) {
            method = HttpConstants.HTTP_METHOD_GET;
        }

        String path = pushPromiseObj.get(HttpConstants.PUSH_PROMISE_PATH_FIELD).toString();
        if (path == null || path.isEmpty()) {
            path = HttpConstants.DEFAULT_BASE_PATH;
        }
        return new Http2PushPromise(method, path);
    }

    //TODO Remove after migration : implemented using bvm values/types
    public static void addCarbonMsg(BMap<String, BValue> struct, HttpCarbonMessage httpCarbonMessage) {
        struct.addNativeData(TRANSPORT_MESSAGE, httpCarbonMessage);
    }

    public static void addCarbonMsg(ObjectValue struct, HttpCarbonMessage httpCarbonMessage) {
        struct.addNativeData(TRANSPORT_MESSAGE, httpCarbonMessage);
    }

    public static void populateInboundRequest(ObjectValue inboundRequest, ObjectValue entity,
                                              ObjectValue mediaType, HttpCarbonMessage inboundRequestMsg) {
        inboundRequest.addNativeData(TRANSPORT_MESSAGE, inboundRequestMsg);
        inboundRequest.addNativeData(REQUEST, true);

        if (inboundRequestMsg.getProperty(HttpConstants.MUTUAL_SSL_RESULT) != null) {
            MapValue mutualSslRecord = BallerinaValues.createRecordValue(PROTOCOL_PACKAGE_HTTP,
                                                                         MUTUAL_SSL_HANDSHAKE_RECORD);
            mutualSslRecord.put(REQUEST_MUTUAL_SSL_HANDSHAKE_STATUS,
                                inboundRequestMsg.getProperty(HttpConstants.MUTUAL_SSL_RESULT));
            inboundRequest.set(REQUEST_MUTUAL_SSL_HANDSHAKE_FIELD, mutualSslRecord);
        }

        enrichWithInboundRequestInfo(inboundRequest, inboundRequestMsg);
        enrichWithInboundRequestHeaders(inboundRequest, inboundRequestMsg);

        populateEntity(entity, mediaType, inboundRequestMsg);
        inboundRequest.set(REQUEST_ENTITY_FIELD, entity);
        inboundRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, false);

        String cacheControlHeader = inboundRequestMsg.getHeader(CACHE_CONTROL.toString());
        if (cacheControlHeader != null) {
            ObjectValue cacheControlObj = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_HTTP,
                                                                               REQUEST_CACHE_CONTROL);
            RequestCacheControlObj requestCacheControl = new RequestCacheControlObj(cacheControlObj);
            requestCacheControl.populateStruct(cacheControlHeader);
            inboundRequest.set(REQUEST_CACHE_CONTROL_FIELD, requestCacheControl.getObj());
        }
    }

    private static void enrichWithInboundRequestHeaders(ObjectValue inboundRequestObj,
                                                        HttpCarbonMessage inboundRequestMsg) {
        if (inboundRequestMsg.getHeader(HttpHeaderNames.USER_AGENT.toString()) != null) {
            String agent = inboundRequestMsg.getHeader(HttpHeaderNames.USER_AGENT.toString());
            inboundRequestObj.set(HttpConstants.REQUEST_USER_AGENT_FIELD, agent);
            inboundRequestMsg.removeHeader(HttpHeaderNames.USER_AGENT.toString());
        }
    }

    private static void enrichWithInboundRequestInfo(ObjectValue inboundRequestObj,
                                                     HttpCarbonMessage inboundRequestMsg) {
        inboundRequestObj.set(HttpConstants.REQUEST_RAW_PATH_FIELD, inboundRequestMsg.getRequestUrl());
        inboundRequestObj.set(HttpConstants.REQUEST_METHOD_FIELD, inboundRequestMsg.getHttpMethod());
        inboundRequestObj.set(HttpConstants.REQUEST_VERSION_FIELD, inboundRequestMsg.getHttpVersion());
        HttpResourceArguments resourceArgValues = (HttpResourceArguments) inboundRequestMsg.getProperty(
                HttpConstants.RESOURCE_ARGS);
        if (resourceArgValues != null && resourceArgValues.getMap().get(HttpConstants.EXTRA_PATH_INFO) != null) {
            inboundRequestObj.set(HttpConstants.REQUEST_EXTRA_PATH_INFO_FIELD,
                                  resourceArgValues.getMap().get(HttpConstants.EXTRA_PATH_INFO));
        }
    }

    /**
     * Populates the HTTP caller with native data.
     *
     * @param caller     Represents the HTTP caller
     * @param inboundMsg Represents carbon message
     * @param config     Represents service endpoint configuration
     */
    public static void enrichHttpCallerWithNativeData(ObjectValue caller, HttpCarbonMessage inboundMsg,
                                                      MapValue config) {
        caller.addNativeData(HttpConstants.TRANSPORT_MESSAGE, inboundMsg);
//        caller.put(HttpConstants.HTTP_CONNECTOR_CONFIG_FIELD, (BMap<String, BValue>) config.getVMValue());
    }

    /**
     * Populates the HTTP caller with connection information.
     * @param httpCaller   Represents the HTTP caller
     * @param inboundMsg   Represents the carbon message
     * @param httpResource Represents the Http Resource
     * @param config       Represents the service endpoint configuration
     */
    public static void enrichHttpCallerWithConnectionInfo(ObjectValue httpCaller, HttpCarbonMessage inboundMsg,
                                                          HttpResource httpResource, MapValue config) {
        MapValue<String, Object> remote = BallerinaValues.createRecordValue(PROTOCOL_PACKAGE_HTTP,
                                                                            HttpConstants.REMOTE);
        MapValue<String, Object> local = BallerinaValues.createRecordValue(PROTOCOL_PACKAGE_HTTP, HttpConstants.LOCAL);

        Object remoteSocketAddress = inboundMsg.getProperty(HttpConstants.REMOTE_ADDRESS);
        if (remoteSocketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteSocketAddress;
            String remoteHost = inetSocketAddress.getHostName();
            long remotePort = inetSocketAddress.getPort();
            remote.put(HttpConstants.REMOTE_HOST_FIELD, remoteHost);
            remote.put(HttpConstants.REMOTE_PORT_FIELD, remotePort);
        }
        httpCaller.set(HttpConstants.REMOTE_STRUCT_FIELD, remote);

        Object localSocketAddress = inboundMsg.getProperty(HttpConstants.LOCAL_ADDRESS);
        if (localSocketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) localSocketAddress;
            String localHost = inetSocketAddress.getHostName();
            long localPort = inetSocketAddress.getPort();
            local.put(HttpConstants.LOCAL_HOST_FIELD, localHost);
            local.put(HttpConstants.LOCAL_PORT_FIELD, localPort);
        }
        httpCaller.set(HttpConstants.LOCAL_STRUCT_INDEX, local);
        httpCaller.set(HttpConstants.SERVICE_ENDPOINT_PROTOCOL_FIELD, inboundMsg.getProperty(HttpConstants.PROTOCOL));
        httpCaller.set(HttpConstants.SERVICE_ENDPOINT_CONFIG_FIELD, config);
        httpCaller.addNativeData(HttpConstants.HTTP_SERVICE, httpResource.getParentService());
    }

    /**
     * Populate inbound response with headers and entity.
     * @param inboundResponse  Ballerina struct to represent response
     * @param entity    Entity of the response
     * @param mediaType Content type of the response
     * @param inboundResponseMsg      Represent carbon message.
     */
    public static void populateInboundResponse(ObjectValue inboundResponse, ObjectValue entity,
                                               ObjectValue mediaType, HttpCarbonMessage inboundResponseMsg) {
        inboundResponse.addNativeData(TRANSPORT_MESSAGE, inboundResponseMsg);
        int statusCode = (Integer) inboundResponseMsg.getHttpStatusCode();
        inboundResponse.set(RESPONSE_STATUS_CODE_FIELD, (long) statusCode);
        inboundResponse.set(RESPONSE_REASON_PHRASE_FIELD,
                HttpResponseStatus.valueOf(statusCode).reasonPhrase());

        if (inboundResponseMsg.getHeader(HttpHeaderNames.SERVER.toString()) != null) {
            inboundResponse.set(HttpConstants.RESPONSE_SERVER_FIELD,
                    inboundResponseMsg.getHeader(HttpHeaderNames.SERVER.toString()));
            inboundResponseMsg.removeHeader(HttpHeaderNames.SERVER.toString());
        }

        if (inboundResponseMsg.getProperty(RESOLVED_REQUESTED_URI) != null) {
            inboundResponse.set(RESOLVED_REQUESTED_URI_FIELD,
                    inboundResponseMsg.getProperty(RESOLVED_REQUESTED_URI).toString());
        }

        String cacheControlHeader = inboundResponseMsg.getHeader(CACHE_CONTROL.toString());
        if (cacheControlHeader != null) {
            ResponseCacheControlObj responseCacheControl
                    = new ResponseCacheControlObj(PROTOCOL_PACKAGE_HTTP, RESPONSE_CACHE_CONTROL);
            responseCacheControl.populateStruct(cacheControlHeader);
            inboundResponse.set(RESPONSE_CACHE_CONTROL_FIELD, responseCacheControl.getObj());
        }

        populateEntity(entity, mediaType, inboundResponseMsg);
        inboundResponse.set(RESPONSE_ENTITY_FIELD, entity);
        inboundResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, false);
    }

    /**
     * Populate entity with headers, content-type and content-length.
     *
     * @param entity    Represent an entity struct
     * @param mediaType mediaType struct that needs to be set to the entity
     * @param cMsg      Represent a carbon message
     */
    private static void populateEntity(ObjectValue entity, ObjectValue mediaType, HttpCarbonMessage cMsg) {
        String contentType = cMsg.getHeader(HttpHeaderNames.CONTENT_TYPE.toString());
        MimeUtil.setContentType(mediaType, entity, contentType);
        long contentLength = -1;
        String lengthStr = cMsg.getHeader(HttpHeaderNames.CONTENT_LENGTH.toString());
        try {
            contentLength = lengthStr != null ? Long.parseLong(lengthStr) : contentLength;
            MimeUtil.setContentLength(entity, contentLength);
        } catch (NumberFormatException e) {
            throw new BallerinaException("Invalid content length");
        }
        entity.addNativeData(ENTITY_HEADERS, cMsg.getHeaders());
    }

    /**
     * Set headers and properties of request/response struct to the outbound transport message.
     *
     * @param outboundMsg    transport Http carbon message.
     * @param outboundMsgObj req/resp struct.
     */
    public static void enrichOutboundMessage(HttpCarbonMessage outboundMsg, ObjectValue outboundMsgObj) {
        setHeadersToTransportMessage(outboundMsg, outboundMsgObj);
        setPropertiesToTransportMessage(outboundMsg, outboundMsgObj);
    }

    @SuppressWarnings("unchecked")
    private static void setHeadersToTransportMessage(HttpCarbonMessage outboundMsg, ObjectValue messageObj) {
        ObjectValue entityObj = (ObjectValue) messageObj
                .get(isRequest(messageObj) ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD);
        HttpHeaders transportHeaders = outboundMsg.getHeaders();
        if (isRequest(messageObj) || isResponse(messageObj)) {
            addRemovedPropertiesBackToHeadersMap(messageObj, transportHeaders);
            // Since now the InRequest & OutRequest are merged to a single Request and InResponse & OutResponse
            // are merged to a single Response, without returning need to populate all headers from the struct
            // to the HttpCarbonMessage.
            // TODO: refactor this logic properly.
            // return;
        }
        HttpHeaders httpHeaders = (HttpHeaders) entityObj.getNativeData(ENTITY_HEADERS);
        if (httpHeaders != transportHeaders) {
            //This is done only when the entity map and transport message do not refer to the same header map
            if (httpHeaders != null) {
                transportHeaders.add(httpHeaders);
            }
            //Once the headers are synced, set the entity headers to transport message headers so that they
            //both refer the same header map for future operations
            entityObj.addNativeData(ENTITY_HEADERS, outboundMsg.getHeaders());
        }
    }

    private static boolean isRequest(ObjectValue value) {
        return value.getType().getName().equals(REQUEST);
    }

    private static boolean isResponse(ObjectValue value) {
        return value.getType().getName().equals(HttpConstants.RESPONSE);
    }

    private static void addRemovedPropertiesBackToHeadersMap(ObjectValue messageObj, HttpHeaders transportHeaders) {
        if (isRequest(messageObj)) {
            Object userAgent = messageObj.get(HttpConstants.REQUEST_USER_AGENT_FIELD);
            if (userAgent != null && !userAgent.toString().isEmpty()) {
                transportHeaders.set(HttpHeaderNames.USER_AGENT.toString(), userAgent.toString());
            }
        } else {
            Object server = messageObj.get(HttpConstants.RESPONSE_SERVER_FIELD);
            if (server != null && !server.toString().isEmpty()) {
                transportHeaders.set(HttpHeaderNames.SERVER.toString(), server.toString());
            }
        }
    }

    private static void setPropertiesToTransportMessage(HttpCarbonMessage outboundResponseMsg, ObjectValue messageObj) {
        if (isResponse(messageObj)) {
            //TODO fix following logic
            long statusCode = (Long) messageObj.get(RESPONSE_STATUS_CODE_FIELD);
            if (statusCode != 0) {
                outboundResponseMsg.setHttpStatusCode(getIntValue(statusCode));
            }
            Object respPhrase = messageObj.get(RESPONSE_REASON_PHRASE_FIELD);
            if (respPhrase != null && !respPhrase.toString().isEmpty()) {
                outboundResponseMsg.setProperty(HttpConstants.HTTP_REASON_PHRASE, respPhrase.toString());
            }
        }
    }

    /**
     * Check the existence of entity. Set new entity of not present.
     *
     * @param value  request/response struct.
     */
    public static void checkEntityAvailability(ObjectValue value) {
        ObjectValue entity = (ObjectValue) value.get(isRequest(value) ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD);
        if (entity == null) {
            createNewEntity(value);
        }
    }

    /**
     * Check the existence of content-length and transfer-encoding headers.
     *
     * @param message transport message
     * @return true if the headers are available else false.
     */
    public static Boolean checkRequestBodySizeHeadersAvailability(HttpCarbonMessage message) {
        String contentLength = message.getHeader(HttpHeaderNames.CONTENT_LENGTH.toString());
        String transferEncoding = message.getHeader(HttpHeaderNames.TRANSFER_ENCODING.toString());
        return contentLength != null || transferEncoding != null;
    }

    /**
     * Check the existence of the message entity data source.
     *
     * @param value  request/response struct.
     * @return true if the message entity data source is available else false.
     */
    public static boolean isEntityDataSourceAvailable(ObjectValue value) {
        ObjectValue entityObj = (ObjectValue) value
                .get(isRequest(value) ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD);
        return (entityObj != null && EntityBodyHandler.getMessageDataSource(entityObj) != null);
    }

    private static void setCompressionHeaders(MapValue<String, Object> compressionConfig, HttpCarbonMessage requestMsg,
                                              HttpCarbonMessage outboundResponseMsg) {
        if (!checkConfigAnnotationAvailability(compressionConfig)) {
            return;
        }
        String contentEncoding = outboundResponseMsg.getHeaders().get(HttpHeaderNames.CONTENT_ENCODING);
        if (contentEncoding != null) {
            return;
        }
        CompressionConfigState compressionState = getCompressionState(
                compressionConfig.getStringValue(ANN_CONFIG_ATTR_COMPRESSION_ENABLE));
        if (compressionState == CompressionConfigState.NEVER) {
            outboundResponseMsg.getHeaders().set(HttpHeaderNames.CONTENT_ENCODING, HTTP_TRANSFER_ENCODING_IDENTITY);
            return;
        }

        String acceptEncodingValue = requestMsg.getHeaders().get(HttpHeaderNames.ACCEPT_ENCODING);
        List<String> contentTypesAnnotationValues = getAsStringList(
                compressionConfig.getArrayValue(ANN_CONFIG_ATTR_COMPRESSION_CONTENT_TYPES).getStringArray());
        String contentType = outboundResponseMsg.getHeader(HttpHeaderNames.CONTENT_TYPE.toString());

        if (contentTypesAnnotationValues.isEmpty() || isContentTypeMatched(contentTypesAnnotationValues, contentType)) {
            if (compressionState == CompressionConfigState.ALWAYS &&
                    (acceptEncodingValue == null || HTTP_TRANSFER_ENCODING_IDENTITY.equals(acceptEncodingValue))) {
                outboundResponseMsg.getHeaders().set(HttpHeaderNames.CONTENT_ENCODING, ENCODING_GZIP);
            }
        } else {
            outboundResponseMsg.getHeaders().set(HttpHeaderNames.CONTENT_ENCODING, HTTP_TRANSFER_ENCODING_IDENTITY);
        }
    }

    public static CompressionConfigState getCompressionState(String compressionState) {
        switch (compressionState) {
            case AUTO:
                return CompressionConfigState.AUTO;
            case ALWAYS:
                return CompressionConfigState.ALWAYS;
            case NEVER:
                return CompressionConfigState.NEVER;
            default:
                return null;
        }
    }

    private static boolean isContentTypeMatched(List<String> contentTypes, String contentType) {
        return contentType != null && contentTypes.stream().anyMatch(contentType.toLowerCase()::contains);
    }

    private static List<String> getAsStringList(Object[] values) {
        List<String> valuesList = new ArrayList<>();
        if (values == null) {
            return valuesList;
        }
        for (Object val : values) {
            valuesList.add(val.toString().trim().toLowerCase());
        }
        return valuesList;
    }

    public static String getListenerInterface(String host, int port) {
        host = host != null ? host : "0.0.0.0";
        return host + ":" + port;
    }

    public static ChunkConfig getChunkConfig(String chunkConfig) {
        switch (chunkConfig) {
            case HttpConstants.AUTO:
                return ChunkConfig.AUTO;
            case HttpConstants.ALWAYS:
                return ChunkConfig.ALWAYS;
            case NEVER:
                return ChunkConfig.NEVER;
            default:
                throw new BallerinaConnectorException(
                        "Invalid configuration found for Transfer-Encoding: " + chunkConfig);
        }
    }

    public static KeepAliveConfig getKeepAliveConfig(String keepAliveConfig) {
        switch (keepAliveConfig) {
            case HttpConstants.AUTO:
                return KeepAliveConfig.AUTO;
            case HttpConstants.ALWAYS:
                return KeepAliveConfig.ALWAYS;
            case NEVER:
                return KeepAliveConfig.NEVER;
            default:
                throw new BallerinaConnectorException(
                        "Invalid configuration found for Keep-Alive: " + keepAliveConfig);
        }
    }

    public static ForwardedExtensionConfig getForwardedExtensionConfig(String forwarded) {
        ForwardedExtensionConfig forwardedConfig;
        if (HttpConstants.FORWARDED_ENABLE.equalsIgnoreCase(forwarded)) {
            forwardedConfig = ForwardedExtensionConfig.ENABLE;
        } else if (HttpConstants.FORWARDED_TRANSITION.equalsIgnoreCase(forwarded)) {
            forwardedConfig = ForwardedExtensionConfig.TRANSITION;
        } else if (HttpConstants.FORWARDED_DISABLE.equalsIgnoreCase(forwarded)) {
            forwardedConfig = ForwardedExtensionConfig.DISABLE;
        } else {
            throw new BallerinaConnectorException("Invalid configuration found for Forwarded : " + forwarded);
        }
        return forwardedConfig;
    }

    public static HttpCarbonMessage createHttpCarbonMessage(boolean isRequest) {
        HttpCarbonMessage httpCarbonMessage;
        if (isRequest) {
            httpCarbonMessage = new HttpCarbonMessage(
                    new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, ""));
        } else {
            httpCarbonMessage = new HttpCarbonMessage(
                    new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        }
        httpCarbonMessage.completeMessage();
        return httpCarbonMessage;
    }

    public static void checkFunctionValidity(ObjectValue connectionObj, HttpCarbonMessage reqMsg,
                                             HttpCarbonMessage outboundResponseMsg) {
        serverConnectionStructCheck(reqMsg);
        int statusCode = outboundResponseMsg.getHttpStatusCode();
        methodInvocationCheck(connectionObj, reqMsg, statusCode);
    }

    private static void methodInvocationCheck(ObjectValue connectionObj, HttpCarbonMessage reqMsg, int statusCode) {
        if (connectionObj.getNativeData(METHOD_ACCESSED) != null || reqMsg == null) {
            throw new IllegalStateException("illegal function invocation");
        }

        if (!is100ContinueRequest(reqMsg, statusCode)) {
            connectionObj.addNativeData(METHOD_ACCESSED, true);
        }
    }

    public static void serverConnectionStructCheck(HttpCarbonMessage reqMsg) {
        if (reqMsg == null) {
            throw new BallerinaException("operation not allowed:invalid Connection variable");
        }
    }

    private static boolean is100ContinueRequest(HttpCarbonMessage reqMsg, int statusCode) {
        return HttpConstants.HEADER_VAL_100_CONTINUE.equalsIgnoreCase(
                reqMsg.getHeader(HttpHeaderNames.EXPECT.toString())) || statusCode == 100;
    }

    public static Annotation getServiceConfigAnnotation(Service service, String pkgPath) {
        List<Annotation> annotationList = service
                .getAnnotationList(pkgPath, HttpConstants.ANN_NAME_HTTP_SERVICE_CONFIG);
        return (annotationList == null || annotationList.isEmpty()) ? null : annotationList.get(0);
    }

    public static Annotation getServiceConfigStruct(Service service, String pkgPath) {
        List<Annotation> annotationList = service.getAnnotationList(pkgPath, HttpConstants.ANN_NAME_CONFIG);
        return (annotationList == null || annotationList.isEmpty()) ? null : annotationList.get(0);
    }

//    protected static void populateKeepAliveAndCompressionStatus(HttpService service, Annotation annotation) {
//        if (annotation == null) {
//            return;
//        }
//        AnnAttrValue keepAliveAttrVal = annotation.getAnnAttrValue(HttpConstants.ANN_CONFIG_ATTR_KEEP_ALIVE);
//        if (keepAliveAttrVal != null) {
//            service.setKeepAlive(keepAliveAttrVal.getBooleanValue());
//        }
//
//        AnnAttrValue compressionEnabled = annotation.getAnnAttrValue(
//                HttpConstants.ANN_CONFIG_ATTR_COMPRESSION_ENABLED);
//        if (compressionEnabled != null) {
//            service.setCompressionEnabled(compressionEnabled.getBooleanValue());
//        }
//    }

    public static MapValue getResourceConfigAnnotation(AttachedFunction resource, String pkgPath) {
        return (MapValue) resource.getAnnotation(pkgPath, HttpConstants.ANN_NAME_RESOURCE_CONFIG);
    }

    public static MapValue getTransactionConfigAnnotation(AttachedFunction resource, String transactionPackagePath) {
        return (MapValue) resource.getAnnotation(transactionPackagePath,
                                                 TransactionConstants.ANN_NAME_TRX_PARTICIPANT_CONFIG);
    }

    private static int getIntValue(long val) {
        int intVal = (int) val;

        if (intVal != val) {
            throw new IllegalArgumentException("invalid argument: " + val);
        }

        return intVal;
    }

    public static String getContentTypeFromTransportMessage(HttpCarbonMessage transportMessage) {
        return transportMessage.getHeader(HttpHeaderNames.CONTENT_TYPE.toString());
    }

    /**
     * If the given Content-Type header value doesn't have a boundary parameter value, get a new boundary string and
     * append it to Content-Type and set it to transport message.
     *
     * @param transportMessage Represent transport message
     * @param contentType      Represent the Content-Type header value
     * @return The boundary string that was extracted from header or the newly generated one
     */
    public static String addBBoundaryIfNotExist(HttpCarbonMessage transportMessage, String contentType) {
        String boundaryString;
        BString boundaryValue = HeaderUtil.extractBoundaryBParameter(contentType);
        boundaryString = boundaryValue != null ? boundaryValue.toString() :
                HttpUtil.addBoundaryParameter(transportMessage, contentType);
        return boundaryString;
    }

    /**
     * If the given Content-Type header value doesn't have a boundary parameter value, get a new boundary string and
     * append it to Content-Type and set it to transport message.
     *
     * @param transportMessage Represent transport message
     * @param contentType      Represent the Content-Type header value
     * @return The boundary string that was extracted from header or the newly generated one
     */
    public static String addBoundaryIfNotExist(HttpCarbonMessage transportMessage, String contentType) {
        String boundaryString;
        String boundaryValue = HeaderUtil.extractBoundaryParameter(contentType);
        boundaryString = boundaryValue != null ? boundaryValue : HttpUtil.addBoundaryParameter(transportMessage,
                                                                                               contentType);
        return boundaryString;
    }

    /**
     * Generate a new boundary string and append it Content-Type and set that to transport message.
     *
     * @param transportMessage Represent transport message
     * @param contentType      Represent the Content-Type header value
     * @return The newly generated boundary string
     */
    private static String addBoundaryParameter(HttpCarbonMessage transportMessage, String contentType) {
        String boundaryString = null;
        if (contentType != null && contentType.startsWith(MULTIPART_AS_PRIMARY_TYPE)) {
            boundaryString = MimeUtil.getNewMultipartDelimiter();
            transportMessage.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), contentType + "; " + BOUNDARY + "=" +
                    boundaryString);
        }
        return boundaryString;
    }

    public static HttpWsConnectorFactory createHttpWsConnectionFactory() {
        return new DefaultHttpWsConnectorFactory();
    }

    public static void checkAndObserveHttpRequest(Strand strand, HttpCarbonMessage message) {
        // TODO fix observabiltiy utils
//        Optional<ObserverContext> observerContext = ObserveUtils.getObserverContextOfCurrentFrame(strand);
//        observerContext.ifPresent(ctx -> {
//            HttpUtil.injectHeaders(message, ObserveUtils.getContextProperties(ctx));
//            ctx.addTag(TAG_KEY_HTTP_METHOD, message.getHttpMethod());
//            ctx.addTag(TAG_KEY_HTTP_URL, String.valueOf(message.getProperty(HttpConstants.TO)));
//            ctx.addTag(TAG_KEY_PEER_ADDRESS,
//                       message.getProperty(PROPERTY_HTTP_HOST) + ":" + message.getProperty(PROPERTY_HTTP_PORT));
//            // Add HTTP Status Code tag. The HTTP status code will be set using the response message.
//            // Sometimes the HTTP status code will not be set due to errors etc. Therefore, it's very important to set
//            // some value to HTTP Status Code to make sure that tags will not change depending on various
//            // circumstances.
//            // HTTP Status code must be a number.
//            ctx.addTag(TAG_KEY_HTTP_STATUS_CODE, Integer.toString(0));
//        });
    }

    public static void injectHeaders(HttpCarbonMessage msg, Map<String, String> headers) {
        if (headers != null) {
            headers.forEach((key, value) -> msg.setHeader(key, String.valueOf(value)));
        }
    }

    private static void setChunkingHeader(String transferValue, HttpCarbonMessage outboundResponseMsg) {
        if (transferValue == null) { //TODO check this logic - chamil
            return;
        }
        outboundResponseMsg.setProperty(CHUNKING_CONFIG, getChunkConfig(transferValue));
    }

    /**
     * Creates InResponse using the native {@code HttpCarbonMessage}.
     *
     * @param httpCarbonMessage the HttpCarbonMessage
     * @return the Response struct
     */
    public static ObjectValue createResponseStruct(HttpCarbonMessage httpCarbonMessage) {
        ObjectValue responseObj = BallerinaValues.createObjectValue(HttpConstants.PROTOCOL_PACKAGE_HTTP,
                                                                    HttpConstants.RESPONSE);
        ObjectValue entity = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_MIME, HttpConstants.ENTITY);
        ObjectValue mediaType = BallerinaValues.createObjectValue(PROTOCOL_PACKAGE_MIME, MEDIA_TYPE);

        HttpUtil.populateInboundResponse(responseObj, entity, mediaType, httpCarbonMessage);
        return responseObj;
    }

    public static void populateSenderConfigurations(SenderConfiguration senderConfiguration, MapValue<String, Object>
            clientEndpointConfig) {
        ProxyServerConfiguration proxyServerConfiguration;
        MapValue secureSocket = clientEndpointConfig.getMapValue(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET);

        if (secureSocket != null) {
            HttpUtil.populateSSLConfiguration(senderConfiguration, secureSocket);
        } else {
            HttpUtil.setDefaultTrustStore(senderConfiguration);
        }
        MapValue proxy = clientEndpointConfig.getMapValue(HttpConstants.PROXY_STRUCT_REFERENCE);
        if (proxy != null) {
            String proxyHost = proxy.getStringValue(HttpConstants.PROXY_HOST);
            int proxyPort = proxy.getIntValue(HttpConstants.PROXY_PORT).intValue();
            String proxyUserName = proxy.getStringValue(HttpConstants.PROXY_USERNAME);
            String proxyPassword = proxy.getStringValue(HttpConstants.PROXY_PASSWORD);
            try {
                proxyServerConfiguration = new ProxyServerConfiguration(proxyHost, proxyPort);
            } catch (UnknownHostException e) {
                throw new BallerinaConnectorException("Failed to resolve host" + proxyHost, e);
            }
            if (!proxyUserName.isEmpty()) {
                proxyServerConfiguration.setProxyUsername(proxyUserName);
            }
            if (!proxyPassword.isEmpty()) {
                proxyServerConfiguration.setProxyPassword(proxyPassword);
            }
            senderConfiguration.setProxyServerConfiguration(proxyServerConfiguration);
        }

        long timeoutMillis = clientEndpointConfig.getIntValue(HttpConstants.CLIENT_EP_ENDPOINT_TIMEOUT);
        if (timeoutMillis < 0) {
            senderConfiguration.setSocketIdleTimeout(0);
        } else {
            senderConfiguration.setSocketIdleTimeout(
                    validateConfig(timeoutMillis, HttpConstants.CLIENT_EP_ENDPOINT_TIMEOUT));
        }

        String httpVersion = clientEndpointConfig.getStringValue(HttpConstants.CLIENT_EP_HTTP_VERSION);
        if (httpVersion != null) {
            senderConfiguration.setHttpVersion(httpVersion);
        }
        String forwardedExtension = clientEndpointConfig.getStringValue(HttpConstants.CLIENT_EP_FORWARDED);
        senderConfiguration.setForwardedExtensionConfig(HttpUtil.getForwardedExtensionConfig(forwardedExtension));
    }

    public static ConnectionManager getConnectionManager(MapValue<String, Long> poolStruct) {
        ConnectionManager poolManager = (ConnectionManager) poolStruct.getNativeData(CONNECTION_MANAGER);
        if (poolManager == null) {
            synchronized (poolStruct) {
                if (poolStruct.getNativeData(CONNECTION_MANAGER) == null) {
                    PoolConfiguration userDefinedPool = new PoolConfiguration();
                    populatePoolingConfig(poolStruct, userDefinedPool);
                    poolManager = new ConnectionManager(userDefinedPool);
                    poolStruct.addNativeData(CONNECTION_MANAGER, poolManager);
                }
            }
        }
        return poolManager;
    }

    public static void populatePoolingConfig(MapValue<String, Long> poolRecord, PoolConfiguration poolConfiguration) {
        long maxActiveConnections = poolRecord.get(HttpConstants.CONNECTION_POOLING_MAX_ACTIVE_CONNECTIONS);
        poolConfiguration.setMaxActivePerPool(
                validateConfig(maxActiveConnections, HttpConstants.CONNECTION_POOLING_MAX_ACTIVE_CONNECTIONS));

        long maxIdleConnections = poolRecord.get(HttpConstants.CONNECTION_POOLING_MAX_IDLE_CONNECTIONS);
        poolConfiguration.setMaxIdlePerPool(
                validateConfig(maxIdleConnections, HttpConstants.CONNECTION_POOLING_MAX_IDLE_CONNECTIONS));

        long waitTime = poolRecord.get(HttpConstants.CONNECTION_POOLING_WAIT_TIME);
        poolConfiguration.setMaxWaitTime(waitTime);

        long maxActiveStreamsPerConnection = poolRecord.get(CONNECTION_POOLING_MAX_ACTIVE_STREAMS_PER_CONNECTION);
        poolConfiguration.setHttp2MaxActiveStreamsPerConnection(
                maxActiveStreamsPerConnection == -1 ? Integer.MAX_VALUE : validateConfig(maxActiveStreamsPerConnection,
                                                                CONNECTION_POOLING_MAX_ACTIVE_STREAMS_PER_CONNECTION));
    }

    private static int validateConfig(long value, String configName) {
        try {
            return Math.toIntExact(value);
        } catch (ArithmeticException e) {
            log.warn("The value set for the configuration needs to be less than {}. The " + configName +
                             "value is set to {}", Integer.MAX_VALUE);
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Populates SSL configuration instance with secure socket configuration.
     *
     * @param sslConfiguration  ssl configuration instance.
     * @param secureSocket    secure socket configuration.
     */
    public static void populateSSLConfiguration(SslConfiguration sslConfiguration, MapValue secureSocket) {
        MapValue trustStore = secureSocket.getMapValue(ENDPOINT_CONFIG_TRUST_STORE);
        MapValue keyStore = secureSocket.getMapValue(ENDPOINT_CONFIG_KEY_STORE);
        MapValue protocols = secureSocket.getMapValue(ENDPOINT_CONFIG_PROTOCOLS);
        MapValue validateCert = secureSocket.getMapValue(ENDPOINT_CONFIG_VALIDATE_CERT);
        String keyFile = secureSocket.getStringValue(ENDPOINT_CONFIG_KEY);
        String certFile = secureSocket.getStringValue(ENDPOINT_CONFIG_CERTIFICATE);
        String trustCerts = secureSocket.getStringValue(ENDPOINT_CONFIG_TRUST_CERTIFICATES);
        String keyPassword = secureSocket.getStringValue(ENDPOINT_CONFIG_KEY_PASSWORD);
        List<Parameter> clientParams = new ArrayList<>();
        if (trustStore != null && StringUtils.isNotBlank(trustCerts)) {
            throw new BallerinaException("Cannot configure both trustStore and trustCerts at the same time.");
        }
        if (trustStore != null) {
            String trustStoreFile = trustStore.getStringValue(FILE_PATH);
            if (StringUtils.isNotBlank(trustStoreFile)) {
                sslConfiguration.setTrustStoreFile(trustStoreFile);
            }
            String trustStorePassword = trustStore.getStringValue(PASSWORD);
            if (StringUtils.isNotBlank(trustStorePassword)) {
                sslConfiguration.setTrustStorePass(trustStorePassword);
            }
        } else if (StringUtils.isNotBlank(trustCerts)) {
            sslConfiguration.setClientTrustCertificates(trustCerts);
        }
        if (keyStore != null && StringUtils.isNotBlank(keyFile)) {
            throw new BallerinaException("Cannot configure both keyStore and keyFile.");
        } else if (StringUtils.isNotBlank(keyFile) && StringUtils.isBlank(certFile)) {
            throw new BallerinaException("Need to configure certFile containing client ssl certificates.");
        }
        if (keyStore != null) {
            String keyStoreFile = keyStore.getStringValue(FILE_PATH);
            if (StringUtils.isNotBlank(keyStoreFile)) {
                sslConfiguration.setKeyStoreFile(keyStoreFile);
            }
            String keyStorePassword = keyStore.getStringValue(PASSWORD);
            if (StringUtils.isNotBlank(keyStorePassword)) {
                sslConfiguration.setKeyStorePass(keyStorePassword);
            }
        } else if (StringUtils.isNotBlank(keyFile)) {
            sslConfiguration.setClientKeyFile(keyFile);
            sslConfiguration.setClientCertificates(certFile);
            if (StringUtils.isNotBlank(keyPassword)) {
                sslConfiguration.setClientKeyPassword(keyPassword);
            }
        }
        if (protocols != null) {
            List<Object> sslEnabledProtocolsValueList = Arrays
                    .asList(protocols.getArrayValue(ENABLED_PROTOCOLS).getValues());
            if (sslEnabledProtocolsValueList.size() > 0) {
                String sslEnabledProtocols = sslEnabledProtocolsValueList.stream().map(Object::toString)
                        .collect(Collectors.joining(",", "", ""));
                Parameter clientProtocols = new Parameter(SSL_ENABLED_PROTOCOLS, sslEnabledProtocols);
                clientParams.add(clientProtocols);
            }
            String sslProtocol = protocols.getStringValue(SSL_PROTOCOL_VERSION);
            if (StringUtils.isNotBlank(sslProtocol)) {
                sslConfiguration.setSSLProtocol(sslProtocol);
            }
        }

        if (validateCert != null) {
            boolean validateCertEnabled = validateCert.getBooleanValue(HttpConstants.ENABLE);
            int cacheSize = validateCert.getIntValue(HttpConstants.SSL_CONFIG_CACHE_SIZE).intValue();
            int cacheValidityPeriod = validateCert.getIntValue(HttpConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD)
                    .intValue();
            sslConfiguration.setValidateCertEnabled(validateCertEnabled);
            if (cacheValidityPeriod != 0) {
                sslConfiguration.setCacheValidityPeriod(cacheValidityPeriod);
            }
            if (cacheSize != 0) {
                sslConfiguration.setCacheSize(cacheSize);
            }
        }
        boolean hostNameVerificationEnabled = secureSocket
                .getBooleanValue(HttpConstants.SSL_CONFIG_HOST_NAME_VERIFICATION_ENABLED);
        boolean ocspStaplingEnabled = secureSocket.getBooleanValue(HttpConstants.ENDPOINT_CONFIG_OCSP_STAPLING);
        sslConfiguration.setOcspStaplingEnabled(ocspStaplingEnabled);
        sslConfiguration.setHostNameVerificationEnabled(hostNameVerificationEnabled);

        sslConfiguration
                .setSslSessionTimeOut((int) secureSocket.getDefaultableIntValue(ENDPOINT_CONFIG_SESSION_TIMEOUT));

        sslConfiguration.setSslHandshakeTimeOut(secureSocket.getDefaultableIntValue(ENDPOINT_CONFIG_HANDSHAKE_TIMEOUT));

        List<Object> ciphersValueList = Arrays.asList(
                secureSocket.getArrayValue(HttpConstants.SSL_CONFIG_CIPHERS).getValues());
        if (ciphersValueList.size() > 0) {
            String ciphers = ciphersValueList.stream().map(Object::toString)
                    .collect(Collectors.joining(",", "", ""));
            Parameter clientCiphers = new Parameter(HttpConstants.CIPHERS, ciphers);
            clientParams.add(clientCiphers);
        }
        String enableSessionCreation = String.valueOf(secureSocket
                .getBooleanValue(HttpConstants.SSL_CONFIG_ENABLE_SESSION_CREATION));
        Parameter clientEnableSessionCreation = new Parameter(HttpConstants.SSL_CONFIG_ENABLE_SESSION_CREATION,
                enableSessionCreation);
        clientParams.add(clientEnableSessionCreation);
        if (!clientParams.isEmpty()) {
            sslConfiguration.setParameters(clientParams);
        }
    }

    public static void setDefaultTrustStore(SslConfiguration sslConfiguration) {
        sslConfiguration.setTrustStoreFile(String.valueOf(
                Paths.get(System.getProperty("ballerina.home"), "bre", "security", "ballerinaTruststore.p12")));
        sslConfiguration.setTrustStorePass("ballerina");
    }

    public static String sanitizeBasePath(String basePath) {
        basePath = basePath.trim();

        if (!basePath.startsWith(HttpConstants.DEFAULT_BASE_PATH)) {
            basePath = HttpConstants.DEFAULT_BASE_PATH.concat(basePath);
        }

        if ((basePath.endsWith(HttpConstants.DEFAULT_BASE_PATH) && basePath.length() != 1)) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        if (basePath.endsWith("*")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        return basePath;
    }

    /**
     * Serialize outbound message.
     *
     * @param outboundMessageSource Represent the outbound message datasource
     * @param entity                Represent the entity of the outbound message
     * @param messageOutputStream   Represent the output stream
     * @throws IOException In case an error occurs while writing to output stream
     */
    public static void serializeDataSource(Object outboundMessageSource, ObjectValue entity,
                                           OutputStream messageOutputStream) throws IOException {
        if (MimeUtil.generateAsJSON(outboundMessageSource, entity)) {
            JSONGenerator gen = new JSONGenerator(messageOutputStream);
            gen.serialize(outboundMessageSource);
            gen.flush();
        } else {
            serialize(outboundMessageSource, messageOutputStream);
        }
    }

    public static void serialize(Object value, OutputStream outputStream) throws IOException {
        //TODO check the possibility of value being null
        if (value == null) {
            throw new BallerinaException("error occurred while serializing null data");
        } else if (value instanceof ArrayValue) {
            if (value instanceof StreamingJsonValue) {
                ((StreamingJsonValue) value).serialize(outputStream);
            } else {
                ((ArrayValue) value).serialize(outputStream);
            }
        } else if (value instanceof MultipartDataSource) {
            ((MultipartDataSource) value).serialize(outputStream);
        } else if (value instanceof XMLItem) {
            ((XMLItem) value).serialize(outputStream);
        } else if (value instanceof XMLSequence) {
            ((XMLSequence) value).serialize(outputStream);
        } else if (value instanceof Long || value instanceof String ||
                value instanceof Double || value instanceof Integer || value instanceof Boolean) {
            outputStream.write(value.toString().getBytes(Charset.defaultCharset()));
        } else {
            ((RefValue) value).serialize(outputStream);
        }
    }

    /**
     * Check the availability of an annotation.
     *
     * @param configAnnotation      Represent the annotation
     * @return True if the annotation and the annotation value are available
     */
    public static boolean checkConfigAnnotationAvailability(MapValue configAnnotation) {
        return configAnnotation != null;
    }

    /**
     * Returns Listener configuration instance populated with endpoint config.
     *
     * @param port              listener port.
     * @param endpointConfig    listener endpoint configuration.
     * @return                  transport listener configuration instance.
     */
    public static ListenerConfiguration getListenerConfig(long port, MapValue endpointConfig) {
        String host = endpointConfig.getStringValue(HttpConstants.ENDPOINT_CONFIG_HOST);
        String keepAlive = endpointConfig.get(HttpConstants.ENDPOINT_CONFIG_KEEP_ALIVE).toString();
        MapValue sslConfig = endpointConfig.getMapValue(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET);
        String httpVersion = endpointConfig.getStringValue(HttpConstants.ENDPOINT_CONFIG_VERSION);
        MapValue requestLimits = endpointConfig.getMapValue(HttpConstants.ENDPOINT_REQUEST_LIMITS);
        long idleTimeout = endpointConfig.getIntValue(HttpConstants.ENDPOINT_CONFIG_TIMEOUT);

        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();

        if (host == null || host.trim().isEmpty()) {
            listenerConfiguration.setHost(ConfigRegistry.getInstance().getConfigOrDefault("b7a.http.host",
                    HttpConstants.HTTP_DEFAULT_HOST));
        } else {
            listenerConfiguration.setHost(host);
        }

        if (port == 0) {
            throw new BallerinaConnectorException("Listener port is not defined!");
        }
        listenerConfiguration.setPort(Math.toIntExact(port));

        listenerConfiguration.setKeepAliveConfig(HttpUtil.getKeepAliveConfig(keepAlive));

        // Set Request validation limits.
        if (requestLimits != null) {
            setRequestSizeValidationConfig(requestLimits, listenerConfiguration);
        }

        if (idleTimeout < 0) {
            throw new BallerinaConnectorException("Idle timeout cannot be negative. If you want to disable the " +
                    "timeout please use value 0");
        }
        listenerConfiguration.setSocketIdleTimeout(Math.toIntExact(idleTimeout));

        // Set HTTP version
        if (httpVersion != null) {
            listenerConfiguration.setVersion(httpVersion);
        }

        listenerConfiguration.setServerHeader(getServerName());

        if (sslConfig != null) {
            return setSslConfig(sslConfig, listenerConfiguration);
        }

        listenerConfiguration.setPipeliningEnabled(true); //Pipelining is enabled all the time
        listenerConfiguration.setPipeliningLimit(endpointConfig.getIntValue(
                HttpConstants.PIPELINING_REQUEST_LIMIT));

        return listenerConfiguration;
    }

    private static void setRequestSizeValidationConfig(MapValue requestLimits,
                                                     ListenerConfiguration listenerConfiguration) {
        long maxUriLength = requestLimits.getIntValue(HttpConstants.REQUEST_LIMITS_MAXIMUM_URL_LENGTH);
        long maxHeaderSize = requestLimits.getIntValue(HttpConstants.REQUEST_LIMITS_MAXIMUM_HEADER_SIZE);
        long maxEntityBodySize = requestLimits.getIntValue(HttpConstants.REQUEST_LIMITS_MAXIMUM_ENTITY_BODY_SIZE);
        RequestSizeValidationConfig requestSizeValidationConfig = listenerConfiguration
                .getRequestSizeValidationConfig();

        if (maxUriLength != -1) {
            if (maxUriLength >= 0) {
                requestSizeValidationConfig.setMaxUriLength(Math.toIntExact(maxUriLength));
            } else {
                throw new BallerinaConnectorException("Invalid configuration found for maxUriLength : " + maxUriLength);
            }
        }

        if (maxHeaderSize != -1) {
            if (maxHeaderSize >= 0) {
                requestSizeValidationConfig.setMaxHeaderSize(Math.toIntExact(maxHeaderSize));
            } else {
                throw new BallerinaConnectorException(
                        "Invalid configuration found for maxHeaderSize : " + maxHeaderSize);
            }
        }

        if (maxEntityBodySize != -1) {
            if (maxEntityBodySize >= 0) {
                requestSizeValidationConfig.setMaxEntityBodySize(maxEntityBodySize);
            } else {
                throw new BallerinaConnectorException(
                        "Invalid configuration found for maxEntityBodySize : " + maxEntityBodySize);
            }
        }
    }

    private static String getServerName() {
        String userAgent;
        String version = System.getProperty(BALLERINA_VERSION);
        if (version != null) {
            userAgent = "ballerina/" + version;
        } else {
            userAgent = "ballerina";
        }
        return userAgent;
    }

    private static ListenerConfiguration setSslConfig(MapValue sslConfig, ListenerConfiguration listenerConfiguration) {
        listenerConfiguration.setScheme(PROTOCOL_HTTPS);
        MapValue trustStore = sslConfig.getMapValue(ENDPOINT_CONFIG_TRUST_STORE);
        MapValue keyStore = sslConfig.getMapValue(ENDPOINT_CONFIG_KEY_STORE);
        MapValue protocols = sslConfig.getMapValue(ENDPOINT_CONFIG_PROTOCOLS);
        MapValue validateCert = sslConfig.getMapValue(ENDPOINT_CONFIG_VALIDATE_CERT);
        MapValue ocspStapling = sslConfig.getMapValue(ENDPOINT_CONFIG_OCSP_STAPLING);
        String keyFile = sslConfig.getStringValue(ENDPOINT_CONFIG_KEY);
        String certFile = sslConfig.getStringValue(ENDPOINT_CONFIG_CERTIFICATE);
        String trustCerts = sslConfig.getStringValue(ENDPOINT_CONFIG_TRUST_CERTIFICATES);
        String keyPassword = sslConfig.getStringValue(ENDPOINT_CONFIG_KEY_PASSWORD);

        if (keyStore != null && StringUtils.isNotBlank(keyFile)) {
            throw new BallerinaException("Cannot configure both keyStore and keyFile at the same time.");
        } else if (keyStore == null && (StringUtils.isBlank(keyFile) || StringUtils.isBlank(certFile))) {
            throw new BallerinaException("Either keystore or certificateKey and server certificates must be provided "
                    + "for secure connection");
        }
        if (keyStore != null) {
            String keyStoreFile = keyStore.getStringValue(FILE_PATH);
            if (StringUtils.isBlank(keyStoreFile)) {
                throw new BallerinaException("Keystore file location must be provided for secure connection.");
            }
            String keyStorePassword = keyStore.getStringValue(PASSWORD);
            if (StringUtils.isBlank(keyStorePassword)) {
                throw new BallerinaException("Keystore password must be provided for secure connection");
            }
            listenerConfiguration.setKeyStoreFile(keyStoreFile);
            listenerConfiguration.setKeyStorePass(keyStorePassword);
        } else {
            listenerConfiguration.setServerKeyFile(keyFile);
            listenerConfiguration.setServerCertificates(certFile);
            if (StringUtils.isNotBlank(keyPassword)) {
                listenerConfiguration.setServerKeyPassword(keyPassword);
            }
        }
        String sslVerifyClient = sslConfig.getStringValue(SSL_CONFIG_SSL_VERIFY_CLIENT);
        listenerConfiguration.setVerifyClient(sslVerifyClient);
        listenerConfiguration
                .setSslSessionTimeOut((int) sslConfig.getDefaultableIntValue(ENDPOINT_CONFIG_SESSION_TIMEOUT));
        listenerConfiguration
                .setSslHandshakeTimeOut(sslConfig.getDefaultableIntValue(ENDPOINT_CONFIG_HANDSHAKE_TIMEOUT));
        if (trustStore == null && StringUtils.isNotBlank(sslVerifyClient) && StringUtils.isBlank(trustCerts)) {
            throw new BallerinaException(
                    "Truststore location or trustCertificates must be provided to enable Mutual SSL");
        }
        if (trustStore != null) {
            String trustStoreFile = trustStore.getStringValue(FILE_PATH);
            String trustStorePassword = trustStore.getStringValue(PASSWORD);
            if (StringUtils.isBlank(trustStoreFile) && StringUtils.isNotBlank(sslVerifyClient)) {
                throw new BallerinaException("Truststore location must be provided to enable Mutual SSL");
            }
            if (StringUtils.isBlank(trustStorePassword) && StringUtils.isNotBlank(sslVerifyClient)) {
                throw new BallerinaException("Truststore password value must be provided to enable Mutual SSL");
            }
            listenerConfiguration.setTrustStoreFile(trustStoreFile);
            listenerConfiguration.setTrustStorePass(trustStorePassword);
        } else if (StringUtils.isNotBlank(trustCerts)) {
            listenerConfiguration.setServerTrustCertificates(trustCerts);
        }
        List<Parameter> serverParamList = new ArrayList<>();
        Parameter serverParameters;
        if (protocols != null) {
            List<String> sslEnabledProtocolsValueList = Arrays.asList(
                    protocols.getArrayValue(ENABLED_PROTOCOLS).getStringArray());
            if (!sslEnabledProtocolsValueList.isEmpty()) {
                String sslEnabledProtocols = sslEnabledProtocolsValueList.stream()
                        .collect(Collectors.joining(",", "", ""));
                serverParameters = new Parameter(ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS, sslEnabledProtocols);
                serverParamList.add(serverParameters);
            }

            String sslProtocol = protocols.getStringValue(SSL_PROTOCOL_VERSION);
            if (StringUtils.isNotBlank(sslProtocol)) {
                listenerConfiguration.setSSLProtocol(sslProtocol);
            }
        }

        List<String> ciphersValueList = Arrays.asList(
                sslConfig.getArrayValue(HttpConstants.SSL_CONFIG_CIPHERS).getStringArray());
        if (!ciphersValueList.isEmpty()) {
            String ciphers = ciphersValueList.stream().collect(Collectors.joining(",", "", ""));
            serverParameters = new Parameter(HttpConstants.CIPHERS, ciphers);
            serverParamList.add(serverParameters);
        }
        if (validateCert != null) {
            boolean validateCertificateEnabled = validateCert.getBooleanValue(HttpConstants.ENABLE);
            long cacheSize = validateCert.getIntValue(HttpConstants.SSL_CONFIG_CACHE_SIZE);
            long cacheValidationPeriod = validateCert.getIntValue(HttpConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD);
            listenerConfiguration.setValidateCertEnabled(validateCertificateEnabled);
            if (validateCertificateEnabled) {
                if (cacheSize != 0) {
                    listenerConfiguration.setCacheSize(Math.toIntExact(cacheSize));
                }
                if (cacheValidationPeriod != 0) {
                    listenerConfiguration.setCacheValidityPeriod(Math.toIntExact(cacheValidationPeriod));
                }
            }
        }
        if (ocspStapling != null) {
            boolean ocspStaplingEnabled = ocspStapling.getBooleanValue(HttpConstants.ENABLE);
            listenerConfiguration.setOcspStaplingEnabled(ocspStaplingEnabled);
            long cacheSize = ocspStapling.getIntValue(HttpConstants.SSL_CONFIG_CACHE_SIZE);
            long cacheValidationPeriod = ocspStapling.getIntValue(HttpConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD);
            listenerConfiguration.setValidateCertEnabled(ocspStaplingEnabled);
            if (ocspStaplingEnabled) {
                if (cacheSize != 0) {
                    listenerConfiguration.setCacheSize(Math.toIntExact(cacheSize));
                }
                if (cacheValidationPeriod != 0) {
                    listenerConfiguration.setCacheValidityPeriod(Math.toIntExact(cacheValidationPeriod));
                }
            }
        }
        listenerConfiguration.setTLSStoreType(PKCS_STORE_TYPE);
        String serverEnableSessionCreation = String
                .valueOf(sslConfig.getBooleanValue(SSL_CONFIG_ENABLE_SESSION_CREATION));
        Parameter enableSessionCreationParam = new Parameter(SSL_CONFIG_ENABLE_SESSION_CREATION,
                serverEnableSessionCreation);
        serverParamList.add(enableSessionCreationParam);
        if (!serverParamList.isEmpty()) {
            listenerConfiguration.setParameters(serverParamList);
        }

        listenerConfiguration
                .setId(HttpUtil.getListenerInterface(listenerConfiguration.getHost(), listenerConfiguration.getPort()));

        return listenerConfiguration;
    }

    private HttpUtil() {
    }
}
