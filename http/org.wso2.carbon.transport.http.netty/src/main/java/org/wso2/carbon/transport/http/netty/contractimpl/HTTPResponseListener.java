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

package org.wso2.carbon.transport.http.netty.contractimpl;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.RequestDataHolder;
import org.wso2.carbon.transport.http.netty.listener.ResponseContentWriter;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Get executed when the response is available.
 */
public class HTTPResponseListener implements HTTPConnectorListener {

    private ChannelHandlerContext ctx;
    private RequestDataHolder requestDataHolder;

    public HTTPResponseListener(ChannelHandlerContext channelHandlerContext, CarbonMessage requestMsg) {
        this.ctx = channelHandlerContext;
        requestDataHolder = new RequestDataHolder(requestMsg);
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpMessage) {
        boolean connectionCloseAfterResponse = shouldConnectionClose(httpMessage);

        Util.prepareBuiltMessageForTransfer(httpMessage);
        Util.setupTransferEncodingForResponse(httpMessage, requestDataHolder);

        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceResponseReceiving(httpMessage);
        }
        final HttpResponse response = Util.createHttpResponse(httpMessage, connectionCloseAfterResponse);

        ctx.write(response);

        if (!httpMessage.isBufferContent()) {
            httpMessage.setWriter(new ResponseContentWriter(ctx));
        } else {
            HTTPCarbonMessage nettyCMsg = httpMessage;
            while (true) {
                if (nettyCMsg.isEndOfMsgAdded() && nettyCMsg.isEmpty()) {
                    ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                    if (connectionCloseAfterResponse) {
                        future.addListener(ChannelFutureListener.CLOSE);
                    }
                    break;
                }
                HttpContent httpContent = nettyCMsg.getHttpContent();
                if (httpContent instanceof LastHttpContent) {
                    ChannelFuture future = ctx.writeAndFlush(httpContent);
                    if (connectionCloseAfterResponse) {
                        future.addListener(ChannelFutureListener.CLOSE);
                    }
                    if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                        HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                                executeAtSourceResponseSending(httpMessage);
                    }
                    break;
                }
                ctx.write(httpContent);
            }

            // TODO: Remove this once the testing is completed.
//            else if (httpMessage instanceof DefaultCarbonMessage) {
//                DefaultCarbonMessage defaultCMsg = (DefaultCarbonMessage) httpMessage;
//                if (defaultCMsg.isEndOfMsgAdded() && defaultCMsg.isEmpty()) {
//                    ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
//                    if (connectionCloseAfterResponse) {
//                        future.addListener(ChannelFutureListener.CLOSE);
//                    }
//                    return;
//                }
//                while (true) {
//                    ByteBuffer byteBuffer = defaultCMsg.getMessageBody();
//                    ByteBuf bbuf = Unpooled.wrappedBuffer(byteBuffer);
//                    DefaultHttpContent httpContent = new DefaultHttpContent(bbuf);
//                    ctx.write(httpContent);
//                    if (defaultCMsg.isEndOfMsgAdded() && defaultCMsg.isEmpty()) {
//                        ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
//                        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
//                            HTTPTransportContextHolder.getInstance().getHandlerExecutor().
//                                    executeAtSourceResponseSending(httpMessage);
//                        }
//                        if (connectionCloseAfterResponse) {
//                            future.addListener(ChannelFutureListener.CLOSE);
//                        }
//                        break;
//                    }
//                }
//            }
        }
    }

    // Decides whether to close the connection after sending the response
    private boolean shouldConnectionClose(CarbonMessage responseMsg) {
        String responseConnectionHeader = responseMsg.getHeader(Constants.HTTP_CONNECTION);
        String requestConnectionHeader = requestDataHolder.getConnectionHeader();
        if ((responseConnectionHeader != null &&
                Constants.CONNECTION_CLOSE.equalsIgnoreCase(responseConnectionHeader))
                || (requestConnectionHeader != null &&
                Constants.CONNECTION_CLOSE.equalsIgnoreCase(requestConnectionHeader))) {
            return true;
        }
        return false;
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
