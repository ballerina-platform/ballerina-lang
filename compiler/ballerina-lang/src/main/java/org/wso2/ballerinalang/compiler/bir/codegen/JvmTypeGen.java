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

import io.ballerina.runtime.api.utils.IdentifierUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.types.IntersectableReferenceType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmBStringConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.analyzer.IsAnydataUniqueVisitor;
import org.wso2.ballerinalang.compiler.semantics.analyzer.IsPureTypeUniqueVisitor;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TypeHashVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANYDATA_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANY_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BOOLEAN_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BOOLEAN_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BYTE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_RECORD_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DOUBLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FINITE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FLOAT_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_PARAMETER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_ANON_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INTEGER_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INTERSECTABLE_REFERENCE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INTERSECTION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JSON_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LINKED_HASH_SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LIST;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LONG_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ANON_TYPES_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_ERRORS_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_OBJECTS_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_RECORDS_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.NEVER_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.NULL_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PARAMETERIZED_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PREDEFINED_TYPES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.READONLY_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SERVICE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SET;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.UNION_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.UNION_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_OF_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_VALUE;
import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getMajorVersion;

/**
 * BIR types to JVM byte code generation class.
 *
 * @since 1.2.0
 */
public class JvmTypeGen {

    private final IsPureTypeUniqueVisitor isPureTypeUniqueVisitor;
    private final IsAnydataUniqueVisitor isAnydataUniqueVisitor;
    private final JvmBStringConstantsGen stringConstantsGen;
    private final TypeHashVisitor typeHashVisitor;
    private final PackageID packageID;
    private final String anonTypesClass;
    private final String recordsClass;
    private final String objectsClass;
    private final String errorsClass;

    public JvmTypeGen(JvmBStringConstantsGen stringConstantsGen, PackageID packageID) {
        this.stringConstantsGen = stringConstantsGen;
        this.packageID = packageID;
        isPureTypeUniqueVisitor = new IsPureTypeUniqueVisitor();
        isAnydataUniqueVisitor = new IsAnydataUniqueVisitor();
        typeHashVisitor = new TypeHashVisitor();
        this.anonTypesClass = getModuleLevelClassName(packageID, MODULE_ANON_TYPES_CLASS_NAME);
        this.recordsClass = getModuleLevelClassName(packageID, MODULE_RECORDS_CLASS_NAME);
        this.objectsClass = getModuleLevelClassName(packageID, MODULE_OBJECTS_CLASS_NAME);
        this.errorsClass = getModuleLevelClassName(packageID, MODULE_ERRORS_CLASS_NAME);
    }

    private BType getConstrainedTypeFromRefType(BType type) {
        BType constraint = type;
        if (type.tag == TypeTags.TYPEREFDESC) {
            constraint = ((BTypeReferenceType) type).referredType;
        }
        return constraint.tag == TypeTags.TYPEREFDESC ? getConstrainedTypeFromRefType(constraint) : constraint;
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
            BType bType = getConstrainedTypeFromRefType(typeDef.type);
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
        FieldVisitor fv = cw.visitField(ACC_STATIC + ACC_PUBLIC, fieldName, String.format("L%s;", TYPE), null,
                                        null);
        fv.visitEnd();
    }

    private void generateTypedescField(ClassWriter cw, String name) {
        String typedescFieldName = getTypedescFieldName(name);
        FieldVisitor fvTypeDesc = cw.visitField(ACC_STATIC + ACC_PUBLIC, typedescFieldName,
                                                String.format("L%s;", TYPEDESC_VALUE), null, null);
        fvTypeDesc.visitEnd();
    }

    // -------------------------------------------------------
    //              getAnonType() generation methods
    // -------------------------------------------------------

