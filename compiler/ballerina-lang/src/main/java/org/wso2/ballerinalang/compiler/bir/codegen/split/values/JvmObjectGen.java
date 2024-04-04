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

package org.wso2.ballerinalang.compiler.bir.codegen.split.values;

import org.ballerinalang.compiler.BLangCompilerException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.FieldNameHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.NAME_HASH_COMPARATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.castToJavaString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.createDefaultCase;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_VALUE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_CALLS_PER_CLIENT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_FIELDS_PER_SPLIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REPOSITORY_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VISIT_MAX_SAFE_MARGIN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ADD_SERVICE_LISTENER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.BOBJECT_CALL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CHECK_FIELD_UPDATE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_JSTRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_OBJECT_FOR_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.OBJECT_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.PASS_B_STRING_RETURN_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.PASS_OBJECT_RETURN_SAME_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeDesc;

/**
 * Class for generate {@link io.ballerina.runtime.api.values.BObject} related methods.
 *
 * @since 2.0.0
 */
public class JvmObjectGen {

    static final FieldNameHashComparator FIELD_NAME_HASH_COMPARATOR = new FieldNameHashComparator();

    public void createAndSplitCallMethod(ClassWriter cw, List<BIRNode.BIRFunction> functions, String objClassName,
                                         JvmCastGen jvmCastGen) {
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        int funcNameRegIndex = 2;
        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        functions.sort(NAME_HASH_COMPARATOR);

        // case body
        int i = 0;
        List<Label> targetLabels = new ArrayList<>();
        String callMethod = "call";
        for (BIRNode.BIRFunction optionalFunc : functions) {
            if (bTypesCount % MAX_CALLS_PER_CLIENT_METHOD == 0) {
                mv = cw.visitMethod(ACC_PUBLIC, callMethod, BOBJECT_CALL, null, null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                int remainingCases = functions.size() - bTypesCount;
                if (remainingCases > MAX_CALLS_PER_CLIENT_METHOD) {
                    remainingCases = MAX_CALLS_PER_CLIENT_METHOD;
                }
                List<Label> labels = JvmCreateTypeGen.createLabelsForSwitch(mv, funcNameRegIndex, functions,
                        bTypesCount, remainingCases, defaultCaseLabel, false);
                targetLabels = JvmCreateTypeGen.createLabelsForEqualCheck(mv, funcNameRegIndex, functions,
                        bTypesCount, remainingCases, labels, defaultCaseLabel, false);
                i = 0;
                callMethod = "call" + ++methodCount;
            }
            BIRNode.BIRFunction func = getFunction(optionalFunc);
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);

            List<BType> paramTypes = func.type.paramTypes;
            BType retType = func.type.retType;

            String methodSig;

            // use index access, since retType can be nil.
            methodSig = JvmCodeGenUtil.getMethodDesc(paramTypes, retType);

            // load self
            mv.visitVarInsn(ALOAD, 0);

            // load strand
            mv.visitVarInsn(ALOAD, 1);
            int j = 0;
            for (BType paramType : paramTypes) {
                // load parameters
                mv.visitVarInsn(ALOAD, 3);

                // load j parameter
                mv.visitLdcInsn((long) j);
                mv.visitInsn(L2I);
                mv.visitInsn(AALOAD);
                jvmCastGen.addUnboxInsn(mv, paramType);
                j += 1;
            }

            mv.visitMethodInsn(INVOKEVIRTUAL, objClassName, func.name.value,
                    methodSig, false);
            int retTypeTag = JvmCodeGenUtil.getImpliedType(retType).tag;
            if (retType == null || retTypeTag == TypeTags.NIL || retTypeTag == TypeTags.NEVER) {
                mv.visitInsn(ACONST_NULL);
            } else {
                jvmCastGen.addBoxInsn(mv, retType);
            }
            if (isListenerAttach(func)) {
                mv.visitVarInsn(ASTORE, 4);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitTypeInsn(CHECKCAST, B_OBJECT);
                mv.visitVarInsn(ALOAD, 3);
                mv.visitInsn(ICONST_0);
                mv.visitInsn(AALOAD);
                mv.visitTypeInsn(CHECKCAST, B_OBJECT);
                mv.visitVarInsn(ALOAD, 3);
                mv.visitInsn(ICONST_1);
                mv.visitInsn(AALOAD);
                mv.visitMethodInsn(INVOKESTATIC, REPOSITORY_IMPL, "addServiceListener", ADD_SERVICE_LISTENER,
                        false);
                mv.visitVarInsn(ALOAD, 4);
            }
            mv.visitInsn(ARETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_CALLS_PER_CLIENT_METHOD == 0) {
                if (bTypesCount == functions.size()) {
                    createDefaultCase(mv, defaultCaseLabel, funcNameRegIndex, "No such method: ");
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitVarInsn(ALOAD, 3);
                    mv.visitMethodInsn(INVOKEVIRTUAL, objClassName, "call" + methodCount, BOBJECT_CALL, false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + VISIT_MAX_SAFE_MARGIN, i + VISIT_MAX_SAFE_MARGIN);
                mv.visitEnd();
            }
        }

        if (methodCount != 0 && bTypesCount % MAX_CALLS_PER_CLIENT_METHOD != 0) {
            createDefaultCase(mv, defaultCaseLabel, funcNameRegIndex, "No such method: ");
            mv.visitMaxs(i + VISIT_MAX_SAFE_MARGIN, i + VISIT_MAX_SAFE_MARGIN);
            mv.visitEnd();
        }
    }

    private static boolean isListenerAttach(BIRNode.BIRFunction func) {
        return func.name.value.equals("attach") && Symbols.isFlagOn(func.parameters.get(0).type.flags, Flags.SERVICE);
    }

    public void createAndSplitGetMethod(ClassWriter cw, Map<String, BField> fields, String className,
                                        JvmCastGen jvmCastGen) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get", PASS_B_STRING_RETURN_OBJECT,
                PASS_OBJECT_RETURN_SAME_TYPE, null);
        mv.visitCode();
        int selfIndex = 0;
        int fieldNameRegIndex = 1;
        int strKeyVarIndex = 2;

