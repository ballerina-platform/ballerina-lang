/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.codegen;

import io.ballerina.runtime.IdentifierUtils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JCast;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JInsKind;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JInstruction;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JType;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.FieldAccess;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewTable;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SchedulerPolicy;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DADD;
import static org.objectweb.asm.Opcodes.DCMPL;
import static org.objectweb.asm.Opcodes.DDIV;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DMUL;
import static org.objectweb.asm.Opcodes.DNEG;
import static org.objectweb.asm.Opcodes.DREM;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.DSUB;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FSTORE;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.I2B;
import static org.objectweb.asm.Opcodes.I2L;
import static org.objectweb.asm.Opcodes.I2S;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IAND;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGE;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.IF_ICMPGT;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INEG;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IOR;
import static org.objectweb.asm.Opcodes.ISHL;
import static org.objectweb.asm.Opcodes.ISHR;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.IUSHR;
import static org.objectweb.asm.Opcodes.IXOR;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.LADD;
import static org.objectweb.asm.Opcodes.LAND;
import static org.objectweb.asm.Opcodes.LCMP;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LMUL;
import static org.objectweb.asm.Opcodes.LNEG;
import static org.objectweb.asm.Opcodes.LOR;
import static org.objectweb.asm.Opcodes.LSHL;
import static org.objectweb.asm.Opcodes.LSHR;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.LSUB;
import static org.objectweb.asm.Opcodes.LUSHR;
import static org.objectweb.asm.Opcodes.LXOR;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.generateCast;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.generateCheckCast;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.generateCheckCastToByte;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.generatePlatformCheckCast;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.getTargetClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_MAP_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BYTE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_INITIAL_VALUE_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_MAPPING_INITIAL_VALUE_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_XML_QNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JSON_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_TO_UNSIGNED_INT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LIST_INITIAL_EXPRESSION_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LIST_INITIAL_VALUE_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LONG_STREAM;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAPPING_INITIAL_KEY_VALUE_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAPPING_INITIAL_SPREAD_FIELD_ENTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MATH_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SHORT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_TYPE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TUPLE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TUPLE_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_CHECKER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_FACTORY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_QNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.duplicateServiceTypeWithAnnots;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeDesc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.loadType;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeDescClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeValueClassName;

/**
 * Instruction generator helper class to hold its enclosing pkg and index map.
 *
 * @since 1.2.0
 */
public class JvmInstructionGen {

    //this anytype is currently set from package gen class
    static BType anyType;
    private final MethodVisitor mv;
    private final BIRVarToJVMIndexMap indexMap;
    private final String currentPackageName;
    private final BIRNode.BIRPackage currentPackage;
    private final JvmPackageGen jvmPackageGen;
    private final SymbolTable symbolTable;

    public JvmInstructionGen(MethodVisitor mv, BIRVarToJVMIndexMap indexMap, BIRNode.BIRPackage currentPackage,
                             JvmPackageGen jvmPackageGen) {

        this.mv = mv;
        this.indexMap = indexMap;
        this.currentPackage = currentPackage;
        this.jvmPackageGen = jvmPackageGen;
        this.symbolTable = jvmPackageGen.symbolTable;
        this.currentPackageName = JvmCodeGenUtil.getPackageName(currentPackage);
    }

