/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LabelGenerator;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JType;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.D2F;
import static org.objectweb.asm.Opcodes.D2I;
import static org.objectweb.asm.Opcodes.D2L;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.F2D;
import static org.objectweb.asm.Opcodes.I2B;
import static org.objectweb.asm.Opcodes.I2C;
import static org.objectweb.asm.Opcodes.I2D;
import static org.objectweb.asm.Opcodes.I2F;
import static org.objectweb.asm.Opcodes.I2L;
import static org.objectweb.asm.Opcodes.I2S;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.L2D;
import static org.objectweb.asm.Opcodes.L2F;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.NEW;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANY_TO_BOOLEAN_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANY_TO_BYTE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANY_TO_DECIMAL_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANY_TO_FLOAT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANY_TO_INT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BMP_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BOOLEAN_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BYTE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_HANDLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE_OF_J_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DOUBLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_VALUE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_TO_STRING_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_TO_UNSIGNED_INT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LONG_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.NUMBER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REF_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SIMPLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STREAM_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_CHECKER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_CONVERTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_OF_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.loadType;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.getSignatureForJType;

/**
 * Check cast methods generation for JVM bytecode generation.
 *
 * @since 1.2.0
 */
public class JvmCastGen {

    //the symbol table is currently set from package gen class
    static SymbolTable symbolTable;

    static void generatePlatformCheckCast(MethodVisitor mv, BIRVarToJVMIndexMap indexMap, BType sourceType,
                                          BType targetType) {

        if (sourceType.tag == JTypeTags.JTYPE) {
            // If a target type is bir type, then we can guarantee source type is a jvm type, hence the cast
            generateJToBCheckCast(mv, indexMap, (JType) sourceType, targetType);
        } else {
            // else target type is jvm and source type is bir
            generateBToJCheckCast(mv, sourceType, (JType) targetType);
        }
    }

