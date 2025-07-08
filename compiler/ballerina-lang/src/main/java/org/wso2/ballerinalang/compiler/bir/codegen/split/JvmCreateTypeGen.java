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

import io.ballerina.identifier.Utils;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmErrorGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTerminatorGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LabelGenerator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadBirBasicBlock;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadingDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.TypeDefHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmArrayTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmErrorTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmObjectTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmRecordTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmRefTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmTupleTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.split.types.JvmUnionTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeHashVisitor;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.NamedNode;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Arrays;
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
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
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
import static org.objectweb.asm.Opcodes.V21;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ADD_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ALL_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_MAP_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FIELD_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_ANON_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_FUNCTION_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOAD_ANNOTATIONS_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_FIELDS_PER_SPLIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_TYPES_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ANON_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_FUNCTION_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_IMMUTABLE_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_ID_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_INIT_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VISIT_MAX_SAFE_MARGIN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ADD_TYPE_ID;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ANY_TO_JBOOLEAN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ERROR_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUNCTION_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUNCTION_TYPE_FOR_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_JBOOLEAN_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_OBJECT_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_RECORD_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TUPLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_UNION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_FIELD_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.MAP_PUT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.PROCESS_ANNOTATIONS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_IMMUTABLE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.createDefaultCase;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.getVarStoreClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.skipRecordDefaultValueFunctions;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.addField;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.generateConstantsClassInit;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;

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
    private final String anonTypesClass;
    private final String annotationVarClassName;
    private final String functionTypesClass;
    private final String allTypesVarClassName;

    public JvmCreateTypeGen(JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen, PackageID packageID,
                            TypeHashVisitor typeHashVisitor) {
        this.jvmTypeGen = jvmTypeGen;
        this.jvmConstantsGen = jvmConstantsGen;
        this.jvmRecordTypeGen = new JvmRecordTypeGen(this, jvmTypeGen, jvmConstantsGen, packageID);
        this.jvmObjectTypeGen = new JvmObjectTypeGen(this, jvmTypeGen, jvmConstantsGen);
        this.jvmErrorTypeGen = new JvmErrorTypeGen(this, jvmTypeGen, jvmConstantsGen);
        this.jvmUnionTypeGen = new JvmUnionTypeGen(this, jvmTypeGen, jvmConstantsGen);
        this.jvmTupleTypeGen = new JvmTupleTypeGen(this, jvmTypeGen, jvmConstantsGen);
        this.jvmRefTypeGen = new JvmRefTypeGen(this, jvmTypeGen, jvmConstantsGen);
        this.jvmArrayTypeGen = new JvmArrayTypeGen(jvmTypeGen);
        this.typeHashVisitor =  typeHashVisitor;
        this.typeDefHashComparator = new TypeDefHashComparator(typeHashVisitor);
        this.allTypesVarClassName =  getModuleLevelClassName(packageID, ALL_TYPES_CLASS_NAME);
        this.anonTypesClass = getModuleLevelClassName(packageID, MODULE_ANON_TYPES_CLASS_NAME);
        this.functionTypesClass = getModuleLevelClassName(packageID, MODULE_FUNCTION_TYPES_CLASS_NAME);
        this.annotationVarClassName = getVarStoreClass(jvmConstantsGen.globalVarsPkgName , ANNOTATION_MAP_NAME);
    }

    public void createTypes(BIRNode.BIRPackage module, JarEntries jarEntries, JvmPackageGen jvmPackageGen,
                            JvmCastGen jvmCastGen, AsyncDataCollector asyncDataCollector,
                            LazyLoadingDataCollector lazyLoadingDataCollector) {
        ClassWriter allTypesCW = new BallerinaClassWriter(COMPUTE_FRAMES);
        allTypesCW.visit(V21, ACC_PUBLIC | ACC_SUPER, allTypesVarClassName, null, OBJECT, null);
        // Create the type
        for (BIRTypeDefinition typeDef : module.typeDefs) {
            BType bType = typeDef.type;
            int bTypeTag = bType.tag;
            switch (bTypeTag) {
                case TypeTags.RECORD ->    createRecordType(typeDef, allTypesCW, module, jvmPackageGen, jvmCastGen,
                        asyncDataCollector, lazyLoadingDataCollector, jarEntries);
                case TypeTags.OBJECT -> createObjectType(typeDef, allTypesCW, jvmPackageGen, jvmCastGen,
                        asyncDataCollector, lazyLoadingDataCollector, jarEntries);
                case TypeTags.ERROR -> createErrorType(typeDef, allTypesCW, jvmPackageGen, jvmCastGen,
                        asyncDataCollector, lazyLoadingDataCollector, jarEntries);
                case TypeTags.TUPLE -> createTupleType(typeDef, allTypesCW, jvmPackageGen, jvmCastGen,
                        asyncDataCollector, lazyLoadingDataCollector, jarEntries);
                default -> createUnionType(typeDef, allTypesCW, jvmPackageGen, jvmCastGen,
                        asyncDataCollector, lazyLoadingDataCollector, jarEntries);
            }
        }
        allTypesCW.visitEnd();
        String typesClass = allTypesVarClassName + CLASS_FILE_SUFFIX;
        jarEntries.put(typesClass, jvmPackageGen.getBytes(allTypesCW, module));
    }

    private void createRecordType(BIRTypeDefinition typeDef, ClassWriter allTypesCW, BIRNode.BIRPackage module,
                                  JvmPackageGen jvmPackageGen, JvmCastGen jvmCastGen,
                                  AsyncDataCollector asyncDataCollector,
                                  LazyLoadingDataCollector lazyLoadingDataCollector, JarEntries jarEntries) {
        BType bType = typeDef.type;
        int bTypeTag = bType.tag;
        if (JvmCodeGenUtil.needNoTypeGeneration(bTypeTag)) {
            // do not generate anything for other types (e.g.: finite type, type reference types etc.)
            return;
        }
        String varName = typeDef.internalName.value;
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        String typeClass =  jvmTypeGen.recordTypesPkgName + typeDef.internalName.value;
        generateConstantsClassInit(cw, typeClass);
        boolean isAnnotatedType = false;
        if (!typeDef.isBuiltin || typeDef.referenceType == null || bTypeTag == TypeTags.RECORD) {
            // Annotations for object constructors are populated at object init site.
            boolean constructorsPopulated = Symbols.isFlagOn(bType.getFlags(), Flags.OBJECT_CTOR);
            if (!constructorsPopulated) {
                loadAnnotations(cw, varName, typeClass, GET_RECORD_TYPE_IMPL, jvmPackageGen, jvmCastGen,
                        asyncDataCollector, lazyLoadingDataCollector);
                isAnnotatedType = true;
            }
        }
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        setTypeInitialized(mv, ICONST_1, typeClass, isAnnotatedType);
        jvmRecordTypeGen.createRecordType(cw, mv, module, typeClass, (BRecordType) bType, varName, isAnnotatedType,
                jarEntries, jvmPackageGen.symbolTable);
        setTypeInitialized(mv, ICONST_0, typeClass, isAnnotatedType);

        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(typeClass + CLASS_FILE_SUFFIX, cw.toByteArray());
        addField(allTypesCW, varName);
    }

    public static void setTypeInitialized(MethodVisitor mv, int status, String typeClass, boolean isAnnotatedType) {
        if (!isAnnotatedType) {
            return;
        }
        mv.visitInsn(status);
        mv.visitFieldInsn(PUTSTATIC, typeClass, TYPE_INIT_VAR_NAME, GET_JBOOLEAN_TYPE);
    }

    private void createObjectType(BIRTypeDefinition typeDef, ClassWriter allTypesCW, JvmPackageGen jvmPackageGen,
                                  JvmCastGen jvmCastGen, AsyncDataCollector asyncDataCollector,
                                  LazyLoadingDataCollector lazyLoadingDataCollector, JarEntries jarEntries) {
        BType bType = typeDef.type;
        int bTypeTag = bType.tag;
        if (JvmCodeGenUtil.needNoTypeGeneration(bTypeTag)) {
            // do not generate anything for other types (e.g.: finite type, type reference types etc.)
            return;
        }
        String varName = typeDef.internalName.value;
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        String typeClass =  jvmTypeGen.objectTypesPkgName + typeDef.internalName.value;
        generateConstantsClassInit(cw, typeClass);
        boolean isAnnotatedType = false;
        if (!typeDef.isBuiltin || typeDef.referenceType == null || bTypeTag == TypeTags.RECORD) {
            // Annotations for object constructors are populated at object init site.
            boolean constructorsPopulated = Symbols.isFlagOn(bType.getFlags(), Flags.OBJECT_CTOR);
            if (!constructorsPopulated) {
                loadAnnotations(cw, varName, typeClass, GET_OBJECT_TYPE_IMPL, jvmPackageGen, jvmCastGen,
                        asyncDataCollector, lazyLoadingDataCollector);
                isAnnotatedType = true;
            }
        }
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        setTypeInitialized(mv, ICONST_1, typeClass, isAnnotatedType);
        jvmObjectTypeGen.createObjectType(cw, mv, typeClass, (BObjectType) bType, varName, isAnnotatedType,
                new BIRVarToJVMIndexMap(), jvmPackageGen.symbolTable);
        setTypeInitialized(mv, ICONST_0, typeClass, isAnnotatedType);
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(typeClass + CLASS_FILE_SUFFIX, cw.toByteArray());
        addField(allTypesCW, varName);
    }

    private void createErrorType(BIRTypeDefinition typeDef, ClassWriter allTypesCW, JvmPackageGen jvmPackageGen,
                                 JvmCastGen jvmCastGen, AsyncDataCollector asyncDataCollector,
                                 LazyLoadingDataCollector lazyLoadingDataCollector, JarEntries jarEntries) {
        BType bType = typeDef.type;
        int bTypeTag = bType.tag;
        if (JvmCodeGenUtil.needNoTypeGeneration(bTypeTag)) {
            // do not generate anything for other types (e.g.: finite type, type reference types etc.)
            return;
        }
        String varName = typeDef.internalName.value;
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        String typeClass =  jvmTypeGen.errorTypesPkgName + typeDef.internalName.value;
        generateConstantsClassInit(cw, typeClass);
        boolean isAnnotatedType = false;
        if (!typeDef.isBuiltin || typeDef.referenceType == null || bTypeTag == TypeTags.RECORD) {
            // Annotations for object constructors are populated at object init site.
            boolean constructorsPopulated = Symbols.isFlagOn(bType.getFlags(), Flags.OBJECT_CTOR);
            if (!constructorsPopulated) {
                loadAnnotations(cw, varName, typeClass, GET_ERROR_TYPE_IMPL, jvmPackageGen, jvmCastGen,
                        asyncDataCollector, lazyLoadingDataCollector);
                isAnnotatedType = true;
            }
        }
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        setTypeInitialized(mv, ICONST_1, typeClass, isAnnotatedType);
        jvmErrorTypeGen.createErrorType(cw, mv, (BErrorType) bType, typeClass, isAnnotatedType);
        setTypeInitialized(mv, ICONST_0, typeClass, isAnnotatedType);
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(typeClass + CLASS_FILE_SUFFIX, cw.toByteArray());
        addField(allTypesCW, varName);
    }


    private void createTupleType(BIRTypeDefinition typeDef, ClassWriter allTypesCW, JvmPackageGen jvmPackageGen,
                                 JvmCastGen jvmCastGen, AsyncDataCollector asyncDataCollector,
                                 LazyLoadingDataCollector lazyLoadingDataCollector, JarEntries jarEntries) {
        BType bType = typeDef.type;
        int bTypeTag = bType.tag;
        if (JvmCodeGenUtil.needNoTypeGeneration(bTypeTag)) {
            // do not generate anything for other types (e.g.: finite type, type reference types etc.)
            return;
        }
        String varName = typeDef.internalName.value;
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        String typeClass =  jvmTypeGen.tupleTypesPkgName + typeDef.internalName.value;
        generateConstantsClassInit(cw, typeClass);
        boolean isAnnotatedType = false;
        if (!typeDef.isBuiltin || typeDef.referenceType == null || bTypeTag == TypeTags.RECORD) {
            // Annotations for object constructors are populated at object init site.
            boolean constructorsPopulated = Symbols.isFlagOn(bType.getFlags(), Flags.OBJECT_CTOR);
            if (!constructorsPopulated) {
                loadAnnotations(cw, varName, typeClass, GET_TUPLE_TYPE_IMPL, jvmPackageGen, jvmCastGen,
                        asyncDataCollector, lazyLoadingDataCollector);
                isAnnotatedType = true;
            }
        }
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        setTypeInitialized(mv, ICONST_1, typeClass, isAnnotatedType);
        jvmTupleTypeGen.createTupleType(cw, mv, typeClass, (BTupleType) bType, isAnnotatedType,
                jvmPackageGen.symbolTable, ACC_PRIVATE);
        setTypeInitialized(mv, ICONST_0, typeClass, isAnnotatedType);
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(typeClass + CLASS_FILE_SUFFIX, cw.toByteArray());
        addField(allTypesCW, varName);
    }

    private void createUnionType(BIRTypeDefinition typeDef, ClassWriter allTypesCW, JvmPackageGen jvmPackageGen,
                                 JvmCastGen jvmCastGen, AsyncDataCollector asyncDataCollector,
                                 LazyLoadingDataCollector lazyLoadingDataCollector, JarEntries jarEntries) {
        BType bType = typeDef.type;
        int bTypeTag = bType.tag;
        if (JvmCodeGenUtil.needNoTypeGeneration(bTypeTag)) {
            // do not generate anything for other types (e.g.: finite type, type reference types etc.)
            return;
        }
        String varName = typeDef.internalName.value;
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        String typeClass =  jvmTypeGen.unionTypesPkgName + typeDef.internalName.value;
        generateConstantsClassInit(cw, typeClass);
        boolean isAnnotatedType = false;
        if (!typeDef.isBuiltin || typeDef.referenceType == null || bTypeTag == TypeTags.RECORD) {
            // Annotations for object constructors are populated at object init site.
            boolean constructorsPopulated = Symbols.isFlagOn(bType.getFlags(), Flags.OBJECT_CTOR);
            if (!constructorsPopulated) {
                loadAnnotations(cw, varName, typeClass, GET_UNION_TYPE_IMPL, jvmPackageGen, jvmCastGen,
                        asyncDataCollector, lazyLoadingDataCollector);
                isAnnotatedType = true;
            }
        }
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        setTypeInitialized(mv, ICONST_1, typeClass, isAnnotatedType);
        jvmUnionTypeGen.createUnionType(cw, mv, typeClass, varName, (BUnionType) bType, isAnnotatedType,
                jvmPackageGen.symbolTable, ACC_PRIVATE);
        setTypeInitialized(mv, ICONST_0, typeClass, isAnnotatedType);
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(typeClass + CLASS_FILE_SUFFIX, cw.toByteArray());
        addField(allTypesCW, varName);
    }

    public void genGetTypeMethod(ClassWriter cw, String typeClass, String methodDescriptor, String typeDescriptor,
                                 boolean isAnnotatedType) {
        FieldVisitor f = cw.visitField(ACC_STATIC + ACC_PRIVATE, TYPE_INIT_VAR_NAME, GET_JBOOLEAN_TYPE, null, null);
        f.visitEnd();
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, GET_TYPE_METHOD, methodDescriptor, null, null);
        mv.visitCode();
        if (isAnnotatedType) {
            mv.visitFieldInsn(GETSTATIC, typeClass, TYPE_INIT_VAR_NAME, GET_JBOOLEAN_TYPE);
            Label ifLabel = new Label();
            mv.visitJumpInsn(IFNE, ifLabel);
            setTypeInitialized(mv, ICONST_1, typeClass, isAnnotatedType);
            mv.visitMethodInsn(INVOKESTATIC, typeClass, LOAD_ANNOTATIONS_METHOD, VOID_METHOD_DESC, false);
            mv.visitLabel(ifLabel);
        }
        mv.visitFieldInsn(GETSTATIC, typeClass, TYPE_VAR_NAME, typeDescriptor);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
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
        mv.visitMethodInsn(INVOKESPECIAL, TYPE_ID_SET, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
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
        String moduleVar = jvmConstantsGen.getModuleConstantVar(typeId.packageID);
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(moduleVar), moduleVar, GET_MODULE);

        mv.visitLdcInsn(typeId.name);
        mv.visitInsn(isPrimaryTypeId ? ICONST_1 : ICONST_0);
        // Add to BTypeIdSet
        mv.visitMethodInsn(INVOKEVIRTUAL, TYPE_ID_SET, ADD_METHOD,
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
                                                        List<Label> labels, Label defaultCaseLabel,
                                                        boolean decodeCase) {
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
    //              Annotation processing related methods
    // -------------------------------------------------------

    public void loadAnnotations(ClassWriter cw, String typeName, String typeClass, String descriptor,
                                JvmPackageGen jvmPackageGen, JvmCastGen jvmCastGen,
                                AsyncDataCollector asyncDataCollector,
                                LazyLoadingDataCollector lazyLoadingDataCollector) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "loadAnnotations", "()V", null, null);
        mv.visitCode();
        LazyLoadBirBasicBlock lazyBB = lazyLoadingDataCollector.lazyLoadingAnnotationsBBMap.get(typeName);
        if (lazyBB != null) {
            BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
            PackageID packageID = jvmPackageGen.currentModule.packageID;
            JvmInstructionGen instructionGen = new JvmInstructionGen(mv, indexMap, packageID, jvmPackageGen, jvmTypeGen,
                    jvmCastGen, jvmConstantsGen, asyncDataCollector);
            JvmErrorGen errorGen = new JvmErrorGen(mv, indexMap, instructionGen);
            LabelGenerator labelGen = new LabelGenerator();
            JvmTerminatorGen termGen = new JvmTerminatorGen(mv, indexMap, labelGen, errorGen, packageID,
                    instructionGen, jvmPackageGen, jvmTypeGen, jvmCastGen, asyncDataCollector);
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ASTORE, 1);
            BIRTerminator.Call call = lazyBB.call();
            termGen.genCall(call, call.calleePkg, 1);
            termGen.storeReturnFromCallIns(call.lhsOp != null ? call.lhsOp.variableDcl : null);
            for (BIRNonTerminator instruction : lazyBB.instructions()) {
                instructionGen.generateInstructions(0, instruction);
            }
        }
        mv.visitFieldInsn(GETSTATIC, this.annotationVarClassName, VALUE_VAR_NAME, GET_MAP_VALUE);
        mv.visitFieldInsn(GETSTATIC, typeClass, TYPE_VAR_NAME, descriptor);
        mv.visitMethodInsn(INVOKESTATIC, ANNOTATION_UTILS, "processAnnotations", PROCESS_ANNOTATIONS, false);
        genMethodReturn(mv);
    }

    // -------------------------------------------------------
    //              getAnonType() generation methods
    // -------------------------------------------------------

    public void generateAnonTypeClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module, JarEntries jarEntries) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V21, ACC_PUBLIC + ACC_SUPER, anonTypesClass, null, OBJECT, null);
        generateGetAnonTypeMainMethod(cw, module.typeDefs);
        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(anonTypesClass + CLASS_FILE_SUFFIX, bytes);
    }

    private void generateGetAnonTypeMainMethod(ClassWriter cw, List<BIRTypeDefinition> typeDefinitions) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, GET_ANON_TYPE_METHOD,
                JvmSignatures.GET_ANON_TYPE, null, null);
        mv.visitCode();
        // filter anon types and sorts them before generating switch case.
        Set<BIRTypeDefinition> typeDefSet = new TreeSet<>(typeDefHashComparator);
        for (BIRTypeDefinition t : typeDefinitions) {
            if (Symbols.isFlagOn(t.type.getFlags(), Flags.ANONYMOUS)) {
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
            mv.visitMethodInsn(INVOKESTATIC, anonTypesClass, GET_ANON_TYPE_METHOD + 0, JvmSignatures.GET_ANON_TYPE,
                    false);
            mv.visitInsn(ARETURN);
            generateGetAnonTypeSplitMethods(cw, anonTypeHashSwitch);
        }
        JvmCodeGenUtil.visitMaxStackForMethod(mv, GET_ANON_TYPE_METHOD, anonTypesClass);
        mv.visitEnd();
    }

    void generateGetAnonTypeSplitMethods(ClassWriter cw,  AnonTypeHashInfo anonTypeHashSwitch) {
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
            BType type = anonTypeHashSwitch.nameTypeMapping.get(fieldName);
            jvmTypeGen.getUserDefinedType(mv, type);
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
        Map<String, BType> nameTypeMapping = new LinkedHashMap<>();
        for (BIRTypeDefinition node : nodes) {
            if (node != null) {
                BType type = node.type;
                String fieldName = node.internalName.value;
                Integer typeHash = typeHashVisitor.visit(type);
                boolean fieldExists = labelFieldMapping.containsKey(fieldName);
                boolean hashExists = labelHashMapping.containsKey(typeHash);
                if (!fieldExists && !hashExists) {
                    Label label = new Label();
                    labelFieldMapping.put(fieldName, label);
                    labelHashMapping.put(typeHash, label);
                    nameTypeMapping.put(fieldName, type);
                } else {
                    assert fieldExists && hashExists; // hashing issues.
                }
                typeHashVisitor.reset();
            }
        }
        int[] hashes = new int[10];
        int count = 0;
        for (Integer integer : labelHashMapping.keySet()) {
            int intValue = integer;
            if (hashes.length == count) {
                hashes = Arrays.copyOf(hashes, count * 2);
            }
            hashes[count++] = intValue;
        }
        hashes = Arrays.copyOfRange(hashes, 0, count);
        Label[] labels = labelHashMapping.values().toArray(new Label[0]);
        return new AnonTypeHashInfo(hashes, labels, labelFieldMapping, nameTypeMapping);
    }

    public void generateRefTypeConstants(List<BIRTypeDefinition> typeDefs, JvmPackageGen jvmPackageGen,
                                         JvmCastGen jvmCastGen, AsyncDataCollector asyncDataCollector,
                                         LazyLoadingDataCollector lazyLoadingDataCollector) {
        for (BIRTypeDefinition typeDef : typeDefs) {
            if (typeDef.referenceType != null) {
                jvmConstantsGen.getRefTypeConstantsVar(typeDef, jvmPackageGen, jvmCastGen, asyncDataCollector,
                        lazyLoadingDataCollector);
            }
        }
    }

    static class AnonTypeHashInfo {

        int[] hashes;
        Label[] labels;
        Map<String, Label> labelFieldMapping;
        Map<String, BType> nameTypeMapping;

        public AnonTypeHashInfo(int[] hashes, Label[] labels, Map<String, Label> labelFieldMapping,
                                Map<String, BType> nameTypeMapping) {
            this.hashes = hashes;
            this.labels = labels;
            this.labelFieldMapping = labelFieldMapping;
            this.nameTypeMapping = nameTypeMapping;
        }
    }

    // -------------------------------------------------------
    //              getFunctionType() generation methods
    // -------------------------------------------------------
    public void generateFunctionTypeClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                          JarEntries jarEntries, List<BIRNode.BIRFunction> sortedFunctions) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V21, ACC_PUBLIC + ACC_SUPER, functionTypesClass, null, OBJECT, null);
        generateGetFunctionTypeMainMethod(cw, sortedFunctions);
        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(functionTypesClass + ".class", bytes);
    }

    private void generateGetFunctionTypeMainMethod(ClassWriter cw, List<BIRNode.BIRFunction> functions) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, GET_FUNCTION_TYPE_METHOD,
                GET_FUNCTION_TYPE_FOR_STRING, null, null);
        mv.visitCode();
        if (functions.isEmpty()) {
            Label defaultCaseLabel = new Label();
            createDefaultCase(mv, defaultCaseLabel, 1, "No such function type: ");
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, functionTypesClass, GET_FUNCTION_TYPE_METHOD + 0,
                    GET_FUNCTION_TYPE_FOR_STRING, false);
            mv.visitInsn(ARETURN);
            generateGetFunctionTypeSplitMethods(cw, functions);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    void generateGetFunctionTypeSplitMethods(ClassWriter cw, List<BIRNode.BIRFunction> functions) {
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        int funcNameRegIndex = 0;
        Label defaultCaseLabel = new Label();
        // case body
        int i = 0;
        List<Label> targetLabels = new ArrayList<>();
        // Skip function types for record default value functions since they can be called directly from function
        // pointers instead of the function name
        List<BIRNode.BIRFunction> filteredFunctions = skipRecordDefaultValueFunctions(functions);
        for (BIRNode.BIRFunction func : filteredFunctions) {
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, GET_FUNCTION_TYPE_METHOD + methodCount++,
                        GET_FUNCTION_TYPE_FOR_STRING, null, null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                int remainingCases = filteredFunctions.size() - bTypesCount;
                if (remainingCases > MAX_TYPES_PER_METHOD) {
                    remainingCases = MAX_TYPES_PER_METHOD;
                }
                List<Label> labels = JvmCreateTypeGen.createLabelsForSwitch(mv, funcNameRegIndex, filteredFunctions,
                        bTypesCount, remainingCases, defaultCaseLabel, false);
                targetLabels = JvmCreateTypeGen.createLabelsForEqualCheck(mv, funcNameRegIndex, filteredFunctions,
                        bTypesCount, remainingCases, labels, defaultCaseLabel, false);
                i = 0;
            }
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            String varName = jvmConstantsGen.getFunctionTypeVar(func.name.value);
            String functionTypeConstantClass = jvmConstantsGen.getFunctionTypeConstantClass(varName);
            mv.visitFieldInsn(GETSTATIC, functionTypeConstantClass, varName, GET_FUNCTION_TYPE);
            mv.visitInsn(ARETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                if (bTypesCount == (filteredFunctions.size())) {
                    createDefaultCase(mv, defaultCaseLabel, funcNameRegIndex, "No such function type: ");
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKESTATIC, functionTypesClass, GET_FUNCTION_TYPE_METHOD + methodCount,
                            GET_FUNCTION_TYPE_FOR_STRING, false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + 10, i + 10);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_TYPES_PER_METHOD != 0) {
            createDefaultCase(mv, defaultCaseLabel, funcNameRegIndex, "No such function type: ");
            mv.visitMaxs(i + 10, i + 10);
            mv.visitEnd();
        }
    }

    public void splitAddFields(ClassWriter cw, BStructureType bType, String typeClassName) {
        int fieldMapIndex = 0;
        MethodVisitor mv = null;
        int methodCount = 0;
        int fieldsCount = 0;
        String addFieldMethod = "addFields";
        for (BField field : bType.fields.values()) {
            if (fieldsCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                mv = cw.visitMethod(ACC_STATIC + ACC_PRIVATE, addFieldMethod, SET_LINKED_HASH_MAP, null, null);
                mv.visitCode();
                addFieldMethod = "addFields$" + ++methodCount;
            }
            mv.visitVarInsn(ALOAD, fieldMapIndex);
            // Load field name
            mv.visitLdcInsn(decodeIdentifier(field.name.value));
            // create and load field type
            createField(mv, field);
            // Add the field to the map
            mv.visitMethodInsn(INVOKEINTERFACE, MAP, "put", MAP_PUT, true);
            // emit a pop, since we are not using the return value from the map.put()
            mv.visitInsn(POP);
            fieldsCount++;
            if (fieldsCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                if (fieldsCount != bType.fields.size()) {
                    mv.visitVarInsn(ALOAD, fieldMapIndex);
                    mv.visitMethodInsn(INVOKESTATIC, typeClassName, addFieldMethod, SET_LINKED_HASH_MAP, false);
                }
                mv.visitInsn(RETURN);
                JvmCodeGenUtil.visitMaxStackForMethod(mv, addFieldMethod, typeClassName);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && fieldsCount % MAX_FIELDS_PER_SPLIT_METHOD != 0) {
            mv.visitInsn(RETURN);
            JvmCodeGenUtil.visitMaxStackForMethod(mv, addFieldMethod, typeClassName);
            mv.visitEnd();
        }
    }

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

    public void splitAddDefaultValues(ClassWriter cw, String typeClassName, BRecordType recordType,
                                      Map<String, String> fieldNameFPNameMap) {
        int fieldMapIndex = 0;
        MethodVisitor mv = null;
        int methodCount = 0;
        int fieldsCount = 0;
        String addDefaultValueMethod = "addDefaultValues";
        Map<String, BField> fields = recordType.fields;
        for (Map.Entry<String, String> field : fieldNameFPNameMap.entrySet()) {
            if (fieldsCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                mv = cw.visitMethod(ACC_STATIC + ACC_PRIVATE, addDefaultValueMethod, SET_LINKED_HASH_MAP, null, null);
                mv.visitCode();
                addDefaultValueMethod = "addDefaultValue$" + ++methodCount;
            }
            mv.visitVarInsn(ALOAD, fieldMapIndex);
            // Load field name
            mv.visitLdcInsn(Utils.unescapeBallerina(field.getKey()));
            // load default value function pointer
            this.loadDefaultValueFp(mv, field.getValue());
            // Add the field to the map
            mv.visitMethodInsn(INVOKEINTERFACE, MAP, "put", MAP_PUT, true);
            // emit a pop, since we are not using the return value from the map.put()
            mv.visitInsn(POP);
            fieldsCount++;
            if (fieldsCount % MAX_FIELDS_PER_SPLIT_METHOD == 0) {
                if (fieldsCount != fields.size()) {
                    mv.visitVarInsn(ALOAD, fieldMapIndex);
                    mv.visitMethodInsn(INVOKESTATIC, typeClassName, addDefaultValueMethod, SET_LINKED_HASH_MAP, false);
                }
                mv.visitInsn(RETURN);
                JvmCodeGenUtil.visitMaxStackForMethod(mv, addDefaultValueMethod, typeClassName);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && fieldsCount % MAX_FIELDS_PER_SPLIT_METHOD != 0) {
            mv.visitInsn(RETURN);
            JvmCodeGenUtil.visitMaxStackForMethod(mv, addDefaultValueMethod, typeClassName);
            mv.visitEnd();
        }
    }

    private void loadDefaultValueFp(MethodVisitor mv, String functionName) {
        String varClass = getVarStoreClass(jvmConstantsGen.globalVarsPkgName, functionName);
        mv.visitFieldInsn(GETSTATIC, varClass, VALUE_VAR_NAME, GET_FUNCTION_POINTER);
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
