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
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmValueCreatorGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.SWAP;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.NAME_HASH_COMPARATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.createDefaultCase;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_RECORD_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_TYPES_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_RECORDS_CREATOR_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeFieldName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeValueClassName;

/**
 * Ballerina record value creation related JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmRecordCreatorGen {

    private final String recordsClass;
    private final JvmValueCreatorGen jvmValueCreatorGen;

    public JvmRecordCreatorGen(JvmValueCreatorGen jvmValueCreatorGen, PackageID packageID) {
        this.recordsClass = getModuleLevelClassName(packageID, MODULE_RECORDS_CREATOR_CLASS_NAME);
        this.jvmValueCreatorGen = jvmValueCreatorGen;
    }

    public void generateRecordsClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                     String moduleInitClass, Map<String, byte[]> jarEntries,
                                     List<BIRTypeDefinition> recordTypeDefList) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, recordsClass, null, OBJECT, null);
        String metadataVarName = JvmCodeGenUtil.getStrandMetadataVarName(CREATE_RECORD_VALUE);
        jvmValueCreatorGen.generateStaticInitializer(module, cw, recordsClass, CREATE_RECORD_VALUE, metadataVarName);
        generateCreateRecordMethods(cw, recordTypeDefList, module.packageID, moduleInitClass, recordsClass,
                metadataVarName);
        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(recordsClass + ".class", bytes);
    }

    private void generateCreateRecordMethods(ClassWriter cw, List<BIRTypeDefinition> recordTypeDefList,
                                             PackageID moduleId, String moduleInitClass, String typeOwnerClass,
                                             String metadataVarName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_RECORD_VALUE,
                String.format("(L%s;)L%s;", STRING_VALUE, MAP_VALUE),
                String.format("(L%s;)L%s<L%s;L%s;>;", STRING_VALUE, MAP_VALUE, STRING_VALUE, OBJECT), null);
        mv.visitCode();
        if (recordTypeDefList.isEmpty()) {
            createDefaultCase(mv, new Label(), 1, "No such record: ");
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_RECORD_VALUE + 0,
                    String.format("(L%s;)L%s;", STRING_VALUE, MAP_VALUE), false);
            mv.visitInsn(ARETURN);
            generateCreateRecordMethodSplits(cw, recordTypeDefList, moduleId, moduleInitClass, typeOwnerClass,
                    metadataVarName);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateCreateRecordMethodSplits(ClassWriter cw, List<BIRTypeDefinition> recordTypeDefList,
                                                  PackageID moduleId, String moduleInitClass, String typeOwnerClass,
                                                  String metadataVarName) {
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;

        List<Label> targetLabels = new ArrayList<>();

        int fieldNameRegIndex = 0;
        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        recordTypeDefList.sort(NAME_HASH_COMPARATOR);

        int i = 0;
        for (BIRTypeDefinition optionalTypeDef : recordTypeDefList) {
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, CREATE_RECORD_VALUE + methodCount++,
                        String.format("(L%s;)L%s;", STRING_VALUE, MAP_VALUE),
                        String.format("(L%s;)L%s<L%s;L%s;>;", STRING_VALUE, MAP_VALUE, STRING_VALUE, OBJECT), null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                int remainingCases = recordTypeDefList.size() - bTypesCount;
                if (remainingCases > MAX_TYPES_PER_METHOD) {
                    remainingCases = MAX_TYPES_PER_METHOD;
                }
                List<Label> labels = JvmCreateTypeGen.createLabelsForSwitch(mv, fieldNameRegIndex, recordTypeDefList,
                        bTypesCount, remainingCases, defaultCaseLabel);
                targetLabels = JvmCreateTypeGen.createLabelsForEqualCheck(mv, fieldNameRegIndex, recordTypeDefList,
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
            mv.visitMethodInsn(INVOKESPECIAL, className, JVM_INIT_METHOD, String.format("(L%s;)V", TYPE), false);

            mv.visitInsn(DUP);
            mv.visitTypeInsn(NEW, STRAND_CLASS);
            mv.visitInsn(DUP);
            mv.visitInsn(ACONST_NULL);
            mv.visitFieldInsn(GETSTATIC, typeOwnerClass, metadataVarName, String.format("L%s;", STRAND_METADATA));
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ACONST_NULL);
            mv.visitMethodInsn(INVOKESPECIAL, STRAND_CLASS, JVM_INIT_METHOD,
                    String.format("(L%s;L%s;L%s;L%s;L%s;)V", STRING_VALUE, STRAND_METADATA, SCHEDULER,
                            STRAND_CLASS, MAP), false);
            mv.visitInsn(SWAP);
            mv.visitMethodInsn(INVOKESTATIC, className, JvmConstants.RECORD_INIT_WRAPPER_NAME,
                    String.format("(L%s;L%s;)V", STRAND_CLASS, MAP_VALUE), false);

            mv.visitInsn(ARETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                if (bTypesCount == recordTypeDefList.size()) {
                    createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex, "No such record: ");
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, fieldNameRegIndex);
                    mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_RECORD_VALUE + methodCount,
                            String.format("(L%s;)L%s;", STRING_VALUE, MAP_VALUE), false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + 10, i + 10);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_TYPES_PER_METHOD != 0) {
            createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex, "No such record: ");
            mv.visitMaxs(i + 10, i + 10);
            mv.visitEnd();
        }
    }
}
