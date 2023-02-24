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
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_TUPLE_TYPE_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_TUPLE_TYPE_POPULATE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_CONSTANTS_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TUPLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.generateConstantsClassInit;

/**
 * Generates Jvm class for the ballerina tuple types as constants for a given module.
 *
 * @since 2.0.0
 */
public class JvmTupleTypeConstantsGen {

    private final String tupleVarConstantsClass;
    private final Map<BTupleType, String> tupleTypeVarMap;
    private final ClassWriter cw;
    private MethodVisitor mv;
    private JvmTupleTypeGen jvmTupleTypeGen;
    private final List<String> funcNames = new ArrayList<>();
    private final Queue<TypeNamePair> queue = new LinkedList<>();
    private int tupleTypeConstCount = 0;
    private int methodCount = 1;
    private int constantIndex = 0;

    public JvmTupleTypeConstantsGen(PackageID packageID, BTypeHashComparator bTypeHashComparator) {
        tupleVarConstantsClass = JvmCodeGenUtil.getModuleLevelClassName(packageID,
                JvmConstants.TUPLE_TYPE_CONSTANT_CLASS_NAME);
        cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        generateConstantsClassInit(cw, tupleVarConstantsClass);
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, B_TUPLE_TYPE_INIT_METHOD, "()V", null, null);
        tupleTypeVarMap = new TreeMap<>(bTypeHashComparator);
    }

    public void setJvmTupleTypeGen(JvmTupleTypeGen jvmTupleTypeGen) {
        this.jvmTupleTypeGen = jvmTupleTypeGen;
    }

    public String add(BTupleType type, SymbolTable symbolTable) {
        String varName = tupleTypeVarMap.get(type);
        if (varName == null) {
            varName = generateBTupleInits(type, symbolTable);
            tupleTypeVarMap.put(type, varName);
        }
        return varName;
    }

    /**
     * Stack keeps track of recursion in tuple types. The method creation is performed only if recursion is completed.
     */
    private String generateBTupleInits(BTupleType type, SymbolTable symbolTable) {
        String varName = JvmConstants.TUPLE_TYPE_VAR_PREFIX + constantIndex++;
        if (tupleTypeConstCount % MAX_CONSTANTS_PER_METHOD == 0 && tupleTypeConstCount != 0) {
            mv.visitMethodInsn(INVOKESTATIC, tupleVarConstantsClass,
                    B_TUPLE_TYPE_INIT_METHOD + methodCount, "()V", false);
            genMethodReturn(mv);
            mv = cw.visitMethod(ACC_STATIC, B_TUPLE_TYPE_INIT_METHOD + methodCount++, "()V",
                    null, null);
        }
        visitBTupleField(varName);
        createBTupleType(type, varName);
        // Queue is used here to avoid recursive calls to the genPopulateMethod. This can happen when a tuple
        // contains a tuple inside it.
        queue.add(new TypeNamePair(type, varName));
        if (queue.size() == 1) {
            genPopulateMethod(type, varName, symbolTable);
            queue.remove();
            while (!queue.isEmpty()) {
                TypeNamePair typeNamePair = queue.remove();
                genPopulateMethod((BTupleType) typeNamePair.type, typeNamePair.varName, symbolTable);
            }
        }
        tupleTypeConstCount++;
        return varName;
    }

    private void genPopulateMethod(BTupleType type, String varName, SymbolTable symbolTable) {
        String methodName = "$populate" + varName;
        funcNames.add(methodName);
        MethodVisitor methodVisitor = cw.visitMethod(ACC_STATIC, methodName, "()V", null, null);
        methodVisitor.visitCode();
        generateGetBTupleType(methodVisitor, varName);
        jvmTupleTypeGen.populateTuple(methodVisitor, type, symbolTable);
        genMethodReturn(methodVisitor);
    }

    private void visitTupleTypeConstPopulateInitMethods() {
        int populateFuncCount = 0;
        int populateInitMethodCount = 1;
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, B_TUPLE_TYPE_POPULATE_METHOD,
                "()V", null, null);
        for (String funcName : funcNames) {
            if (populateFuncCount % MAX_CONSTANTS_PER_METHOD == 0 && populateFuncCount != 0) {
                mv.visitMethodInsn(INVOKESTATIC, tupleVarConstantsClass,
                        B_TUPLE_TYPE_POPULATE_METHOD + populateInitMethodCount, "()V", false);
                genMethodReturn(mv);
                mv = cw.visitMethod(ACC_STATIC, B_TUPLE_TYPE_POPULATE_METHOD + populateInitMethodCount++,
                        "()V", null, null);
            }
            mv.visitMethodInsn(INVOKESTATIC, tupleVarConstantsClass, funcName, "()V", false);
            populateFuncCount++;
        }
        genMethodReturn(mv);
    }

    private void createBTupleType(BTupleType tupleType, String varName) {
        jvmTupleTypeGen.createTupleType(mv, tupleType);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, tupleVarConstantsClass, varName,
                GET_TUPLE_TYPE_IMPL);
    }

    private void visitBTupleField(String varName) {
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, varName, GET_TUPLE_TYPE_IMPL, null, null);
        fv.visitEnd();
    }

    public void generateGetBTupleType(MethodVisitor mv, String varName) {
        mv.visitFieldInsn(GETSTATIC, tupleVarConstantsClass, varName, GET_TUPLE_TYPE_IMPL);
    }

    public void generateClass(Map<String, byte[]> jarEntries) {
        genMethodReturn(mv);
        visitTupleTypeConstPopulateInitMethods();
        cw.visitEnd();
        jarEntries.put(tupleVarConstantsClass + ".class", cw.toByteArray());
    }

    public String getTupleTypeConstantsClass() {
        return this.tupleVarConstantsClass;
    }
}
