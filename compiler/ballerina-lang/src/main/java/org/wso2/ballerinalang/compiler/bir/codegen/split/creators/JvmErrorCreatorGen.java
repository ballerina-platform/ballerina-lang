/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen.split.creators;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.NAME_HASH_COMPARATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.createDefaultCase;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_TYPES_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ERRORS_CREATOR_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CREATE_ERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ERROR_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeFieldName;

/**
 * Ballerina error value creation related JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmErrorCreatorGen {

    private final String errorsClass;

    public JvmErrorCreatorGen(PackageID packageID) {
       this.errorsClass = getModuleLevelClassName(packageID, MODULE_ERRORS_CREATOR_CLASS_NAME);
    }

    public void generateErrorsClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                    String moduleInitClass, Map<String, byte[]> jarEntries,
                                    List<BIRNode.BIRTypeDefinition> errorTypeDefList, SymbolTable symbolTable) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, errorsClass, null, OBJECT, null);
        generateCreateErrorMethods(cw, errorTypeDefList, moduleInitClass, errorsClass, symbolTable);
        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(errorsClass + ".class", bytes);
    }


    private void generateCreateErrorMethods(ClassWriter cw, List<BIRNode.BIRTypeDefinition> errorTypeDefList,
                                            String moduleInitClass, String typeOwnerClass, SymbolTable symbolTable) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_ERROR_VALUE,
                CREATE_ERROR, null, null);
        mv.visitCode();
        if (errorTypeDefList.isEmpty()) {
            createDefaultCase(mv, new Label(), 0, "No such error: ");
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_ERROR_VALUE + 0,
                    CREATE_ERROR, false);
            mv.visitInsn(ARETURN);
            generateCreateErrorMethodSplits(cw, errorTypeDefList, moduleInitClass, typeOwnerClass, symbolTable);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateCreateErrorMethodSplits(ClassWriter cw, List<BIRNode.BIRTypeDefinition> errorTypeDefList,
                                                 String moduleInitClass, String typeOwnerClass,
                                                 SymbolTable symbolTable) {
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        List<Label> targetLabels = new ArrayList<>();

        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        int errorNameIndex = indexMap.addIfNotExists("errorTypeName", symbolTable.stringType);
        int messageIndex = indexMap.addIfNotExists("message", symbolTable.stringType);
        int causeIndex = indexMap.addIfNotExists("cause", symbolTable.errorType);
        int detailsIndex = indexMap.addIfNotExists("details", symbolTable.anyType);
        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        errorTypeDefList.sort(NAME_HASH_COMPARATOR);
        int i = 0;
        for (BIRNode.BIRTypeDefinition errorDefinition : errorTypeDefList) {
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_ERROR_VALUE + methodCount++,
                        CREATE_ERROR
                        , null, null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                int remainingCases = errorTypeDefList.size() - bTypesCount;
                if (remainingCases > MAX_TYPES_PER_METHOD) {
                    remainingCases = MAX_TYPES_PER_METHOD;
                }
                List<Label> labels = JvmCreateTypeGen.createLabelsForSwitch(mv, errorNameIndex, errorTypeDefList,
                        bTypesCount, remainingCases, defaultCaseLabel);
                targetLabels = JvmCreateTypeGen.createLabelsForEqualCheck(mv, errorNameIndex, errorTypeDefList,
                        bTypesCount, remainingCases, labels, defaultCaseLabel);
                i = 0;
            }
            String fieldName = getTypeFieldName(errorDefinition.internalName.value);
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            mv.visitTypeInsn(NEW, ERROR_VALUE);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETSTATIC, moduleInitClass, fieldName, GET_TYPE);
            mv.visitVarInsn(ALOAD, messageIndex);
            mv.visitVarInsn(ALOAD, causeIndex);
            mv.visitVarInsn(ALOAD, detailsIndex);
            mv.visitMethodInsn(INVOKESPECIAL, ERROR_VALUE, JVM_INIT_METHOD,
                    ERROR_INIT, false);
            mv.visitInsn(ARETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                if (bTypesCount == errorTypeDefList.size()) {
                    createDefaultCase(mv, defaultCaseLabel, errorNameIndex, "No such error: ");
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_ERROR_VALUE + methodCount,
                            CREATE_ERROR, false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + 10, i + 10);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_TYPES_PER_METHOD != 0) {
            createDefaultCase(mv, defaultCaseLabel, errorNameIndex, "No such error: ");
            mv.visitMaxs(i + 10, i + 10);
            mv.visitEnd();
        }
    }
}
