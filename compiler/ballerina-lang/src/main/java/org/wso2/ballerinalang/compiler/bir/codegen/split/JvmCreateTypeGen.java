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
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.BallerinaClassWriter;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarEntries;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadBirBasicBlock;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadingDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.TypeDefHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.model.DoubleCheckLabelsRecord;
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
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeHashVisitor;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
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
import static org.objectweb.asm.Opcodes.ACC_VOLATILE;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_RUNTIME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FIELD_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_ANON_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_FUNCTION_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_RECORD_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LOAD_ANNOTATIONS_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_FIELDS_PER_SPLIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_TYPES_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ANON_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_FUNCTION_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_RECORD_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REENTRANT_LOCK;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_ID_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_INITIALIZING_GLOBAL_LOCK_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_INIT_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_INIT_ON_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_VAR_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_VAR_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VISIT_MAX_SAFE_MARGIN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ADD_TYPE_ID;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ANY_TO_JBOOLEAN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ERROR_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUNCTION_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUNCTION_TYPE_FOR_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_JBOOLEAN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_OBJECT_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_RECORD_TYPE_FOR_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_RECORD_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TUPLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_UNION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_FIELD_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_LOCK;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.MAP_PUT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.PROCESS_ANNOTATIONS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_JBOOLEAN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.createDefaultCaseReturnNull;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.createDefaultCaseThrowError;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.getVarStoreClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.lazyLoadAnnotations;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmConstantGenUtils.generateConstantsClassInit;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.LazyLoadingCodeGenUtils.addDebugField;

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
    private final String recordTypesClass;
    private final String functionTypesClass;
    private final String allTypesVarClassName;
    private final String annotationVarClassName;

    public JvmCreateTypeGen(JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen, BIRNode.BIRPackage module,
                            TypeHashVisitor typeHashVisitor) {
        this.jvmTypeGen = jvmTypeGen;
        this.jvmConstantsGen = jvmConstantsGen;
        this.jvmRecordTypeGen = new JvmRecordTypeGen(this, jvmTypeGen, jvmConstantsGen);
        this.jvmObjectTypeGen = new JvmObjectTypeGen(this, jvmTypeGen, jvmConstantsGen);
        this.jvmErrorTypeGen = new JvmErrorTypeGen(this, jvmTypeGen, jvmConstantsGen);
        this.jvmUnionTypeGen = new JvmUnionTypeGen(this, jvmTypeGen, jvmConstantsGen);
        this.jvmTupleTypeGen = new JvmTupleTypeGen(this, jvmTypeGen, jvmConstantsGen);
        this.jvmRefTypeGen = new JvmRefTypeGen(this, jvmTypeGen, jvmConstantsGen);
        this.jvmArrayTypeGen = new JvmArrayTypeGen(jvmTypeGen);
        this.typeHashVisitor =  typeHashVisitor;
        this.typeDefHashComparator = new TypeDefHashComparator(typeHashVisitor);
        this.allTypesVarClassName =  getModuleLevelClassName(module.packageID, ALL_TYPES_CLASS_NAME);
        this.anonTypesClass = getModuleLevelClassName(module.packageID, MODULE_ANON_TYPES_CLASS_NAME);
        this.recordTypesClass = getModuleLevelClassName(module.packageID, MODULE_RECORD_TYPES_CLASS_NAME);
        this.functionTypesClass = getModuleLevelClassName(module.packageID, MODULE_FUNCTION_TYPES_CLASS_NAME);
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
                case TypeTags.RECORD -> createRecordType(typeDef, allTypesCW, module, jvmPackageGen, jvmCastGen,
                        asyncDataCollector, lazyLoadingDataCollector, jarEntries);
                case TypeTags.OBJECT -> createObjectType(typeDef, allTypesCW, jvmPackageGen, jvmCastGen,
                        asyncDataCollector, lazyLoadingDataCollector, jarEntries);
                case TypeTags.ERROR -> createErrorType(typeDef, allTypesCW, jvmPackageGen, jvmCastGen,
                        asyncDataCollector, lazyLoadingDataCollector, jarEntries);
                case TypeTags.TUPLE -> createTupleType(typeDef, allTypesCW, jvmPackageGen, jvmCastGen,
                        asyncDataCollector, lazyLoadingDataCollector, jarEntries);
                default -> createUnionType(typeDef, allTypesCW, jvmPackageGen, jvmCastGen, asyncDataCollector,
                        lazyLoadingDataCollector, jarEntries);
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
        genFieldsForInitFlags(cw);
        String typeClass =  jvmTypeGen.recordTypesPkgName + typeDef.internalName.value;
        generateConstantsClassInit(cw, typeClass);
        loadAnnotations(cw, varName, typeClass, GET_RECORD_TYPE_IMPL, jvmPackageGen, jvmCastGen,
                asyncDataCollector, lazyLoadingDataCollector);
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        mv.visitCode();
        setTypeInitialized(mv, ICONST_1, typeClass);
        jvmRecordTypeGen.createRecordType(cw, mv, module, typeClass, (BRecordType) bType, varName, true,
                jvmPackageGen.symbolTable);
        setTypeInitialized(mv, ICONST_0, typeClass);

        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(typeClass + CLASS_FILE_SUFFIX, cw.toByteArray());
        addDebugField(allTypesCW, varName);
    }

    private void createObjectType(BIRTypeDefinition typeDef, ClassWriter allTypesCW, JvmPackageGen jvmPackageGen,
                                  JvmCastGen jvmCastGen, AsyncDataCollector asyncDataCollector,
                                  LazyLoadingDataCollector lazyLoadingDataCollector, JarEntries jarEntries) {
        BObjectType bType = (BObjectType) typeDef.type;
        int bTypeTag = bType.tag;
        if (JvmCodeGenUtil.needNoTypeGeneration(bTypeTag)) {
            // do not generate anything for other types (e.g.: finite type, type reference types etc.)
            return;
        }
        String varName = typeDef.internalName.value;
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        genFieldsForInitFlags(cw);
        String typeClass =  jvmTypeGen.objectTypesPkgName + typeDef.internalName.value;
        generateConstantsClassInit(cw, typeClass);
        boolean isAnnotatedType = false;
        if (!typeDef.isBuiltin || typeDef.referenceType == null) {
            boolean constructorsPopulated = Symbols.isFlagOn(bType.getFlags(), Flags.OBJECT_CTOR);
            loadAnnotations(cw, bType, varName, typeClass, constructorsPopulated, jvmPackageGen,
                    jvmCastGen, asyncDataCollector, lazyLoadingDataCollector);
            isAnnotatedType = true;
        }
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        mv.visitCode();
        setTypeInitialized(mv, ICONST_1, typeClass);
        jvmObjectTypeGen.createObjectType(cw, mv, typeClass, bType, varName, isAnnotatedType,
                new BIRVarToJVMIndexMap(), jvmPackageGen.symbolTable);
        setTypeInitialized(mv, ICONST_0, typeClass);
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(typeClass + CLASS_FILE_SUFFIX, cw.toByteArray());
        addDebugField(allTypesCW, varName);
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
        genFieldsForInitFlags(cw);
        String typeClass =  jvmTypeGen.errorTypesPkgName + typeDef.internalName.value;
        generateConstantsClassInit(cw, typeClass);
        boolean isAnnotatedType = false;
        if (!typeDef.isBuiltin || typeDef.referenceType == null) {
            // Annotations for object constructors are populated at object init site.
            loadAnnotations(cw, varName, typeClass, GET_ERROR_TYPE_IMPL, jvmPackageGen, jvmCastGen,
                    asyncDataCollector, lazyLoadingDataCollector);
            isAnnotatedType = true;
        }
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        mv.visitCode();
        setTypeInitialized(mv, ICONST_1, typeClass);
        jvmErrorTypeGen.createErrorType(cw, mv, (BErrorType) bType, typeClass, isAnnotatedType);
        setTypeInitialized(mv, ICONST_0, typeClass);
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(typeClass + CLASS_FILE_SUFFIX, cw.toByteArray());
        addDebugField(allTypesCW, varName);
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
        genFieldsForInitFlags(cw);
        String typeClass =  jvmTypeGen.tupleTypesPkgName + typeDef.internalName.value;
        generateConstantsClassInit(cw, typeClass);
        boolean isAnnotatedType = false;
        if (!typeDef.isBuiltin || typeDef.referenceType == null) {
            // Annotations for object constructors are populated at object init site.
            loadAnnotations(cw, varName, typeClass, GET_TUPLE_TYPE_IMPL, jvmPackageGen, jvmCastGen,
                    asyncDataCollector, lazyLoadingDataCollector);
            isAnnotatedType = true;
        }
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        mv.visitCode();
        setTypeInitialized(mv, ICONST_1, typeClass);
        jvmTupleTypeGen.createTupleType(cw, mv, typeClass, (BTupleType) bType, isAnnotatedType,
                jvmPackageGen.symbolTable, ACC_PRIVATE);
        setTypeInitialized(mv, ICONST_0, typeClass);
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(typeClass + CLASS_FILE_SUFFIX, cw.toByteArray());
        addDebugField(allTypesCW, varName);
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
        genFieldsForInitFlags(cw);
        String typeClass =  jvmTypeGen.unionTypesPkgName + typeDef.internalName.value;
        generateConstantsClassInit(cw, typeClass);
        boolean isAnnotatedType = false;
        if (!typeDef.isBuiltin || typeDef.referenceType == null || bTypeTag == TypeTags.RECORD) {
            loadAnnotations(cw, varName, typeClass, GET_UNION_TYPE_IMPL, jvmPackageGen, jvmCastGen,
                    asyncDataCollector, lazyLoadingDataCollector);
            isAnnotatedType = true;
        }
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        mv.visitCode();
        setTypeInitialized(mv, ICONST_1, typeClass);
        jvmUnionTypeGen.createUnionType(cw, mv, typeClass, varName, (BUnionType) bType, isAnnotatedType,
                jvmPackageGen.symbolTable);
        setTypeInitialized(mv, ICONST_0, typeClass);
        genMethodReturn(mv);
        cw.visitEnd();
        jarEntries.put(typeClass + CLASS_FILE_SUFFIX, cw.toByteArray());
        addDebugField(allTypesCW, varName);
    }

    public static void genFieldsForInitFlags(ClassWriter cw) {
        cw.visitField(ACC_STATIC | ACC_PRIVATE | ACC_VOLATILE, TYPE_INIT_FIELD, GET_JBOOLEAN, null, null).visitEnd();
        cw.visitField(ACC_STATIC | ACC_PRIVATE | ACC_VOLATILE, TYPE_INIT_ON_FIELD, GET_JBOOLEAN, null, null).visitEnd();
    }


    public static void setTypeInitialized(MethodVisitor mv, int status, String typeClass) {
//        mv.visitInsn(status);
//        mv.visitFieldInsn(PUTSTATIC, typeClass, TYPE_INIT_FIELD, GET_JBOOLEAN);
    }

    public static DoubleCheckLabelsRecord genDoubleCheckGetStart(MethodVisitor mv, String typeClass,
                                                                 String typeDescriptor) {
        Label tryStart = new Label();
        Label tryEnd = new Label();
        Label tryHandler = new Label();
        mv.visitTryCatchBlock(tryStart, tryEnd, tryHandler, null);
        mv.visitFieldInsn(GETSTATIC, typeClass, TYPE_INIT_FIELD, GET_JBOOLEAN);
        Label ifIsInit = new Label();
        mv.visitJumpInsn(IFEQ, ifIsInit);
        mv.visitFieldInsn(GETSTATIC, typeClass, TYPE_VAR_FIELD, typeDescriptor);
        mv.visitInsn(ARETURN);
        mv.visitLabel(ifIsInit);
        mv.visitFieldInsn(GETSTATIC, typeClass, TYPE_INIT_ON_FIELD, GET_JBOOLEAN);
        Label ifIsOnInit = new Label();
        mv.visitJumpInsn(IFEQ, ifIsOnInit);
        mv.visitFieldInsn(GETSTATIC, BAL_RUNTIME, TYPE_INITIALIZING_GLOBAL_LOCK_VAR_NAME,
                LOAD_LOCK);
        mv.visitMethodInsn(INVOKEVIRTUAL, REENTRANT_LOCK, "isHeldByCurrentThread", RETURN_JBOOLEAN, false);
        mv.visitJumpInsn(IFEQ, ifIsOnInit);
        mv.visitFieldInsn(GETSTATIC, typeClass, TYPE_VAR_FIELD, typeDescriptor);
        mv.visitInsn(ARETURN);
        mv.visitLabel(ifIsOnInit);
        mv.visitFieldInsn(GETSTATIC, BAL_RUNTIME, TYPE_INITIALIZING_GLOBAL_LOCK_VAR_NAME, LOAD_LOCK);
        mv.visitMethodInsn(INVOKEVIRTUAL, REENTRANT_LOCK, "lock", VOID_METHOD_DESC, false);
        mv.visitLabel(tryStart);
        mv.visitFieldInsn(GETSTATIC, typeClass, TYPE_INIT_FIELD, GET_JBOOLEAN);
        mv.visitJumpInsn(IFNE, tryEnd);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTSTATIC, typeClass, TYPE_INIT_ON_FIELD, GET_JBOOLEAN);
        return new DoubleCheckLabelsRecord(tryEnd, tryHandler);
    }

    public static void endDoubleCheckGetEnd(MethodVisitor mv, String typeClass, String typeDescriptor,
                                            DoubleCheckLabelsRecord checkLabelsRecord) {
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTSTATIC, typeClass, TYPE_INIT_FIELD, GET_JBOOLEAN);
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTSTATIC, typeClass, TYPE_INIT_ON_FIELD, GET_JBOOLEAN);
        mv.visitLabel(checkLabelsRecord.tryEnd());
        mv.visitFieldInsn(GETSTATIC, BAL_RUNTIME, TYPE_INITIALIZING_GLOBAL_LOCK_VAR_NAME, LOAD_LOCK);
        mv.visitMethodInsn(INVOKEVIRTUAL, REENTRANT_LOCK, "unlock", VOID_METHOD_DESC, false);
        Label label5 = new Label();
        mv.visitJumpInsn(GOTO, label5);
        mv.visitLabel(checkLabelsRecord.tryHandler());
        mv.visitVarInsn(ASTORE, 0);
        mv.visitFieldInsn(GETSTATIC, BAL_RUNTIME, TYPE_INITIALIZING_GLOBAL_LOCK_VAR_NAME, LOAD_LOCK);
        mv.visitMethodInsn(INVOKEVIRTUAL, REENTRANT_LOCK, "unlock", VOID_METHOD_DESC, false);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ATHROW);
        mv.visitLabel(label5);
        mv.visitFieldInsn(GETSTATIC, typeClass, TYPE_VAR_FIELD, typeDescriptor);
        mv.visitInsn(ARETURN);
    }

    public Optional<BIntersectionType> getImmutableType(BType type, SymbolTable symbolTable) {
        if (type.tsymbol == null) {
            return Optional.empty();
        }
        return Types.getImmutableType(symbolTable, type.tsymbol.pkgID, (SelectivelyImmutableReferenceType) type);
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
        mv.visitMethodInsn(INVOKEVIRTUAL, TYPE_ID_SET, ADD_METHOD, ADD_TYPE_ID, false);
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
            mv.visitMethodInsn(INVOKEVIRTUAL, STRING_VALUE, "equals", ANY_TO_JBOOLEAN, false);
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
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, LOAD_ANNOTATIONS_METHOD, "()V", null, null);
        mv.visitCode();
        LazyLoadBirBasicBlock lazyBB = lazyLoadingDataCollector.lazyLoadingAnnotationsBBMap.get(typeName);
        if (lazyBB != null) {
            lazyLoadAnnotations(mv, lazyBB, jvmPackageGen, jvmCastGen, jvmConstantsGen, jvmTypeGen, asyncDataCollector);
        }
        processAnnotations(typeClass, descriptor, mv);
        genMethodReturn(mv);
    }

    public void loadAnnotations(ClassWriter cw, BObjectType bObjectType, String typeName, String typeClass,
                                boolean constructorsPopulated, JvmPackageGen jvmPackageGen, JvmCastGen jvmCastGen,
                                AsyncDataCollector asyncDataCollector,
                                LazyLoadingDataCollector lazyLoadingDataCollector) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, LOAD_ANNOTATIONS_METHOD, "()V", null, null);
        mv.visitCode();
        Map<String, LazyLoadBirBasicBlock> lazyLoadingAnnotationsBBMap =
                lazyLoadingDataCollector.lazyLoadingAnnotationsBBMap;
        LazyLoadBirBasicBlock lazyBB = lazyLoadingAnnotationsBBMap.get(typeName);
        if (lazyBB != null) {
            lazyLoadAnnotations(mv, lazyBB, jvmPackageGen, jvmCastGen, jvmConstantsGen, jvmTypeGen, asyncDataCollector);
        }
        BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) bObjectType.tsymbol;
        List<BAttachedFunction> attachedFunctions = objectTypeSymbol.attachedFuncs;
        for (BAttachedFunction attachedFunction : attachedFunctions) {
            lazyBB = lazyLoadingAnnotationsBBMap.get(typeName + "." + attachedFunction.symbol.originalName);
            if (lazyBB != null) {
                lazyLoadAnnotations(mv, lazyBB, jvmPackageGen, jvmCastGen, jvmConstantsGen, jvmTypeGen,
                        asyncDataCollector);
            }
        }
        if (!constructorsPopulated) {
            processAnnotations(typeClass, GET_OBJECT_TYPE_IMPL, mv);
        }
        genMethodReturn(mv);
    }

    private void processAnnotations(String typeClass, String descriptor, MethodVisitor mv) {

        mv.visitFieldInsn(GETSTATIC, annotationVarClassName, VALUE_VAR_FIELD, GET_MAP_VALUE);
        mv.visitFieldInsn(GETSTATIC, typeClass, TYPE_VAR_FIELD, descriptor);
        mv.visitMethodInsn(INVOKESTATIC, ANNOTATION_UTILS, "processAnnotations", PROCESS_ANNOTATIONS, false);
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
            createDefaultCaseThrowError(mv, defaultCaseLabel, 1, "No such type: ");
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
                    createDefaultCaseThrowError(mv, defaultCaseLabel, shapeParamRegIndex, "No such type: ");
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
            createDefaultCaseThrowError(mv, defaultCaseLabel, shapeParamRegIndex, "No such type: ");
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
    //              getRecordType() generation methods
    // -------------------------------------------------------
    public void generateRecordTypeClass(JvmPackageGen jvmPackageGen, BIRNode.BIRPackage module,
                                          JarEntries jarEntries, List<BIRTypeDefinition> recordTypeDefList) {
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V21, ACC_PUBLIC + ACC_SUPER, recordTypesClass, null, OBJECT, null);
        generateGetRecordTypeMainMethod(cw, recordTypeDefList);
        cw.visitEnd();
        byte[] bytes = jvmPackageGen.getBytes(cw, module);
        jarEntries.put(recordTypesClass + ".class", bytes);
    }

    private void generateGetRecordTypeMainMethod(ClassWriter cw, List<BIRTypeDefinition> recordTypeDefList) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, GET_RECORD_TYPE_METHOD, GET_RECORD_TYPE_FOR_STRING,
                null, null);
        mv.visitCode();
        if (recordTypeDefList.isEmpty()) {
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, recordTypesClass, GET_RECORD_TYPE_METHOD + 0, GET_RECORD_TYPE_FOR_STRING,
                    false);
            mv.visitInsn(ARETURN);
            generateGetRecordTypeSplitMethods(cw, recordTypeDefList);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    void generateGetRecordTypeSplitMethods(ClassWriter cw, List<BIRTypeDefinition> recordTypeDefList) {
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        int funcNameRegIndex = 0;
        Label defaultCaseLabel = new Label();
        // case body
        int i = 0;
        List<Label> targetLabels = new ArrayList<>();
        for (BIRTypeDefinition recordType : recordTypeDefList) {
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, GET_RECORD_TYPE_METHOD + methodCount++,
                        GET_RECORD_TYPE_FOR_STRING, null, null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                int remainingCases = recordTypeDefList.size() - bTypesCount;
                if (remainingCases > MAX_TYPES_PER_METHOD) {
                    remainingCases = MAX_TYPES_PER_METHOD;
                }
                List<Label> labels = JvmCreateTypeGen.createLabelsForSwitch(mv, funcNameRegIndex, recordTypeDefList,
                        bTypesCount, remainingCases, defaultCaseLabel);
                targetLabels = JvmCreateTypeGen.createLabelsForEqualCheck(mv, funcNameRegIndex, recordTypeDefList,
                        bTypesCount, remainingCases, labels, defaultCaseLabel);
                i = 0;
            }
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            jvmTypeGen.getUserDefinedType(mv, recordType.type);
            mv.visitInsn(ARETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                if (bTypesCount == (recordTypeDefList.size())) {
                    createDefaultCaseReturnNull(mv, defaultCaseLabel);
                } else {
                    mv.visitLabel(defaultCaseLabel);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKESTATIC, recordTypesClass, GET_RECORD_TYPE_METHOD + methodCount,
                            GET_RECORD_TYPE_FOR_STRING, false);
                    mv.visitInsn(ARETURN);
                }
                mv.visitMaxs(i + 10, i + 10);
                mv.visitEnd();
            }
        }
        if (methodCount != 0 && bTypesCount % MAX_TYPES_PER_METHOD != 0) {
            createDefaultCaseReturnNull(mv, defaultCaseLabel);
            mv.visitMaxs(i + 10, i + 10);
            mv.visitEnd();
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

    private void generateGetFunctionTypeMainMethod(ClassWriter cw, List<BIRNode.BIRFunction> sortedFunctions) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, GET_FUNCTION_TYPE_METHOD,
                GET_FUNCTION_TYPE_FOR_STRING, null, null);
        mv.visitCode();
        if (sortedFunctions.isEmpty()) {
            Label defaultCaseLabel = new Label();
            createDefaultCaseThrowError(mv, defaultCaseLabel, 1, "No such function type: ");
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, functionTypesClass, GET_FUNCTION_TYPE_METHOD + 0,
                    GET_FUNCTION_TYPE_FOR_STRING, false);
            mv.visitInsn(ARETURN);
            generateGetFunctionTypeSplitMethods(cw, sortedFunctions);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    void generateGetFunctionTypeSplitMethods(ClassWriter cw, List<BIRNode.BIRFunction> sortedFunctions) {
        int bTypesCount = 0;
        int methodCount = 0;
        MethodVisitor mv = null;
        int funcNameRegIndex = 0;
        Label defaultCaseLabel = new Label();
        // case body
        int i = 0;
        List<Label> targetLabels = new ArrayList<>();
        for (BIRNode.BIRFunction func : sortedFunctions) {
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, GET_FUNCTION_TYPE_METHOD + methodCount++,
                        GET_FUNCTION_TYPE_FOR_STRING, null, null);
                mv.visitCode();
                defaultCaseLabel = new Label();
                int remainingCases = sortedFunctions.size() - bTypesCount;
                if (remainingCases > MAX_TYPES_PER_METHOD) {
                    remainingCases = MAX_TYPES_PER_METHOD;
                }
                List<Label> labels = JvmCreateTypeGen.createLabelsForSwitch(mv, funcNameRegIndex, sortedFunctions,
                        bTypesCount, remainingCases, defaultCaseLabel, false);
                targetLabels = JvmCreateTypeGen.createLabelsForEqualCheck(mv, funcNameRegIndex, sortedFunctions,
                        bTypesCount, remainingCases, labels, defaultCaseLabel, false);
                i = 0;
            }
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            String functionName = func.name.value;
            String functionTypeConstantClass = jvmConstantsGen.getFunctionTypeConstantClass(functionName);
            mv.visitFieldInsn(GETSTATIC, functionTypeConstantClass, VALUE_VAR_FIELD, GET_FUNCTION_TYPE);
            mv.visitInsn(ARETURN);
            i += 1;
            bTypesCount++;
            if (bTypesCount % MAX_TYPES_PER_METHOD == 0) {
                if (bTypesCount == (sortedFunctions.size())) {
                    createDefaultCaseThrowError(mv, defaultCaseLabel, funcNameRegIndex, "No such function type: ");
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
            createDefaultCaseThrowError(mv, defaultCaseLabel, funcNameRegIndex, "No such function type: ");
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
        mv.visitFieldInsn(GETSTATIC, varClass, VALUE_VAR_FIELD, GET_FUNCTION_POINTER);
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
