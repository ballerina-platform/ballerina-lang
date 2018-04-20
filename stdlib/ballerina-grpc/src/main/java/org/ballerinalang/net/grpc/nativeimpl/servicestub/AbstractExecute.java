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
package org.ballerinalang.net.grpc.nativeimpl.servicestub;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import io.grpc.MethodDescriptor;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.net.grpc.MessageUtils;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;

import static org.ballerinalang.bre.bvm.BLangVMErrors.PACKAGE_BUILTIN;
import static org.ballerinalang.bre.bvm.BLangVMErrors.STRUCT_GENERIC_ERROR;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;

/**
 * {@code AbstractExecute} is the Execute action implementation of the gRPC Connector.
 *
 * @since 1.0.0
 */
abstract class AbstractExecute extends BlockingNativeCallableUnit {
    
    /**
     * Returns corresponding Ballerina type for the proto buffer type.
     *
     * @param protoType Protocol buffer type
     * @param context   Ballerina Context
     * @return .
     */
    BType getBalType(String protoType, Context context) {
        if (protoType.equalsIgnoreCase("DoubleValue") || protoType
                .equalsIgnoreCase("FloatValue")) {
            return BTypes.typeFloat;
        } else if (protoType.equalsIgnoreCase("Int32Value") || protoType
                .equalsIgnoreCase("Int64Value") || protoType
                .equalsIgnoreCase("UInt32Value") || protoType
                .equalsIgnoreCase("UInt64Value")) {
            return BTypes.typeInt;
        } else if (protoType.equalsIgnoreCase("BoolValue")) {
            return BTypes.typeBoolean;
        } else if (protoType.equalsIgnoreCase("StringValue")) {
            return BTypes.typeString;
        } else if (protoType.equalsIgnoreCase("BytesValue")) {
            return BTypes.typeBlob;
        } else {
            // TODO: 2/22/18 enum
            return context.getProgramFile().getEntryPackage().getStructInfo(protoType).getType();
        }
    }
    
    MethodDescriptor.MethodType getMethodType(Descriptors.MethodDescriptor
                                                      methodDescriptor) throws GrpcClientException {
        if (methodDescriptor == null) {
            throw new GrpcClientException("Error while processing method type. Method descriptor cannot be null.");
        }
        DescriptorProtos.MethodDescriptorProto methodDescriptorProto = methodDescriptor.toProto();
        return MessageUtils.getMethodType(methodDescriptorProto);
    }
    
    BStruct createStruct(Context context, String structName) {
        PackageInfo httpPackageInfo = context.getProgramFile()
                .getPackageInfo(PROTOCOL_STRUCT_PACKAGE_GRPC);
        StructInfo structInfo = httpPackageInfo.getStructInfo(structName);
        BStructType structType = structInfo.getType();
        return new BStruct(structType);
    }
    
    void notifyErrorReply(Context context, String errorMessage) {
        PackageInfo errorPackageInfo = context.getProgramFile().getPackageInfo(PACKAGE_BUILTIN);
        StructInfo errorStructInfo = errorPackageInfo.getStructInfo(STRUCT_GENERIC_ERROR);
        BStruct outboundError = new BStruct(errorStructInfo.getType());
        outboundError.setStringField(0, errorMessage);
        context.setError(outboundError);
    }
}
