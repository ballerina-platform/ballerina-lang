/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.codegen.split.constants;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

/**
 * The common functions used in jvm constants generation.
 *
 * @since 2201.2.0
 */
public class JvmConstantGenCommons {

    private JvmConstantGenCommons() {
    }

    public static void generateConstantsClassInit(ClassWriter cw, String constantsClass) {
        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, constantsClass, null, JvmConstants.OBJECT, null);
        MethodVisitor methodVisitor = cw.visitMethod(ACC_PRIVATE, JvmConstants.JVM_INIT_METHOD, "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, JvmConstants.OBJECT, JvmConstants.JVM_INIT_METHOD, "()V", false);
        genMethodReturn(methodVisitor);
    }

    public static void genMethodReturn(MethodVisitor methodVisitor) {
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

}
