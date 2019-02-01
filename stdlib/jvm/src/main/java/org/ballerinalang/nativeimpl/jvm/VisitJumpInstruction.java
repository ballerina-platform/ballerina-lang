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

import java.util.Locale;

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
                @Argument(name = "jumpType", type = STRING),
                @Argument(name = "labelId", type = STRING),
        }
)
public class VisitJumpInstruction extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        MethodVisitor mv = ASMCodeGenerator.getInstance().getMethodVisitor();

        String jumpType = context.getStringArgument(0).toUpperCase(Locale.ENGLISH);
        String labelId = context.getStringArgument(1);
        Label label = ASMCodeGenerator.getInstance().getLabel(labelId);

        switch (JumpInstructionType.valueOf(jumpType)) {
            case GOTO:
                mv.visitJumpInsn(GOTO, label);
                break;
            case GREATER_THAN_0:
                mv.visitJumpInsn(IFGT, label);
                break;
            case LESS_THAN_0:
                mv.visitJumpInsn(IFLT, label);
                break;
            case IF_ICMPEQ:
                mv.visitJumpInsn(IF_ICMPEQ, label);
                break;
            case IF_ICMPNE:
                mv.visitJumpInsn(IF_ICMPNE, label);
                break;
            case NOT_EQUAL_0:
                mv.visitJumpInsn(IFNE, label);
                break;
            case LESS_THAN_EQUAL_0:
                mv.visitJumpInsn(IFLE, label);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    enum JumpInstructionType {
        GOTO, GREATER_THAN_0, LESS_THAN_0, NOT_EQUAL_0, LESS_THAN_EQUAL_0, IF_ICMPNE, IF_ICMPEQ;
    }
}
