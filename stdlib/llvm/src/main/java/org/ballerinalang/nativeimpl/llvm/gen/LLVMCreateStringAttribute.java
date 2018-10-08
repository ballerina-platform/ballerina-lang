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
import org.bytedeco.javacpp.LLVM.LLVMAttributeRef;

import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.bytedeco.javacpp.LLVM.LLVMCreateStringAttribute;

/**
 * Auto generated class.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "LLVMCreateStringAttribute",
        args = {
                @Argument(name = "c", type = RECORD, structType = "LLVMContextRef"),
                @Argument(name = "k", type = STRING),
                @Argument(name = "kLength", type = INT),
                @Argument(name = "v", type = STRING),
                @Argument(name = "vLength", type = INT),
        },
        returnType = {
                @ReturnType(type = RECORD, structType = "LLVMAttributeRef", structPackage = "ballerina/llvm"),
        }
)
public class LLVMCreateStringAttribute extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        LLVM.LLVMContextRef c = FFIUtil.getRecodeArgumentNative(context, 0);
        String k = context.getStringArgument(0);
        int kLength = (int) context.getIntArgument(0);
        String v = context.getStringArgument(1);
        int vLength = (int) context.getIntArgument(1);
        LLVMAttributeRef returnValue = LLVMCreateStringAttribute(c, k, kLength, v, vLength);
        BMap<String, BValue> rerunWrapperRecode = FFIUtil.newRecord(context, "LLVMAttributeRef");
        FFIUtil.addNativeToRecode(returnValue, rerunWrapperRecode);
        context.setReturnValues(rerunWrapperRecode);
    }
}
