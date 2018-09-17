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
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.bytedeco.javacpp.LLVM;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.javacpp.LLVM.LLVMAddDestination;

/**
 * Auto generated class.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "LLVMAddDestination",
        args = {
                @Argument(name = "indirectBr", type = RECORD, structType = "LLVMValueRef"),
                @Argument(name = "dest", type = RECORD, structType = "LLVMBasicBlockRef"),
        })
public class LLVMAddDestination extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        LLVM.LLVMValueRef indirectBr = FFIUtil.getRecodeArgumentNative(context, 0);
        LLVM.LLVMBasicBlockRef dest = FFIUtil.getRecodeArgumentNative(context, 1);
        LLVMAddDestination(indirectBr, dest);
    }
}
