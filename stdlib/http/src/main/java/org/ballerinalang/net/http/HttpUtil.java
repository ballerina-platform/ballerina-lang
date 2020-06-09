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
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.JSONGenerator;
import org.ballerinalang.jvm.observability.ObserveUtils;
import org.ballerinalang.jvm.observability.ObserverContext;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.services.ErrorHandlerUtils;
import org.ballerinalang.jvm.transactions.TransactionConstants;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.StreamingJsonValue;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLSequence;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.mime.util.EntityBodyChannel;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityWrapper;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDataSource;
import org.ballerinalang.mime.util.MultipartDecoder;
import org.ballerinalang.net.http.caching.RequestCacheControlObj;
import org.ballerinalang.net.http.caching.ResponseCacheControlObj;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.stdlib.io.utils.IOConstants;
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
import org.wso2.transport.http.netty.contract.exceptions.ClientConnectorException;
import org.wso2.transport.http.netty.contract.exceptions.ConnectionTimedOutException;
import org.wso2.transport.http.netty.contract.exceptions.EndpointTimeOutException;
import org.wso2.transport.http.netty.contract.exceptions.PromiseRejectedException;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contract.exceptions.SslException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.netty.handler.codec.http.HttpHeaderNames.CACHE_CONTROL;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.PROPERTY_HTTP_HOST;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.PROPERTY_HTTP_PORT;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.TAG_KEY_HTTP_METHOD;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.TAG_KEY_HTTP_URL;
import static org.ballerinalang.jvm.observability.ObservabilityConstants.TAG_KEY_PEER_ADDRESS;
import static org.ballerinalang.jvm.runtime.RuntimeConstants.BALLERINA_VERSION;
import static org.ballerinalang.mime.util.EntityBodyHandler.checkEntityBodyAvailability;
import static org.ballerinalang.mime.util.MimeConstants.BOUNDARY;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_TRAILER_HEADERS;
import static org.ballerinalang.mime.util.MimeConstants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.OCTET_STREAM;
import static org.ballerinalang.mime.util.MimeConstants.REQUEST_ENTITY_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.RESPONSE_ENTITY_FIELD;
import static org.ballerinalang.net.http.HttpConstants.ALWAYS;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_COMPRESSION_CONTENT_TYPES;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_COMPRESSION_ENABLE;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.AUTO;
import static org.ballerinalang.net.http.HttpConstants.CONNECTION_MANAGER;
import static org.ballerinalang.net.http.HttpConstants.CONNECTION_POOLING_MAX_ACTIVE_STREAMS_PER_CONNECTION;
import static org.ballerinalang.net.http.HttpConstants.ENABLED_PROTOCOLS;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_CERTIFICATE;
import static org.ballerinalang.net.http.HttpConstants.ENDPOINT_CONFIG_DISABLE_SSL;
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
import static org.ballerinalang.net.http.HttpConstants.HTTP_ERROR_MESSAGE;
import static org.ballerinalang.net.http.HttpConstants.LISTENER_CONFIGURATION;
import static org.ballerinalang.net.http.HttpConstants.MUTUAL_SSL_CERTIFICATE;
import static org.ballerinalang.net.http.HttpConstants.MUTUAL_SSL_HANDSHAKE_RECORD;
import static org.ballerinalang.net.http.HttpConstants.NEVER;
import static org.ballerinalang.net.http.HttpConstants.PASSWORD;
import static org.ballerinalang.net.http.HttpConstants.PKCS_STORE_TYPE;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTPS;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTP_PKG_ID;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;
import static org.ballerinalang.net.http.HttpConstants.REQUEST_CACHE_CONTROL_FIELD;
import static org.ballerinalang.net.http.HttpConstants.REQUEST_MUTUAL_SSL_HANDSHAKE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.REQUEST_MUTUAL_SSL_HANDSHAKE_STATUS;
import static org.ballerinalang.net.http.HttpConstants.RESOLVED_REQUESTED_URI;
import static org.ballerinalang.net.http.HttpConstants.RESOLVED_REQUESTED_URI_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_CACHE_CONTROL;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_CACHE_CONTROL_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_REASON_PHRASE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_STATUS_CODE_FIELD;
import static org.ballerinalang.net.http.HttpConstants.SERVER_NAME;
import static org.ballerinalang.net.http.HttpConstants.SSL_CONFIG_ENABLE_SESSION_CREATION;
import static org.ballerinalang.net.http.HttpConstants.SSL_CONFIG_SSL_VERIFY_CLIENT;
import static org.ballerinalang.net.http.HttpConstants.SSL_PROTOCOL_VERSION;
import static org.ballerinalang.net.http.HttpConstants.TRANSPORT_MESSAGE;
import static org.ballerinalang.net.http.nativeimpl.pipelining.PipeliningHandler.sendPipelinedResponse;
import static org.ballerinalang.stdlib.io.utils.IOConstants.IO_PACKAGE_ID;
import static org.wso2.transport.http.netty.contract.Constants.ENCODING_GZIP;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_1_1_VERSION;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_2_0_VERSION;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_TRANSFER_ENCODING_IDENTITY;
import static org.wso2.transport.http.netty.contract.Constants.PROMISED_STREAM_REJECTED_ERROR;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_100_CONTINUE_RESPONSE;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_OUTBOUND_RESPONSE;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_BODY;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_HEADERS;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_WHILE_WRITING_100_CONTINUE_RESPONSE;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_HEADERS;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_SERVER_CLOSED_BEFORE_INITIATING_OUTBOUND_REQUEST;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE_BODY;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE_HEADERS;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_BODY;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_HEADERS;

/**
 * Utility class providing utility methods.
 */
public class HttpUtil {

