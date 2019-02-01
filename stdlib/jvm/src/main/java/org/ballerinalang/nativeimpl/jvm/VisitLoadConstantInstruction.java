/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.jvm;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.objectweb.asm.MethodVisitor;

import static org.ballerinalang.model.types.TypeKind.ANY;

/**
 * Native class for jvm method byte code creation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "visitLoadConstantInstruction",
        args = {
                @Argument(name = "value", type = ANY)
        }
)
public class VisitLoadConstantInstruction extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        MethodVisitor mv = ASMCodeGenerator.getInstance().getMethodVisitor();
        BValue value = context.getRefArgument(0);
        switch (value.getType().getTag()) {
            case TypeTags.INT_TAG:
                long longVal = ((BInteger) value).intValue();
                mv.visitLdcInsn(longVal);
                break;
            case TypeTags.STRING_TAG:
                String stringVal = value.stringValue();
                mv.visitLdcInsn(stringVal);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
