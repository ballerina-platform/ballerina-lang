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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.ballerinalang.model.types.TypeKind.INT;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;

/**
 * Native class for jvm method byte code creation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "visitJumpInstruction",
        args = {
                @Argument(name = "jumpType", type = INT),
                @Argument(name = "labelId", type = STRING),
        }
)
public class VisitJumpInstruction extends BlockingNativeCallableUnit {

    private static final int JUMP = 0;
    private static final int GREATER_THAN_ZERO = 1;
    private static final int LESS_THAN_ZERO = 2;
    private static final int LESS_THAN_EQUAL_ZERO = 3;
    private static final int NOT_EQUAL_TO_ZERO = 4;
    private static final int IF_NOT_EQUAL = 5;
    private static final int IF_EQUAL = 6;

    @Override
    public void execute(Context context) {

        MethodVisitor mv = ASMCodeGenerator.getInstance().getMethodVisitor();

        int jumpType = (int) context.getIntArgument(0);
        String labelId = context.getStringArgument(0);
        Label label = ASMCodeGenerator.getInstance().getLabel(labelId);

        switch (jumpType) {
            case JUMP:
                mv.visitJumpInsn(GOTO, label);
                break;
            case GREATER_THAN_ZERO:
                mv.visitJumpInsn(IFGT, label);
                break;
            case LESS_THAN_ZERO:
                mv.visitJumpInsn(IFLT, label);
                break;
            case IF_EQUAL:
                mv.visitJumpInsn(IF_ICMPEQ, label);
                break;
            case IF_NOT_EQUAL:
                mv.visitJumpInsn(IF_ICMPNE, label);
                break;
            case NOT_EQUAL_TO_ZERO:
                mv.visitJumpInsn(IFNE, label);
                break;
            case LESS_THAN_EQUAL_ZERO:
                mv.visitJumpInsn(IFLE, label);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
