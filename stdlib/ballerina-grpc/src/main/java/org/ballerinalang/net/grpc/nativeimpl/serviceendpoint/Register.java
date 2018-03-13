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

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.nativeimpl.AbstractGrpcNativeFunction;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.ballerinalang.net.grpc.EndpointConstants.SERVICE_ENDPOINT_INDEX;
import static org.ballerinalang.net.grpc.GrpcServicesBuilder.registerService;

/**
 * Native function to respond the caller.
 *
 * @since 0.96.1
 */
@BallerinaFunction(
        packageName = "ballerina.net.grpc",
        functionName = "register",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Service",
                structPackage = "ballerina.net.grpc"),
        args = {@Argument(name = "serviceType", type = TypeKind.TYPE)},
        isPublic = true
)
public class Register extends AbstractGrpcNativeFunction {
    private static final Logger log = LoggerFactory.getLogger(Register.class);
    
    @Override
    public void execute(Context context) {
        BStruct serviceEndpoint = (BStruct) context.getRefArgument(SERVICE_ENDPOINT_INDEX);
        Service service = BLangConnectorSPIUtil.getServiceRegisted(context);
/*        Annotation annotation = getServiceConfigAnnotation(service,
                "ballerina.net.grpc");
        Struct serviceConfig = annotation != null ? annotation.getValue() : null;*/
        io.grpc.ServerBuilder serverBuilder = getServiceBuilder(serviceEndpoint);
        try {
            registerService(serverBuilder, service);
            context.setReturnValues();
        } catch (GrpcServerException e) {
            context.setError(MessageUtils.getConnectorError(context, e));
        }
    }
    
    public static Annotation getServiceConfigAnnotation(Service service, String pkgPath) {
        List<Annotation> annotationList = service
                .getAnnotationList(pkgPath, "serviceConfig");
        
        if (annotationList == null) {
            return null;
        }
        
        if (annotationList.size() > 1) {
            throw new BallerinaException(
                    "multiple service configuration annotations found in service: " + service.getName());
        }
        
        return annotationList.isEmpty() ? null : annotationList.get(0);
    }
}
