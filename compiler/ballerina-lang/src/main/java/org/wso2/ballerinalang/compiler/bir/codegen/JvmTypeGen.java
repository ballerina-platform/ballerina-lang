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
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.types.IntersectableReferenceType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.semantics.model.types.TypeFlags;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BOOLEAN_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CALL_FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_RECORD_VALUE;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPES_ERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_OF_METHOD;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ERROR_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_REGEXP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STREAM_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TABLE_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPEDESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPE_REF_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_XML;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_FINITE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_FUNCTION_PARAM;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_FUNCTION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_FUNCTION_TYPE_IMPL_WITH_PARAMS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_INTERSECTION_TYPE_WITH_REFERENCE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_INTERSECTION_TYPE_WITH_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_PARAMETERIZED_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_STREAM_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_TABLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_TABLE_TYPE_WITH_FIELD_NAME_LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_WITH_BOOLEAN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INT_VALUE_OF_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_ANYDATA_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_ANY_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_BOOLEAN_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_BYTE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_DECIMAL_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_FLOAT_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_HANDLE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_INTEGER_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_JSON_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_NEVER_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_NULL_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_OBJECT_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_READONLY_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_SERVICE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_STRING_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_UNION_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_XML_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LONG_VALUE_OF;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_TYPE_ARRAY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.TYPE_PARAMETER;

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
            BType bType = JvmCodeGenUtil.getReferredType(typeDef.type);
            if (bType.tag == TypeTags.RECORD || bType.tag == TypeTags.ERROR || bType.tag == TypeTags.OBJECT
                    || bType.tag == TypeTags.UNION || bType.tag == TypeTags.TUPLE) {
                String name = typeDef.internalName.value;
                generateTypeField(cw, name);
                generateTypedescField(cw, name);
            }
            // do not generate anything for other types (e.g.: finite type, unions, etc.)
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

    void generateGetAnonTypeMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, GET_ANON_TYPE_METHOD,
                JvmSignatures.GET_ANON_TYPE, null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKESTATIC, anonTypesClass, GET_ANON_TYPE_METHOD, JvmSignatures.GET_ANON_TYPE, false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    // -------------------------------------------------------
    //              Runtime value creation methods
    // -------------------------------------------------------

    void generateValueCreatorMethods(ClassWriter cw) {
        generateRecordValueCreateMethod(cw);
        generateObjectValueCreateMethod(cw);
        generateErrorValueCreateMethod(cw);
        generateFunctionCallMethod(cw);
    }

    private void generateRecordValueCreateMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, CREATE_RECORD_VALUE,
                CREATE_RECORD,
                CREATE_RECORD_WITH_MAP, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, recordsClass, CREATE_RECORD_VALUE, CREATE_RECORD, false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateObjectValueCreateMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, CREATE_OBJECT_VALUE, CREATE_OBJECT, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitVarInsn(ALOAD, 5);
        mv.visitMethodInsn(INVOKESTATIC, objectsClass, CREATE_OBJECT_VALUE, CREATE_OBJECT, false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateErrorValueCreateMethod(ClassWriter cw) {
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
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateFunctionCallMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, CALL_FUNCTION, FUNCTION_CALL, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKESTATIC, functionCallsClass, CALL_FUNCTION, FUNCTION_CALL, false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
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

    /**
     * Generate code to load an instance of the given type
     * to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType type to load
     */
    public void loadType(MethodVisitor mv, BType bType) {
        String typeFieldName;
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
                case TypeTags.REGEXP:
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
                case TypeTags.RECORD:
                    loadUserDefinedType(mv, bType);
                    return;
                case TypeTags.HANDLE:
                    typeFieldName = "TYPE_HANDLE";
                    break;
                case TypeTags.ARRAY:
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
                    String typeOwner = JvmCodeGenUtil.getModuleLevelClassName(bType.tsymbol.pkgID,
                            JvmConstants.TYPEREF_TYPE_CONSTANT_CLASS_NAME);
                    mv.visitFieldInsn(GETSTATIC, typeOwner,
                            JvmCodeGenUtil.getRefTypeConstantName((BTypeReferenceType) bType), GET_TYPE_REF_TYPE_IMPL);
                    return;
                default:
                    return;
            }
        }

        mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, typeFieldName, loadTypeClass(bType));
    }

    private String loadTypeClass(BType bType) {
        if (bType == null || bType.tag == TypeTags.NIL) {
            return LOAD_NULL_TYPE;
        } else {
            switch (bType.tag) {
                case TypeTags.NEVER:
                    return LOAD_NEVER_TYPE;
                case TypeTags.INT:
                case TypeTags.UNSIGNED8_INT:
                case TypeTags.UNSIGNED16_INT:
                case TypeTags.UNSIGNED32_INT:
                case TypeTags.SIGNED8_INT:
                case TypeTags.SIGNED16_INT:
                case TypeTags.SIGNED32_INT:
                    return LOAD_INTEGER_TYPE;
                case TypeTags.FLOAT:
                    return LOAD_FLOAT_TYPE;
                case TypeTags.STRING:
                case TypeTags.CHAR_STRING:
                    return LOAD_STRING_TYPE;
                case TypeTags.DECIMAL:
                    return LOAD_DECIMAL_TYPE;
                case TypeTags.BOOLEAN:
                    return LOAD_BOOLEAN_TYPE;
                case TypeTags.BYTE:
                    return LOAD_BYTE_TYPE;
                case TypeTags.ANY:
                    return LOAD_ANY_TYPE;
                case TypeTags.ANYDATA:
                case TypeTags.REGEXP:
                    return LOAD_ANYDATA_TYPE;
                case TypeTags.JSON:
                    return LOAD_JSON_TYPE;
                case TypeTags.XML:
                case TypeTags.XML_TEXT:
                    return LOAD_XML_TYPE;
                case TypeTags.XML_ELEMENT:
                case TypeTags.XML_PI:
                case TypeTags.XML_COMMENT:
                    return Symbols.isFlagOn(bType.flags, Flags.READONLY) ? LOAD_TYPE : LOAD_XML_TYPE;
                case TypeTags.OBJECT:
                    return Symbols.isService(bType.tsymbol) ? LOAD_SERVICE_TYPE : LOAD_OBJECT_TYPE;
                case TypeTags.HANDLE:
                    return LOAD_HANDLE_TYPE;
                case TypeTags.READONLY:
                    return LOAD_READONLY_TYPE;
                case TypeTags.UNION:
                    return LOAD_UNION_TYPE;
                case TypeTags.TYPEREFDESC:
                    return loadTypeClass(JvmCodeGenUtil.getReferredType(bType));
                default:
                    return LOAD_TYPE;
            }
        }
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
        // TODO: Builtin error type will be loaded from BTypes java class. Need to handle this properly.
        if (JvmCodeGenUtil.isBuiltInPackage(pkgID)) {
            mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, TYPES_ERROR, GET_ERROR_TYPE);
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
        switch (valueType.tag) {
            case TypeTags.UNION:
                mv.visitInsn(((BUnionType) valueType).isCyclic ? ICONST_1 : ICONST_0);
                break;
            case TypeTags.TUPLE:
                mv.visitInsn(((BTupleType) valueType).isCyclic ? ICONST_1 : ICONST_0);
                break;
            case TypeTags.TYPEREFDESC:
                loadCyclicFlag(mv, JvmCodeGenUtil.getReferredType(valueType));
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
        mv.visitMaxs(0, 0);
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
        String effectiveTypeClass;
        if (bType.effectiveType instanceof IntersectableReferenceType) {
            effectiveTypeClass = INIT_INTERSECTION_TYPE_WITH_REFERENCE_TYPE;
        } else {
            effectiveTypeClass = INIT_INTERSECTION_TYPE_WITH_TYPE;
        }
        mv.visitMethodInsn(INVOKESPECIAL, INTERSECTION_TYPE_IMPL, JVM_INIT_METHOD, effectiveTypeClass, false);
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
        if (!samePackage && (fieldName.contains(BLangAnonymousModelHelper.ANON_PREFIX) ||
                Symbols.isFlagOn(typeToLoad.flags, Flags.ANONYMOUS))) {
            Integer hash = typeHashVisitor.visit(typeToLoad);
            String shape = typeToLoad.toString();
            typeHashVisitor.reset();

            mv.visitTypeInsn(NEW, typeOwner);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, typeOwner, JVM_INIT_METHOD, "()V", false);

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
            BInvokableSymbol bInvokableSymbol = invokableSymbol.defaultValues.get(paramSymbol.name.value);
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
            case TypeTags.INTERSECTION:
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
                return GET_TABLE_VALUE_IMPL;
            case TypeTags.DECIMAL:
                return GET_BDECIMAL;
            case TypeTags.OBJECT:
                return GET_BOBJECT;
            case TypeTags.HANDLE:
                return GET_HANDLE_VALUE;
            case TypeTags.INVOKABLE:
                return GET_FUNCTION_POINTER;
            case TypeTags.TYPEREFDESC:
                return getTypeDesc(JvmCodeGenUtil.getReferredType(bType));
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE + bType);
        }
    }

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
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_SET, JVM_INIT_METHOD, "()V", false);

        for (BLangExpression valueTypePair : finiteType.getValueSpace()) {
            Object value = ((BLangLiteral) valueTypePair).value;
            BType valueType = valueTypePair.getBType();
            mv.visitInsn(DUP);

            JvmCodeGenUtil.loadConstantValue(valueType, value, mv, jvmConstantsGen);

            if (TypeTags.isIntegerTypeTag(valueType.tag)) {
                mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, VALUE_OF_METHOD, LONG_VALUE_OF,
                        false);
            } else {
                loadValueType(mv, valueType);
            }

            // Add the value to the set
            mv.visitMethodInsn(INVOKEINTERFACE, SET, "add", ANY_TO_JBOOLEAN, true);
            mv.visitInsn(POP);
        }

        // Load type flags
        mv.visitLdcInsn(typeFlag(finiteType));

        // initialize the finite type using the value space
        mv.visitMethodInsn(INVOKESPECIAL, FINITE_TYPE_IMPL, JVM_INIT_METHOD, INIT_FINITE_TYPE_IMPL, false);
    }

    private void loadValueType(MethodVisitor mv, BType valueType) {
        switch (valueType.tag) {
            case TypeTags.BOOLEAN:
                mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, VALUE_OF_METHOD,
                        BOOLEAN_VALUE_OF_METHOD, false);
                break;
            case TypeTags.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, VALUE_OF_METHOD,
                        DOUBLE_VALUE_OF_METHOD, false);
                break;
            case TypeTags.BYTE:
                mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, VALUE_OF_METHOD,
                        INT_VALUE_OF_METHOD, false);
                break;
            case TypeTags.TYPEREFDESC:
                loadValueType(mv, JvmCodeGenUtil.getReferredType(valueType));
        }
    }

}
