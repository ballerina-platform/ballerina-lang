// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.javacpp.LLVM.LLVMBuildBr;

/**
 * Auto generated class.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "LLVMBuildBr",
        args = {
                @Argument(name = "arg0", type = RECORD, structType = "LLVMBuilderRef"),
                @Argument(name = "dest", type = RECORD, structType = "LLVMBasicBlockRef"),
        },
        returnType = {
                @ReturnType(type = RECORD, structType = "LLVMValueRef", structPackage = "ballerina/llvm"),
        }
)
public class LLVMBuildBr {

    public static Object llvmBuildBr(MapValue<String, Object> arg0, MapValue<String, Object> dest) {
        LLVM.LLVMBuilderRef arg0Ref = (LLVM.LLVMBuilderRef) FFIUtil.getRecodeArgumentNative(arg0);
        LLVM.LLVMBasicBlockRef destRef = (LLVM.LLVMBasicBlockRef) FFIUtil.getRecodeArgumentNative(dest);
        LLVMValueRef returnValue = LLVM.LLVMBuildBr(arg0Ref, destRef);
        MapValue<String, Object> returnWrappedRecord = FFIUtil.newRecord();
        FFIUtil.addNativeToRecode(returnValue, returnWrappedRecord);
        return returnWrappedRecord;
    }
}
