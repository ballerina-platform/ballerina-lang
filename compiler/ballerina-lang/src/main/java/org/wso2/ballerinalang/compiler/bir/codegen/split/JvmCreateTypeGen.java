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
package org.wso2.ballerinalang.compiler.bir.codegen.split;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.TypeDefHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmArrayTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmErrorTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmObjectTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmRecordTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmRefTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmTupleTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmUnionTypeGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeHashVisitor;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.NamedNode;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static io.ballerina.identifier.Utils.decodeIdentifier;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.createDefaultCase;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_ARRAY_TYPE_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_ARRAY_TYPE_POPULATE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_ERROR_TYPE_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_ERROR_TYPE_POPULATE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_TUPLE_TYPE_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_TUPLE_TYPE_POPULATE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_TYPEREF_TYPE_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_TYPEREF_TYPE_POPULATE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_UNION_TYPE_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_UNION_TYPE_POPULATE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_TYPES_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_TYPE_CONSTANTS_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_TYPE_INSTANCES_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FIELD_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_ANON_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_FIELDS_PER_SPLIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_TYPES_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ANON_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_IMMUTABLE_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_ID_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VISIT_MAX_SAFE_MARGIN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ADD_TYPE_ID;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ANY_TO_JBOOLEAN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_FIELD_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.MAP_PUT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_IMMUTABLE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeFieldName;

/**
 * BIR types to JVM byte code generation class.
 *
 * @since 2.0.0
 */
public class JvmCreateTypeGen {

    private final JvmTypeGen jvmTypeGen;
    private final JvmConstantsGen jvmConstantsGen;
    private final JvmRecordTypeGen jvmRecordTypeGen;
    private final JvmObjectTypeGen jvmObjectTypeGen;
    private final JvmErrorTypeGen jvmErrorTypeGen;
    private final JvmUnionTypeGen jvmUnionTypeGen;
    private final JvmTupleTypeGen jvmTupleTypeGen;
    private final JvmArrayTypeGen jvmArrayTypeGen;
    private final JvmRefTypeGen jvmRefTypeGen;
    private final TypeHashVisitor typeHashVisitor;
    public final TypeDefHashComparator typeDefHashComparator;
    private final String typesClass;
    private final String anonTypesClass;
    private final ClassWriter typesCw;

