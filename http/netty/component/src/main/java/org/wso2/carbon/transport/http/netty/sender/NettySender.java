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
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.EngineException;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.http.netty.NettyCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorConfig;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorFactory;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;
import org.wso2.carbon.transport.http.netty.listener.ssl.SSLConfig;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

/**
 * A class creates connections with BE and send messages.
 */
public class NettySender implements TransportSender {

    private static final Logger log = LoggerFactory.getLogger(NettySender.class);
    private Config config;

    private ConnectionManager connectionManager;

    public NettySender(Config conf, ConnectionManager connectionManager) {
        this.config = conf;
        this.connectionManager = connectionManager;
    }

    @Override
    public boolean send(CarbonMessage msg, CarbonCallback callback) throws EngineException {

        final HttpRequest httpRequest = Util.createHttpRequest(msg);

        final HttpRoute route = new HttpRoute((String) msg.getProperty("HOST"), (Integer) msg.getProperty("PORT"));


        SourceHandler srcHandler = (SourceHandler) msg.getProperty(Constants.SRC_HNDLR);

        RingBuffer ringBuffer = (RingBuffer) msg.getProperty(Constants.DISRUPTOR);
        if (ringBuffer == null) {
            DisruptorConfig disruptorConfig = DisruptorFactory.
                    getDisruptorConfig(DisruptorFactory.DisruptorType.OUTBOUND);
            ringBuffer = disruptorConfig.getDisruptor();
        }

        Channel outboundChannel = null;

        try {
            TargetChannel targetChannel = connectionManager.getTargetChannel(route, srcHandler);
            outboundChannel = targetChannel.getChannel();
            targetChannel.getTargetHandler().setCallback(callback);
            targetChannel.getTargetHandler().setRingBuffer(ringBuffer);
            targetChannel.getTargetHandler().setQueuesize(config.queueSize);
            targetChannel.getTargetHandler().setTargetChannel(targetChannel);
            targetChannel.getTargetHandler().setConnectionManager(connectionManager);

            writeContent(outboundChannel, httpRequest, msg);
        } catch (Exception failedCause) {
            throw new EngineException(failedCause.getMessage(), failedCause);
        }

        return false;
    }

    private boolean writeContent(Channel channel, HttpRequest httpRequest, CarbonMessage carbonMessage) {
        channel.write(httpRequest);
        NettyCarbonMessage nettyCMsg = (NettyCarbonMessage) carbonMessage;
        while (true) {
            HttpContent httpContent = nettyCMsg.getHttpContent();
            if (httpContent instanceof LastHttpContent) {
                channel.writeAndFlush(httpContent);
                break;
        }
            if (httpContent != null) {
                channel.write(httpContent);
            }
        }
        return true;
    }

    /**
     * Class representing configs related to Transport Sender.
     */
    public static class Config {

        private String id;

        private SSLConfig sslConfig;

        private int queueSize;

        public Config(String id) {
            if (id == null) {
                throw new IllegalArgumentException("Netty transport ID is null");
            }
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public Config enableSsl(SSLConfig sslConfig) {
            this.sslConfig = sslConfig;
            return this;
        }

        public SSLConfig getSslConfig() {
            return sslConfig;
        }

        public int getQueueSize() {
            return queueSize;
        }

        public Config setQueueSize(int queuesize) {
            this.queueSize = queuesize;
            return this;
        }

    }

}
