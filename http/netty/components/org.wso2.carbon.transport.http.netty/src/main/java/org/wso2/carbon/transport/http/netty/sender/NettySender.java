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
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.EngagedLocation;
import org.wso2.carbon.messaging.MessageProcessorException;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorConfig;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorFactory;
import org.wso2.carbon.transport.http.netty.config.Parameter;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.BootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.ChannelUtils;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.PoolConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * A class creates connections with BE and send messages.
 */
public class NettySender implements TransportSender {

    private static final Logger log = LoggerFactory.getLogger(NettySender.class);
    private String id;
    private ConnectionManager connectionManager;
    private SenderConfiguration senderConfiguration;

    public NettySender(SenderConfiguration senderConfiguration) {
        this.id = senderConfiguration.getId();
        this.senderConfiguration = senderConfiguration;
        Map<String, String> paramMap = new HashMap<>(senderConfiguration.getParameters().size());
        if (senderConfiguration.getParameters() != null && !senderConfiguration.getParameters().isEmpty()) {
            for (Parameter parameter : senderConfiguration.getParameters()) {
                paramMap.put(parameter.getName(), parameter.getValue());
            }

        }
        PoolConfiguration.createPoolConfiguration(paramMap);
        BootstrapConfiguration.createBootStrapConfiguration(paramMap);
        this.connectionManager = ConnectionManager.getInstance();
    }

    @Override public boolean send(CarbonMessage msg, CarbonCallback callback) throws MessageProcessorException {

        final HttpRequest httpRequest = Util.createHttpRequest(msg);
        final HttpRoute route = new HttpRoute((String) msg.getProperty(Constants.HOST),
                (Integer) msg.getProperty(Constants.PORT));
        SourceHandler srcHandler = (SourceHandler) msg.getProperty(Constants.SRC_HNDLR);

        RingBuffer ringBuffer = (RingBuffer) msg.getProperty(Constants.DISRUPTOR);
        if (ringBuffer == null) {
            DisruptorConfig disruptorConfig = DisruptorFactory.
                    getDisruptorConfig(DisruptorFactory.DisruptorType.OUTBOUND);
            ringBuffer = disruptorConfig.getDisruptor();
        }

        Channel outboundChannel = null;
        try {
            TargetChannel targetChannel = connectionManager
                    .getTargetChannel(route, srcHandler, senderConfiguration, httpRequest, msg, callback, ringBuffer);
            if (targetChannel != null) {
                outboundChannel = targetChannel.getChannel();
                targetChannel.getTargetHandler().setCallback(callback);
                targetChannel.getTargetHandler().setIncomingMsg(msg);
                targetChannel.getTargetHandler().setRingBuffer(ringBuffer);
                targetChannel.getTargetHandler().setTargetChannel(targetChannel);
                targetChannel.getTargetHandler().setConnectionManager(connectionManager);

                NettyTransportContextHolder.getInstance().getInterceptor()
                        .engage(msg, EngagedLocation.SERVER_REQUEST_WRITE_INITIATED);
                NettyTransportContextHolder.getInstance().getInterceptor()
                        .engage(msg, EngagedLocation.SERVER_REQUEST_WRITE_HEADERS_COMPLETED);
                boolean written = ChannelUtils.writeContent(outboundChannel, httpRequest, msg);
                NettyTransportContextHolder.getInstance().getInterceptor()
                        .engage(msg, EngagedLocation.SERVER_REQUEST_WRITE_BODY_COMPLETED);
                if (written) {
                    targetChannel.setRequestWritten(true);
                }
            }
        } catch (Exception failedCause) {
            throw new MessageProcessorException(failedCause.getMessage(), failedCause);
        }

        return false;
    }

    @Override public String getId() {
        return id;
    }

}
