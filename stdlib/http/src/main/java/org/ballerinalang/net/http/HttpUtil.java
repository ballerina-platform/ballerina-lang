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
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityWrapper;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDecoder;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.util.JsonGenerator;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.caching.RequestCacheControlStruct;
import org.ballerinalang.net.http.caching.ResponseCacheControlStruct;
import org.ballerinalang.net.http.session.Session;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.observability.ObserveUtils;
import org.ballerinalang.util.observability.ObserverContext;
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
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.PoolConfiguration;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpHeaderNames.CACHE_CONTROL;
import static org.ballerinalang.mime.util.EntityBodyHandler.checkEntityBodyAvailability;
import static org.ballerinalang.mime.util.MimeConstants.BOUNDARY;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.MimeConstants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MIME_ERROR_CODE;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.NO_CONTENT_LENGTH_FOUND;
import static org.ballerinalang.mime.util.MimeConstants.OCTET_STREAM;
import static org.ballerinalang.mime.util.MimeConstants.ONE_BYTE;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.MimeConstants.REQUEST_ENTITY_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.RESPONSE_ENTITY_FIELD;
import static org.ballerinalang.net.http.HttpConstants.ALWAYS;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_CHUNKING;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_COMPRESSION;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_COMPRESSION_CONTENT_TYPES;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_COMPRESSION_ENABLE;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.AUTO;
import static org.ballerinalang.net.http.HttpConstants.COLON;
import static org.ballerinalang.net.http.HttpConstants.CONNECTION_POOLING_MAX_ACTIVE_STREAMS_PER_CONNECTION;
import static org.ballerinalang.net.http.HttpConstants.ENABLED_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_CERTIFICATE;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_KEY;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_KEY_PASSWORD;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_KEY_STORE;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_OCSP_STAPLING;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_TRUST_CERTIFICATES;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_TRUST_STORE;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_VALIDATE_CERT;
import static org.ballerinalang.net.http.HttpConstants.ENTITY_INDEX;
import static org.ballerinalang.net.http.HttpConstants.FILE_PATH;
import static org.ballerinalang.net.http.HttpConstants.HTTP_ERROR_CODE;
import static org.ballerinalang.net.http.HttpConstants.HTTP_ERROR_MESSAGE;
import static org.ballerinalang.net.http.HttpConstants.HTTP_ERROR_RECORD;
import static org.ballerinalang.net.http.HttpConstants.HTTP_MESSAGE_INDEX;
import static org.ballerinalang.net.http.HttpConstants.HTTP_STATUS_CODE;
import static org.ballerinalang.net.http.HttpConstants.MUTUAL_SSL_HANDSHAKE_RECORD;
import static org.ballerinalang.net.http.HttpConstants.NEVER;
import static org.ballerinalang.net.http.HttpConstants.PASSWORD;
import static org.ballerinalang.net.http.HttpConstants.PKCS_STORE_TYPE;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTPS;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_VERSION;
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
import static org.ballerinalang.net.http.HttpConstants.TRANSPORT_MESSAGE;
import static org.ballerinalang.net.http.nativeimpl.pipelining.PipeliningHandler.sendPipelinedResponse;
import static org.ballerinalang.runtime.Constants.BALLERINA_VERSION;
import static org.ballerinalang.util.observability.ObservabilityConstants.PROPERTY_HTTP_HOST;
import static org.ballerinalang.util.observability.ObservabilityConstants.PROPERTY_HTTP_PORT;
import static org.ballerinalang.util.observability.ObservabilityConstants.TAG_KEY_HTTP_METHOD;
import static org.ballerinalang.util.observability.ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE;
import static org.ballerinalang.util.observability.ObservabilityConstants.TAG_KEY_HTTP_URL;
import static org.ballerinalang.util.observability.ObservabilityConstants.TAG_KEY_PEER_ADDRESS;
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

    public static BValue[] getProperty(Context context, boolean isRequest) {
        BMap<String, BValue> httpMessageStruct = (BMap<String, BValue>) context.getRefArgument(0);
        HttpCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        String propertyName = context.getStringArgument(0);

        Object propertyValue = httpCarbonMessage.getProperty(propertyName);

        if (propertyValue == null) {
            return new BValue[0];
        }

        if (propertyValue instanceof String) {
            return new BValue[] { new BString((String) propertyValue) };
        } else {
            throw new BallerinaException("Property value is of unknown type : " + propertyValue.getClass().getName());
        }
    }

    public static void setProperty(Context context, boolean isRequest) {
        BMap<String, BValue> httpMessageStruct = (BMap<String, BValue>) context.getRefArgument(0);
        String propertyName = context.getStringArgument(0);
        String propertyValue = context.getStringArgument(1);

        if (propertyName != null && propertyValue != null) {
            HttpCarbonMessage httpCarbonMessage = HttpUtil
                    .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
            httpCarbonMessage.setProperty(propertyName, propertyValue);
        }
    }

    /**
     * Set new entity to in/out request/response struct.
     *
     * @param context           ballerina context.
     * @param httpMessageStruct request/response struct.
     * @return created entity.
     */
    public static BMap<String, BValue> createNewEntity(Context context, BMap<String, BValue> httpMessageStruct) {
        BMap<String, BValue> entity = ConnectorUtils.createAndGetStruct(context, PROTOCOL_PACKAGE_MIME, ENTITY);
        HttpCarbonMessage httpCarbonMessage = HttpUtil.getCarbonMsg(httpMessageStruct,
                HttpUtil.createHttpCarbonMessage(isRequestStruct(httpMessageStruct)));
        entity.addNativeData(ENTITY_HEADERS, httpCarbonMessage.getHeaders());
        entity.addNativeData(ENTITY_BYTE_CHANNEL, null);
        httpMessageStruct.put(isRequestStruct(httpMessageStruct) ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD
                , entity);
        httpMessageStruct.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, false);
        return entity;
    }

    /**
     * Set the given entity to request or response message.
     *
     * @param context   Ballerina context
     * @param isRequest boolean representing whether the message is a request or a response
     */
    public static void setEntity(Context context, boolean isRequest) {
        BMap<String, BValue> httpMessageStruct = (BMap<String, BValue>) context.getRefArgument(HTTP_MESSAGE_INDEX);

        HttpCarbonMessage httpCarbonMessage = HttpUtil.getCarbonMsg(httpMessageStruct,
                HttpUtil.createHttpCarbonMessage(isRequest));
        BMap<String, BValue> entity = (BMap<String, BValue>) context.getRefArgument(ENTITY_INDEX);
        String contentType = MimeUtil.getContentTypeWithParameters(entity);
        if (checkEntityBodyAvailability(entity)) {
            httpCarbonMessage.waitAndReleaseAllEntities();
            if (contentType == null) {
                contentType = OCTET_STREAM;
            }
            HeaderUtil.setHeaderToEntity(entity, HttpHeaderNames.CONTENT_TYPE.toString(), contentType);
        }
        httpMessageStruct.put(isRequest ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD, entity);
        httpMessageStruct.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, checkEntityBodyAvailability(entity));
    }

    /**
     * Get the entity from request or response.
     *
     * @param context            Ballerina context
     * @param isRequest          boolean representing whether the message is a request or a response
     * @param entityBodyRequired boolean representing whether the entity body is required
     * @return Entity of the request or response
     */
    public static BValue[] getEntity(Context context, boolean isRequest, boolean entityBodyRequired) {
        try {
            BMap<String, BValue> httpMessageStruct = (BMap<String, BValue>) context.getRefArgument(HTTP_MESSAGE_INDEX);
            BMap<String, BValue> entity = (BMap<String, BValue>) httpMessageStruct
                    .get(isRequest ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD);
            boolean byteChannelAlreadySet = false;

            if (httpMessageStruct.getNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET) != null) {
                byteChannelAlreadySet = (Boolean) httpMessageStruct.getNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET);
            }
            if (entityBodyRequired && !byteChannelAlreadySet) {
                populateEntityBody(context, httpMessageStruct, entity, isRequest);
            }
            return new BValue[]{entity};
        } catch (Throwable throwable) {
            return new BValue[]{MimeUtil.createError(context, MIME_ERROR_CODE,
                    "Error occurred during entity construction: " + throwable.getMessage())};
        }
    }

    /**
     * Populate entity with the relevant body content.
     *
     * @param context           Represent ballerina context
     * @param httpMessageStruct Represent ballerina request/response
     * @param entity            Represent an entity
     * @param isRequest         boolean representing whether the message is a request or a response
     */
    public static void populateEntityBody(Context context, BMap<String, BValue> httpMessageStruct,
                                          BMap<String, BValue> entity, boolean isRequest) {
        HttpCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        HttpMessageDataStreamer httpMessageDataStreamer = new HttpMessageDataStreamer(httpCarbonMessage);
        String contentType = httpCarbonMessage.getHeader(HttpHeaderNames.CONTENT_TYPE.toString());
        if (MimeUtil.isNotNullAndEmpty(contentType) && contentType.startsWith(MULTIPART_AS_PRIMARY_TYPE)
                && context != null) {
            MultipartDecoder.parseBody(context, entity, contentType, httpMessageDataStreamer.getInputStream());
        } else {
            long contentLength = NO_CONTENT_LENGTH_FOUND;
            String lengthStr = httpCarbonMessage.getHeader(HttpHeaderNames.CONTENT_LENGTH.toString());
            try {
                contentLength = lengthStr != null ? Long.parseLong(lengthStr) : contentLength;
                if (contentLength == NO_CONTENT_LENGTH_FOUND) {
                    //Read one byte to make sure the incoming stream has data
                    contentLength = httpCarbonMessage.countMessageLengthTill(ONE_BYTE);
                }
            } catch (NumberFormatException e) {
                throw new BallerinaException("Invalid content length");
            }
            if (contentLength > 0) {
                entity.addNativeData(ENTITY_BYTE_CHANNEL, new EntityWrapper(
                        new EntityBodyChannel(httpMessageDataStreamer.getInputStream())));
            }
        }
        httpMessageStruct.put(isRequest ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD, entity);
        httpMessageStruct.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, true);
    }

    public static BMap<String, BValue> extractEntity(BMap<String, BValue> httpMessageStruct) {
        Object isEntityBodyAvailable = httpMessageStruct.getNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET);
        if (isEntityBodyAvailable == null || !((Boolean) isEntityBodyAvailable)) {
            return null;
        }
        return (BMap<String, BValue>) httpMessageStruct
                .get(isRequestStruct(httpMessageStruct) ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD);
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

    public static void prepareOutboundResponse(Context context, HttpCarbonMessage inboundRequestMsg,
                                               HttpCarbonMessage outboundResponseMsg,
                                               BMap<String, BValue> outboundResponseStruct) {

        HttpUtil.checkEntityAvailability(context, outboundResponseStruct);

        HttpUtil.addHTTPSessionAndCorsHeaders(context, inboundRequestMsg, outboundResponseMsg);
        HttpUtil.enrichOutboundMessage(outboundResponseMsg, outboundResponseStruct);
        HttpUtil.setCompressionHeaders(context, inboundRequestMsg, outboundResponseMsg);
        HttpUtil.setChunkingHeader(context, outboundResponseMsg);
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

    public static void addHTTPSessionAndCorsHeaders(Context context, HttpCarbonMessage requestMsg,
                                                    HttpCarbonMessage responseMsg) {
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

    static void handleFailure(HttpCarbonMessage requestMessage, BError error) {
        String errorMsg = getErrorMessage(error);
        int statusCode = getStatusCode(requestMessage, errorMsg);
        ErrorHandlerUtils.printError("error: " + BLangVMErrors.getPrintableStackTrace(error));
        sendPipelinedResponse(requestMessage, createErrorMessage(errorMsg, statusCode));
    }

    private static String getErrorMessage(BError error) {
        String errorMsg = error.reason;
        BMap<String, BValue> errorDetails = (BMap<String, BValue>) error.getDetails();
        if (!errorDetails.isEmpty()) {
            errorMsg = errorMsg.concat(COLON + errorDetails.get(HTTP_ERROR_MESSAGE));
        }
        return errorMsg;
    }

    private static int getStatusCode(HttpCarbonMessage requestMessage, String errorMsg) {
        Object carbonStatusCode = requestMessage.getProperty(HttpConstants.HTTP_STATUS_CODE);
        if (carbonStatusCode == null) {
            //log only the internal server errors
            log.error(errorMsg);
            return HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
        }
        return Integer.parseInt(carbonStatusCode.toString());
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

        response.setProperty(org.wso2.transport.http.netty.contract.Constants.HTTP_STATUS_CODE, statusCode);
    }

    /**
     * Get error struct.
     *
     * @param context Represent ballerina context
     * @param errMsg  Error message
     * @return Error struct
     */
    public static BError getError(Context context, String errMsg) {
        BMap<String, BValue> httpErrorRecord = createHTTPErrorRecord(context);
        httpErrorRecord.put(HTTP_ERROR_MESSAGE, new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, HTTP_ERROR_CODE, httpErrorRecord);
    }

    private static BMap<String, BValue> createHTTPErrorRecord(Context context) {
        return BLangConnectorSPIUtil.createBStruct(context, PROTOCOL_PACKAGE_HTTP, HTTP_ERROR_RECORD);
    }

    /**
     * Get error struct from throwable.
     *
     * @param context   Represent ballerina context
     * @param throwable Throwable representing the error.
     * @return Error struct
     */
    public static BError getError(Context context, Throwable throwable) {
        if (throwable.getMessage() == null) {
            return getError(context, IO_EXCEPTION_OCCURED);
        } else {
            return getError(context, throwable.getMessage());
        }
    }

    public static HttpCarbonMessage getCarbonMsg(BMap<String, BValue> struct, HttpCarbonMessage defaultMsg) {
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
    public static Http2PushPromise getPushPromise(BMap<String, BValue> pushPromiseStruct,
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
     *
     * @param pushPromiseStruct the push promise struct
     * @param pushPromise the native Http2PushPromise
     */
    public static void populatePushPromiseStruct(BMap<String, BValue> pushPromiseStruct,
                                                 Http2PushPromise pushPromise) {
        pushPromiseStruct.addNativeData(HttpConstants.TRANSPORT_PUSH_PROMISE, pushPromise);
        pushPromiseStruct.put(HttpConstants.PUSH_PROMISE_PATH_FIELD, new BString(pushPromise.getPath()));
        pushPromiseStruct.put(HttpConstants.PUSH_PROMISE_METHOD_FIELD, new BString(pushPromise.getMethod()));
    }

    /**
     * Creates native {@code Http2PushPromise} from PushPromise struct.
     *
     * @param struct the PushPromise struct
     * @return the populated the native {@code Http2PushPromise}
     */
    public static Http2PushPromise createHttpPushPromise(BMap<String, BValue> struct) {
        String method = struct.get(HttpConstants.PUSH_PROMISE_METHOD_FIELD).stringValue();
        if (method == null || method.isEmpty()) {
            method = HttpConstants.HTTP_METHOD_GET;
        }

        String path = struct.get(HttpConstants.PUSH_PROMISE_PATH_FIELD).stringValue();
        if (path == null || path.isEmpty()) {
            path = HttpConstants.DEFAULT_BASE_PATH;
        }
        return new Http2PushPromise(method, path);
    }

    public static void addCarbonMsg(BMap<String, BValue> struct, HttpCarbonMessage httpCarbonMessage) {
        struct.addNativeData(TRANSPORT_MESSAGE, httpCarbonMessage);
    }

    public static void populateInboundRequest(BMap<String, BValue> inboundRequestStruct, BMap<String, BValue> entity,
                                              BMap<String, BValue> mediaType, HttpCarbonMessage inboundRequestMsg,
                                              ProgramFile programFile) {
        inboundRequestStruct.addNativeData(TRANSPORT_MESSAGE, inboundRequestMsg);
        inboundRequestStruct.addNativeData(REQUEST, true);

        if (inboundRequestMsg.getProperty(HttpConstants.MUTUAL_SSL_RESULT) != null) {
            BMap<String, BValue> mutualSslRecord = BLangConnectorSPIUtil
                    .createBStruct(programFile, PROTOCOL_PACKAGE_HTTP, MUTUAL_SSL_HANDSHAKE_RECORD);
            mutualSslRecord.put(REQUEST_MUTUAL_SSL_HANDSHAKE_STATUS,
                    new BString((String) inboundRequestMsg.getProperty(HttpConstants.MUTUAL_SSL_RESULT)));
            inboundRequestStruct.put(REQUEST_MUTUAL_SSL_HANDSHAKE_FIELD, mutualSslRecord);
        }

        enrichWithInboundRequestInfo(inboundRequestStruct, inboundRequestMsg);
        enrichWithInboundRequestHeaders(inboundRequestStruct, inboundRequestMsg);

        populateEntity(entity, mediaType, inboundRequestMsg);
        inboundRequestStruct.put(REQUEST_ENTITY_FIELD, entity);
        inboundRequestStruct.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, false);

        String cacheControlHeader = inboundRequestMsg.getHeader(CACHE_CONTROL.toString());
        if (cacheControlHeader != null) {
            BMap<String, BValue> cacheControlStruct =
                    BLangConnectorSPIUtil.createBStruct(programFile, PROTOCOL_PACKAGE_HTTP, REQUEST_CACHE_CONTROL);
            RequestCacheControlStruct requestCacheControl = new RequestCacheControlStruct(cacheControlStruct);
            requestCacheControl.populateStruct(cacheControlHeader);
            inboundRequestStruct.put(REQUEST_CACHE_CONTROL_FIELD, requestCacheControl.getStruct());
        }
    }

    private static void enrichWithInboundRequestHeaders(BMap<String, BValue> inboundRequestStruct,
                                                        HttpCarbonMessage inboundRequestMsg) {
        if (inboundRequestMsg.getHeader(HttpHeaderNames.USER_AGENT.toString()) != null) {
            String agent = inboundRequestMsg.getHeader(HttpHeaderNames.USER_AGENT.toString());
            inboundRequestStruct.put(HttpConstants.REQUEST_USER_AGENT_FIELD, new BString(agent));
            inboundRequestMsg.removeHeader(HttpHeaderNames.USER_AGENT.toString());
        }
    }

    private static void enrichWithInboundRequestInfo(BMap<String, BValue> inboundRequestStruct,
                                                     HttpCarbonMessage inboundRequestMsg) {
        inboundRequestStruct.put(HttpConstants.REQUEST_RAW_PATH_FIELD,
                new BString((String) inboundRequestMsg.getProperty(HttpConstants.REQUEST_URL)));
        inboundRequestStruct.put(HttpConstants.REQUEST_METHOD_FIELD,
                new BString((String) inboundRequestMsg.getProperty(HttpConstants.HTTP_METHOD)));
        inboundRequestStruct.put(HttpConstants.REQUEST_VERSION_FIELD,
                new BString((String) inboundRequestMsg.getProperty(HttpConstants.HTTP_VERSION)));
        Map<String, String> resourceArgValues =
                (Map<String, String>) inboundRequestMsg.getProperty(HttpConstants.RESOURCE_ARGS);
        if (resourceArgValues != null && resourceArgValues.get(HttpConstants.EXTRA_PATH_INFO) != null) {
            inboundRequestStruct.put(HttpConstants.REQUEST_EXTRA_PATH_INFO_FIELD,
                    new BString(resourceArgValues.get(HttpConstants.EXTRA_PATH_INFO)));
        }
    }

    /**
     * Populates the HTTP caller with native data.
     *
     * @param caller     Represents the HTTP caller
     * @param inboundMsg Represents carbon message
     * @param config     Represents service endpoint configuration
     */
    public static void enrichHttpCallerWithNativeData(BMap<String, BValue> caller, HttpCarbonMessage inboundMsg,
                                                      Struct config) {
        caller.addNativeData(HttpConstants.TRANSPORT_MESSAGE, inboundMsg);
        caller.put(HttpConstants.HTTP_CONNECTOR_CONFIG_FIELD, (BMap<String, BValue>) config.getVMValue());
    }

    /**
     * Populates the HTTP caller with connection information.
     *
     * @param httpCaller   Represents the HTTP caller
     * @param inboundMsg   Represents the carbon message
     * @param httpResource Represents the Http Resource
     * @param config       Represents the service endpoint configuration
     */
    public static void enrichHttpCallerWithConnectionInfo(BMap<String, BValue> httpCaller, HttpCarbonMessage inboundMsg,
                                                          HttpResource httpResource, Struct config) {
        BMap<String, BValue> remote = BLangConnectorSPIUtil.createBStruct(
                httpResource.getBalResource().getResourceInfo().getPackageInfo().getProgramFile(),
                PROTOCOL_PACKAGE_HTTP, HttpConstants.REMOTE);
        BMap<String, BValue> local = BLangConnectorSPIUtil.createBStruct(
                httpResource.getBalResource().getResourceInfo().getPackageInfo().getProgramFile(),
                PROTOCOL_PACKAGE_HTTP, HttpConstants.LOCAL);

        Object remoteSocketAddress = inboundMsg.getProperty(HttpConstants.REMOTE_ADDRESS);
        if (remoteSocketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteSocketAddress;
            String remoteHost = inetSocketAddress.getHostName();
            long remotePort = inetSocketAddress.getPort();
            remote.put(HttpConstants.REMOTE_HOST_FIELD, new BString(remoteHost));
            remote.put(HttpConstants.REMOTE_PORT_FIELD, new BInteger(remotePort));
        }
        httpCaller.put(HttpConstants.REMOTE_STRUCT_FIELD, remote);

        Object localSocketAddress = inboundMsg.getProperty(HttpConstants.LOCAL_ADDRESS);
        if (localSocketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) localSocketAddress;
            String localHost = inetSocketAddress.getHostName();
            long localPort = inetSocketAddress.getPort();
            local.put(HttpConstants.LOCAL_HOST_FIELD, new BString(localHost));
            local.put(HttpConstants.LOCAL_PORT_FIELD, new BInteger(localPort));
        }
        httpCaller.put(HttpConstants.LOCAL_STRUCT_INDEX, local);
        httpCaller.put(HttpConstants.SERVICE_ENDPOINT_PROTOCOL_FIELD,
                new BString((String) inboundMsg.getProperty(HttpConstants.PROTOCOL)));
        httpCaller.put(HttpConstants.SERVICE_ENDPOINT_CONFIG_FIELD, (BMap<String, BValue>) config.getVMValue());
    }

    /**
     * Populate inbound response with headers and entity.
     * @param inboundResponse  Ballerina struct to represent response
     * @param entity    Entity of the response
     * @param mediaType Content type of the response
     * @param programFile  Cache control struct which holds the cache control directives related to the
*                              response
     * @param inboundResponseMsg      Represent carbon message.
     */
    public static void populateInboundResponse(BMap<String, BValue> inboundResponse, BMap<String, BValue> entity,
                                               BMap<String, BValue> mediaType, ProgramFile programFile,
                                               HttpCarbonMessage inboundResponseMsg) {
        inboundResponse.addNativeData(TRANSPORT_MESSAGE, inboundResponseMsg);
        int statusCode = (Integer) inboundResponseMsg.getProperty(HTTP_STATUS_CODE);
        inboundResponse.put(RESPONSE_STATUS_CODE_FIELD, new BInteger(statusCode));
        inboundResponse.put(RESPONSE_REASON_PHRASE_FIELD,
                new BString(HttpResponseStatus.valueOf(statusCode).reasonPhrase()));

        if (inboundResponseMsg.getHeader(HttpHeaderNames.SERVER.toString()) != null) {
            inboundResponse.put(HttpConstants.RESPONSE_SERVER_FIELD,
                    new BString(inboundResponseMsg.getHeader(HttpHeaderNames.SERVER.toString())));
            inboundResponseMsg.removeHeader(HttpHeaderNames.SERVER.toString());
        }

        if (inboundResponseMsg.getProperty(RESOLVED_REQUESTED_URI) != null) {
            inboundResponse.put(RESOLVED_REQUESTED_URI_FIELD,
                    new BString(inboundResponseMsg.getProperty(RESOLVED_REQUESTED_URI).toString()));
        }

        String cacheControlHeader = inboundResponseMsg.getHeader(CACHE_CONTROL.toString());
        if (cacheControlHeader != null) {
            ResponseCacheControlStruct responseCacheControl
                    = new ResponseCacheControlStruct(
                            programFile.getPackageInfo(PROTOCOL_PACKAGE_HTTP).getStructInfo(RESPONSE_CACHE_CONTROL));
            responseCacheControl.populateStruct(cacheControlHeader);
            inboundResponse.put(RESPONSE_CACHE_CONTROL_FIELD, responseCacheControl.getStruct());
        }

        populateEntity(entity, mediaType, inboundResponseMsg);
        inboundResponse.put(RESPONSE_ENTITY_FIELD, entity);
        inboundResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, false);
    }

    /**
     * Populate entity with headers, content-type and content-length.
     *
     * @param entity    Represent an entity struct
     * @param mediaType mediaType struct that needs to be set to the entity
     * @param cMsg      Represent a carbon message
     */
    private static void populateEntity(BMap<String, BValue> entity, BMap<String, BValue> mediaType,
                                       HttpCarbonMessage cMsg) {
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
     * @param outboundStruct req/resp struct.
     */
    public static void enrichOutboundMessage(HttpCarbonMessage outboundMsg, BMap<String, BValue> outboundStruct) {
        setHeadersToTransportMessage(outboundMsg, outboundStruct);
        setPropertiesToTransportMessage(outboundMsg, outboundStruct);
    }

    @SuppressWarnings("unchecked")
    private static void setHeadersToTransportMessage(HttpCarbonMessage outboundMsg, BMap<String, BValue> struct) {
        BMap<String, BValue> entityStruct = (BMap<String, BValue>) struct
                .get(isRequestStruct(struct) ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD);
        HttpHeaders transportHeaders = outboundMsg.getHeaders();
        if (isRequestStruct(struct) || isResponseStruct(struct)) {
            addRemovedPropertiesBackToHeadersMap(struct, transportHeaders);
            // Since now the InRequest & OutRequest are merged to a single Request and InResponse & OutResponse
            // are merged to a single Response, without returning need to populate all headers from the struct
            // to the HttpCarbonMessage.
            // TODO: refactor this logic properly.
            // return;
        }
        HttpHeaders httpHeaders = (HttpHeaders) entityStruct.getNativeData(ENTITY_HEADERS);
        if (httpHeaders != transportHeaders) {
            //This is done only when the entity map and transport message do not refer to the same header map
            if (httpHeaders != null) {
                transportHeaders.add(httpHeaders);
            }
            //Once the headers are synced, set the entity headers to transport message headers so that they
            //both refer the same header map for future operations
            entityStruct.addNativeData(ENTITY_HEADERS, outboundMsg.getHeaders());
        }
    }

    public static boolean isRequestStruct(BMap<String, BValue> struct) {
        return struct.getType().getName().equals(REQUEST);
    }

    private static boolean isResponseStruct(BMap<String, BValue> struct) {
        return struct.getType().getName().equals(HttpConstants.RESPONSE);
    }

    private static void addRemovedPropertiesBackToHeadersMap(BMap<String, BValue> struct,
                                                             HttpHeaders transportHeaders) {
        if (isRequestStruct(struct)) {
            BValue userAgent = struct.get(HttpConstants.REQUEST_USER_AGENT_FIELD);
            if (userAgent != null && !userAgent.stringValue().isEmpty()) {
                transportHeaders.set(HttpHeaderNames.USER_AGENT.toString(), userAgent.stringValue());
            }
        } else {
            BValue server = struct.get(HttpConstants.RESPONSE_SERVER_FIELD);
            if (server != null && !server.stringValue().isEmpty()) {
                transportHeaders.set(HttpHeaderNames.SERVER.toString(), server.stringValue());
            }
        }
    }

    private static void setPropertiesToTransportMessage(HttpCarbonMessage outboundResponseMsg,
                                                        BMap<String, BValue> struct) {
        if (isResponseStruct(struct)) {
            long statusCode = ((BInteger) struct.get(RESPONSE_STATUS_CODE_FIELD)).intValue();
            if (statusCode != 0) {
                outboundResponseMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, getIntValue(statusCode));
            }
            BValue respPhrase = struct.get(RESPONSE_REASON_PHRASE_FIELD);
            if (respPhrase != null && !respPhrase.stringValue().isEmpty()) {
                outboundResponseMsg.setProperty(HttpConstants.HTTP_REASON_PHRASE, respPhrase.stringValue());
            }
        }
    }

    /**
     * Check the existence of entity. Set new entity of not present.
     *
     * @param context ballerina context.
     * @param struct  request/response struct.
     */
    public static void checkEntityAvailability(Context context, BMap<String, BValue> struct) {
        BMap<String, BValue> entity = (BMap<String, BValue>) struct
                .get(isRequestStruct(struct) ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD);
        if (entity == null) {
            createNewEntity(context, struct);
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
     * @param struct  request/response struct.
     * @return true if the message entity data source is available else false.
     */
    public static boolean isEntityDataSourceAvailable(BMap<String, BValue> struct) {
        BMap<String, BValue> entityStruct = (BMap<String, BValue>) struct
                .get(isRequestStruct(struct) ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD);
        return (entityStruct != null && EntityBodyHandler.getMessageDataSource(entityStruct) != null);
    }

    private static void setCompressionHeaders(Context context, HttpCarbonMessage requestMsg, HttpCarbonMessage
            outboundResponseMsg) {
        Service serviceInstance = BLangConnectorSPIUtil.getService(context.getProgramFile(),
                context.getServiceInfo().serviceValue);
        Annotation configAnnot = getServiceConfigAnnotation(serviceInstance, PROTOCOL_PACKAGE_HTTP);
        if (!checkConfigAnnotationAvailability(configAnnot)) {
            return;
        }
        String contentEncoding = outboundResponseMsg.getHeaders().get(HttpHeaderNames.CONTENT_ENCODING);
        if (contentEncoding != null) {
            return;
        }

        Struct compressionConfig = configAnnot.getValue().getStructField(ANN_CONFIG_ATTR_COMPRESSION);
        CompressionConfigState compressionState = getCompressionState(
                compressionConfig.getStringField(ANN_CONFIG_ATTR_COMPRESSION_ENABLE));

        if (compressionState == CompressionConfigState.NEVER) {
            outboundResponseMsg.getHeaders().set(HttpHeaderNames.CONTENT_ENCODING, HTTP_TRANSFER_ENCODING_IDENTITY);
            return;
        }

        String acceptEncodingValue = requestMsg.getHeaders().get(HttpHeaderNames.ACCEPT_ENCODING);
        List<String> contentTypesAnnotationValues = getAsStringList(
                compressionConfig.getArrayField(ANN_CONFIG_ATTR_COMPRESSION_CONTENT_TYPES));
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

    private static List<String> getAsStringList(Value[] values) {
        List<String> valuesList = new ArrayList<>();
        if (values == null) {
            return valuesList;
        }
        for (Value val : values) {
            valuesList.add(val.getStringValue().trim().toLowerCase());
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

    public static void checkFunctionValidity(BMap<String, BValue> connectionStruct, HttpCarbonMessage reqMsg,
                                             HttpCarbonMessage outboundResponseMsg) {
        serverConnectionStructCheck(reqMsg);
        int statusCode = (int) outboundResponseMsg.getProperty(HttpConstants.HTTP_STATUS_CODE);
        methodInvocationCheck(connectionStruct, reqMsg, statusCode);
    }

    private static void methodInvocationCheck(BMap<String, BValue> bStruct, HttpCarbonMessage reqMsg, int statusCode) {
        if (bStruct.getNativeData(METHOD_ACCESSED) != null || reqMsg == null) {
            throw new IllegalStateException("illegal function invocation");
        }

        if (!is100ContinueRequest(reqMsg, statusCode)) {
            bStruct.addNativeData(METHOD_ACCESSED, true);
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

        if (annotationList == null) {
            return null;
        }

        if (annotationList.size() > 1) {
            throw new BallerinaException(
                    "multiple service configuration annotations found in service: " + service.getName());
        }

        return annotationList.isEmpty() ? null : annotationList.get(0);
    }

    public static Annotation getServiceConfigStruct(Service service, String pkgPath) {
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

    public static Annotation getTransactionConfigAnnotation(Resource resource, String transactionPackagePath) {
        List<Annotation> annotationList = resource.getAnnotationList(transactionPackagePath,
                TransactionConstants.ANN_NAME_TRX_PARTICIPANT_CONFIG);

        if (annotationList == null || annotationList.isEmpty()) {
            return null;
        }
        if (annotationList.size() > 1) {
            throw new BallerinaException(
                    "multiple transaction configuration annotations found in resource: " +
                            resource.getServiceName() + "." + resource.getName());
        }
        return annotationList.get(0);
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
    public static String addBoundaryIfNotExist(HttpCarbonMessage transportMessage, String contentType) {
        String boundaryString;
        BString boundaryValue = HeaderUtil.extractBoundaryParameter(contentType);
        boundaryString = boundaryValue != null ? boundaryValue.toString() :
                HttpUtil.addBoundaryParameter(transportMessage, contentType);
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

    public static void checkAndObserveHttpRequest(Context context, HttpCarbonMessage message) {
        Optional<ObserverContext> observerContext = ObserveUtils.getObserverContextOfCurrentFrame(context);
        observerContext.ifPresent(ctx -> {
            HttpUtil.injectHeaders(message, ObserveUtils.getContextProperties(ctx));
            ctx.addTag(TAG_KEY_HTTP_METHOD, String.valueOf(message.getProperty(HttpConstants.HTTP_METHOD)));
            ctx.addTag(TAG_KEY_HTTP_URL, String.valueOf(message.getProperty(HttpConstants.TO)));
            ctx.addTag(TAG_KEY_PEER_ADDRESS,
                       message.getProperty(PROPERTY_HTTP_HOST) + ":" + message.getProperty(PROPERTY_HTTP_PORT));
            // Add HTTP Status Code tag. The HTTP status code will be set using the response message.
            // Sometimes the HTTP status code will not be set due to errors etc. Therefore, it's very important to set
            // some value to HTTP Status Code to make sure that tags will not change depending on various
            // circumstances.
            // HTTP Status code must be a number.
            ctx.addTag(TAG_KEY_HTTP_STATUS_CODE, Integer.toString(0));
        });
    }

    public static void injectHeaders(HttpCarbonMessage msg, Map<String, String> headers) {
        if (headers != null) {
            headers.forEach((key, value) -> msg.setHeader(key, String.valueOf(value)));
        }
    }

    private static void setChunkingHeader(Context context, HttpCarbonMessage
            outboundResponseMsg) {
        Service serviceInstance = BLangConnectorSPIUtil.getService(context.getProgramFile(),
                context.getServiceInfo().serviceValue);
        Annotation configAnnot = getServiceConfigAnnotation(serviceInstance, PROTOCOL_PACKAGE_HTTP);
        if (!checkConfigAnnotationAvailability(configAnnot)) {
            return;
        }
        String transferValue = configAnnot.getValue().getRefField(ANN_CONFIG_ATTR_CHUNKING).getStringValue();
        if (transferValue != null) {
            outboundResponseMsg.setProperty(CHUNKING_CONFIG, getChunkConfig(transferValue));
        }
    }

    /**
     * Creates InResponse using the native {@code HttpCarbonMessage}.
     *
     * @param context           ballerina context
     * @param httpCarbonMessage the HttpCarbonMessage
     * @return the Response struct
     */
    public static BMap<String, BValue> createResponseStruct(Context context, HttpCarbonMessage httpCarbonMessage) {
        BMap<String, BValue> responseStruct = BLangConnectorSPIUtil.createBStruct(context,
                HttpConstants.PROTOCOL_PACKAGE_HTTP, HttpConstants.RESPONSE);
        BMap<String, BValue> entity =
                BLangConnectorSPIUtil.createBStruct(context, PROTOCOL_PACKAGE_MIME, HttpConstants.ENTITY);
        BMap<String, BValue> mediaType =
                BLangConnectorSPIUtil.createBStruct(context, PROTOCOL_PACKAGE_MIME, MEDIA_TYPE);

        HttpUtil.populateInboundResponse(responseStruct, entity, mediaType, context.getProgramFile(),
                httpCarbonMessage);
        return responseStruct;
    }

    /**
     * Populates Sender configuration instance with client endpoint configuration.
     *
     * @param senderConfiguration  sender configuration instance.
     * @param clientEndpointConfig client endpoint configuration.
     */
    @Deprecated
    public static void populateSenderConfigurationOptions(SenderConfiguration senderConfiguration, Struct
            clientEndpointConfig) {
        ProxyServerConfiguration proxyServerConfiguration;
        Struct secureSocket = clientEndpointConfig.getStructField(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET);

        if (secureSocket != null) {
            HttpUtil.populateSSLConfiguration(senderConfiguration, secureSocket);
        } else {
            HttpUtil.setDefaultTrustStore(senderConfiguration);
        }
        Struct proxy = clientEndpointConfig.getStructField(HttpConstants.PROXY_STRUCT_REFERENCE);
        if (proxy != null) {
            String proxyHost = proxy.getStringField(HttpConstants.PROXY_HOST);
            int proxyPort = (int) proxy.getIntField(HttpConstants.PROXY_PORT);
            String proxyUserName = proxy.getStringField(HttpConstants.PROXY_USERNAME);
            String proxyPassword = proxy.getStringField(HttpConstants.PROXY_PASSWORD);
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

        String chunking = clientEndpointConfig.getRefField(HttpConstants.CLIENT_EP_CHUNKING).getStringValue();
        senderConfiguration.setChunkingConfig(HttpUtil.getChunkConfig(chunking));

        long timeoutMillis = clientEndpointConfig.getIntField(HttpConstants.CLIENT_EP_ENDPOINT_TIMEOUT);
        if (timeoutMillis < 0 || !isInteger(timeoutMillis)) {
            throw new BallerinaConnectorException("invalid idle timeout: " + timeoutMillis);
        }
        senderConfiguration.setSocketIdleTimeout((int) timeoutMillis);

        String keepAliveConfig = clientEndpointConfig.getRefField(HttpConstants.CLIENT_EP_IS_KEEP_ALIVE)
                .getStringValue();
        senderConfiguration.setKeepAliveConfig(HttpUtil.getKeepAliveConfig(keepAliveConfig));

        String httpVersion = clientEndpointConfig.getStringField(HttpConstants.CLIENT_EP_HTTP_VERSION);
        if (httpVersion != null) {
            senderConfiguration.setHttpVersion(httpVersion);
        }
        String forwardedExtension = clientEndpointConfig.getStringField(HttpConstants.CLIENT_EP_FORWARDED);
        senderConfiguration.setForwardedExtensionConfig(HttpUtil.getForwardedExtensionConfig(forwardedExtension));

        Struct connectionThrottling = clientEndpointConfig.getStructField(HttpConstants.
                CONNECTION_THROTTLING_STRUCT_REFERENCE);
        if (connectionThrottling != null) {
            long maxActiveConnections = connectionThrottling
                    .getIntField(HttpConstants.CONNECTION_THROTTLING_MAX_ACTIVE_CONNECTIONS);
            if (!isInteger(maxActiveConnections)) {
                throw new BallerinaConnectorException("invalid maxActiveConnections value: "
                        + maxActiveConnections);
            }
            senderConfiguration.getPoolConfiguration().setMaxActivePerPool((int) maxActiveConnections);

            long waitTime = connectionThrottling
                    .getIntField(HttpConstants.CONNECTION_THROTTLING_WAIT_TIME);
            senderConfiguration.getPoolConfiguration().setMaxWaitTime(waitTime);

            long maxActiveStreamsPerConnection = connectionThrottling.
                    getIntField(HttpConstants.CONNECTION_THROTTLING_MAX_ACTIVE_STREAMS_PER_CONNECTION);
            if (!isInteger(maxActiveStreamsPerConnection)) {
                throw new BallerinaConnectorException("invalid maxActiveStreamsPerConnection value: "
                        + maxActiveStreamsPerConnection);
            }
            senderConfiguration.getPoolConfiguration().setHttp2MaxActiveStreamsPerConnection(
                    maxActiveStreamsPerConnection == -1 ? Integer.MAX_VALUE : (int) maxActiveStreamsPerConnection);
        }
    }

    public static void populateSenderConfigurations(SenderConfiguration senderConfiguration, Struct
            clientEndpointConfig) {
        ProxyServerConfiguration proxyServerConfiguration;
        Struct secureSocket = clientEndpointConfig.getStructField(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET);

        if (secureSocket != null) {
            HttpUtil.populateSSLConfiguration(senderConfiguration, secureSocket);
        } else {
            HttpUtil.setDefaultTrustStore(senderConfiguration);
        }
        Struct proxy = clientEndpointConfig.getStructField(HttpConstants.PROXY_STRUCT_REFERENCE);
        if (proxy != null) {
            String proxyHost = proxy.getStringField(HttpConstants.PROXY_HOST);
            int proxyPort = (int) proxy.getIntField(HttpConstants.PROXY_PORT);
            String proxyUserName = proxy.getStringField(HttpConstants.PROXY_USERNAME);
            String proxyPassword = proxy.getStringField(HttpConstants.PROXY_PASSWORD);
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

        String chunking = clientEndpointConfig.getRefField(HttpConstants.CLIENT_EP_CHUNKING).getStringValue();
        senderConfiguration.setChunkingConfig(HttpUtil.getChunkConfig(chunking));

        long timeoutMillis = clientEndpointConfig.getIntField(HttpConstants.CLIENT_EP_ENDPOINT_TIMEOUT);
        if (timeoutMillis < 0) {
            senderConfiguration.setSocketIdleTimeout(0);
        } else {
            senderConfiguration.setSocketIdleTimeout(
                    validateConfig(timeoutMillis, HttpConstants.CLIENT_EP_ENDPOINT_TIMEOUT));
        }
        String keepAliveConfig = clientEndpointConfig.getRefField(HttpConstants.CLIENT_EP_IS_KEEP_ALIVE)
                .getStringValue();
        senderConfiguration.setKeepAliveConfig(HttpUtil.getKeepAliveConfig(keepAliveConfig));

        String httpVersion = clientEndpointConfig.getStringField(HttpConstants.CLIENT_EP_HTTP_VERSION);
        if (httpVersion != null) {
            senderConfiguration.setHttpVersion(httpVersion);
        }
        String forwardedExtension = clientEndpointConfig.getStringField(HttpConstants.CLIENT_EP_FORWARDED);
        senderConfiguration.setForwardedExtensionConfig(HttpUtil.getForwardedExtensionConfig(forwardedExtension));
    }

    public static void populatePoolingConfig(BMap<String, BValue> poolRecord, PoolConfiguration poolConfiguration) {
        long maxActiveConnections = ((BInteger) poolRecord
                .get(HttpConstants.CONNECTION_POOLING_MAX_ACTIVE_CONNECTIONS)).intValue();
        poolConfiguration.setMaxActivePerPool(
                validateConfig(maxActiveConnections, HttpConstants.CONNECTION_POOLING_MAX_ACTIVE_CONNECTIONS));

        long maxIdleConnections = ((BInteger) poolRecord
                .get(HttpConstants.CONNECTION_POOLING_MAX_IDLE_CONNECTIONS)).intValue();
        poolConfiguration.setMaxIdlePerPool(
                validateConfig(maxIdleConnections, HttpConstants.CONNECTION_POOLING_MAX_IDLE_CONNECTIONS));

        long waitTime = ((BInteger) poolRecord.get(HttpConstants.CONNECTION_POOLING_WAIT_TIME)).intValue();
        poolConfiguration.setMaxWaitTime(waitTime);

        long maxActiveStreamsPerConnection = ((BInteger) poolRecord.
                get(CONNECTION_POOLING_MAX_ACTIVE_STREAMS_PER_CONNECTION)).intValue();
        poolConfiguration.setHttp2MaxActiveStreamsPerConnection(
                maxActiveStreamsPerConnection == -1 ? Integer.MAX_VALUE : validateConfig(maxActiveStreamsPerConnection,
                                                                CONNECTION_POOLING_MAX_ACTIVE_STREAMS_PER_CONNECTION));
    }

    private static boolean isInteger(long val) {
        return (int) val == val;
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
    public static void populateSSLConfiguration(SslConfiguration sslConfiguration, Struct secureSocket) {
        Struct trustStore = secureSocket.getStructField(ENDPOINT_CONFIG_TRUST_STORE);
        Struct keyStore = secureSocket.getStructField(ENDPOINT_CONFIG_KEY_STORE);
        Struct protocols = secureSocket.getStructField(ENDPOINT_CONFIG_PROTOCOLS);
        Struct validateCert = secureSocket.getStructField(ENDPOINT_CONFIG_VALIDATE_CERT);
        String keyFile = secureSocket.getStringField(ENDPOINT_CONFIG_KEY);
        String certFile = secureSocket.getStringField(ENDPOINT_CONFIG_CERTIFICATE);
        String trustCerts = secureSocket.getStringField(ENDPOINT_CONFIG_TRUST_CERTIFICATES);
        String keyPassword = secureSocket.getStringField(ENDPOINT_CONFIG_KEY_PASSWORD);
        List<Parameter> clientParams = new ArrayList<>();
        if (trustStore != null && StringUtils.isNotBlank(trustCerts)) {
            throw new BallerinaException("Cannot configure both trustStore and trustCerts at the same time.");
        }
        if (trustStore != null) {
            String trustStoreFile = trustStore.getStringField(FILE_PATH);
            if (StringUtils.isNotBlank(trustStoreFile)) {
                sslConfiguration.setTrustStoreFile(trustStoreFile);
            }
            String trustStorePassword = trustStore.getStringField(PASSWORD);
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
            String keyStoreFile = keyStore.getStringField(FILE_PATH);
            if (StringUtils.isNotBlank(keyStoreFile)) {
                sslConfiguration.setKeyStoreFile(keyStoreFile);
            }
            String keyStorePassword = keyStore.getStringField(PASSWORD);
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
            List<Value> sslEnabledProtocolsValueList = Arrays
                    .asList(protocols.getArrayField(ENABLED_PROTOCOLS));
            if (sslEnabledProtocolsValueList.size() > 0) {
                String sslEnabledProtocols = sslEnabledProtocolsValueList.stream().map(Value::getStringValue)
                        .collect(Collectors.joining(",", "", ""));
                Parameter clientProtocols = new Parameter(SSL_ENABLED_PROTOCOLS, sslEnabledProtocols);
                clientParams.add(clientProtocols);
            }
            String sslProtocol = protocols.getStringField(PROTOCOL_VERSION);
            if (StringUtils.isNotBlank(sslProtocol)) {
                sslConfiguration.setSSLProtocol(sslProtocol);
            }
        }

        if (validateCert != null) {
            boolean validateCertEnabled = validateCert.getBooleanField(HttpConstants.ENABLE);
            int cacheSize = (int) validateCert.getIntField(HttpConstants.SSL_CONFIG_CACHE_SIZE);
            int cacheValidityPeriod = (int) validateCert
                    .getIntField(HttpConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD);
            sslConfiguration.setValidateCertEnabled(validateCertEnabled);
            if (cacheValidityPeriod != 0) {
                sslConfiguration.setCacheValidityPeriod(cacheValidityPeriod);
            }
            if (cacheSize != 0) {
                sslConfiguration.setCacheSize(cacheSize);
            }
        }
        boolean hostNameVerificationEnabled = secureSocket
                .getBooleanField(HttpConstants.SSL_CONFIG_HOST_NAME_VERIFICATION_ENABLED);
        boolean ocspStaplingEnabled = secureSocket.getBooleanField(HttpConstants.ENDPOINT_CONFIG_OCSP_STAPLING);
        sslConfiguration.setOcspStaplingEnabled(ocspStaplingEnabled);
        sslConfiguration.setHostNameVerificationEnabled(hostNameVerificationEnabled);

        List<Value> ciphersValueList = Arrays
                .asList(secureSocket.getArrayField(HttpConstants.SSL_CONFIG_CIPHERS));
        if (ciphersValueList.size() > 0) {
            String ciphers = ciphersValueList.stream().map(Value::getStringValue)
                    .collect(Collectors.joining(",", "", ""));
            Parameter clientCiphers = new Parameter(HttpConstants.CIPHERS, ciphers);
            clientParams.add(clientCiphers);
        }
        String enableSessionCreation = String.valueOf(secureSocket
                .getBooleanField(HttpConstants.SSL_CONFIG_ENABLE_SESSION_CREATION));
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
    public static void serializeDataSource(BValue outboundMessageSource, BMap<String, BValue> entity,
                                           OutputStream messageOutputStream) throws IOException {
        if (MimeUtil.generateAsJSON(outboundMessageSource, entity)) {
            JsonGenerator gen = new JsonGenerator(messageOutputStream);
            gen.serialize(outboundMessageSource);
            gen.flush();
        } else {
            outboundMessageSource.serialize(messageOutputStream);
        }
    }

    /**
     * Check the availability of an annotation.
     *
     * @param configAnnotation      Represent the annotation
     * @return True if the annotation and the annotation value are available
     */
    public static boolean checkConfigAnnotationAvailability(Annotation configAnnotation) {
        return configAnnotation != null && configAnnotation.getValue() != null;
    }

    /**
     * Returns Listener configuration instance populated with endpoint config.
     *
     * @param port              listener port.
     * @param endpointConfig    listener endpoint configuration.
     * @return                  transport listener configuration instance.
     */
    public static ListenerConfiguration getListenerConfig(long port, Struct endpointConfig) {
        String host = endpointConfig.getStringField(HttpConstants.ENDPOINT_CONFIG_HOST);
        String keepAlive = endpointConfig.getRefField(HttpConstants.ENDPOINT_CONFIG_KEEP_ALIVE).getStringValue();
        Struct sslConfig = endpointConfig.getStructField(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET);
        String httpVersion = endpointConfig.getStringField(HttpConstants.ENDPOINT_CONFIG_VERSION);
        Struct requestLimits = endpointConfig.getStructField(HttpConstants.ENDPOINT_REQUEST_LIMITS);
        long idleTimeout = endpointConfig.getIntField(HttpConstants.ENDPOINT_CONFIG_TIMEOUT);

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
        listenerConfiguration.setPipeliningLimit(endpointConfig.getIntField(
                HttpConstants.PIPELINING_REQUEST_LIMIT));

        return listenerConfiguration;
    }

    private static void setRequestSizeValidationConfig(Struct requestLimits,
                                                     ListenerConfiguration listenerConfiguration) {
        long maxUriLength = requestLimits.getIntField(HttpConstants.REQUEST_LIMITS_MAXIMUM_URL_LENGTH);
        long maxHeaderSize = requestLimits.getIntField(HttpConstants.REQUEST_LIMITS_MAXIMUM_HEADER_SIZE);
        long maxEntityBodySize = requestLimits.getIntField(HttpConstants.REQUEST_LIMITS_MAXIMUM_ENTITY_BODY_SIZE);
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

    private static ListenerConfiguration setSslConfig(Struct sslConfig, ListenerConfiguration listenerConfiguration) {
        listenerConfiguration.setScheme(PROTOCOL_HTTPS);
        Struct trustStore = sslConfig.getStructField(ENDPOINT_CONFIG_TRUST_STORE);
        Struct keyStore = sslConfig.getStructField(ENDPOINT_CONFIG_KEY_STORE);
        Struct protocols = sslConfig.getStructField(ENDPOINT_CONFIG_PROTOCOLS);
        Struct validateCert = sslConfig.getStructField(ENDPOINT_CONFIG_VALIDATE_CERT);
        Struct ocspStapling = sslConfig.getStructField(ENDPOINT_CONFIG_OCSP_STAPLING);
        String keyFile = sslConfig.getStringField(ENDPOINT_CONFIG_KEY);
        String certFile = sslConfig.getStringField(ENDPOINT_CONFIG_CERTIFICATE);
        String trustCerts = sslConfig.getStringField(ENDPOINT_CONFIG_TRUST_CERTIFICATES);
        String keyPassword = sslConfig.getStringField(ENDPOINT_CONFIG_KEY_PASSWORD);

        if (keyStore != null && StringUtils.isNotBlank(keyFile)) {
            throw new BallerinaException("Cannot configure both keyStore and keyFile at the same time.");
        } else if (keyStore == null && (StringUtils.isBlank(keyFile) || StringUtils.isBlank(certFile))) {
            throw new BallerinaException("Either keystore or certificateKey and server certificates must be provided "
                    + "for secure connection");
        }
        if (keyStore != null) {
            String keyStoreFile = keyStore.getStringField(FILE_PATH);
            if (StringUtils.isBlank(keyStoreFile)) {
                throw new BallerinaException("Keystore file location must be provided for secure connection.");
            }
            String keyStorePassword = keyStore.getStringField(PASSWORD);
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
        String sslVerifyClient = sslConfig.getStringField(SSL_CONFIG_SSL_VERIFY_CLIENT);
        listenerConfiguration.setVerifyClient(sslVerifyClient);
        if (trustStore == null && StringUtils.isNotBlank(sslVerifyClient) && StringUtils.isBlank(trustCerts)) {
            throw new BallerinaException(
                    "Truststore location or trustCertificates must be provided to enable Mutual SSL");
        }
        if (trustStore != null) {
            String trustStoreFile = trustStore.getStringField(FILE_PATH);
            String trustStorePassword = trustStore.getStringField(PASSWORD);
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
            List<Value> sslEnabledProtocolsValueList = Arrays.asList(protocols.getArrayField(ENABLED_PROTOCOLS));
            if (!sslEnabledProtocolsValueList.isEmpty()) {
                String sslEnabledProtocols = sslEnabledProtocolsValueList.stream().map(Value::getStringValue)
                        .collect(Collectors.joining(",", "", ""));
                serverParameters = new Parameter(ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS, sslEnabledProtocols);
                serverParamList.add(serverParameters);
            }

            String sslProtocol = protocols.getStringField(PROTOCOL_VERSION);
            if (StringUtils.isNotBlank(sslProtocol)) {
                listenerConfiguration.setSSLProtocol(sslProtocol);
            }
        }

        List<Value> ciphersValueList = Arrays.asList(sslConfig.getArrayField(HttpConstants.SSL_CONFIG_CIPHERS));
        if (!ciphersValueList.isEmpty()) {
            String ciphers = ciphersValueList.stream().map(Value::getStringValue)
                    .collect(Collectors.joining(",", "", ""));
            serverParameters = new Parameter(HttpConstants.CIPHERS, ciphers);
            serverParamList.add(serverParameters);
        }
        if (validateCert != null) {
            boolean validateCertificateEnabled = validateCert.getBooleanField(HttpConstants.ENABLE);
            long cacheSize = validateCert.getIntField(HttpConstants.SSL_CONFIG_CACHE_SIZE);
            long cacheValidationPeriod = validateCert.getIntField(HttpConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD);
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
            boolean ocspStaplingEnabled = ocspStapling.getBooleanField(HttpConstants.ENABLE);
            listenerConfiguration.setOcspStaplingEnabled(ocspStaplingEnabled);
            long cacheSize = ocspStapling.getIntField(HttpConstants.SSL_CONFIG_CACHE_SIZE);
            long cacheValidationPeriod = ocspStapling.getIntField(HttpConstants.SSL_CONFIG_CACHE_VALIDITY_PERIOD);
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
                .valueOf(sslConfig.getBooleanField(SSL_CONFIG_ENABLE_SESSION_CREATION));
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