    static void addJUnboxInsn(MethodVisitor mv, JType jType) {

        if (jType == null) {
            return;
        }

        switch (jType.jTag) {
            case JTypeTags.JBYTE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJByte", String.format("(L%s;)B", OBJECT), false);
                break;
            case JTypeTags.JCHAR:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJChar", String.format("(L%s;)C", OBJECT), false);
                break;
            case JTypeTags.JSHORT:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJShort", String.format("(L%s;)S", OBJECT), false);
                break;
            case JTypeTags.JINT:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJInt", String.format("(L%s;)I", OBJECT), false);
                break;
            case JTypeTags.JLONG:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJLong", String.format("(L%s;)J", OBJECT), false);
                break;
            case JTypeTags.JFLOAT:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJFloat", String.format("(L%s;)F", OBJECT), false);
                break;
            case JTypeTags.JDOUBLE:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJDouble", String.format("(L%s;)D", OBJECT), false);
                break;
            case JTypeTags.JBOOLEAN:
                mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJBoolean", String.format("(L%s;)Z", OBJECT),
                        false);
                break;
            case JTypeTags.JREF:
                mv.visitTypeInsn(CHECKCAST, ((JType.JRefType) jType).typeValue);
                break;
        }
    }

    private static void generateJVarLoad(MethodVisitor mv, JType jType, String currentPackageName, int valueIndex) {

        switch (jType.jTag) {
            case JTypeTags.JBYTE:
                mv.visitVarInsn(ILOAD, valueIndex);
                break;
            case JTypeTags.JCHAR:
                mv.visitVarInsn(ILOAD, valueIndex);
                break;
            case JTypeTags.JSHORT:
                mv.visitVarInsn(ILOAD, valueIndex);
                break;
            case JTypeTags.JINT:
                mv.visitVarInsn(ILOAD, valueIndex);
                break;
            case JTypeTags.JLONG:
                mv.visitVarInsn(LLOAD, valueIndex);
                break;
            case JTypeTags.JFLOAT:
                mv.visitVarInsn(FLOAD, valueIndex);
                break;
            case JTypeTags.JDOUBLE:
                mv.visitVarInsn(DLOAD, valueIndex);
                break;
            case JTypeTags.JBOOLEAN:
                mv.visitVarInsn(ILOAD, valueIndex);
                break;
            case JTypeTags.JARRAY:
            case JTypeTags.JREF:
                mv.visitVarInsn(ALOAD, valueIndex);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                        String.format("%s", jType));
        }
    }

    private static void generateJVarStore(MethodVisitor mv, JType jType, String currentPackageName, int valueIndex) {

        switch (jType.jTag) {
            case JTypeTags.JBYTE:
                mv.visitVarInsn(ISTORE, valueIndex);
                break;
            case JTypeTags.JCHAR:
                mv.visitVarInsn(ISTORE, valueIndex);
                break;
            case JTypeTags.JSHORT:
                mv.visitVarInsn(ISTORE, valueIndex);
                break;
            case JTypeTags.JINT:
                mv.visitVarInsn(ISTORE, valueIndex);
                break;
            case JTypeTags.JLONG:
                mv.visitVarInsn(LSTORE, valueIndex);
                break;
            case JTypeTags.JFLOAT:
                mv.visitVarInsn(FSTORE, valueIndex);
                break;
            case JTypeTags.JDOUBLE:
                mv.visitVarInsn(DSTORE, valueIndex);
                break;
            case JTypeTags.JBOOLEAN:
                mv.visitVarInsn(ISTORE, valueIndex);
                break;
            case JTypeTags.JARRAY:
            case JTypeTags.JREF:
                mv.visitVarInsn(ASTORE, valueIndex);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                        String.format("%s", jType));
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

    static void loadConstantValue(BType bType, Object constVal, MethodVisitor mv) {

        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            long intValue = constVal instanceof Long ? (long) constVal : Long.parseLong(String.valueOf(constVal));
            mv.visitLdcInsn(intValue);
            return;
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
            String val = String.valueOf(constVal);
            int[] highSurrogates = listHighSurrogates(val);
            if (highSurrogates.length > 0) {
                createNonBmpString(mv, val, highSurrogates);
            } else {
                createBmpString(mv, val);
            }
            return;
        }

        switch (bType.tag) {
            case TypeTags.BYTE:
                int byteValue = ((Number) constVal).intValue();
                mv.visitLdcInsn(byteValue);
                break;
            case TypeTags.FLOAT:
                double doubleValue = constVal instanceof Double ? (double) constVal :
                        Double.parseDouble(String.valueOf(constVal));
                mv.visitLdcInsn(doubleValue);
                break;
            case TypeTags.BOOLEAN:
                boolean booleanVal = constVal instanceof Boolean ? (boolean) constVal :
                        Boolean.parseBoolean(String.valueOf(constVal));
                mv.visitLdcInsn(booleanVal);
                break;
            case TypeTags.DECIMAL:
                mv.visitTypeInsn(NEW, DECIMAL_VALUE);
                mv.visitInsn(DUP);
                mv.visitLdcInsn(String.valueOf(constVal));
                mv.visitMethodInsn(INVOKESPECIAL, DECIMAL_VALUE, JVM_INIT_METHOD, String.format("(L%s;)V",
                        STRING_VALUE), false);
                break;
            case TypeTags.NIL:
            case TypeTags.NEVER:
                mv.visitInsn(ACONST_NULL);
                break;
            default:
                throw new BLangCompilerException("JVM generation is not supported for type : " +
                        String.format("%s", bType));
        }
    }

    private static void createBmpString(MethodVisitor mv, String val) {
        mv.visitTypeInsn(NEW, JvmConstants.BMP_STRING_VALUE);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(val);
        mv.visitMethodInsn(INVOKESPECIAL, JvmConstants.BMP_STRING_VALUE, JVM_INIT_METHOD,
                           String.format("(L%s;)V", STRING_VALUE), false);
    }

    private static void createNonBmpString(MethodVisitor mv, String val, int[] highSurrogates) {
        mv.visitTypeInsn(NEW, JvmConstants.NON_BMP_STRING_VALUE);
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
        mv.visitMethodInsn(INVOKESPECIAL, JvmConstants.NON_BMP_STRING_VALUE, JVM_INIT_METHOD,
                           String.format("(L%s;[I)V", STRING_VALUE), false);
    }

    private static void generateIntToUnsignedIntConversion(MethodVisitor mv, BType targetType) {

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

    public void generateVarLoad(MethodVisitor mv, BIRNode.BIRVariableDcl varDcl, int valueIndex) {

        BType bType = varDcl.type;

        switch (varDcl.kind) {
            case GLOBAL: {
                BIRNode.BIRGlobalVariableDcl globalVar = (BIRNode.BIRGlobalVariableDcl) varDcl;
                PackageID modId = globalVar.pkgId;
                String moduleName = JvmCodeGenUtil.getPackageName(modId);

                String varName = varDcl.name.value;
                String className = jvmPackageGen.lookupGlobalVarClassName(moduleName, varName);

                String typeSig = getTypeDesc(bType);
                mv.visitFieldInsn(GETSTATIC, className, varName, typeSig);
                return;
            }
            case SELF:
                mv.visitVarInsn(ALOAD, 0);
                return;
            case CONSTANT: {
                String varName = varDcl.name.value;
                PackageID moduleId = ((BIRNode.BIRGlobalVariableDcl) varDcl).pkgId;
                String pkgName = JvmCodeGenUtil.getPackageName(moduleId);
                String className = jvmPackageGen.lookupGlobalVarClassName(pkgName, varName);
                String typeSig = getTypeDesc(bType);
                mv.visitFieldInsn(GETSTATIC, className, varName, typeSig);
                return;
            }
        }

        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            mv.visitVarInsn(LLOAD, valueIndex);
            return;
        }  else if (TypeTags.isXMLTypeTag(bType.tag) ||
                TypeTags.isStringTypeTag(bType.tag)) {
            mv.visitVarInsn(ALOAD, valueIndex);
            return;
        }

        switch (bType.tag) {
            case TypeTags.BYTE:
                mv.visitVarInsn(ILOAD, valueIndex);
                mv.visitInsn(I2B);
                mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, JVM_TO_UNSIGNED_INT_METHOD, "(B)I", false);
                break;
            case TypeTags.FLOAT:
                mv.visitVarInsn(DLOAD, valueIndex);
                break;
            case TypeTags.BOOLEAN:
                mv.visitVarInsn(ILOAD, valueIndex);
                break;
            case TypeTags.ARRAY:
            case TypeTags.MAP:
            case TypeTags.STREAM:
            case TypeTags.TABLE:
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.NIL:
            case TypeTags.NEVER:
            case TypeTags.UNION:
            case TypeTags.INTERSECTION:
            case TypeTags.TUPLE:
            case TypeTags.RECORD:
            case TypeTags.ERROR:
            case TypeTags.JSON:
            case TypeTags.FUTURE:
            case TypeTags.OBJECT:
            case TypeTags.DECIMAL:
            case TypeTags.INVOKABLE:
            case TypeTags.FINITE:
            case TypeTags.HANDLE:
            case TypeTags.TYPEDESC:
            case TypeTags.READONLY:
                mv.visitVarInsn(ALOAD, valueIndex);
                break;
            case JTypeTags.JTYPE:
                generateJVarLoad(mv, (JType) bType, currentPackageName, valueIndex);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                        String.format("%s", bType));
        }
    }

    public void generateVarStore(MethodVisitor mv, BIRNode.BIRVariableDcl varDcl, int valueIndex) {

        BType bType = varDcl.type;

        if (varDcl.kind == VarKind.GLOBAL) {
            String varName = varDcl.name.value;
            String className = jvmPackageGen.lookupGlobalVarClassName(currentPackageName, varName);
            String typeSig = getTypeDesc(bType);
            mv.visitFieldInsn(PUTSTATIC, className, varName, typeSig);
            return;
        } else if (varDcl.kind == VarKind.CONSTANT) {
            String varName = varDcl.name.value;
            PackageID moduleId = ((BIRNode.BIRGlobalVariableDcl) varDcl).pkgId;
            String pkgName = JvmCodeGenUtil.getPackageName(moduleId);
            String className = jvmPackageGen.lookupGlobalVarClassName(pkgName, varName);
            String typeSig = getTypeDesc(bType);
            mv.visitFieldInsn(PUTSTATIC, className, varName, typeSig);
            return;
        }

        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            mv.visitVarInsn(LSTORE, valueIndex);
            return;
        } else if (TypeTags.isStringTypeTag(bType.tag) ||
                TypeTags.isXMLTypeTag(bType.tag)) {
            mv.visitVarInsn(ASTORE, valueIndex);
            return;
        }

        switch (bType.tag) {
            case TypeTags.BYTE:
                mv.visitVarInsn(ISTORE, valueIndex);
                break;
            case TypeTags.FLOAT:
                mv.visitVarInsn(DSTORE, valueIndex);
                break;
            case TypeTags.BOOLEAN:
                mv.visitVarInsn(ISTORE, valueIndex);
                break;
            case TypeTags.ARRAY:
            case TypeTags.MAP:
            case TypeTags.STREAM:
            case TypeTags.TABLE:
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.NIL:
            case TypeTags.NEVER:
            case TypeTags.UNION:
            case TypeTags.INTERSECTION:
            case TypeTags.TUPLE:
            case TypeTags.DECIMAL:
            case TypeTags.RECORD:
            case TypeTags.ERROR:
            case TypeTags.JSON:
            case TypeTags.FUTURE:
            case TypeTags.OBJECT:
            case TypeTags.INVOKABLE:
            case TypeTags.FINITE:
            case TypeTags.HANDLE:
            case TypeTags.TYPEDESC:
            case TypeTags.READONLY:
                mv.visitVarInsn(ASTORE, valueIndex);
                break;
            case JTypeTags.JTYPE:
                generateJVarStore(mv, (JType) bType, currentPackageName, valueIndex);
                break;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                        String.format("%s", bType));
        }
    }

    private BType getSmallerUnsignedIntSubType(BType lhsType, BType rhsType) {

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

    void generatePlatformIns(JInstruction ins) {

        if (ins.jKind == JInsKind.JCAST) {
            JCast castIns = (JCast) ins;
            BType targetType = castIns.targetType;
            this.loadVar(castIns.rhsOp.variableDcl);
            generatePlatformCheckCast(this.mv, this.indexMap, castIns.rhsOp.variableDcl.type, targetType);
            this.storeToVar(castIns.lhsOp.variableDcl);
        }
    }

    void generateMoveIns(BIRNonTerminator.Move moveIns) {

        this.loadVar(moveIns.rhsOp.variableDcl);
        this.storeToVar(moveIns.lhsOp.variableDcl);
    }

    void generateBinaryOpIns(BIRNonTerminator.BinaryOp binaryIns) {

        InstructionKind insKind = binaryIns.kind;
        switch (insKind) {
            case ADD:
                this.generateAddIns(binaryIns);
                break;
            case SUB:
                this.generateSubIns(binaryIns);
                break;
            case MUL:
                this.generateMulIns(binaryIns);
                break;
            case DIV:
                this.generateDivIns(binaryIns);
                break;
            case MOD:
                this.generateRemIns(binaryIns);
                break;
            case EQUAL:
                this.generateEqualIns(binaryIns);
                break;
            case NOT_EQUAL:
                this.generateNotEqualIns(binaryIns);
                break;
            case GREATER_THAN:
                this.generateGreaterThanIns(binaryIns);
                break;
            case GREATER_EQUAL:
                this.generateGreaterEqualIns(binaryIns);
                break;
            case LESS_THAN:
                this.generateLessThanIns(binaryIns);
                break;
            case LESS_EQUAL:
                this.generateLessEqualIns(binaryIns);
                break;
            case REF_EQUAL:
                this.generateRefEqualIns(binaryIns);
                break;
            case REF_NOT_EQUAL:
                this.generateRefNotEqualIns(binaryIns);
                break;
            case CLOSED_RANGE:
                this.generateClosedRangeIns(binaryIns);
                break;
            case HALF_OPEN_RANGE:
                this.generateClosedRangeIns(binaryIns);
                break;
            case ANNOT_ACCESS:
                this.generateAnnotAccessIns(binaryIns);
                break;
            case BITWISE_AND:
                this.generateBitwiseAndIns(binaryIns);
                break;
            case BITWISE_OR:
                this.generateBitwiseOrIns(binaryIns);
                break;
            case BITWISE_XOR:
                this.generateBitwiseXorIns(binaryIns);
                break;
            case BITWISE_LEFT_SHIFT:
                this.generateBitwiseLeftShiftIns(binaryIns);
                break;
            case BITWISE_RIGHT_SHIFT:
                this.generateBitwiseRightShiftIns(binaryIns);
                break;
            case BITWISE_UNSIGNED_RIGHT_SHIFT:
                this.generateBitwiseUnsignedRightShiftIns(binaryIns);
                break;
            default:
                throw new BLangCompilerException("JVM generation is not supported for instruction kind : " +
                        String.format("%s", insKind));
        }
    }

    private void generateBinaryRhsAndLhsLoad(BIRNonTerminator.BinaryOp binaryIns) {

        this.loadVar(binaryIns.rhsOp1.variableDcl);
        this.loadVar(binaryIns.rhsOp2.variableDcl);
    }

    private void generateLessThanIns(BIRNonTerminator.BinaryOp binaryIns) {

        this.generateBinaryCompareIns(binaryIns, IFLT);
    }

    private void generateGreaterThanIns(BIRNonTerminator.BinaryOp binaryIns) {

        this.generateBinaryCompareIns(binaryIns, IFGT);
    }

    private void generateLessEqualIns(BIRNonTerminator.BinaryOp binaryIns) {

        this.generateBinaryCompareIns(binaryIns, IFLE);

    }

    private void generateGreaterEqualIns(BIRNonTerminator.BinaryOp binaryIns) {

        this.generateBinaryCompareIns(binaryIns, IFGE);
    }

    private void generateBinaryCompareIns(BIRNonTerminator.BinaryOp binaryIns, int opcode) {

        if (opcode != IFLT && opcode != IFGT && opcode != IFLE && opcode != IFGE) {
            throw new BLangCompilerException(String.format("Unsupported opcode '%s' for binary operator.", opcode));
        }

        this.generateBinaryRhsAndLhsLoad(binaryIns);
        Label label1 = new Label();
        Label label2 = new Label();

        BType lhsOpType = binaryIns.rhsOp1.variableDcl.type;
        BType rhsOpType = binaryIns.rhsOp2.variableDcl.type;

        if (TypeTags.isIntegerTypeTag(lhsOpType.tag) && TypeTags.isIntegerTypeTag(rhsOpType.tag)) {
            this.mv.visitInsn(LCMP);
            this.mv.visitJumpInsn(opcode, label1);
        } else if (lhsOpType.tag == TypeTags.BYTE && rhsOpType.tag == TypeTags.BYTE) {
            if (opcode == IFLT) {
                this.mv.visitJumpInsn(IF_ICMPLT, label1);
            } else if (opcode != IFGT) {
                this.mv.visitJumpInsn(IF_ICMPGT, label1);
            } else if (opcode != IFLE) {
                this.mv.visitJumpInsn(IF_ICMPLE, label1);
            } else if (opcode == IFGE) {
                this.mv.visitJumpInsn(IF_ICMPGE, label1);
            }
        } else if (lhsOpType.tag == TypeTags.FLOAT && rhsOpType.tag == TypeTags.FLOAT) {
            this.mv.visitInsn(DCMPL);
            this.mv.visitJumpInsn(opcode, label1);
        } else if (lhsOpType.tag == TypeTags.DECIMAL && rhsOpType.tag == TypeTags.DECIMAL) {
            String compareFuncName = this.getDecimalCompareFuncName(opcode);
            this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, compareFuncName,
                    String.format("(L%s;L%s;)Z", DECIMAL_VALUE, DECIMAL_VALUE), false);
            this.storeToVar(binaryIns.lhsOp.variableDcl);
            return;
        }

        this.mv.visitInsn(ICONST_0);
        this.mv.visitJumpInsn(GOTO, label2);

        this.mv.visitLabel(label1);
        this.mv.visitInsn(ICONST_1);

        this.mv.visitLabel(label2);
        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private String getDecimalCompareFuncName(int opcode) {

        switch (opcode) {
            case IFGT:
                return "checkDecimalGreaterThan";
            case IFGE:
                return "checkDecimalGreaterThanOrEqual";
            case IFLT:
                return "checkDecimalLessThan";
            case IFLE:
                return "checkDecimalLessThanOrEqual";
            default:
                throw new BLangCompilerException(String.format("Opcode: '%s' is not a comparison opcode.", opcode));
        }
    }

    private void generateEqualIns(BIRNonTerminator.BinaryOp binaryIns) {

        this.generateBinaryRhsAndLhsLoad(binaryIns);

        Label label1 = new Label();
        Label label2 = new Label();

        BType lhsOpType = binaryIns.rhsOp1.variableDcl.type;
        BType rhsOpType = binaryIns.rhsOp2.variableDcl.type;

        if (TypeTags.isIntegerTypeTag(lhsOpType.tag) && TypeTags.isIntegerTypeTag(rhsOpType.tag)) {
            this.mv.visitInsn(LCMP);
            this.mv.visitJumpInsn(IFNE, label1);
        } else if (lhsOpType.tag == TypeTags.BYTE && rhsOpType.tag == TypeTags.BYTE) {
            this.mv.visitJumpInsn(IF_ICMPNE, label1);
        } else if (lhsOpType.tag == TypeTags.FLOAT && rhsOpType.tag == TypeTags.FLOAT) {
            this.mv.visitInsn(DCMPL);
            this.mv.visitJumpInsn(IFNE, label1);
        } else if (lhsOpType.tag == TypeTags.BOOLEAN && rhsOpType.tag == TypeTags.BOOLEAN) {
            this.mv.visitJumpInsn(IF_ICMPNE, label1);
        } else if (lhsOpType.tag == TypeTags.DECIMAL && rhsOpType.tag == TypeTags.DECIMAL) {
            this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkDecimalEqual",
                    String.format("(L%s;L%s;)Z", DECIMAL_VALUE, DECIMAL_VALUE), false);
            this.storeToVar(binaryIns.lhsOp.variableDcl);
            return;
        } else {
            this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "isEqual",
                    String.format("(L%s;L%s;)Z", OBJECT, OBJECT), false);
            this.storeToVar(binaryIns.lhsOp.variableDcl);
            return;
        }

        this.mv.visitInsn(ICONST_1);
        this.mv.visitJumpInsn(GOTO, label2);

        this.mv.visitLabel(label1);
        this.mv.visitInsn(ICONST_0);

        this.mv.visitLabel(label2);
        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateNotEqualIns(BIRNonTerminator.BinaryOp binaryIns) {

        this.generateBinaryRhsAndLhsLoad(binaryIns);

        Label label1 = new Label();
        Label label2 = new Label();

        // It is assumed that both operands are of same type
        BType lhsOpType = binaryIns.rhsOp1.variableDcl.type;
        BType rhsOpType = binaryIns.rhsOp2.variableDcl.type;
        if (TypeTags.isIntegerTypeTag(lhsOpType.tag) && TypeTags.isIntegerTypeTag(rhsOpType.tag)) {
            this.mv.visitInsn(LCMP);
            this.mv.visitJumpInsn(IFEQ, label1);
        } else if (lhsOpType.tag == TypeTags.BYTE && rhsOpType.tag == TypeTags.BYTE) {
            this.mv.visitJumpInsn(IF_ICMPEQ, label1);
        } else if (lhsOpType.tag == TypeTags.FLOAT && rhsOpType.tag == TypeTags.FLOAT) {
            this.mv.visitInsn(DCMPL);
            this.mv.visitJumpInsn(IFEQ, label1);
        } else if (lhsOpType.tag == TypeTags.BOOLEAN && rhsOpType.tag == TypeTags.BOOLEAN) {
            this.mv.visitJumpInsn(IF_ICMPEQ, label1);
        } else if (lhsOpType.tag == TypeTags.DECIMAL && rhsOpType.tag == TypeTags.DECIMAL) {
            this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkDecimalEqual",
                    String.format("(L%s;L%s;)Z", DECIMAL_VALUE, DECIMAL_VALUE), false);
            this.mv.visitJumpInsn(IFNE, label1);
        } else {
            this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "isEqual",
                    String.format("(L%s;L%s;)Z", OBJECT, OBJECT), false);
            this.mv.visitJumpInsn(IFNE, label1);
        }

        this.mv.visitInsn(ICONST_1);
        this.mv.visitJumpInsn(GOTO, label2);

        this.mv.visitLabel(label1);
        this.mv.visitInsn(ICONST_0);

        this.mv.visitLabel(label2);
        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateRefEqualIns(BIRNonTerminator.BinaryOp binaryIns) {

        this.generateBinaryRhsAndLhsLoad(binaryIns);

        Label label1 = new Label();
        Label label2 = new Label();

        BType lhsOpType = binaryIns.rhsOp1.variableDcl.type;
        BType rhsOpType = binaryIns.rhsOp2.variableDcl.type;
        if (TypeTags.isIntegerTypeTag(lhsOpType.tag) && TypeTags.isIntegerTypeTag(rhsOpType.tag)) {
            this.mv.visitInsn(LCMP);
            this.mv.visitJumpInsn(IFNE, label1);
        } else if (lhsOpType.tag == TypeTags.BYTE && rhsOpType.tag == TypeTags.BYTE) {
            this.mv.visitJumpInsn(IF_ICMPNE, label1);
        } else if (lhsOpType.tag == TypeTags.FLOAT && rhsOpType.tag == TypeTags.FLOAT) {
            this.mv.visitInsn(DCMPL);
            this.mv.visitJumpInsn(IFNE, label1);
        } else if (lhsOpType.tag == TypeTags.BOOLEAN && rhsOpType.tag == TypeTags.BOOLEAN) {
            this.mv.visitJumpInsn(IF_ICMPNE, label1);
        } else {
            this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "isReferenceEqual",
                    String.format("(L%s;L%s;)Z", OBJECT, OBJECT), false);
            this.storeToVar(binaryIns.lhsOp.variableDcl);
            return;
        }

        this.mv.visitInsn(ICONST_1);
        this.mv.visitJumpInsn(GOTO, label2);

        this.mv.visitLabel(label1);
        this.mv.visitInsn(ICONST_0);

        this.mv.visitLabel(label2);
        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateRefNotEqualIns(BIRNonTerminator.BinaryOp binaryIns) {

        this.generateBinaryRhsAndLhsLoad(binaryIns);

        Label label1 = new Label();
        Label label2 = new Label();

        // It is assumed that both operands are of same type
        BType lhsOpType = binaryIns.rhsOp1.variableDcl.type;
        BType rhsOpType = binaryIns.rhsOp2.variableDcl.type;
        if (TypeTags.isIntegerTypeTag(lhsOpType.tag) && TypeTags.isIntegerTypeTag(rhsOpType.tag)) {
            this.mv.visitInsn(LCMP);
            this.mv.visitJumpInsn(IFEQ, label1);
        } else if (lhsOpType.tag == TypeTags.BYTE && rhsOpType.tag == TypeTags.BYTE) {
            this.mv.visitJumpInsn(IF_ICMPEQ, label1);
        } else if (lhsOpType.tag == TypeTags.FLOAT && rhsOpType.tag == TypeTags.FLOAT) {
            this.mv.visitInsn(DCMPL);
            this.mv.visitJumpInsn(IFEQ, label1);
        } else if (lhsOpType.tag == TypeTags.BOOLEAN && rhsOpType.tag == TypeTags.BOOLEAN) {
            this.mv.visitJumpInsn(IF_ICMPEQ, label1);
        } else {
            this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "isReferenceEqual",
                    String.format("(L%s;L%s;)Z", OBJECT, OBJECT), false);
            this.mv.visitJumpInsn(IFNE, label1);
        }

        this.mv.visitInsn(ICONST_1);
        this.mv.visitJumpInsn(GOTO, label2);

        this.mv.visitLabel(label1);
        this.mv.visitInsn(ICONST_0);

        this.mv.visitLabel(label2);
        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateClosedRangeIns(BIRNonTerminator.BinaryOp binaryIns) {

        this.mv.visitTypeInsn(NEW, ARRAY_VALUE_IMPL);
        this.mv.visitInsn(DUP);
        this.generateBinaryRhsAndLhsLoad(binaryIns);
        this.mv.visitMethodInsn(INVOKESTATIC, LONG_STREAM, "rangeClosed", String.format("(JJ)L%s;", LONG_STREAM),
                true);
        this.mv.visitMethodInsn(INVOKEINTERFACE, LONG_STREAM, "toArray", "()[J", true);
        this.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_VALUE_IMPL, JVM_INIT_METHOD, "([J)V", false);
        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateAnnotAccessIns(BIRNonTerminator.BinaryOp binaryIns) {

        this.loadVar(binaryIns.rhsOp1.variableDcl);
        this.loadVar(binaryIns.rhsOp2.variableDcl);
        this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "getAnnotValue", String.format(
                "(L%s;L%s;)L%s;", TYPEDESC_VALUE, JvmConstants.B_STRING_VALUE, OBJECT), false);

        BType targetType = binaryIns.lhsOp.variableDcl.type;
        JvmCastGen.addUnboxInsn(this.mv, targetType);
        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateAddIns(BIRNonTerminator.BinaryOp binaryIns) {

        BType bType = binaryIns.lhsOp.variableDcl.type;
        this.generateBinaryRhsAndLhsLoad(binaryIns);
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            this.mv.visitInsn(LADD);
        } else if (bType.tag == TypeTags.BYTE) {
            this.mv.visitInsn(IADD);
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, JvmConstants.B_STRING_VALUE, "concat",
                                        String.format("(L%s;)L%s;", JvmConstants.B_STRING_VALUE,
                                                      JvmConstants.B_STRING_VALUE), true);
        } else if (bType.tag == TypeTags.DECIMAL) {
            this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "add",
                    String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE), false);
        } else if (bType.tag == TypeTags.FLOAT) {
            this.mv.visitInsn(DADD);
        } else if (TypeTags.isXMLTypeTag(bType.tag)) {
            this.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "concatenate",
                    String.format("(L%s;L%s;)L%s;", XML_VALUE, XML_VALUE, XML_VALUE), false);
        } else {
            throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                    String.format("%s", binaryIns.lhsOp.variableDcl.type));
        }

        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateSubIns(BIRNonTerminator.BinaryOp binaryIns) {

        BType bType = binaryIns.lhsOp.variableDcl.type;
        this.generateBinaryRhsAndLhsLoad(binaryIns);
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            this.mv.visitInsn(LSUB);
        } else if (bType.tag == TypeTags.FLOAT) {
            this.mv.visitInsn(DSUB);
        } else if (bType.tag == TypeTags.DECIMAL) {
            this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "subtract",
                    String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE), false);
        } else {
            throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                    String.format("%s", binaryIns.lhsOp.variableDcl.type));
        }
        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateDivIns(BIRNonTerminator.BinaryOp binaryIns) {

        BType bType = binaryIns.lhsOp.variableDcl.type;
        this.generateBinaryRhsAndLhsLoad(binaryIns);
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            this.mv.visitMethodInsn(INVOKESTATIC, MATH_UTILS, "divide", "(JJ)J", false);
        } else if (bType.tag == TypeTags.FLOAT) {
            this.mv.visitInsn(DDIV);
        } else if (bType.tag == TypeTags.DECIMAL) {
            this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "divide",
                    String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE), false);
        } else {
            throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                    String.format("%s", binaryIns.lhsOp.variableDcl.type));
        }
        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateMulIns(BIRNonTerminator.BinaryOp binaryIns) {

        BType bType = binaryIns.lhsOp.variableDcl.type;
        this.generateBinaryRhsAndLhsLoad(binaryIns);
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            this.mv.visitInsn(LMUL);
        } else if (bType.tag == TypeTags.FLOAT) {
            this.mv.visitInsn(DMUL);
        } else if (bType.tag == TypeTags.DECIMAL) {
            this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "multiply",
                    String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE), false);
        } else {
            throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                    String.format("%s", binaryIns.lhsOp.variableDcl.type));
        }
        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateRemIns(BIRNonTerminator.BinaryOp binaryIns) {

        BType bType = binaryIns.lhsOp.variableDcl.type;
        this.generateBinaryRhsAndLhsLoad(binaryIns);
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            this.mv.visitMethodInsn(INVOKESTATIC, MATH_UTILS, "remainder", "(JJ)J", false);
        } else if (bType.tag == TypeTags.FLOAT) {
            this.mv.visitInsn(DREM);
        } else if (bType.tag == TypeTags.DECIMAL) {
            this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "remainder",
                    String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE), false);
        } else {
            throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                    String.format("%s", binaryIns.lhsOp.variableDcl.type));
        }
        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateBitwiseAndIns(BIRNonTerminator.BinaryOp binaryIns) {

        BType opType1 = binaryIns.rhsOp1.variableDcl.type;
        BType opType2 = binaryIns.rhsOp2.variableDcl.type;

        int opType1Tag = opType1.tag;
        int opType2Tag = opType2.tag;

        if (opType1Tag == TypeTags.BYTE && opType2Tag == TypeTags.BYTE) {
            this.loadVar(binaryIns.rhsOp1.variableDcl);
            generateCheckCastToByte(this.mv, opType1);

            this.loadVar(binaryIns.rhsOp2.variableDcl);
            generateCheckCastToByte(this.mv, opType2);

            this.mv.visitInsn(IAND);
        } else {
            boolean byteResult = false;

            this.loadVar(binaryIns.rhsOp1.variableDcl);
            if (opType1Tag == TypeTags.BYTE) {
                this.mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, "toUnsignedLong", "(I)J", false);
                byteResult = true;
            }

            this.loadVar(binaryIns.rhsOp2.variableDcl);
            if (opType2Tag == TypeTags.BYTE) {
                this.mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, "toUnsignedLong", "(I)J", false);
                byteResult = true;
            }

            this.mv.visitInsn(LAND);
            if (byteResult) {
                generateCheckCastToByte(this.mv, symbolTable.intType);
            }
        }

        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateBitwiseOrIns(BIRNonTerminator.BinaryOp binaryIns) {

        BType opType1 = binaryIns.rhsOp1.variableDcl.type;
        BType opType2 = binaryIns.rhsOp2.variableDcl.type;

        if (opType1.tag == TypeTags.BYTE && opType2.tag == TypeTags.BYTE) {
            this.loadVar(binaryIns.rhsOp1.variableDcl);
            this.loadVar(binaryIns.rhsOp2.variableDcl);
            this.mv.visitInsn(IOR);
            this.storeToVar(binaryIns.lhsOp.variableDcl);
            return;
        }

        this.loadVar(binaryIns.rhsOp1.variableDcl);
        generateCheckCast(this.mv, opType1, symbolTable.intType, this.indexMap);

        this.loadVar(binaryIns.rhsOp2.variableDcl);
        generateCheckCast(this.mv, opType2, symbolTable.intType, this.indexMap);

        this.mv.visitInsn(LOR);

        if (!TypeTags.isSignedIntegerTypeTag(opType1.tag) && !TypeTags.isSignedIntegerTypeTag(opType2.tag)) {
            generateIntToUnsignedIntConversion(this.mv,
                    getSmallerUnsignedIntSubType(opType1, opType2));
        }

        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateBitwiseXorIns(BIRNonTerminator.BinaryOp binaryIns) {

        BType opType1 = binaryIns.rhsOp1.variableDcl.type;
        BType opType2 = binaryIns.rhsOp2.variableDcl.type;

        if (opType1.tag == TypeTags.BYTE && opType2.tag == TypeTags.BYTE) {
            this.loadVar(binaryIns.rhsOp1.variableDcl);
            this.loadVar(binaryIns.rhsOp2.variableDcl);
            this.mv.visitInsn(IXOR);
            this.storeToVar(binaryIns.lhsOp.variableDcl);
            return;
        }

        this.loadVar(binaryIns.rhsOp1.variableDcl);
        generateCheckCast(this.mv, opType1, symbolTable.intType, this.indexMap);

        this.loadVar(binaryIns.rhsOp2.variableDcl);
        generateCheckCast(this.mv, opType2, symbolTable.intType, this.indexMap);

        this.mv.visitInsn(LXOR);

        if (!TypeTags.isSignedIntegerTypeTag(opType1.tag) && !TypeTags.isSignedIntegerTypeTag(opType2.tag)) {
            generateIntToUnsignedIntConversion(this.mv, getSmallerUnsignedIntSubType(opType1, opType2));
        }

        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateBitwiseLeftShiftIns(BIRNonTerminator.BinaryOp binaryIns) {

        this.loadVar(binaryIns.rhsOp1.variableDcl);
        this.loadVar(binaryIns.rhsOp2.variableDcl);

        BType secondOpType = binaryIns.rhsOp2.variableDcl.type;
        if (TypeTags.isIntegerTypeTag(secondOpType.tag)) {
            this.mv.visitInsn(L2I);
        }

        BType firstOpType = binaryIns.rhsOp1.variableDcl.type;
        if (TypeTags.isIntegerTypeTag(firstOpType.tag)) {
            this.mv.visitInsn(LSHL);
        } else {
            this.mv.visitInsn(ISHL);
            this.mv.visitInsn(I2L);
        }

        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateBitwiseRightShiftIns(BIRNonTerminator.BinaryOp binaryIns) {

        this.loadVar(binaryIns.rhsOp1.variableDcl);
        this.loadVar(binaryIns.rhsOp2.variableDcl);

        if (TypeTags.isIntegerTypeTag(binaryIns.rhsOp2.variableDcl.type.tag)) {
            this.mv.visitInsn(L2I);
        }

        if (TypeTags.isIntegerTypeTag(binaryIns.rhsOp1.variableDcl.type.tag)) {
            this.mv.visitInsn(LSHR);
        } else {
            this.mv.visitInsn(ISHR);
        }

        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private void generateBitwiseUnsignedRightShiftIns(BIRNonTerminator.BinaryOp binaryIns) {

        this.loadVar(binaryIns.rhsOp1.variableDcl);
        this.loadVar(binaryIns.rhsOp2.variableDcl);

        if (TypeTags.isIntegerTypeTag(binaryIns.rhsOp2.variableDcl.type.tag)) {
            this.mv.visitInsn(L2I);
        }

        if (TypeTags.isIntegerTypeTag(binaryIns.rhsOp1.variableDcl.type.tag)) {
            this.mv.visitInsn(LUSHR);
        } else {
            this.mv.visitInsn(IUSHR);
        }

        this.storeToVar(binaryIns.lhsOp.variableDcl);
    }

    private int getJVMIndexOfVarRef(BIRNode.BIRVariableDcl varDcl) {

        return this.indexMap.addToMapIfNotFoundAndGetIndex(varDcl);
    }

    void generateMapNewIns(BIRNonTerminator.NewStructure mapNewIns, int localVarOffset) {

        this.loadVar(mapNewIns.rhsOp.variableDcl);
        this.mv.visitVarInsn(ALOAD, localVarOffset);

        List<BIRNode.BIRMappingConstructorEntry> initialValues = mapNewIns.initialValues;
        mv.visitLdcInsn((long) initialValues.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, B_MAPPING_INITIAL_VALUE_ENTRY);

        int i = 0;
        for (BIRNode.BIRMappingConstructorEntry initialValue : initialValues) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) i);
            mv.visitInsn(L2I);
            i += 1;

            if (initialValue.isKeyValuePair()) {
                createKeyValueEntry(mv, (BIRNode.BIRMappingConstructorKeyValueEntry) initialValue);
            } else {
                createSpreadFieldEntry(mv, (BIRNode.BIRMappingConstructorSpreadFieldEntry) initialValue);
            }

            mv.visitInsn(AASTORE);
        }

        this.mv.visitMethodInsn(INVOKEINTERFACE, TYPEDESC_VALUE, "instantiate",
                                String.format("(L%s;[L%s;)L%s;", STRAND_CLASS, B_INITIAL_VALUE_ENTRY, OBJECT), true);
        this.storeToVar(mapNewIns.lhsOp.variableDcl);
    }

    private void createKeyValueEntry(MethodVisitor mv, BIRNode.BIRMappingConstructorKeyValueEntry keyValueEntry) {

        mv.visitTypeInsn(NEW, MAPPING_INITIAL_KEY_VALUE_ENTRY);
        mv.visitInsn(DUP);

        BIRNode.BIRVariableDcl keyOpVarDecl = keyValueEntry.keyOp.variableDcl;
        this.loadVar(keyOpVarDecl);
        JvmCastGen.addBoxInsn(this.mv, keyOpVarDecl.type);

        BIRNode.BIRVariableDcl valueOpVarDecl = keyValueEntry.valueOp.variableDcl;
        this.loadVar(valueOpVarDecl);
        JvmCastGen.addBoxInsn(this.mv, valueOpVarDecl.type);

        mv.visitMethodInsn(INVOKESPECIAL, MAPPING_INITIAL_KEY_VALUE_ENTRY, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;)V", OBJECT, OBJECT), false);
    }

    private void createSpreadFieldEntry(MethodVisitor mv,
                                        BIRNode.BIRMappingConstructorSpreadFieldEntry spreadFieldEntry) {

        mv.visitTypeInsn(NEW, MAPPING_INITIAL_SPREAD_FIELD_ENTRY);
        mv.visitInsn(DUP);

        BIRNode.BIRVariableDcl variableDcl = spreadFieldEntry.exprOp.variableDcl;
        this.loadVar(variableDcl);

        mv.visitMethodInsn(INVOKESPECIAL, MAPPING_INITIAL_SPREAD_FIELD_ENTRY, JVM_INIT_METHOD,
                           String.format("(L%s;)V", B_MAP), false);
    }

    void generateMapStoreIns(BIRNonTerminator.FieldAccess mapStoreIns) {
        // visit map_ref
        this.loadVar(mapStoreIns.lhsOp.variableDcl);
        BType varRefType = mapStoreIns.lhsOp.variableDcl.type;

        // visit key_expr
        this.loadVar(mapStoreIns.keyOp.variableDcl);

        // visit value_expr
        BType valueType = mapStoreIns.rhsOp.variableDcl.type;
        this.loadVar(mapStoreIns.rhsOp.variableDcl);
        JvmCastGen.addBoxInsn(this.mv, valueType);

        if (varRefType.tag == TypeTags.JSON) {
            this.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "setElement",
                                    String.format("(L%s;L%s;L%s;)V", OBJECT, JvmConstants.B_STRING_VALUE), false);
        } else if (mapStoreIns.onInitialization) {
            // We only reach here for stores in a record init function.
            this.mv.visitMethodInsn(INVOKEINTERFACE, MAP_VALUE, "populateInitialValue",
                                    String.format("(L%s;L%s;)V", OBJECT, OBJECT), true);
        } else {
            String signature = String.format("(L%s;L%s;L%s;)V",
                                             MAP_VALUE, JvmConstants.B_STRING_VALUE, OBJECT);
            this.mv.visitMethodInsn(INVOKESTATIC, MAP_UTILS, "handleMapStore", signature, false);
        }
    }

    void generateMapLoadIns(BIRNonTerminator.FieldAccess mapLoadIns) {
        // visit map_ref
        this.loadVar(mapLoadIns.rhsOp.variableDcl);
        BType varRefType = mapLoadIns.rhsOp.variableDcl.type;
        JvmCastGen.addUnboxInsn(this.mv, varRefType);

        // visit key_expr
        this.loadVar(mapLoadIns.keyOp.variableDcl);

        if (varRefType.tag == TypeTags.JSON) {

            if (mapLoadIns.optionalFieldAccess) {
                this.mv.visitTypeInsn(CHECKCAST, JvmConstants.B_STRING_VALUE);
                this.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "getElementOrNil",
                                        String.format("(L%s;L%s;)L%s;", OBJECT, JvmConstants.B_STRING_VALUE, OBJECT),
                                        false);
            } else {
                this.mv.visitTypeInsn(CHECKCAST, JvmConstants.B_STRING_VALUE);
                this.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "getElement", String.format(
                        "(L%s;L%s;)L%s;", OBJECT, JvmConstants.B_STRING_VALUE, OBJECT), false);
            }
        } else {
            if (mapLoadIns.fillingRead) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, MAP_VALUE, "fillAndGet",
                        String.format("(L%s;)L%s;", OBJECT, OBJECT), true);
            } else {
                this.mv.visitMethodInsn(INVOKEINTERFACE, MAP_VALUE, "get",
                        String.format("(L%s;)L%s;", OBJECT, OBJECT), true);
            }
        }

        // store in the target reg
        BType targetType = mapLoadIns.lhsOp.variableDcl.type;
        JvmCastGen.addUnboxInsn(this.mv, targetType);
        this.storeToVar(mapLoadIns.lhsOp.variableDcl);
    }

    void generateObjectLoadIns(BIRNonTerminator.FieldAccess objectLoadIns) {
        // visit object_ref
        this.loadVar(objectLoadIns.rhsOp.variableDcl);

        // visit key_expr
        this.loadVar(objectLoadIns.keyOp.variableDcl);

        // invoke get() method, and unbox if needed
        this.mv.visitMethodInsn(INVOKEINTERFACE, B_OBJECT, "get",
                                String.format("(L%s;)L%s;", JvmConstants.B_STRING_VALUE, OBJECT), true);
        BType targetType = objectLoadIns.lhsOp.variableDcl.type;
        JvmCastGen.addUnboxInsn(this.mv, targetType);

        // store in the target reg
        this.storeToVar(objectLoadIns.lhsOp.variableDcl);
    }

    void generateObjectStoreIns(BIRNonTerminator.FieldAccess objectStoreIns) {
        // visit object_ref
        this.loadVar(objectStoreIns.lhsOp.variableDcl);

        // visit key_expr
        this.loadVar(objectStoreIns.keyOp.variableDcl);

        // visit value_expr
        BType valueType = objectStoreIns.rhsOp.variableDcl.type;
        this.loadVar(objectStoreIns.rhsOp.variableDcl);
        JvmCastGen.addBoxInsn(this.mv, valueType);

        // invoke set() method
        if (objectStoreIns.onInitialization) {
            BObjectType objectType = (BObjectType) objectStoreIns.lhsOp.variableDcl.type;
            this.mv.visitMethodInsn(INVOKESPECIAL,
                                    getTypeValueClassName(objectType.tsymbol.pkgID, toNameString(objectType)),
                                    "setOnInitialization",
                                    String.format("(L%s;L%s;)V", JvmConstants.B_STRING_VALUE, OBJECT), false);
            return;
        }

        this.mv.visitMethodInsn(INVOKEINTERFACE, B_OBJECT, "set",
                                String.format("(L%s;L%s;)V", JvmConstants.B_STRING_VALUE, OBJECT), true);
    }

    void generateStringLoadIns(BIRNonTerminator.FieldAccess stringLoadIns) {
        // visit the string
        this.loadVar(stringLoadIns.rhsOp.variableDcl);

        // visit the key expr
        this.loadVar(stringLoadIns.keyOp.variableDcl);

        // invoke the `getStringAt()` method
        this.mv.visitMethodInsn(INVOKESTATIC, STRING_UTILS, "getStringAt",
                                String.format("(L%s;J)L%s;", JvmConstants.B_STRING_VALUE, JvmConstants.B_STRING_VALUE),
                                false);

        // store in the target reg
        this.storeToVar(stringLoadIns.lhsOp.variableDcl);
    }

    void generateArrayNewIns(BIRNonTerminator.NewArray inst) {

        if (inst.type.tag == TypeTags.ARRAY) {
            this.mv.visitTypeInsn(NEW, ARRAY_VALUE_IMPL);
            this.mv.visitInsn(DUP);
            loadType(this.mv, inst.type);
            this.loadVar(inst.sizeOp.variableDcl);
            loadListInitialValues(inst);
            this.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_VALUE_IMPL, JVM_INIT_METHOD,
                    String.format("(L%s;J[L%s;)V", ARRAY_TYPE, LIST_INITIAL_VALUE_ENTRY), false);
            this.storeToVar(inst.lhsOp.variableDcl);
        } else {
            this.mv.visitTypeInsn(NEW, TUPLE_VALUE_IMPL);
            this.mv.visitInsn(DUP);
            loadType(this.mv, inst.type);
            this.loadVar(inst.sizeOp.variableDcl);
            loadListInitialValues(inst);
            this.mv.visitMethodInsn(INVOKESPECIAL, TUPLE_VALUE_IMPL, JVM_INIT_METHOD,
                    String.format("(L%s;J[L%s;)V", TUPLE_TYPE, LIST_INITIAL_VALUE_ENTRY), false);
            this.storeToVar(inst.lhsOp.variableDcl);
        }
    }

    void generateArrayStoreIns(BIRNonTerminator.FieldAccess inst) {

        this.loadVar(inst.lhsOp.variableDcl);
        this.loadVar(inst.keyOp.variableDcl);
        this.loadVar(inst.rhsOp.variableDcl);

        BType valueType = inst.rhsOp.variableDcl.type;

        String method = "add";

        if (TypeTags.isIntegerTypeTag(valueType.tag)) {
            this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, method, "(JJ)V", true);
        } else if (valueType.tag == TypeTags.FLOAT) {
            this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, method, "(JD)V", true);
        } else if (TypeTags.isStringTypeTag(valueType.tag)) {
            this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, method, String.format(
                    "(JL%s;)V", JvmConstants.B_STRING_VALUE), true);
        } else if (valueType.tag == TypeTags.BOOLEAN) {
            this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, method, "(JZ)V", true);
        } else if (valueType.tag == TypeTags.BYTE) {
            this.mv.visitInsn(I2B);
            this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, method, "(JB)V", true);
        } else {
            this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, method, String.format("(JL%s;)V", OBJECT), true);
        }
    }

    void generateArrayValueLoad(BIRNonTerminator.FieldAccess inst) {

        this.loadVar(inst.rhsOp.variableDcl);
        this.mv.visitTypeInsn(CHECKCAST, ARRAY_VALUE);
        this.loadVar(inst.keyOp.variableDcl);
        BType bType = inst.lhsOp.variableDcl.type;

        BType varRefType = inst.rhsOp.variableDcl.type;
        if (varRefType.tag == TypeTags.TUPLE) {
            if (inst.fillingRead) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "fillAndGetRefValue",
                        String.format("(J)L%s;", OBJECT), true);
            } else {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getRefValue",
                        String.format("(J)L%s;", OBJECT), true);
            }
            JvmCastGen.addUnboxInsn(this.mv, bType);
        } else if (TypeTags.isIntegerTypeTag(bType.tag)) {
            this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getInt", "(J)J", true);
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getBString",
                                        String.format("(J)L%s;", JvmConstants.B_STRING_VALUE), true);
        } else if (bType.tag == TypeTags.BOOLEAN) {
            this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getBoolean", "(J)Z", true);
        } else if (bType.tag == TypeTags.BYTE) {
            this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getByte", "(J)B", true);
            this.mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, JVM_TO_UNSIGNED_INT_METHOD, "(B)I", false);
        } else if (bType.tag == TypeTags.FLOAT) {
            this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getFloat", "(J)D", true);
        } else {
            if (inst.fillingRead) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "fillAndGetRefValue",
                        String.format("(J)L%s;", OBJECT), true);
            } else {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getRefValue",
                        String.format("(J)L%s;", OBJECT), true);
            }
            String targetTypeClass = getTargetClass(bType);
            if (targetTypeClass != null) {
                this.mv.visitTypeInsn(CHECKCAST, targetTypeClass);
            } else {
                JvmCastGen.addUnboxInsn(this.mv, bType);
            }
        }
        this.storeToVar(inst.lhsOp.variableDcl);
    }

    void generateTableNewIns(NewTable inst) {

        this.mv.visitTypeInsn(NEW, TABLE_VALUE_IMPL);
        this.mv.visitInsn(DUP);
        loadType(this.mv, inst.type);
        this.loadVar(inst.dataOp.variableDcl);
        this.loadVar(inst.keyColOp.variableDcl);
        this.mv.visitMethodInsn(INVOKESPECIAL, TABLE_VALUE_IMPL, JVM_INIT_METHOD,
                                String.format("(L%s;L%s;L%s;)V", TABLE_TYPE_IMPL, ARRAY_VALUE, ARRAY_VALUE), false);

        this.storeToVar(inst.lhsOp.variableDcl);
    }

    void generateTableLoadIns(FieldAccess inst) {

        this.loadVar(inst.rhsOp.variableDcl);
        this.mv.visitTypeInsn(CHECKCAST, TABLE_VALUE);
        this.loadVar(inst.keyOp.variableDcl);
        JvmCastGen.addBoxInsn(this.mv, inst.keyOp.variableDcl.type);
        BType bType = inst.lhsOp.variableDcl.type;
        this.mv.visitMethodInsn(INVOKEINTERFACE, TABLE_VALUE, "getOrThrow",
                String.format("(L%s;)L%s;", OBJECT, OBJECT), true);

        String targetTypeClass = getTargetClass(bType);
        if (targetTypeClass != null) {
            this.mv.visitTypeInsn(CHECKCAST, targetTypeClass);
        } else {
            JvmCastGen.addUnboxInsn(this.mv, bType);
        }

        this.storeToVar(inst.lhsOp.variableDcl);
    }

    void generateTableStoreIns(FieldAccess inst) {

        this.loadVar(inst.lhsOp.variableDcl);
        this.loadVar(inst.keyOp.variableDcl);
        BType keyType = inst.keyOp.variableDcl.type;
        JvmCastGen.addBoxInsn(this.mv, keyType);
        BType valueType = inst.rhsOp.variableDcl.type;
        this.loadVar(inst.rhsOp.variableDcl);
        JvmCastGen.addBoxInsn(this.mv, valueType);

        this.mv.visitMethodInsn(INVOKESTATIC, TABLE_UTILS, "handleTableStore",
                String.format("(L%s;L%s;L%s;)V", TABLE_VALUE, OBJECT, OBJECT), false);
    }

    void generateNewErrorIns(BIRNonTerminator.NewError newErrorIns) {

        this.mv.visitTypeInsn(NEW, ERROR_VALUE);
        this.mv.visitInsn(DUP);
        // load errorType
        loadType(this.mv, newErrorIns.type);
        this.loadVar(newErrorIns.messageOp.variableDcl);
        this.loadVar(newErrorIns.causeOp.variableDcl);
        this.loadVar(newErrorIns.detailOp.variableDcl);
        this.mv.visitMethodInsn(INVOKESPECIAL, ERROR_VALUE, JVM_INIT_METHOD, String.format(
                "(L%s;L%s;L%s;L%s;)V", TYPE, JvmConstants.B_STRING_VALUE,
                BERROR,
                OBJECT),
                false);
        this.storeToVar(newErrorIns.lhsOp.variableDcl);
    }

    void generateCastIns(BIRNonTerminator.TypeCast typeCastIns) {
        // load source value
        this.loadVar(typeCastIns.rhsOp.variableDcl);
        if (typeCastIns.checkTypes) {
            generateCheckCast(this.mv, typeCastIns.rhsOp.variableDcl.type, typeCastIns.type, this.indexMap);
        } else {
            generateCast(this.mv, typeCastIns.rhsOp.variableDcl.type, typeCastIns.type);
        }
        this.storeToVar(typeCastIns.lhsOp.variableDcl);
    }

    void generateTypeTestIns(BIRNonTerminator.TypeTest typeTestIns) {
        // load source value
        this.loadVar(typeTestIns.rhsOp.variableDcl);

        // load targetType
        loadType(this.mv, typeTestIns.type);

        this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkIsType",
                                String.format("(L%s;L%s;)Z", OBJECT, TYPE), false);
        this.storeToVar(typeTestIns.lhsOp.variableDcl);
    }

    void generateIsLikeIns(BIRNonTerminator.IsLike isLike) {
        // load source value
        this.loadVar(isLike.rhsOp.variableDcl);

        // load targetType
        loadType(this.mv, isLike.type);

        this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkIsLikeType",
                                String.format("(L%s;L%s;)Z", OBJECT, TYPE), false);
        this.storeToVar(isLike.lhsOp.variableDcl);
    }

    void generateObjectNewIns(BIRNonTerminator.NewInstance objectNewIns, int strandIndex) {

        BType type = jvmPackageGen.lookupTypeDef(objectNewIns);
        String className;
        if (objectNewIns.isExternalDef) {
            className = getTypeValueClassName(objectNewIns.externalPackageId, objectNewIns.objectName);
        } else {
            className = getTypeValueClassName(this.currentPackage, objectNewIns.def.name.value);
        }

        this.mv.visitTypeInsn(NEW, className);
        this.mv.visitInsn(DUP);

        if (type instanceof BServiceType) {
            // For services, create a new type for each new service value. TODO: do only for local vars
            String pkgClassName = currentPackageName.equals(".") || currentPackageName.equals("") ?
                    MODULE_INIT_CLASS_NAME : jvmPackageGen.lookupGlobalVarClassName(currentPackageName,
                    ANNOTATION_MAP_NAME);
            duplicateServiceTypeWithAnnots(this.mv, (BObjectType) type, pkgClassName, strandIndex);
        } else {
            loadType(mv, type);
        }
        this.mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE_IMPL);
        this.mv.visitMethodInsn(INVOKESPECIAL, className, JVM_INIT_METHOD, String.format("(L%s;)V", OBJECT_TYPE_IMPL),
                false);
        this.storeToVar(objectNewIns.lhsOp.variableDcl);
    }

    void generateFPLoadIns(BIRNonTerminator.FPLoad inst, AsyncDataCollector asyncDataCollector) {

        this.mv.visitTypeInsn(NEW, FUNCTION_POINTER);
        this.mv.visitInsn(DUP);

        String lambdaName = IdentifierUtils.encodeIdentifier(inst.funcName.value) + "$lambda" +
                asyncDataCollector.getLambdaIndex() + "$";
        asyncDataCollector.incrementLambdaIndex();
        String pkgName = JvmCodeGenUtil.getPackageName(inst.pkgId);

        BType returnType = inst.lhsOp.variableDcl.type;
        if (returnType.tag != TypeTags.INVOKABLE) {
            throw new BLangCompilerException("Expected BInvokableType, found " + String.format("%s", returnType));
        }

        for (BIROperand operand : inst.closureMaps) {
            if (operand != null) {
                this.loadVar(operand.variableDcl);
            }
        }

        JvmCodeGenUtil.visitInvokeDynamic(mv, asyncDataCollector.getEnclosingClass(), lambdaName,
                                          inst.closureMaps.size());
        loadType(this.mv, returnType);
        if (inst.strandName != null) {
            mv.visitLdcInsn(inst.strandName);
        } else {
            mv.visitInsn(ACONST_NULL);
        }

        if (inst.schedulerPolicy == SchedulerPolicy.ANY) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }
        this.mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_POINTER, JVM_INIT_METHOD,
                                String.format("(L%s;L%s;L%s;Z)V", FUNCTION, TYPE, STRING_VALUE), false);

        // Set annotations if available.
        this.mv.visitInsn(DUP);
        String pkgClassName = pkgName.equals(".") || pkgName.equals("") ? MODULE_INIT_CLASS_NAME :
                jvmPackageGen.lookupGlobalVarClassName(pkgName, ANNOTATION_MAP_NAME);
        this.mv.visitFieldInsn(GETSTATIC, pkgClassName, ANNOTATION_MAP_NAME, String.format("L%s;", MAP_VALUE));
        this.mv.visitLdcInsn(inst.funcName.value);
        this.mv.visitMethodInsn(INVOKESTATIC, String.format("%s", ANNOTATION_UTILS), "processFPValueAnnotations",
                String.format("(L%s;L%s;L%s;)V", FUNCTION_POINTER, MAP_VALUE, STRING_VALUE), false);

        this.storeToVar(inst.lhsOp.variableDcl);
        asyncDataCollector.add(lambdaName, inst);
    }

    void generateNewXMLElementIns(BIRNonTerminator.NewXMLElement newXMLElement) {

        this.loadVar(newXMLElement.startTagOp.variableDcl);
        this.mv.visitTypeInsn(CHECKCAST, XML_QNAME);
        this.loadVar(newXMLElement.defaultNsURIOp.variableDcl);
        if (newXMLElement.readonly) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }

        this.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLElement",
                                String.format("(L%s;L%s;Z)L%s;", B_XML_QNAME, JvmConstants.B_STRING_VALUE, XML_VALUE),
                                false);
        this.storeToVar(newXMLElement.lhsOp.variableDcl);
    }

    void generateNewXMLQNameIns(BIRNonTerminator.NewXMLQName newXMLQName) {

        this.mv.visitTypeInsn(NEW, XML_QNAME);
        this.mv.visitInsn(DUP);
        this.loadVar(newXMLQName.localnameOp.variableDcl);
        this.loadVar(newXMLQName.nsURIOp.variableDcl);
        this.loadVar(newXMLQName.prefixOp.variableDcl);
        this.mv.visitMethodInsn(INVOKESPECIAL, XML_QNAME, JVM_INIT_METHOD, String.format(
                "(L%s;L%s;L%s;)V", JvmConstants.B_STRING_VALUE, JvmConstants.B_STRING_VALUE,
                JvmConstants.B_STRING_VALUE),
                                false);
        this.storeToVar(newXMLQName.lhsOp.variableDcl);
    }

    void generateNewStringXMLQNameIns(BIRNonTerminator.NewStringXMLQName newStringXMLQName) {

        this.mv.visitTypeInsn(NEW, XML_QNAME);
        this.mv.visitInsn(DUP);
        this.loadVar(newStringXMLQName.stringQNameOP.variableDcl);
        this.mv.visitMethodInsn(INVOKESPECIAL, XML_QNAME, JVM_INIT_METHOD,
                String.format("(L%s;)V", STRING_VALUE), false);
        this.storeToVar(newStringXMLQName.lhsOp.variableDcl);
    }

    void generateNewXMLTextIns(BIRNonTerminator.NewXMLText newXMLText) {

        this.loadVar(newXMLText.textOp.variableDcl);
        this.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLText",
                                    String.format("(L%s;)L%s;", JvmConstants.B_STRING_VALUE, XML_VALUE),
                false);
        this.storeToVar(newXMLText.lhsOp.variableDcl);
    }

    void generateNewXMLCommentIns(BIRNonTerminator.NewXMLComment newXMLComment) {

        this.loadVar(newXMLComment.textOp.variableDcl);

        if (newXMLComment.readonly) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }

        this.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLComment",
                                String.format("(L%s;Z)L%s;", JvmConstants.B_STRING_VALUE, XML_VALUE), false);
        this.storeToVar(newXMLComment.lhsOp.variableDcl);
    }

    void generateNewXMLProcIns(BIRNonTerminator.NewXMLProcIns newXMLPI) {

        this.loadVar(newXMLPI.targetOp.variableDcl);
        this.loadVar(newXMLPI.dataOp.variableDcl);

        if (newXMLPI.readonly) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }

        this.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLProcessingInstruction",
                                String.format("(L%s;L%s;Z)L%s;", JvmConstants.B_STRING_VALUE,
                                              JvmConstants.B_STRING_VALUE, XML_VALUE), false);
        this.storeToVar(newXMLPI.lhsOp.variableDcl);
    }

    void generateXMLStoreIns(BIRNonTerminator.XMLAccess xmlStoreIns) {

        this.loadVar(xmlStoreIns.lhsOp.variableDcl);
        this.loadVar(xmlStoreIns.rhsOp.variableDcl);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "addChildren", String.format("(L%s;)V", XML_VALUE),
                false);
    }

    void generateXMLLoadAllIns(BIRNonTerminator.XMLAccess xmlLoadAllIns) {

        this.loadVar(xmlLoadAllIns.rhsOp.variableDcl);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "children", String.format("()L%s;", XML_VALUE),
                false);
        this.storeToVar(xmlLoadAllIns.lhsOp.variableDcl);
    }

    void generateXMLAttrLoadIns(BIRNonTerminator.FieldAccess xmlAttrStoreIns) {
        // visit xml_ref
        this.loadVar(xmlAttrStoreIns.rhsOp.variableDcl);

        // visit attribute name expr
        this.loadVar(xmlAttrStoreIns.keyOp.variableDcl);
        this.mv.visitTypeInsn(CHECKCAST, XML_QNAME);

        // invoke getAttribute() method
        this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "getAttribute",
                                String.format("(L%s;)L%s;", B_XML_QNAME, STRING_VALUE), false);

        // store in the target reg
        BType targetType = xmlAttrStoreIns.lhsOp.variableDcl.type;
        this.storeToVar(xmlAttrStoreIns.lhsOp.variableDcl);
    }

    void generateXMLAttrStoreIns(BIRNonTerminator.FieldAccess xmlAttrStoreIns) {
        // visit xml_ref
        this.loadVar(xmlAttrStoreIns.lhsOp.variableDcl);

        // visit attribute name expr
        this.loadVar(xmlAttrStoreIns.keyOp.variableDcl);
        this.mv.visitTypeInsn(CHECKCAST, XML_QNAME);

        // visit attribute value expr
        this.loadVar(xmlAttrStoreIns.rhsOp.variableDcl);

        // invoke setAttribute() method
        String signature = String.format("(L%s;L%s;)V", B_XML_QNAME, JvmConstants.B_STRING_VALUE);
        this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "setAttribute", signature, false);
    }

    void generateXMLLoadIns(BIRNonTerminator.FieldAccess xmlLoadIns) {
        // visit xml_ref
        this.loadVar(xmlLoadIns.rhsOp.variableDcl);

        // visit element name/index expr
        this.loadVar(xmlLoadIns.keyOp.variableDcl);

        if (TypeTags.isStringTypeTag(xmlLoadIns.keyOp.variableDcl.type.tag)) {
            // invoke `children(name)` method
            this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "children",
                    String.format("(L%s;)L%s;", STRING_VALUE, XML_VALUE), false);
        } else {
            // invoke `getItem(index)` method
            this.mv.visitInsn(L2I);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "getItem",
                    String.format("(I)L%s;", XML_VALUE), false);
        }

        // store in the target reg
        BType targetType = xmlLoadIns.lhsOp.variableDcl.type;
        this.storeToVar(xmlLoadIns.lhsOp.variableDcl);
    }

    void generateTypeofIns(BIRNonTerminator.UnaryOP unaryOp) {

        this.loadVar(unaryOp.rhsOp.variableDcl);
        JvmCastGen.addBoxInsn(this.mv, unaryOp.rhsOp.variableDcl.type);
        this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "getTypedesc",
                String.format("(L%s;)L%s;", OBJECT, TYPEDESC_VALUE), false);
        this.storeToVar(unaryOp.lhsOp.variableDcl);
    }

    void generateNotIns(BIRNonTerminator.UnaryOP unaryOp) {

        this.loadVar(unaryOp.rhsOp.variableDcl);

        Label label1 = new Label();
        Label label2 = new Label();

        this.mv.visitJumpInsn(IFNE, label1);
        this.mv.visitInsn(ICONST_1);
        this.mv.visitJumpInsn(GOTO, label2);
        this.mv.visitLabel(label1);
        this.mv.visitInsn(ICONST_0);
        this.mv.visitLabel(label2);

        this.storeToVar(unaryOp.lhsOp.variableDcl);
    }

    void generateNegateIns(BIRNonTerminator.UnaryOP unaryOp) {

        this.loadVar(unaryOp.rhsOp.variableDcl);

        BType btype = unaryOp.rhsOp.variableDcl.type;
        if (TypeTags.isIntegerTypeTag(btype.tag)) {
            this.mv.visitInsn(LNEG);
        } else if (btype.tag == TypeTags.BYTE) {
            this.mv.visitInsn(INEG);
        } else if (btype.tag == TypeTags.FLOAT) {
            this.mv.visitInsn(DNEG);
        } else if (btype.tag == TypeTags.DECIMAL) {
            this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "negate",
                    String.format("()L%s;", DECIMAL_VALUE), false);
        } else {
            throw new BLangCompilerException(String.format("Negation is not supported for type: %s", btype));
        }

        this.storeToVar(unaryOp.lhsOp.variableDcl);
    }

    void generateNewTypedescIns(BIRNonTerminator.NewTypeDesc newTypeDesc) {

        BType type = newTypeDesc.type;
        String className = TYPEDESC_VALUE_IMPL;
        if (type.tag == TypeTags.RECORD) {
            className = getTypeDescClassName(type.tsymbol.pkgID, toNameString(type));
        }
        this.mv.visitTypeInsn(NEW, className);
        this.mv.visitInsn(DUP);
        loadType(this.mv, newTypeDesc.type);


        List<BIROperand> closureVars = newTypeDesc.closureVars;
        mv.visitIntInsn(BIPUSH, closureVars.size());
        mv.visitTypeInsn(ANEWARRAY, MAP_VALUE);
        for (int i = 0; i < closureVars.size(); i++) {
            BIROperand closureVar = closureVars.get(i);
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, i);
            this.loadVar(closureVar.variableDcl);
            mv.visitInsn(AASTORE);
        }

        String descriptor = String.format("(L%s;[L%s;)V", TYPE, MAP_VALUE);
        this.mv.visitMethodInsn(INVOKESPECIAL, className, JVM_INIT_METHOD, descriptor, false);
        this.storeToVar(newTypeDesc.lhsOp.variableDcl);
    }

    private void loadVar(BIRNode.BIRVariableDcl varDcl) {
        generateVarLoad(this.mv, varDcl, this.getJVMIndexOfVarRef(varDcl));
    }

    private void storeToVar(BIRNode.BIRVariableDcl varDcl) {
        generateVarStore(this.mv, varDcl, this.getJVMIndexOfVarRef(varDcl));
    }

    void generateConstantLoadIns(BIRNonTerminator.ConstantLoad loadIns) {

        loadConstantValue(loadIns.type, loadIns.value, this.mv);
        this.storeToVar(loadIns.lhsOp.variableDcl);
    }

    private void loadListInitialValues(BIRNonTerminator.NewArray arrayNewIns) {
        List<BIROperand> initialValues = arrayNewIns.values;
        mv.visitLdcInsn((long) initialValues.size());
        mv.visitInsn(L2I);
        mv.visitTypeInsn(ANEWARRAY, LIST_INITIAL_EXPRESSION_ENTRY);

        int i = 0;
        for (BIROperand initialValueOp : initialValues) {
            mv.visitInsn(DUP);
            mv.visitLdcInsn((long) i);
            mv.visitInsn(L2I);
            i += 1;

            mv.visitTypeInsn(NEW, LIST_INITIAL_EXPRESSION_ENTRY);
            mv.visitInsn(DUP);

            BIRNode.BIRVariableDcl varDecl = initialValueOp.variableDcl;
            this.loadVar(varDecl);
            JvmCastGen.addBoxInsn(this.mv, varDecl.type);

            mv.visitMethodInsn(INVOKESPECIAL, LIST_INITIAL_EXPRESSION_ENTRY, JVM_INIT_METHOD,
                               String.format("(L%s;)V", OBJECT), false);

            mv.visitInsn(AASTORE);
        }
    }

    void generateInstructions(int localVarOffset, AsyncDataCollector asyncDataCollector, BIRInstruction inst) {
        if (inst instanceof BIRNonTerminator.BinaryOp) {
            generateBinaryOpIns((BIRNonTerminator.BinaryOp) inst);
        } else {
            switch (inst.getKind()) {
                case MOVE:
                    generateMoveIns((BIRNonTerminator.Move) inst);
                    break;
                case CONST_LOAD:
                    generateConstantLoadIns((BIRNonTerminator.ConstantLoad) inst);
                    break;
                case NEW_STRUCTURE:
                    generateMapNewIns((BIRNonTerminator.NewStructure) inst, localVarOffset);
                    break;
                case NEW_INSTANCE:
                    generateObjectNewIns((BIRNonTerminator.NewInstance) inst, localVarOffset);
                    break;
                case MAP_STORE:
                    generateMapStoreIns((FieldAccess) inst);
                    break;
                case NEW_TABLE:
                    generateTableNewIns((NewTable) inst);
                    break;
                case TABLE_STORE:
                    generateTableStoreIns((FieldAccess) inst);
                    break;
                case TABLE_LOAD:
                    generateTableLoadIns((FieldAccess) inst);
                    break;
                case NEW_ARRAY:
                    generateArrayNewIns((BIRNonTerminator.NewArray) inst);
                    break;
                case ARRAY_STORE:
                    generateArrayStoreIns((FieldAccess) inst);
                    break;
                case MAP_LOAD:
                    generateMapLoadIns((FieldAccess) inst);
                    break;
                case ARRAY_LOAD:
                    generateArrayValueLoad((FieldAccess) inst);
                    break;
                case NEW_ERROR:
                    generateNewErrorIns((BIRNonTerminator.NewError) inst);
                    break;
                case TYPE_CAST:
                    generateCastIns((BIRNonTerminator.TypeCast) inst);
                    break;
                case IS_LIKE:
                    generateIsLikeIns((BIRNonTerminator.IsLike) inst);
                    break;
                case TYPE_TEST:
                    generateTypeTestIns((BIRNonTerminator.TypeTest) inst);
                    break;
                case OBJECT_STORE:
                    generateObjectStoreIns((FieldAccess) inst);
                    break;
                case OBJECT_LOAD:
                    generateObjectLoadIns((FieldAccess) inst);
                    break;
                case NEW_XML_ELEMENT:
                    generateNewXMLElementIns((BIRNonTerminator.NewXMLElement) inst);
                    break;
                case NEW_XML_TEXT:
                    generateNewXMLTextIns((BIRNonTerminator.NewXMLText) inst);
                    break;
                case NEW_XML_COMMENT:
                    generateNewXMLCommentIns((BIRNonTerminator.NewXMLComment) inst);
                    break;
                case NEW_XML_PI:
                    generateNewXMLProcIns((BIRNonTerminator.NewXMLProcIns) inst);
                    break;
                case NEW_XML_QNAME:
                    generateNewXMLQNameIns((BIRNonTerminator.NewXMLQName) inst);
                    break;
                case NEW_STRING_XML_QNAME:
                    generateNewStringXMLQNameIns((BIRNonTerminator.NewStringXMLQName) inst);
                    break;
                case XML_SEQ_STORE:
                    generateXMLStoreIns((BIRNonTerminator.XMLAccess) inst);
                    break;
                case XML_SEQ_LOAD:
                case XML_LOAD:
                    generateXMLLoadIns((FieldAccess) inst);
                    break;
                case XML_LOAD_ALL:
                    generateXMLLoadAllIns((BIRNonTerminator.XMLAccess) inst);
                    break;
                case XML_ATTRIBUTE_STORE:
                    generateXMLAttrStoreIns((FieldAccess) inst);
                    break;
                case XML_ATTRIBUTE_LOAD:
                    generateXMLAttrLoadIns((FieldAccess) inst);
                    break;
                case FP_LOAD:
                    generateFPLoadIns((BIRNonTerminator.FPLoad) inst, asyncDataCollector);
                    break;
                case STRING_LOAD:
                    generateStringLoadIns((FieldAccess) inst);
                    break;
                case TYPEOF:
                    generateTypeofIns((BIRNonTerminator.UnaryOP) inst);
                    break;
                case NOT:
                    generateNotIns((BIRNonTerminator.UnaryOP) inst);
                    break;
                case NEW_TYPEDESC:
                    generateNewTypedescIns((BIRNonTerminator.NewTypeDesc) inst);
                    break;
                case NEGATE:
                    generateNegateIns((BIRNonTerminator.UnaryOP) inst);
                    break;
                case PLATFORM:
                    generatePlatformIns((JInstruction) inst);
                    break;
                default:
                    throw new BLangCompilerException("JVM generation is not supported for operation " + inst);
            }
        }
    }
}
