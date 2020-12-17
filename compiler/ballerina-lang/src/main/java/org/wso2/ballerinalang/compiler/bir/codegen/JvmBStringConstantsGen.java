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
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BStringInfo;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BMP_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STRING_CONSTANT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.NON_BMP_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;

/**
 * Generates Jvm class for the ballerina string constants for given module.
 *
 * @since 2.0.0
 */
public class JvmBStringConstantsGen {

    private ConcurrentHashMap<String, BStringInfo> bStringInfoMap;

    private String stringConstantsClass;

    private AtomicInteger constantIndex = new AtomicInteger();

    public JvmBStringConstantsGen(BIRNode.BIRPackage module) {
        this.bStringInfoMap = new ConcurrentHashMap<>();
        this.stringConstantsClass = getModuleLevelClassName(module.org.value, module.name.value,
                                                            module.version.value, MODULE_STRING_CONSTANT_CLASS_NAME);
    }

    public String addBString(String val, int[] highSurrogates) {
        return bStringInfoMap.computeIfAbsent(val, s ->
                new BStringInfo(val, JvmConstants.B_STRING_VAR_PREFIX + constantIndex.getAndIncrement(),
                                highSurrogates)).varName;
    }

    public void generateConstantInit(Map<String, byte[]> jarEntries) {
        if (bStringInfoMap.isEmpty()) {
            return;
        }
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, stringConstantsClass, null, OBJECT, null);

        MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, JVM_INIT_METHOD, "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, OBJECT, JVM_INIT_METHOD, "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        bStringInfoMap.values().forEach(bString -> visitBStringField(cw, bString.varName, bString.isNonBMPString));
        generateStaticInitializer(cw);
        cw.visitEnd();
        jarEntries.put(stringConstantsClass + ".class", cw.toByteArray());
    }

    private void visitBStringField(ClassWriter cw, String varName, boolean isNonBMPString) {
        FieldVisitor fv;
        if (isNonBMPString) {
            fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, varName,
                               String.format("L%s;", NON_BMP_STRING_VALUE), null, null);
        } else {
            fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, varName, String.format("L%s;", BMP_STRING_VALUE),
                               null, null);
        }
        fv.visitEnd();
    }

    private void generateStaticInitializer(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        for (Map.Entry<String, BStringInfo> entry : bStringInfoMap.entrySet()) {
            BStringInfo bStringInfo = entry.getValue();
            if (bStringInfo.isNonBMPString) {
                createNonBmpString(mv, bStringInfo.value, bStringInfo.highSurrogates, bStringInfo.varName);
            } else {
                createBmpString(mv, bStringInfo.value, bStringInfo.varName);
            }
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createBmpString(MethodVisitor mv, String val, String varName) {
        mv.visitTypeInsn(NEW, BMP_STRING_VALUE);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(val);
        mv.visitMethodInsn(INVOKESPECIAL, BMP_STRING_VALUE, JVM_INIT_METHOD,
                           String.format("(L%s;)V", STRING_VALUE), false);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, stringConstantsClass, varName, String.format("L%s;", BMP_STRING_VALUE));
    }

    private void createNonBmpString(MethodVisitor mv, String val, int[] highSurrogates, String varName) {
        mv.visitTypeInsn(NEW, NON_BMP_STRING_VALUE);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(val);
        mv.visitIntInsn(BIPUSH, highSurrogates.length);
        mv.visitIntInsn(NEWARRAY, T_INT);

        int i = 0;
        for (int ch : highSurrogates) {
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, i);
            mv.visitIntInsn(BIPUSH, ch);
            i = i + 1;
            mv.visitInsn(IASTORE);
        }
        mv.visitMethodInsn(INVOKESPECIAL, NON_BMP_STRING_VALUE, JVM_INIT_METHOD,
                           String.format("(L%s;[I)V", STRING_VALUE), false);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, stringConstantsClass, varName,
                          String.format("L%s;", NON_BMP_STRING_VALUE));
    }

    public String getStringConstantsClass() {
        return stringConstantsClass;
    }
}
