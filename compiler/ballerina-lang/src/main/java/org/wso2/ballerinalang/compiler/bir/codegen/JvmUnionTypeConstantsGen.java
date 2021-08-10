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

package org.wso2.ballerinalang.compiler.bir.codegen;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
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

/**
 * Generates Jvm class for the ballerina union types as constants for a given module.
 *
 * @since 2.0.0
 */
public class JvmUnionTypeConstantsGen {

    private final Map<BUnionType, String> unionTypeVarMap;
    private final String unionVarConstantsClass;
    private int constantIndex = 0;
    private JvmTypeGen jvmTypeGen;
    private ClassWriter cw;
    private MethodVisitor mv;
    private int memberCountInMethod;
    private int methodCount;
    /**
     * Stack keeps track of recursion in union types. The method creation is performed only if recursion is completed.
     */
    private final Deque<BUnionType> stack;

    private static final int MAX_MEMBERS_PER_METHOD = 50;

    public JvmUnionTypeConstantsGen(PackageID packageID) {
        unionTypeVarMap = new ConcurrentSkipListMap<>(this::checkUnionEqualityInInts);
        unionVarConstantsClass = JvmCodeGenUtil.getModuleLevelClassName(
                packageID, JvmConstants.BUNION_TYPE_CONSTANT_CLASS_NAME);
        stack = new LinkedList<>();
        generateClassInit();
        visitMethod();
    }

    private int checkUnionEqualityInInts(BUnionType o1, BUnionType o2) {
        if (checkUnionsEquality(o1, o2)) {
            return 0;
        }
        return -1;
    }

    private boolean checkUnionsEquality(BUnionType o1, BUnionType o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1.isCyclic || o2.isCyclic) {
            // Not checking for equality at compile time if it is cyclic
            return false;
        }
        if (o1.getMemberTypes().size() != o2.getMemberTypes().size() || !o1.toString().equals(o2.toString())) {
            return false;
        }
        for (BType type : o1.getMemberTypes()) {
            if (!o2.getMemberTypes().contains(type)) {
                return false;
            }
        }

        return o1.flags == o2.flags;
    }

    public synchronized void setJvmTypeGen(JvmTypeGen jvmTypeGen) {
        this.jvmTypeGen = jvmTypeGen;
    }

    public synchronized String add(BUnionType type) {
        stack.push(type);
        if (canCreateMethod()) {
            visitMethod();
            memberCountInMethod = 0;
        }
        String varName = unionTypeVarMap.computeIfAbsent(type, str -> generateBUnionInits(type));
        if (canCreateMethod()) {
            genMethodReturn();
        }
        stack.pop();
        return varName;
    }

    private boolean canCreateMethod() {
        return stack.size() == 1 && (memberCountInMethod >= MAX_MEMBERS_PER_METHOD);
    }

    private void generateClassInit() {
        cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, unionVarConstantsClass, null, JvmConstants.OBJECT, null);

        mv = cw.visitMethod(ACC_PRIVATE, JvmConstants.JVM_INIT_METHOD, "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, JvmConstants.OBJECT, JvmConstants.JVM_INIT_METHOD, "()V", false);
        genMethodReturn();
    }

    private void visitMethod() {
        mv = cw.visitMethod(ACC_STATIC, JvmConstants.B_UNION_TYPE_INIT_METHOD_PREFIX + methodCount++,
                            "()V", null, null);
    }

    private String generateBUnionInits(BUnionType type) {
        memberCountInMethod += type.getMemberTypes().size();
        String varName = JvmConstants.UNION_TYPE_VAR_PREFIX + constantIndex++;
        visitBUnionField(varName);
        createBunionType(mv, type, varName);
        return varName;
    }

    private void createBunionType(MethodVisitor mv, BUnionType unionType, String varName) {
        jvmTypeGen.loadUnionType(mv, unionType);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, unionVarConstantsClass, varName,
                          String.format("L%s;", JvmConstants.UNION_TYPE_IMPL));
    }

    private void visitBUnionField(String varName) {
        FieldVisitor fv;
        fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, varName,
                           String.format("L%s;", JvmConstants.UNION_TYPE_IMPL), null,
                           null);
        fv.visitEnd();
    }

    private void generateStaticInitializer(ClassWriter cw) {
        mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        for (int i = 0; i < methodCount; i++) {
            mv.visitMethodInsn(INVOKESTATIC, unionVarConstantsClass, JvmConstants.B_UNION_TYPE_INIT_METHOD_PREFIX + i,
                               "()V", false);
        }
        genMethodReturn();
    }

    public void generateGetBUnionType(MethodVisitor mv, String varName) {
        mv.visitFieldInsn(GETSTATIC, unionVarConstantsClass, varName,
                          String.format("L%s;", JvmConstants.UNION_TYPE_IMPL));
    }

    public synchronized void generateClass(Map<String, byte[]> jarEntries) {
        if (memberCountInMethod < MAX_MEMBERS_PER_METHOD) {
            genMethodReturn();
        }
        generateStaticInitializer(cw);
        cw.visitEnd();
        jarEntries.put(unionVarConstantsClass + ".class", cw.toByteArray());
    }

    private void genMethodReturn() {
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
