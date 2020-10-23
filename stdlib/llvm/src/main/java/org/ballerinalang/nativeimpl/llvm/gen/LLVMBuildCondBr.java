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

import io.ballerina.runtime.api.runtime.Module;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.scheduling.Strand;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildCondBr;

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

    public static BMap<String, Object> llvmBuildCondBr(Strand strand, BMap<String, Object> arg0,
                                                           BMap<String, Object> ifValue,
                                                           BMap<String, Object> then,
                                                           BMap<String, Object> elseValue) {

        LLVMBuilderRef arg0Ref = (LLVMBuilderRef) FFIUtil.getRecodeArgumentNative(arg0);
        LLVMValueRef ifValueRef = (LLVMValueRef) FFIUtil.getRecodeArgumentNative(ifValue);
        LLVMBasicBlockRef thenRef = (LLVMBasicBlockRef) FFIUtil.getRecodeArgumentNative(then);
        LLVMBasicBlockRef elseValueRef = (LLVMBasicBlockRef) FFIUtil.getRecodeArgumentNative(elseValue);
        LLVMValueRef returnValue = LLVMBuildCondBr(arg0Ref, ifValueRef, thenRef, elseValueRef);
        BMap<String, Object> returnWrappedRecord = FFIUtil.newRecord(new Module("ballerina",
                "llvm"), "LLVMValueRef");
        FFIUtil.addNativeToRecode(returnValue, returnWrappedRecord);
        return returnWrappedRecord;
    }
}
