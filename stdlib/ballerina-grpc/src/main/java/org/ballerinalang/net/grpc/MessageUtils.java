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
package org.ballerinalang.net.grpc;

import io.grpc.Metadata;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;

import java.util.Base64;


/**
 * Message Utils.
 */
public class MessageUtils {
    public static BValue[] getHeader(Context context, AbstractNativeFunction abstractNativeFunction) {
        String headerName = abstractNativeFunction.getStringArgument(context, 0);
        String headerValue = getHeaderValue(headerName);

        return abstractNativeFunction.getBValues(new BString(headerValue));
    }

    private static String getHeaderValue(String keyName) {
        String headerValue = null;
        if (MessageContext.isPresent()) {
            MessageContext messageContext = MessageContext.DATA_KEY.get();
            if (keyName.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                Metadata.Key<byte[]> key = Metadata.Key.of(keyName, Metadata.BINARY_BYTE_MARSHALLER);
                byte[] byteValues = messageContext.get(key);
                // Referred : https://stackoverflow
                // .com/questions/1536054/how-to-convert-byte-array-to-string-and-vice-versa
                // https://stackoverflow.com/questions/2418485/how-do-i-convert-a-byte-array-to-base64-in-java
                headerValue = byteValues != null ? Base64.getEncoder().encodeToString(byteValues) : null;
            } else {
                Metadata.Key<String> key = Metadata.Key.of(keyName, Metadata.ASCII_STRING_MARSHALLER);
                headerValue =  messageContext.get(key);
            }
        }
        return headerValue;
    }
}
