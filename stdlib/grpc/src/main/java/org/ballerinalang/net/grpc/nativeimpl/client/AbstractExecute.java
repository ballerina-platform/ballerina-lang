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
package org.ballerinalang.net.grpc.nativeimpl.client;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BErrorType;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructureTypeInfo;

import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.Status.Code.INTERNAL;

/**
 * {@code AbstractExecute} is the Execute action implementation of the gRPC Connector.
 *
 * @since 1.0.0
 */
abstract class AbstractExecute implements NativeCallableUnit {

    MethodDescriptor.MethodType getMethodType(Descriptors.MethodDescriptor
                                                      methodDescriptor) throws GrpcClientException {
        if (methodDescriptor == null) {
            throw new GrpcClientException("Error while processing method type. Method descriptor cannot be null.");
        }
        DescriptorProtos.MethodDescriptorProto methodDescriptorProto = methodDescriptor.toProto();
        return MessageUtils.getMethodType(methodDescriptorProto);
    }

    BMap<String, BValue> createStruct(Context context, String structName) {
        PackageInfo httpPackageInfo = context.getProgramFile()
                .getPackageInfo(PROTOCOL_STRUCT_PACKAGE_GRPC);
        StructureTypeInfo structInfo = httpPackageInfo.getStructInfo(structName);
        BStructureType structType = structInfo.getType();
        return new BMap<>(structType);
    }

    void notifyErrorReply(Context context, String errorMessage) {
        BErrorType errType = BTypes.typeError;
        String reason = "{ballerina/grpc}" + INTERNAL.name();
        BMap<String, BValue> refData = new BMap<>(errType.detailType);
        refData.put("message", new BString(errorMessage));
        context.setReturnValues(new BError(BTypes.typeError, reason, refData));
    }
}
