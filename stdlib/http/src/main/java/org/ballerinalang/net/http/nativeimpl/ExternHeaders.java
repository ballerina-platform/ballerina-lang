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

package org.ballerinalang.net.http.nativeimpl;

import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.mime.util.MimeUtil;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.ballerinalang.mime.util.MimeConstants.HEADER_NOT_FOUND_ERROR;
import static org.ballerinalang.mime.util.MimeConstants.INVALID_HEADER_OPERATION_ERROR;
import static org.ballerinalang.net.http.HttpConstants.HTTP_HEADERS;
import static org.ballerinalang.net.http.HttpConstants.HTTP_TRAILER_HEADERS;
import static org.ballerinalang.net.http.HttpConstants.LEADING_HEADER;

/**
 * Utilities related to HTTP request/response headers.
 *
 * @since 1.1.0
 */
public class ExternHeaders {

    public static void addHeader(BObject messageObj, BString headerName, BString headerValue, Object position) {
        if (headerName == null || headerValue == null) {
            return;
        }
        try {
            getOrCreateHeadersBasedOnPosition(messageObj, position).add(headerName.getValue(), headerValue.getValue());
        } catch (IllegalArgumentException ex) {
            throw MimeUtil.createError(INVALID_HEADER_OPERATION_ERROR, ex.getMessage());
        }
    }

    public static BString getHeader(BObject messageObj, BString headerName, Object position) {
        HttpHeaders httpHeaders = getHeadersBasedOnPosition(messageObj, position);
        if (httpHeaders == null) {
            throw MimeUtil.createError(HEADER_NOT_FOUND_ERROR, "Http header does not exist");
        }
        if (httpHeaders.get(headerName.getValue()) != null) {
            return BStringUtils.fromString(httpHeaders.get(headerName.getValue()));
        } else {
            throw MimeUtil.createError(HEADER_NOT_FOUND_ERROR, "Http header does not exist");
        }
    }

    public static BArray getHeaderNames(BObject messageObj, Object position) {
        HttpHeaders httpHeaders = getHeadersBasedOnPosition(messageObj, position);
        if (httpHeaders == null || httpHeaders.isEmpty()) {
            return BValueCreator.createArrayValue(new BString[0]);
        }
        Set<String> distinctNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        distinctNames.addAll(httpHeaders.names());
        return BValueCreator.createArrayValue(BStringUtils.fromStringArray(distinctNames.toArray(new String[0])));
    }

    public static BArray getHeaders(BObject messageObj, BString headerName, Object position) {
        HttpHeaders httpHeaders = getHeadersBasedOnPosition(messageObj, position);
        if (httpHeaders == null) {
            throw MimeUtil.createError(HEADER_NOT_FOUND_ERROR, "Http header does not exist");
        }
        List<String> headerValueList = httpHeaders.getAll(headerName.getValue());
        if (headerValueList == null) {
            throw MimeUtil.createError(HEADER_NOT_FOUND_ERROR, "Http header does not exist");
        }
        return BValueCreator.createArrayValue(BStringUtils.fromStringArray(headerValueList.toArray(new String[0])));
    }

    public static boolean hasHeader(BObject messageObj, BString headerName, Object position) {
        HttpHeaders httpHeaders = getHeadersBasedOnPosition(messageObj, position);
        if (httpHeaders == null) {
            return false;
        }
        List<String> headerValueList = httpHeaders.getAll(headerName.getValue());
        return headerValueList != null && !headerValueList.isEmpty();
    }

    public static void removeAllHeaders(BObject messageObj, Object position) {
        HttpHeaders httpHeaders = getHeadersBasedOnPosition(messageObj, position);
        if (httpHeaders != null) {
            httpHeaders.clear();
        }
    }

    public static void removeHeader(BObject messageObj, BString headerName, Object position) {
        HttpHeaders httpHeaders = getHeadersBasedOnPosition(messageObj, position);
        if (httpHeaders != null) {
            httpHeaders.remove(headerName.getValue());
        }
    }

    public static void setHeader(BObject messageObj, BString headerName, BString headerValue, Object position) {
        if (headerName == null || headerValue == null) {
            return;
        }
        try {
            getOrCreateHeadersBasedOnPosition(messageObj, position).set(headerName.getValue(), headerValue.getValue());
        } catch (IllegalArgumentException ex) {
            throw MimeUtil.createError(INVALID_HEADER_OPERATION_ERROR, ex.getMessage());
        }
    }

    private static HttpHeaders getHeadersBasedOnPosition(BObject messageObj, Object position) {
        return position.equals(BStringUtils.fromString(LEADING_HEADER)) ?
                (HttpHeaders) messageObj.getNativeData(HTTP_HEADERS) :
                (HttpHeaders) messageObj.getNativeData(HTTP_TRAILER_HEADERS);
    }

    private static HttpHeaders getOrCreateHeadersBasedOnPosition(BObject messageObj, Object position) {
        return position.equals(BStringUtils.fromString(LEADING_HEADER)) ?
                getHeaders(messageObj) : getTrailerHeaders(messageObj);
    }

    private static HttpHeaders getHeaders(BObject messageObj) {
        HttpHeaders httpHeaders;
        if (messageObj.getNativeData(HTTP_HEADERS) != null) {
            httpHeaders = (HttpHeaders) messageObj.getNativeData(HTTP_HEADERS);
        } else {
            httpHeaders = new DefaultHttpHeaders();
            messageObj.addNativeData(HTTP_HEADERS, httpHeaders);
        }
        return httpHeaders;
    }

    private static HttpHeaders getTrailerHeaders(BObject messageObj) {
        HttpHeaders httpTrailerHeaders;
        if (messageObj.getNativeData(HTTP_TRAILER_HEADERS) != null) {
            httpTrailerHeaders = (HttpHeaders) messageObj.getNativeData(HTTP_TRAILER_HEADERS);
        } else {
            httpTrailerHeaders = new DefaultLastHttpContent().trailingHeaders();
            messageObj.addNativeData(HTTP_TRAILER_HEADERS, httpTrailerHeaders);
        }
        return httpTrailerHeaders;
    }
}
