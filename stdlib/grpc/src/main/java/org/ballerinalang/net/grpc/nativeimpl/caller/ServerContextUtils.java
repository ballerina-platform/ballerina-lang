/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.grpc.nativeimpl.caller;

import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.jvm.values.ObjectValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.ballerinalang.net.grpc.GrpcConstants.MESSAGE_HEADERS;

/**
 * Utility methods for the server context.
 *
 */
public class ServerContextUtils {
    /**
     * Extern function to check deadline exceeded or not.
     *
     * @param headerValues header values.
     * @return True if caller has terminated the connection, false otherwise.
     */
    public static boolean externIsCancelled(ObjectValue headerValues) {
        HttpHeaders headers = null;
        if (headerValues != null) {
            headers = (HttpHeaders) ((ObjectValue) headerValues).getNativeData(MESSAGE_HEADERS);
            if (!headers.get("DEADLINE").isEmpty()) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                LocalDateTime localDateTime = LocalDateTime.parse(headers.get("DEADLINE"), dateTimeFormatter);
                if (localDateTime.isBefore(LocalDateTime.now())) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }
}
