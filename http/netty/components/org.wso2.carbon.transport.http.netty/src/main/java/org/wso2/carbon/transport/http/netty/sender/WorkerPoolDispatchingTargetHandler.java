/*
 * Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.sender;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.FaultHandler;
import org.wso2.carbon.messaging.exceptions.EndPointTimeOut;
import org.wso2.carbon.transport.http.netty.NettyCarbonMessage;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportContextHolder;

import java.util.concurrent.ExecutorService;

/**
 * A class that dispatches response to engine.
 */
public class WorkerPoolDispatchingTargetHandler extends TargetHandler {

    private SenderConfiguration senderConfiguration;

    public WorkerPoolDispatchingTargetHandler(int timeoutSeconds, SenderConfiguration senderConfiguration) {
        super(timeoutSeconds);
        this.senderConfiguration = senderConfiguration;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse) {
            cMsg = setUpCarbonMessage(ctx, msg);

            if (cMsg.getHeaders().get(Constants.HTTP_CONTENT_LENGTH) != null
                || cMsg.getHeaders().get(Constants.HTTP_TRANSFER_ENCODING) != null) {
                ExecutorService executorService = senderConfiguration.getNettyHandlerExecutorService();
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.done(cMsg);
                    }
                });

            }
        } else {
            if (cMsg != null) {
                if (msg instanceof LastHttpContent) {
                    HttpContent httpContent = (LastHttpContent) msg;
                    ((NettyCarbonMessage) cMsg).addHttpContent(httpContent);
                    NettyTransportContextHolder.getInstance().getHandlerExecutor().executeAtTargetResponseSending(cMsg);
                    targetChannel.setRequestWritten(false);
                    connectionManager.returnChannel(targetChannel);

                    if (cMsg.getHeaders().get(Constants.HTTP_CONTENT_LENGTH) == null
                        && cMsg.getHeaders().get(Constants.HTTP_TRANSFER_ENCODING) == null) {
                        cMsg.getHeaders().put(Constants.HTTP_CONTENT_LENGTH,
                                              String.valueOf(((NettyCarbonMessage) cMsg).getMessageBodyLength()));
                        ExecutorService executorService = senderConfiguration.getNettyHandlerExecutorService();
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                callback.done(cMsg);
                            }
                        });
                    }
                } else {
                    HttpContent httpContent = (DefaultHttpContent) msg;
                    ((NettyCarbonMessage) cMsg).addHttpContent(httpContent);
                }
            }
        }
    }

    @Override
    protected void readTimedOut(ChannelHandlerContext ctx) {
        ctx.channel().close();

        if (targetChannel.isRequestWritten()) {
            String payload = "<errorMessage>" + "ReadTimeoutException occurred for endpoint " + targetChannel.
                       getHttpRoute().toString() + "</errorMessage>";
            FaultHandler faultHandler = incomingMsg.getFaultHandlerStack().pop();

            if (faultHandler != null) {
                faultHandler.handleFault("504", new EndPointTimeOut(payload), incomingMsg, callback);
                incomingMsg.getFaultHandlerStack().push(faultHandler);
            } else {
                callback.done(createErrorMessage(payload));
            }
        }
    }
}