    public static final boolean TRUE = true;
    public static final boolean FALSE = false;

    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    private static final String METHOD_ACCESSED = "isMethodAccessed";
    private static final String IO_EXCEPTION_OCCURRED = "I/O exception occurred";
    private static final String CHUNKING_CONFIG = "chunking_config";

    /**
     * Set new entity to in/out request/response struct.
     *
     * @param httpMessageStruct request/response struct.
     * @return created entity.
     */
    public static ObjectValue createNewEntity(ObjectValue httpMessageStruct) {
        ObjectValue entity = ValueCreatorUtils.createEntityObject();
        HttpCarbonMessage httpCarbonMessage = HttpUtil.getCarbonMsg(httpMessageStruct,
                HttpUtil.createHttpCarbonMessage(isRequest(httpMessageStruct)));
        entity.addNativeData(ENTITY_HEADERS, httpCarbonMessage.getHeaders());
        entity.addNativeData(ENTITY_TRAILER_HEADERS, httpCarbonMessage.getTrailerHeaders());
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
            } else {
                if (HttpHeaderValues.CHUNKED.toString().equals(
                        httpCarbonMessage.getHeader(HttpHeaderNames.TRANSFER_ENCODING.toString()))) {
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
        HttpUtil.addCorsHeaders(inboundRequestMsg, outboundResponseMsg);
        HttpUtil.enrichOutboundMessage(outboundResponseMsg, outboundResponseObj);
        HttpService httpService = (HttpService) connectionObj.getNativeData(HttpConstants.HTTP_SERVICE);
        HttpUtil.setCompressionHeaders(httpService.getCompressionConfig(), inboundRequestMsg, outboundResponseMsg);
        HttpUtil.setChunkingHeader(httpService.getChunkingConfig(), outboundResponseMsg);
    }

    private static void addCorsHeaders(HttpCarbonMessage requestMsg, HttpCarbonMessage responseMsg) {
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
        } catch (ServerConnectorException e) {
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
        } catch (ServerConnectorException e) {
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
        } catch (ServerConnectorException e) {
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
        ErrorHandlerUtils.printError("error: " + error.getPrintableStackTrace());
        sendPipelinedResponse(requestMessage, createErrorMessage(errorMsg, statusCode));
    }

    private static String getErrorMessage(ErrorValue error) {
        MapValue errorDetails = (MapValue) error.getDetails();
        if (!errorDetails.isEmpty()) {
            return errorDetails.get(HTTP_ERROR_MESSAGE).toString();
        }
        return error.getErrorMessage().getValue();
    }

    private static int getStatusCode(HttpCarbonMessage requestMessage, String errorMsg) {
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
     * Get error value from throwable.
     *
     * @param throwable Throwable representing the error.
     * @return Error struct
     */
    public static ErrorValue getError(Throwable throwable) {
        if (throwable instanceof ClientConnectorException) {
            return createHttpError(throwable);
        }
        if (throwable.getMessage() == null) {
            return createHttpError(IO_EXCEPTION_OCCURRED);
        } else {
            return createHttpError(throwable.getMessage());
        }
    }

    public static ErrorValue createHttpError(String errorMessage) {
        HttpErrorType errorType = getErrorType(errorMessage);
        return createHttpError(errorMessage, errorType);
    }

    public static ErrorValue createHttpError(Throwable throwable) {
        ErrorValue cause;
        if (throwable instanceof EndpointTimeOutException) {
            return createHttpError(throwable.getMessage(), HttpErrorType.IDLE_TIMEOUT_TRIGGERED);
        } else if (throwable instanceof SslException) {
            return createHttpError(throwable.getMessage(), HttpErrorType.SSL_ERROR);
        } else if (throwable instanceof PromiseRejectedException) {
            return createHttpError(throwable.getMessage(), HttpErrorType.HTTP2_CLIENT_ERROR);
        } else if (throwable instanceof ConnectionTimedOutException) {
            cause = createErrorCause(throwable.getMessage(),
                                     IOConstants.ErrorCode.ConnectionTimedOut.errorCode(),
                                     IO_PACKAGE_ID);
            return createHttpError("Something wrong with the connection", HttpErrorType.GENERIC_CLIENT_ERROR, cause);
        } else if (throwable instanceof ClientConnectorException) {
            cause = createErrorCause(throwable.getMessage(),
                                     IOConstants.ErrorCode.GenericError.errorCode(),
                                     IO_PACKAGE_ID);
            return createHttpError("Something wrong with the connection", HttpErrorType.GENERIC_CLIENT_ERROR, cause);
        } else {
            return createHttpError(throwable.getMessage());
        }
    }

    public static ErrorValue createHttpError(String message, HttpErrorType errorType) {
        return BallerinaErrors.createDistinctError(errorType.getErrorName(), PROTOCOL_HTTP_PKG_ID, message);
    }

    public static ErrorValue createHttpError(String message, HttpErrorType errorType, ErrorValue cause) {
        return BallerinaErrors.createDistinctError(errorType.getErrorName(), PROTOCOL_HTTP_PKG_ID, message, cause);
    }

    // TODO: Find a better way to get the error type than String matching.
    private static HttpErrorType getErrorType(String errorMessage) {
        // Every Idle Timeout triggered error is mapped to IdleTimeoutError
        if (errorMessage.contains("Idle timeout triggered")) {
            return HttpErrorType.IDLE_TIMEOUT_TRIGGERED;
        }

        switch (errorMessage) {
            case REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE:
                return HttpErrorType.INIT_INBOUND_RESPONSE_FAILED;
            case REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE_HEADERS:
                return HttpErrorType.READING_INBOUND_RESPONSE_HEADERS_FAILED;
            case REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE_BODY:
                return HttpErrorType.READING_INBOUND_RESPONSE_BODY_FAILED;
            case REMOTE_SERVER_CLOSED_BEFORE_INITIATING_OUTBOUND_REQUEST:
                return HttpErrorType.INIT_OUTBOUND_REQUEST_FAILED;
            case REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_HEADERS:
                return HttpErrorType.WRITING_OUTBOUND_REQUEST_HEADER_FAILED;
            case REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_BODY:
                return HttpErrorType.WRITING_OUTBOUND_REQUEST_BODY_FAILED;
            case REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_INBOUND_REQUEST:
                return HttpErrorType.INIT_INBOUND_REQUEST_FAILED;
            case REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_HEADERS:
                return HttpErrorType.READING_INBOUND_REQUEST_HEADER_FAILED;
            case REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_BODY:
                return HttpErrorType.READING_INBOUND_REQUEST_BODY_FAILED;
            case REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_OUTBOUND_RESPONSE:
                return HttpErrorType.INIT_OUTBOUND_RESPONSE_FAILED;
            case REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_HEADERS:
                return HttpErrorType.WRITING_OUTBOUND_RESPONSE_HEADERS_FAILED;
            case REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY:
                return HttpErrorType.WRITING_OUTBOUND_RESPONSE_BODY_FAILED;
            case REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_100_CONTINUE_RESPONSE:
                return HttpErrorType.INIT_100_CONTINUE_RESPONSE_FAILED;
            case REMOTE_CLIENT_CLOSED_WHILE_WRITING_100_CONTINUE_RESPONSE:
                return HttpErrorType.WRITING_100_CONTINUE_RESPONSE_FAILED;
            case PROMISED_STREAM_REJECTED_ERROR:
                return HttpErrorType.HTTP2_CLIENT_ERROR;
            default:
                return HttpErrorType.GENERIC_CLIENT_ERROR;
        }
    }

    private static ErrorValue createErrorCause(String message, String errorTypeId, BPackage packageName) {
        return BallerinaErrors.createDistinctError(errorTypeId, packageName, message);
    }

    public static HttpCarbonMessage getCarbonMsg(ObjectValue objectValue, HttpCarbonMessage defaultMsg) {
        HttpCarbonMessage httpCarbonMessage = (HttpCarbonMessage) objectValue.getNativeData(TRANSPORT_MESSAGE);
        if (httpCarbonMessage != null) {
            return httpCarbonMessage;
        }
        addCarbonMsg(objectValue, defaultMsg);
        return defaultMsg;
    }

    /**
     * Gets the {@code Http2PushPromise} represented by the PushPromise object.
     *
     * @param pushPromiseObj  the push promise object
     * @param defaultPushPromise the Http2PushPromise to use if the object does not have native data of a push promise
     * @return the {@code Http2PushPromise} represented by the PushPromise object
     */
    public static Http2PushPromise getPushPromise(ObjectValue pushPromiseObj, Http2PushPromise defaultPushPromise) {
        Http2PushPromise pushPromise =
                (Http2PushPromise) pushPromiseObj.getNativeData(HttpConstants.TRANSPORT_PUSH_PROMISE);
        if (pushPromise != null) {
            return pushPromise;
        }
        pushPromiseObj.addNativeData(HttpConstants.TRANSPORT_PUSH_PROMISE, defaultPushPromise);
        return defaultPushPromise;
    }

    /**
     * Populates the push promise object from native {@code Http2PushPromise}.
     *  @param pushPromiseObj the push promise object
     * @param pushPromise the native Http2PushPromise
     */
    public static void populatePushPromiseStruct(ObjectValue pushPromiseObj,
                                                 Http2PushPromise pushPromise) {
        pushPromiseObj.addNativeData(HttpConstants.TRANSPORT_PUSH_PROMISE, pushPromise);
        pushPromiseObj.set(HttpConstants.PUSH_PROMISE_PATH_FIELD, org.ballerinalang.jvm.StringUtils
                .fromString(pushPromise.getPath()));
        pushPromiseObj.set(HttpConstants.PUSH_PROMISE_METHOD_FIELD, org.ballerinalang.jvm.StringUtils
                .fromString(pushPromise.getMethod()));
    }

    /**
     * Creates native {@code Http2PushPromise} from PushPromise object.
     *
     * @param pushPromiseObj the PushPromise object
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

    public static void addCarbonMsg(ObjectValue struct, HttpCarbonMessage httpCarbonMessage) {
        struct.addNativeData(TRANSPORT_MESSAGE, httpCarbonMessage);
    }

    public static void populateInboundRequest(ObjectValue inboundRequest, ObjectValue entity,
                                              HttpCarbonMessage inboundRequestMsg) {
        inboundRequest.addNativeData(TRANSPORT_MESSAGE, inboundRequestMsg);
        inboundRequest.addNativeData(REQUEST, true);

        MapValue<BString, Object> mutualSslRecord = ValueCreatorUtils.createHTTPRecordValue(
                MUTUAL_SSL_HANDSHAKE_RECORD);
        mutualSslRecord.put(REQUEST_MUTUAL_SSL_HANDSHAKE_STATUS,
                            org.ballerinalang.jvm.StringUtils.fromString(
                                    (String) inboundRequestMsg.getProperty(HttpConstants.MUTUAL_SSL_RESULT)));
        mutualSslRecord.put(MUTUAL_SSL_CERTIFICATE, org.ballerinalang.jvm.StringUtils
                .fromString((String) inboundRequestMsg.getProperty(HttpConstants.BASE_64_ENCODED_CERT)));
        inboundRequest.set(REQUEST_MUTUAL_SSL_HANDSHAKE_FIELD, mutualSslRecord);

        enrichWithInboundRequestInfo(inboundRequest, inboundRequestMsg);
        enrichWithInboundRequestHeaders(inboundRequest, inboundRequestMsg);

        populateEntity(entity, inboundRequestMsg);
        inboundRequest.set(REQUEST_ENTITY_FIELD, entity);
        inboundRequest.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, false);

        String cacheControlHeader = inboundRequestMsg.getHeader(CACHE_CONTROL.toString());
        if (cacheControlHeader != null) {
            ObjectValue cacheControlObj = ValueCreatorUtils.createRequestCacheControlObject();
            RequestCacheControlObj requestCacheControl = new RequestCacheControlObj(cacheControlObj);
            requestCacheControl.populateStruct(cacheControlHeader);
            inboundRequest.set(REQUEST_CACHE_CONTROL_FIELD, requestCacheControl.getObj());
        }
    }

    private static void enrichWithInboundRequestHeaders(ObjectValue inboundRequestObj,
                                                        HttpCarbonMessage inboundRequestMsg) {
        if (inboundRequestMsg.getHeader(HttpHeaderNames.USER_AGENT.toString()) != null) {
            BString agent = org.ballerinalang.jvm.StringUtils.fromString(
                    inboundRequestMsg.getHeader(HttpHeaderNames.USER_AGENT.toString()));
            inboundRequestObj.set(HttpConstants.REQUEST_USER_AGENT_FIELD, agent);
            inboundRequestMsg.removeHeader(HttpHeaderNames.USER_AGENT.toString());
        }
    }

    private static void enrichWithInboundRequestInfo(ObjectValue inboundRequestObj,
                                                     HttpCarbonMessage inboundRequestMsg) {
        inboundRequestObj.set(HttpConstants.REQUEST_RAW_PATH_FIELD,
                              org.ballerinalang.jvm.StringUtils.fromString(inboundRequestMsg.getRequestUrl()));
        inboundRequestObj.set(HttpConstants.REQUEST_METHOD_FIELD,
                              org.ballerinalang.jvm.StringUtils.fromString(inboundRequestMsg.getHttpMethod()));
        inboundRequestObj.set(HttpConstants.REQUEST_VERSION_FIELD,
                              org.ballerinalang.jvm.StringUtils.fromString(inboundRequestMsg.getHttpVersion()));
        HttpResourceArguments resourceArgValues = (HttpResourceArguments) inboundRequestMsg.getProperty(
                HttpConstants.RESOURCE_ARGS);
        if (resourceArgValues != null && resourceArgValues.getMap().get(HttpConstants.EXTRA_PATH_INFO) != null) {
            inboundRequestObj.set(
                    HttpConstants.REQUEST_EXTRA_PATH_INFO_FIELD, org.ballerinalang.jvm.StringUtils.fromString(
                            resourceArgValues.getMap().get(HttpConstants.EXTRA_PATH_INFO)));
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
        caller.set(HttpConstants.HTTP_CONNECTOR_CONFIG_FIELD, config);
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
        MapValue<BString, Object> remote = ValueCreatorUtils.createHTTPRecordValue(HttpConstants.REMOTE);
        MapValue<BString, Object> local = ValueCreatorUtils.createHTTPRecordValue(HttpConstants.LOCAL);

        Object remoteSocketAddress = inboundMsg.getProperty(HttpConstants.REMOTE_ADDRESS);
        if (remoteSocketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteSocketAddress;
            BString remoteHost = org.ballerinalang.jvm.StringUtils.fromString(inetSocketAddress.getHostString());
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
            local.put(HttpConstants.LOCAL_HOST_FIELD, org.ballerinalang.jvm.StringUtils.fromString(localHost));
            local.put(HttpConstants.LOCAL_PORT_FIELD, localPort);
        }
        httpCaller.set(HttpConstants.LOCAL_STRUCT_INDEX, local);
        httpCaller.set(HttpConstants.SERVICE_ENDPOINT_PROTOCOL_FIELD, org.ballerinalang.jvm.StringUtils
                .fromString((String) inboundMsg.getProperty(HttpConstants.PROTOCOL)));
        httpCaller.set(HttpConstants.SERVICE_ENDPOINT_CONFIG_FIELD, config);
        httpCaller.addNativeData(HttpConstants.HTTP_SERVICE, httpResource.getParentService());
        httpCaller.addNativeData(HttpConstants.REMOTE_SOCKET_ADDRESS, remoteSocketAddress);
    }

    /**
     * Populate inbound response with headers and entity.
     * @param inboundResponse  Ballerina struct to represent response
     * @param entity    Entity of the response
     * @param inboundResponseMsg      Represent carbon message.
     */
    public static void populateInboundResponse(ObjectValue inboundResponse, ObjectValue entity,
                                               HttpCarbonMessage inboundResponseMsg) {
        inboundResponse.addNativeData(TRANSPORT_MESSAGE, inboundResponseMsg);
        int statusCode = inboundResponseMsg.getHttpStatusCode();
        inboundResponse.set(RESPONSE_STATUS_CODE_FIELD, (long) statusCode);
        inboundResponse.set(RESPONSE_REASON_PHRASE_FIELD, org.ballerinalang.jvm.StringUtils
                .fromString(HttpResponseStatus.valueOf(statusCode).reasonPhrase()));

        if (inboundResponseMsg.getHeader(HttpHeaderNames.SERVER.toString()) != null) {
            inboundResponse.set(HttpConstants.RESPONSE_SERVER_FIELD, org.ballerinalang.jvm.StringUtils
                    .fromString(inboundResponseMsg.getHeader(HttpHeaderNames.SERVER.toString())));
            inboundResponseMsg.removeHeader(HttpHeaderNames.SERVER.toString());
        }

        if (inboundResponseMsg.getProperty(RESOLVED_REQUESTED_URI) != null) {
            inboundResponse.set(RESOLVED_REQUESTED_URI_FIELD, org.ballerinalang.jvm.StringUtils
                    .fromString(inboundResponseMsg.getProperty(RESOLVED_REQUESTED_URI).toString()));
        }

        String cacheControlHeader = inboundResponseMsg.getHeader(CACHE_CONTROL.toString());
        if (cacheControlHeader != null) {
            ResponseCacheControlObj responseCacheControl = new ResponseCacheControlObj(PROTOCOL_HTTP_PKG_ID,
                    RESPONSE_CACHE_CONTROL);
            responseCacheControl.populateStruct(cacheControlHeader);
            inboundResponse.set(RESPONSE_CACHE_CONTROL_FIELD, responseCacheControl.getObj());
        }

        populateEntity(entity, inboundResponseMsg);
        inboundResponse.set(RESPONSE_ENTITY_FIELD, entity);
        inboundResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, false);
    }

    /**
     * Populate entity with headers, content-type and content-length.
     *
     * @param entity Represent an entity struct
     * @param cMsg   Represent a carbon message
     */
    private static void populateEntity(ObjectValue entity, HttpCarbonMessage cMsg) {
        long contentLength = -1;
        String lengthStr = cMsg.getHeader(HttpHeaderNames.CONTENT_LENGTH.toString());
        try {
            contentLength = lengthStr != null ? Long.parseLong(lengthStr) : contentLength;
            MimeUtil.setContentLength(entity, contentLength);
        } catch (NumberFormatException e) {
            throw createHttpError("Invalid content length", HttpErrorType.INVALID_CONTENT_LENGTH);
        }
        entity.addNativeData(ENTITY_HEADERS, cMsg.getHeaders());
        entity.addNativeData(ENTITY_TRAILER_HEADERS, cMsg.getTrailerHeaders());
    }

    /**
     * Set headers and properties of request/response object to the outbound transport message.
     *
     * @param outboundMsg    transport Http carbon message.
     * @param outboundMsgObj req/resp object.
     */
    public static void enrichOutboundMessage(HttpCarbonMessage outboundMsg, ObjectValue outboundMsgObj) {
        setHeadersToTransportMessage(outboundMsg, outboundMsgObj);
        setPropertiesToTransportMessage(outboundMsg, outboundMsgObj);
    }

    private static void setHeadersToTransportMessage(HttpCarbonMessage outboundMsg, ObjectValue messageObj) {
        boolean request = isRequest(messageObj);
        ObjectValue entityObj = (ObjectValue) messageObj
                .get(request ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD);
        HttpHeaders transportHeaders = outboundMsg.getHeaders();
        if (request || isResponse(messageObj)) {
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
        if (!request) {
            HttpHeaders transportTrailingHeaders = outboundMsg.getTrailerHeaders();
            HttpHeaders trailingHeaders = (HttpHeaders) entityObj.getNativeData(ENTITY_TRAILER_HEADERS);
            if (trailingHeaders != null && trailingHeaders != transportTrailingHeaders) {
                transportTrailingHeaders.add(trailingHeaders);
            }
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
     * @param value  request/response object.
     * @return true if the message entity data source is available else false.
     */
    public static boolean isEntityDataSourceAvailable(ObjectValue value) {
        ObjectValue entityObj = (ObjectValue) value
                .get(isRequest(value) ? REQUEST_ENTITY_FIELD : RESPONSE_ENTITY_FIELD);
        return (entityObj != null && EntityBodyHandler.getMessageDataSource(entityObj) != null);
    }

    private static void setCompressionHeaders(MapValue<BString, Object> compressionConfig, HttpCarbonMessage requestMsg,
                                              HttpCarbonMessage outboundResponseMsg) {
        if (!checkConfigAnnotationAvailability(compressionConfig)) {
            return;
        }
        String contentEncoding = outboundResponseMsg.getHeaders().get(HttpHeaderNames.CONTENT_ENCODING);
        if (contentEncoding != null) {
            return;
        }
        CompressionConfigState compressionState = getCompressionState(
                compressionConfig.getStringValue(ANN_CONFIG_ATTR_COMPRESSION_ENABLE).getValue());
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
            throw createHttpError("operation not allowed:invalid Connection variable",
                    HttpErrorType.GENERIC_LISTENER_ERROR);
        }
    }

    private static boolean is100ContinueRequest(HttpCarbonMessage reqMsg, int statusCode) {
        return HttpConstants.HEADER_VAL_100_CONTINUE.equalsIgnoreCase(
                reqMsg.getHeader(HttpHeaderNames.EXPECT.toString())) || statusCode == 100;
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
        Optional<ObserverContext> observerContext = ObserveUtils.getObserverContextOfCurrentFrame(strand);
        observerContext.ifPresent(ctx -> {
            HttpUtil.injectHeaders(message, ObserveUtils.getContextProperties(strand.observerContext));
            strand.observerContext.addTag(TAG_KEY_HTTP_METHOD, message.getHttpMethod());
            strand.observerContext.addTag(TAG_KEY_HTTP_URL, String.valueOf(message.getProperty(HttpConstants.TO)));
            strand.observerContext.addTag(TAG_KEY_PEER_ADDRESS,
                       message.getProperty(PROPERTY_HTTP_HOST) + ":" + message.getProperty(PROPERTY_HTTP_PORT));
            // Add HTTP Status Code tag. The HTTP status code will be set using the response message.
            // Sometimes the HTTP status code will not be set due to errors etc. Therefore, it's very important to set
            // some value to HTTP Status Code to make sure that tags will not change depending on various
            // circumstances.
            // HTTP Status code must be a number.
            strand.observerContext.addTag(TAG_KEY_HTTP_STATUS_CODE, Integer.toString(0));
        });
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
        ObjectValue responseObj = ValueCreatorUtils.createResponseObject();
        ObjectValue entity = ValueCreatorUtils.createEntityObject();

        HttpUtil.populateInboundResponse(responseObj, entity, httpCarbonMessage);
        return responseObj;
    }

    public static void populateSenderConfigurations(SenderConfiguration senderConfiguration,
            MapValue<BString, Object> clientEndpointConfig, String scheme) {
        ProxyServerConfiguration proxyServerConfiguration;
        MapValue secureSocket = clientEndpointConfig.getMapValue(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET);
        String httpVersion = clientEndpointConfig.getStringValue(HttpConstants.CLIENT_EP_HTTP_VERSION).getValue();
        if (secureSocket != null) {
            HttpUtil.populateSSLConfiguration(senderConfiguration, secureSocket);
        } else if (scheme.equals(PROTOCOL_HTTPS)) {
            if (httpVersion.equals(HTTP_2_0_VERSION)) {
                throw createHttpError("To enable https you need to configure secureSocket record",
                        HttpErrorType.SSL_ERROR);
            } else {
                senderConfiguration.useJavaDefaults();
            }
        }
        if (HTTP_1_1_VERSION.equals(httpVersion)) {
            MapValue<BString, Object> http1Settings = (MapValue<BString, Object>) clientEndpointConfig
                    .get(HttpConstants.HTTP1_SETTINGS);
            MapValue proxy = http1Settings.getMapValue(HttpConstants.PROXY_STRUCT_REFERENCE);
            if (proxy != null) {
                String proxyHost = proxy.getStringValue(HttpConstants.PROXY_HOST).getValue();
                int proxyPort = proxy.getIntValue(HttpConstants.PROXY_PORT).intValue();
                String proxyUserName = proxy.getStringValue(HttpConstants.PROXY_USERNAME).getValue();
                String proxyPassword = proxy.getStringValue(HttpConstants.PROXY_PASSWORD).getValue();
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
        }
        long timeoutMillis = clientEndpointConfig.getIntValue(HttpConstants.CLIENT_EP_ENDPOINT_TIMEOUT);
        if (timeoutMillis < 0) {
            senderConfiguration.setSocketIdleTimeout(0);
        } else {
            senderConfiguration.setSocketIdleTimeout(
                    validateConfig(timeoutMillis, HttpConstants.CLIENT_EP_ENDPOINT_TIMEOUT.getValue()));
        }
        if (httpVersion != null) {
            senderConfiguration.setHttpVersion(httpVersion);
        }
        String forwardedExtension = clientEndpointConfig.getStringValue(HttpConstants.CLIENT_EP_FORWARDED).getValue();
        senderConfiguration.setForwardedExtensionConfig(HttpUtil.getForwardedExtensionConfig(forwardedExtension));
    }

    public static ConnectionManager getConnectionManager(MapValue<BString, Long> poolStruct) {
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

    public static void populatePoolingConfig(MapValue<BString, Long> poolRecord, PoolConfiguration poolConfiguration) {
        long maxActiveConnections = poolRecord.get(HttpConstants.CONNECTION_POOLING_MAX_ACTIVE_CONNECTIONS);
        poolConfiguration.setMaxActivePerPool(
                validateConfig(maxActiveConnections,
                               HttpConstants.CONNECTION_POOLING_MAX_ACTIVE_CONNECTIONS.getValue()));

        long maxIdleConnections = poolRecord.get(HttpConstants.CONNECTION_POOLING_MAX_IDLE_CONNECTIONS);
        poolConfiguration.setMaxIdlePerPool(
                validateConfig(maxIdleConnections, HttpConstants.CONNECTION_POOLING_MAX_IDLE_CONNECTIONS.getValue()));

        long waitTime = poolRecord.get(HttpConstants.CONNECTION_POOLING_WAIT_TIME);
        poolConfiguration.setMaxWaitTime(waitTime);

        long maxActiveStreamsPerConnection = poolRecord.get(CONNECTION_POOLING_MAX_ACTIVE_STREAMS_PER_CONNECTION);
        poolConfiguration.setHttp2MaxActiveStreamsPerConnection(
                maxActiveStreamsPerConnection == -1 ? Integer.MAX_VALUE : validateConfig(
                        maxActiveStreamsPerConnection,
                        CONNECTION_POOLING_MAX_ACTIVE_STREAMS_PER_CONNECTION.getValue()));
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
        String keyFile = secureSocket.getStringValue(ENDPOINT_CONFIG_KEY).getValue();
        String certFile = secureSocket.getStringValue(ENDPOINT_CONFIG_CERTIFICATE).getValue();
        String trustCerts = secureSocket.getStringValue(ENDPOINT_CONFIG_TRUST_CERTIFICATES).getValue();
        String keyPassword = secureSocket.getStringValue(ENDPOINT_CONFIG_KEY_PASSWORD).getValue();
        boolean disableSslValidation = secureSocket.getBooleanValue(ENDPOINT_CONFIG_DISABLE_SSL);
        List<Parameter> clientParams = new ArrayList<>();
        if (disableSslValidation) {
            sslConfiguration.disableSsl();
            return;
        } else if (StringUtils.isEmpty(trustCerts) && trustStore == null) {
            sslConfiguration.useJavaDefaults();
            return;
        }
        if (trustStore != null && StringUtils.isNotBlank(trustCerts)) {
            throw createHttpError("Cannot configure both trustStore and trustCerts at the same time.",
                    HttpErrorType.SSL_ERROR);
        }
        if (trustStore != null) {
            String trustStoreFile = trustStore.getStringValue(FILE_PATH).getValue();
            if (StringUtils.isNotBlank(trustStoreFile)) {
                sslConfiguration.setTrustStoreFile(trustStoreFile);
            }
            String trustStorePassword = trustStore.getStringValue(PASSWORD).getValue();
            if (StringUtils.isNotBlank(trustStorePassword)) {
                sslConfiguration.setTrustStorePass(trustStorePassword);
            }
        } else if (StringUtils.isNotBlank(trustCerts)) {
            sslConfiguration.setClientTrustCertificates(trustCerts);
        }
        if (keyStore != null && StringUtils.isNotBlank(keyFile)) {
            throw createHttpError("Cannot configure both keyStore and keyFile.", HttpErrorType.SSL_ERROR);
        } else if (StringUtils.isNotBlank(keyFile) && StringUtils.isBlank(certFile)) {
            throw createHttpError("Need to configure certFile containing client ssl certificates.",
                    HttpErrorType.SSL_ERROR);
        }
        if (keyStore != null) {
            String keyStoreFile = keyStore.getStringValue(FILE_PATH).getValue();
            if (StringUtils.isNotBlank(keyStoreFile)) {
                sslConfiguration.setKeyStoreFile(keyStoreFile);
            }
            String keyStorePassword = keyStore.getStringValue(PASSWORD).getValue();
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
            List<String> sslEnabledProtocolsValueList = Arrays.asList(
                    protocols.getArrayValue(ENABLED_PROTOCOLS).getStringArray());
            if (!sslEnabledProtocolsValueList.isEmpty()) {
                String sslEnabledProtocols = sslEnabledProtocolsValueList.stream()
                        .collect(Collectors.joining(",", "", ""));
                Parameter clientProtocols = new Parameter(ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS, sslEnabledProtocols);
                clientParams.add(clientProtocols);
            }

            String sslProtocol = protocols.getStringValue(SSL_PROTOCOL_VERSION).getValue();
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

        Object[] cipherConfigs = secureSocket.getArrayValue(HttpConstants.SSL_CONFIG_CIPHERS).getStringArray();
        if (cipherConfigs != null) {
            List<Object> ciphersValueList = Arrays.asList(cipherConfigs);
            if (ciphersValueList.size() > 0) {
                String ciphers = ciphersValueList.stream().map(Object::toString)
                        .collect(Collectors.joining(",", "", ""));
                Parameter clientCiphers = new Parameter(HttpConstants.CIPHERS, ciphers);
                clientParams.add(clientCiphers);
            }
        }
        String enableSessionCreation = String.valueOf(
                secureSocket.getBooleanValue(HttpConstants.SSL_CONFIG_ENABLE_SESSION_CREATION));
        Parameter clientEnableSessionCreation = new Parameter(
                HttpConstants.SSL_CONFIG_ENABLE_SESSION_CREATION.getValue(), enableSessionCreation);
        clientParams.add(clientEnableSessionCreation);
        if (!clientParams.isEmpty()) {
            sslConfiguration.setParameters(clientParams);
        }
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
            throw createHttpError("error occurred while serializing null data");
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
        } else if (value instanceof BString) {
            outputStream.write(((BString) value).getValue().getBytes(Charset.defaultCharset()));
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
        String host = endpointConfig.getStringValue(HttpConstants.ENDPOINT_CONFIG_HOST).getValue();
        MapValue sslConfig = endpointConfig.getMapValue(HttpConstants.ENDPOINT_CONFIG_SECURE_SOCKET);
        String httpVersion = endpointConfig.getStringValue(HttpConstants.ENDPOINT_CONFIG_VERSION).getValue();
        MapValue<BString, Object> http1Settings;
        long idleTimeout = endpointConfig.getIntValue(HttpConstants.ENDPOINT_CONFIG_TIMEOUT);

        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        if (HTTP_1_1_VERSION.equals(httpVersion)) {
            http1Settings = (MapValue<BString, Object>) endpointConfig.get(HttpConstants.HTTP1_SETTINGS);
            listenerConfiguration.setPipeliningLimit(http1Settings.getIntValue(HttpConstants.PIPELINING_REQUEST_LIMIT));
            String keepAlive = http1Settings.getStringValue(HttpConstants.ENDPOINT_CONFIG_KEEP_ALIVE).getValue();
            listenerConfiguration.setKeepAliveConfig(HttpUtil.getKeepAliveConfig(keepAlive));
            // Set Request validation limits.
            setRequestSizeValidationConfig(http1Settings, listenerConfiguration);
        }

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

        if (idleTimeout < 0) {
            throw new BallerinaConnectorException("Idle timeout cannot be negative. If you want to disable the " +
                    "timeout please use value 0");
        }
        listenerConfiguration.setSocketIdleTimeout(Math.toIntExact(idleTimeout));

        // Set HTTP version
        if (httpVersion != null) {
            listenerConfiguration.setVersion(httpVersion);
        }

        if (endpointConfig.getType().getName().equalsIgnoreCase(LISTENER_CONFIGURATION)) {
            BString serverName = endpointConfig.getStringValue(SERVER_NAME);
            listenerConfiguration.setServerHeader(serverName != null ? serverName.getValue() : getServerName());
        } else {
            listenerConfiguration.setServerHeader(getServerName());
        }

        if (sslConfig != null) {
            return setSslConfig(sslConfig, listenerConfiguration);
        }

        listenerConfiguration.setPipeliningEnabled(true); //Pipelining is enabled all the time
        Object webSocketCompressionEnabled = endpointConfig.get(WebSocketConstants.COMPRESSION_ENABLED_CONFIG);
        if (webSocketCompressionEnabled != null) {
            listenerConfiguration.setWebSocketCompressionEnabled((Boolean) webSocketCompressionEnabled);
        }

        return listenerConfiguration;
    }

    private static void setRequestSizeValidationConfig(MapValue http1Settings,
                                                     ListenerConfiguration listenerConfiguration) {
        long maxUriLength = http1Settings.getIntValue(HttpConstants.REQUEST_LIMITS_MAXIMUM_URL_LENGTH);
        long maxHeaderSize = http1Settings.getIntValue(HttpConstants.REQUEST_LIMITS_MAXIMUM_HEADER_SIZE);
        long maxEntityBodySize = http1Settings.getIntValue(HttpConstants.REQUEST_LIMITS_MAXIMUM_ENTITY_BODY_SIZE);
        RequestSizeValidationConfig requestSizeValidationConfig = listenerConfiguration
                .getRequestSizeValidationConfig();

        if (maxUriLength >= 0) {
            requestSizeValidationConfig.setMaxUriLength(Math.toIntExact(maxUriLength));
        } else {
            throw new BallerinaConnectorException("Invalid configuration found for maxUriLength : " + maxUriLength);
        }

        if (maxHeaderSize >= 0) {
            requestSizeValidationConfig.setMaxHeaderSize(Math.toIntExact(maxHeaderSize));
        } else {
            throw new BallerinaConnectorException("Invalid configuration found for maxHeaderSize : " + maxHeaderSize);
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
        String keyFile = sslConfig.getStringValue(ENDPOINT_CONFIG_KEY).getValue();
        String certFile = sslConfig.getStringValue(ENDPOINT_CONFIG_CERTIFICATE).getValue();
        String trustCerts = sslConfig.getStringValue(ENDPOINT_CONFIG_TRUST_CERTIFICATES).getValue();
        String keyPassword = sslConfig.getStringValue(ENDPOINT_CONFIG_KEY_PASSWORD).getValue();

        if (keyStore != null && StringUtils.isNotBlank(keyFile)) {
            throw createHttpError("Cannot configure both keyStore and keyFile at the same time.",
                                  HttpErrorType.SSL_ERROR);
        } else if (keyStore == null && (StringUtils.isBlank(keyFile) || StringUtils.isBlank(certFile))) {
            throw createHttpError("Either keystore or certificateKey and server certificates must be provided "
                                          + "for secure connection", HttpErrorType.SSL_ERROR);
        }
        if (keyStore != null) {
            String keyStoreFile = keyStore.getStringValue(FILE_PATH).getValue();
            if (StringUtils.isBlank(keyStoreFile)) {
                throw createHttpError("Keystore file location must be provided for secure connection.",
                        HttpErrorType.SSL_ERROR);
            }
            String keyStorePassword = keyStore.getStringValue(PASSWORD).getValue();
            if (StringUtils.isBlank(keyStorePassword)) {
                throw createHttpError("Keystore password must be provided for secure connection",
                        HttpErrorType.SSL_ERROR);
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
        String sslVerifyClient = sslConfig.getStringValue(SSL_CONFIG_SSL_VERIFY_CLIENT).getValue();
        listenerConfiguration.setVerifyClient(sslVerifyClient);
        listenerConfiguration
                .setSslSessionTimeOut((int) sslConfig.getDefaultableIntValue(ENDPOINT_CONFIG_SESSION_TIMEOUT));
        listenerConfiguration
                .setSslHandshakeTimeOut(sslConfig.getDefaultableIntValue(ENDPOINT_CONFIG_HANDSHAKE_TIMEOUT));
        if (trustStore == null && StringUtils.isNotBlank(sslVerifyClient) && StringUtils.isBlank(trustCerts)) {
            throw createHttpError("Truststore location or trustCertificates must be provided to enable Mutual SSL",
                    HttpErrorType.SSL_ERROR);
        }
        if (trustStore != null) {
            String trustStoreFile = trustStore.getStringValue(FILE_PATH).getValue();
            String trustStorePassword = trustStore.getStringValue(PASSWORD).getValue();
            if (StringUtils.isBlank(trustStoreFile) && StringUtils.isNotBlank(sslVerifyClient)) {
                throw createHttpError("Truststore location must be provided to enable Mutual SSL",
                                      HttpErrorType.SSL_ERROR);
            }
            if (StringUtils.isBlank(trustStorePassword) && StringUtils.isNotBlank(sslVerifyClient)) {
                throw createHttpError("Truststore password value must be provided to enable Mutual SSL",
                                      HttpErrorType.SSL_ERROR);
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

            String sslProtocol = protocols.getStringValue(SSL_PROTOCOL_VERSION).getValue();
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
        Parameter enableSessionCreationParam = new Parameter(SSL_CONFIG_ENABLE_SESSION_CREATION.getValue(),
                                                             serverEnableSessionCreation);
        serverParamList.add(enableSessionCreationParam);
        if (!serverParamList.isEmpty()) {
            listenerConfiguration.setParameters(serverParamList);
        }

        listenerConfiguration
                .setId(HttpUtil.getListenerInterface(listenerConfiguration.getHost(), listenerConfiguration.getPort()));

        return listenerConfiguration;
    }

    public static String getServiceName(ObjectValue balService) {
        String serviceTypeName = balService.getType().getName();
        int serviceIndex = serviceTypeName.lastIndexOf("$$service$");
        return serviceTypeName.substring(0, serviceIndex);
    }

    private HttpUtil() {
    }
}
