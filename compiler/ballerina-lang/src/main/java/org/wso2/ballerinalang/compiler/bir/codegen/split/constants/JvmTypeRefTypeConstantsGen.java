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
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmTypeRefTypeGen;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_TYPEREF_TYPE_INIT_METHOD_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPE_REF_TYPE_IMPL;

/**
 * Generates Jvm class for the ballerina type reference types as constants for a given module.
 *
 * @since 2.0.0
 */
public class JvmTypeRefTypeConstantsGen {

    private final String typeRefVarConstantsClass;
    private JvmTypeRefTypeGen jvmTypeRefTypeGen;
    private ClassWriter cw;
    private MethodVisitor mv;
    private int methodCount;
    private final List<String> funcNames;
    private final Map<BTypeReferenceType, String> typeRefVarMap;

    /**
     * Stack keeps track of recursion in union types. The method creation is performed only if recursion is completed.
     */
    public JvmTypeRefTypeConstantsGen(PackageID packageID, BTypeHashComparator bTypeHashComparator) {
        typeRefVarConstantsClass = JvmCodeGenUtil.getModuleLevelClassName(packageID,
                JvmConstants.TYPEREF_TYPE_CONSTANT_CLASS_NAME);
        generateUnionTypeConstantsClassInit();
        visitTypeRefTypeInitMethod();
        funcNames = new ArrayList<>();
        typeRefVarMap = new TreeMap<>(bTypeHashComparator);
    }

    public void setJvmTypeRefTypeGen(JvmTypeRefTypeGen jvmTypeRefTypeGen) {
        this.jvmTypeRefTypeGen = jvmTypeRefTypeGen;
    }

    public String add(BTypeReferenceType type) {
        return typeRefVarMap.computeIfAbsent(type, str -> generateTypeRefInits(type));
    }

    private void generateUnionTypeConstantsClassInit() {
        cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, typeRefVarConstantsClass, null, OBJECT, null);

        MethodVisitor methodVisitor = cw.visitMethod(ACC_PRIVATE, JVM_INIT_METHOD, "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, OBJECT, JVM_INIT_METHOD, "()V", false);
        genMethodReturn(methodVisitor);
    }

    private void visitTypeRefTypeInitMethod() {
        mv = cw.visitMethod(ACC_STATIC, B_TYPEREF_TYPE_INIT_METHOD_PREFIX + methodCount++, "()V", null, null);
    }

    private String generateTypeRefInits(BTypeReferenceType type) {
        String varName = JvmConstants.TYPEREF_TYPE_VAR_PREFIX + type.tsymbol.name;
        visitTypeRefField(varName);
        createTypeRefType(mv, type, varName);
        return varName;
    }

    private void createTypeRefType(MethodVisitor mv, BTypeReferenceType type, String varName) {
        jvmTypeRefTypeGen.createTypeRefType(mv, type);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, typeRefVarConstantsClass, varName,
                GET_TYPE_REF_TYPE_IMPL);
    }

    private void visitTypeRefField(String varName) {
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, varName,
                GET_TYPE_REF_TYPE_IMPL, null, null);
        fv.visitEnd();
    }

    public void generateGetBTypeRefType(MethodVisitor mv, String varName) {
        mv.visitFieldInsn(GETSTATIC, typeRefVarConstantsClass, varName, GET_TYPE_REF_TYPE_IMPL);
    }

    public void generateClass(Map<String, byte[]> jarEntries) {
        genMethodReturn(mv);
        visitTypeRefTypeInitMethod();
        for (String funcName : funcNames) {
            mv.visitMethodInsn(INVOKESTATIC, typeRefVarConstantsClass, funcName, "()V", false);
        }
        genMethodReturn(mv);
        generateStaticInitializer(cw);
        cw.visitEnd();
        jarEntries.put(typeRefVarConstantsClass + ".class", cw.toByteArray());
    }

    private void generateStaticInitializer(ClassWriter cw) {
        MethodVisitor methodVisitor = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        for (int i = 0; i < methodCount; i++) {
            methodVisitor.visitMethodInsn(INVOKESTATIC, typeRefVarConstantsClass,
                    B_TYPEREF_TYPE_INIT_METHOD_PREFIX + i, "()V", false);
        }
        genMethodReturn(methodVisitor);
    }

    private void genMethodReturn(MethodVisitor methodVisitor) {
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

}
