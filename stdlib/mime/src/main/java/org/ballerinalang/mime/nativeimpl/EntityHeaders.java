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
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.mime.util.MimeUtil;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.ballerinalang.mime.util.MimeConstants.ENTITY_HEADERS;
import static org.ballerinalang.mime.util.MimeConstants.ENTITY_TRAILER_HEADERS;
import static org.ballerinalang.mime.util.MimeConstants.HEADER_NOT_FOUND_ERROR;
import static org.ballerinalang.mime.util.MimeConstants.INVALID_HEADER_OPERATION_ERROR;
import static org.ballerinalang.mime.util.MimeConstants.LEADING_HEADER;

/**
 * Utilities related to entity headers.
 *
 * @since 1.1.0
 */
public class EntityHeaders {

    private static final BArrayType bArrayType = new BArrayType(BTypes.typeHandle);

    public static void addHeader(ObjectValue entityObj, BString headerName, BString headerValue, Object position) {
        if (headerName == null || headerValue == null) {
            return;
        }
        try {
            getOrCreateHeadersBasedOnPosition(entityObj, position).add(headerName.getValue(), headerValue.getValue());
        } catch (IllegalArgumentException ex) {
            throw MimeUtil.createError(INVALID_HEADER_OPERATION_ERROR, ex.getMessage());
        }
    }

    public static BString getHeader(ObjectValue entityObj, BString headerName, Object position) {
        HttpHeaders httpHeaders = getHeadersBasedOnPosition(entityObj, position);
        if (httpHeaders == null) {
            throw MimeUtil.createError(HEADER_NOT_FOUND_ERROR, "Http header does not exist");
        }
        if (httpHeaders.get(headerName.getValue()) != null) {
            return StringUtils.fromString(httpHeaders.get(headerName.getValue()));
        } else {
            throw MimeUtil.createError(HEADER_NOT_FOUND_ERROR, "Http header does not exist");
        }
    }

    public static ArrayValue getHeaderNames(ObjectValue entityObj, Object position) {
        HttpHeaders httpHeaders = getHeadersBasedOnPosition(entityObj, position);
        if (httpHeaders == null || httpHeaders.isEmpty()) {
            return new ArrayValueImpl(new BString[0]);
        }
        Set<String> distinctNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        distinctNames.addAll(httpHeaders.names());
        return new ArrayValueImpl(StringUtils.fromStringArray(distinctNames.toArray(new String[0])));
    }

    public static ArrayValue getHeaders(ObjectValue entityObj, BString headerName, Object position) {
        HttpHeaders httpHeaders = getHeadersBasedOnPosition(entityObj, position);
        if (httpHeaders == null) {
            throw MimeUtil.createError(HEADER_NOT_FOUND_ERROR, "Http header does not exist");
        }
        List<String> headerValueList = httpHeaders.getAll(headerName.getValue());
        if (headerValueList == null) {
            throw MimeUtil.createError(HEADER_NOT_FOUND_ERROR, "Http header does not exist");
        }
        return new ArrayValueImpl(StringUtils.fromStringArray(headerValueList.toArray(new String[0])));
    }

    public static boolean hasHeader(ObjectValue entityObj, BString headerName, Object position) {
        HttpHeaders httpHeaders = getHeadersBasedOnPosition(entityObj, position);
        if (httpHeaders == null) {
            return false;
        }
        List<String> headerValueList = httpHeaders.getAll(headerName.getValue());
        return headerValueList != null && !headerValueList.isEmpty();
    }

    public static void removeAllHeaders(ObjectValue entityObj, Object position) {
        HttpHeaders httpHeaders = getHeadersBasedOnPosition(entityObj, position);
        if (httpHeaders != null) {
            httpHeaders.clear();
        }
    }

    public static void removeHeader(ObjectValue entityObj, BString headerName, Object position) {
        HttpHeaders httpHeaders = getHeadersBasedOnPosition(entityObj, position);
        if (httpHeaders != null) {
            httpHeaders.remove(headerName.getValue());
        }
    }

    public static void setHeader(ObjectValue entityObj, BString headerName, BString headerValue, Object position) {
        if (headerName == null || headerValue == null) {
            return;
        }
        try {
            getOrCreateHeadersBasedOnPosition(entityObj, position).set(headerName.getValue(), headerValue.getValue());
        } catch (IllegalArgumentException ex) {
            throw MimeUtil.createError(INVALID_HEADER_OPERATION_ERROR, ex.getMessage());
        }
    }

    private static HttpHeaders getHeadersBasedOnPosition(ObjectValue entityObj, Object position) {
        return position.equals(StringUtils.fromString(LEADING_HEADER)) ?
                (HttpHeaders) entityObj.getNativeData(ENTITY_HEADERS) :
                (HttpHeaders) entityObj.getNativeData(ENTITY_TRAILER_HEADERS);
    }

    private static HttpHeaders getOrCreateHeadersBasedOnPosition(ObjectValue entityObj, Object position) {
        return position.equals(StringUtils.fromString(LEADING_HEADER)) ?
                getHeaders(entityObj) : getTrailerHeaders(entityObj);
    }

    private static HttpHeaders getHeaders(ObjectValue entityObj) {
        HttpHeaders httpHeaders;
        if (entityObj.getNativeData(ENTITY_HEADERS) != null) {
            httpHeaders = (HttpHeaders) entityObj.getNativeData(ENTITY_HEADERS);
        } else {
            httpHeaders = new DefaultHttpHeaders();
            entityObj.addNativeData(ENTITY_HEADERS, httpHeaders);
        }
        return httpHeaders;
    }

    private static HttpHeaders getTrailerHeaders(ObjectValue entityObj) {
        HttpHeaders httpTrailerHeaders;
        if (entityObj.getNativeData(ENTITY_TRAILER_HEADERS) != null) {
            httpTrailerHeaders = (HttpHeaders) entityObj.getNativeData(ENTITY_TRAILER_HEADERS);
        } else {
            httpTrailerHeaders = new DefaultLastHttpContent().trailingHeaders();
            entityObj.addNativeData(ENTITY_TRAILER_HEADERS, httpTrailerHeaders);
        }
        return httpTrailerHeaders;
    }
}
