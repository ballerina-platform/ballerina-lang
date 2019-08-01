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
package org.ballerinalang.net.grpc.nativeimpl.serviceendpoint;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.nativeimpl.AbstractGrpcNativeFunction;

import static org.ballerinalang.net.grpc.GrpcConstants.LISTENER;
import static org.ballerinalang.net.grpc.GrpcConstants.LISTENER_CONNECTION_FIELD;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;

/**
 * Get the client responder instance binds to the service endpoint.
 *
 * @since 1.0.0
 */

@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "getCallerActions",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = LISTENER,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        isPublic = true
)
public class GetCallerActions extends AbstractGrpcNativeFunction {

    public static Object getCallerActions(Strand strand, ObjectValue listenerObject) {
        // Service client responder is populated in method listener. There we create new service endpoint instance
        // and bind client responder instance.
        Object clientType = listenerObject.get(LISTENER_CONNECTION_FIELD);
        if (clientType instanceof ObjectValue) {
            return clientType;
        } else {
            return MessageUtils.getConnectorError(new GrpcServerException("Error while " +
                    "retrieving endpoint client."));
        }
    }
}
