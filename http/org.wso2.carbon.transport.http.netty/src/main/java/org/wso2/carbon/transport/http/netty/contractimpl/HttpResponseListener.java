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
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.RequestDataHolder;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Get executed when the response is available.
 */
public class HttpResponseListener implements HttpConnectorListener {

    private ChannelHandlerContext sourceContext;
    private RequestDataHolder requestDataHolder;

    public HttpResponseListener(ChannelHandlerContext channelHandlerContext, HTTPCarbonMessage requestMsg) {
        this.sourceContext = channelHandlerContext;
        this.requestDataHolder = new RequestDataHolder(requestMsg);
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

        sourceContext.write(response);

        while (true) {
            if (httpMessage.isEndOfMsgAdded() && httpMessage.isEmpty()) {
                ChannelFuture future = sourceContext.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                if (connectionCloseAfterResponse) {
                    future.addListener(ChannelFutureListener.CLOSE);
                }
                break;
            }
            HttpContent httpContent = httpMessage.getHttpContent();
            if (httpContent instanceof LastHttpContent) {
                ChannelFuture future = sourceContext.writeAndFlush(httpContent);
                if (connectionCloseAfterResponse) {
                    future.addListener(ChannelFutureListener.CLOSE);
                }
                if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                    HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                            executeAtSourceResponseSending(httpMessage);
                }
                break;
            }
            sourceContext.write(httpContent);
        }
    }

    // Decides whether to close the connection after sending the response
    private boolean shouldConnectionClose(HTTPCarbonMessage responseMsg) {
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
