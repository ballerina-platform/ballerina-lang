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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDecoder;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.caching.RequestCacheControlStruct;
import org.ballerinalang.net.http.caching.ResponseCacheControlStruct;
import org.ballerinalang.net.http.session.Session;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.observability.ObservabilityUtils;
import org.ballerinalang.util.observability.ObserverContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.config.ForwardedExtensionConfig;
import org.wso2.transport.http.netty.config.KeepAliveConfig;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.Parameter;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpWsConnectorFactory;
import org.wso2.transport.http.netty.contractimpl.DefaultHttpWsConnectorFactory;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.CACHE_CONTROL;
import static org.ballerinalang.bre.bvm.BLangVMErrors.PACKAGE_BUILTIN;
import static org.ballerinalang.bre.bvm.BLangVMErrors.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.mime.util.Constants.BOUNDARY;
import static org.ballerinalang.mime.util.Constants.BYTE_LIMIT;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.Constants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.MULTIPART_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.Constants.NO_CONTENT_LENGTH_FOUND;
import static org.ballerinalang.mime.util.Constants.OCTET_STREAM;
import static org.ballerinalang.mime.util.EntityBodyHandler.checkEntityBodyAvailability;
import static org.ballerinalang.mime.util.EntityBodyHandler.createNewEntity;
import static org.ballerinalang.mime.util.EntityBodyHandler.setDiscreteMediaTypeBodyContent;
import static org.ballerinalang.net.http.HttpConstants.ALWAYS;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_CHUNKING;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_COMPRESSION;
import static org.ballerinalang.net.http.HttpConstants.ENTITY_INDEX;
import static org.ballerinalang.net.http.HttpConstants.HTTP_MESSAGE_INDEX;
import static org.ballerinalang.net.http.HttpConstants.HTTP_STATUS_CODE;
import static org.ballerinalang.net.http.HttpConstants.NEVER;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;
import static org.ballerinalang.net.http.HttpConstants.REQUEST_CACHE_CONTROL_INDEX;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_CACHE_CONTROL_INDEX;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_REASON_PHRASE_INDEX;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_STATUS_CODE_INDEX;
import static org.ballerinalang.net.http.HttpConstants.TRANSPORT_MESSAGE;
import static org.ballerinalang.util.observability.ObservabilityConstants.PROPERTY_HTTP_HOST;
import static org.ballerinalang.util.observability.ObservabilityConstants.PROPERTY_HTTP_PORT;
import static org.ballerinalang.util.observability.ObservabilityConstants.TAG_KEY_HTTP_METHOD;
import static org.ballerinalang.util.observability.ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE;
import static org.ballerinalang.util.observability.ObservabilityConstants.TAG_KEY_HTTP_URL;
import static org.ballerinalang.util.observability.ObservabilityConstants.TAG_KEY_PEER_ADDRESS;
import static org.wso2.transport.http.netty.common.Constants.ENCODING_GZIP;
import static org.wso2.transport.http.netty.common.Constants.HTTP_TRANSFER_ENCODING_IDENTITY;

/**
 * Utility class providing utility methods.
 */
public class HttpUtil {

    public static final int TRUE = 1;
    public static final int FALSE = 0;

    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    private static final String METHOD_ACCESSED = "isMethodAccessed";
    private static final String IO_EXCEPTION_OCCURED = "I/O exception occurred";
    private static final String CHUNKING_CONFIG = "chunking_config";

    public static BValue[] getProperty(Context context, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) context.getRefArgument(0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
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
        BStruct httpMessageStruct = (BStruct) context.getRefArgument(0);
        String propertyName = context.getStringArgument(0);
        String propertyValue = context.getStringArgument(1);

        if (propertyName != null && propertyValue != null) {
            HTTPCarbonMessage httpCarbonMessage = HttpUtil
                    .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
            httpCarbonMessage.setProperty(propertyName, propertyValue);
        }
    }

