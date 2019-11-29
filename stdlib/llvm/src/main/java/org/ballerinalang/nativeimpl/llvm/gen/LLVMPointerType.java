// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.nativeimpl.llvm.gen;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.bytedeco.javacpp.LLVM;

import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.javacpp.LLVM.LLVMPointerType;

/**
 * Auto generated class.
 *
 * @since 1.0.3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "llvmPointerType",
        args = {
                @Argument(name = "elementType", type = RECORD, structType = "LLVMTypeRef"),
                @Argument(name = "addressSpace", type = INT),
        },
        returnType = {
                @ReturnType(type = RECORD, structType = "LLVMTypeRef", structPackage = "ballerina/llvm"),
        }
)

public class LLVMPointerType {

    public static MapValue<String, Object> llvmPointerType(Strand strand, MapValue<String, Object> typeRef,
                                                           long addressSpace) {

        LLVM.LLVMTypeRef elementType = (LLVM.LLVMTypeRef) FFIUtil.getRecodeArgumentNative(typeRef);
        LLVM.LLVMTypeRef returnValue = LLVMPointerType(elementType, (int) addressSpace);
        MapValue<String, Object> returnWrappedRecord = BallerinaValues.createRecordValue(new BPackage("ballerina",
                "llvm"), "LLVMTypeRef");
        FFIUtil.addNativeToRecode(returnValue, returnWrappedRecord);

        return returnWrappedRecord;
    }
}
