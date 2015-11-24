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

package org.wso2.carbon.transport.http.netty.listener;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.CarbonTransportServerInitializer;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorConfig;
import org.wso2.carbon.transport.http.netty.common.disruptor.config.DisruptorFactory;
import org.wso2.carbon.transport.http.netty.sender.channel.BootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.PoolConfiguration;

import java.io.File;
import java.util.Map;

/**
 * A class that responsible for create server side channels.
 */
public class GatewayNettyInitializer implements CarbonTransportServerInitializer {

    private static final Logger log = Logger.getLogger(GatewayNettyInitializer.class);
    private int queueSize = 32544;
    private ConnectionManager connectionManager;

    public static final String CAMEL_CONTEXT_CONFIG_FILE = "repository" + File.separator + "conf" +
                                                           File.separator +
                                                           "camel" + File.separator
                                                           + "camel-context.xml";

    public GatewayNettyInitializer() {

    }

    @Override
    public void setup(Map<String, String> parameters) {


        BootstrapConfiguration.createBootStrapConfiguration(parameters);
        PoolConfiguration.createPoolConfiguration(parameters);

//        SpringCamelContext.setNoStart(true);
//        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
//                new String[]{CAMEL_CONTEXT_CONFIG_FILE});
        try {
//            SpringCamelContext camelContext = (SpringCamelContext) applicationContext.getBean("wso2-cc");
//            camelContext.start();
//            CamelMediationComponent component = (CamelMediationComponent) camelContext.getComponent("wso2-gw");
//            CamelMediationEngine engine = component.getEngine();
//            connectionManager = component.getConnectionManager();


            if (parameters != null) {
                DisruptorConfig disruptorConfig =
                        new DisruptorConfig(
                                parameters.get(Constants.DISRUPTOR_BUFFER_SIZE),
                                parameters.get(Constants.DISRUPTOR_COUNT),
                                parameters.get(Constants.DISRUPTOR_EVENT_HANDLER_COUNT),
                                parameters.get(Constants.WAIT_STRATEGY),
                                Boolean.parseBoolean(Constants.SHARE_DISRUPTOR_WITH_OUTBOUND));
                // TODO: Need to have a proper service
                DisruptorFactory.createDisruptors(DisruptorFactory.DisruptorType.INBOUND,
                        disruptorConfig, new Tempinit());
                String queueSize = parameters.get(Constants.CONTENT_QUEUE_SIZE);
                if (queueSize != null) {
                    this.queueSize = Integer.parseInt(queueSize);
                }
            } else {
                log.warn("Disruptor specific parameters are not specified in " +
                         "configuration hence using default configs");
                DisruptorConfig disruptorConfig = new DisruptorConfig();
                DisruptorFactory.createDisruptors(DisruptorFactory.DisruptorType.INBOUND,
                                                  disruptorConfig, new Tempinit());
            }
        } catch (Exception e) {
            String msg = "Error while loading " + CAMEL_CONTEXT_CONFIG_FILE + " configuration file";
            log.error(msg + e);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public void initChannel(Object ch) {
        if (log.isDebugEnabled()) {
            log.info("Initializing source channel pipeline");
        }
        ChannelPipeline p = ((SocketChannel) ch).pipeline();
        p.addLast("decoder", new HttpRequestDecoder());
        p.addLast("encoder", new HttpResponseEncoder());
        try {
            p.addLast("handler", new SourceHandler(queueSize, connectionManager));
        } catch (Exception e) {
            log.error("Cannot Create SourceHandler ", e);
        }
    }

    static class Tempinit implements CarbonMessageProcessor {
        @Override
        public boolean receive(CarbonMessage carbonMessage,
                CarbonCallback carbonCallback) throws Exception {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
