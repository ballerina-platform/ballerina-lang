/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.nativeimpl.llvm.gen;

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.scheduling.Strand;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.bytedeco.llvm.LLVM.LLVMTypeRef;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.llvm.global.LLVM.LLVMGetStructName;

/**
 *
 * @since 1.1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "llvmCheckIfTypesMatch",
        args = {
                @Argument(name = "castType", type = RECORD, structType = "LLVMTypeRef"),
                @Argument(name = "lhsType", type = RECORD, structType = "LLVMTypeRef")
        }
)
public class TypeCheck {
    public static boolean llvmCheckIfTypesMatch(Strand strand, BMap<String, Object> castType,
            BMap<String, Object> lhsType) {
        LLVMTypeRef castTypeRef = (LLVMTypeRef) FFIUtil.getRecodeArgumentNative(castType);
        LLVMTypeRef lhsTypeRef = (LLVMTypeRef) FFIUtil.getRecodeArgumentNative(lhsType);
        String castTypeName = LLVMGetStructName(castTypeRef).getString();
        String lhsTypeName = LLVMGetStructName(lhsTypeRef).getString();
        return castTypeName.compareTo(lhsTypeName) == 0;
    }
}
