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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;

import static org.ballerinalang.net.grpc.EndpointConstants.SERVICE_ENDPOINT_INDEX;
import static org.ballerinalang.net.grpc.GrpcServicesBuilder.stop;
import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_PACKAGE_GRPC;

/**
 * Native function to respond the caller.
 *
 * @since 0.96.1
 */
@BallerinaFunction(
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "start",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Service",
                structPackage = PROTOCOL_PACKAGE_GRPC),
        isPublic = true
)
public class Start extends AbstractGrpcNativeFunction {
    private static final Logger log = LoggerFactory.getLogger(Start.class);
    private static final PrintStream console = System.out;

    @Override
    public void execute(Context context) {
        BStruct serviceEndpoint = (BStruct) context.getRefArgument(SERVICE_ENDPOINT_INDEX);
        io.grpc.ServerBuilder serverBuilder = getServiceBuilder(serviceEndpoint);
        try {
            Server server = GrpcServicesBuilder.start(serverBuilder);
            serviceEndpoint.addNativeData("server", server);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> stop(server)));
            console.println("ballerina: started gRPC server connector on port " + server.getPort());
            GrpcServicesBuilder.blockUntilShutdown(server);
        } catch (GrpcServerException e) {
            context.setError(MessageUtils.getConnectorError(context, new GrpcServerException("Error in starting gRPC " +
                    "service.", e)));
        } catch (InterruptedException e) {
            context.setError(MessageUtils.getConnectorError(context, new GrpcServerException("gRPC server " +
                    "interrupted", e)));
        }
        context.setReturnValues();
    }
}
