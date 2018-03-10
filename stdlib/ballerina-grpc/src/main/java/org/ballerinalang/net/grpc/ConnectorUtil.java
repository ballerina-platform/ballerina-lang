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

import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.net.grpc.config.ServiceConfiguration;

public class ConnectorUtil {
    public static ServiceConfiguration generateServiceConfiguration(Struct serviceEndpointConfig) {
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
        serviceConfiguration.setPort(serviceEndpointConfig.getIntField("port"));
        serviceConfiguration.setClientStreaming(serviceEndpointConfig.getBooleanField("clientStreaming"));
        serviceConfiguration.setGenerateClientConnector(serviceEndpointConfig
                .getBooleanField("generateClientConnector"));
        serviceConfiguration.setRpcEndpoint(serviceEndpointConfig.getStringField("rpcEndpoint"));
        serviceConfiguration.setServerStreaming(serviceEndpointConfig.getBooleanField("serverStreaming"));
        return serviceConfiguration;
    }
}
