/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc.nativeimpl.serviceendpoint;

import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.grpc.nativeimpl.AbstractGrpcNativeFunction;
import org.ballerinalang.net.http.HttpConstants;

/**
 * Extern function to stop gRPC server instance.
 *
 * @since 1.0.0
 */
public class Stop extends AbstractGrpcNativeFunction {

    public static Object externStop(ObjectValue serverEndpoint) {
        getServerConnector(serverEndpoint).stop();
        serverEndpoint.addNativeData(HttpConstants.CONNECTOR_STARTED, false);
        return null;
    }
}
