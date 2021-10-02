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
package org.wso2.ballerinalang.compiler.bir.codegen.split.types;

import io.ballerina.runtime.api.utils.IdentifierUtils;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.constants.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.NAME_HASH_COMPARATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.createDefaultCase;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ERRORS_CREATOR_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ERROR_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_DETAIL_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_TYPEID_SET_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_ID_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeFieldName;
import static org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen.MAX_TYPES_PER_METHOD;

/**
 * BIR error type to JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmErrorTypeGen {

    private final String errorsClass;
    public final String errorTypesClass;
    public final ClassWriter errorTypesCw;
    private final JvmCreateTypeGen jvmCreateTypeGen;
    private final JvmTypeGen jvmTypeGen;
    private final  JvmConstantsGen jvmConstantsGen;

    public JvmErrorTypeGen(JvmCreateTypeGen jvmCreateTypeGen, JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen,
                           PackageID packageID) {
        this.errorTypesClass = getModuleLevelClassName(packageID, MODULE_ERROR_TYPES_CLASS_NAME);
        this.errorsClass = getModuleLevelClassName(packageID, MODULE_ERRORS_CREATOR_CLASS_NAME);
        this.jvmCreateTypeGen = jvmCreateTypeGen;
        this.jvmTypeGen = jvmTypeGen;
        this.jvmConstantsGen = jvmConstantsGen;
        this.errorTypesCw = new BallerinaClassWriter(COMPUTE_FRAMES);
        this.errorTypesCw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, errorTypesClass, null, OBJECT, null);
    }

    public void visitEnd(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module, Map<String, byte[]> jarEntries) {
        errorTypesCw.visitEnd();
        jarEntries.put(errorTypesClass + ".class", jvmPackageGen.getBytes(errorTypesCw, module));
    }


    /**
     * Create a runtime type instance for the error.
     *
     * @param mv        method visitor
     * @param errorType error type
     * @param name      name of the error
     */
    public void createErrorType(MethodVisitor mv, BErrorType errorType, String name) {
        // Create the error type
        mv.visitTypeInsn(NEW, ERROR_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load error type name
        mv.visitLdcInsn(IdentifierUtils.decodeIdentifier(name));

        // Load package
        String varName = jvmConstantsGen.getModuleConstantVar(errorType.tsymbol.pkgID);
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(), varName,
                String.format("L%s;", MODULE));
        // initialize the error type
        mv.visitMethodInsn(INVOKESPECIAL, ERROR_TYPE_IMPL, JVM_INIT_METHOD,
                String.format("(L%s;L%s;)V", STRING_VALUE, MODULE), false);
    }

    public  void populateError(MethodVisitor mv, BErrorType bType) {
        mv.visitTypeInsn(CHECKCAST, ERROR_TYPE_IMPL);
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        jvmTypeGen.loadType(mv, bType.detailType);
        mv.visitMethodInsn(INVOKEVIRTUAL, ERROR_TYPE_IMPL, SET_DETAIL_TYPE_METHOD,
                String.format("(L%s;)V", TYPE), false);
        BTypeIdSet typeIdSet = bType.typeIdSet;
        if (!typeIdSet.isEmpty()) {
            mv.visitInsn(DUP);
            jvmCreateTypeGen.loadTypeIdSet(mv, typeIdSet);
            mv.visitMethodInsn(INVOKEVIRTUAL, ERROR_TYPE_IMPL, SET_TYPEID_SET_METHOD,
                    String.format("(L%s;)V", TYPE_ID_SET), false);
        }
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
                String.format("(L%s;L%s;L%s;L%s;)L%s;", STRING_VALUE, B_STRING_VALUE, BERROR,
                        OBJECT, BERROR), null, null);
        mv.visitCode();
        if (errorTypeDefList.isEmpty()) {
            createDefaultCase(mv, new Label(), 1, "No such error: ");
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_ERROR_VALUE + 0,
                    String.format("(L%s;L%s;L%s;L%s;)L%s;", STRING_VALUE, B_STRING_VALUE, BERROR,
                            OBJECT, BERROR), false);
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
                        String.format("(L%s;L%s;L%s;L%s;)L%s;", STRING_VALUE, B_STRING_VALUE, BERROR, OBJECT, BERROR)
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
            mv.visitFieldInsn(GETSTATIC, moduleInitClass, fieldName, String.format("L%s;", TYPE));
            mv.visitVarInsn(ALOAD, messageIndex);
            mv.visitVarInsn(ALOAD, causeIndex);
            mv.visitVarInsn(ALOAD, detailsIndex);
            mv.visitMethodInsn(INVOKESPECIAL, ERROR_VALUE, JVM_INIT_METHOD,
                    String.format("(L%s;L%s;L%s;L%s;)V", TYPE, B_STRING_VALUE, BERROR, OBJECT), false);
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
                            String.format("(L%s;L%s;L%s;L%s;)L%s;", STRING_VALUE, B_STRING_VALUE, BERROR,
                                    OBJECT, BERROR), false);
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
