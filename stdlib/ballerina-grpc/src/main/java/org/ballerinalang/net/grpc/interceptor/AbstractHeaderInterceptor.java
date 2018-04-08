/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.net.grpc.interceptor;

import io.grpc.Metadata;
import org.ballerinalang.net.grpc.MessageContext;

/**
 * Abstract interceptor to handle common header read/write methods.
 *
 * @since 1.0.0
 */
public abstract class AbstractHeaderInterceptor {

    public Metadata assignMessageHeaders(Metadata headers) {

        if (MessageContext.isPresent()) {
            MessageContext messageContext = MessageContext.DATA_KEY.get();
            for (String headerKey : messageContext.keys()) {
                if (headerKey.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                    Metadata.Key<byte[]> key = Metadata.Key.of(headerKey, Metadata.BINARY_BYTE_MARSHALLER);
                    byte[] byteValues = messageContext.get(key);
                    headers.put(key, byteValues);
                } else {
                    Metadata.Key<String> key = Metadata.Key.of(headerKey, Metadata.ASCII_STRING_MARSHALLER);
                    String headerValue = messageContext.get(key);
                    headers.put(key, headerValue);
                }
            }
        }
        return headers;
    }

    public MessageContext readIncomingHeaders(Metadata headers) {

        MessageContext ctx = MessageContext.DATA_KEY.get();
        ctx = ctx != null ? ctx : new MessageContext();

        boolean found = false;
        for (String keyName : headers.keys()) {
            if (keyName.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                Metadata.Key<byte[]> key = Metadata.Key.of(keyName, Metadata.BINARY_BYTE_MARSHALLER);
                Iterable<byte[]> values = headers.getAll(key);
                if (values == null) {
                    continue;
                }
                // remove all header details of the same key
                if (ctx.containsKey(key)) {
                    ctx.removeAll(key);
                }

                for (byte[] value : values) {
                    ctx.put(key, value);
                }
            } else {
                Metadata.Key<String> key = Metadata.Key.of(keyName, Metadata.ASCII_STRING_MARSHALLER);
                Iterable<String> values = headers.getAll(key);
                if (values == null) {
                    continue;
                }
                // remove all header details of the same key
                if (ctx.containsKey(key)) {
                    ctx.removeAll(key);
                }

                for (String value : values) {
                    ctx.put(key, value);
                }
            }

            found = true;
        }
        return found ? ctx : null;
    }
}
