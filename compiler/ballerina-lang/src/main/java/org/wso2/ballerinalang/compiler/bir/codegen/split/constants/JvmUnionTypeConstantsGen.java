/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.ballerinalang.compiler.bir.codegen.TypeNamePair;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmUnionTypeGen;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentSkipListMap;

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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_UNION_TYPE_INIT_METHOD_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_UNION_TYPE_IMPL;

/**
 * Generates Jvm class for the ballerina union types as constants for a given module.
 *
 * @since 2.0.0
 */
public class JvmUnionTypeConstantsGen {

    private final String unionVarConstantsClass;
    private int constantIndex = 0;
    private JvmUnionTypeGen jvmUnionTypeGen;
    private ClassWriter cw;
    private MethodVisitor mv;
    private int methodCount;
    private final List<String> funcNames;
    private final Queue<TypeNamePair> queue;
    private final Map<BUnionType, String> unionTypeVarMap;

    /**
     * Stack keeps track of recursion in union types. The method creation is performed only if recursion is completed.
     */
    public JvmUnionTypeConstantsGen(PackageID packageID) {
        unionVarConstantsClass = JvmCodeGenUtil.getModuleLevelClassName(packageID,
                JvmConstants.UNION_TYPE_CONSTANT_CLASS_NAME);
        generateUnionTypeConstantsClassInit();
        visitUnionTypeInitMethod();
        funcNames = new ArrayList<>();
        queue = new LinkedList<>();
        unionTypeVarMap = new ConcurrentSkipListMap<>(JvmConstantsGen.TYPE_HASH_COMPARATOR);
    }

    public void setJvmUnionTypeGen(JvmUnionTypeGen jvmUnionTypeGen) {
        this.jvmUnionTypeGen = jvmUnionTypeGen;
    }

    public String add(BUnionType type) {
        return unionTypeVarMap.computeIfAbsent(type, str -> generateBUnionInits(type));
    }

    private void generateUnionTypeConstantsClassInit() {
        cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, unionVarConstantsClass, null, OBJECT, null);

        MethodVisitor methodVisitor = cw.visitMethod(ACC_PRIVATE, JVM_INIT_METHOD, "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, OBJECT, JVM_INIT_METHOD, "()V", false);
        genMethodReturn(methodVisitor);
    }

    private void visitUnionTypeInitMethod() {
        mv = cw.visitMethod(ACC_STATIC, B_UNION_TYPE_INIT_METHOD_PREFIX + methodCount++,
                            "()V", null, null);
    }

    private String generateBUnionInits(BUnionType type) {
        String varName = JvmConstants.UNION_TYPE_VAR_PREFIX + constantIndex++;
        visitBUnionField(varName);
        createBunionType(mv, type, varName);
        // Queue is used here to avoid recursive calls to the genPopulateMethod. This can happen when a union
        // contains a union inside it.
        queue.add(new TypeNamePair(type, varName));
        if (queue.size() == 1) {
            genPopulateMethod(type, varName);
            queue.remove();
            while (!queue.isEmpty()) {
                TypeNamePair typeNamePair = queue.remove();
                genPopulateMethod((BUnionType) typeNamePair.type, typeNamePair.varName);
            }
        }
        return varName;
    }

    private void genPopulateMethod(BUnionType type, String varName) {
        String methodName = "$populate" + varName;
        funcNames.add(methodName);
        MethodVisitor methodVisitor = cw.visitMethod(ACC_STATIC, methodName, "()V", null, null);
        methodVisitor.visitCode();
        generateGetBUnionType(methodVisitor, varName);
        jvmUnionTypeGen.populateUnion(cw, methodVisitor, type, unionVarConstantsClass, varName);
        genMethodReturn(methodVisitor);
    }

    private void createBunionType(MethodVisitor mv, BUnionType unionType, String varName) {
        jvmUnionTypeGen.createUnionType(mv, unionType);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, unionVarConstantsClass, varName,
                          GET_UNION_TYPE_IMPL);
    }

    private void visitBUnionField(String varName) {
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, varName,
                                        GET_UNION_TYPE_IMPL, null, null);
        fv.visitEnd();
    }

    public void generateGetBUnionType(MethodVisitor mv, String varName) {
        mv.visitFieldInsn(GETSTATIC, unionVarConstantsClass, varName,
                          GET_UNION_TYPE_IMPL);
    }

    public void generateClass(Map<String, byte[]> jarEntries) {
        genMethodReturn(mv);
        visitUnionTypeInitMethod();
        for (String funcName : funcNames) {
            mv.visitMethodInsn(INVOKESTATIC, unionVarConstantsClass, funcName, "()V", false);
        }
        genMethodReturn(mv);
        generateStaticInitializer(cw);
        cw.visitEnd();
        jarEntries.put(unionVarConstantsClass + ".class", cw.toByteArray());
    }

    private void generateStaticInitializer(ClassWriter cw) {
        MethodVisitor methodVisitor = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        for (int i = 0; i < methodCount; i++) {
            methodVisitor.visitMethodInsn(INVOKESTATIC, unionVarConstantsClass,
                                         B_UNION_TYPE_INIT_METHOD_PREFIX + i,
                                          "()V", false);
        }
        genMethodReturn(methodVisitor);
    }

    private void genMethodReturn(MethodVisitor methodVisitor) {
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

}
