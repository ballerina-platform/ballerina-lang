/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.transport.http.netty.encoding;

import com.google.common.io.ByteStreams;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HTTP connector Listener for Content reading.
 */
public class ContentReadingListener implements HttpConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(ContentReadingListener.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onMessage(HttpCarbonMessage httpMessage) {
        executor.execute(() -> {
            try {
                InputStream inputStream = new HttpMessageDataStreamer(httpMessage).getInputStream();
                String response = new String(ByteStreams.toByteArray(inputStream), Charset.defaultCharset());
                String alteredContent = "Altered " + response + " content";

                HttpCarbonMessage newMsg = httpMessage.cloneCarbonMessageWithOutData();
                newMsg.addHttpContent(new DefaultLastHttpContent(
                        Unpooled.wrappedBuffer(alteredContent.getBytes(Charset.defaultCharset()))));
                newMsg.completeMessage();

                httpMessage.respond(newMsg);
            } catch (IOException | ServerConnectorException e) {
                LOG.error("Error occurred during message processing ", e);
            }

        });
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
