/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.composer.server.core;

import org.ballerinalang.composer.server.spi.ComposerServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesRunner;

import java.util.ServiceLoader;

/**
 * Backend server of ballerina composer.
 */
public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    private final ServerConfig serverConfig;
    private final ServiceLoader<ComposerServiceProvider> serviceProviderLoader;
    private final MicroservicesRunner microservicesRunner;

    public Server(ServerConfig config) {
        serverConfig = config;
        serviceProviderLoader = ServiceLoader.load(ComposerServiceProvider.class);
        microservicesRunner = new MicroservicesRunner(serverConfig.getServerPort());
    }

    public void start() {
        for (ComposerServiceProvider serviceProvider : serviceProviderLoader) {
            microservicesRunner.deploy(serviceProvider.createService(serverConfig));
        }
        microservicesRunner.start();
    }

    public void stop() {
        microservicesRunner.stop();
    }
}
