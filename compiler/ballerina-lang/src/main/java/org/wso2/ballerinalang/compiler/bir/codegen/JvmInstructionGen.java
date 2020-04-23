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
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JType;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRGlobalVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FSTORE;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.I2B;
import static org.objectweb.asm.Opcodes.I2S;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.generateCast;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BYTE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SHORT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_CHECKER;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getPackageName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lookupGlobalVarClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.symbolTable;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeDesc;

/**
 * BIR instructions to JVM byte code generation related methods.
 *
 * @since 1.2.0
 */
public class JvmInstructionGen {

    public static final String I_STRING_VALUE = "org/ballerinalang/jvm/values/StringValue";
    public static final String B_STRING_VALUE = "org/ballerinalang/jvm/values/api/BString";
    public static final String NON_BMP_STRING_VALUE = "org/ballerinalang/jvm/values/NonBmpStringValue";
    public static boolean isBString = false;

    public static void addBoxInsn(MethodVisitor mv, BType bType) {

        if (bType != null) {
            generateCast(mv, bType, symbolTable.anyType);
        }
    }

    public static void addUnboxInsn(MethodVisitor mv, BType bType) {

        if (bType != null) {
            generateCast(mv, symbolTable.anyType, bType);
        }
    }

    public static void addJUnboxInsn(MethodVisitor mv, JType jType) {

        if (jType == null) {
            return;
        }
        if (jType.jTag == JTypeTags.JBYTE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJByte", String.format("(L%s;)B", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JCHAR) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJChar", String.format("(L%s;)C", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JSHORT) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJShort", String.format("(L%s;)S", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JINT) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJInt", String.format("(L%s;)I", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JLONG) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJLong", String.format("(L%s;)J", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JFLOAT) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJFloat", String.format("(L%s;)F", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JDOUBLE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJDouble", String.format("(L%s;)D", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JBOOLEAN) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJBoolean", String.format("(L%s;)Z", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JREF) {
            mv.visitTypeInsn(CHECKCAST, ((JType.JRefType) jType).typeValue);
            //} else {
            //    error err = error(io:sprintf("Unboxing is not supported for '%s'", bType));
            //    panic err;
        }
    }

    public static void generateVarLoad(MethodVisitor mv, BIRVariableDcl varDcl, String currentPackageName,
                                       int valueIndex) {

        BType bType = varDcl.type;

        if (varDcl.kind == VarKind.GLOBAL) {
            BIRGlobalVariableDcl globalVar = (BIRGlobalVariableDcl) varDcl;
            PackageID modId = globalVar.pkgId;
            String moduleName = getPackageName(modId.orgName, modId.name);

            String varName = varDcl.name.value;
            String className = lookupGlobalVarClassName(moduleName, varName);

            String typeSig = getTypeDesc(bType);
            mv.visitFieldInsn(GETSTATIC, className, varName, typeSig);
            return;
        } else if (varDcl.kind == VarKind.SELF) {
            mv.visitVarInsn(ALOAD, 0);
            return;
        } else if (varDcl.kind == VarKind.CONSTANT) {
            String varName = varDcl.name.value;
            PackageID moduleId = ((BIRGlobalVariableDcl) varDcl).pkgId;
            String pkgName = getPackageName(moduleId.orgName, moduleId.name);
            String className = lookupGlobalVarClassName(pkgName, varName);
            String typeSig = getTypeDesc(bType);
            mv.visitFieldInsn(GETSTATIC, className, varName, typeSig);
            return;
        }

        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            mv.visitVarInsn(LLOAD, valueIndex);
        } else if (bType.tag == TypeTags.BYTE) {
            mv.visitVarInsn(ILOAD, valueIndex);
            mv.visitInsn(I2B);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "toUnsignedInt", "(B)I", false);
        } else if (bType.tag == TypeTags.FLOAT) {
            mv.visitVarInsn(DLOAD, valueIndex);
        } else if (bType.tag == TypeTags.BOOLEAN) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (bType.tag == TypeTags.ARRAY ||
                TypeTags.isStringTypeTag(bType.tag) ||
                bType.tag == TypeTags.MAP ||
                bType.tag == TypeTags.STREAM ||
                bType.tag == TypeTags.ANY ||
                bType.tag == TypeTags.ANYDATA ||
                bType.tag == TypeTags.NIL ||
                bType.tag == TypeTags.UNION ||
                bType.tag == TypeTags.TUPLE ||
                bType.tag == TypeTags.RECORD ||
                bType.tag == TypeTags.ERROR ||
                bType.tag == TypeTags.JSON ||
                bType.tag == TypeTags.FUTURE ||
                bType.tag == TypeTags.OBJECT ||
                bType.tag == TypeTags.DECIMAL ||
                TypeTags.isXMLTypeTag(bType.tag) ||
                bType.tag == TypeTags.INVOKABLE ||
                bType.tag == TypeTags.FINITE ||
                bType.tag == TypeTags.HANDLE ||
                bType.tag == TypeTags.TYPEDESC ||
                bType.tag == TypeTags.READONLY) {
            mv.visitVarInsn(ALOAD, valueIndex);
        } else if (bType.tag == JTypeTags.JTYPE) {
            generateJVarLoad(mv, (JType) bType, currentPackageName, valueIndex);
        } else {
            throw new BLangCompilerException("JVM generation is not supported for type " + String.format("%s", bType));
        }
    }

