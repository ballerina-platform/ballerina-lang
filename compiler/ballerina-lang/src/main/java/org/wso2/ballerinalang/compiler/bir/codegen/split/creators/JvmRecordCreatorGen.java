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
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
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
import static org.objectweb.asm.Opcodes.V21;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_RECORD_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_TYPES_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_RECORDS_CREATOR_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VISIT_MAX_SAFE_MARGIN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CREATE_RECORD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CREATE_RECORD_WITH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPEDESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_TYPEDESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeDescClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeValueClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.NAME_HASH_COMPARATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.createDefaultCase;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getPackageName;

/**
 * Ballerina record value creation related JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmRecordCreatorGen {

    private final String recordsClass;

    public JvmRecordCreatorGen(PackageID packageID) {
        this.recordsClass = getModuleLevelClassName(packageID, MODULE_RECORDS_CREATOR_CLASS_NAME);
    }

    public void generateRecordsClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                     JarEntries jarEntries, List<BIRTypeDefinition> recordTypeDefList) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V21, ACC_PUBLIC + ACC_SUPER, recordsClass, null, OBJECT, null);
        generateCreateRecordMethods(cw, recordTypeDefList, recordsClass);
        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(recordsClass + CLASS_FILE_SUFFIX, bytes);
    }

    private void generateCreateRecordMethods(ClassWriter cw, List<BIRTypeDefinition> recordTypeDefList,
                                             String typeOwnerClass) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_RECORD_VALUE,
                CREATE_RECORD,
                CREATE_RECORD_WITH_MAP, null);
        mv.visitCode();
        if (recordTypeDefList.isEmpty()) {
            createDefaultCase(mv, new Label(), 0, "No such record: ");
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_RECORD_VALUE + 0, CREATE_RECORD, false);
            mv.visitInsn(ARETURN);
            generateCreateRecordMethodSplits(cw, recordTypeDefList, typeOwnerClass);
        }
        JvmCodeGenUtil.visitMaxStackForMethod(mv, CREATE_RECORD_VALUE, recordsClass);
        mv.visitEnd();
    }

    private void generateCreateRecordMethodSplits(ClassWriter cw, List<BIRTypeDefinition> recordTypeDefList,
                                                  String typeOwnerClass) {
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
                        CREATE_RECORD,
                        CREATE_RECORD_WITH_MAP, null);
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
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);

            BTypeSymbol referredTypeSymbol = JvmCodeGenUtil.getImpliedType(optionalTypeDef.type).tsymbol;
            String className = getTypeValueClassName(referredTypeSymbol.pkgID, referredTypeSymbol.name.getValue());
            mv.visitTypeInsn(NEW, className);
            mv.visitInsn(DUP);
            BType typeDefType = optionalTypeDef.type;
            BType recordType;
            if (typeDefType.tag == TypeTags.TYPEREFDESC) {
                recordType = optionalTypeDef.referenceType;
            } else {
                recordType = optionalTypeDef.type;
            }
            BType type = JvmCodeGenUtil.getImpliedType(recordType);
            String typeDescClassName = getTypeDescClassName(getPackageName(type.tsymbol.pkgID), toNameString(type));
            mv.visitFieldInsn(GETSTATIC, typeDescClassName, VALUE_VAR_NAME, GET_TYPEDESC);
            mv.visitMethodInsn(INVOKESPECIAL, className, JVM_INIT_METHOD, INIT_TYPEDESC, false);

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
                            CREATE_RECORD, false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + VISIT_MAX_SAFE_MARGIN, i + VISIT_MAX_SAFE_MARGIN);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_TYPES_PER_METHOD != 0) {
            createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex, "No such record: ");
            mv.visitMaxs(i + VISIT_MAX_SAFE_MARGIN, i + VISIT_MAX_SAFE_MARGIN);
            mv.visitEnd();
        }
    }
}
