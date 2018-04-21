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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.GrpcServicesBuilder;
import org.ballerinalang.net.grpc.MessageUtils;
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
        receiver = @Receiver(type = TypeKind.STRUCT, structType = SERVICE_ENDPOINT_TYPE,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        isPublic = true
)
public class Start extends AbstractGrpcNativeFunction {
    private static final PrintStream console = System.out;
    private static final PrintStream consoleErr = System.err;
    
    @Override
    public void execute(Context context) {
        BStruct serviceEndpoint = (BStruct) context.getRefArgument(SERVICE_ENDPOINT_INDEX);
        io.grpc.ServerBuilder serverBuilder = getServiceBuilder(serviceEndpoint);
        try {
            Server server = GrpcServicesBuilder.start(serverBuilder);
            serviceEndpoint.addNativeData(GRPC_SERVER, server);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> stop(server)));
            console.println("ballerina: started gRPC server connector on port " + server.getPort());
            GrpcServicesBuilder.blockUntilShutdown(server);
        } catch (GrpcServerException e) {
            consoleErr.println("ballerina: failed to bind gRPC server to port. address already in use ");
            context.setError(MessageUtils.getConnectorError(context, new GrpcServerException("Error in starting gRPC " +
                    "service.", e)));
        } catch (InterruptedException e) {
            context.setError(MessageUtils.getConnectorError(context, new GrpcServerException("gRPC server " +
                    "interrupted", e)));
        }
        context.setReturnValues();
    }
}
