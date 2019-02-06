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
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.STRING;

/**
 * Native class for jvm java class byte code creation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "visitMethodInit",
        args = {
                @Argument(name = "access", type = INT),
                @Argument(name = "name", type = STRING),
                @Argument(name = "descriptor", type = STRING)
        }
)
public class VisitMethodInit extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        ClassWriter cw = ASMCodeGenerator.getInstance().getClassWriter();
        int access = (int) context.getIntArgument(0);
        String name = context.getStringArgument(0);
        String descriptor = context.getStringArgument(1);
        MethodVisitor mv = cw.visitMethod(access, name, descriptor, null, null);
        ASMCodeGenerator.getInstance().setMethodVisitor(mv);
    }
}
