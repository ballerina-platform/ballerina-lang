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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.javacpp.LLVM.LLVMBuildCondBr;

/**
 * Auto generated class.
 *
 * @since 1.0.3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "llvmBuildCondBr",
        args = {
                @Argument(name = "arg0", type = RECORD, structType = "LLVMBuilderRef"),
                @Argument(name = "ifValue", type = RECORD, structType = "LLVMValueRef"),
                @Argument(name = "then", type = RECORD, structType = "LLVMBasicBlockRef"),
                @Argument(name = "elseValue", type = RECORD, structType = "LLVMBasicBlockRef"),
        },
        returnType = {
                @ReturnType(type = RECORD, structType = "LLVMValueRef", structPackage = "ballerina/llvm"),
        }
)
public class LLVMBuildCondBr {

    public static MapValue<String, Object> llvmBuildCondBr(Strand strand, MapValue<String, Object> arg0,
                                                           MapValue<String, Object> ifValue,
                                                           MapValue<String, Object> then,
                                                           MapValue<String, Object> elseValue) {

        LLVM.LLVMBuilderRef arg0Ref = (LLVM.LLVMBuilderRef) FFIUtil.getRecodeArgumentNative(arg0);
        LLVM.LLVMValueRef ifValueRef = (LLVMValueRef) FFIUtil.getRecodeArgumentNative(ifValue);
        LLVM.LLVMBasicBlockRef thenRef = (LLVM.LLVMBasicBlockRef) FFIUtil.getRecodeArgumentNative(then);
        LLVM.LLVMBasicBlockRef elseValueRef = (LLVM.LLVMBasicBlockRef) FFIUtil.getRecodeArgumentNative(elseValue);
        LLVMValueRef returnValue = LLVMBuildCondBr(arg0Ref, ifValueRef, thenRef, elseValueRef);
        MapValue<String, Object> returnWrappedRecord = FFIUtil.newRecord(new BPackage("ballerina",
                "llvm"), "LLVMValueRef");
        FFIUtil.addNativeToRecode(returnValue, returnWrappedRecord);
        return returnWrappedRecord;
    }
}
