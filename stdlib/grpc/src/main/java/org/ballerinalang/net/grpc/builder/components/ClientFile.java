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
package org.ballerinalang.net.grpc.builder.components;

/**
 * gRPC client file bean class.
 *
 * @since 0.982.0
 */
public class ClientFile {
    private String serviceName;
    private boolean blockingEP;
    
    public ClientFile(String serviceName, boolean blockingEP) {
        this.serviceName = serviceName;
        this.blockingEP = blockingEP;
    }

    public String getServiceName() {
        return serviceName;
    }

    public boolean isBlockingEP() {
        return blockingEP;
    }
}
