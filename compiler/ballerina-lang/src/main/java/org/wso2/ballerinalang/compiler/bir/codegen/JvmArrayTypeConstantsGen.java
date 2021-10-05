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
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmArrayTypeGen;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_TYPE_IMPL;

public class JvmArrayTypeConstantsGen {

    private final String arrayConstantsClass;
    private ClassWriter cw;
    private MethodVisitor mv;
    private int methodCount;
    private final Map<BArrayType, String> arrayTypeVarMap;
    private int constantIndex = 0;
    private JvmArrayTypeGen jvmArrayTypeGen;
    private final List<String> funcNames;
    private final Types types;

    public JvmArrayTypeConstantsGen(PackageID packageID, Types types) {
        this.arrayConstantsClass =
                JvmCodeGenUtil.getModuleLevelClassName(packageID, JvmConstants.BARRAY_TYPE_CONSTANT_CLASS_NAME);
        generateArrayTypeConstantsClassInit();
        visitArrayTypeInitMethod();
        this.arrayTypeVarMap = new ConcurrentSkipListMap<>(JvmConstantsGen.TYPE_HASH_COMPARATOR);
        this.funcNames = new ArrayList<>();
        this.types = types;
    }

    public synchronized void setJvmArrayTypeGen(JvmArrayTypeGen jvmArrayTypeGen) {
        this.jvmArrayTypeGen = jvmArrayTypeGen;
    }

    private void generateArrayTypeConstantsClassInit() {
        cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, arrayConstantsClass, null, JvmConstants.OBJECT, null);

        MethodVisitor methodVisitor = cw.visitMethod(ACC_PRIVATE, JvmConstants.JVM_INIT_METHOD, "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, JvmConstants.OBJECT, JvmConstants.JVM_INIT_METHOD, "()V", false);
        genMethodReturn(methodVisitor);
    }

    private void genMethodReturn(MethodVisitor methodVisitor) {
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void visitArrayTypeInitMethod() {
        mv = cw.visitMethod(ACC_STATIC, JvmConstants.B_ARRAY_TYPE_INIT_METHOD_PREFIX + methodCount++, "()V", null,
                null);
    }

    public synchronized String add(BArrayType arrayType) {
        return arrayTypeVarMap.computeIfAbsent(arrayType, str -> generateBArrayInits(arrayType));
    }

    private String generateBArrayInits(BArrayType arrayType) {
        String varName = JvmConstants.ARRAY_TYPE_VAR_PREFIX + constantIndex++;
        createBArrayType(mv, arrayType, varName);
        if(!TypeTags.isSimpleBasicType(arrayType.eType.tag)) {
            genPopulateMethod(arrayType, varName);
        }
        return varName;
    }

    private void genPopulateMethod(BArrayType arrayType, String varName) {
        String methodName = String.format("$populate%s", varName);
        funcNames.add(methodName);
        MethodVisitor methodVisitor = cw.visitMethod(ACC_STATIC, methodName, "()V", null, null);
        methodVisitor.visitCode();
        generateGetBArrayType(methodVisitor, varName);
        jvmArrayTypeGen.populateArray(methodVisitor, arrayType);
        genMethodReturn(methodVisitor);
    }

    private void createBArrayType(MethodVisitor mv, BArrayType arrayType, String varName) {
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, varName,
                String.format("L%s;", JvmConstants.ARRAY_TYPE_IMPL), null, null);
        fv.visitEnd();
        jvmArrayTypeGen.createArrayType(mv, arrayType, types);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, arrayConstantsClass, varName, String.format("L%s;",
                ARRAY_TYPE_IMPL));
    }

    public void generateGetBArrayType(MethodVisitor mv, String varName) {
        mv.visitFieldInsn(GETSTATIC, arrayConstantsClass, varName, String.format("L%s;", ARRAY_TYPE_IMPL));
    }

    public void generateClass(Map<String, byte[]> jarEntries) {
        genMethodReturn(mv);
        visitArrayTypeInitMethod();
        for (String funcName : funcNames) {
            mv.visitMethodInsn(INVOKESTATIC, arrayConstantsClass, funcName, "()V", false);
        }
        genMethodReturn(mv);
        generateStaticInitializer(cw);
        cw.visitEnd();
        jarEntries.put(arrayConstantsClass + ".class", cw.toByteArray());
    }

    private void generateStaticInitializer(ClassWriter cw) {
        MethodVisitor methodVisitor = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        for (int i = 0; i < methodCount; i++) {
            methodVisitor.visitMethodInsn(INVOKESTATIC, arrayConstantsClass,
                    JvmConstants.B_ARRAY_TYPE_INIT_METHOD_PREFIX + i, "()V", false);
        }
        genMethodReturn(methodVisitor);
    }
}
