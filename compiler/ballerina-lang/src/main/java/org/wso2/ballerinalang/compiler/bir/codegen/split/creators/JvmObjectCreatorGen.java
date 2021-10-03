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
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmValueCreatorGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.NAME_HASH_COMPARATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.createDefaultCase;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_RECORD_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_TYPES_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_OBJECTS_CREATOR_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeFieldName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeValueClassName;

/**
 * Ballerina object value creation related JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmObjectCreatorGen {

    private final String objectsClass;
    private final JvmValueCreatorGen jvmValueCreatorGen;

    public JvmObjectCreatorGen(JvmValueCreatorGen jvmValueCreatorGen, PackageID packageID) {
        this.objectsClass = getModuleLevelClassName(packageID, MODULE_OBJECTS_CREATOR_CLASS_NAME);
        this.jvmValueCreatorGen = jvmValueCreatorGen;
    }

    public void generateObjectsClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                     String moduleInitClass, Map<String, byte[]> jarEntries,
                                     List<BIRTypeDefinition> objectTypeDefList,
                                     SymbolTable symbolTable) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, objectsClass, null, OBJECT, null);
        String metadataVarName = JvmCodeGenUtil.getStrandMetadataVarName(CREATE_RECORD_VALUE);
        jvmValueCreatorGen.generateStaticInitializer(module, cw, objectsClass, CREATE_OBJECT_VALUE, metadataVarName);
        generateCreateObjectMethods(cw, objectTypeDefList, module.packageID, moduleInitClass, objectsClass,
                symbolTable, metadataVarName);

        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(objectsClass + ".class", bytes);
    }


    private void generateCreateObjectMethods(ClassWriter cw, List<BIRTypeDefinition> objectTypeDefList,
                                             PackageID moduleId, String moduleInitClass, String typeOwnerClass,
                                             SymbolTable symbolTable, String metadataVarName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_OBJECT_VALUE,
                String.format("(L%s;L%s;L%s;L%s;[L%s;)L%s;",
                        STRING_VALUE, SCHEDULER, STRAND_CLASS, MAP, OBJECT, B_OBJECT), null, null);
        mv.visitCode();
        if (objectTypeDefList.isEmpty()) {
            createDefaultCase(mv, new Label(), 1, "No such object: ");
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_OBJECT_VALUE + 0,
                    String.format("(L%s;L%s;L%s;L%s;[L%s;)L%s;",
                            STRING_VALUE, SCHEDULER, STRAND_CLASS, MAP, OBJECT, B_OBJECT), false);
            mv.visitInsn(ARETURN);
            generateCreateObjectMethodSplits(cw, objectTypeDefList, moduleId, moduleInitClass, typeOwnerClass,
                    symbolTable, metadataVarName);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateCreateObjectMethodSplits(ClassWriter cw, List<BIRTypeDefinition> objectTypeDefList,
                                                  PackageID moduleId, String moduleInitClass, String typeOwnerClass,
                                                  SymbolTable symbolTable, String metadataVarName) {
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        List<Label> targetLabels = new ArrayList<>();

        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        int var1Index = indexMap.addIfNotExists("var1", symbolTable.stringType);
        int schedulerIndex = indexMap.addIfNotExists("scheduler", symbolTable.anyType);
        int parentIndex = indexMap.addIfNotExists("parent", symbolTable.anyType);
        int propertiesIndex = indexMap.addIfNotExists("properties", symbolTable.anyType);
        int argsIndex = indexMap.addIfNotExists("args", symbolTable.anyType);
        Label defaultCaseLabel = new Label();
        // sort the fields before generating switch case
        objectTypeDefList.sort(NAME_HASH_COMPARATOR);
        int i = 0;
        for (BIRTypeDefinition optionalTypeDef : objectTypeDefList) {
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_OBJECT_VALUE + methodCount++,
                        String.format("(L%s;L%s;L%s;L%s;[L%s;)L%s;",
                                STRING_VALUE, SCHEDULER, STRAND_CLASS, MAP, OBJECT, B_OBJECT), null, null);
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
            mv.visitFieldInsn(GETSTATIC, moduleInitClass, fieldName, String.format("L%s;", TYPE));
            mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE_IMPL);
            mv.visitMethodInsn(INVOKESPECIAL, className, JVM_INIT_METHOD, String.format("(L%s;)V", OBJECT_TYPE_IMPL),
                    false);

            int tempVarIndex = indexMap.addIfNotExists("tempVar", optionalTypeDef.type);
            mv.visitVarInsn(ASTORE, tempVarIndex);
            int strandVarIndex = indexMap.addIfNotExists("strandVar", symbolTable.anyType);

            mv.visitVarInsn(ALOAD, parentIndex);
            Label parentNonNullLabel = new Label();
            mv.visitJumpInsn(IFNONNULL, parentNonNullLabel);
            Label parentNullLabel = new Label();
            mv.visitLabel(parentNullLabel);
            mv.visitTypeInsn(NEW, STRAND_CLASS);
            mv.visitInsn(DUP);
            mv.visitInsn(ACONST_NULL);
            mv.visitFieldInsn(GETSTATIC, typeOwnerClass, metadataVarName, String.format("L%s;", STRAND_METADATA));
            mv.visitVarInsn(ALOAD, schedulerIndex);
            mv.visitVarInsn(ALOAD, parentIndex);
            mv.visitVarInsn(ALOAD, propertiesIndex);
            mv.visitMethodInsn(INVOKESPECIAL, STRAND_CLASS, JVM_INIT_METHOD,
                    String.format("(L%s;L%s;L%s;L%s;L%s;)V", STRING_VALUE, STRAND_METADATA, SCHEDULER,
                            STRAND_CLASS, MAP), false);
            mv.visitVarInsn(ASTORE, strandVarIndex);
            Label endConditionLabel = new Label();
            mv.visitJumpInsn(GOTO, endConditionLabel);
            mv.visitLabel(parentNonNullLabel);
            mv.visitVarInsn(ALOAD, parentIndex);
            mv.visitVarInsn(ASTORE, strandVarIndex);
            mv.visitLabel(endConditionLabel);

            mv.visitVarInsn(ALOAD, tempVarIndex);
            mv.visitVarInsn(ALOAD, strandVarIndex);

            mv.visitLdcInsn("$init$");
            mv.visitVarInsn(ALOAD, argsIndex);

            String methodDesc = String.format("(L%s;L%s;[L%s;)L%s;", STRAND_CLASS, STRING_VALUE, OBJECT, OBJECT);
            mv.visitMethodInsn(INVOKEINTERFACE, B_OBJECT, "call", methodDesc, true);

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
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitVarInsn(ALOAD, 4);
                    mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_OBJECT_VALUE + methodCount,
                            String.format("(L%s;L%s;L%s;L%s;[L%s;)L%s;",
                                    STRING_VALUE, SCHEDULER, STRAND_CLASS, MAP, OBJECT, B_OBJECT), false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + 10, i + 10);
                mv.visitEnd();
            }
        }

        if (methodCount != 0 && bTypesCount % MAX_TYPES_PER_METHOD != 0) {
            createDefaultCase(mv, defaultCaseLabel, var1Index, "No such object: ");
            mv.visitMaxs(i + 10, i + 10);
            mv.visitEnd();
        }
    }
}