    private static void generateJVarLoad(MethodVisitor mv, JType jType, String currentPackageName, int valueIndex) {

        if (jType.jTag == JTypeTags.JBYTE) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JCHAR) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JSHORT) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JINT) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JLONG) {
            mv.visitVarInsn(LLOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JFLOAT) {
            mv.visitVarInsn(FLOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JDOUBLE) {
            mv.visitVarInsn(DLOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JBOOLEAN) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JARRAY ||
                jType.jTag == JTypeTags.JREF) {
            mv.visitVarInsn(ALOAD, valueIndex);
        } else {
            throw new BLangCompilerException("JVM generation is not supported for type " + String.format("%s", jType));
        }
    }

    public static void generateVarStore(MethodVisitor mv, BIRVariableDcl varDcl, String currentPackageName,
                                        int valueIndex) {

        BType bType = varDcl.type;

        if (varDcl.kind == VarKind.GLOBAL) {
            String varName = varDcl.name.value;
            String className = lookupGlobalVarClassName(currentPackageName, varName);
            String typeSig = getTypeDesc(bType);
            mv.visitFieldInsn(PUTSTATIC, className, varName, typeSig);
            return;
        } else if (varDcl.kind == VarKind.CONSTANT) {
            String varName = varDcl.name.value;
            PackageID moduleId = ((BIRGlobalVariableDcl) varDcl).pkgId;
            String pkgName = getPackageName(moduleId.orgName, moduleId.name);
            String className = lookupGlobalVarClassName(pkgName, varName);
            String typeSig = getTypeDesc(bType);
            mv.visitFieldInsn(PUTSTATIC, className, varName, typeSig);
            return;
        }

        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            mv.visitVarInsn(LSTORE, valueIndex);
        } else if (bType.tag == TypeTags.BYTE) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (bType.tag == TypeTags.FLOAT) {
            mv.visitVarInsn(DSTORE, valueIndex);
        } else if (bType.tag == TypeTags.BOOLEAN) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (bType.tag == TypeTags.ARRAY ||
                TypeTags.isStringTypeTag(bType.tag) ||
                bType.tag == TypeTags.MAP ||
                bType.tag == TypeTags.STREAM ||
                bType.tag == TypeTags.ANY ||
                bType.tag == TypeTags.ANYDATA ||
                bType.tag == TypeTags.NIL ||
                bType.tag == TypeTags.UNION ||
                bType.tag == TypeTags.TUPLE ||
                bType.tag == TypeTags.DECIMAL ||
                bType.tag == TypeTags.RECORD ||
                bType.tag == TypeTags.ERROR ||
                bType.tag == TypeTags.JSON ||
                bType.tag == TypeTags.FUTURE ||
                bType.tag == TypeTags.OBJECT ||
                bType.tag == TypeTags.SERVICE ||
                TypeTags.isXMLTypeTag(bType.tag) ||
                bType.tag == TypeTags.INVOKABLE ||
                bType.tag == TypeTags.FINITE ||
                bType.tag == TypeTags.HANDLE ||
                bType.tag == TypeTags.TYPEDESC ||
                bType.tag == TypeTags.READONLY) {
            mv.visitVarInsn(ASTORE, valueIndex);
        } else if (bType.tag == JTypeTags.JTYPE) {
            generateJVarStore(mv, (JType) bType, currentPackageName, valueIndex);
        } else {
            throw new BLangCompilerException("JVM generation is not supported for type " + String.format("%s", bType));
        }
    }

    private static void generateJVarStore(MethodVisitor mv, JType jType, String currentPackageName, int valueIndex) {

        if (jType.jTag == JTypeTags.JBYTE) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JCHAR) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JSHORT) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JINT) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JLONG) {
            mv.visitVarInsn(LSTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JFLOAT) {
            mv.visitVarInsn(FSTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JDOUBLE) {
            mv.visitVarInsn(DSTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JBOOLEAN) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JARRAY ||
                jType.jTag == JTypeTags.JREF) {
            mv.visitVarInsn(ASTORE, valueIndex);
        } else {
            throw new BLangCompilerException("JVM generation is not supported for type " + String.format("%s", jType));
        }
    }

    private static int[] listHighSurrogates(String str) {

        List<Integer> highSurrogates = new ArrayList<>();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isHighSurrogate(c)) {
                highSurrogates.add(i - highSurrogates.size());
            }
        }
        int[] highSurrogatesArr = new int[highSurrogates.size()];
        for (int i = 0; i < highSurrogates.size(); i++) {
            Integer highSurrogate = highSurrogates.get(i);
            highSurrogatesArr[i] = highSurrogate;
        }
        return highSurrogatesArr;
    }

    public static void loadConstantValue(BType bType, Object constVal, MethodVisitor mv) {

        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            long intValue = constVal instanceof Long ? (long) constVal : Long.parseLong(String.valueOf(constVal));
            mv.visitLdcInsn(intValue);
        } else if (bType.tag == TypeTags.BYTE) {
            int byteValue = ((Number) constVal).intValue();
            mv.visitLdcInsn(byteValue);
        } else if (bType.tag == TypeTags.FLOAT) {
            double doubleValue = constVal instanceof Double ? (double) constVal :
                    Double.parseDouble(String.valueOf(constVal));
            mv.visitLdcInsn(doubleValue);
        } else if (bType.tag == TypeTags.BOOLEAN) {
            boolean booleanVal = constVal instanceof Boolean ? (boolean) constVal :
                    Boolean.parseBoolean(String.valueOf(constVal));
            mv.visitLdcInsn(booleanVal);
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
            String val = String.valueOf(constVal);
            if (isBString) {
                int[] highSurrogates = listHighSurrogates(val);

                mv.visitTypeInsn(NEW, NON_BMP_STRING_VALUE);
                mv.visitInsn(DUP);
                mv.visitLdcInsn(val);
                mv.visitIntInsn(BIPUSH, highSurrogates.length);
                mv.visitIntInsn(NEWARRAY, T_INT);

                int i = 0;
                for (int ch : highSurrogates) {
                    mv.visitInsn(DUP);
                    mv.visitIntInsn(BIPUSH, i);
                    mv.visitIntInsn(BIPUSH, ch);
                    i = i + 1;
                    mv.visitInsn(IASTORE);
                }
                mv.visitMethodInsn(INVOKESPECIAL, NON_BMP_STRING_VALUE, "<init>",
                        String.format("(L%s;[I)V", STRING_VALUE), false);
            } else {
                mv.visitLdcInsn(val);
            }
        } else if (bType.tag == TypeTags.DECIMAL) {
            mv.visitTypeInsn(NEW, DECIMAL_VALUE);
            mv.visitInsn(DUP);
            mv.visitLdcInsn(String.valueOf(constVal));
            mv.visitMethodInsn(INVOKESPECIAL, DECIMAL_VALUE, "<init>", String.format("(L%s;)V", STRING_VALUE), false);
        } else if (bType.tag == TypeTags.NIL) {
            mv.visitInsn(ACONST_NULL);
        } else {
            throw new BLangCompilerException("JVM generation is not supported for type : " +
                    String.format("%s", bType));
        }
    }

    public static void generateIntToUnsignedIntConversion(MethodVisitor mv, BType targetType) {
        switch (targetType.tag) {
            case TypeTags.BYTE: // Wouldn't reach here for int atm.
            case TypeTags.UNSIGNED8_INT:
                mv.visitInsn(L2I);
                mv.visitInsn(I2B);
                mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, "toUnsignedLong", "(B)J", false);
                return;
            case TypeTags.UNSIGNED16_INT:
                mv.visitInsn(L2I);
                mv.visitInsn(I2S);
                mv.visitMethodInsn(INVOKESTATIC, SHORT_VALUE, "toUnsignedLong", "(S)J", false);
                return;
            case TypeTags.UNSIGNED32_INT:
                mv.visitInsn(L2I);
                mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, "toUnsignedLong", "(I)J", false);
        }
    }

    public static BType getSmallerUnsignedIntSubType(BType lhsType, BType rhsType) {
        if (TypeTags.isSignedIntegerTypeTag(lhsType.tag) || TypeTags.isSignedIntegerTypeTag(rhsType.tag)) {
            throw new BLangCompilerException("expected two unsigned int subtypes, found '" + lhsType + "' and '" +
                                                     rhsType + "'");
        }

        if (lhsType.tag == TypeTags.BYTE || rhsType.tag == TypeTags.BYTE) {
            return symbolTable.unsigned8IntType;
        }

        if (lhsType.tag == TypeTags.UNSIGNED8_INT || rhsType.tag == TypeTags.UNSIGNED8_INT) {
            return symbolTable.unsigned8IntType;
        }

        if (lhsType.tag == TypeTags.UNSIGNED16_INT || rhsType.tag == TypeTags.UNSIGNED16_INT) {
            return symbolTable.unsigned16IntType;
        }

        return symbolTable.unsigned32IntType;
    }
}
