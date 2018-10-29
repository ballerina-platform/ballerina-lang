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

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.nativeimpl.AbstractGrpcNativeFunction;

import static org.ballerinalang.net.grpc.GrpcConstants.CALLER_ACTION;
import static org.ballerinalang.net.grpc.GrpcConstants.LISTENER_CONNECTION_FIELD;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_ENDPOINT_INDEX;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_ENDPOINT_TYPE;

/**
 * Get the client responder instance binds to the service endpoint.
 *
 * @since 1.0.0
 */

@BallerinaFunction(
        orgName = ORG_NAME,
        packageName = PROTOCOL_PACKAGE_GRPC,
        functionName = "getCallerActions",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = SERVICE_ENDPOINT_TYPE,
                structPackage = PROTOCOL_STRUCT_PACKAGE_GRPC),
        returnType = {@ReturnType(type = TypeKind.OBJECT, structType = CALLER_ACTION, structPackage =
                PROTOCOL_STRUCT_PACKAGE_GRPC)},
        isPublic = true
)
public class GetCallerActions extends AbstractGrpcNativeFunction {
    
    @Override
    public void execute(Context context) {
        BMap<String, BValue> serviceEndpoint = (BMap<String, BValue>) context.getRefArgument(SERVICE_ENDPOINT_INDEX);
        // Service client responder is populated in method listener. There we create new service endpoint instance
        // and bind client responder instance.
        BValue clientType = serviceEndpoint.get(LISTENER_CONNECTION_FIELD);
        if (clientType != null && clientType.getType().getTag() == TypeTags.OBJECT_TYPE_TAG) {
            BMap<String, BValue> endpointClient = (BMap<String, BValue>) clientType;
            context.setReturnValues(endpointClient);
        } else {
            context.setError(MessageUtils.getConnectorError(new GrpcServerException("Error while " +
                    "retrieving endpoint client.")));
        }
    }
}
