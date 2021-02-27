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

import io.ballerina.runtime.api.values.BObject;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HttpConstants;

import static org.ballerinalang.mime.util.MimeConstants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.MimeConstants.MESSAGE_DATA_SOURCE;
import static org.ballerinalang.net.http.HttpConstants.HTTP_HEADERS;
import static org.ballerinalang.net.http.HttpConstants.TRANSPORT_MESSAGE;

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
    public static void enrichTestEntity(BObject entity, String contentType, String payload) {
        enrichTestMessageHeaders(entity, contentType);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
    }

    /**
     * Enriches req/resp message with provided header.
     *
     * @param messageObj      mime entity to be enriched.
     * @param contentType content-type header value.
     */
    public static void enrichTestMessageHeaders(BObject messageObj, String contentType) {
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add(CONTENT_TYPE, contentType);
        messageObj.addNativeData(HTTP_HEADERS, httpHeaders);
    }

    /**
     * Enriches entity with default transport message.
     *
     * @param entity      mime entity to be enriched.
     * @param payload     mime entity payload.
     */
    @Deprecated
    public static void enrichEntityWithDefaultMsg(BMap<String, BValue> entity, String payload) {
        HTTPTestRequest inRequestMsg =
                MessageUtils.generateHTTPMessage("", HttpConstants.HTTP_METHOD_POST, payload);
        inRequestMsg.setHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(payload.length()));
        entity.addNativeData(TRANSPORT_MESSAGE, inRequestMsg);
    }

    /**
     * Enriches entity with default transport message.
     *
     * @param entity      mime entity to be enriched.
     * @param payload     mime entity payload.
     */
    public static void enrichEntityWithDefaultMsg(BObject entity, String payload) {
        HTTPTestRequest inRequestMsg =
                MessageUtils.generateHTTPMessage("", HttpConstants.HTTP_METHOD_POST, payload);
        inRequestMsg.setHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(payload.length()));
        entity.addNativeData(TRANSPORT_MESSAGE, inRequestMsg);
    }

    public static Object getMessageDataSource(BMap<String, BValue> entityObj) {
        return entityObj.getNativeData(MESSAGE_DATA_SOURCE);
    }
}
