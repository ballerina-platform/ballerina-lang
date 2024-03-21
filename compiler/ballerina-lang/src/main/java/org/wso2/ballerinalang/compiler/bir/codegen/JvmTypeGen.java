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

import io.ballerina.identifier.Utils;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.Core;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.subtypedata.AllOrNothingSubtype;
import io.ballerina.types.subtypedata.BooleanSubtype;
import io.ballerina.types.subtypedata.CharStringSubtype;
import io.ballerina.types.subtypedata.DecimalSubtype;
import io.ballerina.types.subtypedata.FloatSubtype;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.NonCharStringSubtype;
import io.ballerina.types.subtypedata.Range;
import io.ballerina.types.subtypedata.StringSubtype;
import io.ballerina.types.subtypedata.XmlSubtype;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.semantics.analyzer.IsAnydataUniqueVisitor;
import org.wso2.ballerinalang.compiler.semantics.analyzer.IsPureTypeUniqueVisitor;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeHashVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleMember;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.semantics.model.types.SemNamedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.TypeFlags;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static io.ballerina.types.Core.getComplexSubtypeData;
import static io.ballerina.types.SemTypes.isSubtypeSimple;
import static io.ballerina.types.BasicTypeCode.BT_BOOLEAN;
import static io.ballerina.types.BasicTypeCode.BT_DECIMAL;
import static io.ballerina.types.BasicTypeCode.BT_FLOAT;
import static io.ballerina.types.BasicTypeCode.BT_INT;
import static io.ballerina.types.BasicTypeCode.BT_STRING;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getStringConstantsClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ADD_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BINARY_TYPE_OPERATION_DESCRIPTOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BINARY_TYPE_OPERATION_WITH_IDENTIFIER_DESCRIPTOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BOOLEAN_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CALL_FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_RECORD_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DOUBLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FINITE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_PARAMETER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_ANON_TYPE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INTERSECTION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LIST_SUBTYPE_BUILDER_DESCRIPTOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LONG_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ANON_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ERRORS_CREATOR_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_FUNCTION_CALLS_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_OBJECTS_CREATOR_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_RECORDS_CREATOR_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PARAMETERIZED_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PREDEFINED_TYPES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STREAM_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_SUBTYPE_BUILDER_DESCRIPTOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_SUBTYPE_DATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_SUBTYPE_DATA_BUILDER_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TO_SEMTYPE_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPES_ERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_BUILDER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_BUILDER_IDENTIFIER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_BUILDER_INIT_DESCRIPTOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_OF_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_SIMPLE_SUBTYPE_BUILDER_DESCRIPTOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_SUBTYPE_BUILDER_DESCRIPTOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ANY_TO_JBOOLEAN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.BOOLEAN_VALUE_OF_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CREATE_ERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CREATE_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CREATE_RECORD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CREATE_RECORD_WITH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.DOUBLE_VALUE_OF_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.FUNCTION_CALL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_BDECIMAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_BOBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_BSTRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_REGEXP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STREAM_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TABLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPEDESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPE_REF_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_XML;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_FINITE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_FUNCTION_PARAM;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_FUNCTION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_FUNCTION_TYPE_IMPL_WITH_PARAMS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_INTERSECTION_TYPE_WITH_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_PARAMETERIZED_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_STREAM_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_TABLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_TABLE_TYPE_WITH_FIELD_NAME_LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_WITH_BOOLEAN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_WITH_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INT_VALUE_OF_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LONG_VALUE_OF;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_TYPE_ARRAY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TYPE_PARAMETER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;

/**
 * BIR types to JVM byte code generation class.
 *
 * @since 1.2.0
 */
public class JvmTypeGen {

    private final IsPureTypeUniqueVisitor isPureTypeUniqueVisitor;
    private final IsAnydataUniqueVisitor isAnydataUniqueVisitor;
    private final JvmConstantsGen jvmConstantsGen;
    private final TypeHashVisitor typeHashVisitor;
    private final SymbolTable symbolTable;
    private final PackageID packageID;
    private final String anonTypesClass;
    private final String recordsClass;
    private final String objectsClass;
    private final String errorsClass;
    private final String functionCallsClass;

    public JvmTypeGen(JvmConstantsGen jvmConstantsGen, PackageID packageID, TypeHashVisitor typeHashVisitor,
                      SymbolTable symbolTable) {
        this.jvmConstantsGen = jvmConstantsGen;
        this.packageID = packageID;
        isPureTypeUniqueVisitor = new IsPureTypeUniqueVisitor();
        isAnydataUniqueVisitor = new IsAnydataUniqueVisitor();
        this.typeHashVisitor = typeHashVisitor;
        this.symbolTable = symbolTable;
        this.anonTypesClass = getModuleLevelClassName(packageID, MODULE_ANON_TYPES_CLASS_NAME);
        this.recordsClass = getModuleLevelClassName(packageID, MODULE_RECORDS_CREATOR_CLASS_NAME);
        this.objectsClass = getModuleLevelClassName(packageID, MODULE_OBJECTS_CREATOR_CLASS_NAME);
        this.errorsClass = getModuleLevelClassName(packageID, MODULE_ERRORS_CREATOR_CLASS_NAME);
        this.functionCallsClass = getModuleLevelClassName(packageID, MODULE_FUNCTION_CALLS_CLASS_NAME);
    }

    /**
     * Create static fields to hold the user defined types.
     *
     * @param cw       class writer
     * @param typeDefs array of type definitions
     */
    void generateUserDefinedTypeFields(ClassWriter cw, List<BIRTypeDefinition> typeDefs) {
        // create the type
        for (BIRTypeDefinition typeDef : typeDefs) {
            BType bType = typeDef.type;
            int bTypeTag = bType.tag;
            if (JvmCodeGenUtil.needNoTypeGeneration(bTypeTag)) {
                // do not generate anything for other types (e.g.: finite type, type reference types etc.)
                continue;
            }
            String name = typeDef.internalName.value;
            generateTypeField(cw, name);
            generateTypedescField(cw, name);
        }
    }

    private void generateTypeField(ClassWriter cw, String name) {
        String fieldName = getTypeFieldName(name);
        FieldVisitor fv = cw.visitField(ACC_STATIC + ACC_PUBLIC, fieldName, GET_TYPE, null, null);
        fv.visitEnd();
    }

    private void generateTypedescField(ClassWriter cw, String name) {
        String typedescFieldName = getTypedescFieldName(name);
        FieldVisitor fvTypeDesc = cw.visitField(ACC_STATIC + ACC_PUBLIC, typedescFieldName, GET_TYPEDESC, null, null);
        fvTypeDesc.visitEnd();
    }

    // -------------------------------------------------------
    //              getAnonType() generation methods
    // -------------------------------------------------------