    public static void generateBToJCheckCast(MethodVisitor mv, BType sourceType, JType targetType) {

        switch (targetType.jTag) {
            case JTypeTags.JBYTE:
                generateCheckCastBToJByte(mv, sourceType);
                break;
            case JTypeTags.JCHAR:
                generateCheckCastBToJChar(mv, sourceType);
                break;
            case JTypeTags.JSHORT:
                generateCheckCastBToJShort(mv, sourceType);
                break;
            case JTypeTags.JINT:
                generateCheckCastBToJInt(mv, sourceType);
                break;
            case JTypeTags.JLONG:
                generateCheckCastBToJLong(mv, sourceType);
                break;
            case JTypeTags.JFLOAT:
                generateCheckCastBToJFloat(mv, sourceType);
                break;
            case JTypeTags.JDOUBLE:
                generateCheckCastBToJDouble(mv, sourceType);
                break;
            case JTypeTags.JREF:
                generateCheckCastBToJRef(mv, sourceType, targetType);
                break;
            case JTypeTags.JARRAY:
                generateCheckCastBToJRef(mv, sourceType, targetType);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java %s'",
                        sourceType, targetType));
        }
    }

    private static void generateCheckCastBToJByte(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitInsn(L2I);
            mv.visitInsn(I2B);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.BYTE:
                // do nothing
                break;
            case TypeTags.FLOAT:
                mv.visitInsn(D2I);
                mv.visitInsn(I2B);
                break;
            case TypeTags.HANDLE:
                mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, GET_VALUE_METHOD, "()Ljava/lang/Object;",
                        false);
                break;
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_BYTE_METHOD, String.format("(L%s;)I", OBJECT),
                        false);
                mv.visitInsn(I2B);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java byte'",
                        sourceType));
        }

    }

    private static void generateCheckCastBToJChar(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitInsn(L2I);
            mv.visitInsn(I2C);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.BYTE:
                mv.visitInsn(I2C);
                break;
            case TypeTags.FLOAT:
                mv.visitInsn(D2I);
                mv.visitInsn(I2C);
                break;
            case TypeTags.HANDLE:
                mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, GET_VALUE_METHOD,
                        String.format("()L%s;", OBJECT), false);
                break;
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_INT_METHOD, String.format("(L%s;)J", OBJECT),
                        false);
                mv.visitInsn(L2I);
                mv.visitInsn(I2C);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java char'",
                        sourceType));
        }
    }

    private static void generateCheckCastBToJShort(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitInsn(L2I);
            mv.visitInsn(I2S);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.BYTE:
                mv.visitInsn(I2S);
                break;
            case TypeTags.FLOAT:
                mv.visitInsn(D2I);
                mv.visitInsn(I2S);
                break;
            case TypeTags.HANDLE:
                mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, GET_VALUE_METHOD,
                        String.format("()L%s;", OBJECT), false);
                break;
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_INT_METHOD, String.format("(L%s;)J", OBJECT),
                        false);
                mv.visitInsn(L2I);
                mv.visitInsn(I2S);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java short'",
                        sourceType));
        }
    }

    private static void generateCheckCastBToJInt(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitInsn(L2I);
            return;
        }
        switch (sourceType.tag) {
            case TypeTags.BYTE:
                break;
            case TypeTags.FLOAT:
                mv.visitInsn(D2I);
                break;
            case TypeTags.HANDLE:
                mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, GET_VALUE_METHOD,
                        String.format("()L%s;", OBJECT), false);
                break;
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_INT_METHOD, String.format("(L%s;)J", OBJECT),
                        false);
                mv.visitInsn(L2I);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java int'",
                        sourceType));
        }

    }

    private static void generateCheckCastBToJLong(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.BYTE:
                mv.visitInsn(I2L);
                break;
            case TypeTags.FLOAT:
                mv.visitInsn(D2L);
                break;
            case TypeTags.HANDLE:
                mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, GET_VALUE_METHOD,
                        String.format("()L%s;", OBJECT), false);
                mv.visitTypeInsn(CHECKCAST, LONG_VALUE);
                mv.visitMethodInsn(INVOKEVIRTUAL, LONG_VALUE, "longValue", "()J", false);
                break;
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_INT_METHOD, String.format("(L%s;)J", OBJECT),
                        false);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java long'",
                        sourceType));
        }
    }

    private static void generateCheckCastBToJFloat(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitInsn(L2F);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.BYTE:
                mv.visitInsn(I2F);
                break;
            case TypeTags.FLOAT:
                mv.visitInsn(D2F);
                break;
            case TypeTags.HANDLE:
                mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, GET_VALUE_METHOD,
                        String.format("()L%s;", OBJECT), false);
                break;
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_FLOAT_METHOD, String.format("(L%s;)D", OBJECT),
                        false);
                mv.visitInsn(D2F);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'java float'",
                        sourceType));
        }
    }

    private static void generateCheckCastBToJDouble(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitInsn(L2D);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.BYTE:
                mv.visitInsn(I2D);
                break;
            case TypeTags.FLOAT:
                // do nothing
                break;
            case TypeTags.HANDLE:
                mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, GET_VALUE_METHOD,
                        String.format("()L%s;", OBJECT), false);
                break;
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_FLOAT_METHOD, String.format("(L%s;)D", OBJECT),
                        false);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' " +
                        "to 'java double'", sourceType));
        }
    }

    private static void generateCheckCastBToJRef(MethodVisitor mv, BType sourceType, JType targetType) {

        if (sourceType.tag == TypeTags.DECIMAL) {
            return;
        }

        if (sourceType.tag == TypeTags.HANDLE) {
            if (targetType.jTag == JTypeTags.JREF) {
                JType.JRefType jRefType = (JType.JRefType) targetType;
                if (jRefType.typeValue.equals(HANDLE_VALUE) || jRefType.typeValue.equals(B_HANDLE)) {
                    return;
                }
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, HANDLE_VALUE, GET_VALUE_METHOD,
                    String.format("()L%s;", OBJECT), false);
            String sig = getSignatureForJType(targetType);
            mv.visitTypeInsn(CHECKCAST, sig);
        } else {
            if (targetType.jTag == JTypeTags.JREF) {
                addBoxInsn(mv, sourceType);
                mv.visitTypeInsn(CHECKCAST, ((JType.JRefType) targetType).typeValue);
            } else {
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to '%s'",
                        sourceType, targetType));
            }
        }
    }

    private static void generateJToBCheckCast(MethodVisitor mv, BIRVarToJVMIndexMap indexMap, JType sourceType,
                                              BType targetType) {

        if (TypeTags.isIntegerTypeTag(targetType.tag)) {
            generateCheckCastJToBInt(mv, sourceType);
            return;
        }

        switch (targetType.tag) {
            case TypeTags.FLOAT:
                generateCheckCastJToBFloat(mv, sourceType);
                break;
            case TypeTags.DECIMAL:
                generateCheckCastJToBDecimal(mv, sourceType);
                break;
            case TypeTags.BOOLEAN:
                generateCheckCastJToBBoolean(mv, sourceType);
                break;
            case TypeTags.BYTE:
                generateCheckCastJToBByte(mv, sourceType);
                break;
            case TypeTags.NIL:
            case TypeTags.NEVER:
                break;
            default:
                switch (targetType.tag) {
                    case TypeTags.UNION:
                        generateCheckCastJToBUnionType(mv, indexMap, sourceType, (BUnionType) targetType);
                        break;
                    case TypeTags.ANYDATA:
                        generateCheckCastJToBAnyData(mv, indexMap, sourceType);
                        break;
                    case TypeTags.HANDLE:
                        generateJCastToBHandle(mv);
                        break;
                    case TypeTags.ANY:
                        generateJCastToBAny(mv, indexMap, sourceType, targetType);
                        break;
                    case TypeTags.JSON:
                        generateCheckCastJToBJSON(mv, indexMap, sourceType);
                        break;
                    case TypeTags.READONLY:
                        generateCheckCastJToBReadOnly(mv, indexMap, sourceType);
                        break;
                    case TypeTags.FINITE:
                        generateCheckCastJToBFiniteType(mv, indexMap, sourceType, targetType);
                        break;
                }

                checkCast(mv, targetType);
                String targetTypeClass = getTargetClass(targetType);
                if (targetTypeClass != null) {
                    mv.visitTypeInsn(CHECKCAST, targetTypeClass);
                }
                break;
        }
    }

    private static void generateCheckCastJToBInt(MethodVisitor mv, JType sourceType) {

        switch (sourceType.jTag) {
            case JTypeTags.JBYTE:
                mv.visitInsn(I2B);
                mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, JVM_TO_UNSIGNED_INT_METHOD, "(B)I", false);
                mv.visitInsn(I2L);
                break;
            case JTypeTags.JCHAR:
                mv.visitInsn(I2L);
                break;
            case JTypeTags.JSHORT:
                mv.visitInsn(I2L);
                break;
            case JTypeTags.JINT:
                mv.visitInsn(I2L);
                break;
            case JTypeTags.JLONG:
                break;
            case JTypeTags.JREF:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJLong", String.format("(L%s;)J", OBJECT), false);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'int'",
                        sourceType));
        }
    }

    private static void generateCheckCastJToBFloat(MethodVisitor mv, JType sourceType) {

        switch (sourceType.jTag) {
            case JTypeTags.JBYTE:
                mv.visitInsn(I2B);
                mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, JVM_TO_UNSIGNED_INT_METHOD, "(B)I", false);
                mv.visitInsn(I2D);
                break;
            case JTypeTags.JCHAR:
                mv.visitInsn(I2D);
                break;
            case JTypeTags.JSHORT:
                mv.visitInsn(I2D);
                break;
            case JTypeTags.JINT:
                mv.visitInsn(I2D);
                break;
            case JTypeTags.JLONG:
                mv.visitInsn(L2D);
                break;
            case JTypeTags.JFLOAT:
                mv.visitInsn(F2D);
                break;
            case JTypeTags.JDOUBLE:
                break;
            case JTypeTags.JREF:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJDouble", String.format("(L%s;)D", OBJECT), false);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'float'",
                        sourceType));
        }
    }

    private static void generateCheckCastJToBDecimal(MethodVisitor mv, JType sourceType) {

        switch (sourceType.jTag) {
            case JTypeTags.JBYTE:
                mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, DECIMAL_VALUE_OF_J_METHOD,
                        String.format("(B)L%s;", DECIMAL_VALUE),
                        false);
                break;
            case JTypeTags.JCHAR:
                mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, DECIMAL_VALUE_OF_J_METHOD,
                        String.format("(C)L%s;", DECIMAL_VALUE),
                        false);
                break;
            case JTypeTags.JSHORT:
                mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, DECIMAL_VALUE_OF_J_METHOD,
                        String.format("(S)L%s;", DECIMAL_VALUE),
                        false);
                break;
            case JTypeTags.JINT:
                mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, DECIMAL_VALUE_OF_J_METHOD,
                        String.format("(I)L%s;", DECIMAL_VALUE),
                        false);
                break;
            case JTypeTags.JLONG:
                mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, DECIMAL_VALUE_OF_J_METHOD,
                        String.format("(J)L%s;", DECIMAL_VALUE),
                        false);
                break;
            case JTypeTags.JFLOAT:
                mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, DECIMAL_VALUE_OF_J_METHOD,
                        String.format("(F)L%s;", DECIMAL_VALUE),
                        false);
                break;
            case JTypeTags.JDOUBLE:
                mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, DECIMAL_VALUE_OF_J_METHOD,
                        String.format("(D)L%s;", DECIMAL_VALUE),
                        false);
                break;
            case JTypeTags.JREF:
                mv.visitTypeInsn(CHECKCAST, DECIMAL_VALUE);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'decimal'",
                        sourceType));
        }
    }

    private static void generateCheckCastJToBBoolean(MethodVisitor mv, JType sourceType) {

        if (sourceType.jTag == JTypeTags.JBOOLEAN) {
            return;
        }

        if (sourceType.jTag == JTypeTags.JREF) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_BOOLEAN_METHOD, String.format("(L%s;)Z", OBJECT),
                    false);
        } else {
            throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'boolean'",
                    sourceType));
        }
    }

    private static void generateCheckCastJToBByte(MethodVisitor mv, JType sourceType) {

        if (sourceType.jTag == JTypeTags.JBYTE) {
            return;
        }

        if (sourceType.jTag == JTypeTags.JREF) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_BYTE_METHOD, String.format("(L%s;)I", OBJECT), false);
        } else {
            throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'byte'", sourceType));
        }
    }

    private static void generateCheckCastJToBUnionType(MethodVisitor mv, BIRVarToJVMIndexMap indexMap, JType sourceType,
                                                       BUnionType targetType) {

        generateJCastToBAny(mv, indexMap, sourceType, targetType);
    }

    private static void generateCheckCastJToBAnyData(MethodVisitor mv, BIRVarToJVMIndexMap indexMap, JType sourceType) {

        if (!(sourceType.jTag == JTypeTags.JREF || sourceType.jTag == JTypeTags.JARRAY)) {
            // if value types, then ad box instruction
            generateJCastToBAny(mv, indexMap, sourceType, symbolTable.anydataType);
        }
    }

    private static void generateJCastToBHandle(MethodVisitor mv) {

        // If the returned value is a HandleValue, do nothing
        LabelGenerator labelGen = new LabelGenerator();
        Label afterHandle = labelGen.getLabel("after_handle");
        mv.visitInsn(DUP);
        mv.visitTypeInsn(INSTANCEOF, B_HANDLE);
        mv.visitJumpInsn(IFNE, afterHandle);

        // Otherwise wrap it with a HandleValue
        mv.visitMethodInsn(INVOKESTATIC, HANDLE_VALUE, DECIMAL_VALUE_OF_J_METHOD,
                String.format("(L%s;)L%s;", OBJECT, HANDLE_VALUE), false);
        mv.visitLabel(afterHandle);
    }

    private static void generateJCastToBAny(MethodVisitor mv, BIRVarToJVMIndexMap indexMap, JType sourceType,
                                            BType targetType) {

        switch (sourceType.jTag) {
            case JTypeTags.JBOOLEAN:
                mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, VALUE_OF_METHOD, String.format("(Z)L%s;",
                        BOOLEAN_VALUE), false);
                break;
            case JTypeTags.JBYTE:
                mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, VALUE_OF_METHOD, String.format("(I)L%s;", INT_VALUE),
                        false);
                break;
            case JTypeTags.JCHAR:
            case JTypeTags.JSHORT:
            case JTypeTags.JINT:
                mv.visitInsn(I2L);
                mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, VALUE_OF_METHOD, String.format("(J)L%s;", LONG_VALUE),
                        false);
                break;
            case JTypeTags.JLONG:
                mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, VALUE_OF_METHOD, String.format("(J)L%s;", LONG_VALUE),
                        false);
                break;
            case JTypeTags.JFLOAT:
                mv.visitInsn(F2D);
                mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, VALUE_OF_METHOD, String.format("(D)L%s;", DOUBLE_VALUE),
                        false);
                break;
            case JTypeTags.JDOUBLE:
                mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, VALUE_OF_METHOD, String.format("(D)L%s;", DOUBLE_VALUE),
                        false);
                break;
            case JTypeTags.JREF:
                Label afterHandle = new Label();
                if (((JType.JRefType) sourceType).typeValue.equals(OBJECT)) {
                    mv.visitInsn(DUP);
                    mv.visitTypeInsn(INSTANCEOF, BERROR);
                    mv.visitJumpInsn(IFNE, afterHandle);

                    mv.visitInsn(DUP);
                    mv.visitTypeInsn(INSTANCEOF, NUMBER);
                    mv.visitJumpInsn(IFNE, afterHandle);

                    mv.visitInsn(DUP);
                    mv.visitTypeInsn(INSTANCEOF, BOOLEAN_VALUE);
                    mv.visitJumpInsn(IFNE, afterHandle);

                    mv.visitInsn(DUP);
                    mv.visitTypeInsn(INSTANCEOF, SIMPLE_VALUE);
                    mv.visitJumpInsn(IFNE, afterHandle);
                }

                if (isNillable(targetType)) {
                    mv.visitInsn(DUP);
                    mv.visitJumpInsn(IFNULL, afterHandle);
                }

                mv.visitInsn(DUP);
                mv.visitTypeInsn(INSTANCEOF, REF_VALUE);
                mv.visitJumpInsn(IFNE, afterHandle);

                BIRVariableDcl retJObjectVarDcl = new BIRVariableDcl(null, symbolTable.anyType,
                        new Name("$_ret_jobject_val_$"), VarScope.FUNCTION, VarKind.LOCAL, "");
                int returnJObjectVarRefIndex = indexMap.addToMapIfNotFoundAndGetIndex(retJObjectVarDcl);
                mv.visitVarInsn(ASTORE, returnJObjectVarRefIndex);
                mv.visitTypeInsn(NEW, HANDLE_VALUE);
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, returnJObjectVarRefIndex);
                mv.visitMethodInsn(INVOKESPECIAL, HANDLE_VALUE, JVM_INIT_METHOD, "(Ljava/lang/Object;)V", false);
                mv.visitLabel(afterHandle);
                break;
            case JTypeTags.JARRAY:
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'any'",
                        sourceType));
        }
    }

    private static boolean isNillable(BType targetType) {

        switch (targetType.tag) {
            case TypeTags.NIL:
            case TypeTags.NEVER:
            case TypeTags.JSON:
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.READONLY:
                return true;
            case TypeTags.UNION:
            case TypeTags.INTERSECTION:
                return targetType.isNullable();
            case TypeTags.FINITE:
                return targetType.isNullable();
        }

        return false;
    }

    private static void generateCheckCastJToBJSON(MethodVisitor mv, BIRVarToJVMIndexMap indexMap, JType sourceType) {

        if (sourceType.jTag == JTypeTags.JREF || sourceType.jTag == JTypeTags.JARRAY) {
            return;
        }

        // if value types, then ad box instruction
        generateJCastToBAny(mv, indexMap, sourceType, symbolTable.jsonType);
    }

    private static void generateCheckCastJToBReadOnly(MethodVisitor mv, BIRVarToJVMIndexMap indexMap,
                                                      JType sourceType) {

        if (sourceType.jTag == JTypeTags.JREF || sourceType.jTag == JTypeTags.JARRAY) {
            return;
        }

        // if value types, then add box instruction
        generateJCastToBAny(mv, indexMap, sourceType, symbolTable.readonlyType);
    }

    private static void generateCheckCastJToBFiniteType(MethodVisitor mv, BIRVarToJVMIndexMap indexMap,
                                                        JType sourceType, BType targetType) {
        // Finite types are stored in ref registry at ballerina side. Therefore if the return
        // type if a primitive, then add a box instruction.
        if (!(sourceType.jTag == JTypeTags.JREF || sourceType.jTag == JTypeTags.JARRAY)) {
            generateJCastToBAny(mv, indexMap, sourceType, targetType);
        }
    }

    static void generateCheckCast(MethodVisitor mv, BType sourceType, BType targetType, BIRVarToJVMIndexMap indexMap) {

        if (TypeTags.isXMLTypeTag(sourceType.tag) && targetType.tag == TypeTags.MAP) {
            generateXMLToAttributesMap(mv);
            return;
        } else if (sourceType.tag == TypeTags.RECORD && (targetType.tag == TypeTags.MAP
                && ((BMapType) targetType).constraint.tag == TypeTags.ANY)) {
            // do nothing
        } else {
            switch (targetType.tag) {
                case TypeTags.INT:
                    generateCheckCastToInt(mv, sourceType);
                    return;
                case TypeTags.SIGNED32_INT:
                    generateCheckCastToSigned32(mv, sourceType);
                    return;
                case TypeTags.SIGNED16_INT:
                    generateCheckCastToSigned16(mv, sourceType);
                    return;
                case TypeTags.SIGNED8_INT:
                    generateCheckCastToSigned8(mv, sourceType);
                    return;
                case TypeTags.UNSIGNED32_INT:
                    generateCheckCastToUnsigned32(mv, sourceType);
                    return;
                case TypeTags.UNSIGNED16_INT:
                    generateCheckCastToUnsigned16(mv, sourceType);
                    return;
                case TypeTags.UNSIGNED8_INT:
                    generateCheckCastToUnsigned8(mv, sourceType);
                    return;
                case TypeTags.FLOAT:
                    generateCheckCastToFloat(mv, sourceType);
                    return;
                case TypeTags.STRING:
                    generateCheckCastToString(mv, sourceType, indexMap);
                    return;
                case TypeTags.CHAR_STRING:
                    generateCheckCastToChar(mv, sourceType);
                    return;
                case TypeTags.DECIMAL:
                    generateCheckCastToDecimal(mv, sourceType);
                    return;
                case TypeTags.BOOLEAN:
                    generateCheckCastToBoolean(mv, sourceType);
                    return;
                case TypeTags.BYTE:
                    generateCheckCastToByte(mv, sourceType);
                    return;
                case TypeTags.NIL:
                case TypeTags.NEVER:
                    checkCast(mv, targetType);
                    return;
                case TypeTags.UNION:
                    generateCheckCastToUnionType(mv, sourceType, (BUnionType) targetType);
                    return;
                case TypeTags.INTERSECTION:
                    generateCheckCast(mv, sourceType, ((BIntersectionType) targetType).effectiveType, indexMap);
                    return;
                case TypeTags.ANYDATA:
                    generateCheckCastToAnyData(mv, sourceType);
                    return;
                case TypeTags.ANY:
                    generateCastToAny(mv, sourceType);
                    return;
                case TypeTags.JSON:
                    generateCheckCastToJSON(mv, sourceType);
                    return;
                case TypeTags.READONLY:
                    generateCheckCastToReadonlyType(mv, sourceType, targetType);
                    return;
                case TypeTags.FINITE:
                    generateCheckCastToFiniteType(mv, sourceType, (BFiniteType) targetType);
                    return;
                default:
                    // do the ballerina checkcast
                    checkCast(mv, targetType);
                    break;
            }
        }

        // cast to the specific java class
        String targetTypeClass = getTargetClass(targetType);
        if (targetTypeClass != null) {
            mv.visitTypeInsn(CHECKCAST, targetTypeClass);
        }
    }

    private static void generateCheckCastToInt(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.BYTE:
                mv.visitInsn(I2B);
                mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, JVM_TO_UNSIGNED_INT_METHOD, "(B)I", false);
                mv.visitInsn(I2L);
                break;
            case TypeTags.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "floatToInt", "(D)J", false);
                break;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.DECIMAL:
            case TypeTags.JSON:
            case TypeTags.READONLY:
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_INT_METHOD, String.format("(L%s;)J", OBJECT),
                        false);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'int'",
                        sourceType));
        }
    }

    private static void generateCheckCastToSigned32(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "intToSigned32", "(J)J", false);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.BYTE:
                mv.visitInsn(I2B);
                mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, JVM_TO_UNSIGNED_INT_METHOD, "(B)I", false);
                mv.visitInsn(I2L);
                break;
            case TypeTags.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "floatToSigned32", "(D)J", false);
                break;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.DECIMAL:
            case TypeTags.JSON:
            case TypeTags.READONLY:
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToSigned32", String.format("(L%s;)J", OBJECT),
                        false);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' " +
                        "to 'int:Signed32'", sourceType));
        }
    }

    private static void generateCheckCastToSigned16(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "intToSigned16", "(J)J", false);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.BYTE:
                mv.visitInsn(I2B);
                mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, JVM_TO_UNSIGNED_INT_METHOD, "(B)I", false);
                mv.visitInsn(I2L);
                break;
            case TypeTags.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "floatToSigned16", "(D)J", false);
                break;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.DECIMAL:
            case TypeTags.JSON:
            case TypeTags.READONLY:
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToSigned16", String.format("(L%s;)J", OBJECT),
                        false);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' " +
                        "to 'int:Signed16'", sourceType));
        }
    }

    private static void generateCheckCastToSigned8(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "intToSigned8", "(J)J", false);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.BYTE:
                mv.visitInsn(I2B);
                mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, JVM_TO_UNSIGNED_INT_METHOD, "(B)I", false);
                mv.visitInsn(I2L);
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "intToSigned8", "(J)J", false);
                break;
            case TypeTags.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "floatToSigned8", "(D)J", false);
                break;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.DECIMAL:
            case TypeTags.JSON:
            case TypeTags.READONLY:
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToSigned8", String.format("(L%s;)J", OBJECT),
                        false);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s'" +
                        " to 'int:Signed8'", sourceType));
        }
    }

    private static void generateCheckCastToUnsigned32(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "intToUnsigned32", "(J)J", false);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.BYTE:
                mv.visitInsn(I2B);
                mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, JVM_TO_UNSIGNED_INT_METHOD, "(B)I", false);
                mv.visitInsn(I2L);
                break;
            case TypeTags.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "floatToUnsigned32", "(D)J", false);
                break;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.DECIMAL:
            case TypeTags.JSON:
            case TypeTags.READONLY:
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToUnsigned32", String.format("(L%s;)J", OBJECT),
                        false);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to " +
                        "'int:Unsigned32'", sourceType));
        }
    }

    private static void generateCheckCastToUnsigned16(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "intToUnsigned16", "(J)J", false);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.BYTE:
                mv.visitInsn(I2B);
                mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, JVM_TO_UNSIGNED_INT_METHOD, "(B)I", false);
                mv.visitInsn(I2L);
                break;
            case TypeTags.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "floatToUnsigned16", "(D)J", false);
                break;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.DECIMAL:
            case TypeTags.JSON:
            case TypeTags.READONLY:
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToUnsigned16", String.format("(L%s;)J", OBJECT),
                        false);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' " +
                                "to 'int:Unsigned16'", sourceType));
        }
    }

    private static void generateCheckCastToUnsigned8(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "intToUnsigned8", "(J)J", false);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.BYTE:
                mv.visitInsn(I2B);
                mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, JVM_TO_UNSIGNED_INT_METHOD, "(B)I", false);
                mv.visitInsn(I2L);
                break;
            case TypeTags.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "floatToUnsigned8", "(D)J", false);
                break;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.DECIMAL:
            case TypeTags.JSON:
            case TypeTags.READONLY:
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToUnsigned8", String.format("(L%s;)J", OBJECT),
                        false);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' " +
                                "to 'int:Unsigned8'", sourceType));
        }
    }

    private static void generateCheckCastToFloat(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitInsn(L2D);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.FLOAT:
                // do nothing
                break;
            case TypeTags.BYTE:
                mv.visitInsn(I2D);
                break;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.DECIMAL:
            case TypeTags.JSON:
            case TypeTags.READONLY:
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_FLOAT_METHOD, String.format("(L%s;)D", OBJECT),
                        false);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'float'",
                        sourceType));
        }
    }

    private static void generateCheckCastToDecimal(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, VALUE_OF_METHOD, String.format("(J)L%s;", DECIMAL_VALUE),
                    false);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.DECIMAL:
                // do nothing
                break;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.JSON:
            case TypeTags.READONLY:
            case TypeTags.FINITE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_DECIMAL_METHOD,
                        String.format("(L%s;)L%s;", OBJECT, DECIMAL_VALUE), false);
                break;
            case TypeTags.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, VALUE_OF_METHOD,
                        String.format("(D)L%s;", DECIMAL_VALUE), false);
                break;
            case TypeTags.BYTE:
                mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, VALUE_OF_METHOD,
                        String.format("(I)L%s;", DECIMAL_VALUE), false);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'decimal'",
                        sourceType));
        }
    }

    private static void generateCheckCastToString(MethodVisitor mv, BType sourceType, BIRVarToJVMIndexMap indexMap) {

        if (TypeTags.isStringTypeTag(sourceType.tag)) {
            return;
        } else if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, JVM_TO_STRING_METHOD,
                    String.format("(J)L%s;", STRING_VALUE), false);
        } else {
            switch (sourceType.tag) {
                case TypeTags.ANY:
                case TypeTags.ANYDATA:
                case TypeTags.UNION:
                case TypeTags.JSON:
                case TypeTags.READONLY:
                case TypeTags.FINITE:
                    checkCast(mv, symbolTable.stringType);
                    mv.visitTypeInsn(CHECKCAST, B_STRING_VALUE);
                    return;
                case TypeTags.FLOAT:
                    mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, JVM_TO_STRING_METHOD,
                            String.format("(D)L%s;", STRING_VALUE), false);
                    break;
                case TypeTags.BOOLEAN:
                    mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, JVM_TO_STRING_METHOD,
                            String.format("(Z)L%s;", STRING_VALUE), false);
                    break;
                case TypeTags.DECIMAL:
                    mv.visitMethodInsn(INVOKESTATIC, STRING_VALUE, VALUE_OF_METHOD, String.format("(L%s;)L%s;", OBJECT,
                            STRING_VALUE),
                            false);
                    break;
                default:
                    throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'string'",
                            sourceType));
            }
        }

        generateNonBMPStringValue(mv, indexMap);
    }

    private static void generateNonBMPStringValue(MethodVisitor mv, BIRVarToJVMIndexMap indexMap) {

        BIRVariableDcl strVar = new BIRVariableDcl(null, symbolTable.anyType,
                new Name("str"), VarScope.FUNCTION, VarKind.LOCAL, "");
        int tmpVarIndex = indexMap.addToMapIfNotFoundAndGetIndex(strVar);

        mv.visitVarInsn(ASTORE, tmpVarIndex);
        mv.visitTypeInsn(NEW, BMP_STRING_VALUE);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, tmpVarIndex);
        mv.visitMethodInsn(INVOKESPECIAL, BMP_STRING_VALUE, JVM_INIT_METHOD,
                String.format("(L%s;)V", STRING_VALUE), false);
    }

    private static void generateCheckCastToChar(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isStringTypeTag(sourceType.tag)) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "stringToChar",
                               String.format("(L%s;)L%s;", OBJECT, B_STRING_VALUE), false);
        } else if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.JSON ||
                sourceType.tag == TypeTags.READONLY ||
                sourceType.tag == TypeTags.FINITE ||
                TypeTags.isIntegerTypeTag(sourceType.tag) ||
                sourceType.tag == TypeTags.FLOAT ||
                sourceType.tag == TypeTags.BOOLEAN ||
                sourceType.tag == TypeTags.DECIMAL) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "anyToChar",
                               String.format("(L%s;)L%s;", OBJECT, B_STRING_VALUE), false);
        } else {
            throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'char'",
                    sourceType));
        }
    }

    private static void generateCheckCastToBoolean(MethodVisitor mv, BType sourceType) {

        if (sourceType.tag == TypeTags.BOOLEAN) {
            return;
        }

        if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.JSON ||
                sourceType.tag == TypeTags.READONLY ||
                sourceType.tag == TypeTags.FINITE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_BOOLEAN_METHOD, String.format("(L%s;)Z", OBJECT),
                    false);
        } else {
            throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'boolean'",
                    sourceType));
        }
    }

    static void generateCheckCastToByte(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "intToByte", "(J)I", false);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CONVERTER, "floatToByte", "(D)I", false);
                break;
            case TypeTags.DECIMAL:
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.FINITE:
            case TypeTags.JSON:
            case TypeTags.READONLY:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_BYTE_METHOD, String.format("(L%s;)I", OBJECT),
                        false);
                break;
            case TypeTags.BYTE:
                // do nothing
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'byte'",
                        sourceType));
        }
    }

    private static void generateCheckCastToAnyData(MethodVisitor mv, BType sourceType) {

        if (sourceType.tag == TypeTags.ANY || sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.INTERSECTION) {
            checkCast(mv, symbolTable.anydataType);
        } else {
            // if value types, then ad box instruction
            generateCastToAny(mv, sourceType);
        }
    }

    private static void generateCheckCastToJSON(MethodVisitor mv, BType sourceType) {

        if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.INTERSECTION ||
                sourceType.tag == TypeTags.READONLY ||
                sourceType.tag == TypeTags.MAP) {
            checkCast(mv, symbolTable.jsonType);
        } else {
            // if value types, then ad box instruction
            generateCastToAny(mv, sourceType);
        }
    }

    private static void generateCheckCastToUnionType(MethodVisitor mv, BType sourceType, BUnionType targetType) {

        generateCastToAny(mv, sourceType);
        checkCast(mv, targetType);
    }

    private static void checkCast(MethodVisitor mv, BType targetType) {

        loadType(mv, targetType);
        mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkCast",
                           String.format("(L%s;L%s;)L%s;", OBJECT, TYPE, OBJECT), false);
    }

    static String getTargetClass(BType targetType) {

        if (TypeTags.isXMLTypeTag(targetType.tag)) {
            return XML_VALUE;
        }

        String targetTypeClass;
        switch (targetType.tag) {
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                targetTypeClass = ARRAY_VALUE;
                break;
            case TypeTags.MAP:
            case TypeTags.RECORD:
                targetTypeClass = MAP_VALUE;
                break;
            case TypeTags.TABLE:
                targetTypeClass = TABLE_VALUE_IMPL;
                break;
            case TypeTags.STREAM:
                targetTypeClass = STREAM_VALUE;
                break;
            case TypeTags.OBJECT:
                targetTypeClass = B_OBJECT;
                break;
            case TypeTags.ERROR:
                targetTypeClass = ERROR_VALUE;
                break;
            case TypeTags.TYPEDESC:
                targetTypeClass = TYPEDESC_VALUE;
                break;
            case TypeTags.INVOKABLE:
                targetTypeClass = FUNCTION_POINTER;
                break;
            case TypeTags.FUTURE:
                targetTypeClass = FUTURE_VALUE;
                break;
            case TypeTags.HANDLE:
                targetTypeClass = HANDLE_VALUE;
                break;
            default:
                return null;
        }

        return targetTypeClass;
    }

    private static void generateCheckCastToFiniteType(MethodVisitor mv, BType sourceType, BFiniteType targetType) {

        generateCastToAny(mv, sourceType);
        checkCast(mv, targetType);
    }

    private static void generateCheckCastToReadonlyType(MethodVisitor mv, BType sourceType, BType targetType) {

        generateCastToAny(mv, sourceType);
        checkCast(mv, targetType);
    }

    static void addBoxInsn(MethodVisitor mv, BType bType) {
        if (bType != null) {
            generateCast(mv, bType, JvmInstructionGen.anyType);
        }
    }

    public static void addUnboxInsn(MethodVisitor mv, BType bType) {
        if (bType != null) {
            generateCast(mv, JvmInstructionGen.anyType, bType);
        }
    }
    // ------------------------------------------------------------------
    //   Generate Cast Methods - Performs cast without type checking
    // ------------------------------------------------------------------

    static void generateCast(MethodVisitor mv, BType sourceType, BType targetType) {

        if (TypeTags.isIntegerTypeTag(targetType.tag)) {
            generateCastToInt(mv, sourceType);
            return;
        } else if (TypeTags.isStringTypeTag(targetType.tag)) {
            generateCastToString(mv, sourceType);
            return;
        } else {
            switch (targetType.tag) {
                case TypeTags.FLOAT:
                    generateCastToFloat(mv, sourceType);
                    return;
                case TypeTags.BOOLEAN:
                    generateCastToBoolean(mv, sourceType);
                    return;
                case TypeTags.BYTE:
                    generateCastToByte(mv, sourceType);
                    return;
                case TypeTags.DECIMAL:
                    generateCastToDecimal(mv, sourceType);
                    return;
                case TypeTags.NIL:
                case TypeTags.NEVER:
                    return;
                case TypeTags.UNION:
                case TypeTags.ANYDATA:
                case TypeTags.ANY:
                case TypeTags.JSON:
                case TypeTags.READONLY:
                case TypeTags.FINITE:
                    generateCastToAny(mv, sourceType);
                    return;
                case TypeTags.INTERSECTION:
                    generateCast(mv, sourceType, ((BIntersectionType) targetType).effectiveType);
                    return;
            }
        }
        // cast to the specific java class
        String targetTypeClass = getTargetClass(targetType);
        if (targetTypeClass != null) {
            mv.visitTypeInsn(CHECKCAST, targetTypeClass);
        }
    }

    private static void generateCastToInt(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.BYTE:
                mv.visitInsn(I2L);
                break;
            case TypeTags.FLOAT:
                mv.visitInsn(D2L);
                break;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.JSON:
            case TypeTags.READONLY:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_INT_METHOD, String.format("(L%s;)J", OBJECT),
                        false);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'int'",
                        sourceType));
        }
    }

    private static void generateCastToFloat(MethodVisitor mv, BType sourceType) {

        if (sourceType.tag == TypeTags.FLOAT) {
            return;
        }

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitInsn(L2D);
        } else if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.JSON ||
                sourceType.tag == TypeTags.READONLY) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_FLOAT_METHOD, String.format("(L%s;)D", OBJECT),
                    false);
        } else {
            throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'float'",
                    sourceType));
        }
    }

    private static void generateCastToString(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isStringTypeTag(sourceType.tag)) {
            return;
        } else if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, JVM_TO_STRING_METHOD,
                    String.format("(J)L%s;", STRING_VALUE), false);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, JVM_TO_STRING_METHOD,
                        String.format("(D)L%s;", STRING_VALUE), false);
                break;
            case TypeTags.BOOLEAN:
                mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, JVM_TO_STRING_METHOD,
                        String.format("(Z)L%s;", STRING_VALUE), false);
                break;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.JSON:
            case TypeTags.READONLY:
                mv.visitTypeInsn(CHECKCAST, B_STRING_VALUE);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'string'",
                        sourceType));
        }
    }

    private static void generateCastToDecimal(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, VALUE_OF_METHOD, String.format("(J)L%s;", DECIMAL_VALUE),
                    false);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.DECIMAL:
                break;
            case TypeTags.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, DECIMAL_VALUE, VALUE_OF_METHOD,
                        String.format("(D)L%s;", DECIMAL_VALUE), false);
                break;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.JSON:
            case TypeTags.READONLY:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_DECIMAL_METHOD,
                        String.format("(L%s;)L%s;", OBJECT, DECIMAL_VALUE), false);
                break;
            default:
                throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'decimal'",
                        sourceType));
        }
    }

    private static void generateCastToBoolean(MethodVisitor mv, BType sourceType) {

        if (sourceType.tag == TypeTags.BOOLEAN) {
            return;
        }

        if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.JSON ||
                sourceType.tag == TypeTags.READONLY) {
            mv.visitTypeInsn(CHECKCAST, BOOLEAN_VALUE);
            mv.visitMethodInsn(INVOKEVIRTUAL, BOOLEAN_VALUE, "booleanValue", "()Z", false);
        } else {
            throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'boolean'",
                    sourceType));
        }
    }

    private static void generateCastToByte(MethodVisitor mv, BType sourceType) {

        if (sourceType.tag == TypeTags.BYTE) {
            return;
        }

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitInsn(L2I);
        } else if (sourceType.tag == TypeTags.ANY ||
                sourceType.tag == TypeTags.ANYDATA ||
                sourceType.tag == TypeTags.UNION ||
                sourceType.tag == TypeTags.JSON ||
                sourceType.tag == TypeTags.READONLY) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, ANY_TO_BYTE_METHOD, String.format("(L%s;)I", OBJECT), false);
        } else {
            throw new BLangCompilerException(String.format("Casting is not supported from '%s' to 'byte'", sourceType));
        }
    }

    private static void generateCastToAny(MethodVisitor mv, BType sourceType) {

        if (TypeTags.isIntegerTypeTag(sourceType.tag)) {
            mv.visitMethodInsn(INVOKESTATIC, LONG_VALUE, VALUE_OF_METHOD, String.format("(J)L%s;", LONG_VALUE), false);
            return;
        }

        switch (sourceType.tag) {
            case TypeTags.BYTE:
                mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, VALUE_OF_METHOD, String.format("(I)L%s;", INT_VALUE),
                        false);
                break;
            case TypeTags.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, DOUBLE_VALUE, VALUE_OF_METHOD, String.format("(D)L%s;", DOUBLE_VALUE),
                        false);
                break;
            case TypeTags.BOOLEAN:
                mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_VALUE, VALUE_OF_METHOD,
                                   String.format("(Z)L%s;", BOOLEAN_VALUE), false);
                break;
        }
    }

    private static void generateXMLToAttributesMap(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "getAttributesMap", String.format("()L%s;", MAP_VALUE), false);
    }

    private JvmCastGen() {
    }
}
