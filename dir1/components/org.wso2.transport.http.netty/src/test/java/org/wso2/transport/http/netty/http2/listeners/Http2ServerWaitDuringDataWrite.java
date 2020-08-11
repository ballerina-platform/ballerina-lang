/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.http2.listeners;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sleeps during response data write.
 */
public class Http2ServerWaitDuringDataWrite implements HttpConnectorListener {
    private static final Logger LOG = LoggerFactory.getLogger(Http2ServerWaitDuringDataWrite.class);
    private long waitTimeInMillis;
    private ExecutorService executor = Executors.newScheduledThreadPool(2);

    public Http2ServerWaitDuringDataWrite(long waitTimeInMillis) {
        this.waitTimeInMillis = waitTimeInMillis;
    }

    @Override
    public void onMessage(HttpCarbonMessage httpRequest) {
        executor.execute(() -> {
            try {
                HttpCarbonMessage httpResponse = new HttpCarbonResponse(
                        new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
                httpResponse.setHeader(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.KEEP_ALIVE.toString());
                httpResponse.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), Constants.TEXT_PLAIN);
                httpResponse.setHttpStatusCode(HttpResponseStatus.OK.code());

                byte[] data1 = "Slow content data part1".getBytes(StandardCharsets.UTF_8);
                ByteBuffer byteBuff1 = ByteBuffer.wrap(data1);
                httpResponse.addHttpContent(new DefaultHttpContent(Unpooled.wrappedBuffer(byteBuff1)));
                HttpResponseFuture responseFuture = httpRequest.respond(httpResponse);
                //Wait a considerable about of time to simulate server timeout during 'SendingEntityBody' state.
                Thread.sleep(waitTimeInMillis);
                responseFuture.sync();
            } catch (ServerConnectorException e) {
                LOG.error("Error occurred while processing message: " + e.getMessage());
            } catch (InterruptedException e) {
                LOG.error("InterruptedException occurred while processing message: " + e.getMessage());
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.error("Error occurred in Http2ServerWaitDuringDataWrite: " + throwable.getMessage());
    }
}
