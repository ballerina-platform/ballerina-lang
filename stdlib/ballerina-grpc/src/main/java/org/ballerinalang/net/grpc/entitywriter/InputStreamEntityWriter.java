/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.net.grpc.entitywriter;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import org.ballerinalang.net.grpc.OutboundMessage;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

/**
 * EntityWriter for entity of type InputStream.
 */
public class InputStreamEntityWriter implements EntityWriter<InputStream> {

    /** maximum buffer to be read is 16 KB. */
    private static final int MAX_BUFFER_LENGTH = 16384;

    /**
     * Supported entity type.
     */
    @Override
    public Class<InputStream> getType() {
        return InputStream.class;
    }

    /**
     * Write the entity to the carbon message.
     */
    @Override
    public void writeData(HTTPCarbonMessage carbonMessage, InputStream ipStream,
                          String mediaType, int chunkSize, HTTPCarbonMessage responder) {
        try {
            if (chunkSize == OutboundMessage.NO_CHUNK || chunkSize == OutboundMessage.DEFAULT_CHUNK_SIZE) {
                chunkSize = MAX_BUFFER_LENGTH;
            }
//            carbonMessage.setHeader(HttpHeaderNames.TRANSFER_ENCODING.toString(), CHUNKED);
//            carbonMessage.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), mediaType);
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    responder.respond(carbonMessage);
                } catch (ServerConnectorException e) {
                    throw new RuntimeException("Error while sending the response.", e);
                }
            });
            byte[] data = new byte[chunkSize];
            int len;
            while ((len = ipStream.read(data)) != -1) {
                carbonMessage.addHttpContent(new DefaultHttpContent(Unpooled.wrappedBuffer(ByteBuffer.wrap(data, 0,
                        len))));
                data = new byte[chunkSize];
            }

        } catch (IOException e) {
            throw new RuntimeException("Error occurred while reading from InputStream", e);
        } finally {
            if (ipStream != null) {
                try {
                    ipStream.close();
                } catch (IOException ignore) {

                }
            }
        }
    }
}
