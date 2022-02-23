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
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BTypeHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmTupleTypeGen;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_TUPLE_TYPE_INIT_METHOD_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TUPLE_TYPE_IMPL;

/**
 * Generates Jvm class for the ballerina tuple types as constants for a given module.
 *
 * @since 2.0.0
 */
public class JvmTupleTypeConstantsGen {

    private final String tupleVarConstantsClass;
    private int constantIndex = 0;
    private JvmTupleTypeGen jvmTupleTypeGen;
    private ClassWriter cw;
    private MethodVisitor mv;
    private int methodCount;
    private final List<String> funcNames;
    private final Queue<TypeNamePair> queue;
    private final Map<BTupleType, String> tupleTypeVarMap;

    public JvmTupleTypeConstantsGen(PackageID packageID, BTypeHashComparator bTypeHashComparator) {
        tupleVarConstantsClass = JvmCodeGenUtil.getModuleLevelClassName(packageID,
                JvmConstants.TUPLE_TYPE_CONSTANT_CLASS_NAME);
        generateTupleTypeConstantsClassInit();
        visitTupleTypeInitMethod();
        funcNames = new ArrayList<>();
        queue = new LinkedList<>();
        tupleTypeVarMap = new TreeMap<>(bTypeHashComparator);
    }

    public void setJvmTupleTypeGen(JvmTupleTypeGen jvmTupleTypeGen) {
        this.jvmTupleTypeGen = jvmTupleTypeGen;
    }

    public String add(BTupleType type) {
        return tupleTypeVarMap.computeIfAbsent(type, str -> generateBTupleInits(type));
    }

    private void generateTupleTypeConstantsClassInit() {
        cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, tupleVarConstantsClass, null, OBJECT, null);

        MethodVisitor methodVisitor = cw.visitMethod(ACC_PRIVATE, JVM_INIT_METHOD, "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, OBJECT, JVM_INIT_METHOD, "()V", false);
        genMethodReturn(methodVisitor);
    }

    private void visitTupleTypeInitMethod() {
        mv = cw.visitMethod(ACC_STATIC, B_TUPLE_TYPE_INIT_METHOD_PREFIX + methodCount++,
                "()V", null, null);
    }

    /**
     * Stack keeps track of recursion in tuple types. The method creation is performed only if recursion is completed.
     */
    private String generateBTupleInits(BTupleType type) {
        String varName = JvmConstants.TUPLE_TYPE_VAR_PREFIX + constantIndex++;
        visitBTupleField(varName);
        createBTupleType(mv, type, varName);
        // Queue is used here to avoid recursive calls to the genPopulateMethod. This can happen when a tuple
        // contains a tuple inside it.
        queue.add(new TypeNamePair(type, varName));
        if (queue.size() == 1) {
            genPopulateMethod(type, varName);
            queue.remove();
            while (!queue.isEmpty()) {
                TypeNamePair typeNamePair = queue.remove();
                genPopulateMethod((BTupleType) typeNamePair.type, typeNamePair.varName);
            }
        }
        return varName;
    }

    private void genPopulateMethod(BTupleType type, String varName) {
        String methodName = "$populate" + varName;
        funcNames.add(methodName);
        MethodVisitor methodVisitor = cw.visitMethod(ACC_STATIC, methodName, "()V", null, null);
        methodVisitor.visitCode();
        generateGetBTupleType(methodVisitor, varName);
        jvmTupleTypeGen.populateTuple(methodVisitor, type);
        genMethodReturn(methodVisitor);
    }

    private void createBTupleType(MethodVisitor mv, BTupleType tupleType, String varName) {
        jvmTupleTypeGen.createTupleType(mv, tupleType);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, tupleVarConstantsClass, varName,
                GET_TUPLE_TYPE_IMPL);
    }

    private void visitBTupleField(String varName) {
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, varName,
                GET_TUPLE_TYPE_IMPL, null, null);
        fv.visitEnd();
    }

    public void generateGetBTupleType(MethodVisitor mv, String varName) {
        mv.visitFieldInsn(GETSTATIC, tupleVarConstantsClass, varName, GET_TUPLE_TYPE_IMPL);
    }

    public void generateClass(Map<String, byte[]> jarEntries) {
        genMethodReturn(mv);
        visitTupleTypeInitMethod();
        for (String funcName : funcNames) {
            mv.visitMethodInsn(INVOKESTATIC, tupleVarConstantsClass, funcName, "()V", false);
        }
        genMethodReturn(mv);
        generateStaticInitializer(cw);
        cw.visitEnd();
        jarEntries.put(tupleVarConstantsClass + ".class", cw.toByteArray());
    }

    private void generateStaticInitializer(ClassWriter cw) {
        MethodVisitor methodVisitor = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        for (int i = 0; i < methodCount; i++) {
            methodVisitor.visitMethodInsn(INVOKESTATIC, tupleVarConstantsClass,
                   B_TUPLE_TYPE_INIT_METHOD_PREFIX + i, "()V", false);
        }
        genMethodReturn(methodVisitor);
    }

    private void genMethodReturn(MethodVisitor methodVisitor) {
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }
}
