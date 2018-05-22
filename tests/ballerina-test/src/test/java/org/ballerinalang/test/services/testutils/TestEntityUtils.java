/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.services.testutils;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.model.values.BStruct;

import static org.ballerinalang.mime.util.Constants.ENTITY_BYTE_CHANNEL;
import static org.ballerinalang.mime.util.Constants.ENTITY_HEADERS;

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
    public static void enrichTestEntity(BStruct entity, String contentType, String payload) {
        enrichTestEntityHeaders(entity, contentType);
        entity.addNativeData(ENTITY_BYTE_CHANNEL, EntityBodyHandler.getEntityWrapper(payload));
    }

    /**
     * Enriches entity with provided header.
     *
     * @param entity      mime entity to be enriched.
     * @param contentType content-type header value.
     */
    public static void enrichTestEntityHeaders(BStruct entity, String contentType) {
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        httpHeaders.add(CONTENT_TYPE, contentType);
        entity.addNativeData(ENTITY_HEADERS, httpHeaders);
    }
}
