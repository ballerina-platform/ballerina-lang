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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import javax.ws.rs.core.StreamingOutput;

/**
 * EntityWriter for entity of type {@link StreamingOutput}.
 */
public class StreamingOutputEntityWriter implements EntityWriter<StreamingOutput> {

    /**
     * Supported entity type.
     */
    @Override
    public Class<StreamingOutput> getType() {
        return StreamingOutput.class;
    }

    /**
     * Write the entity to the carbon message.
     */
    @Override
    public void writeData(HTTPCarbonMessage carbonMessage, StreamingOutput output,
                          String mediaType, int chunkSize, HTTPCarbonMessage responder) {
        try {
            carbonMessage.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), mediaType);
            carbonMessage.setHeader(HttpHeaderNames.TRANSFER_ENCODING.toString(), CHUNKED);
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    responder.respond(carbonMessage);
                } catch (ServerConnectorException e) {
                    throw new RuntimeException("Error while sending the response.", e);
                }
            });
            OutputStream outputStream = new HttpMessageDataStreamer(carbonMessage).getOutputStream();
            output.write(outputStream);
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while streaming output", e);
        }
    }
}
