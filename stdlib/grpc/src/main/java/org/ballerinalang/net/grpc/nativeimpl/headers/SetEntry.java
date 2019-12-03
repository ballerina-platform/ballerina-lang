/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc.nativeimpl.headers;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.values.ObjectValue;

import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_HEADERS;

/**
 * Set custom Header to the Message.
 *
 * @since 1.0.0
 */
public class SetEntry {

    public static void externSetEntry(ObjectValue headerValues, String headerName, String headerValue) {
        HttpHeaders headers = (HttpHeaders) headerValues.getNativeData(MESSAGE_HEADERS);

        // Only initialize headers if not yet initialized
        headers = headers != null ? headers : new DefaultHttpHeaders();
        headers.set(headerName, headerValue);
        headerValues.addNativeData(MESSAGE_HEADERS, headers);
    }
}
