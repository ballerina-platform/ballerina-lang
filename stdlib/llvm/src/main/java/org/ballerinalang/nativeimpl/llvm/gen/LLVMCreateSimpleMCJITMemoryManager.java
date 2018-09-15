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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMMCJITMemoryManagerRef;
import org.bytedeco.javacpp.Pointer;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.javacpp.LLVM.LLVMCreateSimpleMCJITMemoryManager;

/**
 * Auto generated class.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "LLVMCreateSimpleMCJITMemoryManager",
        args = {
                @Argument(name = "opaque", type = RECORD, structType = "Pointer"),
                @Argument(name = "allocateCodeSection", type = RECORD, structType =
                        "LLVMMemoryManagerAllocateCodeSectionCallback"),
                @Argument(name = "allocateDataSection", type = RECORD, structType =
                        "LLVMMemoryManagerAllocateDataSectionCallback"),
                @Argument(name = "finalizeMemory", type = RECORD, structType =
                        "LLVMMemoryManagerFinalizeMemoryCallback"),
                @Argument(name = "destroy", type = RECORD, structType = "LLVMMemoryManagerDestroyCallback"),
        },
        returnType = {
                @ReturnType(type = RECORD, structType = "LLVMMCJITMemoryManagerRef", structPackage = "ballerina/llvm"),
        }
)
public class LLVMCreateSimpleMCJITMemoryManager extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        Pointer opaque = FFIUtil.getRecodeArgumentNative(context, 0);
        LLVM.LLVMMemoryManagerAllocateCodeSectionCallback allocateCodeSection = FFIUtil.getRecodeArgumentNative
                (context, 1);
        LLVM.LLVMMemoryManagerAllocateDataSectionCallback allocateDataSection = FFIUtil.getRecodeArgumentNative
                (context, 2);
        LLVM.LLVMMemoryManagerFinalizeMemoryCallback finalizeMemory = FFIUtil.getRecodeArgumentNative(context, 3);
        LLVM.LLVMMemoryManagerDestroyCallback destroy = FFIUtil.getRecodeArgumentNative(context, 4);
        LLVMMCJITMemoryManagerRef returnValue = LLVMCreateSimpleMCJITMemoryManager(opaque, allocateCodeSection,
                                                                                   allocateDataSection,
                                                                                   finalizeMemory, destroy);
        BMap<String, BValue> rerunWrapperRecode = FFIUtil.newRecord(context, "LLVMMCJITMemoryManagerRef");
        FFIUtil.addNativeToRecode(returnValue, rerunWrapperRecode);
        context.setReturnValues(rerunWrapperRecode);
    }
}
