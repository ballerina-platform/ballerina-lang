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
package org.ballerinalang.net.grpc.nativeimpl;

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.grpc.GrpcConstants;
import org.ballerinalang.net.grpc.ServicesRegistry;
import org.wso2.transport.http.netty.contract.ServerConnector;

/**
 * Abstract class of gRPC service endpoint extern functions.
 *
 * @since 1.0.0
 */
public abstract class AbstractGrpcNativeFunction {

    protected static ServicesRegistry.Builder getServiceRegistryBuilder(ObjectValue serviceEndpoint) {
        return (ServicesRegistry.Builder) serviceEndpoint.getNativeData(GrpcConstants.SERVICE_REGISTRY_BUILDER);
    }

    protected static ServerConnector getServerConnector(ObjectValue serviceEndpoint) {
        return (ServerConnector) serviceEndpoint.getNativeData(GrpcConstants.SERVER_CONNECTOR);
    }

    protected static boolean isConnectorStarted(ObjectValue serviceEndpoint) {
        return serviceEndpoint.getNativeData(GrpcConstants.CONNECTOR_STARTED) != null;
    }
}
