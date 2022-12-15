/*
 * Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
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
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmErrorTypeGen;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_ERROR_TYPE_INIT_METHOD_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ERROR_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantGenCommons.generateConstantsClassInit;

/**
 * Generates the JVM class for the ballerina error types as constants for a given module.
 * Anonymous distinct error types are generated in this class.
 *
 * @since 2201.4.0
 */
public class JvmErrorTypeConstantsGen {

    private final String errorVarConstantsClass;
    private int constantIndex = 0;
    private JvmErrorTypeGen jvmErrorTypeGen;
    private final ClassWriter cw;
    private MethodVisitor mv;
    private int methodCount;
    private final List<String> funcNames;
    private final Map<BErrorType, String> errorTypeVarMap;

    public JvmErrorTypeConstantsGen(PackageID packageID, BTypeHashComparator bTypeHashComparator) {
        errorVarConstantsClass = JvmCodeGenUtil.getModuleLevelClassName(packageID,
                JvmConstants.ERROR_TYPE_CONSTANT_CLASS_NAME);
        cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        generateConstantsClassInit(cw, errorVarConstantsClass);
        visitErrorTypeInitMethod();
        funcNames = new ArrayList<>();
        errorTypeVarMap = new TreeMap<>(bTypeHashComparator);
    }

    public void setJvmErrorTypeGen(JvmErrorTypeGen jvmErrorTypeGen) {
        this.jvmErrorTypeGen = jvmErrorTypeGen;
    }

    public String add(BErrorType type) {
        String varName = errorTypeVarMap.get(type);
        if (varName == null) {
            varName = generateBErrorInits(type);
            errorTypeVarMap.put(type, varName);
        }
        return varName;
    }

    private void visitErrorTypeInitMethod() {
        mv = cw.visitMethod(ACC_STATIC, B_ERROR_TYPE_INIT_METHOD_PREFIX + methodCount++,
                            "()V", null, null);
    }

    private String generateBErrorInits(BErrorType type) {
        String varName = JvmConstants.ERROR_TYPE_VAR_PREFIX + constantIndex++;
        visitBErrorField(varName);
        createBErrorType(mv, type, varName);
        genPopulateMethod(type, varName);
        return varName;
    }

    private void genPopulateMethod(BErrorType type, String varName) {
        String methodName = "$populate" + varName;
        funcNames.add(methodName);
        MethodVisitor methodVisitor = cw.visitMethod(ACC_STATIC, methodName, "()V", null, null);
        methodVisitor.visitCode();
        generateGetBErrorType(methodVisitor, varName);
        jvmErrorTypeGen.populateError(methodVisitor, type);
        genMethodReturn(methodVisitor);
    }

    private void createBErrorType(MethodVisitor mv, BErrorType errorType, String varName) {
        jvmErrorTypeGen.createErrorType(mv, errorType, errorType.tsymbol.name.value);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, errorVarConstantsClass, varName,
                          GET_ERROR_TYPE_IMPL);
    }

    private void visitBErrorField(String varName) {
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, varName,
                                        GET_ERROR_TYPE_IMPL, null, null);
        fv.visitEnd();
    }

    public void generateGetBErrorType(MethodVisitor mv, String varName) {
        mv.visitFieldInsn(GETSTATIC, errorVarConstantsClass, varName,
                          GET_ERROR_TYPE_IMPL);
    }

    public void generateClass(Map<String, byte[]> jarEntries) {
        genMethodReturn(mv);
        visitErrorTypeInitMethod();
        for (String funcName : funcNames) {
            mv.visitMethodInsn(INVOKESTATIC, errorVarConstantsClass, funcName, "()V", false);
        }
        genMethodReturn(mv);
        generateStaticInitializer(cw);
        cw.visitEnd();
        jarEntries.put(errorVarConstantsClass + ".class", cw.toByteArray());
    }

    private void generateStaticInitializer(ClassWriter cw) {
        MethodVisitor methodVisitor = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        for (int i = 0; i < methodCount; i++) {
            methodVisitor.visitMethodInsn(INVOKESTATIC, errorVarConstantsClass,
                                         B_ERROR_TYPE_INIT_METHOD_PREFIX + i,
                                          "()V", false);
        }
        genMethodReturn(methodVisitor);
    }
}
