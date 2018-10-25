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
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.PointerPointer;

import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.javacpp.LLVM.LLVMRunFunctionAsMain;

/**
 * Auto generated class.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "LLVMRunFunctionAsMain1",
        args = {
                @Argument(name = "ee", type = RECORD, structType = "LLVMExecutionEngineRef"),
                @Argument(name = "f", type = RECORD, structType = "LLVMValueRef"),
                @Argument(name = "argC", type = INT),
                @Argument(name = "argV", type = RECORD, structType = "PointerPointer"),
                @Argument(name = "envP", type = RECORD, structType = "PointerPointer"),
        },
        returnType = {
                @ReturnType(type = INT, structPackage = "ballerina/llvm"),
        }
)
public class LLVMRunFunctionAsMain1 extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        LLVM.LLVMExecutionEngineRef ee = FFIUtil.getRecodeArgumentNative(context, 0);
        LLVM.LLVMValueRef f = FFIUtil.getRecodeArgumentNative(context, 1);
        int argC = (int) context.getIntArgument(0);
        PointerPointer argV = FFIUtil.getRecodeArgumentNative(context, 2);
        PointerPointer envP = FFIUtil.getRecodeArgumentNative(context, 3);
        int returnValue = LLVMRunFunctionAsMain(ee, f, argC, argV, envP);
        context.setReturnValues(new BInteger(returnValue));
    }
}
