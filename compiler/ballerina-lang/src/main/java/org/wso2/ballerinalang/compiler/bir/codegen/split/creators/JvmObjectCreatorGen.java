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
import org.wso2.ballerinalang.compiler.bir.codegen.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.V21;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.NAME_HASH_COMPARATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.createDefaultCase;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_TYPES_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_OBJECTS_CREATOR_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VISIT_MAX_SAFE_MARGIN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.BOBJECT_CALL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CREATE_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.OBJECT_TYPE_IMPL_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeFieldName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeValueClassName;

/**
 * Ballerina object value creation related JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmObjectCreatorGen {

    private final String objectsClass;

    public JvmObjectCreatorGen(PackageID packageID) {
        this.objectsClass = getModuleLevelClassName(packageID, MODULE_OBJECTS_CREATOR_CLASS_NAME);
    }

    public void generateObjectsClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                     String moduleInitClass, JarEntries jarEntries,
                                     List<BIRTypeDefinition> objectTypeDefList,
                                     SymbolTable symbolTable) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V21, ACC_PUBLIC + ACC_SUPER, objectsClass, null, OBJECT, null);
        generateCreateObjectMethods(cw, objectTypeDefList, module.packageID, moduleInitClass, objectsClass,
                symbolTable);
        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(objectsClass + CLASS_FILE_SUFFIX, bytes);
    }


    private void generateCreateObjectMethods(ClassWriter cw, List<BIRTypeDefinition> objectTypeDefList,
                                             PackageID moduleId, String moduleInitClass, String typeOwnerClass,
                                             SymbolTable symbolTable) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_OBJECT_VALUE, CREATE_OBJECT, null, null);
        mv.visitCode();
        if (objectTypeDefList.isEmpty()) {
            createDefaultCase(mv, new Label(), 0, "No such object: ");
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_OBJECT_VALUE + 0, CREATE_OBJECT, false);
            mv.visitInsn(ARETURN);
            generateCreateObjectMethodSplits(cw, objectTypeDefList, moduleId, moduleInitClass, typeOwnerClass,
                    symbolTable);
        }
        JvmCodeGenUtil.visitMaxStackForMethod(mv, CREATE_OBJECT_VALUE, objectsClass);
        mv.visitEnd();
    }

    private void generateCreateObjectMethodSplits(ClassWriter cw, List<BIRTypeDefinition> objectTypeDefList,
                                                  PackageID moduleId, String moduleInitClass, String typeOwnerClass,
                                                  SymbolTable symbolTable) {
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        List<Label> targetLabels = new ArrayList<>();

        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        int var1Index = indexMap.addIfNotExists("var1", symbolTable.stringType);
        int parentIndex = indexMap.addIfNotExists("parent", symbolTable.anyType);
        int argsIndex = indexMap.addIfNotExists("args", symbolTable.anyType);
        Label defaultCaseLabel = new Label();
        // sort the fields before generating switch case
        objectTypeDefList.sort(NAME_HASH_COMPARATOR);
        int i = 0;
        for (BIRTypeDefinition optionalTypeDef : objectTypeDefList) {
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_OBJECT_VALUE + methodCount++, CREATE_OBJECT, null
                        , null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                int remainingCases = objectTypeDefList.size() - bTypesCount;
                if (remainingCases > MAX_TYPES_PER_METHOD) {
                    remainingCases = MAX_TYPES_PER_METHOD;
                }
                List<Label> labels = JvmCreateTypeGen.createLabelsForSwitch(mv, var1Index, objectTypeDefList,
                        bTypesCount, remainingCases, defaultCaseLabel);
                targetLabels = JvmCreateTypeGen.createLabelsForEqualCheck(mv, var1Index, objectTypeDefList,
                        bTypesCount, remainingCases, labels, defaultCaseLabel);
                i = 0;
            }
            String fieldName = getTypeFieldName(optionalTypeDef.internalName.value);
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            String className = getTypeValueClassName(moduleId, optionalTypeDef.internalName.value);
            mv.visitTypeInsn(NEW, className);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETSTATIC, moduleInitClass, fieldName, GET_TYPE);
            mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE_IMPL);
            mv.visitMethodInsn(INVOKESPECIAL, className, JVM_INIT_METHOD, OBJECT_TYPE_IMPL_INIT, false);
            int tempVarIndex = indexMap.addIfNotExists("tempVar", optionalTypeDef.type);
            mv.visitVarInsn(ASTORE, tempVarIndex);
            mv.visitVarInsn(ALOAD, tempVarIndex);
            mv.visitVarInsn(ALOAD, parentIndex);
            mv.visitLdcInsn("$init$");
            mv.visitVarInsn(ALOAD, argsIndex);
            mv.visitMethodInsn(INVOKEINTERFACE, B_OBJECT, "call", BOBJECT_CALL, true);
            int tempResultIndex = indexMap.addIfNotExists("tempResult", symbolTable.anyType);
            mv.visitVarInsn(ASTORE, tempResultIndex);
            mv.visitVarInsn(ALOAD, tempResultIndex);
            mv.visitTypeInsn(INSTANCEOF, BERROR);
            Label noErrorLabel = new Label();
            mv.visitJumpInsn(IFEQ, noErrorLabel);
            mv.visitVarInsn(ALOAD, tempResultIndex);
            mv.visitTypeInsn(CHECKCAST, BERROR);
            mv.visitInsn(ATHROW);
            mv.visitLabel(noErrorLabel);
            mv.visitVarInsn(ALOAD, tempVarIndex);
            mv.visitInsn(ARETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                if (bTypesCount == objectTypeDefList.size()) {
                    createDefaultCase(mv, defaultCaseLabel, var1Index, "No such object: ");
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_OBJECT_VALUE + methodCount, CREATE_OBJECT
                            , false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + VISIT_MAX_SAFE_MARGIN, i + VISIT_MAX_SAFE_MARGIN);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_TYPES_PER_METHOD != 0) {
            createDefaultCase(mv, defaultCaseLabel, var1Index, "No such object: ");
            mv.visitMaxs(i + VISIT_MAX_SAFE_MARGIN, i + VISIT_MAX_SAFE_MARGIN);
            mv.visitEnd();
        }
    }
}
