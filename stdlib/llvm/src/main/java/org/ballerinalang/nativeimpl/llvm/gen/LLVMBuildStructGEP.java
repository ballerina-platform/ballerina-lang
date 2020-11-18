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

import io.ballerina.runtime.api.runtime.Module;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.scheduling.Strand;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.bytedeco.llvm.LLVM.LLVMBuilderRef;
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildStructGEP;

/**
 * Auto generated class.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "llvmBuildStructGEP",
        args = {
                @Argument(name = "b", type = RECORD, structType = "LLVMBuilderRef"),
                @Argument(name = "pointer", type = RECORD, structType = "LLVMValueRef"),
                @Argument(name = "idx", type = INT),
                @Argument(name = "name", type = STRING),
        },
        returnType = {
                @ReturnType(type = RECORD, structType = "LLVMValueRef", structPackage = "ballerina/llvm"),
        }
)
public class LLVMBuildStructGEP {

    public static BMap<String, Object> llvmBuildStructGEP(Strand strand, BMap<String, Object> b,
            BMap<String, Object> pointer, long idx, String name) {
        LLVMBuilderRef bRef = (LLVMBuilderRef) FFIUtil.getRecodeArgumentNative(b);
        LLVMValueRef pointerRef = (LLVMValueRef) FFIUtil.getRecodeArgumentNative(pointer);
        int idxRef = (int) idx;
        LLVMValueRef returnValue = LLVMBuildStructGEP(bRef, pointerRef, idxRef, name);
        BMap<String, Object> returnWrappedRecord = FFIUtil.newRecord(new Module("ballerina",
                "llvm"), "LLVMValueRef");
        FFIUtil.addNativeToRecode(returnValue, returnWrappedRecord);
        return returnWrappedRecord;
    }
}
