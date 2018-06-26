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

import io.grpc.Server;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.GrpcServicesBuilder;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.nativeimpl.AbstractGrpcNativeFunction;

import java.io.PrintStream;

import static org.ballerinalang.net.grpc.EndpointConstants.SERVICE_ENDPOINT_INDEX;
import static org.ballerinalang.net.grpc.GrpcConstants.GRPC_SERVER;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_ENDPOINT_TYPE;
import static org.ballerinalang.net.grpc.GrpcServicesBuilder.stop;

/**
 * Native function to start gRPC server instance.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "start",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = SERVICE_ENDPOINT_TYPE,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        isPublic = true
)
public class Start extends AbstractGrpcNativeFunction {
    private static final PrintStream console = System.out;

    @Override
    public void execute(Context context) {
        BMap<String, BValue> serviceEndpoint = (BMap<String, BValue>) context.getRefArgument(SERVICE_ENDPOINT_INDEX);
        io.grpc.ServerBuilder serverBuilder = getServiceBuilder(serviceEndpoint);
        try {
            Server server = GrpcServicesBuilder.start(serverBuilder);
            serviceEndpoint.addNativeData(GRPC_SERVER, server);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> stop(server)));
            console.println("ballerina: started gRPC server connector on port " + server.getPort());
            GrpcServicesBuilder.blockUntilShutdown(server);
        } catch (GrpcServerException e) {
            //failed to bind gRPC server to port. address already in use.
            throw new BallerinaConnectorException(e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new BallerinaConnectorException("gRPC server is interrupted.", e);
        }
        context.setReturnValues();
    }
}
