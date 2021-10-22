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

package org.wso2.ballerinalang.compiler.bir.codegen.split.constants;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;

import java.util.ArrayList;
import java.util.List;
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
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BMP_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_INIT_METHOD_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_STRINGS_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STRING_CONSTANT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.NON_BMP_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_BSTRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_NON_BMP_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_WITH_STRING;

/**
 * Generates Jvm class for the ballerina string constants for given module.
 *
 * @since 2.0.0
 */
public class JvmBStringConstantsGen {

    private final Map<String, String> bStringVarMap;

    private final String stringConstantsClass;

    private final AtomicInteger constantIndex = new AtomicInteger();

    public JvmBStringConstantsGen(PackageID module) {
        this.bStringVarMap = new ConcurrentHashMap<>();
        this.stringConstantsClass = JvmCodeGenUtil.getModuleLevelClassName(module, MODULE_STRING_CONSTANT_CLASS_NAME);
    }

    public String addBString(String val) {
        return bStringVarMap.computeIfAbsent(val, s ->
               B_STRING_VAR_PREFIX + constantIndex.getAndIncrement());
    }

    public void generateConstantInit(Map<String, byte[]> jarEntries) {
        if (bStringVarMap.isEmpty()) {
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

        bStringVarMap.values().forEach(bStringVar -> visitBStringField(cw, bStringVar));
        // Create multiple string constant init methods based on string count.
        generateBStringInits(cw);
        // Create static initializer which will call previously generated string init methods.
        generateStaticInitializer(cw);
        cw.visitEnd();
        jarEntries.put(stringConstantsClass + ".class", cw.toByteArray());
    }

    private void visitBStringField(ClassWriter cw, String varName) {
        FieldVisitor fv;
        fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, varName,
                           GET_BSTRING, null,
                           null);
        fv.visitEnd();
    }

    private void generateBStringInits(ClassWriter cw) {
        MethodVisitor mv = null;
        int bStringCount = 0;
        int methodCount = 0;
        for (Map.Entry<String, String> entry : bStringVarMap.entrySet()) {
            if (bStringCount % MAX_STRINGS_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_STATIC, B_STRING_INIT_METHOD_PREFIX + methodCount++, "()V", null, null);
            }
            String bString = entry.getKey();
            String bStringVarName = entry.getValue();
            int[] highSurrogates = listHighSurrogates(bString);
            if (highSurrogates.length > 0) {
                createNonBmpString(mv, bString, highSurrogates, bStringVarName);
            } else {
                createBmpString(mv, bString, bStringVarName);
            }
            bStringCount++;
            if (bStringCount % MAX_STRINGS_PER_METHOD == 0) {
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
        // Visit the previously started string init method if not ended.
        if (bStringCount % MAX_STRINGS_PER_METHOD != 0) {
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private void generateStaticInitializer(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, "()V", null, null);
        int methodIndex = (bStringVarMap.size() - 1) / MAX_STRINGS_PER_METHOD;
        for (int i = 0; i <= methodIndex; i++) {
            mv.visitMethodInsn(INVOKESTATIC, stringConstantsClass, B_STRING_INIT_METHOD_PREFIX + i, "()V", false);
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
                           INIT_WITH_STRING, false);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, stringConstantsClass, varName, GET_BSTRING);
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
                           INIT_NON_BMP_STRING_VALUE, false);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, stringConstantsClass, varName, GET_BSTRING);
    }

    public String getStringConstantsClass() {
        return stringConstantsClass;
    }


    private int[] listHighSurrogates(String str) {
        List<Integer> highSurrogates = new ArrayList<>();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isHighSurrogate(c)) {
                highSurrogates.add(i - highSurrogates.size());
            }
        }
        int[] highSurrogatesArr = new int[highSurrogates.size()];
        for (int i = 0; i < highSurrogates.size(); i++) {
            Integer highSurrogate = highSurrogates.get(i);
            highSurrogatesArr[i] = highSurrogate;
        }
        return highSurrogatesArr;
    }
}
