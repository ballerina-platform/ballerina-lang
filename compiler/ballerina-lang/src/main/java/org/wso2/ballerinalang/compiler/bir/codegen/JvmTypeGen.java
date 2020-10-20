/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.codegen;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.ScheduleFunctionInfo;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeIdSet;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.semantics.model.types.TypeFlags;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.ResolvedTypeBuilder;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.ballerina.runtime.IdentifierUtils.decodeIdentifier;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SWAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_MAP_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANYDATA_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANY_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ATTACHED_FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ATTACHED_FUNCTION_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BALLERINA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BOOLEAN_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BOOLEAN_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BUILT_IN_PACKAGE_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BYTE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_RECORD_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_TYPES_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_TYPE_INSTANCES_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DOUBLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FIELD_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FINITE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FLOAT_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INTEGER_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INTERSECTION_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INTERSECTION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JSON_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LONG_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.NEVER_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.NULL_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PREDEFINED_TYPES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.READONLY_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RECORD_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SERVICE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SERVICE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_DETAIL_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_IMMUTABLE_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET_TYPEID_SET_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STREAM_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STREAM_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TUPLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPES_ERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_ID_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.UNION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_OF_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.loadConstantValue;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.NAME_HASH_COMPARATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.createDefaultCase;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.createLabelsForEqualCheck;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.createLabelsForSwitch;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeValueClassName;

/**
 * BIR types to JVM byte code generation class.
 *
 * @since 1.2.0
 */
class JvmTypeGen {

    private static ResolvedTypeBuilder typeBuilder = new ResolvedTypeBuilder();