    /**
     * Set the given entity to request or response message.
     *
     * @param context   Ballerina context
     * @param isRequest boolean representing whether the message is a request or a response
     */
    public static void setEntity(Context context, boolean isRequest) {
        BStruct httpMessageStruct = (BStruct) context.getRefArgument(HTTP_MESSAGE_INDEX);

        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        BStruct entity = (BStruct) context.getRefArgument(ENTITY_INDEX);
        String contentType = MimeUtil.getContentTypeWithParameters(entity);
        if (checkEntityBodyAvailability(entity)) {
            httpCarbonMessage.waitAndReleaseAllEntities();
            if (contentType == null) {
                contentType = OCTET_STREAM;
            }
            HeaderUtil.setHeaderToEntity(entity, HttpHeaderNames.CONTENT_TYPE.toString(), contentType);
        }
        httpMessageStruct.addNativeData(MESSAGE_ENTITY, entity);
        httpMessageStruct.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, checkEntityBodyAvailability(entity));
    }

    /**
     * Get the entity from request or response.
     *
     * @param context                Ballerina context
     * @param isRequest              boolean representing whether the message is a request or a response
     * @param isEntityBodyRequired   boolean representing whether the entity body is required
     * @return Entity of the request or response
     */
    public static BValue[] getEntity(Context context, boolean isRequest, boolean isEntityBodyRequired) {
        try {
            BStruct httpMessageStruct = (BStruct) context.getRefArgument(HTTP_MESSAGE_INDEX);
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
            return new BValue[]{entity};
        } catch (Throwable throwable) {
            return new BValue[]{MimeUtil.createEntityError(context,
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
    public static void populateEntityBody(Context context, BStruct httpMessageStruct, BStruct entity,
                                          boolean isRequest) {
        HTTPCarbonMessage httpCarbonMessage = HttpUtil
                .getCarbonMsg(httpMessageStruct, HttpUtil.createHttpCarbonMessage(isRequest));
        HttpMessageDataStreamer httpMessageDataStreamer = new HttpMessageDataStreamer(httpCarbonMessage);
        String contentType = httpCarbonMessage.getHeader(HttpHeaderNames.CONTENT_TYPE.toString());
        if (MimeUtil.isNotNullAndEmpty(contentType) && contentType.startsWith(MULTIPART_AS_PRIMARY_TYPE)
                && context != null) {
            MultipartDecoder.parseBody(context, entity, contentType, httpMessageDataStreamer.getInputStream());
        } else {
            int contentLength = NO_CONTENT_LENGTH_FOUND;
            String lengthStr = httpCarbonMessage.getHeader(HttpHeaderNames.CONTENT_LENGTH.toString());
            try {
                contentLength = lengthStr != null ? Integer.parseInt(lengthStr) : contentLength;
                if (contentLength == NO_CONTENT_LENGTH_FOUND) {
                    contentLength = httpCarbonMessage.countMessageLengthTill(BYTE_LIMIT);
                }
            } catch (NumberFormatException e) {
                throw new BallerinaException("Invalid content length");
            }
            setDiscreteMediaTypeBodyContent(entity, httpMessageDataStreamer.getInputStream(),
                    contentLength);
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

    public static void prepareOutboundResponse(Context context, HTTPCarbonMessage inboundRequestMsg,
                                               HTTPCarbonMessage outboundResponseMsg, BStruct outboundResponseStruct) {

        HttpUtil.checkEntityAvailability(context, outboundResponseStruct);

        HttpUtil.addHTTPSessionAndCorsHeaders(context, inboundRequestMsg, outboundResponseMsg);
        HttpUtil.enrichOutboundMessage(outboundResponseMsg, outboundResponseStruct);
        HttpUtil.setCompressionHeaders(context, inboundRequestMsg, outboundResponseMsg);
        HttpUtil.setChunkingHeader(context, outboundResponseMsg);
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

    /**
     * Sends an HTTP/2 Server Push message back to the client.
     *
     * @param requestMsg   the request message associated to the server push response
     * @param pushResponse the server push message
     * @param pushPromise  the push promise associated with the server push
     * @return the future to get notifications of the operation asynchronously
     */
    public static HttpResponseFuture pushResponse(HTTPCarbonMessage requestMsg, HTTPCarbonMessage pushResponse,
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
    public static HttpResponseFuture pushPromise(HTTPCarbonMessage requestMsg, Http2PushPromise pushPromise) {
        HttpResponseFuture responseFuture;
        try {
            responseFuture = requestMsg.pushPromise(pushPromise);
        } catch (org.wso2.transport.http.netty.contract.ServerConnectorException e) {
            throw new BallerinaConnectorException("Error occurred during response", e);
        }
        return responseFuture;
    }

    public static void handleFailure(HTTPCarbonMessage requestMessage, BallerinaConnectorException ex) {
        Object carbonStatusCode = requestMessage.getProperty(HTTP_STATUS_CODE);
        int statusCode = (carbonStatusCode == null) ? 500 : Integer.parseInt(carbonStatusCode.toString());
        String errorMsg = ex.getMessage();
        log.error(errorMsg, ex);
        sendOutboundResponse(requestMessage, createErrorMessage(errorMsg, statusCode));
    }

    public static void handleFailure(HTTPCarbonMessage requestMessage, BStruct error) {
        Object carbonStatusCode = requestMessage.getProperty(HttpConstants.HTTP_STATUS_CODE);
        int statusCode = (carbonStatusCode == null) ? 500 : Integer.parseInt(carbonStatusCode.toString());
        String errorMsg = error.getStringField(0);
        log.error(errorMsg);
        ErrorHandlerUtils.printError("error: " + BLangVMErrors.getPrintableStackTrace(error));
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
        httpHeaders.set(HttpHeaderNames.CONTENT_TYPE, org.wso2.transport.http.netty.common.Constants.TEXT_PLAIN);

        response.setProperty(org.wso2.transport.http.netty.common.Constants.HTTP_STATUS_CODE, statusCode);
    }

    public static BStruct getHttpConnectorError(Context context, Throwable throwable) {
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
        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) struct.getNativeData(TRANSPORT_MESSAGE);
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
    public static Http2PushPromise getPushPromise(BStruct pushPromiseStruct, Http2PushPromise defaultPushPromise) {
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
    public static void populatePushPromiseStruct(BStruct pushPromiseStruct, Http2PushPromise pushPromise) {
        pushPromiseStruct.addNativeData(HttpConstants.TRANSPORT_PUSH_PROMISE, pushPromise);
        pushPromiseStruct.setStringField(HttpConstants.PUSH_PROMISE_PATH_INDEX, pushPromise.getPath());
        pushPromiseStruct.setStringField(HttpConstants.PUSH_PROMISE_METHOD_INDEX, pushPromise.getMethod());
    }

    /**
     * Creates native {@code Http2PushPromise} from PushPromise struct.
     *
     * @param struct the PushPromise struct
     * @return the populated the native {@code Http2PushPromise}
     */
    public static Http2PushPromise createHttpPushPromise(BStruct struct) {
        String method = struct.getStringField(HttpConstants.PUSH_PROMISE_METHOD_INDEX);
        if (method == null || method.isEmpty()) {
            method = HttpConstants.HTTP_METHOD_GET;
        }

        String path = struct.getStringField(HttpConstants.PUSH_PROMISE_PATH_INDEX);
        if (path == null || path.isEmpty()) {
            path = HttpConstants.DEFAULT_BASE_PATH;
        }
        return new Http2PushPromise(method, path);
    }

    public static void addCarbonMsg(BStruct struct, HTTPCarbonMessage httpCarbonMessage) {
        struct.addNativeData(TRANSPORT_MESSAGE, httpCarbonMessage);
    }

    public static void populateInboundRequest(BStruct inboundRequestStruct, BStruct entity, BStruct mediaType,
                                              HTTPCarbonMessage inboundRequestMsg,
                                              RequestCacheControlStruct requestCacheControl) {
        inboundRequestStruct.addNativeData(TRANSPORT_MESSAGE, inboundRequestMsg);
        inboundRequestStruct.addNativeData(REQUEST, true);

        enrichWithInboundRequestInfo(inboundRequestStruct, inboundRequestMsg);
        enrichWithInboundRequestHeaders(inboundRequestStruct, inboundRequestMsg);

        populateEntity(entity, mediaType, inboundRequestMsg);
        inboundRequestStruct.addNativeData(MESSAGE_ENTITY, entity);
        inboundRequestStruct.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, false);

        if (inboundRequestMsg.getHeader(CACHE_CONTROL.toString()) != null) {
            requestCacheControl.populateStruct(inboundRequestMsg.getHeader(CACHE_CONTROL.toString()));
        }
        inboundRequestStruct.setRefField(REQUEST_CACHE_CONTROL_INDEX, requestCacheControl.getStruct());
    }

    private static void enrichWithInboundRequestHeaders(BStruct inboundRequestStruct,
                                                        HTTPCarbonMessage inboundRequestMsg) {
        if (inboundRequestMsg.getHeader(HttpHeaderNames.USER_AGENT.toString()) != null) {
            inboundRequestStruct.setStringField(HttpConstants.REQUEST_USER_AGENT_INDEX,
                                                inboundRequestMsg.getHeader(HttpHeaderNames.USER_AGENT.toString()));
            inboundRequestMsg.removeHeader(HttpHeaderNames.USER_AGENT.toString());
        }
    }

    private static void enrichWithInboundRequestInfo(BStruct inboundRequestStruct,
                                                     HTTPCarbonMessage inboundRequestMsg) {
        inboundRequestStruct.setStringField(HttpConstants.REQUEST_RAW_PATH_INDEX,
                (String) inboundRequestMsg.getProperty(HttpConstants.REQUEST_URL));
        inboundRequestStruct.setStringField(HttpConstants.REQUEST_METHOD_INDEX,
                (String) inboundRequestMsg.getProperty(HttpConstants.HTTP_METHOD));
        inboundRequestStruct.setStringField(HttpConstants.REQUEST_VERSION_INDEX,
                (String) inboundRequestMsg.getProperty(HttpConstants.HTTP_VERSION));
        Map<String, String> resourceArgValues =
                (Map<String, String>) inboundRequestMsg.getProperty(HttpConstants.RESOURCE_ARGS);
        if (resourceArgValues != null) {
            inboundRequestStruct.setStringField(HttpConstants.REQUEST_EXTRA_PATH_INFO_INDEX,
                                                resourceArgValues.get(HttpConstants.EXTRA_PATH_INFO));
        }
    }

    /**
     * Populate connection information.
     *
     * @param connection Represent the connection struct
     * @param inboundMsg Represent carbon message.
     */
    public static void enrichConnectionInfo(BStruct connection, HTTPCarbonMessage inboundMsg) {
        connection.addNativeData(HttpConstants.TRANSPORT_MESSAGE, inboundMsg);
    }

    /**
     * Populate serviceEndpoint information.
     *
     * @param serviceEndpoint Represent the serviceEndpoint struct
     * @param inboundMsg Represent carbon message.
     * @param httpResource Represent Http Resource.
     */
    public static void enrichServiceEndpointInfo(BStruct serviceEndpoint, HTTPCarbonMessage inboundMsg,
            HttpResource httpResource) {
        BStruct remote = BLangConnectorSPIUtil.createBStruct(
                httpResource.getBalResource().getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                PROTOCOL_PACKAGE_HTTP, HttpConstants.REMOTE);
        BStruct local = BLangConnectorSPIUtil.createBStruct(
                httpResource.getBalResource().getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                PROTOCOL_PACKAGE_HTTP, HttpConstants.LOCAL);

        Object remoteSocketAddress = inboundMsg.getProperty(HttpConstants.REMOTE_ADDRESS);
        if (remoteSocketAddress != null && remoteSocketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteSocketAddress;
            String remoteHost = inetSocketAddress.getHostName();
            long remotePort = inetSocketAddress.getPort();
            remote.setStringField(HttpConstants.REMOTE_HOST_INDEX, remoteHost);
            remote.setIntField(HttpConstants.REMOTE_PORT_INDEX, remotePort);
        }
        serviceEndpoint.setRefField(HttpConstants.REMOTE_STRUCT_INDEX, remote);

        Object localSocketAddress = inboundMsg.getProperty(HttpConstants.LOCAL_ADDRESS);
        if (localSocketAddress != null && localSocketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) localSocketAddress;
            String localHost = inetSocketAddress.getHostName();
            long localPort = inetSocketAddress.getPort();
            local.setStringField(HttpConstants.LOCAL_HOST_INDEX, localHost);
            local.setIntField(HttpConstants.LOCAL_PORT_INDEX, localPort);
        }
        serviceEndpoint.setRefField(HttpConstants.LOCAL_STRUCT_INDEX, local);
        serviceEndpoint.setStringField(HttpConstants.SERVICE_ENDPOINT_PROTOCOL_INDEX,
                (String) inboundMsg.getProperty(HttpConstants.PROTOCOL));
    }

    /**
     * Populate inbound response with headers and entity.
     * @param inboundResponse  Ballerina struct to represent response
     * @param entity    Entity of the response
     * @param mediaType Content type of the response
     * @param responseCacheControl  Cache control struct which holds the cache control directives related to the
     *                              response
     * @param inboundResponseMsg      Represent carbon message.
     */
    public static void populateInboundResponse(BStruct inboundResponse, BStruct entity, BStruct mediaType,
                                               ResponseCacheControlStruct responseCacheControl,
                                               HTTPCarbonMessage inboundResponseMsg) {
        inboundResponse.addNativeData(TRANSPORT_MESSAGE, inboundResponseMsg);
        int statusCode = (Integer) inboundResponseMsg.getProperty(HTTP_STATUS_CODE);
        inboundResponse.setIntField(RESPONSE_STATUS_CODE_INDEX, statusCode);
        inboundResponse.setStringField(RESPONSE_REASON_PHRASE_INDEX,
                                       HttpResponseStatus.valueOf(statusCode).reasonPhrase());

        if (inboundResponseMsg.getHeader(HttpHeaderNames.SERVER.toString()) != null) {
            inboundResponse.setStringField(HttpConstants.RESPONSE_SERVER_INDEX,
                                           inboundResponseMsg.getHeader(HttpHeaderNames.SERVER.toString()));
            inboundResponseMsg.removeHeader(HttpHeaderNames.SERVER.toString());
        }

        if (inboundResponseMsg.getHeader(CACHE_CONTROL.toString()) != null) {
            responseCacheControl.populateStruct(inboundResponseMsg.getHeader(CACHE_CONTROL.toString()));
        }
        inboundResponse.setRefField(RESPONSE_CACHE_CONTROL_INDEX, responseCacheControl.getStruct());

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
        String contentType = cMsg.getHeader(HttpHeaderNames.CONTENT_TYPE.toString());
        MimeUtil.setContentType(mediaType, entity, contentType);
        int contentLength = -1;
        String lengthStr = cMsg.getHeader(HttpHeaderNames.CONTENT_LENGTH.toString());
        try {
            contentLength = lengthStr != null ? Integer.parseInt(lengthStr) : contentLength;
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
    public static void enrichOutboundMessage(HTTPCarbonMessage outboundMsg, BStruct outboundStruct) {
        setHeadersToTransportMessage(outboundMsg, outboundStruct);
        setPropertiesToTransportMessage(outboundMsg, outboundStruct);
    }

    @SuppressWarnings("unchecked")
    private static void setHeadersToTransportMessage(HTTPCarbonMessage outboundMsg, BStruct struct) {
        BStruct entityStruct = (BStruct) struct.getNativeData(MESSAGE_ENTITY);
        HttpHeaders transportHeaders = outboundMsg.getHeaders();
        if (isRequestStruct(struct) || isResponseStruct(struct)) {
            addRemovedPropertiesBackToHeadersMap(struct, transportHeaders);
            // Since now the InRequest & OutRequest are merged to a single Request and InResponse & OutResponse
            // are merged to a single Response, without returning need to populate all headers from the struct
            // to the HTTPCarbonMessage.
            // TODO: refactor this logic properly.
            // return;
        }
        HttpHeaders httpHeaders = (HttpHeaders) entityStruct.getNativeData(ENTITY_HEADERS);
        if (httpHeaders != transportHeaders) {
            //This is done only when the entity map and transport message do not refer to the same header map
            if (httpHeaders != null) {
                transportHeaders.add(httpHeaders);
            }
        }
    }

    private static boolean isRequestStruct(BStruct struct) {
        return struct.getType().getName().equals(REQUEST);
    }

    private static boolean isResponseStruct(BStruct struct) {
        return struct.getType().getName().equals(HttpConstants.RESPONSE);
    }

    private static void addRemovedPropertiesBackToHeadersMap(BStruct struct, HttpHeaders transportHeaders) {
        if (isRequestStruct(struct)) {
            if (struct.getStringField(HttpConstants.REQUEST_USER_AGENT_INDEX) != null && !struct.getStringField
                    (HttpConstants.REQUEST_USER_AGENT_INDEX).isEmpty()) {
                transportHeaders.set(HttpHeaderNames.USER_AGENT.toString(),
                                     struct.getStringField(HttpConstants.REQUEST_USER_AGENT_INDEX));
            }
        } else {
            if (struct.getStringField(HttpConstants.RESPONSE_SERVER_INDEX) != null && !struct.getStringField
                    (HttpConstants.RESPONSE_SERVER_INDEX).isEmpty()) {
                transportHeaders.set(HttpHeaderNames.SERVER.toString(),
                                     struct.getStringField(HttpConstants.RESPONSE_SERVER_INDEX));
            }
        }
    }

    private static void setPropertiesToTransportMessage(HTTPCarbonMessage outboundResponseMsg, BStruct struct) {
        if (isResponseStruct(struct)) {
            if (struct.getIntField(
                    RESPONSE_STATUS_CODE_INDEX) != 0) {
                outboundResponseMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, getIntValue(
                        struct.getIntField(RESPONSE_STATUS_CODE_INDEX)));
            }
            if (struct.getStringField(RESPONSE_REASON_PHRASE_INDEX) != null && !struct
                    .getStringField(RESPONSE_REASON_PHRASE_INDEX).isEmpty()) {
                outboundResponseMsg.setProperty(HttpConstants.HTTP_REASON_PHRASE,
                        struct.getStringField(RESPONSE_REASON_PHRASE_INDEX));
            }
        }
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
            createNewEntity(context, struct);
        }
    }

    private static void setCompressionHeaders(Context context, HTTPCarbonMessage requestMsg, HTTPCarbonMessage
            outboundResponseMsg) {
        Service serviceInstance = BLangConnectorSPIUtil.getService(context.getProgramFile(),
                context.getServiceInfo().getType());
        Annotation configAnnot = getServiceConfigAnnotation(serviceInstance, PROTOCOL_PACKAGE_HTTP);
        if (configAnnot == null) {
            return;
        }
        String contentEncoding = outboundResponseMsg.getHeaders().get(HttpHeaderNames.CONTENT_ENCODING);
        if (contentEncoding != null) {
            return;
        }
        String compressionValue = configAnnot.getValue().getRefField(ANN_CONFIG_ATTR_COMPRESSION).getStringValue();
        String acceptEncodingValue = requestMsg.getHeaders().get(HttpHeaderNames.ACCEPT_ENCODING);
        if (ALWAYS.equalsIgnoreCase(compressionValue)) {
            if (acceptEncodingValue == null || HTTP_TRANSFER_ENCODING_IDENTITY.equals(acceptEncodingValue)) {
                outboundResponseMsg.getHeaders().set(HttpHeaderNames.CONTENT_ENCODING, ENCODING_GZIP);
            }
        } else if (NEVER.equalsIgnoreCase(compressionValue)) {
            outboundResponseMsg.getHeaders().set(HttpHeaderNames.CONTENT_ENCODING,
                    HTTP_TRANSFER_ENCODING_IDENTITY);
        }
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
        } else {
            httpCarbonMessage = new HTTPCarbonMessage(
                    new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        }
        httpCarbonMessage.completeMessage();
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

    public static void serverConnectionStructCheck(HTTPCarbonMessage reqMsg) {
        if (reqMsg == null) {
            throw new BallerinaException("operation not allowed:invalid Connection variable");
        }
    }

    private static boolean is100ContinueRequest(HTTPCarbonMessage reqMsg) {
        return HttpConstants.HEADER_VAL_100_CONTINUE.equalsIgnoreCase(
                reqMsg.getHeader(HttpHeaderNames.EXPECT.toString()));
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

    private static int getIntValue(long val) {
        int intVal = (int) val;

        if (intVal != val) {
            throw new IllegalArgumentException("invalid argument: " + val);
        }

        return intVal;
    }

    public static String getContentTypeFromTransportMessage(HTTPCarbonMessage transportMessage) {
        return transportMessage.getHeader(HttpHeaderNames.CONTENT_TYPE.toString()) != null ?
                transportMessage.getHeader(HttpHeaderNames.CONTENT_TYPE.toString()) : null;
    }

    /**
     * If the given Content-Type header value doesn't have a boundary parameter value, get a new boundary string and
     * append it to Content-Type and set it to transport message.
     *
     * @param transportMessage Represent transport message
     * @param contentType      Represent the Content-Type header value
     * @return The boundary string that was extracted from header or the newly generated one
     */
    public static String addBoundaryIfNotExist(HTTPCarbonMessage transportMessage, String contentType) {
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
    private static String addBoundaryParameter(HTTPCarbonMessage transportMessage, String contentType) {
        String boundaryString = null;
        if (contentType != null && contentType.startsWith(MULTIPART_AS_PRIMARY_TYPE)) {
            boundaryString = MimeUtil.getNewMultipartDelimiter();
            transportMessage.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), contentType + "; " + BOUNDARY + "=" +
                    boundaryString);
        }
        return boundaryString;
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

    public static HttpWsConnectorFactory createHttpWsConnectionFactory() {
        return new DefaultHttpWsConnectorFactory();
    }

    public static void checkAndObserveHttpRequest(Context context, HTTPCarbonMessage message) {
        Optional<ObserverContext> observerContext = ObservabilityUtils.getParentContext(context);
        observerContext.ifPresent(ctx -> {
            HttpUtil.injectHeaders(message, ObservabilityUtils.getContextProperties(ctx));
            ctx.addTag(TAG_KEY_HTTP_METHOD, String.valueOf(message.getProperty(HttpConstants.HTTP_METHOD)));
            ctx.addTag(TAG_KEY_HTTP_URL, String.valueOf(message.getProperty(HttpConstants.TO)));
            ctx.addTag(TAG_KEY_PEER_ADDRESS, String.valueOf(message.getProperty(PROPERTY_HTTP_HOST)
                + ":" + message.getProperty(PROPERTY_HTTP_PORT)));
            // Add HTTP Status Code tag. The HTTP status code will be set using the response message.
            // Sometimes the HTTP status code will not be set due to errors etc. Therefore, it's very important to set
            // some value to HTTP Status Code to make sure that tags will not change depending on various
            // circumstances.
            // HTTP Status code must be a number.
            ctx.addTag(TAG_KEY_HTTP_STATUS_CODE, Integer.toString(0));
        });
    }

    public static void injectHeaders(HTTPCarbonMessage msg, Map<String, String> headers) {
        if (headers != null) {
            headers.forEach((key, value) -> msg.setHeader(key, String.valueOf(value)));
        }
    }

    private static void setChunkingHeader(Context context, HTTPCarbonMessage
            outboundResponseMsg) {
        Service serviceInstance = BLangConnectorSPIUtil.getService(context.getProgramFile(),
                context.getServiceInfo().getType());
        Annotation configAnnot = getServiceConfigAnnotation(serviceInstance, PROTOCOL_PACKAGE_HTTP);
        if (configAnnot == null) {
            return;
        }
        String transferValue = configAnnot.getValue().getRefField(ANN_CONFIG_ATTR_CHUNKING).getStringValue();
        if (transferValue != null) {
            outboundResponseMsg.setProperty(CHUNKING_CONFIG, getChunkConfig(transferValue));
        }
    }
}

