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

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BTypeHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmRefTypeGen;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_CONSTANTS_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_TYPEREF_TYPE_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_TYPEREF_TYPE_POPULATE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPE_REF_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.generateConstantsClassInit;

/**
 * Generates Jvm class for the ballerina type reference types as constants for a given module.
 *
 * @since 2201.2.0
 */
public class JvmRefTypeConstantsGen {

    private final String typeRefVarConstantsClass;
    private JvmRefTypeGen jvmRefTypeGen;
    private final List<String> funcNames;

    private final Map<BTypeReferenceType, String> typeRefVarMap;

    public JvmRefTypeConstantsGen(PackageID packageID, BTypeHashComparator bTypeHashComparator) {
        typeRefVarConstantsClass = JvmCodeGenUtil.getModuleLevelClassName(packageID,
                JvmConstants.TYPEREF_TYPE_CONSTANT_CLASS_NAME);
        funcNames = new ArrayList<>();
        typeRefVarMap = new TreeMap<>(bTypeHashComparator);
    }

    public void setJvmRefTypeGen(JvmRefTypeGen jvmRefTypeGen) {
        this.jvmRefTypeGen = jvmRefTypeGen;
    }

    public String add(BTypeReferenceType type) {
        String varName = typeRefVarMap.get(type);
        if (varName == null) {
            varName = JvmCodeGenUtil.getRefTypeConstantName(type);
            typeRefVarMap.put(type, varName);
        }
        return varName;
    }

    public void generateClass(Map<String, byte[]> jarEntries) {
        if (typeRefVarMap.isEmpty()) {
            return;
        }

        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        generateConstantsClassInit(cw, typeRefVarConstantsClass);

        visitTypeRefTypeInitMethod(cw);

        visitRefTypePopulateInitMethod(cw);

        generateStaticInitializer(cw);
        cw.visitEnd();
        jarEntries.put(typeRefVarConstantsClass + ".class", cw.toByteArray());
    }

    private void visitTypeRefTypeInitMethod(ClassWriter cw) {
        int typeDefCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        for (Map.Entry<BTypeReferenceType, String> entry : typeRefVarMap.entrySet()) {
            BTypeReferenceType type = entry.getKey();
            String varName = entry.getValue();
            visitTypeRefField(cw, varName);
            if (typeDefCount % MAX_CONSTANTS_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_STATIC, B_TYPEREF_TYPE_INIT_METHOD + methodCount++, "()V", null, null);
            }
            createTypeRefType(mv, type, varName);
            genPopulateMethod(cw, type, varName);

            typeDefCount++;
            if (typeDefCount % MAX_CONSTANTS_PER_METHOD == 0) {
                if (typeDefCount != typeRefVarMap.size()) {
                    mv.visitMethodInsn(INVOKESTATIC, typeRefVarConstantsClass,
                            B_TYPEREF_TYPE_INIT_METHOD + methodCount, "()V", false);
                }
                genMethodReturn(mv);
            }
        }

        if (typeDefCount % MAX_CONSTANTS_PER_METHOD != 0) {
            genMethodReturn(mv);
        }
    }

    private void visitRefTypePopulateInitMethod(ClassWriter cw) {
        int populateFuncCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        for (String funcName : funcNames) {
            if (populateFuncCount % MAX_CONSTANTS_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_STATIC, B_TYPEREF_TYPE_POPULATE_METHOD + methodCount++,
                        "()V", null, null);
            }
            mv.visitMethodInsn(INVOKESTATIC, typeRefVarConstantsClass, funcName, "()V", false);

            populateFuncCount++;
            if (populateFuncCount % MAX_CONSTANTS_PER_METHOD == 0) {
                if (populateFuncCount != funcNames.size()) {
                    mv.visitMethodInsn(INVOKESTATIC, typeRefVarConstantsClass,
                            B_TYPEREF_TYPE_POPULATE_METHOD + methodCount, "()V", false);
                }
                genMethodReturn(mv);
            }
        }

        if (populateFuncCount % MAX_CONSTANTS_PER_METHOD != 0) {
            genMethodReturn(mv);
        }
    }

    private void genPopulateMethod(ClassWriter cw, BTypeReferenceType referenceType, String varName) {
        String methodName = "$populate" + varName;
        funcNames.add(methodName);
        MethodVisitor methodVisitor = cw.visitMethod(ACC_STATIC, methodName, "()V", null, null);
        methodVisitor.visitCode();
        generateGetBTypeRefType(methodVisitor, varName);
        jvmRefTypeGen.populateTypeRef(methodVisitor, referenceType);
        genMethodReturn(methodVisitor);
    }

    private void createTypeRefType(MethodVisitor mv, BTypeReferenceType type, String varName) {
        jvmRefTypeGen.createTypeRefType(mv, type);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, typeRefVarConstantsClass, varName,
                GET_TYPE_REF_TYPE_IMPL);
    }

    private void visitTypeRefField(ClassWriter cw, String varName) {
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, varName,
                GET_TYPE_REF_TYPE_IMPL, null, null);
        fv.visitEnd();
    }

    public void generateGetBTypeRefType(MethodVisitor mv, String varName) {
        mv.visitFieldInsn(GETSTATIC, typeRefVarConstantsClass, varName, GET_TYPE_REF_TYPE_IMPL);
    }

    private void generateStaticInitializer(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, "()V", null, null);
        mv.visitMethodInsn(INVOKESTATIC, typeRefVarConstantsClass, B_TYPEREF_TYPE_INIT_METHOD + 0,
                "()V", false);
        mv.visitMethodInsn(INVOKESTATIC, typeRefVarConstantsClass, B_TYPEREF_TYPE_POPULATE_METHOD + 0,
                "()V", false);
        genMethodReturn(mv);
    }

    public String getRefTypeConstantsClass() {
        return this.typeRefVarConstantsClass;
    }
}
