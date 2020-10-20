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
import org.bytedeco.llvm.LLVM.LLVMContextRef;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.bytedeco.llvm.global.LLVM.LLVMStructCreateNamed;

/**
 * Auto generated class.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "llvmStructCreateNamed",
        args = {
                @Argument(name = "c", type = RECORD, structType = "LLVMContextRef"),
                @Argument(name = "name", type = STRING),
        },
        returnType = {
                @ReturnType(type = RECORD, structType = "LLVMTypeRef", structPackage = "ballerina/llvm"),
        }
)
public class LLVMStructCreateNamed {

    public static BMap<String, Object> llvmStructCreateNamed(Strand strand, BMap<String, Object> c,
            String name) {
        LLVMContextRef cRef = (LLVMContextRef) FFIUtil.getRecodeArgumentNative(c);
        LLVMTypeRef returnValue = LLVMStructCreateNamed(cRef, name);
        BMap<String, Object> returnWrappedValue = FFIUtil.newRecord(new Module("ballerina",
                "llvm"), "LLVMTypeRef");
        FFIUtil.addNativeToRecode(returnValue, returnWrappedValue);
        return returnWrappedValue;
    }
}