    /**
     * Create static fields to hold the user defined types.
     *
     * @param cw       class writer
     * @param typeDefs array of type definitions
     */
    static void generateUserDefinedTypeFields(ClassWriter cw, List<BIRTypeDefinition> typeDefs) {

        String fieldName;
        // create the type
        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            fieldName = getTypeFieldName(optionalTypeDef.name.value);
            BType bType = optionalTypeDef.type;
            if (bType.tag == TypeTags.RECORD || bType.tag == TypeTags.ERROR || bType.tag == TypeTags.OBJECT) {
                FieldVisitor fv = cw.visitField(ACC_STATIC + ACC_PUBLIC, fieldName, String.format("L%s;", TYPE), null,
                                                null);
                fv.visitEnd();
            }
            // do not generate anything for other types (e.g.: finite type, unions, etc.)
        }
    }

    static void generateCreateTypesMethod(ClassWriter cw, List<BIRTypeDefinition> typeDefs, String typeOwnerClass,
                                          SymbolTable symbolTable) {

        createTypesInstance(cw, typeDefs, typeOwnerClass);
        List<String> populateTypeFuncNames = populateTypes(cw, typeDefs, typeOwnerClass, symbolTable);

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_TYPES_METHOD, "()V", null, null);
        mv.visitCode();

        // Invoke create-type-instances method
        mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, CREATE_TYPE_INSTANCES_METHOD, "()V", false);

        // Invoke the populate-type functions
        for (String funcName : populateTypeFuncNames) {
            mv.visitMethodInsn(INVOKESTATIC, typeOwnerClass, funcName, "()V", false);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void createTypesInstance(ClassWriter cw, List<BIRTypeDefinition> typeDefs, String typeOwnerClass) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, CREATE_TYPE_INSTANCES_METHOD, "()V", null, null);
        mv.visitCode();

        // Create the type
        String fieldName;
        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            fieldName = getTypeFieldName(optionalTypeDef.name.value);
            BType bType = optionalTypeDef.type;
            if (bType.tag == TypeTags.RECORD) {
                createRecordType(mv, (BRecordType) bType);
            } else if (bType.tag == TypeTags.OBJECT) {
                if (bType instanceof BServiceType) {
                    createServiceType(mv, (BServiceType) bType);
                } else {
                    createObjectType(mv, (BObjectType) bType);
                }
            } else if (bType.tag == TypeTags.ERROR) {
                createErrorType(mv, (BErrorType) bType, bType.tsymbol.name.value);
            } else {
                // do not generate anything for other types (e.g.: finite type, unions, etc.)
                continue;
            }

            mv.visitFieldInsn(PUTSTATIC, typeOwnerClass, fieldName, String.format("L%s;", TYPE));
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static List<String> populateTypes(ClassWriter cw, List<BIRTypeDefinition> typeDefs, String typeOwnerClass,
                                              SymbolTable symbolTable) {

        List<String> funcNames = new ArrayList<>();
        String fieldName;
        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            BType bType = optionalTypeDef.type;
            if (!(bType.tag == TypeTags.RECORD || bType.tag == TypeTags.OBJECT || bType.tag == TypeTags.ERROR)) {
                continue;
            }

            fieldName = getTypeFieldName(optionalTypeDef.name.value);
            String methodName = String.format("$populate%s", fieldName);
            funcNames.add(methodName);

            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, "()V", null, null);
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, String.format("L%s;", TYPE));

            BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
            switch (bType.tag) {
                case TypeTags.RECORD:
                    BRecordType recordType = (BRecordType) bType;
                    mv.visitTypeInsn(CHECKCAST, RECORD_TYPE_IMPL);
                    mv.visitInsn(DUP);
                    mv.visitInsn(DUP);
                    addRecordFields(mv, recordType.fields);
                    addRecordRestField(mv, recordType.restFieldType);
                    addImmutableType(mv, recordType);
                    break;
                case TypeTags.OBJECT:
                    if (bType instanceof BServiceType) {
                        BServiceType serviceType = (BServiceType) bType;
                        mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE_IMPL);
                        mv.visitInsn(DUP);
                        addObjectFields(mv, serviceType.fields);
                        addObjectAttachedFunctions(mv, ((BObjectTypeSymbol) serviceType.tsymbol).attachedFuncs,
                                serviceType, indexMap, symbolTable);
                    } else {
                        BObjectType objectType = (BObjectType) bType;
                        mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE_IMPL);
                        mv.visitInsn(DUP);
                        mv.visitInsn(DUP);
                        addObjectFields(mv, objectType.fields);
                        BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) objectType.tsymbol;
                        addObjectInitFunction(mv, objectTypeSymbol.generatedInitializerFunc, objectType, indexMap,
                                "$init$", "setGeneratedInitializer", symbolTable);
                        addObjectInitFunction(mv, objectTypeSymbol.initializerFunc, objectType, indexMap, "init",
                                "setInitializer", symbolTable);
                        addObjectAttachedFunctions(mv, objectTypeSymbol.attachedFuncs, objectType, indexMap,
                                symbolTable);
                        addImmutableType(mv, objectType);
                    }
                    BTypeIdSet objTypeIdSet = ((BObjectType) bType).typeIdSet;
                    if (!objTypeIdSet.isEmpty()) {
                        mv.visitInsn(DUP);
                        loadTypeIdSet(mv, objTypeIdSet);
                        mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE_IMPL, SET_TYPEID_SET_METHOD,
                                           String.format("(L%s;)V", TYPE_ID_SET), false);
                    }
                    break;
                case TypeTags.ERROR:
                    // populate detail field
                    mv.visitTypeInsn(CHECKCAST, ERROR_TYPE_IMPL);
                    mv.visitInsn(DUP);
                    mv.visitInsn(DUP);
                    loadType(mv, ((BErrorType) bType).detailType);
                    mv.visitMethodInsn(INVOKEVIRTUAL, ERROR_TYPE_IMPL, SET_DETAIL_TYPE_METHOD,
                                       String.format("(L%s;)V", TYPE), false);
                    BTypeIdSet typeIdSet = ((BErrorType) bType).typeIdSet;
                    if (!typeIdSet.isEmpty()) {
                        mv.visitInsn(DUP);
                        loadTypeIdSet(mv, typeIdSet);
                        mv.visitMethodInsn(INVOKEVIRTUAL, ERROR_TYPE_IMPL, SET_TYPEID_SET_METHOD,
                                           String.format("(L%s;)V", TYPE_ID_SET), false);
                    }
                    break;
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        return funcNames;
    }

    private static void addImmutableType(MethodVisitor mv, BStructureType structureType) {
        BIntersectionType immutableType = ((SelectivelyImmutableReferenceType) structureType).getImmutableType();
        if (immutableType == null) {
            return;
        }

        mv.visitInsn(DUP);
        loadType(mv, immutableType);
        mv.visitMethodInsn(INVOKEINTERFACE, TYPE, SET_IMMUTABLE_TYPE_METHOD, String.format("(L%s;)V",
                                                                                         INTERSECTION_TYPE), true);
    }

    private static void loadTypeIdSet(MethodVisitor mv, BTypeIdSet typeIdSet) {
        // Create TypeIdSet
        mv.visitTypeInsn(NEW, TYPE_ID_SET);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, TYPE_ID_SET, JVM_INIT_METHOD, "()V", false);

        for (BTypeIdSet.BTypeId typeId : typeIdSet.primary) {
            addTypeId(mv, typeId, true);
        }

        for (BTypeIdSet.BTypeId typeId : typeIdSet.secondary) {
            addTypeId(mv, typeId, false);
        }
    }

    private static void addTypeId(MethodVisitor mv, BTypeIdSet.BTypeId typeId, boolean isPrimaryTypeId) {
        mv.visitInsn(DUP);
        // Load package
        mv.visitTypeInsn(NEW, MODULE);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(typeId.packageID.orgName.value);
        mv.visitLdcInsn(typeId.packageID.name.value);
        mv.visitLdcInsn(typeId.packageID.version.value);
        mv.visitMethodInsn(INVOKESPECIAL, MODULE, JVM_INIT_METHOD,
                String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);

        mv.visitLdcInsn(typeId.name);
        mv.visitInsn(isPrimaryTypeId ? ICONST_1 : ICONST_0);
        // Add to BTypeIdSet
        mv.visitMethodInsn(INVOKEVIRTUAL, TYPE_ID_SET, "add",
                           String.format("(L%s;L%s;Z)V", MODULE, STRING_VALUE), false);
    }

    // -------------------------------------------------------
    //              Runtime value creation methods
    // -------------------------------------------------------

    static void generateValueCreatorMethods(ClassWriter cw, List<BIRTypeDefinition> typeDefs,
                                            BIRNode.BIRPackage moduleId, String typeOwnerClass,
                                            SymbolTable symbolTable, AsyncDataCollector asyncDataCollector) {

        List<BIRTypeDefinition> recordTypeDefs = new ArrayList<>();
        List<BIRTypeDefinition> objectTypeDefs = new ArrayList<>();

        int i = 0;
        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            BType bType = optionalTypeDef.type;
            if (bType.tag == TypeTags.RECORD) {
                recordTypeDefs.add(i, optionalTypeDef);
                i += 1;
            }
        }

        i = 0;
        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            BType bType = optionalTypeDef.type;
            if (bType.tag == TypeTags.OBJECT &&
                    Symbols.isFlagOn(((BObjectType) bType).tsymbol.flags, Flags.CLASS)) {
                objectTypeDefs.add(i, optionalTypeDef);
                i += 1;
            }
        }

        generateRecordValueCreateMethod(cw, recordTypeDefs, moduleId, typeOwnerClass, asyncDataCollector);
        generateObjectValueCreateMethod(cw, objectTypeDefs, moduleId, typeOwnerClass, symbolTable,
                                        asyncDataCollector);
    }

    private static void generateRecordValueCreateMethod(ClassWriter cw, List<BIRTypeDefinition> recordTypeDefs,
                                                        BIRNode.BIRPackage moduleId, String typeOwnerClass,
                                                        AsyncDataCollector asyncDataCollector) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, CREATE_RECORD_VALUE,
                String.format("(L%s;)L%s;", STRING_VALUE, MAP_VALUE),
                String.format("(L%s;)L%s<L%s;L%s;>;", STRING_VALUE, MAP_VALUE, STRING_VALUE, OBJECT), null);

        mv.visitCode();

        int fieldNameRegIndex = 1;
        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        recordTypeDefs.sort(NAME_HASH_COMPARATOR);

        List<Label> labels = createLabelsForSwitch(mv, fieldNameRegIndex, recordTypeDefs, defaultCaseLabel);
        List<Label> targetLabels = createLabelsForEqualCheck(mv, fieldNameRegIndex, recordTypeDefs, labels,
                defaultCaseLabel);

        int i = 0;

        for (BIRTypeDefinition optionalTypeDef : recordTypeDefs) {
            String fieldName = getTypeFieldName(optionalTypeDef.name.value);
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            String className = getTypeValueClassName(moduleId, optionalTypeDef.name.value);
            mv.visitTypeInsn(NEW, className);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, String.format("L%s;", TYPE));
            mv.visitMethodInsn(INVOKESPECIAL, className, JVM_INIT_METHOD, String.format("(L%s;)V", TYPE), false);

            mv.visitInsn(DUP);
            mv.visitTypeInsn(NEW, STRAND_CLASS);
            mv.visitInsn(DUP);
            mv.visitInsn(ACONST_NULL);
            String metaDataVarName = JvmCodeGenUtil.getStrandMetadataVarName(CREATE_RECORD_VALUE);
            asyncDataCollector
                    .getStrandMetadata().putIfAbsent(metaDataVarName, new ScheduleFunctionInfo(CREATE_RECORD_VALUE));
            mv.visitFieldInsn(GETSTATIC, typeOwnerClass, metaDataVarName, String.format("L%s;", STRAND_METADATA));
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
        }

        createDefaultCase(mv, defaultCaseLabel, fieldNameRegIndex);
        mv.visitMaxs(recordTypeDefs.size() + 10, recordTypeDefs.size() + 10);
        mv.visitEnd();
    }

    private static void generateObjectValueCreateMethod(ClassWriter cw, List<BIRTypeDefinition> objectTypeDefs,
                                                        BIRNode.BIRPackage moduleId, String typeOwnerClass,
                                                        SymbolTable symbolTable,
                                                        AsyncDataCollector asyncDataCollector) {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, CREATE_OBJECT_VALUE,
                                          String.format("(L%s;L%s;L%s;L%s;[L%s;)L%s;", STRING_VALUE, SCHEDULER,
                                                        STRAND_CLASS, MAP, OBJECT,
                                                        B_OBJECT), null, null);

        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();

        BIRVariableDcl selfVar = new BIRVariableDcl(symbolTable.anyType, new Name("self"), VarScope.FUNCTION,
                VarKind.ARG);

        BIRVariableDcl var1 = new BIRVariableDcl(symbolTable.stringType, new Name("var1"), VarScope.FUNCTION,
                VarKind.ARG);

        BIRVariableDcl scheduler = new BIRVariableDcl(symbolTable.anyType, new Name("scheduler"), VarScope.FUNCTION,
                VarKind.ARG);

        BIRVariableDcl parent = new BIRVariableDcl(symbolTable.anyType, new Name("parent"), VarScope.FUNCTION,
                VarKind.ARG);

        BIRVariableDcl properties = new BIRVariableDcl(symbolTable.anyType, new Name("properties"), VarScope.FUNCTION,
                VarKind.ARG);

        BIRVariableDcl args = new BIRVariableDcl(symbolTable.anyType, new Name("args"), VarScope.FUNCTION, VarKind.ARG);

        indexMap.addToMapIfNotFoundAndGetIndex(selfVar);

        int var1Index = indexMap.addToMapIfNotFoundAndGetIndex(var1);
        int schedulerIndex = indexMap.addToMapIfNotFoundAndGetIndex(scheduler);
        int parentIndex = indexMap.addToMapIfNotFoundAndGetIndex(parent);
        int propertiesIndex = indexMap.addToMapIfNotFoundAndGetIndex(properties);
        int argsIndex = indexMap.addToMapIfNotFoundAndGetIndex(args);

        mv.visitCode();

        Label defaultCaseLabel = new Label();

        // sort the fields before generating switch case
        objectTypeDefs.sort(NAME_HASH_COMPARATOR);

        List<Label> labels = createLabelsForSwitch(mv, var1Index, objectTypeDefs, defaultCaseLabel);
        List<Label> targetLabels = createLabelsForEqualCheck(mv, var1Index, objectTypeDefs, labels,
                defaultCaseLabel);

        int i = 0;

        for (BIRTypeDefinition optionalTypeDef : objectTypeDefs) {
            String fieldName = getTypeFieldName(optionalTypeDef.name.value);
            Label targetLabel = targetLabels.get(i);
            mv.visitLabel(targetLabel);
            mv.visitVarInsn(ALOAD, 0);
            String className = getTypeValueClassName(moduleId, optionalTypeDef.name.value);
            mv.visitTypeInsn(NEW, className);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETSTATIC, typeOwnerClass, fieldName, String.format("L%s;", TYPE));
            mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE_IMPL);
            mv.visitMethodInsn(INVOKESPECIAL, className, JVM_INIT_METHOD, String.format("(L%s;)V", OBJECT_TYPE_IMPL),
                               false);

            BIRVariableDcl tempVar = new BIRVariableDcl(optionalTypeDef.type, new Name("tempVar"), VarScope.FUNCTION,
                                                        VarKind.LOCAL);

            int tempVarIndex = indexMap.addToMapIfNotFoundAndGetIndex(tempVar);
            mv.visitVarInsn(ASTORE, tempVarIndex);
            BIRVariableDcl strandVar = new BIRVariableDcl(symbolTable.anyType, new Name("strandVar"), VarScope.FUNCTION,
                    VarKind.LOCAL);
            int strandVarIndex = indexMap.addToMapIfNotFoundAndGetIndex(strandVar);

            mv.visitVarInsn(ALOAD, parentIndex);
            Label parentNonNullLabel = new Label();
            mv.visitJumpInsn(IFNONNULL, parentNonNullLabel);
            Label parentNullLabel = new Label();
            mv.visitLabel(parentNullLabel);
            mv.visitTypeInsn(NEW, STRAND_CLASS);
            mv.visitInsn(DUP);
            mv.visitInsn(ACONST_NULL);
            String metaDataVarName = JvmCodeGenUtil.getStrandMetadataVarName(CREATE_OBJECT_VALUE);
            asyncDataCollector
                    .getStrandMetadata().putIfAbsent(metaDataVarName, new ScheduleFunctionInfo(CREATE_OBJECT_VALUE));
            mv.visitFieldInsn(GETSTATIC, typeOwnerClass, metaDataVarName, String.format("L%s;", STRAND_METADATA));
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

            BIRVariableDcl tempResult = new BIRVariableDcl(symbolTable.anyType, new Name("tempResult"),
                    VarScope.FUNCTION, VarKind.LOCAL);

            int tempResultIndex = indexMap.addToMapIfNotFoundAndGetIndex(tempResult);
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
        }

        createDefaultCase(mv, defaultCaseLabel, var1Index);
        mv.visitMaxs(objectTypeDefs.size() + 100, objectTypeDefs.size() + 100);
        mv.visitEnd();
    }

    // -------------------------------------------------------
    //              Record type generation methods
    // -------------------------------------------------------

    /**
     * Create a runtime type instance for the record.
     *
     * @param mv         method visitor
     * @param recordType record type
     */
    private static void createRecordType(MethodVisitor mv, BRecordType recordType) {
        // Create the record type
        mv.visitTypeInsn(NEW, RECORD_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load type name
        BTypeSymbol typeSymbol = recordType.tsymbol;
        String name = typeSymbol.name.getValue();
        mv.visitLdcInsn(name);

        // Load package path
        // TODO: get it from the type
        mv.visitTypeInsn(NEW, MODULE);
        mv.visitInsn(DUP);

        PackageID packageID = recordType.tsymbol.pkgID;

        mv.visitLdcInsn(packageID.orgName.value);
        mv.visitLdcInsn(packageID.name.value);
        mv.visitLdcInsn(packageID.version.value);
        mv.visitMethodInsn(INVOKESPECIAL, MODULE, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);

        // Load flags
        mv.visitLdcInsn(recordType.tsymbol.flags);

        // Load 'sealed' flag
        mv.visitLdcInsn(recordType.sealed);

        // Load type flags
        mv.visitLdcInsn(typeFlag(recordType));

        // initialize the record type
        mv.visitMethodInsn(INVOKESPECIAL, RECORD_TYPE_IMPL, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;IZI)V", STRING_VALUE, MODULE), false);
    }

    /**
     * Add the field type information of a record type. The record type is assumed
     * to be at the top of the stack.
     *
     * @param mv     method visitor
     * @param fields record fields to be added
     */
    private static void addRecordFields(MethodVisitor mv, Map<String, BField> fields) {
        // Create the fields map
        mv.visitTypeInsn(NEW, LINKED_HASH_MAP);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, JVM_INIT_METHOD, "()V", false);

        for (BField optionalField : fields.values()) {
            mv.visitInsn(DUP);

            // Load field name
            mv.visitLdcInsn(decodeIdentifier(optionalField.name.value));

            // create and load field type
            createRecordField(mv, optionalField);

            // Add the field to the map
            mv.visitMethodInsn(INVOKEINTERFACE, MAP, "put",
                    String.format("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT),
                    true);

            // emit a pop, since we are not using the return value from the map.put()
            mv.visitInsn(POP);
        }

        // Set the fields of the record
        mv.visitMethodInsn(INVOKEVIRTUAL, RECORD_TYPE_IMPL, "setFields", String.format("(L%s;)V", MAP), false);
    }

    /**
     * Create a field information for records.
     *
     * @param mv    method visitor
     * @param field field Parameter Description
     */
    private static void createRecordField(MethodVisitor mv, BField field) {

        mv.visitTypeInsn(NEW, FIELD_IMPL);
        mv.visitInsn(DUP);

        // Load the field type
        loadType(mv, field.type);

        // Load field name
        mv.visitLdcInsn(field.name.value);

        // Load flags
        mv.visitLdcInsn(field.symbol.flags);

        mv.visitMethodInsn(INVOKESPECIAL, FIELD_IMPL, JVM_INIT_METHOD, String.format("(L%s;L%s;I)V", TYPE,
                                                                                     STRING_VALUE), false);
    }

    private static int typeFlag(BType type) {

        return TypeFlags.asMask(type.isNullable(), type.isAnydata(), type.isPureType());
    }

    /**
     * Add the rest field to a record type. The record type is assumed
     * to be at the top of the stack.
     *
     * @param mv            method visitor
     * @param restFieldType type of the rest field
     */
    private static void addRecordRestField(MethodVisitor mv, BType restFieldType) {
        // Load the rest field type
        loadType(mv, restFieldType);
        mv.visitFieldInsn(PUTFIELD, RECORD_TYPE_IMPL, "restFieldType", String.format("L%s;", TYPE));
    }

    // -------------------------------------------------------
    //              Object type generation methods
    // -------------------------------------------------------

    /**
     * Create a runtime type instance for the object.
     *
     * @param mv         method visitor
     * @param objectType object type
     */
    private static void createObjectType(MethodVisitor mv, BObjectType objectType) {
        // Create the object type
        mv.visitTypeInsn(NEW, OBJECT_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load type name
        BTypeSymbol typeSymbol = objectType.tsymbol;
        mv.visitLdcInsn(decodeIdentifier(typeSymbol.name.getValue()));

        // Load package path
        mv.visitTypeInsn(NEW, MODULE);
        mv.visitInsn(DUP);

        PackageID packageID = objectType.tsymbol.pkgID;

        mv.visitLdcInsn(packageID.orgName.value);
        mv.visitLdcInsn(packageID.name.value);
        mv.visitLdcInsn(packageID.version.value);
        mv.visitMethodInsn(INVOKESPECIAL, MODULE, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);

        // Load flags
        mv.visitLdcInsn(typeSymbol.flags);

        // initialize the object
        mv.visitMethodInsn(INVOKESPECIAL, OBJECT_TYPE_IMPL, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;I)V", STRING_VALUE, MODULE), false);
    }

    /**
     * Create a runtime type instance for the service.
     *
     * @param mv         method visitor
     * @param objectType object type
     */
    private static void createServiceType(MethodVisitor mv, BObjectType objectType) {
        // Create the object type
        mv.visitTypeInsn(NEW, SERVICE_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load type name
        BTypeSymbol typeSymbol = objectType.tsymbol;

        mv.visitLdcInsn(decodeIdentifier(typeSymbol.name.getValue()));

        // Load package path
        mv.visitTypeInsn(NEW, MODULE);
        mv.visitInsn(DUP);
        PackageID packageID = objectType.tsymbol.pkgID;

        mv.visitLdcInsn(packageID.orgName.value);
        mv.visitLdcInsn(packageID.name.value);
        mv.visitLdcInsn(packageID.version.value);
        mv.visitMethodInsn(INVOKESPECIAL, MODULE, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);

        // Load flags
        mv.visitLdcInsn(typeSymbol.flags);

        // initialize the object
        mv.visitMethodInsn(INVOKESPECIAL, SERVICE_TYPE_IMPL, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;I)V", STRING_VALUE, MODULE), false);
    }

    static void duplicateServiceTypeWithAnnots(MethodVisitor mv, BObjectType objectType, String pkgClassName,
                                               int strandIndex) {

        createServiceType(mv, objectType);
        mv.visitInsn(DUP);

        mv.visitFieldInsn(GETSTATIC, pkgClassName, ANNOTATION_MAP_NAME, String.format("L%s;", MAP_VALUE));

        mv.visitVarInsn(ALOAD, strandIndex);

        loadType(mv, objectType);
        mv.visitTypeInsn(CHECKCAST, SERVICE_TYPE_IMPL);

        BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) objectType.tsymbol;

        List<BAttachedFunction> attachedFunctions = objectTypeSymbol.attachedFuncs;
        mv.visitLdcInsn((long) attachedFunctions.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, ATTACHED_FUNCTION_IMPL);
        int i = 0;
        for (BAttachedFunction attachedFunc : attachedFunctions) {
            if (attachedFunc == null) {
                continue;
            }
            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) i);
            mv.visitInsn(L2I);
            createObjectAttachedFunction(mv, attachedFunc, objectType);
            mv.visitInsn(AASTORE);
            i += 1;
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, SERVICE_TYPE_IMPL, "setAttachedFuncsAndProcessAnnots", String.format(
                "(L%s;L%s;L%s;[L%s;)V", MAP_VALUE, STRAND_CLASS, SERVICE_TYPE_IMPL, ATTACHED_FUNCTION_IMPL), false);
    }

    /**
     * Add the field type information to an object type. The object type is assumed
     * to be at the top of the stack.
     *
     * @param mv     method visitor
     * @param fields object fields to be added
     */
    private static void addObjectFields(MethodVisitor mv, Map<String, BField> fields) {
        // Create the fields map
        mv.visitTypeInsn(NEW, LINKED_HASH_MAP);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_MAP, JVM_INIT_METHOD, "()V", false);

        for (BField optionalField : fields.values()) {
            mv.visitInsn(DUP);

            // Load field name
            mv.visitLdcInsn(decodeIdentifier(optionalField.name.value));

            // create and load field type
            createObjectField(mv, optionalField);

            // Add the field to the map
            mv.visitMethodInsn(INVOKEINTERFACE, MAP, "put",
                    String.format("(L%s;L%s;)L%s;", OBJECT, OBJECT, OBJECT),
                    true);

            // emit a pop, since we are not using the return value from the map.put()
            mv.visitInsn(POP);
        }

        // Set the fields of the object
        mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE_IMPL, "setFields", String.format("(L%s;)V", MAP), false);
    }

    /**
     * Create a field information for objects.
     *
     * @param mv    method visitor
     * @param field object field
     */
    private static void createObjectField(MethodVisitor mv, BField field) {

        mv.visitTypeInsn(NEW, FIELD_IMPL);
        mv.visitInsn(DUP);

        // Load the field type
        loadType(mv, field.type);

        // Load field name
        mv.visitLdcInsn(decodeIdentifier(field.name.value));

        // Load flags
        mv.visitLdcInsn(field.symbol.flags);

        mv.visitMethodInsn(INVOKESPECIAL, FIELD_IMPL, JVM_INIT_METHOD, String.format("(L%s;L%s;I)V", TYPE,
                                                                                     STRING_VALUE), false);
    }

    /**
     * Add the attached function information to an object type. The object type is assumed
     * to be at the top of the stack.
     *
     * @param mv                method visitor
     * @param attachedFunctions attached functions to be added
     * @param objType           object type to be used to create attached functions
     * @param indexMap          jvm index generation map for function generation
     */
    private static void addObjectAttachedFunctions(MethodVisitor mv, List<BAttachedFunction> attachedFunctions,
                                                   BObjectType objType, BIRVarToJVMIndexMap indexMap,
                                                   SymbolTable symbolTable) {
        // Create the attached function array
        mv.visitLdcInsn((long) attachedFunctions.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, ATTACHED_FUNCTION_IMPL);
        int i = 0;
        for (BAttachedFunction attachedFunc : attachedFunctions) {
            if (attachedFunc == null) {
                continue;
            }
            // create and load attached function
            createObjectAttachedFunction(mv, attachedFunc, objType);
            BIRVariableDcl attachedFuncVar = new BIRVariableDcl(symbolTable.anyType,
                    new Name(toNameString(objType) + attachedFunc.funcName.value), VarScope.FUNCTION,
                    VarKind.LOCAL);
            int attachedFunctionVarIndex = indexMap.addToMapIfNotFoundAndGetIndex(attachedFuncVar);
            mv.visitVarInsn(ASTORE, attachedFunctionVarIndex);

            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) i);
            mv.visitInsn(L2I);

            // Add the member to the array
            mv.visitVarInsn(ALOAD, attachedFunctionVarIndex);
            mv.visitInsn(AASTORE);
            i += 1;
        }

        // Set the fields of the object
        mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE_IMPL, "setAttachedFunctions",
                           String.format("([L%s;)V", ATTACHED_FUNCTION), false);
    }

    private static void addObjectInitFunction(MethodVisitor mv, BAttachedFunction initFunction,
                                              BObjectType objType, BIRVarToJVMIndexMap indexMap, String funcName,
                                              String initializerFuncName, SymbolTable symbolTable) {

        if (initFunction == null || !initFunction.funcName.value.contains(funcName)) {
            return;
        }

        mv.visitInsn(DUP);
        createObjectAttachedFunction(mv, initFunction, objType);
        BType anyType = symbolTable.anyType;
        BIRVariableDcl attachedFuncVar = new BIRVariableDcl(anyType,
                new Name(objType.name + initFunction.funcName.value), VarScope.FUNCTION, VarKind.LOCAL);
        int attachedFunctionVarIndex = indexMap.addToMapIfNotFoundAndGetIndex(attachedFuncVar);
        mv.visitVarInsn(ASTORE, attachedFunctionVarIndex);
        mv.visitVarInsn(ALOAD, attachedFunctionVarIndex);
        mv.visitInsn(DUP);
        mv.visitInsn(POP);
        mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_TYPE_IMPL, initializerFuncName,
                           String.format("(L%s;)V", ATTACHED_FUNCTION_IMPL), false);
    }

    /**
     * Create a attached function information for objects.
     *
     * @param mv           method visitor
     * @param attachedFunc object attached function
     * @param objType      object type used for creating the attached function
     */
    private static void createObjectAttachedFunction(MethodVisitor mv, BAttachedFunction attachedFunc,
                                                     BObjectType objType) {

        mv.visitTypeInsn(NEW, ATTACHED_FUNCTION_IMPL);
        mv.visitInsn(DUP);

        // Load function name
        mv.visitLdcInsn(decodeIdentifier(attachedFunc.funcName.value));

        // Load the parent object type
        loadType(mv, objType);
        mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE_IMPL);

        // Load the field type
        loadType(mv, attachedFunc.type);

        // Load flags
        mv.visitLdcInsn(attachedFunc.symbol.flags);

        mv.visitMethodInsn(INVOKESPECIAL, ATTACHED_FUNCTION_IMPL, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;L%s;I)V", STRING_VALUE, OBJECT_TYPE_IMPL, FUNCTION_TYPE_IMPL),
                           false);
    }

    // -------------------------------------------------------
    //              Error type generation methods
    // -------------------------------------------------------

    /**
     * Create a runtime type instance for the error.
     *
     * @param mv        method visitor
     * @param errorType error type
     * @param name      name of the error
     */
    private static void createErrorType(MethodVisitor mv, BErrorType errorType, String name) {
        // Create the error type
        mv.visitTypeInsn(NEW, ERROR_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load error type name
        mv.visitLdcInsn(name);

        // Load package
        mv.visitTypeInsn(NEW, MODULE);
        mv.visitInsn(DUP);
        PackageID packageID = errorType.tsymbol.pkgID;
        mv.visitLdcInsn(packageID.orgName.value);
        mv.visitLdcInsn(packageID.name.value);
        mv.visitLdcInsn(packageID.version.value);
        mv.visitMethodInsn(INVOKESPECIAL, MODULE, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);

        // initialize the error type
        mv.visitMethodInsn(INVOKESPECIAL, ERROR_TYPE_IMPL, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;)V", STRING_VALUE, MODULE), false);
    }

    // -------------------------------------------------------
    //              Type loading methods
    // -------------------------------------------------------

    /**
     * Generate code to load an instance of the given type
     * to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType type to load
     */
    static void loadType(MethodVisitor mv, BType bType) {
        String typeFieldName = "";
        if (bType == null || bType.tag == TypeTags.NIL) {
            typeFieldName = "TYPE_NULL";
        } else {
            switch (bType.tag) {
                case TypeTags.NEVER:
                    typeFieldName = "TYPE_NEVER";
                    break;
                case TypeTags.INT:
                    typeFieldName = "TYPE_INT";
                    break;
                case TypeTags.SIGNED32_INT:
                    typeFieldName = "TYPE_INT_SIGNED_32";
                    break;
                case TypeTags.SIGNED16_INT:
                    typeFieldName = "TYPE_INT_SIGNED_16";
                    break;
                case TypeTags.SIGNED8_INT:
                    typeFieldName = "TYPE_INT_SIGNED_8";
                    break;
                case TypeTags.UNSIGNED32_INT:
                    typeFieldName = "TYPE_INT_UNSIGNED_32";
                    break;
                case TypeTags.UNSIGNED16_INT:
                    typeFieldName = "TYPE_INT_UNSIGNED_16";
                    break;
                case TypeTags.UNSIGNED8_INT:
                    typeFieldName = "TYPE_INT_UNSIGNED_8";
                    break;
                case TypeTags.FLOAT:
                    typeFieldName = "TYPE_FLOAT";
                    break;
                case TypeTags.STRING:
                    typeFieldName = "TYPE_STRING";
                    break;
                case TypeTags.CHAR_STRING:
                    typeFieldName = "TYPE_STRING_CHAR";
                    break;
                case TypeTags.DECIMAL:
                    typeFieldName = "TYPE_DECIMAL";
                    break;
                case TypeTags.BOOLEAN:
                    typeFieldName = "TYPE_BOOLEAN";
                    break;
                case TypeTags.BYTE:
                    typeFieldName = "TYPE_BYTE";
                    break;
                case TypeTags.ANY:
                    typeFieldName = Symbols.isFlagOn(bType.flags, Flags.READONLY) ? "TYPE_READONLY_ANY" : "TYPE_ANY";
                    break;
                case TypeTags.ANYDATA:
                    typeFieldName = Symbols.isFlagOn(bType.flags, Flags.READONLY) ? "TYPE_READONLY_ANYDATA" :
                            "TYPE_ANYDATA";
                    break;
                case TypeTags.JSON:
                    typeFieldName = Symbols.isFlagOn(bType.flags, Flags.READONLY) ? "TYPE_READONLY_JSON" : "TYPE_JSON";
                    break;
                case TypeTags.XML:
                    loadXmlType(mv, (BXMLType) bType);
                    return;
                case TypeTags.XML_ELEMENT:
                    typeFieldName = Symbols.isFlagOn(bType.flags, Flags.READONLY) ? "TYPE_READONLY_ELEMENT" :
                            "TYPE_ELEMENT";
                    break;
                case TypeTags.XML_PI:
                    typeFieldName = Symbols.isFlagOn(bType.flags, Flags.READONLY) ?
                            "TYPE_READONLY_PROCESSING_INSTRUCTION" : "TYPE_PROCESSING_INSTRUCTION";
                    break;
                case TypeTags.XML_COMMENT:
                    typeFieldName = Symbols.isFlagOn(bType.flags, Flags.READONLY) ? "TYPE_READONLY_COMMENT" :
                            "TYPE_COMMENT";
                    break;
                case TypeTags.XML_TEXT:
                    typeFieldName = "TYPE_TEXT";
                    break;
                case TypeTags.TYPEDESC:
                    loadTypedescType(mv, (BTypedescType) bType);
                    return;
                case TypeTags.OBJECT:
                    if (bType instanceof BServiceType) {
                        if (!Objects.equals(getTypeFieldName(toNameString(bType)), "$type$service")) {
                            loadUserDefinedType(mv, bType);
                            return;
                        } else {
                            typeFieldName = "TYPE_ANY_SERVICE";
                        }
                    } else if (bType instanceof BObjectType) {
                        loadUserDefinedType(mv, bType);
                        return;
                    }
                    break;
                case TypeTags.HANDLE:
                    typeFieldName = "TYPE_HANDLE";
                    break;
                case TypeTags.ARRAY:
                    loadArrayType(mv, (BArrayType) bType);
                    return;
                case TypeTags.MAP:
                    loadMapType(mv, (BMapType) bType);
                    return;
                case TypeTags.STREAM:
                    loadStreamType(mv, (BStreamType) bType);
                    return;
                case TypeTags.TABLE:
                    loadTableType(mv, (BTableType) bType);
                    return;
                case TypeTags.ERROR:
                    loadErrorType(mv, (BErrorType) bType);
                    return;
                case TypeTags.UNION:
                    loadUnionType(mv, (BUnionType) bType);
                    return;
                case TypeTags.INTERSECTION:
                    loadIntersectionType(mv, (BIntersectionType) bType);
                    return;
                case TypeTags.RECORD:
                    loadUserDefinedType(mv, bType);
                    return;
                case TypeTags.INVOKABLE:
                    loadInvokableType(mv, (BInvokableType) bType);
                    return;
                case TypeTags.NONE:
                    mv.visitInsn(ACONST_NULL);
                    return;
                case TypeTags.TUPLE:
                    loadTupleType(mv, (BTupleType) bType);
                    return;
                case TypeTags.FINITE:
                    loadFiniteType(mv, (BFiniteType) bType);
                    return;
                case TypeTags.FUTURE:
                    loadFutureType(mv, (BFutureType) bType);
                    return;
                case TypeTags.READONLY:
                    typeFieldName = "TYPE_READONLY";
                    break;
                default:
                    return;
            }
        }

        mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, typeFieldName, String.format("L%s;", loadTypeClass(bType)));
    }

    private static String loadTypeClass(BType bType) {
        if (bType == null || bType.tag == TypeTags.NIL) {
            return NULL_TYPE;
        } else {
            switch (bType.tag) {
                case TypeTags.NEVER:
                    return NEVER_TYPE;
                case TypeTags.INT:
                    return INTEGER_TYPE;
                case TypeTags.SIGNED32_INT:
                    return INTEGER_TYPE;
                case TypeTags.SIGNED16_INT:
                    return INTEGER_TYPE;
                case TypeTags.SIGNED8_INT:
                    return INTEGER_TYPE;
                case TypeTags.UNSIGNED32_INT:
                    return INTEGER_TYPE;
                case TypeTags.UNSIGNED16_INT:
                    return INTEGER_TYPE;
                case TypeTags.UNSIGNED8_INT:
                    return INTEGER_TYPE;
                case TypeTags.FLOAT:
                    return FLOAT_TYPE;
                case TypeTags.STRING:
                    return STRING_TYPE;
                case TypeTags.CHAR_STRING:
                    return STRING_TYPE;
                case TypeTags.DECIMAL:
                    return DECIMAL_TYPE;
                case TypeTags.BOOLEAN:
                    return BOOLEAN_TYPE;
                case TypeTags.BYTE:
                    return BYTE_TYPE;
                case TypeTags.ANY:
                    return ANY_TYPE;
                case TypeTags.ANYDATA:
                    return ANYDATA_TYPE;
                case TypeTags.JSON:
                    return JSON_TYPE;
                case TypeTags.XML:
                    return XML_TYPE;
                case TypeTags.XML_ELEMENT:
                    return Symbols.isFlagOn(bType.flags, Flags.READONLY) ? TYPE : XML_TYPE;
                case TypeTags.XML_PI:
                    return Symbols.isFlagOn(bType.flags, Flags.READONLY) ? TYPE : XML_TYPE;
                case TypeTags.XML_COMMENT:
                    return Symbols.isFlagOn(bType.flags, Flags.READONLY) ? TYPE : XML_TYPE;
                case TypeTags.XML_TEXT:
                    return XML_TYPE;
                case TypeTags.OBJECT:
                    return (bType instanceof BServiceType) ? SERVICE_TYPE : OBJECT_TYPE;
                case TypeTags.HANDLE:
                    return HANDLE_TYPE;
                case TypeTags.READONLY:
                    return READONLY_TYPE;
                default:
                    return TYPE;
            }
        }
    }

    /**
     * Generate code to load an instance of the given array type
     * to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType array type to load
     */
    private static void loadArrayType(MethodVisitor mv, BArrayType bType) {
        // Create an new array type
        mv.visitTypeInsn(NEW, ARRAY_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load the element type
        loadType(mv, bType.eType);

        int arraySize = bType.size;
        mv.visitLdcInsn((long) arraySize);
        mv.visitInsn(L2I);

        loadReadonlyFlag(mv, bType);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, ARRAY_TYPE_IMPL, JVM_INIT_METHOD, String.format("(L%s;IZ)V", TYPE), false);
    }

    /**
     * Generate code to load an instance of the given typedesc type
     * to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType typedesc type to load
     */
    private static void loadTypedescType(MethodVisitor mv, BTypedescType bType) {
        // Create an new map type
        mv.visitTypeInsn(NEW, TYPEDESC_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load the constraint type
        loadType(mv, bType.constraint);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, TYPEDESC_TYPE_IMPL, JVM_INIT_METHOD, String.format("(L%s;)V", TYPE), false);
    }

    /**
     * Generate code to load an instance of the given map type
     * to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType map type to load
     */
    private static void loadMapType(MethodVisitor mv, BMapType bType) {
        // Create an new map type
        mv.visitTypeInsn(NEW, MAP_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load the constraint type
        loadType(mv, bType.constraint);

        loadReadonlyFlag(mv, bType);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, MAP_TYPE_IMPL, JVM_INIT_METHOD, String.format("(L%s;Z)V", TYPE), false);
    }

    private static void loadReadonlyFlag(MethodVisitor mv, BType bType) {
        if (Symbols.isFlagOn(bType.flags, Flags.READONLY)) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }
    }

    /**
     * Generate code to load an instance of the given xml sequence type
     * to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType xml type to load
     */
    private static void loadXmlType(MethodVisitor mv, BXMLType bType) {
        // Create an new xml type
        mv.visitTypeInsn(NEW, XML_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load the constraint type
        loadType(mv, bType.constraint);

        loadReadonlyFlag(mv, bType);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, XML_TYPE_IMPL, JVM_INIT_METHOD, String.format("(L%s;Z)V", TYPE), false);
    }

    /**
     * Generate code to load an instance of the given table type
     * to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType table type to load
     */
    private static void loadTableType(MethodVisitor mv, BTableType bType) {
        // Create an new table type
        mv.visitTypeInsn(NEW, TABLE_TYPE_IMPL);
        mv.visitInsn(DUP);

        loadType(mv, bType.constraint);
        if (bType.fieldNameList != null) {
            // Create the field names array
            List<String> fieldNames = bType.fieldNameList;
            mv.visitLdcInsn((long) fieldNames.size());
            mv.visitInsn(L2I);
            mv.visitTypeInsn(ANEWARRAY, STRING_VALUE);
            int i = 0;
            for (String fieldName : fieldNames) {

                mv.visitInsn(DUP);
                mv.visitLdcInsn((long) i);
                mv.visitInsn(L2I);
                mv.visitLdcInsn(fieldName);
                mv.visitInsn(AASTORE);
                i += 1;
            }

            loadReadonlyFlag(mv, bType);
            mv.visitMethodInsn(INVOKESPECIAL, TABLE_TYPE_IMPL, JVM_INIT_METHOD,
                               String.format("(L%s;[L%s;Z)V", TYPE, STRING_VALUE), false);
        } else if (bType.keyTypeConstraint != null) {
            loadType(mv, bType.keyTypeConstraint);
            loadReadonlyFlag(mv, bType);
            mv.visitMethodInsn(INVOKESPECIAL, TABLE_TYPE_IMPL, JVM_INIT_METHOD,
                               String.format("(L%s;L%s;Z)V", TYPE, TYPE), false);
        } else {
            loadReadonlyFlag(mv, bType);
            mv.visitMethodInsn(INVOKESPECIAL, TABLE_TYPE_IMPL, JVM_INIT_METHOD, String.format("(L%s;Z)V", TYPE), false);
        }
    }

    private static void loadStreamType(MethodVisitor mv, BStreamType bType) {
        // Create an new stream type
        mv.visitTypeInsn(NEW, STREAM_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load the constraint type
        loadType(mv, bType.constraint);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, STREAM_TYPE_IMPL, JVM_INIT_METHOD, String.format("(L%s;)V", TYPE), false);
    }

    /**
     * Generate code to load an instance of the given error type
     * to the top of the stack.
     *
     * @param mv        method visitor
     * @param errorType error type to load
     */
    private static void loadErrorType(MethodVisitor mv, BErrorType errorType) {

        PackageID packageID = errorType.tsymbol.pkgID;
        // TODO: Builtin error type will be loaded from BTypes java class. Need to handle this properly.
        if (packageID.orgName.value.equals(BALLERINA) && packageID.name.value.equals(BUILT_IN_PACKAGE_NAME)) {
            mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, TYPES_ERROR, String.format("L%s;", ERROR_TYPE));
            return;
        }
        String typeOwner =
                JvmCodeGenUtil.getPackageName(packageID) + MODULE_INIT_CLASS_NAME;
        String fieldName = getTypeFieldName(toNameString(errorType));
        mv.visitFieldInsn(GETSTATIC, typeOwner, fieldName, String.format("L%s;", TYPE));
    }

    /**
     * Generate code to load an instance of the given union type
     * to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType union type to load
     */
    private static void loadUnionType(MethodVisitor mv, BUnionType bType) {
        // Create the union type
        mv.visitTypeInsn(NEW, UNION_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Create the members array
        Set<BType> memberTypes = bType.getMemberTypes();
        mv.visitLdcInsn((long) memberTypes.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, TYPE);
        int i = 0;
        for (BType memberType : memberTypes) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) i);
            mv.visitInsn(L2I);

            // Load the member type
            loadType(mv, memberType);

            // Add the member to the array
            mv.visitInsn(AASTORE);
            i += 1;
        }

        // Load type flags
        mv.visitLdcInsn(typeFlag(bType));

        loadReadonlyFlag(mv, bType);

        // initialize the union type using the members array
        mv.visitMethodInsn(INVOKESPECIAL, UNION_TYPE_IMPL, JVM_INIT_METHOD, String.format("([L%s;IZ)V", TYPE), false);
    }

    /**
     * Generate code to load an instance of the given intersection type to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType intersection type to load
     */
    private static void loadIntersectionType(MethodVisitor mv, BIntersectionType bType) {
        // Create the intersection type
        mv.visitTypeInsn(NEW, INTERSECTION_TYPE_IMPL);
        mv.visitInsn(DUP);

        mv.visitTypeInsn(NEW, MODULE);
        mv.visitInsn(DUP);

        PackageID packageID = bType.tsymbol.pkgID;

        mv.visitLdcInsn(packageID.orgName.value);
        mv.visitLdcInsn(packageID.name.value);
        mv.visitLdcInsn(packageID.version.value);
        mv.visitMethodInsn(INVOKESPECIAL, MODULE, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);

        // Create the constituent types array.
        Set<BType> constituentTypes = bType.getConstituentTypes();
        mv.visitLdcInsn((long) constituentTypes.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, TYPE);
        int i = 0;
        for (BType memberType : constituentTypes) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) i);
            mv.visitInsn(L2I);

            // Load the member type.
            loadType(mv, memberType);

            // Add the member to the array.
            mv.visitInsn(AASTORE);
            i += 1;
        }

        // Load the effective type of the intersection.
        loadType(mv, bType.effectiveType);

        // Load type flags.
        mv.visitLdcInsn(typeFlag(bType));

        loadReadonlyFlag(mv, bType);

        mv.visitMethodInsn(INVOKESPECIAL, INTERSECTION_TYPE_IMPL, JVM_INIT_METHOD,
                           String.format("(L%s;[L%s;L%s;IZ)V", MODULE, TYPE, TYPE), false);
    }

    /**
     * Load a Tuple type instance to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType tuple type to be loaded
     */
    private static void loadTupleType(MethodVisitor mv, BTupleType bType) {

        mv.visitTypeInsn(NEW, TUPLE_TYPE_IMPL);
        mv.visitInsn(DUP);
        //new arraylist
        mv.visitTypeInsn(NEW, ARRAY_LIST);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, ARRAY_LIST, JVM_INIT_METHOD, "()V", false);

        List<BType> tupleTypes = bType.tupleTypes;
        for (BType tupleType : tupleTypes) {
            mv.visitInsn(DUP);
            loadType(mv, tupleType);
            mv.visitMethodInsn(INVOKEINTERFACE, LIST, "add", String.format("(L%s;)Z", OBJECT), true);
            mv.visitInsn(POP);
        }

        BType restType = bType.restType;
        if (restType == null) {
            mv.visitInsn(ACONST_NULL);
        } else {
            loadType(mv, restType);
        }

        // Load type flags
        mv.visitLdcInsn(typeFlag(bType));

        loadReadonlyFlag(mv, bType);

        mv.visitMethodInsn(INVOKESPECIAL, TUPLE_TYPE_IMPL, JVM_INIT_METHOD, String.format("(L%s;L%s;IZ)V", LIST, TYPE),
                           false);
    }

    /**
     * Load a user defined type instance to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType user defined type
     */
    private static void loadUserDefinedType(MethodVisitor mv, BType bType) {

        PackageID packageID = bType.tsymbol.pkgID;

        String typeOwner = JvmCodeGenUtil.getPackageName(packageID) + MODULE_INIT_CLASS_NAME;
        String fieldName = getTypeFieldName(toNameString(bType));

        mv.visitFieldInsn(GETSTATIC, typeOwner, fieldName, String.format("L%s;", TYPE));
    }

    /**
     * Return the name of the field that holds the instance of a given type.
     *
     * @param typeName type name
     * @return name of the field that holds the type instance
     */
    private static String getTypeFieldName(String typeName) {

        return String.format("$type$%s", JvmCodeGenUtil.cleanupReadOnlyTypeName(typeName));
    }

    private static void loadFutureType(MethodVisitor mv, BFutureType bType) {

        mv.visitTypeInsn(NEW, FUTURE_TYPE_IMPL);
        mv.visitInsn(DUP);

        loadType(mv, bType.constraint);
        mv.visitMethodInsn(INVOKESPECIAL, FUTURE_TYPE_IMPL, JVM_INIT_METHOD, String.format("(L%s;)V", TYPE), false);
    }

    /**
     * Create and load an invokable type.
     *
     * @param mv    method visitor
     * @param bType invokable type to be created
     */
    private static void loadInvokableType(MethodVisitor mv, BInvokableType bType) {

        mv.visitTypeInsn(NEW, FUNCTION_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Create param types array
        mv.visitLdcInsn((long) bType.paramTypes.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, TYPE);
        int i = 0;
        for (BType paramType : bType.paramTypes) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) i);
            mv.visitInsn(L2I);

            // load param type
            loadType(mv, paramType);

            // Add the member to the array
            mv.visitInsn(AASTORE);
            i += 1;
        }

        BType restType = bType.restType;
        if (restType == null) {
            mv.visitInsn(ACONST_NULL);
        } else {
            loadType(mv, restType);
        }

        BType retType;
        if (Symbols.isFlagOn(bType.retType.flags, Flags.PARAMETERIZED)) {
            retType = typeBuilder.build(bType.retType);
        } else {
            retType = bType.retType;
        }

        // load return type type
        loadType(mv, retType);

        mv.visitLdcInsn(bType.flags);

        // initialize the function type using the param types array and the return type
        mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_TYPE_IMPL, JVM_INIT_METHOD,
                           String.format("([L%s;L%s;L%s;I)V", TYPE, TYPE, TYPE), false);
    }

    static String getTypeDesc(BType bType) {

        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            return "J";
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
            return String.format("L%s;", B_STRING_VALUE);
        } else if (TypeTags.isXMLTypeTag(bType.tag)) {
            return String.format("L%s;", XML_VALUE);
        }

        switch (bType.tag) {
            case TypeTags.BYTE:
                return "I";
            case TypeTags.FLOAT:
                return "D";
            case TypeTags.BOOLEAN:
                return "Z";
            case TypeTags.NIL:
            case TypeTags.NEVER:
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.INTERSECTION:
            case TypeTags.JSON:
            case TypeTags.FINITE:
            case TypeTags.READONLY:
                return String.format("L%s;", OBJECT);
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                return String.format("L%s;", ARRAY_VALUE);
            case TypeTags.ERROR:
                return String.format("L%s;", ERROR_VALUE);
            case TypeTags.FUTURE:
                return String.format("L%s;", FUTURE_VALUE);
            case TypeTags.MAP:
            case TypeTags.RECORD:
                return String.format("L%s;", MAP_VALUE);
            case TypeTags.TYPEDESC:
                return String.format("L%s;", TYPEDESC_VALUE);
            case TypeTags.STREAM:
                return String.format("L%s;", STREAM_VALUE);
            case TypeTags.TABLE:
                return String.format("L%s;", TABLE_VALUE_IMPL);
            case TypeTags.DECIMAL:
                return String.format("L%s;", DECIMAL_VALUE);
            case TypeTags.OBJECT:
                return String.format("L%s;", B_OBJECT);
            case TypeTags.HANDLE:
                return String.format("L%s;", HANDLE_VALUE);
            case TypeTags.INVOKABLE:
                return String.format("L%s;", FUNCTION_POINTER);
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE + bType);
        }
    }

    private static void loadFiniteType(MethodVisitor mv, BFiniteType finiteType) {

        mv.visitTypeInsn(NEW, FINITE_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load type name
        String name = toNameString(finiteType);
        mv.visitLdcInsn(name);

        mv.visitTypeInsn(NEW, LINKED_HASH_SET);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_SET, JVM_INIT_METHOD, "()V", false);

        for (BLangExpression valueTypePair : finiteType.getValueSpace()) {
            Object value = ((BLangLiteral) valueTypePair).value;
            BType valueType = valueTypePair.type;
            mv.visitInsn(DUP);

            loadConstantValue(valueType, value, mv);

            if (TypeTags.isIntegerTypeTag(valueType.tag)) {
                mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, VALUE_OF_METHOD, String.format("(J)L%s;", LONG_VALUE),
                        false);
            } else {
                switch (valueType.tag) {
                    case TypeTags.BOOLEAN:
                        mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, VALUE_OF_METHOD,
                                String.format("(Z)L%s;", BOOLEAN_VALUE), false);
                        break;
                    case TypeTags.FLOAT:
                        mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, VALUE_OF_METHOD,
                                String.format("(D)L%s;", DOUBLE_VALUE), false);
                        break;
                    case TypeTags.BYTE:
                        mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, VALUE_OF_METHOD,
                                String.format("(I)L%s;", INT_VALUE), false);
                        break;
                }
            }

            // Add the value to the set
            mv.visitMethodInsn(INVOKEINTERFACE, SET, "add", String.format("(L%s;)Z", OBJECT), true);
            mv.visitInsn(POP);
        }

        // Load type flags
        mv.visitLdcInsn(typeFlag(finiteType));

        // initialize the finite type using the value space
        mv.visitMethodInsn(INVOKESPECIAL, FINITE_TYPE_IMPL, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;I)V", STRING_VALUE, SET), false);
    }

    static boolean isServiceDefAvailable(List<BIRTypeDefinition> typeDefs) {

        for (BIRTypeDefinition optionalTypeDef : typeDefs) {
            BType bType = optionalTypeDef.type;
            if (bType instanceof BServiceType) {
                return true;
            }
        }
        return false;
    }

    private JvmTypeGen() {
    }
}
