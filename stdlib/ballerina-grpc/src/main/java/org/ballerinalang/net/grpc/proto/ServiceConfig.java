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
package org.ballerinalang.net.grpc.proto;

/**
 * gRPC service configuration object.
 */
public class ServiceConfig {
    private final String rpcEndpoint;
    private final boolean clientStreaming;
    private final boolean serverStreaming;
    private final boolean generateClientConnector;
    private final int port;

    public ServiceConfig(int port, String rpcEndpoint, boolean clientStreaming, boolean serverStreaming, boolean
            generateClientConnector) {
        this.port = port;
        this.rpcEndpoint = rpcEndpoint;
        this.clientStreaming = clientStreaming;
        this.serverStreaming = serverStreaming;
        this.generateClientConnector = generateClientConnector;

    }

    public String getRpcEndpoint() {
        return rpcEndpoint;
    }

    public boolean isClientStreaming() {
        return clientStreaming;
    }

    public boolean isServerStreaming() {
        return serverStreaming;
    }

    public boolean isGenerateClientConnector() {
        return generateClientConnector;
    }

    public int getPort() {
        return port;
    }
}
