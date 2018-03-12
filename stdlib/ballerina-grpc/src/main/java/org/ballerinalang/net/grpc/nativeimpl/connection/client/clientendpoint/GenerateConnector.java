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

package org.ballerinalang.net.grpc.nativeimpl.connection.client.clientendpoint;

import io.grpc.ManagedChannel;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.EndpointConstants;
import org.ballerinalang.net.grpc.MessageConstants;

/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */
@BallerinaFunction(
        packageName = "ballerina.net.grpc",
        functionName = "generateConnector",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Client",
                             structPackage = "ballerina.net.grpc"),
        args = {@Argument(name = "serviceDescriptor", type = TypeKind.TYPE)},
        isPublic = true
)
public class GenerateConnector extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BStruct clientEndpoint = (BStruct) context.getRefArgument( 0);;
        // Creating server connector
        BStruct serviceDescriptor = (BStruct) context.getRefArgument( 1);
        if (serviceDescriptor == null) {
            throw new BallerinaConnectorException("Service Descriptor not defined in client connector sub. Please " +
                    "revisit client sub implementation");
        }
        ManagedChannel channel = (ManagedChannel) clientEndpoint.getNativeData(EndpointConstants.CHANNEL_KEY);

        BConnector clientConnector = BLangConnectorSPIUtil.createBConnector(context.getProgramFile(),
                MessageConstants.PROTOCOL_PACKAGE_GRPC, "ClientConnector", channel, serviceDescriptor);

        clientEndpoint.addNativeData("clientConnector", clientConnector);
    }

}
