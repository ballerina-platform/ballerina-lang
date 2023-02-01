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
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BTypeHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmArrayTypeGen;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_ARRAY_TYPE_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_ARRAY_TYPE_POPULATE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_CONSTANTS_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ARRAY_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.generateConstantsClassInit;

/**
 * Generates Jvm class for the ballerina array types as constants for a given module.
 *
 * @since 2.0.0
 */
public class JvmArrayTypeConstantsGen {

    private final String arrayConstantsClass;
    private final ClassWriter cw;
    private MethodVisitor mv;
    private final Map<BArrayType, String> arrayTypeVarMap;
    private JvmArrayTypeGen jvmArrayTypeGen;
    private final Types types;
    private final List<String> funcNames = new ArrayList<>();
    private int arrayTypeVarCount = 0;
    private int methodCount = 1;
    private int constantIndex = 0;

    public JvmArrayTypeConstantsGen(PackageID packageID, BTypeHashComparator bTypeHashComparator, Types types) {
        this.arrayConstantsClass =
                JvmCodeGenUtil.getModuleLevelClassName(packageID, JvmConstants.ARRAY_TYPE_CONSTANT_CLASS_NAME);
        cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        generateConstantsClassInit(cw, arrayConstantsClass);
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, B_ARRAY_TYPE_INIT_METHOD, "()V", null, null);
        this.arrayTypeVarMap = new TreeMap<>(bTypeHashComparator);
        this.types = types;
    }

    public void setJvmArrayTypeGen(JvmArrayTypeGen jvmArrayTypeGen) {
        this.jvmArrayTypeGen = jvmArrayTypeGen;
    }

    public String add(BArrayType arrayType) {
        String varName = arrayTypeVarMap.get(arrayType);
        if (varName == null) {
            varName = generateBArrayInits(arrayType);
            arrayTypeVarMap.put(arrayType, varName);
        }
        return varName;
    }

    private String generateBArrayInits(BArrayType arrayType) {
        String varName = JvmConstants.ARRAY_TYPE_VAR_PREFIX + constantIndex++;
        if (arrayTypeVarCount % MAX_CONSTANTS_PER_METHOD == 0 && arrayTypeVarCount != 0) {
            mv.visitMethodInsn(INVOKESTATIC, arrayConstantsClass,
                    B_ARRAY_TYPE_INIT_METHOD + methodCount, "()V", false);
            genMethodReturn(mv);
            mv = cw.visitMethod(ACC_STATIC, B_ARRAY_TYPE_INIT_METHOD + methodCount++, "()V",
                    null, null);
        }
        createBArrayType(mv, arrayType, varName);
        if (!TypeTags.isSimpleBasicType(arrayType.eType.tag)) {
            genPopulateMethod(arrayType, varName);
        }
        arrayTypeVarCount++;
        return varName;
    }

    private void genPopulateMethod(BArrayType arrayType, String varName) {
        String methodName = "$populate" + varName;
        funcNames.add(methodName);
        MethodVisitor methodVisitor = cw.visitMethod(ACC_STATIC, methodName, "()V", null, null);
        methodVisitor.visitCode();
        generateGetBArrayType(methodVisitor, varName);
        jvmArrayTypeGen.populateArray(methodVisitor, arrayType);
        genMethodReturn(methodVisitor);
    }

    private void visitArrayTypeConstPopulateInitMethods() {
        int populateFuncCount = 0;
        int populateInitMethodCount = 1;
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, B_ARRAY_TYPE_POPULATE_METHOD,
                "()V", null, null);
        for (String funcName : funcNames) {
            if (populateFuncCount % MAX_CONSTANTS_PER_METHOD == 0 && populateFuncCount != 0) {
                mv.visitMethodInsn(INVOKESTATIC, arrayConstantsClass,
                        B_ARRAY_TYPE_POPULATE_METHOD + populateInitMethodCount, "()V", false);
                genMethodReturn(mv);
                mv = cw.visitMethod(ACC_STATIC, B_ARRAY_TYPE_POPULATE_METHOD + populateInitMethodCount++,
                        "()V", null, null);
            }
            mv.visitMethodInsn(INVOKESTATIC, arrayConstantsClass, funcName, "()V", false);
            populateFuncCount++;
        }
        genMethodReturn(mv);
    }

    private void createBArrayType(MethodVisitor mv, BArrayType arrayType, String varName) {
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, varName, GET_ARRAY_TYPE_IMPL, null, null);
        fv.visitEnd();
        jvmArrayTypeGen.createArrayType(mv, arrayType, types);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, arrayConstantsClass, varName, GET_ARRAY_TYPE_IMPL);
    }

    public void generateGetBArrayType(MethodVisitor mv, String varName) {
        mv.visitFieldInsn(GETSTATIC, arrayConstantsClass, varName, GET_ARRAY_TYPE_IMPL);
    }

    public void generateClass(Map<String, byte[]> jarEntries) {
        genMethodReturn(mv);
        visitArrayTypeConstPopulateInitMethods();
        cw.visitEnd();
        jarEntries.put(arrayConstantsClass + ".class", cw.toByteArray());
    }

    public String getArrayTypeConstantClass() {
        return this.arrayConstantsClass;
    }
}
