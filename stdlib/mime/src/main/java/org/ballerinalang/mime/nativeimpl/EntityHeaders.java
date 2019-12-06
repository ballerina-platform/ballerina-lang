/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.mime.nativeimpl;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.HandleValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.mime.util.MimeUtil;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.ballerinalang.mime.util.MimeConstants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.MimeConstants.HEADER_NOT_FOUND;

/**
 * Utilities related to entity headers.
 *
 * @since 1.1.0
 */
public class EntityHeaders {
    public static void addHeader(ObjectValue entityObj, String headerName, String headerValue) {
        if (headerName == null || headerValue == null) {
            return;
        }
        getHeaders(entityObj).add(headerName, headerValue);
    }

    public static String getHeader(ObjectValue entityObj, String headerName) {
        if (entityObj.getNativeData(ENTITY_HEADERS) == null) {
            throw BallerinaErrors.createError(HEADER_NOT_FOUND, "Http header does not exist");
        }
        HttpHeaders httpHeaders = (HttpHeaders) entityObj.getNativeData(ENTITY_HEADERS);
        if (httpHeaders != null && httpHeaders.get(headerName) != null && !httpHeaders.get(headerName).isEmpty()) {
            return httpHeaders.get(headerName);
        } else {
            throw BallerinaErrors.createError(HEADER_NOT_FOUND, "Http header does not exist");
        }
    }

    public static ArrayValue getHeaderNames(ObjectValue entityObj) {
        ArrayValue stringArray = new ArrayValueImpl(new BArrayType(BTypes.typeHandle));
        if (entityObj.getNativeData(ENTITY_HEADERS) == null) {
            return stringArray;
        }
        HttpHeaders httpHeaders = (HttpHeaders) entityObj.getNativeData(ENTITY_HEADERS);
        if (httpHeaders != null && !httpHeaders.isEmpty()) {
            int i = 0;
            Set<String> distinctNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            distinctNames.addAll(httpHeaders.names());
            for (String headerName : distinctNames) {
                stringArray.add(i, new HandleValue(headerName));
                i++;
            }
        }
        return stringArray;
    }

    public static ArrayValue getHeaders(ObjectValue entityObj, String headerName) {
        if (entityObj.getNativeData(ENTITY_HEADERS) == null) {
            throw MimeUtil.createError(HEADER_NOT_FOUND, "Http header does not exist");
        }
        HttpHeaders httpHeaders = (HttpHeaders) entityObj.getNativeData(ENTITY_HEADERS);
        List<String> headerValueList = httpHeaders.getAll(headerName);
        if (headerValueList == null) {
            throw MimeUtil.createError(HEADER_NOT_FOUND, "Http header does not exist");
        }
        int i = 0;
        ArrayValue stringArray = new ArrayValueImpl(new BArrayType(BTypes.typeHandle));
        for (String headerValue : headerValueList) {
            stringArray.add(i, new HandleValue(headerValue));
            i++;
        }
        return stringArray;
    }

    public static boolean hasHeader(ObjectValue entityObj, String headerName) {
        if (entityObj.getNativeData(ENTITY_HEADERS) == null) {
            return false;
        }
        HttpHeaders httpHeaders = (HttpHeaders) entityObj.getNativeData(ENTITY_HEADERS);
        List<String> headerValueList = httpHeaders.getAll(headerName);
        if (headerValueList == null || headerValueList.isEmpty()) {
            return false;
        }
        return true;
    }

    public static void removeAllHeaders(ObjectValue entityObj) {
        if (entityObj.getNativeData(ENTITY_HEADERS) != null) {
            HttpHeaders httpHeaders = (HttpHeaders) entityObj.getNativeData(ENTITY_HEADERS);
            httpHeaders.clear();
        }
    }

    public static void removeHeader(ObjectValue entityObj, String headerName) {
        if (entityObj.getNativeData(ENTITY_HEADERS) != null) {
            HttpHeaders httpHeaders = (HttpHeaders) entityObj.getNativeData(ENTITY_HEADERS);
            httpHeaders.remove(headerName);
        }
    }

    public static void setHeader(ObjectValue entityObj, String headerName, String headerValue) {
        if (headerName == null || headerValue == null) {
            return;
        }
        HttpHeaders httpHeaders = getHeaders(entityObj);
        httpHeaders.set(headerName, headerValue);
    }

    private static HttpHeaders getHeaders(ObjectValue entityObj) {
        HttpHeaders httpHeaders;
        if (entityObj.getNativeData(ENTITY_HEADERS) != null) {
            httpHeaders = (HttpHeaders) entityObj.getNativeData(ENTITY_HEADERS);
        } else {
            httpHeaders = setEntityHeaders(entityObj);
        }
        return httpHeaders;
    }

    private static HttpHeaders setEntityHeaders(ObjectValue entityObj) {
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        entityObj.addNativeData(ENTITY_HEADERS, httpHeaders);
        return httpHeaders;
    }
}
