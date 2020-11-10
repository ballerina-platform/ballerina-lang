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
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import static org.ballerinalang.model.types.TypeKind.ARRAY;
import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.llvm.global.LLVM.LLVMStructSetBody;

/**
 * Auto generated class.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "llvmStructSetBody1",
        args = {
                @Argument(name = "structTy", type = RECORD, structType = "LLVMTypeRef"),
                @Argument(name = "elementTypes", type = ARRAY, structType = "PointerPointer"),
                @Argument(name = "elementCount", type = INT),
                @Argument(name = "packed", type = INT),
        })
public class LLVMStructSetBody1 {

    public static void llvmStructSetBody1(Strand strand, BMap<String, Object> structTy,
            BArray elementTypes, long elementCount, long packed) {
        LLVMTypeRef structTyRef = (LLVMTypeRef) FFIUtil.getRecodeArgumentNative(structTy);
        Pointer[] elementTypesRef = FFIUtil.getRecodeArrayArgumentNative(elementTypes);
        int elementCountRef = (int) elementCount;
        int packedRef = (int) packed;

        PointerPointer pointerPointer = new PointerPointer(elementTypesRef);
        LLVMStructSetBody(structTyRef, pointerPointer, elementCountRef, packedRef);
    }
}
