/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.transport.http.netty.sender;

import com.lmax.disruptor.RingBuffer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.*;
import org.wso2.carbon.transport.http.netty.NettyCarbonMessage;
/*import org.wso2.carbon.transport.http.netty.common.Constants;*/
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.common.disruptor.publisher.CarbonEventPublisher;
import org.wso2.carbon.transport.http.netty.exception.EndpointTimeOutException;
/*
import org.wso2.carbon.transport.http.netty.latency.metrics.ConnectionMetricsHolder;
import org.wso2.carbon.transport.http.netty.latency.metrics.RequestMetricsHolder;
import org.wso2.carbon.transport.http.netty.latency.metrics.ResponseMetricsHolder;
*/
import org.wso2.carbon.transport.http.netty.internal.NettyTransportContextHolder;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * A class responsible for handling responses coming from BE.
 */
public class TargetHandler extends ReadTimeoutHandler {
    private static Logger log = LoggerFactory.getLogger(TargetHandler.class);

    private CarbonCallback callback;
    private RingBuffer ringBuffer;
    private CarbonMessage cMsg;
    private ConnectionManager connectionManager;
    private TargetChannel targetChannel;
    private CarbonMessage incomingMsg;

    public TargetHandler(int timeoutSeconds) {
        super(timeoutSeconds);
    }

    @Override public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @SuppressWarnings("unchecked") @Override public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof HttpResponse) {
            //TODO: RESPONSE_LIFE_TIMER
            NettyTransportContextHolder.getInstance().getInterceptor()
                    .engage(cMsg, EngagedLocation.SERVER_RESPONSE_READ_INITIATED);

            cMsg = new NettyCarbonMessage();
            cMsg.setProperty(Constants.PORT, ((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
            cMsg.setProperty(Constants.HOST, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
            cMsg.setProperty(Constants.DIRECTION, Constants.DIRECTION_RESPONSE);
            cMsg.setProperty(Constants.CALL_BACK, callback);
            HttpResponse httpResponse = (HttpResponse) msg;

            cMsg.setProperty(Constants.HTTP_STATUS_CODE, httpResponse.getStatus().code());
            //TODO: RESPONSE_HEADER_READ_TIMER);
            cMsg.setHeaders(Util.getHeaders(httpResponse));
            //TODO: RESPONSE_HEADER_READ_TIMER);
            NettyTransportContextHolder.getInstance().getInterceptor()
                    .engage(cMsg, EngagedLocation.SERVER_RESPONSE_READ_HEADERS_COMPLETED);
            ringBuffer.publishEvent(new CarbonEventPublisher(cMsg));
        } else {
            if (cMsg != null) {
                //TODO RESPONSE_BODY_READ_TIMER);
                if (msg instanceof LastHttpContent) {
                    //TODO: RESPONSE_BODY_READ_TIMER);
                    NettyTransportContextHolder.getInstance().getInterceptor()
                            .engage(cMsg, EngagedLocation.SERVER_RESPONSE_READ_BODY_COMPLETED);
                    HttpContent httpContent = (LastHttpContent) msg;
                    ((NettyCarbonMessage) cMsg).addHttpContent(httpContent);
                    targetChannel.setRequestWritten(false);
                    connectionManager.returnChannel(targetChannel);
                } else {
                    HttpContent httpContent = (DefaultHttpContent) msg;
                    ((NettyCarbonMessage) cMsg).addHttpContent(httpContent);
                }
            }
        }
    }

    @Override public void channelInactive(ChannelHandlerContext ctx) {
        // Set the client channel close metric
        //TODO Connection stopTimer
        NettyTransportContextHolder.getInstance().getInterceptor()
                .engage(cMsg, EngagedLocation.SERVER_CONNECTION_COMPLETED);
        log.debug("Target channel closed.");
    }

    public void setCallback(CarbonCallback callback) {
        this.callback = callback;
    }

    public void setRingBuffer(RingBuffer ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void setIncomingMsg(CarbonMessage incomingMsg) {
        this.incomingMsg = incomingMsg;
    }

    public void setTargetChannel(TargetChannel targetChannel) {
        this.targetChannel = targetChannel;
    }

    //TODO
    /*public void setClientConnectionMetricHolder(ConnectionMetricsHolder clientConnectionMetricHolder) {
        this.clientConnectionMetricHolder = clientConnectionMetricHolder;
    }

    public void setServerConnectionMetricHolder(ConnectionMetricsHolder serverConnectionMetricHolder) {
        this.serverConnectionMetricHolder = serverConnectionMetricHolder;
    }

    public void setClientRequestMetricsHolder(RequestMetricsHolder clientRequestMetricsHolder) {
        this.clientRequestMetricsHolder = clientRequestMetricsHolder;
    }

    public void setServerRequestMetricsHolder(RequestMetricsHolder serverRequestMetricsHolder) {
        this.serverRequestMetricsHolder = serverRequestMetricsHolder;
    }

    public void setClientResponseMetricsHolder(ResponseMetricsHolder clientResponseMetricsHolder) {
        this.clientResponseMetricsHolder = clientResponseMetricsHolder;
    }

    public void setServerResponseMetricsHolder(ResponseMetricsHolder serverResponseMetricsHolder) {
        this.serverResponseMetricsHolder = serverResponseMetricsHolder;
    }*/

    @Override protected void readTimedOut(ChannelHandlerContext ctx) {

        if (targetChannel.isRequestWritten()) {
            String payload = "<errorMessage>" + "ReadTimeoutException occurred for endpoint" + targetChannel.
                    getHttpRoute().toString() + "</errorMessage>";
            FaultHandler faultHandler = incomingMsg.getFaultHandlerStack().pop();
            if (faultHandler != null) {
                faultHandler.handleFault("504", new EndpointTimeOutException(payload), callback);
                incomingMsg.getFaultHandlerStack().push(faultHandler);
            } else {

                DefaultCarbonMessage response = new DefaultCarbonMessage();

                response.setStringMessageBody(payload);
                byte[] errorMessageBytes = payload.getBytes(Charset.defaultCharset());

                Map<String, String> transportHeaders = new HashMap<>();
                transportHeaders.put(Constants.HTTP_CONNECTION, Constants.KEEP_ALIVE);
                transportHeaders.put(Constants.HTTP_CONTENT_ENCODING, Constants.GZIP);

                transportHeaders.put(Constants.HTTP_CONTENT_TYPE, Constants.TEXT_XML);

                transportHeaders.put(Constants.HTTP_CONTENT_LENGTH, (String.valueOf(errorMessageBytes.length)));

                response.setHeaders(transportHeaders);

                response.setProperty(Constants.HTTP_STATUS_CODE, 504);
                response.setProperty(Constants.DIRECTION, Constants.DIRECTION_RESPONSE);
                response.setProperty(Constants.CALL_BACK, callback);
                ringBuffer.publishEvent(new CarbonEventPublisher(response));
            }
        }
    }
}
