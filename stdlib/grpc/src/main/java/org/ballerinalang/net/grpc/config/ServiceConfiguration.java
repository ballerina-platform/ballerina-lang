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
package org.ballerinalang.net.grpc.config;

/**
 * Service configuration of gRPC Service.
 *
 * @since 1.0.0
 */
public class ServiceConfiguration {

    private String rpcEndpoint;
    private boolean clientStreaming;
    private boolean serverStreaming;

    public ServiceConfiguration(String rpcEndpoint, boolean clientStreaming, boolean serverStreaming) {
        this.rpcEndpoint = rpcEndpoint;
        this.clientStreaming = clientStreaming;
        this.serverStreaming = serverStreaming;

    }
    
    public String getRpcEndpoint() {
        return rpcEndpoint;
    }
    
    public void setRpcEndpoint(String rpcEndpoint) {
        this.rpcEndpoint = rpcEndpoint;
    }
    
    public boolean isClientStreaming() {
        return clientStreaming;
    }
    
    public void setClientStreaming(boolean clientStreaming) {
        this.clientStreaming = clientStreaming;
    }
    
    public boolean isServerStreaming() {
        return serverStreaming;
    }
    
    public void setServerStreaming(boolean serverStreaming) {
        this.serverStreaming = serverStreaming;
    }
}
