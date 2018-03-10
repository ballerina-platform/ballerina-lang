/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.ballerinalang.net.grpc;

import io.grpc.Server;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.BallerinaServerConnector;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.net.grpc.exception.GrpcServerException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is the gRPC implementation for the {@code BallerinaServerConnector} API.
 *
 * @since 0.96.1
 */
@JavaSPIService("org.ballerinalang.connector.api.BallerinaServerConnector")
public class BallerinaGrpcServerConnector implements BallerinaServerConnector {

    private static final String PROTOCOL_PACKAGE_GRPC = "ballerina.net.grpc";
    private final GrpcServicesBuilder servicesBuilder = new GrpcServicesBuilder();
    private Map<String, Service> serviceMap = new HashMap<>();

    @Override
    public List<String> getProtocolPackages() {
        List<String> protocolPackages = new LinkedList<>();
        protocolPackages.add(PROTOCOL_PACKAGE_GRPC);
        return protocolPackages;
    }

    @Override
    public void serviceRegistered(Service service) throws BallerinaConnectorException {
        if (PROTOCOL_PACKAGE_GRPC.equals(service.getProtocolPackage())) {
            try {
                Annotation serviceAnnotation = MessageUtils.getMessageListenerAnnotation(service, MessageConstants
                        .PROTOCOL_PACKAGE_GRPC);
                if (serviceAnnotation != null) {
                    serviceMap.put(service.getName(), service);
                } else {
                    servicesBuilder.initService(service);
                    serviceMap.put(service.getName(), service);
                }
            } catch (GrpcServerException e) {
                throw new BallerinaConnectorException("Error while registering the service : " + service.getName(), e);
            }
        }
    }

    @Override
    public void deploymentComplete() throws BallerinaConnectorException {
        try {
            Server server = servicesBuilder.start();
            servicesBuilder.blockUntilShutdown(server);
        } catch (GrpcServerException | InterruptedException e) {
            throw new BallerinaConnectorException(e);
        }
    }

    public Service getListenerService(String serviceName) {
        if (serviceMap.containsKey(serviceName)) {
            return serviceMap.get(serviceName);
        }
        return null;
    }
}
