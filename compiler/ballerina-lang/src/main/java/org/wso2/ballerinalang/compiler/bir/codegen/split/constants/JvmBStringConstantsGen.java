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
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BMP_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_INIT_METHOD_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LARGE_STRING_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_STRINGS_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STRING_CONSTANT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.NON_BMP_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_BUILDER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_BSTRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_NON_BMP_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_WITH_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.STRING_BUILDER_APPEND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TO_STRING_RETURN;

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
        this.bStringVarMap = new HashMap<>();
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
        Map<String, Map<String, String>> stringVarMap = splitLargeStrings();

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
        stringVarMap.values().forEach(stringVar -> visitStringField(cw, stringVar));

        // Create multiple string constant init methods based on string count.
        generateBStringInits(cw, stringVarMap);
        // Create static initializer which will call previously generated string init methods.
        generateStaticInitializer(cw);
        cw.visitEnd();
        jarEntries.put(stringConstantsClass + ".class", cw.toByteArray());
    }

    private void visitStringField(ClassWriter cw, Map<String, String> varList) {
        FieldVisitor fv;
        for (String varName : varList.keySet()) {
            fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, varName, GET_STRING, null, null);
            fv.visitEnd();
        }

    }

    private String getStringVarName(String bStringVar, int index) {
        return bStringVar + LARGE_STRING_VAR_PREFIX + index;
    }

    private Map<String, Map<String, String>> splitLargeStrings() {
        Map<String, Map<String, String>> stringChunkMap = new HashMap<>();
        for (Map.Entry<String, String> entry: bStringVarMap.entrySet()) {
            String str = entry.getKey();
            int length = str.length();
            if (length <= 65535) {
                continue;
            }
            int chunkSize = 64000;
            Map<String, String> splitStrings = new LinkedHashMap<>();
            String varName = entry.getValue();
            for (int i = 0; i < (length / chunkSize) + 1; i++) {
                int beginIndex = i * chunkSize;
                int endIndex = Math.min(beginIndex + chunkSize + 1, length);
                splitStrings.put(getStringVarName(varName, i), str.substring(beginIndex, endIndex));
            }
            if (!splitStrings.isEmpty()) {
                stringChunkMap.put(varName, splitStrings);
            }
        }
        return stringChunkMap;
    }

    private void visitBStringField(ClassWriter cw, String varName) {
        FieldVisitor fv;
        fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, varName,
                           GET_BSTRING, null,
                           null);
        fv.visitEnd();
    }

    private void generateBStringInits(ClassWriter cw, Map<String, Map<String, String>> stringVarMap) {
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
            if (stringVarMap.containsKey(bStringVarName)) {
                Map<String, String> stringChunkMap = stringVarMap.get(bStringVarName);
                createConcatenatedBString(mv, highSurrogates, bStringVarName, stringChunkMap);
                bStringCount += stringChunkMap.size() + 1; //
            } else {
                createDirectBString(mv, bString, bStringVarName, highSurrogates);
                bStringCount++;
            }
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

    private void createConcatenatedBString(MethodVisitor mv, int[] highSurrogates, String bStringVarName,
                                           Map<String, String> stringChunks) {
        for (Map.Entry<String, String> stringEntry : stringChunks.entrySet()) {
            mv.visitLdcInsn(stringEntry.getValue());
            mv.visitFieldInsn(PUTSTATIC, stringConstantsClass, stringEntry.getKey(), GET_STRING);
        }
        if (highSurrogates.length > 0) {
            mv.visitTypeInsn(NEW, NON_BMP_STRING_VALUE);
            generateAppendStringConstants(mv, stringChunks);
            generateHighSurrogatesArray(mv, highSurrogates);
            mv.visitMethodInsn(INVOKESPECIAL, NON_BMP_STRING_VALUE, JVM_INIT_METHOD, INIT_NON_BMP_STRING_VALUE, false);
        } else {
            mv.visitTypeInsn(NEW, BMP_STRING_VALUE);
            generateAppendStringConstants(mv, stringChunks);
            mv.visitMethodInsn(INVOKESPECIAL, BMP_STRING_VALUE, JVM_INIT_METHOD, INIT_WITH_STRING, false);
        }
        mv.visitFieldInsn(PUTSTATIC, stringConstantsClass, bStringVarName, GET_BSTRING);
    }

    private void generateAppendStringConstants(MethodVisitor mv, Map<String, String> stringChunks) {
        mv.visitInsn(DUP);
        mv.visitTypeInsn(NEW, STRING_BUILDER);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, STRING_BUILDER, JVM_INIT_METHOD, "()V", false);
        for (Map.Entry<String, String> stringEntry : stringChunks.entrySet()) {
            mv.visitFieldInsn(GETSTATIC, stringConstantsClass, stringEntry.getKey(), GET_STRING);
            mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER, "append", STRING_BUILDER_APPEND, false);
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER, "toString", TO_STRING_RETURN, false);
    }

    private void createDirectBString(MethodVisitor mv, String bString, String bStringVarName, int[] highSurrogates) {
        if (highSurrogates.length > 0) {
            createNonBmpString(mv, bString, highSurrogates, bStringVarName);
        } else {
            createBmpString(mv, bString, bStringVarName);
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
        mv.visitFieldInsn(PUTSTATIC, stringConstantsClass, varName, GET_BSTRING);
    }

    private void createNonBmpString(MethodVisitor mv, String val, int[] highSurrogates, String varName) {
        mv.visitTypeInsn(NEW, NON_BMP_STRING_VALUE);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(val);
        generateHighSurrogatesArray(mv, highSurrogates);
        mv.visitMethodInsn(INVOKESPECIAL, NON_BMP_STRING_VALUE, JVM_INIT_METHOD, INIT_NON_BMP_STRING_VALUE, false);
        mv.visitFieldInsn(PUTSTATIC, stringConstantsClass, varName, GET_BSTRING);
    }

    private void generateHighSurrogatesArray(MethodVisitor mv, int[] highSurrogates) {
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
