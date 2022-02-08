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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.FieldNameHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmCreateTypeGen;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.identifier.Utils.decodeIdentifier;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.castToJavaString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_SIMPLE_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_FIELDS_PER_SPLIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.UNSUPPORTED_OPERATION_EXCEPTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ADD_COLLECTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ANY_TO_JBOOLEAN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.COLLECTION_OP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CONTAINS_KEY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.FROM_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LINKED_HASH_SET_OP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.MAP_PUT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.MAP_VALUES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.MAP_VALUES_WITH_COLLECTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.PASS_OBJECT_RETURN_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.PASS_OBJECT_RETURN_SAME_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RECORD_GET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RECORD_GET_KEYS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RECORD_PUT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RECORD_REMOVE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RECORD_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RECORD_SET_MAP_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TO_ARRAY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TWO_OBJECTS_ARGS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeDesc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getFieldIsPresentFlagName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.isOptionalRecordField;

/**
 * Class for generate {@link io.ballerina.runtime.api.values.BMap} related methods.
 *
 * @since 2.0.0
 */
public class JvmRecordGen {

    static final FieldNameHashComparator FIELD_NAME_HASH_COMPARATOR = new FieldNameHashComparator();

    private final BType booleanType;

    public JvmRecordGen(SymbolTable symbolTable) {
        this.booleanType = symbolTable.booleanType;
    }