    void generateGetAnonTypeMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, GET_ANON_TYPE,
                String.format("(IL%s;)L%s;", STRING_VALUE, TYPE), null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKESTATIC, anonTypesClass, GET_ANON_TYPE, String.format("(IL%s;)L%s;",
                                                                                      STRING_VALUE, TYPE), false);
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
    }

    private void generateRecordValueCreateMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, CREATE_RECORD_VALUE,
                String.format("(L%s;)L%s;", STRING_VALUE, MAP_VALUE),
                String.format("(L%s;)L%s<L%s;L%s;>;", STRING_VALUE, MAP_VALUE, STRING_VALUE, OBJECT), null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, recordsClass, CREATE_RECORD_VALUE,
                           String.format("(L%s;)L%s;", STRING_VALUE, MAP_VALUE), false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateObjectValueCreateMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, CREATE_OBJECT_VALUE,
                String.format("(L%s;L%s;L%s;L%s;[L%s;)L%s;", STRING_VALUE, SCHEDULER, STRAND_CLASS, MAP, OBJECT,
                        B_OBJECT), null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitVarInsn(ALOAD, 5);
        mv.visitMethodInsn(INVOKESTATIC, objectsClass, CREATE_OBJECT_VALUE,
                           String.format("(L%s;L%s;L%s;L%s;[L%s;)L%s;",
                        STRING_VALUE, SCHEDULER, STRAND_CLASS, MAP, OBJECT, B_OBJECT), false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateErrorValueCreateMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, CREATE_ERROR_VALUE,
                String.format("(L%s;L%s;L%s;L%s;)L%s;", STRING_VALUE, B_STRING_VALUE, BERROR, OBJECT, BERROR), null,
                null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKESTATIC, errorsClass, CREATE_ERROR_VALUE,
                           String.format("(L%s;L%s;L%s;L%s;)L%s;", STRING_VALUE, B_STRING_VALUE, BERROR,
                                         OBJECT, BERROR), false);
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
                    BUnionType unionType = (BUnionType) bType;
                    if (unionType.isCyclic) {
                        loadUserDefinedType(mv, bType);
                    } else {
                        loadUnionType(mv, unionType);
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
                        loadTupleType(mv, (BTupleType) bType);
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
                    loadType(mv, JvmCodeGenUtil.getReferredType(bType));
                    return;
                default:
                    return;
            }
        }

        mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, typeFieldName, String.format("L%s;", loadTypeClass(bType)));
    }

    private String loadTypeClass(BType bType) {
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
                    return Symbols.isService(bType.tsymbol) ? SERVICE_TYPE : OBJECT_TYPE;
                case TypeTags.HANDLE:
                    return HANDLE_TYPE;
                case TypeTags.READONLY:
                    return READONLY_TYPE;
                case TypeTags.UNION:
                    return UNION_TYPE;
                case TypeTags.TYPEREFDESC:
                    return loadTypeClass(((BTypeReferenceType) bType).referredType);
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
    private void loadArrayType(MethodVisitor mv, BArrayType bType) {
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
    private void loadTypedescType(MethodVisitor mv, BTypedescType bType) {
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
    private void loadMapType(MethodVisitor mv, BMapType bType) {
        // Create an new map type
        mv.visitTypeInsn(NEW, MAP_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load the constraint type
        loadType(mv, bType.constraint);

        loadReadonlyFlag(mv, bType);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, MAP_TYPE_IMPL, JVM_INIT_METHOD, String.format("(L%s;Z)V", TYPE), false);
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
        mv.visitMethodInsn(INVOKESPECIAL, XML_TYPE_IMPL, JVM_INIT_METHOD, String.format("(L%s;Z)V", TYPE), false);
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
                mv.visitLdcInsn(StringEscapeUtils.unescapeJava(fieldName));
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

    private void loadStreamType(MethodVisitor mv, BStreamType bType) {
        // Create an new stream type
        mv.visitTypeInsn(NEW, STREAM_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load constraint type and completion type
        loadType(mv, bType.constraint);
        loadType(mv, bType.completionType);

        // invoke the constructor
        mv.visitMethodInsn(INVOKESPECIAL, STREAM_TYPE_IMPL, JVM_INIT_METHOD,
                String.format("(L%s;L%s;)V", TYPE, TYPE), false);
    }

    /**
     * Generate code to load an instance of the given error type
     * to the top of the stack.
     *
     * @param mv        method visitor
     * @param errorType error type to load
     */
    private void loadErrorType(MethodVisitor mv, BErrorType errorType) {

        PackageID packageID = errorType.tsymbol.pkgID;
        // TODO: Builtin error type will be loaded from BTypes java class. Need to handle this properly.
        if (JvmCodeGenUtil.isBuiltInPackage(packageID)) {
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
     * @param mv        method visitor
     * @param unionType union type to load
     */
    private void loadUnionType(MethodVisitor mv, BUnionType unionType) {
        // Create the union type
        mv.visitTypeInsn(NEW, UNION_TYPE_IMPL);
        mv.visitInsn(DUP);

        createUnionMembersArray(mv, unionType.getMemberTypes());
        createUnionMembersArray(mv, unionType.getOriginalMemberTypes());

        boolean nameLoaded = loadUnionName(mv, unionType);

        if (nameLoaded) {
            BTypeSymbol tsymbol = unionType.tsymbol;
            if (tsymbol == null) {
                mv.visitInsn(ACONST_NULL);
            } else {
                mv.visitTypeInsn(NEW, MODULE);
                mv.visitInsn(DUP);

                PackageID packageID = tsymbol.pkgID;

                mv.visitLdcInsn(packageID.orgName.value);
                mv.visitLdcInsn(packageID.name.value);
                mv.visitLdcInsn(getMajorVersion(packageID.version.value));
                mv.visitMethodInsn(INVOKESPECIAL, MODULE, JVM_INIT_METHOD,
                        String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);
            }
        }

        mv.visitLdcInsn(typeFlag(unionType));

        loadCyclicFlag(mv, unionType);

        mv.visitLdcInsn(unionType.flags);

        // initialize the union type using the members array
        if (nameLoaded) {
            mv.visitMethodInsn(INVOKESPECIAL, UNION_TYPE_IMPL, JVM_INIT_METHOD,
                               String.format("([L%s;[L%s;L%s;L%s;IZJ)V", TYPE, TYPE, STRING_VALUE, MODULE), false);
        } else {
            mv.visitMethodInsn(INVOKESPECIAL, UNION_TYPE_IMPL, JVM_INIT_METHOD, String.format("([L%s;[L%s;IZJ)V",
                    TYPE, TYPE), false);
        }
    }

    public boolean loadUnionName(MethodVisitor mv, BUnionType unionType) {
        if ((unionType.tsymbol != null) && (unionType.tsymbol.name != null)) {
            mv.visitLdcInsn(IdentifierUtils.decodeIdentifier(unionType.tsymbol.name.getValue()));
        } else if (unionType.name != null) {
            mv.visitLdcInsn(IdentifierUtils.decodeIdentifier(unionType.name.getValue()));
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
                loadCyclicFlag(mv, ((BTypeReferenceType) valueType).referredType);
                break;
        }
    }

    public void createUnionMembersArray(MethodVisitor mv, Set<BType> members) {
        mv.visitLdcInsn((long) members.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, TYPE);
        int i = 0;
        for (BType memberType : members) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) i);
            mv.visitInsn(L2I);

            // Load the member type
            loadType(mv, memberType);

            // Add the member to the array
            mv.visitInsn(AASTORE);
            i += 1;
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

        mv.visitTypeInsn(NEW, MODULE);
        mv.visitInsn(DUP);

        PackageID packageID = bType.tsymbol.pkgID;

        mv.visitLdcInsn(packageID.orgName.value);
        mv.visitLdcInsn(packageID.name.value);
        mv.visitLdcInsn(getMajorVersion(packageID.version.value));
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
        String effectiveTypeClass;
        if (bType.effectiveType instanceof IntersectableReferenceType) {
            effectiveTypeClass = INTERSECTABLE_REFERENCE_TYPE;
        } else {
            effectiveTypeClass = TYPE;
        }
        mv.visitMethodInsn(INVOKESPECIAL, INTERSECTION_TYPE_IMPL, JVM_INIT_METHOD,
                           String.format("(L%s;[L%s;L%s;IZ)V", MODULE, TYPE, effectiveTypeClass), false);
    }

    /**
     * Load a Tuple type instance to the top of the stack.
     *
     * @param mv    method visitor
     * @param bType tuple type to be loaded
     */
    private void loadTupleType(MethodVisitor mv, BTupleType bType) {

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

        loadCyclicFlag(mv, bType);

        loadReadonlyFlag(mv, bType);

        mv.visitMethodInsn(INVOKESPECIAL, TUPLE_TYPE_IMPL, JVM_INIT_METHOD, String.format("(L%s;L%s;IZZ)V", LIST, TYPE),
                           false);
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
        PackageID packageID = typeSymbol.pkgID;
        String typeOwner = JvmCodeGenUtil.getPackageName(packageID) + MODULE_INIT_CLASS_NAME;
        String fieldName = getTypeFieldName(toNameString(typeToLoad));

        // if name contains $anon and doesn't belong to the same package, load type using getAnonType() method.
        if (!this.packageID.equals(packageID) &&
                (fieldName.contains(BLangAnonymousModelHelper.ANON_PREFIX)
                        || Symbols.isFlagOn(typeToLoad.flags, Flags.ANONYMOUS))) {
            Integer hash = typeHashVisitor.visit(typeToLoad);
            String shape = typeToLoad.toString();
            typeHashVisitor.reset();

            mv.visitTypeInsn(NEW, typeOwner);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, typeOwner, JVM_INIT_METHOD, "()V", false);

            mv.visitLdcInsn(hash);
            mv.visitLdcInsn(String.format("Package: %s, TypeName: %s, Shape: %s", typeOwner, fieldName, shape));
            mv.visitMethodInsn(INVOKEVIRTUAL, typeOwner, GET_ANON_TYPE,
                    String.format("(IL%s;)L%s;", STRING_VALUE, TYPE), false);
        } else {
            mv.visitFieldInsn(GETSTATIC, typeOwner, fieldName, String.format("L%s;", TYPE));
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
        return String.format("$type$%s", typeName);
    }

    public String getTypedescFieldName(String name) {
        return String.format("$typedesce$%s", name);
    }

    private void loadFutureType(MethodVisitor mv, BFutureType bType) {

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
    private void loadInvokableType(MethodVisitor mv, BInvokableType bType) {

        mv.visitTypeInsn(NEW, FUNCTION_TYPE_IMPL);
        mv.visitInsn(DUP);

        if (Symbols.isFlagOn(bType.flags, Flags.ANY_FUNCTION)) {
            mv.visitLdcInsn(bType.flags);
            mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_TYPE_IMPL, JVM_INIT_METHOD, "(J)V", false);
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

        // initialize the function type using the param types array and the return type
        mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_TYPE_IMPL, JVM_INIT_METHOD,
                           String.format("([L%s;L%s;L%s;J)V", FUNCTION_PARAMETER, TYPE, TYPE), false);
    }

    private void loadFunctionParameters(MethodVisitor mv, BInvokableType invokableType) {

        BInvokableTypeSymbol invokableSymbol = (BInvokableTypeSymbol) invokableType.tsymbol;
        List<BVarSymbol> params =  new ArrayList<>();
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
            mv.visitLdcInsn(paramSymbol.isDefaultable);
            mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, VALUE_OF_METHOD, String.format("(Z)L%s;", BOOLEAN_VALUE),
                               false);
            loadType(mv, paramSymbol.type);
            mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_PARAMETER, JVM_INIT_METHOD,
                               String.format("(L%s;L%s;L%s;)V", STRING_VALUE, BOOLEAN_VALUE, TYPE), false);
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
            mv.visitLdcInsn(false);
            mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, VALUE_OF_METHOD, String.format("(Z)L%s;", BOOLEAN_VALUE),
                               false);
            loadType(mv, paramTypes.get(i));
            mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_PARAMETER, JVM_INIT_METHOD,
                               String.format("(L%s;L%s;L%s;)V", STRING_VALUE, BOOLEAN_VALUE, TYPE), false);
            mv.visitInsn(AASTORE);
        }
    }

    private void loadParameterizedType(MethodVisitor mv, BParameterizedType bType) {
        mv.visitTypeInsn(NEW, PARAMETERIZED_TYPE_IMPL);
        mv.visitInsn(DUP);

        loadType(mv, bType.paramValueType);
        mv.visitLdcInsn(bType.paramIndex);

        mv.visitMethodInsn(INVOKESPECIAL, PARAMETERIZED_TYPE_IMPL, JVM_INIT_METHOD, String.format("(L%s;I)V", TYPE),
                           false);
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
            case TypeTags.TYPEREFDESC:
                return getTypeDesc(((BTypeReferenceType) bType).referredType);
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE + bType);
        }
    }

    private void loadFiniteType(MethodVisitor mv, BFiniteType finiteType) {

        mv.visitTypeInsn(NEW, FINITE_TYPE_IMPL);
        mv.visitInsn(DUP);

        // Load type name
        String name = IdentifierUtils.decodeIdentifier(toNameString(finiteType));
        mv.visitLdcInsn(name);

        mv.visitTypeInsn(NEW, LINKED_HASH_SET);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, LINKED_HASH_SET, JVM_INIT_METHOD, "()V", false);

        for (BLangExpression valueTypePair : finiteType.getValueSpace()) {
            Object value = ((BLangLiteral) valueTypePair).value;
            BType valueType = valueTypePair.getBType();
            mv.visitInsn(DUP);

            JvmCodeGenUtil.loadConstantValue(valueType, value, mv, stringConstantsGen);

            if (TypeTags.isIntegerTypeTag(valueType.tag)) {
                mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, VALUE_OF_METHOD, String.format("(J)L%s;", LONG_VALUE),
                        false);
            } else {
                loadValueType(mv, valueType);
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

    private void loadValueType(MethodVisitor mv, BType valueType) {
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
            case TypeTags.TYPEREFDESC:
                loadValueType(mv, getConstrainedTypeFromRefType(valueType));
        }
    }
}
