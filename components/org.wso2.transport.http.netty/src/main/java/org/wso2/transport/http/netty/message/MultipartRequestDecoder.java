/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.wso2.transport.http.netty.message;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpPostRequestDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Decode multipart request messages.
 */
public class MultipartRequestDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartRequestDecoder.class);

    private InterfaceHttpPostRequestDecoder nettyRequestDecoder;
    private HTTPCarbonMessage httpCarbonMessage;
    private List<HttpBodyPart> multiparts = new ArrayList<>();

    public MultipartRequestDecoder(HTTPCarbonMessage httpCarbonMessage) {
        this.httpCarbonMessage = httpCarbonMessage;
    }

    /**
     * Check whether the carbon message represent a multipart request.
     *
     * @return boolean indicating whether the carbon message contains multipart content
     */
    public boolean isMultipartRequest() {
        nettyRequestDecoder = new HttpPostRequestDecoder(Util.createHttpRequest(httpCarbonMessage));
        return nettyRequestDecoder.isMultipart();
    }

    /**
     * Parse the content of carbon message.
     *
     * @throws IOException when no body content is found
     */
    public void parseBody() throws IOException {
        boolean isReadAll = false;
        while (!isReadAll) {
            HttpContent httpContent = httpCarbonMessage.getHttpContent();
            if (httpContent == null) {
                resetPostRequestDecoder();
                throw new IOException("No content was found to decode!");
            } else {
                nettyRequestDecoder = nettyRequestDecoder.offer(httpContent);
                readChunkByChunk();
                if (httpContent instanceof LastHttpContent) {
                    resetPostRequestDecoder();
                    isReadAll = true;
                } else {
                    isReadAll = false;
                }
            }
        }
    }

    /**
     * Read data chunk by chunk and process them.
     */
    private void readChunkByChunk() {
        try {
            while (nettyRequestDecoder.hasNext()) {
                InterfaceHttpData data = nettyRequestDecoder.next();
                if (data != null) {
                    try {
                        processChunk(data);
                    } finally {
                        data.release();
                    }
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e) {
            LOG.debug("EndOfDataDecoderException occurred since there's no more data to decode but that's fine");
        }
    }

    /**
     * Construct body parts from chunks.
     *
     * @param data Data object that needs to be decoded
     */
    private void processChunk(InterfaceHttpData data) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Multipart HTTP Data Name: {}, Type: {}", data.getName(), data.getHttpDataType());
        }
        HttpBodyPart bodyPart = null;
        switch (data.getHttpDataType()) {
            case Attribute:
                Attribute attribute = (Attribute) data;
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Attribute content size: {}", attribute.getByteBuf().readableBytes());
                    }
                    bodyPart = new HttpBodyPart(attribute.getName(), attribute.get(), Constants.TEXT_PLAIN,
                            attribute.getByteBuf().readableBytes());
                } catch (IOException e) {
                    LOG.error("Unable to read attribute content", e);
                }
                break;
            case FileUpload:
                FileUpload fileUpload = (FileUpload) data;
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Fileupload size: {}", fileUpload.getByteBuf().readableBytes());
                    }
                    bodyPart = new HttpBodyPart(fileUpload.getName(), fileUpload.getFilename(), fileUpload.get(),
                            fileUpload.getContentType(), fileUpload.getByteBuf().readableBytes());
                } catch (IOException e) {
                    LOG.error("Unable to read fileupload content", e);
                }
                break;
            default:
                LOG.warn("Received unknown attribute type.");
                break;
        }
        multiparts.add(bodyPart);
    }

    /**
     * Reset request decoder.
     */
    private void resetPostRequestDecoder() {
        nettyRequestDecoder.destroy();
        nettyRequestDecoder = null;
    }

    /**
     * Get a list of body parts.
     *
     * @return a list of multiparts
     */
    public List<HttpBodyPart> getMultiparts() {
        return multiparts;
    }
}
