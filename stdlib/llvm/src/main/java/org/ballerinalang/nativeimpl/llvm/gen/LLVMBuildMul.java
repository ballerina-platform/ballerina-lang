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

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.bytedeco.llvm.global.LLVM.LLVMBuildMul;

/**
 * Auto generated class.
 *
 * @since 1.0.3
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "llvmBuildMul",
        args = {
                @Argument(name = "arg0", type = RECORD, structType = "LLVMBuilderRef"),
                @Argument(name = "lhs", type = RECORD, structType = "LLVMValueRef"),
                @Argument(name = "rhs", type = RECORD, structType = "LLVMValueRef"),
                @Argument(name = "name", type = STRING),
        },
        returnType = {
                @ReturnType(type = RECORD, structType = "LLVMValueRef", structPackage = "ballerina/llvm"),
        }
)
public class LLVMBuildMul {

        public static BMap<String, Object> llvmBuildMul(Strand strand, BMap<String, Object> arg0,
                BMap<String, Object> lhs, BMap<String, Object> rhs, String name) {
                LLVMBuilderRef arg0Ref = (LLVMBuilderRef) FFIUtil.getRecodeArgumentNative(arg0);
                LLVMValueRef lhsRef = (LLVMValueRef) FFIUtil.getRecodeArgumentNative(lhs);
                LLVMValueRef rhsRef = (LLVMValueRef) FFIUtil.getRecodeArgumentNative(rhs);
                LLVMValueRef returnValue = LLVMBuildMul(arg0Ref, lhsRef, rhsRef, name);
                BMap<String, Object> returnWrappedRecord = FFIUtil.newRecord(new Module("ballerina",
                        "llvm"), "LLVMValueRef");
                FFIUtil.addNativeToRecode(returnValue, returnWrappedRecord);
                return returnWrappedRecord;
        }
}
