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
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMTargetMachineRef;

import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.javacpp.LLVM.LLVMCreateTargetMachine;

/**
 * Auto generated class.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "LLVMCreateTargetMachine",
        args = {
                @Argument(name = "t", type = RECORD, structType = "LLVMTargetRef"),
                @Argument(name = "triple", type = RECORD, structType = "BytePointer"),
                @Argument(name = "cpu", type = RECORD, structType = "BytePointer"),
                @Argument(name = "features", type = RECORD, structType = "BytePointer"),
                @Argument(name = "level", type = INT),
                @Argument(name = "reloc", type = INT),
                @Argument(name = "codeModel", type = INT),
        },
        returnType = {
                @ReturnType(type = RECORD, structType = "LLVMTargetMachineRef", structPackage = "ballerina/llvm"),
        }
)
public class LLVMCreateTargetMachine extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        LLVM.LLVMTargetRef t = FFIUtil.getRecodeArgumentNative(context, 0);
        BytePointer triple = FFIUtil.getRecodeArgumentNative(context, 1);
        BytePointer cpu = FFIUtil.getRecodeArgumentNative(context, 2);
        BytePointer features = FFIUtil.getRecodeArgumentNative(context, 3);
        int level = (int) context.getIntArgument(0);
        int reloc = (int) context.getIntArgument(1);
        int codeModel = (int) context.getIntArgument(2);
        LLVMTargetMachineRef returnValue = LLVMCreateTargetMachine(t, triple, cpu, features, level, reloc, codeModel);
        BMap<String, BValue> rerunWrapperRecode = FFIUtil.newRecord(context, "LLVMTargetMachineRef");
        FFIUtil.addNativeToRecode(returnValue, rerunWrapperRecode);
        context.setReturnValues(rerunWrapperRecode);
    }
}
