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
package org.wso2.ballerinalang.compiler.bir.codegen.stringgen;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.NON_BMP_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;

/**
 * Class which contains util methods required for ballerina string constant generation .
 *
 * @since 2.0.0
 */
public class JVMBStringGenUtils {

    /*
     MAX_STRINGS_PER_METHOD is calculated as below.
        No of instructions required for create ballerina string constant object = 12
        Java method limit = 64000
        Max strings constant initializations per method = 64000/12 -> 5000
     */
    private static final int MAX_STRINGS_PER_METHOD = 5000;

    public static void generateConstantInit(Map<String, byte[]> jarEntries,
                                            String stringConstantsClass,
                                            Map<String, JvmBStringConstant> bStringVarMap) {
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

        bStringVarMap.values().forEach(bStringVar -> visitBStringField(cw, bStringVar.varName));
        // Create multiple string constant init methods based on string count.
        generateBStringInits(cw, stringConstantsClass, bStringVarMap);
        // Create static initializer which will call previously generated string init methods.
        generateStaticInitializer(cw, stringConstantsClass, bStringVarMap);
        cw.visitEnd();
        jarEntries.put(stringConstantsClass + ".class", cw.toByteArray());
    }

    private static void visitBStringField(ClassWriter cw, String varName) {
        FieldVisitor fv;
        fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, varName,
                           String.format("L%s;", B_STRING_VALUE), null,
                           null);
        fv.visitEnd();
    }

    private static void generateBStringInits(ClassWriter cw, String stringConstantsClass,
                                             Map<String, JvmBStringConstant> bStringVarMap) {
        MethodVisitor mv = null;
        int bStringCount = 0;
        int methodCount = 0;
        for (Map.Entry<String, JvmBStringConstant> entry : bStringVarMap.entrySet()) {
            if (bStringCount % MAX_STRINGS_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_STATIC, B_STRING_INIT_METHOD_PREFIX + methodCount++, "()V", null, null);
            }
            String bString = entry.getKey();
            String bStringVarName = entry.getValue().varName;
            int[] highSurrogates = listHighSurrogates(bString);
            if (highSurrogates.length > 0) {
                createNonBmpString(mv, stringConstantsClass, bString, highSurrogates, bStringVarName);
            } else {
                createBmpString(mv, stringConstantsClass, bString, bStringVarName);
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

    private static void generateStaticInitializer(ClassWriter cw, String stringConstantsClass,
                                                  Map<String, JvmBStringConstant> bStringVarMap) {
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        int methodIndex = (bStringVarMap.size() - 1) / MAX_STRINGS_PER_METHOD;
        for (int i = 0; i <= methodIndex; i++) {
            mv.visitMethodInsn(INVOKESTATIC, stringConstantsClass, B_STRING_INIT_METHOD_PREFIX + i, "()V", false);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void createBmpString(MethodVisitor mv, String stringConstantsClass, String val, String varName) {
        mv.visitTypeInsn(NEW, BMP_STRING_VALUE);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(val);
        mv.visitMethodInsn(INVOKESPECIAL, BMP_STRING_VALUE, JVM_INIT_METHOD,
                           String.format("(L%s;)V", STRING_VALUE), false);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, stringConstantsClass, varName, String.format("L%s;", B_STRING_VALUE));
    }

    private static void createNonBmpString(MethodVisitor mv, String stringConstantsClass, String val,
                                           int[] highSurrogates, String varName) {
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
        mv.visitFieldInsn(Opcodes.PUTSTATIC, stringConstantsClass, varName, String.format("L%s;", B_STRING_VALUE));
    }

    private static int[] listHighSurrogates(String str) {
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
