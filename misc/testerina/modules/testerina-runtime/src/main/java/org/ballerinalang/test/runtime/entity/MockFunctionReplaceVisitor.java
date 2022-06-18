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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.ballerinalang.test.runtime.util.TesterinaConstants.ORIGINAL_FUNC_NAME_PREFIX;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FRETURN;
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
        MethodVisitor methodVisitor;
        if (!name.equals(methodName) || !desc.equals(methodDesc)) {
            // reproduce the methods where no changes needed
            methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        } else {
            // Rename function as $ORIG_<name> to restore the original function with a different name.
            methodVisitor = super.visitMethod(access, ORIGINAL_FUNC_NAME_PREFIX + name,
                    desc, signature, exceptions);

            // Create a new function with the same name of original function and call the mock function
            // $MOCK_<name> inside it.
            generateMethodWithMockFunctionCall(access, name, desc, signature, exceptions);
        }

        return methodVisitor;
    }

    private void generateMethodWithMockFunctionCall(int access, String name, String desc, String signature,
                                                    String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        mv.visitCode();
        Class<?>[] parameterTypes = mockFunc.getParameterTypes();
        int paramOffset = 0;
        for (Class<?> parameterType : parameterTypes) {
            generateLoadInstruction(mv, parameterType, paramOffset);
            if (parameterType == Long.TYPE || parameterType == Double.TYPE) {
                paramOffset += 2;
            } else {
                paramOffset++;
            }
        }

        String mockFuncClassName = mockFunc.getDeclaringClass().getName().replace(".", "/");
        mv.visitMethodInsn(INVOKESTATIC, mockFuncClassName, mockFunc.getName(),
                Type.getMethodDescriptor(mockFunc), false);

        generateReturnInstruction(mv, mockFunc.getReturnType());
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateLoadInstruction(MethodVisitor mv, Class<?> type, int index) {
        if (type.isPrimitive()) {
            if (type == Integer.TYPE || type == Boolean.TYPE) {
                mv.visitVarInsn(ILOAD, index);
            } else if (type == Long.TYPE) {
                mv.visitVarInsn(LLOAD, index);
            } else if (type == Float.TYPE) {
                mv.visitVarInsn(FLOAD, index);
            } else if (type == Double.TYPE) {
                mv.visitVarInsn(DLOAD, index);
            }
        } else {
            mv.visitVarInsn(ALOAD, index);
        }
    }

    private void generateReturnInstruction(MethodVisitor mv, Class<?> returnType) {
        if (returnType.isPrimitive()) {
            if (returnType == Integer.TYPE || returnType == Boolean.TYPE || returnType == Byte.TYPE) {
                mv.visitInsn(Opcodes.IRETURN);
            } else if (returnType == Long.TYPE) {
                mv.visitInsn(LRETURN);
            } else if (returnType == Float.TYPE) {
                mv.visitInsn(FRETURN);
            } else if (returnType == Double.TYPE) {
                mv.visitInsn(DRETURN);
            } else if (returnType == Void.TYPE) {
                mv.visitInsn(RETURN);
            }
        } else {
            mv.visitInsn(ARETURN);
        }
    }
}
