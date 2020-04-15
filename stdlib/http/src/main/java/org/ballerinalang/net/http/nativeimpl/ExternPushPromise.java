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

package org.ballerinalang.net.http.nativeimpl;

import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.HandleValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.message.Http2PushPromise;

import java.util.Set;
import java.util.TreeSet;

/**
 * Utilities related to push promises.
 *
 * @since 1.1.0
 */
public class ExternPushPromise {

    private static final BArrayType bArrayType = new BArrayType(BTypes.typeHandle);

    public static void addHeader(ObjectValue pushPromiseObj, String headerName, String headerValue) {
        Http2PushPromise http2PushPromise =
                HttpUtil.getPushPromise(pushPromiseObj, HttpUtil.createHttpPushPromise(pushPromiseObj));
        http2PushPromise.addHeader(headerName, headerValue);
    }

    public static String getHeader(ObjectValue pushPromiseObj, String headerName) {
        Http2PushPromise http2PushPromise =
                HttpUtil.getPushPromise(pushPromiseObj, HttpUtil.createHttpPushPromise(pushPromiseObj));
        return http2PushPromise.getHeader(headerName);
    }

    public static ArrayValue getHeaderNames(ObjectValue pushPromiseObj) {
        Http2PushPromise http2PushPromise =
                HttpUtil.getPushPromise(pushPromiseObj, HttpUtil.createHttpPushPromise(pushPromiseObj));
        Set<String> httpHeaderNames = http2PushPromise.getHttpRequest().headers().names();
        if (httpHeaderNames == null || httpHeaderNames.isEmpty()) {
            return (ArrayValue) BValueCreator.createArrayValue(new HandleValue[0], bArrayType);
        }
        int i = 0;
        Set<String> distinctNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        distinctNames.addAll(httpHeaderNames);
        HandleValue[] handleValues = new HandleValue[distinctNames.size()];
        for (String headerName : distinctNames) {
            handleValues[i++] = new HandleValue(headerName);
        }
        return (ArrayValue) BValueCreator.createArrayValue(handleValues, bArrayType);
    }

    public static ArrayValue getHeaders(ObjectValue pushPromiseObj, String headerName) {
        Http2PushPromise http2PushPromise =
                HttpUtil.getPushPromise(pushPromiseObj, HttpUtil.createHttpPushPromise(pushPromiseObj));
        String[] headers = http2PushPromise.getHeaders(headerName);
        int i = 0;
        ArrayValue stringArray = (ArrayValue) BValueCreator.createArrayValue(bArrayType);
        for (String header : headers) {
            stringArray.add(i, new HandleValue(header));
            i++;
        }
        return stringArray;
    }

    public static boolean hasHeader(ObjectValue pushPromiseObj, String headerName) {
        Http2PushPromise http2PushPromise =
                HttpUtil.getPushPromise(pushPromiseObj, HttpUtil.createHttpPushPromise(pushPromiseObj));
        return http2PushPromise.getHeader(headerName) != null;
    }

    public static void removeAllHeaders(ObjectValue pushPromiseObj) {
        Http2PushPromise http2PushPromise =
                HttpUtil.getPushPromise(pushPromiseObj, HttpUtil.createHttpPushPromise(pushPromiseObj));
        http2PushPromise.removeAllHeaders();
    }

    public static void removeHeader(ObjectValue pushPromiseObj, String headerName) {
        Http2PushPromise http2PushPromise =
                HttpUtil.getPushPromise(pushPromiseObj, HttpUtil.createHttpPushPromise(pushPromiseObj));
        http2PushPromise.removeHeader(headerName);
    }

    public static void setHeader(ObjectValue pushPromiseObj, String headerName, String headerValue) {
        Http2PushPromise http2PushPromise =
                HttpUtil.getPushPromise(pushPromiseObj, HttpUtil.createHttpPushPromise(pushPromiseObj));
        http2PushPromise.setHeader(headerName, headerValue);
    }
}