        // cast key to java.lang.String
        castToJavaString(mv, fieldNameRegIndex, strKeyVarIndex);
        if (!fields.isEmpty()) {
            mv.visitVarInsn(ALOAD, selfIndex);
            mv.visitVarInsn(ALOAD, strKeyVarIndex);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "get",
                    GET_OBJECT_FOR_STRING, false);
            mv.visitInsn(ARETURN);
            JvmCodeGenUtil.visitMaxStackForMethod(mv, "get", className);
            mv.visitEnd();
            splitObjectGetMethod(cw, fields, className, jvmCastGen);
            return;
        }
        Label defaultCaseLabel = new Label();
        createDefaultCase(mv, defaultCaseLabel, strKeyVarIndex, "No such field: ");
        JvmCodeGenUtil.visitMaxStackForMethod(mv, "get", className);
        mv.visitEnd();
    }

    private void splitObjectGetMethod(ClassWriter cw, Map<String, BField> fields, String className,
                                      JvmCastGen jvmCastGen) {

        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        int selfRegIndex = 0;
        int strKeyVarIndex = 1;
        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        List<BField> sortedFields = new ArrayList<>(fields.values());
        sortedFields.sort(FIELD_NAME_HASH_COMPARATOR);

        List<Label> targetLabels = new ArrayList<>();

        int i = 0;
        String getMethod = "get";
        for (BField optionalField : sortedFields) {
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                mv = cw.visitMethod(ACC_PUBLIC, getMethod, GET_OBJECT_FOR_STRING,
                        null, null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                int remainingCases = sortedFields.size() - bTypesCount;
                if (remainingCases > MAX_FIELDS_PER_SPLIT_METHOD) {
                    remainingCases = MAX_FIELDS_PER_SPLIT_METHOD;
                }
                List<Label> labels = JvmCreateTypeGen.createLabelsForSwitch(mv, strKeyVarIndex, sortedFields,
                        bTypesCount, remainingCases, defaultCaseLabel);
                targetLabels = JvmCreateTypeGen.createLabelsForEqualCheck(mv, strKeyVarIndex, sortedFields,
                        bTypesCount, remainingCases, labels, defaultCaseLabel);
                i = 0;
                getMethod = "get" + ++methodCount;
            }
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, optionalField.name.value, getTypeDesc(optionalField.type));
            jvmCastGen.addBoxInsn(mv, optionalField.type);
            mv.visitInsn(ARETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                if (bTypesCount == sortedFields.size()) {
                    createDefaultCase(mv, defaultCaseLabel, strKeyVarIndex, "No such field: ");
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, selfRegIndex);
                    mv.visitVarInsn(ALOAD, strKeyVarIndex);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, getMethod, GET_OBJECT_FOR_STRING, false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + VISIT_MAX_SAFE_MARGIN, i + VISIT_MAX_SAFE_MARGIN);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD != 0) {
            createDefaultCase(mv, defaultCaseLabel, strKeyVarIndex, "No such field: ");
            mv.visitMaxs(i + VISIT_MAX_SAFE_MARGIN, i + VISIT_MAX_SAFE_MARGIN);
            mv.visitEnd();
        }
    }

    public void createAndSplitSetMethod(ClassWriter cw, Map<String, BField> fields, String className,
                                        JvmCastGen jvmCastGen) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "set", SET_VALUE,
                null, null);
        mv.visitCode();
        int selfIndex = 0;
        int fieldNameRegIndex = 1;
        int valueRegIndex = 2;
        int strKeyVarIndex = 3;

        // code gen type checking for inserted value
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitMethodInsn(INVOKEINTERFACE, B_STRING_VALUE, GET_VALUE_METHOD, GET_JSTRING,
                true);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ASTORE, strKeyVarIndex);
        mv.visitVarInsn(ALOAD, valueRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "checkFieldUpdate", CHECK_FIELD_UPDATE, false);
        if (!fields.isEmpty()) {
            callFirstSetMethod(className, mv, selfIndex, fieldNameRegIndex, valueRegIndex, strKeyVarIndex, "set");
            splitObjectSplitMethod(cw, fields, className, jvmCastGen);
            return;
        }
        Label defaultCaseLabel = new Label();
        createDefaultCase(mv, defaultCaseLabel, strKeyVarIndex, "No such field: ");
        JvmCodeGenUtil.visitMaxStackForMethod(mv, "set", className);
        mv.visitEnd();
    }

    public void createAndSplitSetOnInitializationMethod(ClassWriter cw, Map<String, BField> fields,
                                                        String className) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "setOnInitialization",
                SET_VALUE, null, null);
        mv.visitCode();
        int selfIndex = 0;
        int fieldNameRegIndex = 1;
        int valueRegIndex = 2;
        int strKeyVarIndex = 3;

        // cast key to java.lang.String
        castToJavaString(mv, fieldNameRegIndex, strKeyVarIndex);
        if (!fields.isEmpty()) {
            callFirstSetMethod(className, mv, selfIndex, fieldNameRegIndex, valueRegIndex,
                    strKeyVarIndex, "setOnInitialization");
            return;
        }
        Label defaultCaseLabel = new Label();
        createDefaultCase(mv, defaultCaseLabel, strKeyVarIndex, "No such field: ");
        JvmCodeGenUtil.visitMaxStackForMethod(mv, "setOnInitialization", className);
        mv.visitEnd();
    }

    private void callFirstSetMethod(String className, MethodVisitor mv, int selfIndex, int fieldNameRegIndex,
                                    int valueRegIndex, int strKeyVarIndex, String methodName) {
        mv.visitVarInsn(ALOAD, selfIndex);
        mv.visitVarInsn(ALOAD, strKeyVarIndex);
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitVarInsn(ALOAD, valueRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "set", OBJECT_SET, false);
        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, methodName, className);
        mv.visitEnd();
    }

    private void splitObjectSplitMethod(ClassWriter cw, Map<String, BField> fields, String className,
                                        JvmCastGen jvmCastGen) {
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        int selfRegIndex = 0;
        int strKeyVarIndex = 1;
        int fieldNameRegIndex = 2;
        int valueRegIndex = 3;
        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        List<BField> sortedFields = new ArrayList<>(fields.values());
        sortedFields.sort(FIELD_NAME_HASH_COMPARATOR);

        List<Label> targetLabels = new ArrayList<>();

        int i = 0;
        String setMethod = "set";
        for (BField optionalField : sortedFields) {
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                mv = cw.visitMethod(ACC_PROTECTED, setMethod, OBJECT_SET, null, null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                int remainingCases = sortedFields.size() - bTypesCount;
                if (remainingCases > MAX_FIELDS_PER_SPLIT_METHOD) {
                    remainingCases = MAX_FIELDS_PER_SPLIT_METHOD;
                }
                List<Label> labels = JvmCreateTypeGen.createLabelsForSwitch(mv, strKeyVarIndex, sortedFields,
                        bTypesCount, remainingCases, defaultCaseLabel);
                targetLabels = JvmCreateTypeGen.createLabelsForEqualCheck(mv, strKeyVarIndex, sortedFields,
                        bTypesCount, remainingCases, labels, defaultCaseLabel);
                i = 0;
                setMethod = "set" + ++methodCount;
            }
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, valueRegIndex);
            jvmCastGen.addUnboxInsn(mv, optionalField.type);
            String filedName = optionalField.name.value;
            mv.visitFieldInsn(PUTFIELD, className, filedName, getTypeDesc(optionalField.type));
            mv.visitInsn(RETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                if (bTypesCount == sortedFields.size()) {
                    createDefaultCase(mv, defaultCaseLabel, strKeyVarIndex, "No such field: ");
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, selfRegIndex);
                    mv.visitVarInsn(ALOAD, strKeyVarIndex);
                    mv.visitVarInsn(ALOAD, fieldNameRegIndex);
                    mv.visitVarInsn(ALOAD, valueRegIndex);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, setMethod, OBJECT_SET, false);
                    mv.visitInsn(RETURN);
                }
                JvmCodeGenUtil.visitMaxStackForMethod(mv, setMethod, className);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD != 0) {
            createDefaultCase(mv, defaultCaseLabel, strKeyVarIndex, "No such field: ");
            JvmCodeGenUtil.visitMaxStackForMethod(mv, setMethod, className);
            mv.visitEnd();
        }
    }

    private static BIRNode.BIRFunction getFunction(BIRNode.BIRFunction func) {
        if (func == null) {
            throw new BLangCompilerException("Invalid function");
        }
        return func;
    }
}
