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

import org.ballerinalang.composer.server.service.ConfigService;
import org.ballerinalang.composer.server.service.PublicContentService;
import org.ballerinalang.composer.server.spi.ComposerService;
import org.ballerinalang.composer.server.spi.ComposerServiceProvider;
import org.ballerinalang.composer.server.spi.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Backend server of ballerina composer.
 */
public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    private final ServerConfig serverConfig;
    private final ServiceLoader<ComposerServiceProvider> serviceProviderLoader;
    private final MicroservicesRunner microservicesRunner;
    private List<ComposerService> serviceList;

    public Server(ServerConfig config) {
        serverConfig = config;
        serviceProviderLoader = ServiceLoader.load(ComposerServiceProvider.class);
        microservicesRunner = new MicroservicesRunner(serverConfig.getPort());
        serviceList = new ArrayList<>();
    }

    public void start() {
        // create services
        for (ComposerServiceProvider serviceProvider : serviceProviderLoader) {
            ComposerService service = serviceProvider.createService(serverConfig);
            if (service != null) {
                serviceList.add(service);
            }
        }
        // deploy services
        for (ComposerService service: serviceList) {
            if (service.getServiceInfo().getType() == ServiceType.HTTP) {
                microservicesRunner.deploy(service);
            } else if (service.getServiceInfo().getType() == ServiceType.WS) {
                microservicesRunner.deployWebSocketEndpoint(service);
            }
        }
        // deploy ep info service
        microservicesRunner.deploy(new ConfigService(serverConfig, serviceList));
        // deploy public content service
        microservicesRunner.deploy(new PublicContentService(serverConfig));
        microservicesRunner.start();
    }

    public void stop() {
        microservicesRunner.stop();
    }

    public void setServiceList(List<ComposerService> serviceList) {
        this.serviceList = serviceList;
    }

    public List<ComposerService> getServiceList() {
        return serviceList;
    }
}
