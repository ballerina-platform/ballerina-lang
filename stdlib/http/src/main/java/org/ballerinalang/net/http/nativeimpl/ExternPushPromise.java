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

import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.message.Http2PushPromise;

import java.util.Set;

/**
 * Utilities related to push promises.
 *
 * @since 1.1.0
 */
public class ExternPushPromise {

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
        return new ArrayValueImpl(httpHeaderNames.toArray(new String[httpHeaderNames.size()]));
    }

    public static ArrayValue getHeaders(ObjectValue pushPromiseObj, String headerName) {
        Http2PushPromise http2PushPromise =
                HttpUtil.getPushPromise(pushPromiseObj, HttpUtil.createHttpPushPromise(pushPromiseObj));
        return new ArrayValueImpl(http2PushPromise.getHeaders(headerName));
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
