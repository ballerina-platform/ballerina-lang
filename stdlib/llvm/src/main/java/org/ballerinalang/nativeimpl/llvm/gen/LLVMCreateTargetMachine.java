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
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.LLVMTargetMachineRef;
import org.bytedeco.llvm.LLVM.LLVMTargetRef;

import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.llvm.global.LLVM.LLVMCreateTargetMachine;

/**
 * Auto generated class.
 *
 * @since 1.0.3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "llvmCreateTargetMachine",
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
public class LLVMCreateTargetMachine {

    public static BMap<String, Object> llvmCreateTargetMachine(Strand strand, BMap<String, Object> arg0,
                                                                   BMap<String, Object> arg1,
                                                                   BMap<String, Object> arg2,
                                                                   BMap<String, Object> arg3,
                                                                   long arg4, long arg5, long arg6) {

        LLVMTargetRef t = (LLVMTargetRef) FFIUtil.getRecodeArgumentNative(arg0);
        BytePointer triple = (BytePointer) FFIUtil.getRecodeArgumentNative(arg1);
        BytePointer cpu = (BytePointer) FFIUtil.getRecodeArgumentNative(arg2);
        BytePointer features = (BytePointer) FFIUtil.getRecodeArgumentNative(arg3);
        LLVMTargetMachineRef returnValue = LLVMCreateTargetMachine(t, triple, cpu, features, (int) arg4,
                (int) arg5, (int) arg6);
        BMap<String, Object> rerunWrapperRecode = FFIUtil.newRecord(new Module("ballerina",
                "llvm"), "LLVMTargetMachineRef");
        FFIUtil.addNativeToRecode(returnValue, rerunWrapperRecode);
        return rerunWrapperRecode;
    }
}
