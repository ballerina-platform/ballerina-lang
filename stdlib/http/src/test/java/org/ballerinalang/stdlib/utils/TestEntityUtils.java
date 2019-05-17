/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.utils;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpResourceArguments;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.SignatureParams;
import org.ballerinalang.net.http.caching.RequestCacheControlObj;
import org.ballerinalang.stdlib.mime.Util;
import org.ballerinalang.util.codegen.ProgramFile;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CACHE_CONTROL;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.MimeConstants.IS_BODY_BYTE_CHANNEL_ALREADY_SET;
import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.MESSAGE_DATA_SOURCE;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.mime.util.MimeConstants.REQUEST_ENTITY_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.RESPONSE_ENTITY_FIELD;
import static org.ballerinalang.net.http.HttpConstants.CALLER;
import static org.ballerinalang.net.http.HttpConstants.HTTP_LISTENER_ENDPOINT;
import static org.ballerinalang.net.http.HttpConstants.HTTP_STATUS_CODE;
import static org.ballerinalang.net.http.HttpConstants.MUTUAL_SSL_HANDSHAKE_RECORD;
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
import static org.ballerinalang.net.http.HttpConstants.SERVICE_ENDPOINT_CONNECTION_FIELD;
import static org.ballerinalang.net.http.HttpConstants.TRANSPORT_MESSAGE;
import static org.ballerinalang.net.http.nativeimpl.pipelining.PipeliningHandler.sendPipelinedResponse;

/**
 * Utility functions for interact with Ballerina mime Entity.
 */
public class TestEntityUtils {

    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * Enriches the mime entity with the provided data.
     *
     * @param entity      mime entity to be enriched.
     * @param contentType content-type header value.
     * @param payload     mime entity payload.
     */
    public static void enrichTestEntity(BMap<String, BValue> entity, String contentType, String payload) {
        enrichTestEntityHeaders(entity, contentType);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
    }

    /**
     * Enriches entity with provided header.
     *
     * @param entity      mime entity to be enriched.
     * @param contentType content-type header value.
     */
    public static void enrichTestEntityHeaders(BMap<String, BValue> entity, String contentType) {
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add(CONTENT_TYPE, contentType);
        entity.addNativeData(ENTITY_HEADERS, httpHeaders);
    }

    /**
     * Enriches entity with default transport message.
     *
     * @param entity      mime entity to be enriched.
     * @param payload     mime entity payload.
     */
    public static void enrichEntityWithDefaultMsg(BMap<String, BValue> entity, String payload) {
        HTTPTestRequest inRequestMsg =
                MessageUtils.generateHTTPMessage("", HttpConstants.HTTP_METHOD_POST, payload);
        inRequestMsg.setHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(payload.length()));
        entity.addNativeData(TRANSPORT_MESSAGE, inRequestMsg);
    }

    //TODO Remove all following util methods after migration : implemented using bvm values/types
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
            //TODO fix following with bvm values
//            RequestCacheControlStruct requestCacheControl = new RequestCacheControlStruct(cacheControlStruct);
//            requestCacheControl.populateStruct(cacheControlHeader);
//            inboundRequestStruct.put(REQUEST_CACHE_CONTROL_FIELD, requestCacheControl.getStruct());
        }
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
            //TODO fix following with bvm values
//            ResponseCacheControlStruct responseCacheControl
//                    = new ResponseCacheControlStruct(
//                    programFile.getPackageInfo(PROTOCOL_PACKAGE_HTTP).getStructInfo(RESPONSE_CACHE_CONTROL));
//            responseCacheControl.populateStruct(cacheControlHeader);
//            inboundResponse.put(RESPONSE_CACHE_CONTROL_FIELD, responseCacheControl.getStruct());
        }

        populateEntity(entity, mediaType, inboundResponseMsg);
        inboundResponse.put(RESPONSE_ENTITY_FIELD, entity);
        inboundResponse.addNativeData(IS_BODY_BYTE_CHANNEL_ALREADY_SET, false);
    }


    private static void enrichWithInboundRequestInfo(BMap<String, BValue> inboundRequestStruct,
                                                     HttpCarbonMessage inboundRequestMsg) {
        inboundRequestStruct.put(HttpConstants.REQUEST_RAW_PATH_FIELD,
                                 new BString((String) inboundRequestMsg.getProperty(HttpConstants.REQUEST_URL)));
        inboundRequestStruct.put(HttpConstants.REQUEST_METHOD_FIELD,
                                 new BString((String) inboundRequestMsg.getProperty(HttpConstants.HTTP_METHOD)));
        inboundRequestStruct.put(HttpConstants.REQUEST_VERSION_FIELD,
                                 new BString((String) inboundRequestMsg.getProperty(HttpConstants.HTTP_VERSION)));
        HttpResourceArguments resourceArgValues =
                (HttpResourceArguments) inboundRequestMsg.getProperty(HttpConstants.RESOURCE_ARGS);
        if (resourceArgValues != null && resourceArgValues.getMap().get(HttpConstants.EXTRA_PATH_INFO) != null) {
            inboundRequestStruct.put(HttpConstants.REQUEST_EXTRA_PATH_INFO_FIELD,
                                     new BString(resourceArgValues.getMap().get(HttpConstants.EXTRA_PATH_INFO)));
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
        Util.setContentType(mediaType, entity, contentType);
        long contentLength = -1;
        String lengthStr = cMsg.getHeader(HttpHeaderNames.CONTENT_LENGTH.toString());
        try {
            contentLength = lengthStr != null ? Long.parseLong(lengthStr) : contentLength;
            Util.setContentLength(entity, contentLength);
        } catch (NumberFormatException e) {
            throw new BallerinaException("Invalid content length");
        }
        entity.addNativeData(ENTITY_HEADERS, cMsg.getHeaders());
    }

    public static void handleFailure(HttpCarbonMessage requestMessage, org.ballerinalang.connector.api.BallerinaConnectorException ex) {
        String errorMsg = ex.getMessage();
        int statusCode = HttpUtil.getStatusCode(requestMessage, errorMsg);
        sendPipelinedResponse(requestMessage, HttpUtil.createErrorMessage(errorMsg, statusCode));
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
}
