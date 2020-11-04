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
package org.ballerinalang.net.http;

import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.ValueCreator;

import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_MIME_PKG_ID;
import static org.ballerinalang.net.http.HttpConstants.CALLER;
import static org.ballerinalang.net.http.HttpConstants.ENTITY;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTP_PKG_ID;
import static org.ballerinalang.net.http.HttpConstants.PUSH_PROMISE;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;
import static org.ballerinalang.net.http.HttpConstants.REQUEST_CACHE_CONTROL;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_CACHE_CONTROL;

/**
 * Utility functions to create JVM values.
 *
 * @since 1.0
 */
public class ValueCreatorUtils {

    private static final ValueCreator httpValueCreator = ValueCreator.getValueCreator(PROTOCOL_HTTP_PKG_ID.toString());
    private static final ValueCreator mimeValueCreator = ValueCreator.getValueCreator(PROTOCOL_MIME_PKG_ID.toString());

    public static BObject createRequestObject() {
        return createObjectValue(httpValueCreator, REQUEST);
    }

    public static BObject createResponseObject() {
        return createObjectValue(httpValueCreator, RESPONSE);
    }

    public static BObject createEntityObject() {
        return createObjectValue(mimeValueCreator, ENTITY);
    }

    public static BObject createMediaTypeObject() {
        return createObjectValue(mimeValueCreator, MEDIA_TYPE);
    }

    public static BObject createPushPromiseObject() {
        return createObjectValue(httpValueCreator, PUSH_PROMISE, BStringUtils.fromString("/"),
                                 BStringUtils.fromString("GET"));
    }

    public static BObject createRequestCacheControlObject() {
        return createObjectValue(httpValueCreator, REQUEST_CACHE_CONTROL);
    }

    public static BObject createResponseCacheControlObject() {
        return createObjectValue(httpValueCreator, RESPONSE_CACHE_CONTROL);
    }

    public static BObject createCallerObject() {
        return createObjectValue(httpValueCreator, CALLER);
    }
    
    /**
     * Method that creates a runtime record value using the given record type in the http package.
     *
     * @param recordTypeName name of the record type.
     * @return value of the record.
     */
    public static BMap<BString, Object> createHTTPRecordValue(String recordTypeName) {
        return httpValueCreator.createRecordValue(recordTypeName);
    }

    /**
     * Method that creates a runtime object value using the given package id and object type name.
     *
     * @param valueCreator value creator specific for the package.
     * @param objectTypeName name of the object type.
     * @param fieldValues values to be used for fields when creating the object value instance.
     * @return value of the object.
     */
    private static BObject createObjectValue(ValueCreator valueCreator, String objectTypeName,
                                                 Object... fieldValues) {

        Object[] fields = new Object[fieldValues.length * 2];

        // Adding boolean values for each arg
        for (int i = 0, j = 0; i < fieldValues.length; i++) {
            fields[j++] = fieldValues[i];
            fields[j++] = true;
        }

        // passing scheduler, strand and properties as null for the moment, but better to expose them via this method
        return valueCreator.createObjectValue(objectTypeName, null, null, null, fields);
    }
}