    public JvmCreateTypeGen(JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen, PackageID packageID,
                            TypeHashVisitor typeHashVisitor) {
        this.jvmTypeGen = jvmTypeGen;
        this.jvmConstantsGen = jvmConstantsGen;
        this.typesClass = getModuleLevelClassName(packageID, MODULE_TYPES_CLASS_NAME);
        this.anonTypesClass = getModuleLevelClassName(packageID, MODULE_ANON_TYPES_CLASS_NAME);
        this.jvmRecordTypeGen = new JvmRecordTypeGen(this, jvmTypeGen, jvmConstantsGen, packageID);
        this.jvmObjectTypeGen = new JvmObjectTypeGen(this, typesClass, jvmTypeGen, jvmConstantsGen, packageID);
        this.jvmErrorTypeGen = new JvmErrorTypeGen(this, jvmTypeGen, jvmConstantsGen, packageID);
        this.jvmUnionTypeGen = new JvmUnionTypeGen(this, jvmTypeGen, jvmConstantsGen, packageID);
        this.jvmTupleTypeGen = new JvmTupleTypeGen(this, jvmTypeGen, jvmConstantsGen, packageID);
        this.jvmArrayTypeGen = new JvmArrayTypeGen(jvmTypeGen);
        this.jvmRefTypeGen = new JvmRefTypeGen(jvmTypeGen, jvmConstantsGen);
        this.typesCw = new BallerinaClassWriter(COMPUTE_FRAMES);
        this.typeHashVisitor =  typeHashVisitor;
        this.typeDefHashComparator = new TypeDefHashComparator(typeHashVisitor);
        typesCw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, typesClass, null, OBJECT, null);
    }

    public void generateTypeClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                  Map<String, byte[]> jarEntries,
                                  String moduleInitClass, SymbolTable symbolTable) {
        generateCreateTypesMethod(typesCw, module.typeDefs, moduleInitClass, symbolTable);
        typesCw.visitEnd();
        jvmRecordTypeGen.visitEnd(jvmPackageGen, module, jarEntries);
        jvmObjectTypeGen.visitEnd(jvmPackageGen, module, jarEntries);
        jvmErrorTypeGen.visitEnd(jvmPackageGen, module, jarEntries);
        jvmUnionTypeGen.visitEnd(jvmPackageGen, module, jarEntries);
        jvmTupleTypeGen.visitEnd(jvmPackageGen, module, jarEntries);
        jarEntries.put(typesClass + ".class", jvmPackageGen.getBytes(typesCw, module));
    }

    void createTypeConstants(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_TYPE_CONSTANTS_METHOD, "()V", null, null);
        mv.visitCode();

        String refTypeConstantsClass = jvmConstantsGen.getRefTypeConstantsClass();
        String arrayTypeConstantClass = jvmConstantsGen.getArrayTypeConstantClass();
        String tupleTypeConstantsClass = jvmConstantsGen.getTupleTypeConstantsClass();
        String unionTypeConstantClass = jvmConstantsGen.getUnionTypeConstantClass();
        String errorTypeConstantClass = jvmConstantsGen.getErrorTypeConstantClass();

        mv.visitMethodInsn(INVOKESTATIC, refTypeConstantsClass, B_TYPEREF_TYPE_INIT_METHOD, "()V", false);
        mv.visitMethodInsn(INVOKESTATIC, arrayTypeConstantClass, B_ARRAY_TYPE_INIT_METHOD, "()V", false);
        mv.visitMethodInsn(INVOKESTATIC, tupleTypeConstantsClass, B_TUPLE_TYPE_INIT_METHOD, "()V", false);
        mv.visitMethodInsn(INVOKESTATIC, unionTypeConstantClass, B_UNION_TYPE_INIT_METHOD, "()V", false);
        mv.visitMethodInsn(INVOKESTATIC, errorTypeConstantClass, B_ERROR_TYPE_INIT_METHOD, "()V", false);

        mv.visitMethodInsn(INVOKESTATIC, refTypeConstantsClass, B_TYPEREF_TYPE_POPULATE_METHOD, "()V", false);
        mv.visitMethodInsn(INVOKESTATIC, arrayTypeConstantClass, B_ARRAY_TYPE_POPULATE_METHOD, "()V", false);
        mv.visitMethodInsn(INVOKESTATIC, tupleTypeConstantsClass, B_TUPLE_TYPE_POPULATE_METHOD, "()V", false);
        mv.visitMethodInsn(INVOKESTATIC, unionTypeConstantClass, B_UNION_TYPE_POPULATE_METHOD, "()V", false);
        mv.visitMethodInsn(INVOKESTATIC, errorTypeConstantClass, B_ERROR_TYPE_POPULATE_METHOD, "()V", false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    void generateCreateTypesMethod(ClassWriter cw, List<BIRTypeDefinition> typeDefs,
                                   String moduleInitClass, SymbolTable symbolTable) {
        createTypeConstants(cw);
        createTypesInstance(cw, typeDefs, moduleInitClass);
        Map<String, String> populateTypeFuncNames = populateTypes(cw, typeDefs, moduleInitClass, symbolTable);

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_TYPES_METHOD, "()V", null, null);
        mv.visitCode();

        // Invoke create-type-instances method
        mv.visitMethodInsn(INVOKESTATIC, typesClass, CREATE_TYPE_INSTANCES_METHOD, "()V", false);

        // Invoke create-type-constants method
        mv.visitMethodInsn(INVOKESTATIC, typesClass, CREATE_TYPE_CONSTANTS_METHOD, "()V", false);

        // Invoke the populate-type functions
        for (Map.Entry<String, String> entry : populateTypeFuncNames.entrySet()) {
            String funcName = entry.getKey();
            String typeClassName = entry.getValue();
            mv.visitMethodInsn(INVOKESTATIC, typeClassName, funcName, "()V", false);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void createTypesInstance(ClassWriter cw, List<BIRTypeDefinition> typeDefs, String moduleInitClass) {
        int instanceSplits = createTypesInstanceSplits(cw, typeDefs, moduleInitClass);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_TYPE_INSTANCES_METHOD, "()V", null, null);
        mv.visitCode();
        for (int i = 0; i < instanceSplits; i++) {
            mv.visitMethodInsn(INVOKESTATIC, typesClass, CREATE_TYPE_INSTANCES_METHOD + i, "()V", false);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private int createTypesInstanceSplits(ClassWriter cw, List<BIRTypeDefinition> typeDefs, String typeOwnerClass) {

        if (typeDefs.isEmpty()) {
            return 0;
        }
        MethodVisitor mv = null;
        int bTypesCount = 0;
        int methodCount = 0;

        // Create the type
        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            String name = optionalTypeDef.internalName.value;
            BType bType = JvmCodeGenUtil.getReferredType(optionalTypeDef.type);
            if (bType.tag != TypeTags.RECORD && bType.tag != TypeTags.OBJECT && bType.tag != TypeTags.ERROR &&
                    bType.tag != TypeTags.UNION && bType.tag != TypeTags.TUPLE) {
                        // do not generate anything for other types (e.g.: finite type, etc.)
                        continue;
                    } else {
                if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                    mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_TYPE_INSTANCES_METHOD + methodCount++, "()V",
                            null, null);
                    mv.visitCode();
                }
            }
            switch (bType.tag) {
                case TypeTags.RECORD:
                    jvmRecordTypeGen.createRecordType(mv, (BRecordType) bType, typeOwnerClass, name);
                    break;
                case TypeTags.OBJECT:
                    jvmObjectTypeGen.createObjectType(mv, (BObjectType) bType);
                    break;
                case TypeTags.ERROR:
                    jvmErrorTypeGen.createErrorType(mv, (BErrorType) bType, bType.tsymbol.name.value);
                    break;
                case TypeTags.TUPLE:
                    jvmTupleTypeGen.createTupleType(mv, (BTupleType) bType);
                    break;
                default:
                    jvmUnionTypeGen.createUnionType(mv, (BUnionType) bType);
                    break;
            }

            mv.visitFieldInsn(PUTSTATIC, typeOwnerClass, getTypeFieldName(name), GET_TYPE);
            bTypesCount++;
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_TYPES_PER_METHOD != 0) {
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        return methodCount;
    }

    private Map<String, String> populateTypes(ClassWriter cw, List<BIRTypeDefinition> typeDefs, String typeOwnerClass,
                                              SymbolTable symbolTable) {

        Map<String, String> funcTypeClassMap = new HashMap<>();
        String fieldName;
        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            BType bType = JvmCodeGenUtil.getReferredType(optionalTypeDef.type);
            int bTypeTag = bType.tag;
            if (!(bTypeTag == TypeTags.RECORD || bTypeTag == TypeTags.ERROR || bTypeTag == TypeTags.OBJECT
                    || bTypeTag == TypeTags.UNION || bTypeTag == TypeTags.TUPLE)) {
                continue;
            }

            fieldName = getTypeFieldName(optionalTypeDef.internalName.value);
            String methodName = "$populate" + fieldName;
            MethodVisitor mv;
            switch (bTypeTag) {
                case TypeTags.RECORD:
                    funcTypeClassMap.put(methodName, jvmRecordTypeGen.recordTypesClass);
                    mv = createPopulateTypeMethod(jvmRecordTypeGen.recordTypesCw, methodName, typeOwnerClass,
                            fieldName);
                    jvmRecordTypeGen.populateRecord(mv, methodName, (BRecordType) bType, symbolTable);
                    break;
                case TypeTags.OBJECT:
                    funcTypeClassMap.put(methodName, jvmObjectTypeGen.objectTypesClass);
                    mv = createPopulateTypeMethod(jvmObjectTypeGen.objectTypesCw, methodName,
                            typeOwnerClass, fieldName);
                    jvmObjectTypeGen.populateObject(cw, mv, methodName, symbolTable, fieldName, (BObjectType) bType,
                            new BIRVarToJVMIndexMap());
                    break;
                case TypeTags.ERROR:
                    // populate detail field
                    funcTypeClassMap.put(methodName, jvmErrorTypeGen.errorTypesClass);
                    mv = createPopulateTypeMethod(jvmErrorTypeGen.errorTypesCw, methodName, typeOwnerClass, fieldName);
                    jvmErrorTypeGen.populateError(mv, (BErrorType) bType);
                    break;
                case TypeTags.TUPLE:
                    funcTypeClassMap.put(methodName, jvmTupleTypeGen.tupleTypesClass);
                    mv = createPopulateTypeMethod(jvmTupleTypeGen.tupleTypesCw, methodName, typeOwnerClass, fieldName);
                    jvmTupleTypeGen.populateTuple(mv, (BTupleType) bType, symbolTable);
                    break;
                default:
                    funcTypeClassMap.put(methodName, jvmUnionTypeGen.unionTypesClass);
                    mv = createPopulateTypeMethod(jvmUnionTypeGen.unionTypesCw, methodName, typeOwnerClass, fieldName);
                    jvmUnionTypeGen.populateUnion(cw, mv, (BUnionType) bType, typesClass, fieldName, symbolTable);
                    break;
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        return funcTypeClassMap;
    }

    private MethodVisitor createPopulateTypeMethod(ClassWriter cw, String methodName, String typeOwnerClass,
                                                   String fieldName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, "()V", null, null);
        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, GET_TYPE);
        return mv;
    }

    public void addImmutableType(MethodVisitor mv, BType type, SymbolTable symbolTable) {
        if (type.tsymbol == null) {
            return;
        }

        Optional<BIntersectionType> immutableType = Types.getImmutableType(symbolTable, type.tsymbol.pkgID,
                                                                           (SelectivelyImmutableReferenceType) type);
        if (immutableType.isEmpty()) {
            return;
        }

        mv.visitInsn(DUP);
        jvmTypeGen.loadType(mv, immutableType.get());
        mv.visitMethodInsn(INVOKEINTERFACE, TYPE, SET_IMMUTABLE_TYPE_METHOD, SET_IMMUTABLE_TYPE, true);
    }

    public void loadTypeIdSet(MethodVisitor mv, BTypeIdSet typeIdSet) {
        // Create TypeIdSet
        mv.visitTypeInsn(NEW, TYPE_ID_SET);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, TYPE_ID_SET, JVM_INIT_METHOD, "()V", false);

        for (BTypeIdSet.BTypeId typeId : typeIdSet.getPrimary()) {
            addTypeId(mv, typeId, true);
        }

        for (BTypeIdSet.BTypeId typeId : typeIdSet.getSecondary()) {
            addTypeId(mv, typeId, false);
        }
    }

    private void addTypeId(MethodVisitor mv, BTypeIdSet.BTypeId typeId, boolean isPrimaryTypeId) {
        mv.visitInsn(DUP);
        // Load package
        String varName = jvmConstantsGen.getModuleConstantVar(typeId.packageID);
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(), varName, JvmSignatures.GET_MODULE);

        mv.visitLdcInsn(typeId.name);
        mv.visitInsn(isPrimaryTypeId ? ICONST_1 : ICONST_0);
        // Add to BTypeIdSet
        mv.visitMethodInsn(INVOKEVIRTUAL, TYPE_ID_SET, "add",
                           ADD_TYPE_ID, false);
    }

    public static List<Label> createLabelsForSwitch(MethodVisitor mv, int nameRegIndex,
                                                    List<? extends NamedNode> nodes, int start,
                                                    int length, Label defaultCaseLabel) {
        return createLabelsForSwitch(mv, nameRegIndex, nodes, start, length, defaultCaseLabel, true);
    }

    public static List<Label> createLabelsForSwitch(MethodVisitor mv, int nameRegIndex,
                                                    List<? extends NamedNode> nodes, int start,
                                                    int length, Label defaultCaseLabel, boolean decodeCase) {
        mv.visitVarInsn(ALOAD, nameRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "hashCode", "()I", false);

        // Create labels for the cases
        int i = 0;
        List<Label> labels = new ArrayList<>();
        int[] hashCodes = new int[length];
        for (int j = start; j < (start + length); j++) {
            NamedNode node = nodes.get(j);
            if (node != null) {
                labels.add(i, new Label());
                String name;
                if (decodeCase) {
                    name = decodeIdentifier(node.getName().value);
                } else {
                    name = node.getName().value;
                }
                hashCodes[i] = name.hashCode();
                i += 1;
            }
        }
        mv.visitLookupSwitchInsn(defaultCaseLabel, hashCodes, labels.toArray(new Label[0]));
        return labels;
    }

    public static List<Label> createLabelsForEqualCheck(MethodVisitor mv, int nameRegIndex,
                                                        List<? extends NamedNode> nodes, int start, int length,
                                                        List<Label> labels, Label defaultCaseLabel) {
        return createLabelsForEqualCheck(mv,  nameRegIndex, nodes,  start,  length, labels,  defaultCaseLabel, true);
    }

   public static List<Label> createLabelsForEqualCheck(MethodVisitor mv, int nameRegIndex,
                                                       List<? extends NamedNode> nodes, int start, int length,
                                                       List<Label> labels, Label defaultCaseLabel, boolean decodeCase) {
        List<Label> targetLabels = new ArrayList<>();
        int i = 0;
        for (int j = start; j < start + length; j++) {
            NamedNode node = nodes.get(j);
            if (node == null) {
                continue;
            }
            mv.visitLabel(labels.get(i));
            mv.visitVarInsn(ALOAD, nameRegIndex);
            if (decodeCase) {
                mv.visitLdcInsn(decodeIdentifier(node.getName().value));
            } else {
                mv.visitLdcInsn(node.getName().value);
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "equals",
                    ANY_TO_JBOOLEAN, false);
            Label targetLabel = new Label();
            mv.visitJumpInsn(IFNE, targetLabel);
            mv.visitJumpInsn(GOTO, defaultCaseLabel);
            targetLabels.add(i, targetLabel);
            i += 1;
        }
        return targetLabels;
    }


    // -------------------------------------------------------
    //              getAnonType() generation methods
    // -------------------------------------------------------

    public void generateAnonTypeClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                      String moduleInitClass, Map<String, byte[]> jarEntries) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, anonTypesClass, null, OBJECT, null);
        generateGetAnonTypeMainMethod(cw, module.typeDefs, moduleInitClass);
        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(anonTypesClass + ".class", bytes);
    }

    private void generateGetAnonTypeMainMethod(ClassWriter cw, List<BIRTypeDefinition> typeDefinitions,
                                               String moduleInitClass) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, GET_ANON_TYPE_METHOD,
                JvmSignatures.GET_ANON_TYPE, null, null);
        mv.visitCode();
        // filter anon types and sorts them before generating switch case.
        Set<BIRTypeDefinition> typeDefSet = new TreeSet<>(typeDefHashComparator);
        for (BIRTypeDefinition t : typeDefinitions) {
            if (t.internalName.value.contains(BLangAnonymousModelHelper.ANON_PREFIX)
                    || Symbols.isFlagOn(t.type.flags, Flags.ANONYMOUS)) {
                typeDefSet.add(t);
            }
        }
        AnonTypeHashInfo anonTypeHashSwitch = createLabelsForAnonTypeHashSwitch(typeDefSet);
        if (anonTypeHashSwitch.labelFieldMapping.isEmpty()) {
            Label defaultCaseLabel = new Label();
            createDefaultCase(mv, defaultCaseLabel, 1, "No such type: ");
        } else {
            mv.visitVarInsn(ILOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, anonTypesClass, GET_ANON_TYPE_METHOD + 0,
                    JvmSignatures.GET_ANON_TYPE, false);
            mv.visitInsn(ARETURN);
            generateGetAnonTypeSplitMethods(cw, anonTypeHashSwitch, moduleInitClass);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    void generateGetAnonTypeSplitMethods(ClassWriter cw,  AnonTypeHashInfo anonTypeHashSwitch,
                                        String typeOwnerClass) {

        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;

        int hashParamRegIndex = 0;
        int shapeParamRegIndex = 1;
        Label defaultCaseLabel = new Label();
        Map<String, Label> labelFieldMapping = anonTypeHashSwitch.labelFieldMapping;
        int i = 0;
        for (Map.Entry<String, Label> labelEntry : labelFieldMapping.entrySet()) {
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, GET_ANON_TYPE_METHOD + methodCount++,
                        JvmSignatures.GET_ANON_TYPE, null, null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                mv.visitVarInsn(ILOAD, hashParamRegIndex);
                int remainingCases = labelFieldMapping.size() - bTypesCount;
                if (remainingCases > MAX_TYPES_PER_METHOD) {
                    remainingCases = MAX_TYPES_PER_METHOD;
                }
                int[] hashes = Arrays.copyOfRange(anonTypeHashSwitch.hashes, bTypesCount, bTypesCount + remainingCases);
                Label[] labels = Arrays.copyOfRange(anonTypeHashSwitch.labels, bTypesCount,
                        bTypesCount + remainingCases);
                mv.visitLookupSwitchInsn(defaultCaseLabel, hashes, labels);
            }
            mv.visitVarInsn(ILOAD, hashParamRegIndex);
            String fieldName = labelEntry.getKey();
            Label targetLabel = labelEntry.getValue();
            mv.visitLabel(targetLabel);
            mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, JvmSignatures.GET_TYPE);
            mv.visitInsn(ARETURN);
            i++;
            bTypesCount++;
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                if (bTypesCount == labelFieldMapping.size()) {
                    createDefaultCase(mv, defaultCaseLabel, shapeParamRegIndex, "No such type: ");
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ILOAD, hashParamRegIndex);
                    mv.visitVarInsn(ALOAD, shapeParamRegIndex);
                    mv.visitMethodInsn(INVOKESTATIC, anonTypesClass, GET_ANON_TYPE_METHOD + methodCount,
                            JvmSignatures.GET_ANON_TYPE, false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + VISIT_MAX_SAFE_MARGIN, i + VISIT_MAX_SAFE_MARGIN);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_TYPES_PER_METHOD != 0) {
            createDefaultCase(mv, defaultCaseLabel, shapeParamRegIndex, "No such type: ");
            mv.visitMaxs(i + VISIT_MAX_SAFE_MARGIN, i + VISIT_MAX_SAFE_MARGIN);
            mv.visitEnd();
        }
    }

    private AnonTypeHashInfo createLabelsForAnonTypeHashSwitch(Set<BIRTypeDefinition> nodes) {

        // Create labels for the cases
        Map<String, Label> labelFieldMapping = new LinkedHashMap<>();
        Map<Integer, Label> labelHashMapping = new LinkedHashMap<>();
        for (BIRTypeDefinition node : nodes) {
            if (node != null) {
                BType type = node.type;
                String fieldName = getTypeFieldName(node.internalName.value);
                Integer typeHash = typeHashVisitor.visit(type);
                boolean fieldExists = labelFieldMapping.containsKey(fieldName);
                boolean hashExists = labelHashMapping.containsKey(typeHash);
                if (!fieldExists && !hashExists) {
                    Label label = new Label();
                    labelFieldMapping.put(fieldName, label);
                    labelHashMapping.put(typeHash, label);
                } else {
                    assert fieldExists && hashExists; // hashing issues.
                }
                typeHashVisitor.reset();
            }
        }
        int[] hashes = new int[10];
        int count = 0;
        for (Integer integer : labelHashMapping.keySet()) {
            int intValue = integer.intValue();
            if (hashes.length == count) {
                hashes = Arrays.copyOf(hashes, count * 2);
            }
            hashes[count++] = intValue;
        }
        hashes = Arrays.copyOfRange(hashes, 0, count);
        Label[] labels = labelHashMapping.values().toArray(new Label[0]);
        return new AnonTypeHashInfo(hashes, labels, labelFieldMapping);
    }

    public void generateRefTypeConstants(List<BIRTypeDefinition> typeDefs, SymbolTable symbolTable) {
        for (BIRTypeDefinition typeDef : typeDefs) {
            if (typeDef.referenceType != null) {
                jvmConstantsGen.getTypeConstantsVar(typeDef.referenceType, symbolTable);
            }
        }
    }

    static class AnonTypeHashInfo {

        int[] hashes;
        Label[] labels;
        Map<String, Label> labelFieldMapping;

        public AnonTypeHashInfo(int[] hashes, Label[] labels, Map<String, Label> labelFieldMapping) {
            this.hashes = hashes;
            this.labels = labels;
            this.labelFieldMapping = labelFieldMapping;
        }
    }

    public void splitAddFields(ClassWriter cw, String typeClassName, String methodName, Map<String, BField> fields) {
        int fieldMapIndex = 0;
        MethodVisitor mv = null;
        int methodCount = 0;
        int fieldsCount = 0;
        String addFieldMethod = methodName + "$addField$";
        for (BField optionalField : fields.values()) {
            if (fieldsCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                mv = cw.visitMethod(ACC_STATIC + ACC_PRIVATE, addFieldMethod, SET_LINKED_HASH_MAP, null, null);
                mv.visitCode();
                addFieldMethod = methodName + "$addField$" + ++methodCount;
            }
            mv.visitVarInsn(ALOAD, fieldMapIndex);

            // Load field name
            mv.visitLdcInsn(decodeIdentifier(optionalField.name.value));

            // create and load field type
            createField(mv, optionalField);

            // Add the field to the map
            mv.visitMethodInsn(INVOKEINTERFACE, MAP, "put", MAP_PUT,
                    true);

            // emit a pop, since we are not using the return value from the map.put()
            mv.visitInsn(POP);
            fieldsCount++;
            if (fieldsCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                if (fieldsCount != fields.size()) {
                    mv.visitVarInsn(ALOAD, fieldMapIndex);
                    mv.visitMethodInsn(INVOKESTATIC, typeClassName, addFieldMethod, SET_LINKED_HASH_MAP, false);
                }
                mv.visitInsn(RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && fieldsCount % MAX_FIELDS_PER_SPLIT_METHOD != 0) {
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    /**
     * Create a field information for objects and records.
     *
     * @param mv    method visitor
     * @param field field parameter description
     */
    private void createField(MethodVisitor mv, BField field) {

        mv.visitTypeInsn(NEW, FIELD_IMPL);
        mv.visitInsn(DUP);

        // Load the field type
        jvmTypeGen.loadType(mv, field.symbol.type);

        // Load field name
        mv.visitLdcInsn(decodeIdentifier(field.name.value));

        // Load flags
        mv.visitLdcInsn(field.symbol.flags);

        mv.visitMethodInsn(INVOKESPECIAL, FIELD_IMPL, JVM_INIT_METHOD, INIT_FIELD_IMPL, false);
    }

    public JvmUnionTypeGen getJvmUnionTypeGen() {
        return jvmUnionTypeGen;
    }

    public JvmErrorTypeGen getJvmErrorTypeGen() {
        return jvmErrorTypeGen;
    }

    public JvmTupleTypeGen getJvmTupleTypeGen() {
        return jvmTupleTypeGen;
    }

    public JvmArrayTypeGen getJvmArrayTypeGen() {
        return jvmArrayTypeGen;
    }

    public JvmRefTypeGen getJvmRefTypeGen() {
        return jvmRefTypeGen;
    }

    public JvmTypeGen getJvmTypeGen() {
        return jvmTypeGen;
    }
}
