/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.grpc.nativeimpl.headers;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BValueCreator;

import java.util.List;

import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_HEADERS;

/**
 * Utility methods represents actions to the custom metadata.
 *
 * @since 1.0.0
 */
public class FunctionUtils {

    /**
     * Extern function to add new entries to headers.
     *
     * @param headerValues header instance.
     * @param headerName   header name.
     * @param headerValue  header value.
     */
    public static void externAddEntry(final ObjectValue headerValues, final BString headerName,
            final BString headerValue) {
        HttpHeaders headers = (HttpHeaders) headerValues.getNativeData(MESSAGE_HEADERS);
        // Only initialize headers if not yet initialized
        headers = headers != null ? headers : new DefaultHttpHeaders();
        headers.add(headerName.getValue(), headerValue.getValue());
        headerValues.addNativeData(MESSAGE_HEADERS, headers);
    }

    /**
     * Extern function to check whether header key exists.
     *
     * @param headerValues header instance.
     * @param headerName   header name.
     * @return True if header value exists for the given name, False otherwise.
     */
    public static boolean externExists(final ObjectValue headerValues, final BString headerName) {
        final HttpHeaders headers = headerValues != null ? 
                (HttpHeaders) headerValues.getNativeData(MESSAGE_HEADERS) : null;
        boolean isExist = false;
        if (headers != null) {
            isExist = headers.contains(headerName.getValue());
        }
        return isExist;
    }

    /**
     * Extern function to get header value with the specified name. If there are
     * more than one values for the specified name, the first value is returned.
     *
     * @param headerValues header instance.
     * @param headerName   header name.
     * @return Header value if it exists, else returns nil.
     */
    public static Object externGet(final ObjectValue headerValues, final BString headerName) {
        final HttpHeaders headers = headerValues != null ? 
                (HttpHeaders) headerValues.getNativeData(MESSAGE_HEADERS) : null;
        return headers != null ? StringUtils.fromString(headers.get(headerName.getValue())) : null;
    }

    /**
     * Extern function to get the values of headers with the specified name.
     *
     * @param headerValues header instance.
     * @param headerName   header name.
     * @return A array of header values. returns nil if no values are found
     */
    public static ArrayValue externGetAll(final ObjectValue headerValues, final BString headerName) {
        final HttpHeaders headers = headerValues != null ? 
                (HttpHeaders) headerValues.getNativeData(MESSAGE_HEADERS) : null;
        final List<String> headersList = headers != null ? headers.getAll(headerName.getValue()) : null;

        if (headersList != null) {
            BString[] headerValue = headersList.stream().map(s -> StringUtils.fromString(s)).toArray(BString[]::new);
            return (ArrayValue) BValueCreator.createArrayValue(headerValue);
        } else {
            return null;
        }
    }

    /**
     * Extern function to remove the header with the specified name.
     *
     * @param headerValues header instance.
     * @param headerName   header name.
     */
    public static void externRemove(final ObjectValue headerValues, final BString headerName) {
        final HttpHeaders headers = headerValues != null ? 
                (HttpHeaders) headerValues.getNativeData(MESSAGE_HEADERS) : null;
        if (headers != null) {
            headers.remove(headerName.getValue());
        }
    }

    /**
     * Removes all headers from the message.
     *
     * @param headerValues header instance.
     */
    public static void externRemoveAll(final ObjectValue headerValues) {
        final HttpHeaders headers = headerValues != null ? 
                (HttpHeaders) headerValues.getNativeData(MESSAGE_HEADERS) : null;
        if (headers != null) {
            headers.clear();
        }
    }

    /**
     * Sets a header with the specified name and value. If there is an existing
     * header with the same name, it is removed.
     * 
     * @param headerValues header instance.
     * @param headerName   header name.
     * @param headerValue  header value.
     */
    public static void externSetEntry(final ObjectValue headerValues, final BString headerName,
            final BString headerValue) {
        HttpHeaders headers = (HttpHeaders) headerValues.getNativeData(MESSAGE_HEADERS);

        // Only initialize headers if not yet initialized
        headers = headers != null ? headers : new DefaultHttpHeaders();
        headers.set(headerName.getValue(), headerValue.getValue());
        headerValues.addNativeData(MESSAGE_HEADERS, headers);
    }
}
