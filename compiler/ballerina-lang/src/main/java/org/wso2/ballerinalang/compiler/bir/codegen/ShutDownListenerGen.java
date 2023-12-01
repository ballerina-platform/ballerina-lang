/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.codegen;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V17;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JAVA_THREAD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STOP_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_REGISTRY_VARIABLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_RUNTIME_REGISTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_RUNTIME_REGISTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;

/**
 * Generate the thread for the addShutDownHook.
 *
 * @since 2.0.0
 */
public class ShutDownListenerGen {
    void generateShutdownSignalListener(String initClass, Map<String, byte[]> jarEntries) {
        String innerClassName = initClass + "$SignalListener";
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V17, ACC_SUPER, innerClassName, null, JAVA_THREAD, null);
        FieldVisitor fv = cw.visitField(ACC_PRIVATE , RUNTIME_REGISTRY_VARIABLE,
                GET_RUNTIME_REGISTRY, null, null);
        fv.visitEnd();

        // create constructor
        genConstructor(innerClassName, cw);

        // implement run() method
        genRunMethod(initClass, innerClassName, cw);

        cw.visitEnd();
        jarEntries.put(innerClassName + CLASS_FILE_SUFFIX, cw.toByteArray());
    }

    private void genConstructor(String innerClassName, ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, JVM_INIT_METHOD, INIT_RUNTIME_REGISTRY, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_THREAD, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, innerClassName , RUNTIME_REGISTRY_VARIABLE,
                GET_RUNTIME_REGISTRY);
        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, JVM_INIT_METHOD, innerClassName);
        mv.visitEnd();
    }

    private void genRunMethod(String initClass, String innerClassName, ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "run", VOID_METHOD_DESC, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, innerClassName , RUNTIME_REGISTRY_VARIABLE,
                GET_RUNTIME_REGISTRY);
        mv.visitMethodInsn(INVOKESTATIC, initClass, MODULE_STOP_METHOD,
                INIT_RUNTIME_REGISTRY, false);
        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, "run", innerClassName);
        mv.visitEnd();
    }
}
