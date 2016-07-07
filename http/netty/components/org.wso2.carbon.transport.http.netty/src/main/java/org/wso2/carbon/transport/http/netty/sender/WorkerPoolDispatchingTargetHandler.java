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
import org.wso2.carbon.messaging.FaultHandler;
import org.wso2.carbon.messaging.exceptions.EndPointTimeOut;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportContextHolder;
import org.wso2.carbon.transport.http.netty.message.NettyCarbonMessage;

import java.util.concurrent.ExecutorService;

/**
 * A class that dispatches response to engine.
 */
public class WorkerPoolDispatchingTargetHandler extends TargetHandler {

    public WorkerPoolDispatchingTargetHandler(int timeoutSeconds) {
        super(timeoutSeconds);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse) {

            cMsg = setUpCarbonMessage(ctx, msg);
            if (NettyTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                NettyTransportContextHolder.getInstance().getHandlerExecutor().
                        executeAtTargetResponseReceiving(cMsg);
            }

            ExecutorService executorService = (ExecutorService) incomingMsg
                    .getProperty(org.wso2.carbon.transport.http.netty.common.Constants.EXECUTOR_WORKER_POOL);
            if (executorService != null) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.done(cMsg);
                    }
                });
            } else {
                LOG.error("Executor service is not registered to request may be listener "
                        + "configuration is wrong or incoming" + "request properties are modified incorrectly");
            }

        } else {
            if (cMsg != null) {
                if (msg instanceof LastHttpContent) {
                    HttpContent httpContent = (LastHttpContent) msg;
                    ((NettyCarbonMessage) cMsg).addHttpContent(httpContent);
                    cMsg.setEndOfMsgAdded(true);
                    if (NettyTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                        NettyTransportContextHolder.getInstance().getHandlerExecutor().
                                executeAtTargetResponseSending(cMsg);
                    }
                    targetChannel.setRequestWritten(false);
                    connectionManager.returnChannel(targetChannel);
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
            FaultHandler faultHandler = null;
            try {
                faultHandler = incomingMsg.getFaultHandlerStack().pop();
            } catch (Exception e) {
                LOG.debug("Cannot find registered fault handler");
                //expected
            }

            if (faultHandler != null) {
                faultHandler.handleFault("504", new EndPointTimeOut(payload), incomingMsg, callback);
                incomingMsg.getFaultHandlerStack().push(faultHandler);
            } else {
                callback.done(createErrorMessage(payload));
            }
        }
    }
}
