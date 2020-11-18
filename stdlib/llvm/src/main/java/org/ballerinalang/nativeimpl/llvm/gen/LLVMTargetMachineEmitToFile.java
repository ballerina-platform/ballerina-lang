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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.scheduling.Strand;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.bytedeco.llvm.LLVM.LLVMModuleRef;
import org.bytedeco.llvm.LLVM.LLVMTargetMachineRef;

import static org.ballerinalang.model.types.TypeKind.ARRAY;
import static org.ballerinalang.model.types.TypeKind.BYTE;
import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.llvm.global.LLVM.LLVMTargetMachineEmitToFile;

/**
 * Auto generated class.
 *
 * @since 1.2.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "llvmTargetMachineEmitToFile",
        args = {
                @Argument(name = "t", type = RECORD, structType = "LLVMTargetMachineRef"),
                @Argument(name = "m", type = RECORD, structType = "LLVMModuleRef"),
                @Argument(name = "filename", type = ARRAY, elementType = BYTE),
                @Argument(name = "codegen", type = INT),
                @Argument(name = "errorMessage", type = RECORD, structType = "PointerPointer"),
        },
        returnType = {
                @ReturnType(type = INT, structPackage = "ballerina/llvm"),
        }
)
public class LLVMTargetMachineEmitToFile {

    public static long llvmTargetMachineEmitToFile(Strand strand, BMap<String, Object> arg0, BMap<String,
            Object> arg1, BArray arg2, long arg3, BArray arg4) {

        LLVMTargetMachineRef t = (LLVMTargetMachineRef) FFIUtil.getRecodeArgumentNative(arg0);
        LLVMModuleRef m = (LLVMModuleRef) FFIUtil.getRecodeArgumentNative(arg1);

        return LLVMTargetMachineEmitToFile(t, m, arg2.getBytes(), (int) arg3, arg4.getBytes());
    }
}
