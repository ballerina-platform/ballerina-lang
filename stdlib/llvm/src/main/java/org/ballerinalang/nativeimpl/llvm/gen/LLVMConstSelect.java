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
import org.bytedeco.javacpp.LLVM.LLVMValueRef;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.javacpp.LLVM.LLVMConstSelect;

/**
 * Auto generated class.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "LLVMConstSelect",
        args = {
                @Argument(name = "constantCondition", type = RECORD, structType = "LLVMValueRef"),
                @Argument(name = "constantIfTrue", type = RECORD, structType = "LLVMValueRef"),
                @Argument(name = "constantIfFalse", type = RECORD, structType = "LLVMValueRef"),
        },
        returnType = {
                @ReturnType(type = RECORD, structType = "LLVMValueRef", structPackage = "ballerina/llvm"),
        }
)
public class LLVMConstSelect extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        LLVM.LLVMValueRef constantCondition = FFIUtil.getRecodeArgumentNative(context, 0);
        LLVM.LLVMValueRef constantIfTrue = FFIUtil.getRecodeArgumentNative(context, 1);
        LLVM.LLVMValueRef constantIfFalse = FFIUtil.getRecodeArgumentNative(context, 2);
        LLVMValueRef returnValue = LLVMConstSelect(constantCondition, constantIfTrue, constantIfFalse);
        BMap<String, BValue> rerunWrapperRecode = FFIUtil.newRecord(context, "LLVMValueRef");
        FFIUtil.addNativeToRecode(returnValue, rerunWrapperRecode);
        context.setReturnValues(rerunWrapperRecode);
    }
}