    void generateGetAnonTypeMethod(ClassWriter cw, String moduleClass) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, GET_ANON_TYPE_METHOD,
                JvmSignatures.GET_ANON_TYPE, null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKESTATIC, anonTypesClass, GET_ANON_TYPE_METHOD, JvmSignatures.GET_ANON_TYPE, false);
        mv.visitInsn(ARETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, GET_ANON_TYPE_METHOD, moduleClass);
        mv.visitEnd();
    }

    // -------------------------------------------------------
    //              Runtime value creation methods
    // -------------------------------------------------------

    void generateValueCreatorMethods(ClassWriter cw, String moduleClass) {
        generateRecordValueCreateMethod(cw, moduleClass);
        generateObjectValueCreateMethod(cw, moduleClass);
        generateErrorValueCreateMethod(cw, moduleClass);
        generateFunctionCallMethod(cw, moduleClass);
    }

    private void generateRecordValueCreateMethod(ClassWriter cw, String moduleClass) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, CREATE_RECORD_VALUE,
                CREATE_RECORD,
                CREATE_RECORD_WITH_MAP, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, recordsClass, CREATE_RECORD_VALUE, CREATE_RECORD, false);
        mv.visitInsn(ARETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, CREATE_RECORD_VALUE, moduleClass);
        mv.visitEnd();
    }

    private void generateObjectValueCreateMethod(ClassWriter cw, String moduleClass) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, CREATE_OBJECT_VALUE, CREATE_OBJECT, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitVarInsn(ALOAD, 5);
        mv.visitMethodInsn(INVOKESTATIC, objectsClass, CREATE_OBJECT_VALUE, CREATE_OBJECT, false);
        mv.visitInsn(ARETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, CREATE_OBJECT_VALUE, moduleClass);
        mv.visitEnd();
    }

    private void generateErrorValueCreateMethod(ClassWriter cw, String moduleClass) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, CREATE_ERROR_VALUE,
                CREATE_ERROR, null,
                null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKESTATIC, errorsClass, CREATE_ERROR_VALUE, CREATE_ERROR, false);
        mv.visitInsn(ARETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, CREATE_ERROR_VALUE, moduleClass);
        mv.visitEnd();
    }

    private void generateFunctionCallMethod(ClassWriter cw, String moduleClass) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, CALL_FUNCTION, FUNCTION_CALL, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKESTATIC, functionCallsClass, CALL_FUNCTION, FUNCTION_CALL, false);
        mv.visitInsn(ARETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, CALL_FUNCTION, moduleClass);
        mv.visitEnd();
    }

    public int typeFlag(BType type) {
        isAnydataUniqueVisitor.reset();
        isPureTypeUniqueVisitor.reset();
        return TypeFlags.asMask(type.isNullable(), isAnydataUniqueVisitor.visit(type),
                isPureTypeUniqueVisitor.visit(type));
    }

    // -------------------------------------------------------
    //              Type loading methods
    // -------------------------------------------------------

    // TODO: as we implement more and more runtime types using semtypes update TypeBuilder and this to create
    //   those types using the new API. (When eventually this method handles all types replace loadType with this)
    public void loadTypeUsingTypeBuilder(MethodVisitor mv, BType type) {
        if (type == null || type.tag == TypeTags.NIL) {
            mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, "TYPE_NULL", GET_TYPE);
            return;
        }
        // NOTE: don't fully remove this until we unconditionally support xml
        if (isXmlSubType(type)) {
            loadXmlSubType(mv, type);
            return;
        }
        if (isMayBeReadonlyType(type)) {
            loadMayBeReadonlyTypeUsingTypeBuilder(mv, type);
            return;
        }
        if (isIntSubType(type)) {
            loadIntSubTypeUsingTypeBuilder(mv, type);
            return;
        }
        int tag = type.tag;
        if (tag == TypeTags.INTERSECTION) {
            loadIntersectionTypeUsingTypeBuilder(mv, type);
            return;
        }
        if (tag == TypeTags.UNION) {
            loadUnionTypeUsingTypeBuilder(mv, type);
            return;
        }
        if (tag == TypeTags.FINITE) {
            loadFiniteTypeUsingTypeBuilder(mv, type);
            return;
        }
        String fieldName = "TYPE_" + switch (tag) {
            case TypeTags.BOOLEAN -> "BOOLEAN";
            case TypeTags.DECIMAL -> "DECIMAL";
            case TypeTags.FLOAT -> "FLOAT";
            case TypeTags.INT -> "INT";
            case TypeTags.NEVER -> "NEVER";
            case TypeTags.STRING -> "STRING";
            case TypeTags.XML -> "XML";
            case TypeTags.ERROR -> "ERROR";
            case TypeTags.CHAR_STRING -> "STRING_CHAR";
            default -> throw new UnsupportedOperationException("Unexpected type: " + type);
        };
        mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, fieldName, GET_TYPE);
    }

    private static void loadFiniteTypeUsingTypeBuilder(MethodVisitor mv, BType type) {
        BFiniteType finiteType = (BFiniteType) type;
        int numberOfTypesOnStack = 0;
        for (int i = 0; i < finiteType.valueSpace.length; i++) {
            SemType semType = finiteType.valueSpace[i].semType();
            if (Core.isSubtypeSimple(semType, PredefinedType.STRING)) {
                SubtypeData subtypeData = Core.stringSubtype(semType);
                // TODO: create other type singletons here as well
                if (subtypeData instanceof StringSubtype stringSubtype) {
                    CharStringSubtype chars = stringSubtype.getChar();
                    NonCharStringSubtype nonChars = stringSubtype.getNonChar();
                    loadStringSubTypeUsingTypeBuilder(
                            mv,
                            chars.allowed,
                            Arrays.stream(chars.values).map(each -> each.value).toList(),
                            nonChars.allowed,
                            Arrays.stream(nonChars.values).map(each -> each.value).toList());
                    numberOfTypesOnStack++;
                } else if (subtypeData instanceof AllOrNothingSubtype allOrNothing) {
                    loadAllOrNothingType(mv, allOrNothing, "TYPE_STRING");
                } else {
                    throw new IllegalStateException("Unexpected string subtype data " + subtypeData);
                }
            } else if (Core.isSubtypeSimple(semType, PredefinedType.INT)) {
                SubtypeData subtypeData = Core.intSubtype(semType);
                if (subtypeData instanceof IntSubtype intSubType) {
                    for (Range range : intSubType.ranges) {
                        loadIntSubTypeUsingTypeBuilder(mv, range.min, range.max);
                        numberOfTypesOnStack++;
                    }
                } else if (subtypeData instanceof AllOrNothingSubtype allOrNothing) {
                    loadAllOrNothingType(mv, allOrNothing, "TYPE_INT");
                } else {
                    throw new IllegalStateException("Unexpected int subtype data " + subtypeData);
                }
            } else {
                throw new IllegalStateException("Unexpected finite type value type " + semType);
            }
        }
        boolean needToSetIdentifier = hasIdentifier(type);
        while (numberOfTypesOnStack > 1) {
            if (needToSetIdentifier && numberOfTypesOnStack == 2) {
                loadTypeBuilderIdentifier(mv, type);
                mv.visitMethodInsn(INVOKESTATIC, TYPE_BUILDER, "union",
                        BINARY_TYPE_OPERATION_WITH_IDENTIFIER_DESCRIPTOR, false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, TYPE_BUILDER, "union", BINARY_TYPE_OPERATION_DESCRIPTOR, false);
            }
            numberOfTypesOnStack--;
        }
    }

    private static void loadAllOrNothingType(MethodVisitor mv, AllOrNothingSubtype allOrNothing, String allTypeName) {
        if (allOrNothing.isAllSubtype()) {
            mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, allTypeName, GET_TYPE);
        } else {
            mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, "TYPE_NEVER", GET_TYPE);
        }
    }

    private void loadIntersectionTypeUsingTypeBuilder(MethodVisitor mv, BType type) {
        BIntersectionType intersectionType = (BIntersectionType) type;
        BType effectiveType = intersectionType.effectiveType;
        loadTypeUsingTypeBuilder(mv, effectiveType);
    }

    private void loadUnionTypeUsingTypeBuilder(MethodVisitor mv, BType type) {
        jvmConstantsGen.generateGetSemType(mv, jvmConstantsGen.getSemTypeConstantsVar(type));
    }

    public static boolean hasIdentifier(BType type) {
        return type.tsymbol != null && (type.name != null || type.tsymbol.name != null);
    }

    public static void loadTypeBuilderIdentifier(MethodVisitor mv, BType type) {
        PackageID packageID = type.tsymbol.pkgID;
        String org = packageID.orgName.value;
        String pkgName = packageID.pkgName.value;
        String version = packageID.version.value;
        String name = type.tsymbol.name != null ? Utils.decodeIdentifier(type.tsymbol.name.getValue()) :
                Utils.decodeIdentifier(type.name.getValue());
        mv.visitTypeInsn(NEW, TYPE_BUILDER_IDENTIFIER);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(name);
        mv.visitLdcInsn(org);
        mv.visitLdcInsn(pkgName);
        mv.visitLdcInsn(version);
        mv.visitMethodInsn(INVOKESPECIAL, TYPE_BUILDER_IDENTIFIER, JVM_INIT_METHOD, TYPE_BUILDER_INIT_DESCRIPTOR,
                false);
    }

    private static boolean isXmlSubType(BType type) {
        return switch (type.tag) {
            case TypeTags.XML_ELEMENT, TypeTags.XML_PI, TypeTags.XML_COMMENT, TypeTags.XML_TEXT -> true;
            case TypeTags.XML -> ((BXMLType) type).constraint != null;
            default -> false;
        };
    }

    private void loadXmlSubType(MethodVisitor mv, BType type) {
        if (type instanceof BXMLType xmlType && xmlType.constraint != null) {
            loadComplexXmlSubType(mv, xmlType);
            return;
        }
        int primitives = xmlPrimitiveFlag(type);
        mv.visitLdcInsn(primitives);
        mv.visitMethodInsn(INVOKESTATIC, TYPE_BUILDER, "xmlSimpleSubType", XML_SIMPLE_SUBTYPE_BUILDER_DESCRIPTOR,
                false);
    }

    private void loadComplexXmlSubType(MethodVisitor mv, BXMLType type) {
        loadTypeUsingTypeBuilder(mv, type.constraint);
        mv.visitMethodInsn(INVOKESTATIC, TYPE_BUILDER, "xmlSubType", XML_SUBTYPE_BUILDER_DESCRIPTOR, false);
    }

    private static int xmlPrimitiveFlag(BType type) {
        return switch (type.tag) {
            case TypeTags.XML_ELEMENT ->
                    Symbols.isFlagOn(type.flags, Flags.READONLY) ? XmlSubtype.XML_PRIMITIVE_ELEMENT_RO
                            : XmlSubtype.XML_PRIMITIVE_ELEMENT_RW;
            case TypeTags.XML_PI -> Symbols.isFlagOn(type.flags, Flags.READONLY) ? XmlSubtype.XML_PRIMITIVE_PI_RO
                    : XmlSubtype.XML_PRIMITIVE_PI_RW;
            case TypeTags.XML_COMMENT ->
                    Symbols.isFlagOn(type.flags, Flags.READONLY) ? XmlSubtype.XML_PRIMITIVE_COMMENT_RO
                            : XmlSubtype.XML_PRIMITIVE_COMMENT_RW;
            case TypeTags.XML_TEXT -> XmlSubtype.XML_PRIMITIVE_TEXT;
            default -> throw new UnsupportedOperationException("Unexpected XML subtype" + type);
        };
    }

    private static boolean isIntSubType(BType type) {
        return type.tag == TypeTags.BYTE || (type.tag >= TypeTags.SIGNED32_INT && type.tag <= TypeTags.UNSIGNED8_INT);
    }

    private void loadStructureTypeUsingTypeBuilder(MethodVisitor mv, BType type) {
        switch (type.tag) {
            case TypeTags.ARRAY -> loadArraySubTypeUsingTypeBuilder(mv, type);
            case TypeTags.TUPLE -> loadTupleTypeUsingTypeBuilder(mv, type);
            default -> throw new UnsupportedOperationException("Unexpected structure type: " + type);
        }
    }

    private void loadTupleTypeUsingTypeBuilder(MethodVisitor mv, BType type) {
        BTupleType tupleType = (BTupleType) type;
        List<BTupleMember> members = tupleType.getMembers();
        mv.visitLdcInsn(members.size());
        mv.visitTypeInsn(ANEWARRAY, TYPE);
        for (int i = 0; i < members.size(); i++) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(i);
            loadTypeUsingTypeBuilder(mv, members.get(i).type);
            mv.visitInsn(AASTORE);
        }

        mv.visitLdcInsn(members.size());
        if (tupleType.restType != null) {
            loadTypeUsingTypeBuilder(mv, tupleType.restType);
        } else {
            loadNeverType(mv);
        }
        mv.visitMethodInsn(INVOKESTATIC, TYPE_BUILDER, "tupleSubType", LIST_SUBTYPE_BUILDER_DESCRIPTOR, false);
    }

    private void loadArraySubTypeUsingTypeBuilder(MethodVisitor mv, BType type) {
        BArrayType arrayType = (BArrayType) type;
        BType elementType = arrayType.eType;
        if (arrayType.size != -1) {
            // fixed length array
            mv.visitLdcInsn(1);
            mv.visitTypeInsn(ANEWARRAY, TYPE);
            mv.visitInsn(DUP);
            mv.visitLdcInsn(0);
            loadTypeUsingTypeBuilder(mv, elementType);
            mv.visitInsn(AASTORE);

            mv.visitLdcInsn(arrayType.size);

            loadNeverType(mv);
        } else {
            mv.visitLdcInsn(0);
            mv.visitTypeInsn(ANEWARRAY, TYPE);
            mv.visitLdcInsn(0);
            loadTypeUsingTypeBuilder(mv, elementType);
        }
        mv.visitMethodInsn(INVOKESTATIC, TYPE_BUILDER, "listSubType", LIST_SUBTYPE_BUILDER_DESCRIPTOR, false);
    }

    private static void loadNeverType(MethodVisitor mv) {
        mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, "TYPE_NEVER", GET_TYPE);
    }

    private static boolean isMayBeReadonlyType(BType type) {
        return switch (type.tag) {
            case TypeTags.ANY, TypeTags.ANYDATA, TypeTags.JSON -> true;
            default -> isStructureType(type);
        };
    }

    private static boolean isStructureType(BType type) {
        return switch (type.tag) {
            case TypeTags.ARRAY, TypeTags.TUPLE -> true;
            default -> false;
        };
    }

    // TODO: ideally we should handle xml here as well
    private void loadMayBeReadonlyTypeUsingTypeBuilder(MethodVisitor mv, BType type) {
        if (isStructureType(type)) {
            loadStructureTypeUsingTypeBuilder(mv, type);
        } else {
            String fieldName = "TYPE_" + switch (type.tag) {
                case TypeTags.ANY -> "ANY";
                case TypeTags.ANYDATA -> "ANYDATA";
                case TypeTags.JSON -> "JSON";
                default -> throw new UnsupportedOperationException("Unexpected maybe readonly type: " + type);
            };
            mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, fieldName, GET_TYPE);
        }
        if (!Symbols.isFlagOn(type.flags, Flags.READONLY)) {
            return;
        }
        mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, "TYPE_READONLY", GET_TYPE);
        mv.visitMethodInsn(INVOKESTATIC, TYPE_BUILDER, "intersect", BINARY_TYPE_OPERATION_DESCRIPTOR, false);
    }

    private static void loadIntSubTypeUsingTypeBuilder(MethodVisitor mv, long min, long max) {
        mv.visitLdcInsn(min);
        mv.visitLdcInsn(max);
        // TODO: move descriptor to constants
        mv.visitMethodInsn(INVOKESTATIC, TYPE_BUILDER, "intSubType", "(JJ)" + GET_TYPE, false);
    }

    // TODO: inline this
    private static void loadIntSubTypeUsingTypeBuilder(MethodVisitor mv, BType type) {
        // TODO: BTypeHack: this is because we a separate BByte type that is treated
        //  different from BIntegerType
        String fieldName = "TYPE_" + switch (type.tag) {
            case TypeTags.BYTE -> "BYTE";
            case TypeTags.UNSIGNED8_INT -> "INT_UNSIGNED_8";
            case TypeTags.UNSIGNED16_INT -> "INT_UNSIGNED_16";
            case TypeTags.UNSIGNED32_INT -> "INT_UNSIGNED_32";
            case TypeTags.SIGNED8_INT -> "INT_SIGNED_8";
            case TypeTags.SIGNED16_INT -> "INT_SIGNED_16";
            case TypeTags.SIGNED32_INT -> "INT_SIGNED_32";
            default -> throw new IllegalStateException("Unexpected Int subtype" + type);
        };
        mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, fieldName, GET_TYPE);
    }

    private static void loadStringSubTypeUsingTypeBuilder(MethodVisitor mv, boolean allowChars, List<String> chars,
                                                          boolean allowNonChars, List<String> nonChars) {
        mv.visitTypeInsn(NEW, STRING_SUBTYPE_DATA);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, STRING_SUBTYPE_DATA, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
        stringSubtypeDataBuilderHelper(mv, chars, allowChars ? "includeChars" : "excludeChars");
        stringSubtypeDataBuilderHelper(mv, nonChars, allowNonChars ? "includeNonChars" : "excludeNonChars");
        mv.visitMethodInsn(INVOKESTATIC, TYPE_BUILDER, "stringSubType", STRING_SUBTYPE_BUILDER_DESCRIPTOR, false);
    }

    private static void stringSubtypeDataBuilderHelper(MethodVisitor mv, List<String> chars, String methodName) {
        mv.visitLdcInsn(chars.size());
        mv.visitTypeInsn(ANEWARRAY, STRING_VALUE);
        for (int i = 0; i < chars.size(); i++) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn(i);
            mv.visitLdcInsn(chars.get(i));
            mv.visitInsn(AASTORE);
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_SUBTYPE_DATA, methodName, STRING_SUBTYPE_DATA_BUILDER_DESC, false);
    }

    private static boolean canBeHandledByTypeBuilder(BType type, Set<BType> seen) {
        if (type == null) {
            return true;
        }
        if (seen.contains(type)) {
            // TODO: type builder can't handle cyclic types (basic types are not cyclic)
            return false;
        }
        seen.add(type);
        return switch (type.tag) {
            case TypeTags.NEVER, TypeTags.INT, TypeTags.NIL, TypeTags.BYTE,
                    TypeTags.UNSIGNED8_INT, TypeTags.UNSIGNED16_INT, TypeTags.UNSIGNED32_INT,
                    TypeTags.SIGNED8_INT, TypeTags.SIGNED16_INT, TypeTags.SIGNED32_INT,
                    TypeTags.FLOAT, TypeTags.STRING, TypeTags.CHAR_STRING, TypeTags.DECIMAL, TypeTags.BOOLEAN,
                    TypeTags.ANY, TypeTags.ANYDATA, TypeTags.JSON,
                    TypeTags.XML_COMMENT, TypeTags.XML_PI, TypeTags.XML_ELEMENT, TypeTags.XML_TEXT -> true;
            case TypeTags.ERROR -> {
                BErrorType errorType = (BErrorType) type;
                PackageID pkgID = errorType.tsymbol.pkgID;
                yield JvmCodeGenUtil.isBuiltInPackage(pkgID);
            }
            case TypeTags.ARRAY -> {
                BArrayType arrayType = (BArrayType) type;
                yield canBeHandledByTypeBuilder(arrayType.eType, seen);
            }
            case TypeTags.UNION -> {
                BUnionType unionType = (BUnionType) type;
                if (unionType.isCyclic) {
                    yield false;
                }
                LinkedHashSet<BType> members = unionType.getMemberTypes();
                if (members.size() < 2) {
                    // TODO: how to handle this?
                    yield false;
                }
                for (BType memberType : members) {
                    if (!canBeHandledByTypeBuilder(memberType, seen)) {
                        yield false;
                    }
                }
                yield true;
            }
            case TypeTags.TUPLE -> {
                BTupleType tupleType = (BTupleType) type;
                if (tupleType.isCyclic) {
                    yield false;
                }
                for (BTupleMember member : tupleType.getMembers()) {
                    if (!canBeHandledByTypeBuilder(member.type, seen)) {
                        yield false;
                    }
                }
                yield canBeHandledByTypeBuilder(tupleType.restType, seen);
            }
            case TypeTags.INTERSECTION -> {
                BIntersectionType intersectionType = (BIntersectionType) type;
                yield canBeHandledByTypeBuilder(intersectionType.effectiveType, seen);
            }
            case TypeTags.FINITE -> {
                BFiniteType finiteType = (BFiniteType) type;
                for (SemNamedType valueType : finiteType.valueSpace) {
                    SemType semType = valueType.semType();
                    if (!Core.isSubtypeSimple(semType, PredefinedType.STRING) &&
                            !Core.isSubtypeSimple(semType, PredefinedType.INT)) {
                        yield false;
                    }
                }
                yield true;
            }
            case TypeTags.XML -> {
                BXMLType xmlType = (BXMLType) type;
                yield xmlType.constraint == null || canBeHandledByTypeBuilder(xmlType.constraint, seen);
            }
            default -> false;
        };
    }

    /**
     * Generate code to load an instance of the given type
     * to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType type to load
     */
    public void loadType(MethodVisitor mv, BType bType) {
        if (canBeHandledByTypeBuilder(bType, new HashSet<>())) {
            loadTypeUsingTypeBuilder(mv, bType);
            return;
        }
        String typeFieldName;
        switch (bType.tag) {
            case TypeTags.REGEXP:
                typeFieldName = Symbols.isFlagOn(bType.flags, Flags.READONLY) ? "TYPE_READONLY_ANYDATA"
                        : "TYPE_ANYDATA";
                break;
            case TypeTags.XML:
                loadXmlType(mv, (BXMLType) bType);
                return;
            case TypeTags.TYPEDESC:
                loadTypedescType(mv, (BTypedescType) bType);
                return;
            case TypeTags.OBJECT:
            case TypeTags.RECORD:
                loadUserDefinedType(mv, bType);
                return;
            case TypeTags.HANDLE:
                typeFieldName = "TYPE_HANDLE";
                break;
            case TypeTags.ARRAY:
                // NOTE: this is to handle cases where we still can't generate the element using
                // type builder
                jvmConstantsGen.generateGetBArrayType(mv, jvmConstantsGen.getTypeConstantsVar(bType, symbolTable));
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
                BUnionType unionType = (BUnionType) bType;
                if (unionType.isCyclic) {
                    loadUserDefinedType(mv, bType);
                } else {
                    jvmConstantsGen.generateGetBUnionType(mv,
                            jvmConstantsGen.getTypeConstantsVar(bType, symbolTable));
                }
                return;
            case TypeTags.INTERSECTION:
                loadIntersectionType(mv, (BIntersectionType) bType);
                return;
            case TypeTags.INVOKABLE:
                loadInvokableType(mv, (BInvokableType) bType);
                return;
            case TypeTags.NONE:
                mv.visitInsn(ACONST_NULL);
                return;
            case TypeTags.TUPLE:
                BTupleType tupleType = (BTupleType) bType;
                if (tupleType.isCyclic) {
                    loadUserDefinedType(mv, bType);
                } else {
                    jvmConstantsGen.generateGetBTupleType(mv, jvmConstantsGen.getTypeConstantsVar(tupleType,
                            symbolTable));
                }
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
            case TypeTags.PARAMETERIZED_TYPE:
                loadParameterizedType(mv, (BParameterizedType) bType);
                return;
            case TypeTags.TYPEREFDESC:
                String typeOwner = getModuleLevelClassName(bType.tsymbol.pkgID,
                        JvmConstants.TYPEREF_TYPE_CONSTANT_CLASS_NAME);
                mv.visitFieldInsn(GETSTATIC, typeOwner,
                        JvmCodeGenUtil.getRefTypeConstantName((BTypeReferenceType) bType), GET_TYPE_REF_TYPE_IMPL);
                return;
            default:
                return;
        }

        mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, typeFieldName, LOAD_TYPE);
    }

    /**
     * Generate code to load an instance of the given typedesc type
     * to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType typedesc type to load
     */
    private void loadTypedescType(MethodVisitor mv, BTypedescType bType) {
        // Create an new map type
        mv.visitTypeInsn(NEW, TYPEDESC_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load the constraint type
        loadType(mv, bType.constraint);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, TYPEDESC_TYPE_IMPL, JVM_INIT_METHOD, TYPE_PARAMETER, false);
        wrapType(mv);
    }

    /**
     * Generate code to load an instance of the given map type
     * to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType map type to load
     */
    private void loadMapType(MethodVisitor mv, BMapType bType) {
        // Create an new map type
        mv.visitTypeInsn(NEW, MAP_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load the constraint type
        loadType(mv, bType.constraint);

        loadReadonlyFlag(mv, bType);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, MAP_TYPE_IMPL, JVM_INIT_METHOD, INIT_WITH_BOOLEAN, false);
        wrapType(mv);
    }

    public void loadReadonlyFlag(MethodVisitor mv, BType bType) {
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
    private void loadXmlType(MethodVisitor mv, BXMLType bType) {
        // Create an new xml type
        mv.visitTypeInsn(NEW, XML_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load the constraint type
        loadType(mv, bType.constraint);

        loadReadonlyFlag(mv, bType);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, XML_TYPE_IMPL, JVM_INIT_METHOD, INIT_WITH_BOOLEAN, false);
        wrapType(mv);
    }

    /**
     * Generate code to load an instance of the given table type
     * to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType table type to load
     */
    private void loadTableType(MethodVisitor mv, BTableType bType) {
        // Create an new table type
        mv.visitTypeInsn(NEW, TABLE_TYPE_IMPL);
        mv.visitInsn(DUP);

        loadType(mv, bType.constraint);
        if (!bType.fieldNameList.isEmpty()) {
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
                mv.visitLdcInsn(StringEscapeUtils.unescapeJava(fieldName));
                mv.visitInsn(AASTORE);
                i += 1;
            }

            loadReadonlyFlag(mv, bType);
            mv.visitMethodInsn(INVOKESPECIAL, TABLE_TYPE_IMPL, JVM_INIT_METHOD, INIT_TABLE_TYPE_WITH_FIELD_NAME_LIST,
                    false);
        } else if (bType.keyTypeConstraint != null) {
            loadType(mv, bType.keyTypeConstraint);
            loadReadonlyFlag(mv, bType);
            mv.visitMethodInsn(INVOKESPECIAL, TABLE_TYPE_IMPL, JVM_INIT_METHOD, INIT_TABLE_TYPE_IMPL, false);
        } else {
            loadReadonlyFlag(mv, bType);
            mv.visitMethodInsn(INVOKESPECIAL, TABLE_TYPE_IMPL, JVM_INIT_METHOD, INIT_WITH_BOOLEAN, false);
        }
        wrapType(mv);
    }

    private void loadStreamType(MethodVisitor mv, BStreamType bType) {
        // Create an new stream type
        mv.visitTypeInsn(NEW, STREAM_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load constraint type and completion type
        loadType(mv, bType.constraint);
        loadType(mv, bType.completionType);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, STREAM_TYPE_IMPL, JVM_INIT_METHOD, INIT_STREAM_TYPE_IMPL, false);
        wrapType(mv);
    }

    /**
     * Generate code to load an instance of the given error type
     * to the top of the stack.
     *
     * @param mv        method visitor
     * @param errorType error type to load
     */
    private void loadErrorType(MethodVisitor mv, BErrorType errorType) {

        PackageID pkgID = errorType.tsymbol.pkgID;
        if (JvmCodeGenUtil.isBuiltInPackage(pkgID)) {
            mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, TYPES_ERROR, GET_TYPE);
            return;
        }

        if (Symbols.isFlagOn(errorType.flags, Flags.ANONYMOUS)) {
            jvmConstantsGen.generateGetBErrorType(mv, jvmConstantsGen.getTypeConstantsVar(errorType, symbolTable));
        } else {
            String typeOwner = JvmCodeGenUtil.getPackageName(pkgID) + MODULE_INIT_CLASS_NAME;
            String fieldName = getTypeFieldName(toNameString(errorType));
            mv.visitFieldInsn(GETSTATIC, typeOwner, fieldName, GET_TYPE);
        }
    }

    public boolean loadUnionName(MethodVisitor mv, BUnionType unionType) {
        if ((unionType.tsymbol != null) && (unionType.tsymbol.name != null)) {
            mv.visitLdcInsn(Utils.decodeIdentifier(unionType.tsymbol.name.getValue()));
        } else if (unionType.name != null) {
            mv.visitLdcInsn(Utils.decodeIdentifier(unionType.name.getValue()));
        } else {
            return false;
        }
        return true;
    }

    public void loadCyclicFlag(MethodVisitor mv, BType valueType) {
        valueType = JvmCodeGenUtil.getImpliedType(valueType);
        switch (valueType.tag) {
            case TypeTags.UNION:
                mv.visitInsn(((BUnionType) valueType).isCyclic ? ICONST_1 : ICONST_0);
                break;
            case TypeTags.TUPLE:
                mv.visitInsn(((BTupleType) valueType).isCyclic ? ICONST_1 : ICONST_0);
                break;
        }
    }

    public void createUnionMembersArray(ClassWriter cw, MethodVisitor methodVisitor, Set<BType> members,
                                        String className, String name) {
        generateCreateNewArray(methodVisitor, members);
        int arrayIndex = 1;
        methodVisitor.visitVarInsn(ASTORE, arrayIndex);
        int i = 0;
        MethodVisitor mv = null;
        int methodCount = 0;
        String curMethodName = "";
        for (BType memberType : members) {
            if (i % JvmConstants.MAX_MEMBERS_PER_METHOD == 0) {
                curMethodName = "createMembers$" + name + methodCount++;
                mv = cw.visitMethod(ACC_STATIC, curMethodName, SET_TYPE_ARRAY, null, null);
            }
            mv.visitVarInsn(ALOAD, 0);
            mv.visitLdcInsn((long) i++);
            mv.visitInsn(L2I);
            // Load the member type
            loadType(mv, memberType);

            // Add the member to the array
            mv.visitInsn(AASTORE);
            if (i % JvmConstants.MAX_MEMBERS_PER_METHOD == 0) {
                generateMethodReturnAndInvoke(methodVisitor, className, arrayIndex, mv, curMethodName);
            }
        }
        if (i % JvmConstants.MAX_MEMBERS_PER_METHOD != 0) {
            generateMethodReturnAndInvoke(methodVisitor, className, arrayIndex, mv, curMethodName);
        }
        methodVisitor.visitVarInsn(ALOAD, arrayIndex);
    }

    private void generateMethodReturnAndInvoke(MethodVisitor methodVisitor, String className, int arrayIndex,
                                               MethodVisitor mv, String curMethodName) {
        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, curMethodName, className);
        mv.visitEnd();
        methodVisitor.visitVarInsn(ALOAD, arrayIndex);
        methodVisitor.visitMethodInsn(INVOKESTATIC, className, curMethodName, SET_TYPE_ARRAY, false);
    }

    public void createUnionMembersArray(MethodVisitor mv, Set<BType> members) {
        generateCreateNewArray(mv, members);
        int i = 0;
        for (BType memberType : members) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) i++);
            mv.visitInsn(L2I);

            // Load the member type
            loadType(mv, memberType);

            // Add the member to the array
            mv.visitInsn(AASTORE);
        }
    }

    /**
     * Generate code to load an instance of the given intersection type to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType intersection type to load
     */
    private void loadIntersectionType(MethodVisitor mv, BIntersectionType bType) {
        // Create the intersection type
        mv.visitTypeInsn(NEW, INTERSECTION_TYPE_IMPL);
        mv.visitInsn(DUP);

        mv.visitLdcInsn(Utils.decodeIdentifier(bType.tsymbol.name.value));
        String varName = jvmConstantsGen.getModuleConstantVar(bType.tsymbol.pkgID);
        mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(), varName, GET_MODULE);
        // Create the constituent types array.
        Set<BType> constituentTypes = bType.getConstituentTypes();
        generateCreateNewArray(mv, constituentTypes);
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
        mv.visitMethodInsn(INVOKESPECIAL, INTERSECTION_TYPE_IMPL, JVM_INIT_METHOD, INIT_INTERSECTION_TYPE_WITH_TYPE,
                false);
        wrapType(mv);
    }

    private void generateCreateNewArray(MethodVisitor methodVisitor, Set<BType> members) {
        methodVisitor.visitLdcInsn((long) members.size());
        methodVisitor.visitInsn(L2I);
        methodVisitor.visitTypeInsn(ANEWARRAY, TYPE);
    }

    /**
     * Load a user defined type instance to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType user defined type
     */
    private void loadUserDefinedType(MethodVisitor mv, BType bType) {
        BTypeSymbol typeSymbol = bType.tsymbol.isTypeParamResolved ? bType.tsymbol.typeParamTSymbol : bType.tsymbol;
        BType typeToLoad = bType.tsymbol.isTypeParamResolved ? typeSymbol.type : bType;
        PackageID pkgID = typeSymbol.pkgID;
        String typeOwner = JvmCodeGenUtil.getPackageName(pkgID) + MODULE_INIT_CLASS_NAME;
        String defName = "";
        if ((typeSymbol.kind == SymbolKind.RECORD || typeSymbol.kind == SymbolKind.OBJECT)
                && typeSymbol.name.value.isEmpty()) {
            defName = Utils
                    .encodeNonFunctionIdentifier(((BStructureTypeSymbol) typeSymbol).typeDefinitionSymbol.name.value);
        }
        //class symbols
        String fieldName = defName.isEmpty() ? getTypeFieldName(toNameString(typeToLoad)) : defName;

        boolean samePackage = JvmCodeGenUtil.isSameModule(this.packageID, pkgID);

        // if name contains $anon and doesn't belong to the same package, load type using getAnonType() method.
        if (!samePackage && Symbols.isFlagOn(typeToLoad.flags, Flags.ANONYMOUS)) {
            Integer hash = typeHashVisitor.visit(typeToLoad);
            String shape = typeToLoad.toString();
            typeHashVisitor.reset();

            mv.visitTypeInsn(NEW, typeOwner);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, typeOwner, JVM_INIT_METHOD, VOID_METHOD_DESC, false);

            mv.visitLdcInsn(hash);
            mv.visitLdcInsn("Package: " + typeOwner + ", TypeName: " + fieldName + ", Shape: " + shape + "");
            mv.visitMethodInsn(INVOKEVIRTUAL, typeOwner, GET_ANON_TYPE_METHOD, JvmSignatures.GET_ANON_TYPE, false);
        } else {
            mv.visitFieldInsn(GETSTATIC, typeOwner, fieldName, GET_TYPE);
        }
    }

    /**
     * Return the name of the field that holds the instance of a given type.
     *
     * @param typeName type name
     * @return name of the field that holds the type instance
     */
    public static String getTypeFieldName(String typeName) {
        if (typeName.isEmpty()) {
            throw new AssertionError("Could not resolve the field for type");
        }
        return "$type$" + typeName;
    }

    public String getTypedescFieldName(String name) {
        return "$typedesce$" + name;
    }

    private void loadFutureType(MethodVisitor mv, BFutureType bType) {

        mv.visitTypeInsn(NEW, FUTURE_TYPE_IMPL);
        mv.visitInsn(DUP);

        loadType(mv, bType.constraint);
        mv.visitMethodInsn(INVOKESPECIAL, FUTURE_TYPE_IMPL, JVM_INIT_METHOD, TYPE_PARAMETER, false);
        wrapType(mv);
    }

    /**
     * Create and load an invokable type.
     *
     * @param mv    method visitor
     * @param bType invokable type to be created
     */
    public void loadInvokableType(MethodVisitor mv, BInvokableType bType) {

        mv.visitTypeInsn(NEW, FUNCTION_TYPE_IMPL);
        mv.visitInsn(DUP);
        if (bType.tsymbol == null) {
            mv.visitInsn(ACONST_NULL);
        } else {
            String moduleName = jvmConstantsGen.getModuleConstantVar(bType.tsymbol.pkgID);
            mv.visitFieldInsn(GETSTATIC, jvmConstantsGen.getModuleConstantClass(), moduleName, GET_MODULE);
        }

        if (Symbols.isFlagOn(bType.flags, Flags.ANY_FUNCTION)) {
            mv.visitLdcInsn(bType.flags);
            mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_TYPE_IMPL, JVM_INIT_METHOD, INIT_FUNCTION_TYPE_IMPL, false);
            return;
        }

        loadFunctionParameters(mv, bType);

        BType restType = bType.restType;
        if (restType == null) {
            mv.visitInsn(ACONST_NULL);
        } else {
            loadType(mv, restType);
        }

        // load return type type
        loadType(mv, bType.retType);

        mv.visitLdcInsn(bType.flags);
        mv.visitLdcInsn(bType.name.getValue());
        // initialize the function type using the param types array and the return type
        mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_TYPE_IMPL, JVM_INIT_METHOD, INIT_FUNCTION_TYPE_IMPL_WITH_PARAMS,
                false);
    }

    protected void loadFunctionPathParameters(MethodVisitor mv, BInvokableTypeSymbol invokableSymbol) {
        List<BVarSymbol> params = new ArrayList<>();
        if (invokableSymbol != null) {
            for (BVarSymbol param : invokableSymbol.params) {
                SymbolKind paramKind = param.getKind();
                if (paramKind != SymbolKind.PATH_PARAMETER && paramKind != SymbolKind.PATH_REST_PARAMETER) {
                    break;
                }
                params.add(param);
            }
        }
        populateFunctionParameters(mv, invokableSymbol, params);
    }

    private void populateFunctionParameters(MethodVisitor mv, BInvokableTypeSymbol invokableSymbol,
                                            List<BVarSymbol> params) {
        mv.visitLdcInsn((long) params.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, FUNCTION_PARAMETER);
        for (int i = 0; i < params.size(); i++) {
            BVarSymbol paramSymbol = params.get(i);
            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) i);
            mv.visitInsn(L2I);
            mv.visitTypeInsn(NEW, FUNCTION_PARAMETER);
            mv.visitInsn(DUP);
            mv.visitLdcInsn(paramSymbol.name.value);
            if (paramSymbol.isDefaultable) {
                mv.visitInsn(ICONST_1);
            } else {
                mv.visitInsn(ICONST_0);
            }
            BInvokableSymbol bInvokableSymbol = invokableSymbol.defaultValues.get(
                    Utils.decodeIdentifier(paramSymbol.name.value));
            if (bInvokableSymbol == null) {
                mv.visitInsn(ACONST_NULL);
            } else {
                mv.visitLdcInsn(bInvokableSymbol.name.value);
            }
            loadType(mv, paramSymbol.type);
            mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_PARAMETER, JVM_INIT_METHOD, INIT_FUNCTION_PARAM, false);
            mv.visitInsn(AASTORE);
        }
    }

    private void loadFunctionParameters(MethodVisitor mv, BInvokableType invokableType) {
        BInvokableTypeSymbol invokableSymbol = (BInvokableTypeSymbol) invokableType.tsymbol;
        List<BVarSymbol> params = new ArrayList<>();
        if (invokableSymbol == null) {
            if (!invokableType.paramTypes.isEmpty()) {
                loadFunctionPointerParameters(mv, invokableType);
                return;
            }
        } else {
            params = invokableSymbol.params;
        }
        populateFunctionParameters(mv, invokableSymbol, params);
    }

    private void loadFunctionPointerParameters(MethodVisitor mv, BInvokableType invokableType) {
        List<BType> paramTypes = invokableType.paramTypes;
        mv.visitLdcInsn((long) paramTypes.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, FUNCTION_PARAMETER);
        for (int i = 0; i < paramTypes.size(); i++) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) i);
            mv.visitInsn(L2I);
            mv.visitTypeInsn(NEW, FUNCTION_PARAMETER);
            mv.visitInsn(DUP);
            mv.visitLdcInsn("");
            mv.visitInsn(ICONST_0);
            mv.visitInsn(ACONST_NULL);
            loadType(mv, paramTypes.get(i));
            mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_PARAMETER, JVM_INIT_METHOD, INIT_FUNCTION_PARAM, false);
            mv.visitInsn(AASTORE);
        }
    }

    private void loadParameterizedType(MethodVisitor mv, BParameterizedType bType) {
        mv.visitTypeInsn(NEW, PARAMETERIZED_TYPE_IMPL);
        mv.visitInsn(DUP);

        loadType(mv, bType.paramValueType);
        mv.visitLdcInsn(bType.paramIndex);

        mv.visitMethodInsn(INVOKESPECIAL, PARAMETERIZED_TYPE_IMPL, JVM_INIT_METHOD, INIT_PARAMETERIZED_TYPE_IMPL,
                false);
    }

    public static String getTypeDesc(BType bType) {
        bType = JvmCodeGenUtil.getImpliedType(bType);
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            return "J";
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
            return GET_BSTRING;
        } else if (TypeTags.isXMLTypeTag(bType.tag)) {
            return GET_XML;
        } else if (bType.tag == TypeTags.REGEXP) {
            return GET_REGEXP;
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
            case TypeTags.JSON:
            case TypeTags.FINITE:
            case TypeTags.READONLY:
                return GET_OBJECT;
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                return GET_ARRAY_VALUE;
            case TypeTags.ERROR:
                return GET_ERROR_VALUE;
            case TypeTags.FUTURE:
                return GET_FUTURE_VALUE;
            case TypeTags.MAP:
            case TypeTags.RECORD:
                return GET_MAP_VALUE;
            case TypeTags.TYPEDESC:
                return GET_TYPEDESC;
            case TypeTags.STREAM:
                return GET_STREAM_VALUE;
            case TypeTags.TABLE:
                return GET_TABLE_VALUE;
            case TypeTags.DECIMAL:
                return GET_BDECIMAL;
            case TypeTags.OBJECT:
                return GET_BOBJECT;
            case TypeTags.HANDLE:
                return GET_HANDLE_VALUE;
            case TypeTags.INVOKABLE:
                return GET_FUNCTION_POINTER;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE + bType);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent") // xxxSubtypeSingleValue() are guaranteed to have a value
    private void loadFiniteType(MethodVisitor mv, BFiniteType finiteType) {

        mv.visitTypeInsn(NEW, FINITE_TYPE_IMPL);
        mv.visitInsn(DUP);

        // load type name
        String name = Utils.decodeIdentifier(toNameString(finiteType));
        mv.visitLdcInsn(name);
        // load original type name
        mv.visitLdcInsn(finiteType.tsymbol.originalName.value);

        mv.visitTypeInsn(NEW, LINKED_HASH_SET);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_SET, JVM_INIT_METHOD, VOID_METHOD_DESC, false);

        for (SemNamedType semNamedType : finiteType.valueSpace) {
            SemType s = semNamedType.semType();
            if (PredefinedType.NIL.equals(s)) {
                loadNilValue(mv);
                continue;
            }

            ComplexSemType cs = (ComplexSemType) s;
            if (isSubtypeSimple(s, PredefinedType.BOOLEAN)) {
                boolean boolVal = BooleanSubtype.booleanSubtypeSingleValue(getComplexSubtypeData(cs, BT_BOOLEAN)).get();
                loadBooleanValue(mv, boolVal);
            } else if (isSubtypeSimple(s, PredefinedType.INT)) {
                long longVal = IntSubtype.intSubtypeSingleValue(getComplexSubtypeData(cs, BT_INT)).get();
                if (0 <= longVal && longVal <= 255) {
                    loadByteValue(mv, (int) longVal);
                } else {
                    loadIntValue(mv, longVal);
                }
            } else if (isSubtypeSimple(s, PredefinedType.FLOAT)) {
                double doubleVal = FloatSubtype.floatSubtypeSingleValue(getComplexSubtypeData(cs, BT_FLOAT)).get();
                loadFloatValue(mv, doubleVal);
            } else if (isSubtypeSimple(s, PredefinedType.DECIMAL)) {
                BigDecimal bVal = DecimalSubtype.decimalSubtypeSingleValue(getComplexSubtypeData(cs, BT_DECIMAL)).get();
                loadDecimalValue(mv, bVal);
            } else if (isSubtypeSimple(s, PredefinedType.STRING)) {
                String stringVal = StringSubtype.stringSubtypeSingleValue(getComplexSubtypeData(cs, BT_STRING)).get();
                loadStringValue(mv, stringVal);
            } else {
                throw new IllegalStateException("Unexpected value space type: " + s);
            }
        }

        // Load type flags
        mv.visitLdcInsn(TypeFlags.asMask(finiteType.isNullable(), true, true));

        // initialize the finite type using the value space
        mv.visitMethodInsn(INVOKESPECIAL, FINITE_TYPE_IMPL, JVM_INIT_METHOD, INIT_FINITE_TYPE_IMPL, false);
    }

    private void loadNilValue(MethodVisitor mv) {
        mv.visitInsn(DUP);
        mv.visitInsn(ACONST_NULL);

        // Add the value to the set
        mv.visitMethodInsn(INVOKEINTERFACE, SET, ADD_METHOD, ANY_TO_JBOOLEAN, true);
        mv.visitInsn(POP);
    }

    private void loadBooleanValue(MethodVisitor mv, boolean booleanVal) {
        mv.visitInsn(DUP);

        mv.visitLdcInsn(booleanVal);
        mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, VALUE_OF_METHOD, BOOLEAN_VALUE_OF_METHOD, false);

        // Add the value to the set
        mv.visitMethodInsn(INVOKEINTERFACE, SET, ADD_METHOD, ANY_TO_JBOOLEAN, true);
        mv.visitInsn(POP);
    }

    private void loadByteValue(MethodVisitor mv, int intValue) {
        mv.visitInsn(DUP);

        mv.visitLdcInsn(intValue);
        mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, VALUE_OF_METHOD, INT_VALUE_OF_METHOD, false);

        // Add the value to the set
        mv.visitMethodInsn(INVOKEINTERFACE, SET, ADD_METHOD, ANY_TO_JBOOLEAN, true);
        mv.visitInsn(POP);
    }

    private void loadIntValue(MethodVisitor mv, long intValue) {
        mv.visitInsn(DUP);

        mv.visitLdcInsn(intValue);
        mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, VALUE_OF_METHOD, LONG_VALUE_OF, false);

        // Add the value to the set
        mv.visitMethodInsn(INVOKEINTERFACE, SET, ADD_METHOD, ANY_TO_JBOOLEAN, true);
        mv.visitInsn(POP);
    }

    private void loadStringValue(MethodVisitor mv, String stringVal) {
        mv.visitInsn(DUP);

        int index = jvmConstantsGen.getBStringConstantVarIndex(stringVal);
        String varName = B_STRING_VAR_PREFIX + index;
        String stringConstantsClass = getStringConstantsClass(index, jvmConstantsGen);
        mv.visitFieldInsn(GETSTATIC, stringConstantsClass, varName, GET_BSTRING);

        // Add the value to the set
        mv.visitMethodInsn(INVOKEINTERFACE, SET, ADD_METHOD, ANY_TO_JBOOLEAN, true);
        mv.visitInsn(POP);
    }

    private void loadDecimalValue(MethodVisitor mv, BigDecimal bigDecimal) {
        mv.visitInsn(DUP);

        mv.visitTypeInsn(NEW, DECIMAL_VALUE);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(bigDecimal.toPlainString());
        mv.visitMethodInsn(INVOKESPECIAL, DECIMAL_VALUE, JVM_INIT_METHOD, INIT_WITH_STRING, false);

        // Add the value to the set
        mv.visitMethodInsn(INVOKEINTERFACE, SET, ADD_METHOD, ANY_TO_JBOOLEAN, true);
        mv.visitInsn(POP);
    }

    private void loadFloatValue(MethodVisitor mv, double doubleVal) {
        mv.visitInsn(DUP);

        mv.visitLdcInsn(doubleVal);
        mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, VALUE_OF_METHOD, DOUBLE_VALUE_OF_METHOD, false);

        // Add the value to the set
        mv.visitMethodInsn(INVOKEINTERFACE, SET, ADD_METHOD, ANY_TO_JBOOLEAN, true);
        mv.visitInsn(POP);
    }

    // TODO: ideally instead of this we should instead build the type using the TypeBuilder. As we convert more and
    //   more types to semtypes that should happen naturally
    @Deprecated
    public static void wrapType(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, TYPE_BUILDER, "toSemType", TO_SEMTYPE_DESC, false);
    }
}