    public void createAndSplitGetMethod(ClassWriter cw, Map<String, BField> fields, String className,
                                        JvmCastGen jvmCastGen) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get", PASS_OBJECT_RETURN_OBJECT,
                PASS_OBJECT_RETURN_SAME_TYPE, null);
        mv.visitCode();
        int selfIndex = 0;
        int fieldNameRegIndex = 1;
        int strKeyVarIndex = 2;

        // cast key to java.lang.String
        castToJavaString(mv, fieldNameRegIndex, strKeyVarIndex);
        if (fields.isEmpty()) {
            Label defaultCaseLabel = new Label();
            this.createGetDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            return;
        }
        mv.visitVarInsn(ALOAD, selfIndex);
        mv.visitVarInsn(ALOAD, strKeyVarIndex);
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "get", RECORD_GET, false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        splitGetMethod(cw, fields, className, jvmCastGen);
    }

    private void splitGetMethod(ClassWriter cw, Map<String, BField> fields, String className,
                                JvmCastGen jvmCastGen) {
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        int selfRegIndex = 0;
        int strKeyVarIndex = 1;
        int fieldNameRegIndex = 2;
        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        List<BField> sortedFields = new ArrayList<>(fields.values());
        sortedFields.sort(FIELD_NAME_HASH_COMPARATOR);

        List<Label> targetLabels = new ArrayList<>();

        int i = 0;
        String getMethod = "get";
        for (BField optionalField : sortedFields) {
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                mv = cw.visitMethod(ACC_PUBLIC, getMethod, RECORD_GET, null, null);
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

            // if the field is an optional-field, first check the 'isPresent' flag of that field.
            Label ifPresentLabel = new Label();
            String fieldName = optionalField.name.value;
            if (isOptionalRecordField(optionalField)) {
                mv.visitVarInsn(ALOAD, selfRegIndex);
                mv.visitFieldInsn(GETFIELD, className, getFieldIsPresentFlagName(fieldName),
                        getTypeDesc(booleanType));
                mv.visitJumpInsn(IFNE, ifPresentLabel);
                mv.visitInsn(ACONST_NULL);
                mv.visitInsn(ARETURN);
            }

            mv.visitLabel(ifPresentLabel);
            // return the value of the field
            mv.visitVarInsn(ALOAD, selfRegIndex);
            mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(optionalField.type));
            jvmCastGen.addBoxInsn(mv, optionalField.type);
            mv.visitInsn(ARETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                if (bTypesCount == sortedFields.size()) {
                    this.createGetDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, selfRegIndex);
                    mv.visitVarInsn(ALOAD, strKeyVarIndex);
                    mv.visitVarInsn(ALOAD, fieldNameRegIndex);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, getMethod, RECORD_GET, false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + 10, i + 10);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD != 0) {
            this.createGetDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
            mv.visitMaxs(i + 10, i + 10);
            mv.visitEnd();
        }
    }

    private void createGetDefaultCase(MethodVisitor mv, Label defaultCaseLabel, int nameRegIndex) {
        mv.visitLabel(defaultCaseLabel);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, nameRegIndex);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "get", PASS_OBJECT_RETURN_OBJECT,
                false);
        mv.visitInsn(ARETURN);
    }

    public void createAndSplitSetMethod(ClassWriter cw, Map<String, BField> fields, String className,
                                        JvmCastGen jvmCastGen) {
        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "putValue", MAP_PUT, "(TK;TV;)TV;", null);
        mv.visitCode();
        int selfIndex = 0;
        int fieldNameRegIndex = 1;
        int valueRegIndex = 2;
        int strKeyVarIndex = 3;

        // cast key to java.lang.String
        castToJavaString(mv, fieldNameRegIndex, strKeyVarIndex);
        if (fields.isEmpty()) {
            Label defaultCaseLabel = new Label();
            this.createPutDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex, valueRegIndex);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            return;
        }
        mv.visitVarInsn(ALOAD, selfIndex);
        mv.visitVarInsn(ALOAD, strKeyVarIndex);
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitVarInsn(ALOAD, valueRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "putValue", RECORD_PUT, false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        splitSetMethod(cw, fields, className, jvmCastGen);
    }

    private void splitSetMethod(ClassWriter cw, Map<String, BField> fields, String className,
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
        String setMethod = "putValue";
        for (BField optionalField : sortedFields) {
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                mv = cw.visitMethod(ACC_PROTECTED, setMethod, RECORD_PUT, null, null);
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
                setMethod = "putValue" + ++methodCount;
            }
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);

            // load the existing value to return
            String fieldName = optionalField.name.value;
            mv.visitVarInsn(ALOAD, selfRegIndex);
            mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(optionalField.type));
            jvmCastGen.addBoxInsn(mv, optionalField.type);

            mv.visitVarInsn(ALOAD, selfRegIndex);
            mv.visitVarInsn(ALOAD, valueRegIndex);
            jvmCastGen.addUnboxInsn(mv, optionalField.type);
            mv.visitFieldInsn(PUTFIELD, className, fieldName, getTypeDesc(optionalField.type));

            // if the field is an optional-field, then also set the isPresent flag of that field to true.
            if (isOptionalRecordField(optionalField)) {
                mv.visitVarInsn(ALOAD, selfRegIndex);
                mv.visitInsn(ICONST_1);
                mv.visitFieldInsn(PUTFIELD, className, getFieldIsPresentFlagName(fieldName),
                        getTypeDesc(booleanType));
            }

            mv.visitInsn(ARETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                if (bTypesCount == sortedFields.size()) {
                    this.createPutDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex, valueRegIndex);
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, selfRegIndex);
                    mv.visitVarInsn(ALOAD, strKeyVarIndex);
                    mv.visitVarInsn(ALOAD, fieldNameRegIndex);
                    mv.visitVarInsn(ALOAD, valueRegIndex);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, setMethod, RECORD_PUT, false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD != 0) {
            this.createPutDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex, valueRegIndex);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private void createPutDefaultCase(MethodVisitor mv, Label defaultCaseLabel, int nameRegIndex,
                                      int valueRegIndex) {
        mv.visitLabel(defaultCaseLabel);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, nameRegIndex);
        mv.visitVarInsn(ALOAD, valueRegIndex);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "putValue",
                MAP_PUT, false);
        mv.visitInsn(ARETURN);
    }

    public void createAndSplitEntrySetMethod(ClassWriter cw, Map<String, BField> fields, String className,
                                             JvmCastGen jvmCastGen) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "entrySet", RECORD_SET, RECORD_SET_MAP_ENTRY, null);
        mv.visitCode();
        int selfIndex = 0;
        int entrySetVarIndex = 1;
        mv.visitTypeInsn(NEW, LINKED_HASH_SET);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_SET, JVM_INIT_METHOD, "()V", false);
        mv.visitVarInsn(ASTORE, entrySetVarIndex);
        if (!fields.isEmpty()) {
            mv.visitVarInsn(ALOAD, selfIndex);
            mv.visitVarInsn(ALOAD, entrySetVarIndex);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "addEntry", LINKED_HASH_SET_OP,
                    false);
            splitEntrySetMethod(cw, fields, className, jvmCastGen);
        }
        // Add all from super.entrySet() to the current entry set.
        mv.visitVarInsn(ALOAD, entrySetVarIndex);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, "entrySet", RECORD_SET, false);
        mv.visitMethodInsn(INVOKEINTERFACE, SET, "addAll", ADD_COLLECTION, true);
        mv.visitInsn(POP);

        mv.visitVarInsn(ALOAD, entrySetVarIndex);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void splitEntrySetMethod(ClassWriter cw, Map<String, BField> fields, String className,
                                     JvmCastGen jvmCastGen) {

        int selfRegIndex = 0;
        int entrySetVarIndex = 1;
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        String addEntryMethod = "addEntry";
        for (BField optionalField : fields.values()) {
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                mv = cw.visitMethod(ACC_PRIVATE, addEntryMethod, LINKED_HASH_SET_OP, null,
                        null);
                mv.visitCode();
                addEntryMethod = "addEntry" + ++methodCount;
            }
            Label ifNotPresent = new Label();

            // If its an optional field, generate if-condition to check the presence of the field.
            String fieldName = optionalField.name.value;
            if (isOptionalRecordField(optionalField)) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, getFieldIsPresentFlagName(fieldName),
                        getTypeDesc(booleanType));
                mv.visitJumpInsn(IFEQ, ifNotPresent);
            }

            mv.visitVarInsn(ALOAD, entrySetVarIndex);
            mv.visitTypeInsn(NEW, MAP_SIMPLE_ENTRY);
            mv.visitInsn(DUP);

            // field name as key
            mv.visitLdcInsn(decodeIdentifier(fieldName));
            mv.visitMethodInsn(INVOKESTATIC, STRING_UTILS, "fromString",
                    FROM_STRING, false);

            // field value as the map-entry value
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(optionalField.type));
            jvmCastGen.addBoxInsn(mv, optionalField.type);

            mv.visitMethodInsn(INVOKESPECIAL, MAP_SIMPLE_ENTRY, JVM_INIT_METHOD, TWO_OBJECTS_ARGS, false);
            mv.visitMethodInsn(INVOKEINTERFACE, SET, "add", ANY_TO_JBOOLEAN, true);
            mv.visitInsn(POP);

            mv.visitLabel(ifNotPresent);
            bTypesCount++;
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                if (bTypesCount != fields.size()) {
                    mv.visitVarInsn(ALOAD, selfRegIndex);
                    mv.visitVarInsn(ALOAD, entrySetVarIndex);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, addEntryMethod, LINKED_HASH_SET_OP, false);
                }
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD != 0) {
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    public void createAndSplitContainsKeyMethod(ClassWriter cw, Map<String, BField> fields, String className) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "containsKey", ANY_TO_JBOOLEAN, null, null);
        mv.visitCode();
        int selfIndex = 0;
        int fieldNameRegIndex = 1;
        int strKeyVarIndex = 2;

        // cast key to java.lang.String
        castToJavaString(mv, fieldNameRegIndex, strKeyVarIndex);
        if (fields.isEmpty()) {
            Label defaultCaseLabel = new Label();
            this.createContainsDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            return;
        }
        mv.visitVarInsn(ALOAD, selfIndex);
        mv.visitVarInsn(ALOAD, strKeyVarIndex);
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "containsKey", CONTAINS_KEY, false);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        splitContainsKeyMethod(cw, fields, className);
    }

    private void splitContainsKeyMethod(ClassWriter cw, Map<String, BField> fields, String className) {

        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        int selfRegIndex = 0;
        int strKeyVarIndex = 1;
        int fieldNameRegIndex = 2;
        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        List<BField> sortedFields = new ArrayList<>(fields.values());
        sortedFields.sort(FIELD_NAME_HASH_COMPARATOR);

        List<Label> targetLabels = new ArrayList<>();

        int i = 0;
        String containsMethod = "containsKey";
        for (BField optionalField : sortedFields) {
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                mv = cw.visitMethod(ACC_PUBLIC, containsMethod, CONTAINS_KEY, null, null);
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
                containsMethod = "containsKey" + ++methodCount;
            }
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);

            String fieldName = optionalField.name.value;
            if (isOptionalRecordField(optionalField)) {
                // if the field is optional, then return the value is the 'isPresent' flag.
                mv.visitVarInsn(ALOAD, selfRegIndex);
                mv.visitFieldInsn(GETFIELD, className, getFieldIsPresentFlagName(fieldName),
                        getTypeDesc(booleanType));
            } else {
                // else always return true.
                mv.visitLdcInsn(true);
            }

            mv.visitInsn(IRETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                if (bTypesCount == sortedFields.size()) {
                    this.createContainsDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, selfRegIndex);
                    mv.visitVarInsn(ALOAD, strKeyVarIndex);
                    mv.visitVarInsn(ALOAD, fieldNameRegIndex);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, containsMethod, CONTAINS_KEY, false);
                    mv.visitInsn(IRETURN);
                }
                mv.visitMaxs(i + 10, i + 10);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD != 0) {
            this.createContainsDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
            mv.visitMaxs(i + 10, i + 10);
            mv.visitEnd();
        }
    }

    private void createContainsDefaultCase(MethodVisitor mv, Label defaultCaseLabel, int fieldNameRegIndex) {
        mv.visitLabel(defaultCaseLabel);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "containsKey", ANY_TO_JBOOLEAN, false);
        mv.visitInsn(IRETURN);
    }

    public void createAndSplitGetValuesMethod(ClassWriter cw, Map<String, BField> fields, String className,
                                              JvmCastGen jvmCastGen) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "values", MAP_VALUES, MAP_VALUES_WITH_COLLECTION, null);
        mv.visitCode();
        int selfIndex = 0;
        int valuesVarIndex = 1;
        mv.visitTypeInsn(NEW, ARRAY_LIST);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, ARRAY_LIST, JVM_INIT_METHOD, "()V", false);
        mv.visitVarInsn(ASTORE, valuesVarIndex);
        if (!fields.isEmpty()) {
            mv.visitVarInsn(ALOAD, selfIndex);
            mv.visitVarInsn(ALOAD, valuesVarIndex);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "values", COLLECTION_OP,
                    false);
            splitGetValuesMethod(cw, fields, className, jvmCastGen);
        }
        mv.visitVarInsn(ALOAD, valuesVarIndex);
        mv.visitVarInsn(ALOAD, 0); // this
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "values", MAP_VALUES, false);
        mv.visitMethodInsn(INVOKEINTERFACE, LIST, "addAll", ADD_COLLECTION, true);
        mv.visitInsn(POP);

        mv.visitVarInsn(ALOAD, 1);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void splitGetValuesMethod(ClassWriter cw, Map<String, BField> fields, String className,
                                      JvmCastGen jvmCastGen) {
        int selfRegIndex = 0;
        int valuesVarIndex = 1;
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        String valuesMethod = "values";
        for (BField optionalField : fields.values()) {
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                mv = cw.visitMethod(ACC_PRIVATE, valuesMethod, COLLECTION_OP, null,
                        null);
                mv.visitCode();
                valuesMethod = "values" + ++methodCount;
            }
            Label ifNotPresent = new Label();

            // If its an optional field, generate if-condition to check the presence of the field.
            String fieldName = optionalField.name.value;
            if (isOptionalRecordField(optionalField)) {
                mv.visitVarInsn(ALOAD, 0); // this
                mv.visitFieldInsn(GETFIELD, className, getFieldIsPresentFlagName(fieldName),
                        getTypeDesc(booleanType));
                mv.visitJumpInsn(IFEQ, ifNotPresent);
            }

            mv.visitVarInsn(ALOAD, valuesVarIndex);
            mv.visitVarInsn(ALOAD, 0); // this
            mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(optionalField.type));
            jvmCastGen.addBoxInsn(mv, optionalField.type);
            mv.visitMethodInsn(INVOKEINTERFACE, LIST, "add", ANY_TO_JBOOLEAN, true);
            mv.visitInsn(POP);
            mv.visitLabel(ifNotPresent);
            bTypesCount++;
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                if (bTypesCount != fields.size()) {
                    mv.visitVarInsn(ALOAD, selfRegIndex);
                    mv.visitVarInsn(ALOAD, valuesVarIndex);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, valuesMethod, COLLECTION_OP,
                            false);
                }
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD != 0) {
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    public void createAndSplitRemoveMethod(ClassWriter cw, Map<String, BField> fields, String className,
                                           JvmCastGen jvmCastGen) {
        // throw an UnsupportedOperationException, since remove is not supported by for records.
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "remove", PASS_OBJECT_RETURN_OBJECT,
                PASS_OBJECT_RETURN_SAME_TYPE, null);
        mv.visitCode();
        int selfRegIndex = 0;
        int fieldNameRegIndex = 1;
        int strKeyVarIndex = 2;

        // cast key to java.lang.String
        castToJavaString(mv, fieldNameRegIndex, strKeyVarIndex);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "validateFreezeStatus", "()V", false);
        if (fields.isEmpty()) {
            Label defaultCaseLabel = new Label();
            this.createRemoveDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            return;
        }
        mv.visitVarInsn(ALOAD, selfRegIndex);
        mv.visitVarInsn(ALOAD, strKeyVarIndex);
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "remove", RECORD_REMOVE, false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        splitRemoveMethod(cw, fields, className, jvmCastGen);
    }

    private void splitRemoveMethod(ClassWriter cw, Map<String, BField> fields, String className,
                                   JvmCastGen jvmCastGen) {
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        int selfRegIndex = 0;
        int strKeyVarIndex = 1;
        int fieldNameRegIndex = 2;
        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        List<BField> sortedFields = new ArrayList<>(fields.values());
        sortedFields.sort(FIELD_NAME_HASH_COMPARATOR);

        List<Label> targetLabels = new ArrayList<>();

        int i = 0;
        String removeMethod = "remove";
        for (BField optionalField : sortedFields) {
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                mv = cw.visitMethod(ACC_PROTECTED, removeMethod, RECORD_REMOVE, null, null);
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
                removeMethod = "remove" + ++methodCount;
            }
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);

            //Setting isPresent as zero
            if (isOptionalRecordField(optionalField)) {
                String fieldName = optionalField.name.value;
                mv.visitVarInsn(ALOAD, 0);
                mv.visitInsn(ICONST_0);
                mv.visitFieldInsn(PUTFIELD, className, getFieldIsPresentFlagName(fieldName),
                        getTypeDesc(booleanType));

                // load the existing value to return
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, fieldName, getTypeDesc(optionalField.type));
                jvmCastGen.addBoxInsn(mv, optionalField.type);

                // Set default value for reference types
                if (checkIfValueIsJReferenceType(optionalField.type)) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitInsn(ACONST_NULL);
                    mv.visitFieldInsn(PUTFIELD, className, fieldName, getTypeDesc(optionalField.type));
                }

                mv.visitInsn(ARETURN);
            } else {
                mv.visitTypeInsn(NEW, UNSUPPORTED_OPERATION_EXCEPTION);
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, UNSUPPORTED_OPERATION_EXCEPTION, JVM_INIT_METHOD, "()V", false);
                mv.visitInsn(ATHROW);
            }
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                if (bTypesCount == sortedFields.size()) {
                    this.createRemoveDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, selfRegIndex);
                    mv.visitVarInsn(ALOAD, strKeyVarIndex);
                    mv.visitVarInsn(ALOAD, fieldNameRegIndex);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, removeMethod, RECORD_REMOVE, false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD != 0) {
            this.createRemoveDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private void createRemoveDefaultCase(MethodVisitor mv, Label defaultCaseLabel, int fieldNameRegIndex) {
        mv.visitLabel(defaultCaseLabel);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitMethodInsn(INVOKESPECIAL, MAP_VALUE_IMPL, "remove",
                PASS_OBJECT_RETURN_OBJECT, false);
        mv.visitInsn(ARETURN);
    }

    public void createAndSplitGetKeysMethod(ClassWriter cw, Map<String, BField> fields, String className) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getKeys", RECORD_GET_KEYS, "()[TK;", null);
        mv.visitCode();
        int selfIndex = 0;
        int keysVarIndex = 1;
        mv.visitTypeInsn(NEW, LINKED_HASH_SET);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_SET, JVM_INIT_METHOD, "()V", false);
        mv.visitVarInsn(ASTORE, keysVarIndex);

        if (!fields.isEmpty()) {
            mv.visitVarInsn(ALOAD, selfIndex);
            mv.visitVarInsn(ALOAD, keysVarIndex);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "getKeys", LINKED_HASH_SET_OP,
                    false);
            splitGetKeysMethod(cw, fields, className);
        }
        mv.visitVarInsn(ALOAD, keysVarIndex);
        mv.visitVarInsn(ALOAD, selfIndex); // this
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, "keySet", RECORD_SET, false);
        mv.visitMethodInsn(INVOKEINTERFACE, SET, "addAll", ADD_COLLECTION, true);
        mv.visitInsn(POP);

        mv.visitVarInsn(ALOAD, keysVarIndex);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKEINTERFACE, SET, "size", "()I", true);
        mv.visitTypeInsn(ANEWARRAY, B_STRING_VALUE);
        mv.visitMethodInsn(INVOKEINTERFACE, SET, "toArray", TO_ARRAY, true);

        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void splitGetKeysMethod(ClassWriter cw, Map<String, BField> fields, String className) {

        int selfRegIndex = 0;
        int keysVarIndex = 1;
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        String getKeysMethod = "getKeys";
        for (BField optionalField : fields.values()) {
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                mv = cw.visitMethod(ACC_PRIVATE, getKeysMethod, LINKED_HASH_SET_OP, null,
                        null);
                mv.visitCode();
                getKeysMethod = "getKeys" + ++methodCount;
            }
            Label ifNotPresent = new Label();

            // If its an optional field, generate if-condition to check the presense of the field.
            String fieldName = optionalField.name.value;
            if (isOptionalRecordField(optionalField)) {
                mv.visitVarInsn(ALOAD, 0); // this
                mv.visitFieldInsn(GETFIELD, className, getFieldIsPresentFlagName(fieldName),
                        getTypeDesc(booleanType));
                mv.visitJumpInsn(IFEQ, ifNotPresent);
            }

            mv.visitVarInsn(ALOAD, keysVarIndex);
            mv.visitLdcInsn(decodeIdentifier(fieldName));
            mv.visitMethodInsn(INVOKESTATIC, STRING_UTILS, "fromString",
                    FROM_STRING, false);
            mv.visitMethodInsn(INVOKEINTERFACE, SET, "add", ANY_TO_JBOOLEAN, true);
            mv.visitInsn(POP);
            mv.visitLabel(ifNotPresent);
            bTypesCount++;
            if (bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                if (bTypesCount != fields.size()) {
                    mv.visitVarInsn(ALOAD, selfRegIndex);
                    mv.visitVarInsn(ALOAD, keysVarIndex);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, getKeysMethod, LINKED_HASH_SET_OP, false);
                }
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_FIELDS_PER_SPLIT_METHOD != 0) {
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private boolean checkIfValueIsJReferenceType(BType bType) {

        switch (bType.getKind()) {
            case INT:
            case BOOLEAN:
            case FLOAT:
            case BYTE:
                return false;
            case TYPEREFDESC:
                return checkIfValueIsJReferenceType(((BTypeReferenceType) bType).referredType);
            default:
                return true;
        }
    }
}
