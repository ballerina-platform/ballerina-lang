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

import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BValueCreator;

import java.util.List;

import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_HEADERS;

/**
 * Get the Headers of the Message.
 *
 * @since 1.0.0
 */
public class GetAll {

    public static ArrayValue externGetAll(ObjectValue headerValues, String headerName) {
        HttpHeaders headers = headerValues != null ? (HttpHeaders) headerValues.getNativeData(MESSAGE_HEADERS) : null;
        List<String> headersList =  headers != null ? headers.getAll(headerName) : null;

        if (headersList != null) {
            String[] headerValue = new String[headersList.size()];
            headerValue = headers.getAll(headerName).toArray(headerValue);
            return (ArrayValue) BValueCreator.createArrayValue(headerValue);
        } else {
            return null;
        }
    }
}
