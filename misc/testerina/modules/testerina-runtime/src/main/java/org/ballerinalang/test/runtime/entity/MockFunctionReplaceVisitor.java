/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.runtime.entity;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.ballerinalang.test.runtime.util.TesterinaConstants.IS_CALL_ORIGINAL;
import static org.ballerinalang.test.runtime.util.TesterinaConstants.TESTERINA_UTILS;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 * Remove existing method body and replace it with a method call.
 *
 * @since 2201.1.0
 */
public class MockFunctionReplaceVisitor extends ClassVisitor {

    private final String methodName;
    private final String methodDesc;
    private final Method mockFunc;

    public MockFunctionReplaceVisitor(int api, ClassWriter cw, String name, String methodDescriptor, Method mockFunc) {
        super(api, cw);
        this.methodName = name;
        this.methodDesc = methodDescriptor;
        this.mockFunc = mockFunc;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

        if (!name.equals(methodName) || !desc.equals(methodDesc)) {
            // reproduce the methods where no changes needed
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        return new MethodReplaceAdapter(
                super.visitMethod(access, name, desc, signature, exceptions), mockFunc);
    }


    private static class MethodReplaceAdapter extends MethodVisitor {

        private final Method mockFunc;

        MethodReplaceAdapter(MethodVisitor mv, Method mockFunc) {
            super(Opcodes.ASM7, mv);
            this.mockFunc = mockFunc;
        }

        @Override
        public void visitCode() {

            Label label0 = new Label();
            super.visitLabel(label0);
            super.visitMethodInsn(INVOKESTATIC, TESTERINA_UTILS, IS_CALL_ORIGINAL, "()Z", false);
            Label label1 = new Label();
            super.visitJumpInsn(IFNE, label1);
            Label label2 = new Label();
            super.visitLabel(label2);

            Class<?>[] parameterTypes = mockFunc.getParameterTypes();
            int paramOffset = 0;
            for (Class<?> parameterType : parameterTypes) {
                generateLoadInstruction(parameterType, paramOffset);
                if (parameterType == Long.TYPE || parameterType == Double.TYPE) {
                    paramOffset += 2;
                } else {
                    paramOffset++;
                }
            }

            String mockFuncClassName = mockFunc.getDeclaringClass().getName().replace(".", "/");
            super.visitMethodInsn(INVOKESTATIC, mockFuncClassName, mockFunc.getName(),
                    Type.getMethodDescriptor(mockFunc), false);

            generateReturnInstruction(mockFunc.getReturnType());
            super.visitLabel(label1);
            super.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            super.visitCode();
        }

        private void generateLoadInstruction(Class<?> type, int index) {
            if (type.isPrimitive()) {
                if (type == Integer.TYPE || type == Boolean.TYPE) {
                    super.visitVarInsn(ILOAD, index);
                } else if (type == Long.TYPE) {
                    super.visitVarInsn(LLOAD, index);
                } else if (type == Float.TYPE) {
                    super.visitVarInsn(FLOAD, index);
                } else if (type == Double.TYPE) {
                    super.visitVarInsn(DLOAD, index);
                }
            } else {
                super.visitVarInsn(ALOAD, index);
            }
        }

        private void generateReturnInstruction(Class<?> returnType) {
            if (returnType.isPrimitive()) {
                if (returnType == Integer.TYPE || returnType == Boolean.TYPE || returnType == Byte.TYPE) {
                    super.visitInsn(Opcodes.IRETURN);
                } else if (returnType == Long.TYPE) {
                    super.visitInsn(LRETURN);
                } else if (returnType == Float.TYPE) {
                    super.visitInsn(FRETURN);
                } else if (returnType == Double.TYPE) {
                    super.visitInsn(DRETURN);
                } else if (returnType == Void.TYPE) {
                    super.visitInsn(RETURN);
                }
            } else {
                super.visitInsn(ARETURN);
            }
        }
    }
}
