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

import com.sun.codemodel.internal.JType;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRGlobalVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRTypeDefinition;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
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
import static org.objectweb.asm.Opcodes.SWAP;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.generateCast;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.generateCheckCast;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.generateCheckCastToByte;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.generatePlatformCheckCast;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.getTargetClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_MAP_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BTYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BXML_QNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JSON_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LONG_STREAM;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MATH_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TUPLE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TUPLE_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_CHECKER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_FACTORY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_QNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.BalToJVMIndexMap;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.currentClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getPackageName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lambdaIndex;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lambdas;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lookupFullQualifiedClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lookupGlobalVarClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lookupTypeDef;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.duplicateServiceTypeWithAnnots;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeDesc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.loadExternalOrLocalType;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.loadType;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeValueClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.ExternalMethodGen.BIRVarRef;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.JCast;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.JInstruction;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.ConstantLoad;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewInstance;


//import ballerina/io;
//import ballerina/bir;
//import ballerina/jvm;
//import ballerina/runtime;

public class JvmInstructionGen {
    public static final String I_STRING_VALUE = "org/ballerinalang/jvm/values/StringValue";
    public static final String BMP_STRING_VALUE = "org/ballerinalang/jvm/values/BmpStringValue";
    public static final String NON_BMP_STRING_VALUE = "org/ballerinalang/jvm/values/NonBmpStringValue";
    public static boolean IS_BSTRING = System.getProperty("ballerina.bstring") != "";
    public static String BSTRING_VALUE = System.getProperty("ballerina.bstring") == "" ? STRING_VALUE : I_STRING_VALUE;

    static void addBoxInsn(MethodVisitor mv, @Nilable BType bType) {
        if (bType == null) {
            return;
        } else {
            generateCast(mv, bType, "any");
        }
    }

    ;

    static void addUnboxInsn(MethodVisitor mv, @Nilable BType bType, boolean useBString /* = false */) {
        if (bType == null) {
            return;
        } else {
            generateCast(mv, "any", bType, useBString = useBString);
        }
    }

    static void addJUnboxInsn(MethodVisitor mv, @Nilable BType bType) {
        if (bType == null) {
            return;
        } else if (bType.tag == JTypeTags.JBYTE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJByte", String.format("(L%s;)B", OBJECT), false);
        } else if (bType.tag == JTypeTags.JCHAR) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJChar", String.format("(L%s;)C", OBJECT), false);
        } else if (bType.tag == JTypeTags.JSHORT) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJShort", String.format("(L%s;)S", OBJECT), false);
        } else if (bType.tag == JTypeTags.JINT) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJInt", String.format("(L%s;)I", OBJECT), false);
        } else if (bType.tag == JTypeTags.JLONG) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJLong", String.format("(L%s;)J", OBJECT), false);
        } else if (bType.tag == JTypeTags.JFLOAT) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJFloat", String.format("(L%s;)F", OBJECT), false);
        } else if (bType.tag == JTypeTags.JDOUBLE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJDouble", String.format("(L%s;)D", OBJECT), false);
        } else if (bType.tag == JTypeTags.JBOOLEAN) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJBoolean", String.format("(L%s;)Z", OBJECT), false);
        } else if (bType.tag == JTypeTags.JREF) {
            mv.visitTypeInsn(CHECKCAST, bType.type);
            //} else {
            //    error err = error(io:sprintf("Unboxing is not supported for '%s'", bType));
            //    panic err;
        }
    }

    static void generateVarLoad(MethodVisitor mv, BIRVariableDcl varDcl, String currentPackageName, int valueIndex) {
        BType bType = varDcl.type;

        if (varDcl.kind == BIRVAR_KIND_GLOBAL) {
            BIRGlobalVariableDcl globalVar = (BIRGlobalVariableDcl) varDcl;
            PackageID modId = (PackageID) globalVar ?.moduleId;
            String moduleName = getPackageName(modId.org, modId.name);

            String varName = varDcl.name.value;
            String className = lookupGlobalVarClassName(moduleName + varName);

            String typeSig = getTypeDesc(bType);
            mv.visitFieldInsn(GETSTATIC, className, varName, typeSig);
            return;
        } else if (varDcl.kind == BIRVAR_KIND_SELF) {
            mv.visitVarInsn(ALOAD, 0);
            return;
        } else if (varDcl.kind == BIRVAR_KIND_CONSTANT) {
            String varName = varDcl.name.value;
            PackageID moduleId = (PackageID) varDcl ?.moduleId;
            String pkgName = getPackageName(moduleId.org, moduleId.name);
            String className = lookupGlobalVarClassName(pkgName + varName);
            String typeSig = getTypeDesc(bType);
            mv.visitFieldInsn(GETSTATIC, className, varName, typeSig);
            return;
        }

        if (bType.tag == TypeTags.INT) {
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
                bType.tag == TypeTags.STRING ||
                bType.tag == TypeTags.MAP ||
                bType.tag == TypeTags.TABLE ||
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
                bType.tag == TypeTags.SERVICE ||
                bType.tag == TypeTags.DECIMAL ||
                bType.tag == TypeTags.XML ||
                bType.tag == TypeTags.INVOKABLE ||
                bType.tag == TypeTags.FINITE ||
                bType.tag == TypeTags.HANDLE ||
                bType.tag == TypeTags.TYPEDESC) {
            mv.visitVarInsn(ALOAD, valueIndex);
        } else if (bType.tag == JTypeTags.JTYPE) {
            generateJVarLoad(mv, bType, currentPackageName, valueIndex);
        } else {
            BLangCompilerException err = new BLangCompilerException("JVM generation is not supported for type " + String.format("%s", bType));
            throw err;
        }
    }

    static void generateJVarLoad(MethodVisitor mv, JType jType, String currentPackageName, int valueIndex) {

        if (jType.tag == JTypeTags.JBYTE) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (jType.tag == JTypeTags.JCHAR) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (jType.tag == JTypeTags.JSHORT) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (jType.tag == JTypeTags.JINT) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (jType.tag == JTypeTags.JLONG) {
            mv.visitVarInsn(LLOAD, valueIndex);
        } else if (jType.tag == JTypeTags.JFLOAT) {
            mv.visitVarInsn(FLOAD, valueIndex);
        } else if (jType.tag == JTypeTags.JDOUBLE) {
            mv.visitVarInsn(DLOAD, valueIndex);
        } else if (jType.tag == JTypeTags.JBOOLEAN) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (jType.tag == JTypeTags.JARRAY ||
                jType.tag == JTypeTags.JREF) {
            mv.visitVarInsn(ALOAD, valueIndex);
        } else {
            BLangCompilerException err = new BLangCompilerException("JVM generation is not supported for type " + String.format("%s", jType));
            throw err;
        }
    }

    static void generateVarStore(MethodVisitor mv, BIRVariableDcl varDcl, String currentPackageName, int valueIndex) {
        BType bType = varDcl.type;

        if (varDcl.kind == BIRVAR_KIND_GLOBAL) {
            String varName = varDcl.name.value;
            String className = lookupGlobalVarClassName(currentPackageName + varName);
            String typeSig = getTypeDesc(bType);
            mv.visitFieldInsn(PUTSTATIC, className, varName, typeSig);
            return;
        } else if (varDcl.kind == BIRVAR_KIND_CONSTANT) {
            String varName = varDcl.name.value;
            PackageID moduleId = (PackageID) varDcl ?.moduleId;
            String pkgName = getPackageName(moduleId.org, moduleId.name);
            String className = lookupGlobalVarClassName(pkgName + varName);
            String typeSig = getTypeDesc(bType);
            mv.visitFieldInsn(PUTSTATIC, className, varName, typeSig);
            return;
        }

        if (bType.tag == TypeTags.INT) {
            mv.visitVarInsn(LSTORE, valueIndex);
        } else if (bType.tag == TypeTags.BYTE) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (bType.tag == TypeTags.FLOAT) {
            mv.visitVarInsn(DSTORE, valueIndex);
        } else if (bType.tag == TypeTags.BOOLEAN) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (bType.tag == TypeTags.ARRAY ||
                bType.tag == TypeTags.STRING ||
                bType.tag == TypeTags.MAP ||
                bType.tag == TypeTags.TABLE ||
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
                bType.tag == TypeTags.XML ||
                bType.tag == TypeTags.INVOKABLE ||
                bType.tag == TypeTags.FINITE ||
                bType.tag == TypeTags.HANDLE ||
                bType.tag == TypeTags.TYPEDESC) {
            mv.visitVarInsn(ASTORE, valueIndex);
        } else if (bType.tag == JTypeTags.JTYPE) {
            generateJVarStore(mv, bType, currentPackageName, valueIndex);
        } else {
            BLangCompilerException err = new BLangCompilerException("JVM generation is not supported for type " + String.format("%s", bType));
            throw err;
        }
    }

    static void generateJVarStore(MethodVisitor mv, JType jType, String currentPackageName, int valueIndex) {
        if (jType.tag == JTypeTags.JBYTE) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (jType.tag == JTypeTags.JCHAR) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (jType.tag == JTypeTags.JSHORT) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (jType.tag == JTypeTags.JINT) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (jType.tag == JTypeTags.JLONG) {
            mv.visitVarInsn(LSTORE, valueIndex);
        } else if (jType.tag == JTypeTags.JFLOAT) {
            mv.visitVarInsn(FSTORE, valueIndex);
        } else if (jType.tag == JTypeTags.JDOUBLE) {
            mv.visitVarInsn(DSTORE, valueIndex);
        } else if (jType.tag == JTypeTags.JBOOLEAN) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (jType.tag == JTypeTags.JARRAY ||
                jType.tag == JTypeTags.JREF) {
            mv.visitVarInsn(ASTORE, valueIndex);
        } else {
            BLangCompilerException err = new BLangCompilerException("JVM generation is not supported for type " + String.format("%s", jType));
            throw err;
        }
    }

    static class InstructionGenerator {
        MethodVisitor mv;
        BalToJVMIndexMap indexMap;
        String currentPackageName;
        BIRPackage currentPackage;

        static void generatePlatformIns(JInstruction ins) {
            if (ins.jKind == JCAST) {
                JCast castIns = (JCast) ins;
                BType targetType = castIns.targetType;
                this.loadVar(castIns.rhsOp.variableDcl);
                generatePlatformCheckCast(this.mv, this.indexMap, castIns.rhsOp.type, targetType);
                this.storeToVar(castIns.lhsOp.variableDcl);
            }
        }

        static void generateMoveIns(BIRMove moveIns) {
            this.loadVar(moveIns.rhsOp.variableDcl);
            this.storeToVar(moveIns.lhsOp.variableDcl);
        }

        static void generateBinaryOpIns(BIRBinaryOp binaryIns) {
            int insKind = binaryIns.kind;
            if (insKind <= BIRBINARY_LESS_EQUAL) {
                if (insKind == BIRBINARY_ADD) {
                    this.generateAddIns(binaryIns);
                } else if (insKind == BIRBINARY_SUB) {
                    this.generateSubIns(binaryIns);
                } else if (insKind == BIRBINARY_MUL) {
                    this.generateMulIns(binaryIns);
                } else if (insKind == BIRBINARY_DIV) {
                    this.generateDivIns(binaryIns);
                } else if (insKind == BIRBINARY_MOD) {
                    this.generateRemIns(binaryIns);
                } else if (insKind == BIRBINARY_EQUAL) {
                    this.generateEqualIns(binaryIns);
                } else if (insKind == BIRBINARY_NOT_EQUAL) {
                    this.generateNotEqualIns(binaryIns);
                } else if (insKind == BIRBINARY_GREATER_THAN) {
                    this.generateGreaterThanIns(binaryIns);
                } else if (insKind == BIRBINARY_GREATER_EQUAL) {
                    this.generateGreaterEqualIns(binaryIns);
                } else if (insKind == BIRBINARY_LESS_THAN) {
                    this.generateLessThanIns(binaryIns);
                } else {
                    this.generateLessEqualIns(binaryIns);
                }
            } else if (insKind <= BIRBINARY_BITWISE_UNSIGNED_RIGHT_SHIFT) {
                if (insKind == BIRBINARY_REF_EQUAL) {
                    this.generateRefEqualIns(binaryIns);
                } else if (insKind == BIRBINARY_REF_NOT_EQUAL) {
                    this.generateRefNotEqualIns(binaryIns);
                } else if (insKind == BIRBINARY_CLOSED_RANGE) {
                    this.generateClosedRangeIns(binaryIns);
                } else if (insKind == BIRBINARY_HALF_OPEN_RANGE) {
                    this.generateClosedRangeIns(binaryIns);
                } else if (insKind == BIRBINARY_ANNOT_ACCESS) {
                    this.generateAnnotAccessIns(binaryIns);
                } else if (insKind == BIRBINARY_BITWISE_AND) {
                    this.generateBitwiseAndIns(binaryIns);
                } else if (insKind == BIRBINARY_BITWISE_OR) {
                    this.generateBitwiseOrIns(binaryIns);
                } else if (insKind == BIRBINARY_BITWISE_XOR) {
                    this.generateBitwiseXorIns(binaryIns);
                } else if (insKind == BIRBINARY_BITWISE_LEFT_SHIFT) {
                    this.generateBitwiseLeftShiftIns(binaryIns);
                } else if (insKind == BIRBINARY_BITWISE_RIGHT_SHIFT) {
                    this.generateBitwiseRightShiftIns(binaryIns);
                } else {
                    this.generateBitwiseUnsignedRightShiftIns(binaryIns);
                }
            } else {
                BLangCompilerException err = new BLangCompilerException("JVM generation is not supported for type : " + String.format("%s", insKind));
                throw err;
            }
        }

        static void generateBinaryRhsAndLhsLoad(BIRBinaryOp binaryIns) {
            this.loadVar(binaryIns.rhsOp1.variableDcl);
            this.loadVar(binaryIns.rhsOp2.variableDcl);
        }

        private static void generateLessThanIns(BIRBinaryOp binaryIns) {
            this.generateBinaryCompareIns(binaryIns, IFLT);
        }

        private static void generateGreaterThanIns(BIRBinaryOp binaryIns) {
            this.generateBinaryCompareIns(binaryIns, IFGT);
        }

        private static void generateLessEqualIns(BIRBinaryOp binaryIns) {
            this.generateBinaryCompareIns(binaryIns, IFLE);

        }

        private static void generateGreaterEqualIns(BIRBinaryOp binaryIns) {
            this.generateBinaryCompareIns(binaryIns, IFGE);
        }

        private static void generateBinaryCompareIns(BIRBinaryOp binaryIns, int opcode) {
            if (opcode != IFLT && opcode != IFGT && opcode != IFLE && opcode != IFGE) {
                BLangCompilerException err = new BLangCompilerException(String.format("Unsupported opcode '%s' for binary operator.", opcode));
                throw err;
            }

            this.generateBinaryRhsAndLhsLoad(binaryIns);
            Label label1 = new;
            Label label2 = new;

            BType lhsOpType = binaryIns.rhsOp1.variableDcl.type;
            BType rhsOpType = binaryIns.rhsOp2.variableDcl.type;

            if (lhsOpType.tag == TypeTags.INT && rhsOpType.tag == TypeTags.INT) {
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

        private static String getDecimalCompareFuncName(int opcode) {
            if (opcode == IFGT) {
                return "checkDecimalGreaterThan";
            } else if (opcode == IFGE) {
                return "checkDecimalGreaterThanOrEqual";
            } else if (opcode == IFLT) {
                return "checkDecimalLessThan";
            } else if (opcode == IFLE) {
                return "checkDecimalLessThanOrEqual";
            } else {
                BLangCompilerException err = new BLangCompilerException(String.format("Opcode: '%s' is not a comparison opcode.", opcode));
                throw err;
            }
        }

        static void generateEqualIns(BIRBinaryOp binaryIns) {
            this.generateBinaryRhsAndLhsLoad(binaryIns);

            Label label1 = new;
            Label label2 = new;

            BType lhsOpType = binaryIns.rhsOp1.variableDcl.type;
            BType rhsOpType = binaryIns.rhsOp2.variableDcl.type;

            if (lhsOpType.tag == TypeTags.INT && rhsOpType.tag == TypeTags.INT) {
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

        static void generateNotEqualIns(BIRBinaryOp binaryIns) {
            this.generateBinaryRhsAndLhsLoad(binaryIns);

            Label label1 = new;
            Label label2 = new;

            // It is assumed that both operands are of same type
            BType lhsOpType = binaryIns.rhsOp1.variableDcl.type;
            BType rhsOpType = binaryIns.rhsOp2.variableDcl.type;
            if (lhsOpType.tag == TypeTags.INT && rhsOpType.tag == TypeTags.INT) {
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

        static void generateRefEqualIns(BIRBinaryOp binaryIns) {
            this.generateBinaryRhsAndLhsLoad(binaryIns);

            Label label1 = new;
            Label label2 = new;

            BType lhsOpType = binaryIns.rhsOp1.variableDcl.type;
            BType rhsOpType = binaryIns.rhsOp2.variableDcl.type;
            if (lhsOpType.tag == TypeTags.INT && rhsOpType.tag == TypeTags.INT) {
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

        static void generateRefNotEqualIns(BIRBinaryOp binaryIns) {
            this.generateBinaryRhsAndLhsLoad(binaryIns);

            Label label1 = new;
            Label label2 = new;

            // It is assumed that both operands are of same type
            BType lhsOpType = binaryIns.rhsOp1.variableDcl.type;
            BType rhsOpType = binaryIns.rhsOp2.variableDcl.type;
            if (lhsOpType.tag == TypeTags.INT && rhsOpType.tag == TypeTags.INT) {
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

        static void generateClosedRangeIns(BIRBinaryOp binaryIns) {
            this.mv.visitTypeInsn(NEW, ARRAY_VALUE_IMPL);
            this.mv.visitInsn(DUP);
            this.generateBinaryRhsAndLhsLoad(binaryIns);
            this.mv.visitMethodInsn(INVOKESTATIC, LONG_STREAM, "rangeClosed", String.format("(JJ)L%s;", LONG_STREAM), true);
            this.mv.visitMethodInsn(INVOKEINTERFACE, LONG_STREAM, "toArray", "()[J", true);
            this.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_VALUE_IMPL, "<init>", "([J)V", false);
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        static void generateAnnotAccessIns(BIRBinaryOp binaryIns) {
            this.loadVar(binaryIns.rhsOp1.variableDcl);
            this.loadVar(binaryIns.rhsOp2.variableDcl);
            this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "getAnnotValue",
                    String.format("(L%s;L%s;)L%s;", TYPEDESC_VALUE, STRING_VALUE, OBJECT), false);

            BType targetType = binaryIns.lhsOp.variableDcl.type;
            addUnboxInsn(this.mv, targetType);
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        static void generateAddIns(BIRBinaryOp binaryIns) {
            BType bType = binaryIns.lhsOp.type;
            this.generateBinaryRhsAndLhsLoad(binaryIns);
            if (bType.tag == TypeTags.INT) {
                this.mv.visitInsn(LADD);
            } else if (bType.tag == TypeTags.BYTE) {
                this.mv.visitInsn(IADD);
            } else if (bType.tag == TypeTags.STRING) {
                if (IS_BSTRING) {
                    this.mv.visitMethodInsn(INVOKEINTERFACE, BSTRING_VALUE, "concat",
                            String.format("(L%s;)L%s;", BSTRING_VALUE, BSTRING_VALUE), true);
                } else {
                    this.mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat",
                            String.format("(L%s;)L%s;", STRING_VALUE, STRING_VALUE), false);
                }
            } else if (bType.tag == TypeTags.DECIMAL) {
                this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "add",
                        String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE), false);
            } else if (bType.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DADD);
            } else if (bType.tag == TypeTags.XML) {
                this.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "concatenate",
                        String.format("(L%s;L%s;)L%s;", XML_VALUE, XML_VALUE, XML_VALUE), false);
            } else {
                BLangCompilerException err = new BLangCompilerException("JVM generation is not supported for type " +
                        String.format("%s", binaryIns.lhsOp.type));
                throw err;
            }

            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        static void generateSubIns(BIRBinaryOp binaryIns) {
            BType bType = binaryIns.lhsOp.type;
            this.generateBinaryRhsAndLhsLoad(binaryIns);
            if (bType.tag == TypeTags.INT) {
                this.mv.visitInsn(LSUB);
            } else if (bType.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DSUB);
            } else if (bType.tag == TypeTags.DECIMAL) {
                this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "subtract",
                        String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE), false);
            } else {
                BLangCompilerException err = new BLangCompilerException("JVM generation is not supported for type " +
                        String.format("%s", binaryIns.lhsOp.type));
                throw err;
            }
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        static void generateDivIns(BIRBinaryOp binaryIns) {
            BType bType = binaryIns.lhsOp.type;
            this.generateBinaryRhsAndLhsLoad(binaryIns);
            if (bType.tag == TypeTags.INT) {
                this.mv.visitMethodInsn(INVOKESTATIC, MATH_UTILS, "divide", "(JJ)J", false);
            } else if (bType.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DDIV);
            } else if (bType.tag == TypeTags.DECIMAL) {
                this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "divide",
                        String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE), false);
            } else {
                BLangCompilerException err = new BLangCompilerException("JVM generation is not supported for type " +
                        String.format("%s", binaryIns.lhsOp.type));
                throw err;
            }
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        static void generateMulIns(BIRBinaryOp binaryIns) {
            BType bType = binaryIns.lhsOp.type;
            this.generateBinaryRhsAndLhsLoad(binaryIns);
            if (bType.tag == TypeTags.INT) {
                this.mv.visitInsn(LMUL);
            } else if (bType.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DMUL);
            } else if (bType.tag == TypeTags.DECIMAL) {
                this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "multiply",
                        String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE), false);
            } else {
                BLangCompilerException err = new BLangCompilerException("JVM generation is not supported for type " +
                        String.format("%s", binaryIns.lhsOp.type));
                throw err;
            }
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        static void generateRemIns(BIRBinaryOp binaryIns) {
            BType bType = binaryIns.lhsOp.type;
            this.generateBinaryRhsAndLhsLoad(binaryIns);
            if (bType.tag == TypeTags.INT) {
                this.mv.visitMethodInsn(INVOKESTATIC, MATH_UTILS, "remainder", "(JJ)J", false);
            } else if (bType.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DREM);
            } else if (bType.tag == TypeTags.DECIMAL) {
                this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "remainder",
                        String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE), false);
            } else {
                BLangCompilerException err = new BLangCompilerException("JVM generation is not supported for type " +
                        String.format("%s", binaryIns.lhsOp.type));
                throw err;
            }
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        static void generateBitwiseAndIns(BIRBinaryOp binaryIns) {
            BType opType1 = binaryIns.rhsOp1.type;
            BType opType2 = binaryIns.rhsOp2.type;

            if (opType1.tag == TypeTags.INT && opType2.tag == TypeTags.INT) {
                this.loadVar(binaryIns.rhsOp1.variableDcl);
                this.loadVar(binaryIns.rhsOp2.variableDcl);
                this.mv.visitInsn(LAND);
            } else {
                this.loadVar(binaryIns.rhsOp1.variableDcl);
                generateCheckCastToByte(this.mv, opType1);

                this.loadVar(binaryIns.rhsOp2.variableDcl);
                generateCheckCastToByte(this.mv, opType2);

                this.mv.visitInsn(IAND);
            }
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        static void generateBitwiseOrIns(BIRBinaryOp binaryIns) {
            this.loadVar(binaryIns.rhsOp1.variableDcl);
            this.loadVar(binaryIns.rhsOp2.variableDcl);

            BType opType = binaryIns.rhsOp1.type;
            if (opType.tag == TypeTags.INT) {
                this.mv.visitInsn(LOR);
            } else {
                this.mv.visitInsn(IOR);
            }
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        static void generateBitwiseXorIns(BIRBinaryOp binaryIns) {
            this.loadVar(binaryIns.rhsOp1.variableDcl);
            this.loadVar(binaryIns.rhsOp2.variableDcl);

            BType opType = binaryIns.rhsOp1.type;
            if (opType.tag == TypeTags.INT) {
                this.mv.visitInsn(LXOR);
            } else {
                this.mv.visitInsn(IXOR);
            }
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        static void generateBitwiseLeftShiftIns(BIRBinaryOp binaryIns) {
            this.loadVar(binaryIns.rhsOp1.variableDcl);
            this.loadVar(binaryIns.rhsOp2.variableDcl);

            BType secondOpType = binaryIns.rhsOp2.type;
            if (secondOpType.tag == TypeTags.INT) {
                this.mv.visitInsn(L2I);
            }

            BType firstOpType = binaryIns.rhsOp1.type;
            if (firstOpType.tag == TypeTags.INT) {
                this.mv.visitInsn(LSHL);
            } else {
                this.mv.visitInsn(ISHL);
                this.mv.visitInsn(I2L);
            }

            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        static void generateBitwiseRightShiftIns(BIRBinaryOp binaryIns) {
            this.loadVar(binaryIns.rhsOp1.variableDcl);
            this.loadVar(binaryIns.rhsOp2.variableDcl);

            BType secondOpType = binaryIns.rhsOp2.type;
            if (secondOpType.tag == TypeTags.INT) {
                this.mv.visitInsn(L2I);
            }

            BType firstOpType = binaryIns.rhsOp1.type;
            if (firstOpType.tag == TypeTags.INT) {
                this.mv.visitInsn(LSHR);
            } else {
                this.mv.visitInsn(ISHR);
            }

            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        static void generateBitwiseUnsignedRightShiftIns(BIRBinaryOp binaryIns) {
            this.loadVar(binaryIns.rhsOp1.variableDcl);
            this.loadVar(binaryIns.rhsOp2.variableDcl);

            BType secondOpType = binaryIns.rhsOp2.type;
            if (secondOpType.tag == TypeTags.INT) {
                this.mv.visitInsn(L2I);
            }

            BType firstOpType = binaryIns.rhsOp1.type;
            if (firstOpType.tag == TypeTags.INT) {
                this.mv.visitInsn(LUSHR);
            } else {
                this.mv.visitInsn(IUSHR);
            }

            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        static int getJVMIndexOfVarRef(BIRVariableDcl varDcl) {
            return this.indexMap.getIndex(varDcl);
        }

        static void generateMapNewIns(BIRNewMap mapNewIns, int localVarOffset) {
            BType typeOfMapNewIns = mapNewIns.bType;
            String className = MAP_VALUE_IMPL;

            if (typeOfMapNewIns.tag == TypeTags.RECORD) {
                var typeRef = mapNewIns.typeRef;
                if (typeRef instanceof BIRTypeRef) {
                    className = getTypeValueClassName(typeRef.externalPkg, typeOfMapNewIns.name.value);
                } else {
                    className = getTypeValueClassName(this.currentPackage, typeOfMapNewIns.name.value);
                }

                this.mv.visitTypeInsn(NEW, className);
                this.mv.visitInsn(DUP);
                this.mv.visitInsn(DUP);
                if (typeRef instanceof BIRTypeRef) {
                    loadExternalOrLocalType(this.mv, typeRef);
                } else {
                    loadType(this.mv, mapNewIns.bType);
                }
                this.mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", String.format("(L%s;)V", BTYPE), false);

                // Invoke the init-function of this type.
                this.mv.visitVarInsn(ALOAD, localVarOffset);
                this.mv.visitInsn(SWAP);
                this.mv.visitMethodInsn(INVOKESTATIC, className, "$init", String.format("(L%s;L%s;)V", STRAND, MAP_VALUE), false);
            } else {
                this.mv.visitTypeInsn(NEW, className);
                this.mv.visitInsn(DUP);
                loadType(this.mv, mapNewIns.bType);
                this.mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", String.format("(L%s;)V", BTYPE), false);
            }
            this.storeToVar(mapNewIns.lhsOp.variableDcl);
        }

        static void generateTableNewIns(BIRNewTable tableNewIns) {
            this.mv.visitTypeInsn(NEW, TABLE_VALUE);
            this.mv.visitInsn(DUP);
            loadType(this.mv, tableNewIns.type);
            this.loadVar(tableNewIns.keyColOp.variableDcl);
            this.loadVar(tableNewIns.dataOp.variableDcl);
            this.mv.visitMethodInsn(INVOKESPECIAL, TABLE_VALUE, "<init>", String.format("(L%s;L%s;L%s;)V", BTYPE,
                    ARRAY_VALUE, ARRAY_VALUE), false);
            this.storeToVar(tableNewIns.lhsOp.variableDcl);
        }

        static void generateMapStoreIns(BIRFieldAccess mapStoreIns) {
            // visit map_ref
            this.loadVar(mapStoreIns.lhsOp.variableDcl);
            BType varRefType = mapStoreIns.lhsOp.variableDcl.type;

            // visit key_expr
            this.loadVar(mapStoreIns.keyOp.variableDcl);

            // visit value_expr
            BType valueType = mapStoreIns.rhsOp.variableDcl.type;
            this.loadVar(mapStoreIns.rhsOp.variableDcl);
            addBoxInsn(this.mv, valueType);

            if (varRefType.tag == TypeTags.JSON) {
                this.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "setElement",
                        String.format("(L%s;L%s;L%s;)V", OBJECT, STRING_VALUE, OBJECT), false);
            } else {
                this.mv.visitMethodInsn(INVOKESTATIC, MAP_UTILS, "handleMapStore",
                        String.format("(L%s;L%s;L%s;)V", MAP_VALUE, STRING_VALUE, OBJECT),
                        false);
            }
        }

        static void generateMapLoadIns(BIRFieldAccess mapLoadIns) {
            // visit map_ref
            this.loadVar(mapLoadIns.rhsOp.variableDcl);
            BType varRefType = mapLoadIns.rhsOp.variableDcl.type;
            addUnboxInsn(this.mv, varRefType);

            // visit key_expr
            this.loadVar(mapLoadIns.keyOp.variableDcl);

            if (varRefType.tag == TypeTags.JSON) {
                if (mapLoadIns.optionalFieldAccess) {
                    this.mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
                    this.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "getElementOrNil",
                            String.format("(L%s;L%s;)L%s;", OBJECT, STRING_VALUE, OBJECT), false);
                } else {
                    this.mv.visitTypeInsn(CHECKCAST, STRING_VALUE);
                    this.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "getElement",
                            String.format("(L%s;L%s;)L%s;", OBJECT, STRING_VALUE, OBJECT), false);
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
            addUnboxInsn(this.mv, targetType);
            this.storeToVar(mapLoadIns.lhsOp.variableDcl);
        }

        static void generateObjectLoadIns(BIRFieldAccess objectLoadIns) {
            // visit object_ref
            this.loadVar(objectLoadIns.rhsOp.variableDcl);
            BType varRefType = objectLoadIns.rhsOp.variableDcl.type;

            // visit key_expr
            this.loadVar(objectLoadIns.keyOp.variableDcl);

            // invoke get() method, and unbox if needed
            this.mv.visitMethodInsn(INVOKEINTERFACE, OBJECT_VALUE, "get",
                    String.format("(L%s;)L%s;", STRING_VALUE, OBJECT), true);
            BType targetType = objectLoadIns.lhsOp.variableDcl.type;
            addUnboxInsn(this.mv, targetType);

            // store in the target reg
            this.storeToVar(objectLoadIns.lhsOp.variableDcl);
        }

        static void generateObjectStoreIns(BIRFieldAccess objectStoreIns, boolean useBString) {
            // visit object_ref
            this.loadVar(objectStoreIns.lhsOp.variableDcl);
            BType varRefType = objectStoreIns.lhsOp.variableDcl.type;

            // visit key_expr
            this.loadVar(objectStoreIns.keyOp.variableDcl);

            // visit value_expr
            BType valueType = objectStoreIns.rhsOp.variableDcl.type;
            this.loadVar(objectStoreIns.rhsOp.variableDcl);
            addBoxInsn(this.mv, valueType);

            // invoke set() method
            this.mv.visitMethodInsn(INVOKEINTERFACE, OBJECT_VALUE, "set",
                    String.format("(L%s;L%s;)V", useBString ? I_STRING_VALUE : STRING_VALUE, OBJECT), true);
        }

        static void generateStringLoadIns(BIRFieldAccess stringLoadIns) {
            // visit the string
            this.loadVar(stringLoadIns.rhsOp.variableDcl);

            // visit the key expr
            this.loadVar(stringLoadIns.keyOp.variableDcl);

            // invoke the `getStringAt()` method
            this.mv.visitMethodInsn(INVOKESTATIC, STRING_UTILS, "getStringAt",
                    String.format("(L%s;J)L%s;", STRING_VALUE, STRING_VALUE), false);

            // store in the target reg
            this.storeToVar(stringLoadIns.lhsOp.variableDcl);
        }

        //    # Generate a new instance of an array value
//    #
//    # + inst - the new array instruction
        static void generateArrayNewIns(BIRNewArray inst) {
            if (inst.type.tag == TypeTags.ARRAY) {
                this.mv.visitTypeInsn(NEW, ARRAY_VALUE_IMPL);
                this.mv.visitInsn(DUP);
                loadType(this.mv, inst.type);
                this.loadVar(inst.sizeOp.variableDcl);
                this.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_VALUE_IMPL, "<init>", String.format("(L%s;J)V", ARRAY_TYPE), false);
                this.storeToVar(inst.lhsOp.variableDcl);
            } else {
                this.mv.visitTypeInsn(NEW, TUPLE_VALUE_IMPL);
                this.mv.visitInsn(DUP);
                loadType(this.mv, inst.type);
                this.loadVar(inst.sizeOp.variableDcl);
                this.mv.visitMethodInsn(INVOKESPECIAL, TUPLE_VALUE_IMPL, "<init>", String.format("(L%s;J)V", TUPLE_TYPE), false);
                this.storeToVar(inst.lhsOp.variableDcl);
            }
        }

        //    # Generate adding a new value to an array
//    #
//    # + inst - array store instruction
        static void generateArrayStoreIns(BIRFieldAccess inst) {
            this.loadVar(inst.lhsOp.variableDcl);
            this.loadVar(inst.keyOp.variableDcl);
            this.loadVar(inst.rhsOp.variableDcl);

            BType valueType = inst.rhsOp.variableDcl.type;
            BType varRefType = inst.lhsOp.variableDcl.type;
            if (varRefType.tag == TypeTags.JSON ||
                    (varRefType.tag == TypeTags.ARRAY && varRefType.eType instanceof BIRBJSONType)) {
                addBoxInsn(this.mv, valueType);
                this.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "setArrayElement",
                        String.format("(L%s;JL%s;)V", OBJECT, OBJECT), false);
                return;
            }

            if (valueType.tag == TypeTags.INT) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", "(JJ)V", true);
            } else if (valueType.tag == TypeTags.FLOAT) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", "(JD)V", true);
            } else if (valueType.tag == TypeTags.STRING) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", String.format("(JL%s;)V", STRING_VALUE), true);
            } else if (valueType.tag == TypeTags.BOOLEAN) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", "(JZ)V", true);
            } else if (valueType.tag == TypeTags.BYTE) {
                this.mv.visitInsn(I2B);
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", "(JB)V", true);
            } else {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", String.format("(JL%s;)V", OBJECT), true);
            }
        }

        //    # Generating loading a new value from an array to the top of the stack
//    #
//    # + inst - field access instruction
        static void generateArrayValueLoad(BIRFieldAccess inst) {
            this.loadVar(inst.rhsOp.variableDcl);
            this.mv.visitTypeInsn(CHECKCAST, ARRAY_VALUE);
            this.loadVar(inst.keyOp.variableDcl);
            BType bType = inst.lhsOp.variableDcl.type;

            BType varRefType = inst.rhsOp.variableDcl.type;
            if (varRefType.tag == TypeTags.TUPLE) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getRefValue", String.format("(J)L%s;", OBJECT), true);
                addUnboxInsn(this.mv, bType);
            } else if (bType.tag == TypeTags.INT) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getInt", "(J)J", true);
            } else if (bType.tag == TypeTags.STRING) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getString", String.format("(J)L%s;", STRING_VALUE),
                        true);
            } else if (bType.tag == TypeTags.BOOLEAN) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getBoolean", "(J)Z", true);
            } else if (bType.tag == TypeTags.BYTE) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getByte", "(J)B", true);
                this.mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "toUnsignedInt", "(B)I", false);
            } else if (bType.tag == TypeTags.FLOAT) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getFloat", "(J)D", true);
            } else {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getRefValue", String.format("(J)L%s;", OBJECT), true);
                @Nilable String targetTypeClass = getTargetClass(bType);
                if (targetTypeClass instanceof String) {
                    this.mv.visitTypeInsn(CHECKCAST, targetTypeClass);
                } else {
                    addUnboxInsn(this.mv, bType);
                }
            }
            this.storeToVar(inst.lhsOp.variableDcl);
        }

        static void generateNewErrorIns(BIRNewError newErrorIns) {
            this.mv.visitTypeInsn(NEW, ERROR_VALUE);
            this.mv.visitInsn(DUP);
            // load errorType
            loadType(this.mv, newErrorIns.type);
            this.loadVar(newErrorIns.reasonOp.variableDcl);
            this.loadVar(newErrorIns.detailsOp.variableDcl);
            this.mv.visitMethodInsn(INVOKESPECIAL, ERROR_VALUE, "<init>",
                    String.format("(L%s;L%s;L%s;)V", BTYPE, STRING_VALUE, OBJECT), false);
            this.storeToVar(newErrorIns.lhsOp.variableDcl);
        }

        static void generateCastIns(BIRTypeCast typeCastIns) {
            // load source value
            this.loadVar(typeCastIns.rhsOp.variableDcl);
            if (typeCastIns.checkType) {
                generateCheckCast(this.mv, typeCastIns.rhsOp.type, typeCastIns.castType);
            } else {
                generateCast(this.mv, typeCastIns.rhsOp.type, typeCastIns.castType);
            }
            this.storeToVar(typeCastIns.lhsOp.variableDcl);
        }

        static void generateTypeTestIns(BIRTypeTest typeTestIns) {
            // load source value
            this.loadVar(typeTestIns.rhsOp.variableDcl);

            // load targetType
            loadType(this.mv, typeTestIns.type);

            this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkIsType",
                    String.format("(L%s;L%s;)Z", OBJECT, BTYPE), false);
            this.storeToVar(typeTestIns.lhsOp.variableDcl);
        }

        static void generateIsLikeIns(BIRIsLike isLike) {
            // load source value
            this.loadVar(isLike.rhsOp.variableDcl);

            // load targetType
            loadType(this.mv, isLike.typeVal);

            this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkIsLikeType",
                    String.format("(L%s;L%s;)Z", OBJECT, BTYPE), false);
            this.storeToVar(isLike.lhsOp.variableDcl);
        }

        static void generateObjectNewIns(NewInstance objectNewIns, int strandIndex) {
            BIRTypeDefinition typeDef = lookupTypeDef(objectNewIns);
            String className;
            if (typeDefRef instanceof BIRTypeRef) {
                className = getTypeValueClassName(typeDefRef.externalPkg, typeDefRef.name.value);
            } else {
                className = getTypeValueClassName(this.currentPackage, typeDefRef.name.value);
            }

            this.mv.visitTypeInsn(NEW, className);
            this.mv.visitInsn(DUP);

            BType type = typeDef.type;
            if (type.tag == TypeTags.SERVICE) {
                // For services, create a new type for each new service value. TODO: do only for local vars
                duplicateServiceTypeWithAnnots(this.mv, type.oType, typeDef, this.currentPackageName, strandIndex);
            } else {
                loadExternalOrLocalType(this.mv, typeDefRef);
            }
            this.mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE);
            this.mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", String.format("(L%s;)V", OBJECT_TYPE), false);
            this.storeToVar(objectNewIns.lhsOp.variableDcl);
        }

        static void generateFPLoadIns(BIRFPLoad inst) {
            this.mv.visitTypeInsn(NEW, FUNCTION_POINTER);
            this.mv.visitInsn(DUP);

            String lambdaName = inst.name.value + "$lambda" + lambdaIndex.toString() + "$";
            lambdaIndex += 1;
            String pkgName = getPackageName(inst.pkgID.org, inst.pkgID.name);
            String lookupKey = pkgName + inst.name.value;
            String methodClass = lookupFullQualifiedClassName(lookupKey);

            BType returnType = inst.lhsOp.type;
            if !(returnType.tag == TypeTags.INVOKABLE) {
                BLangCompilerException err = new BLangCompilerException("Expected BInvokableType, found " + String.format("%s", returnType));
                throw err;
            }

            for (T v : inst.closureMaps) {
                if (v instanceof BIRVarRef) {
                    this.loadVar(v.variableDcl);
                }
            }

            this.mv.visitInvokeDynamicInsn(currentClass, lambdaName, inst.closureMaps.size());
            loadType(this.mv, returnType);
            this.mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_POINTER, "<init>",
                    String.format("(L%s;L%s;)V", FUNCTION, BTYPE), false);

            // Set annotations if available.
            this.mv.visitInsn(DUP);
            String pkgClassName = pkgName.equals(".") || pkgName.equals("") ? MODULE_INIT_CLASS_NAME :
                    lookupGlobalVarClassName(pkgName + ANNOTATION_MAP_NAME);
            this.mv.visitFieldInsn(GETSTATIC, pkgClassName, ANNOTATION_MAP_NAME, String.format("L%s;", MAP_VALUE));
            this.mv.visitLdcInsn(inst.name.value);
            this.mv.visitMethodInsn(INVOKESTATIC, String.format("%s", ANNOTATION_UTILS), "processFPValueAnnotations",
                    String.format("(L%s;L%s;L%s;)V", FUNCTION_POINTER, MAP_VALUE, STRING_VALUE), false);

            this.storeToVar(inst.lhsOp.variableDcl);
            lambdas.add(lambdaName, inst);
        }

        static void generateNewXMLElementIns(BIRNewXMLElement newXMLElement) {
            this.loadVar(newXMLElement.startTagOp.variableDcl);
            this.mv.visitTypeInsn(CHECKCAST, XML_QNAME);
            this.loadVar(newXMLElement.endTagOp.variableDcl);
            this.mv.visitTypeInsn(CHECKCAST, XML_QNAME);
            this.loadVar(newXMLElement.defaultNsURIOp.variableDcl);
            this.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLElement",
                    String.format("(L%s;L%s;L%s;)L%s;", XML_QNAME, XML_QNAME, STRING_VALUE, XML_VALUE), false);
            this.storeToVar(newXMLElement.lhsOp.variableDcl);
        }

        static void generateNewXMLQNameIns(BIRNewXMLQName newXMLQName) {
            this.mv.visitTypeInsn(NEW, XML_QNAME);
            this.mv.visitInsn(DUP);
            this.loadVar(newXMLQName.localnameOp.variableDcl);
            this.loadVar(newXMLQName.nsURIOp.variableDcl);
            this.loadVar(newXMLQName.prefixOp.variableDcl);
            this.mv.visitMethodInsn(INVOKESPECIAL, XML_QNAME, "<init>",
                    String.format("(L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE, STRING_VALUE), false);
            this.storeToVar(newXMLQName.lhsOp.variableDcl);
        }

        static void generateNewStringXMLQNameIns(BIRNewStringXMLQName newStringXMLQName) {
            this.mv.visitTypeInsn(NEW, XML_QNAME);
            this.mv.visitInsn(DUP);
            this.loadVar(newStringXMLQName.stringQNameOp.variableDcl);
            this.mv.visitMethodInsn(INVOKESPECIAL, XML_QNAME, "<init>",
                    String.format("(L%s;)V", STRING_VALUE), false);
            this.storeToVar(newStringXMLQName.lhsOp.variableDcl);
        }

        static void generateNewXMLTextIns(BIRNewXMLText newXMLText) {
            this.loadVar(newXMLText.textOp.variableDcl);
            this.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLText",
                    String.format("(L%s;)L%s;", STRING_VALUE, XML_VALUE), false);
            this.storeToVar(newXMLText.lhsOp.variableDcl);
        }

        static void generateNewXMLCommentIns(BIRNewXMLComment newXMLComment) {
            this.loadVar(newXMLComment.textOp.variableDcl);
            this.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLComment",
                    String.format("(L%s;)L%s;", STRING_VALUE, XML_VALUE), false);
            this.storeToVar(newXMLComment.lhsOp.variableDcl);
        }

        static void generateNewXMLProcIns(BIRNewXMLPI newXMLPI) {
            this.loadVar(newXMLPI.targetOp.variableDcl);
            this.loadVar(newXMLPI.dataOp.variableDcl);
            this.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLProcessingInstruction",
                    String.format("(L%s;L%s;)L%s;", STRING_VALUE, STRING_VALUE, XML_VALUE), false);
            this.storeToVar(newXMLPI.lhsOp.variableDcl);
        }

        static void generateXMLStoreIns(BIRXMLAccess xmlStoreIns) {
            this.loadVar(xmlStoreIns.lhsOp.variableDcl);
            this.loadVar(xmlStoreIns.rhsOp.variableDcl);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "addChildren", String.format("(L%s;)V", XML_VALUE),
                    false);
        }

        static void generateXMLLoadAllIns(BIRXMLAccess xmlLoadAllIns) {
            this.loadVar(xmlLoadAllIns.rhsOp.variableDcl);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "children", String.format("()L%s;", XML_VALUE),
                    false);
            this.storeToVar(xmlLoadAllIns.lhsOp.variableDcl);
        }

        static void generateXMLAttrLoadIns(BIRFieldAccess xmlAttrStoreIns) {
            // visit xml_ref
            this.loadVar(xmlAttrStoreIns.rhsOp.variableDcl);

            // visit attribute name expr
            this.loadVar(xmlAttrStoreIns.keyOp.variableDcl);
            this.mv.visitTypeInsn(CHECKCAST, XML_QNAME);

            // invoke getAttribute() method
            this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "getAttribute",
                    String.format("(L%s;)L%s;", BXML_QNAME, STRING_VALUE), false);

            // store in the target reg
            BType targetType = xmlAttrStoreIns.lhsOp.variableDcl.type;
            this.storeToVar(xmlAttrStoreIns.lhsOp.variableDcl);
        }

        static void generateXMLAttrStoreIns(BIRFieldAccess xmlAttrStoreIns) {
            // visit xml_ref
            this.loadVar(xmlAttrStoreIns.lhsOp.variableDcl);

            // visit attribute name expr
            this.loadVar(xmlAttrStoreIns.keyOp.variableDcl);
            this.mv.visitTypeInsn(CHECKCAST, XML_QNAME);

            // visit attribute value expr
            this.loadVar(xmlAttrStoreIns.rhsOp.variableDcl);

            // invoke setAttribute() method
            this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "setAttribute",
                    String.format("(L%s;L%s;)V", BXML_QNAME, STRING_VALUE), false);
        }

        static void generateXMLLoadIns(BIRFieldAccess xmlLoadIns) {
            // visit xml_ref
            this.loadVar(xmlLoadIns.rhsOp.variableDcl);

            // visit element name/index expr
            this.loadVar(xmlLoadIns.keyOp.variableDcl);

            if (xmlLoadIns.keyOp.variableDcl.type.tag == TypeTags.STRING) {
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

        static void generateTypeofIns(BIRUnaryOp unaryOp) {
            this.loadVar(unaryOp.rhsOp.variableDcl);
            addBoxInsn(this.mv, unaryOp.rhsOp.variableDcl.type);
            this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "getTypedesc",
                    String.format("(L%s;)L%s;", OBJECT, TYPEDESC_VALUE), false);
            this.storeToVar(unaryOp.lhsOp.variableDcl);
        }

        static void generateNotIns(BIRUnaryOp unaryOp) {
            this.loadVar(unaryOp.rhsOp.variableDcl);

            Label label1 = new;
            Label label2 = new;

            this.mv.visitJumpInsn(IFNE, label1);
            this.mv.visitInsn(ICONST_1);
            this.mv.visitJumpInsn(GOTO, label2);
            this.mv.visitLabel(label1);
            this.mv.visitInsn(ICONST_0);
            this.mv.visitLabel(label2);

            this.storeToVar(unaryOp.lhsOp.variableDcl);
        }

        static void generateNegateIns(BIRUnaryOp unaryOp) {
            this.loadVar(unaryOp.rhsOp.variableDcl);

            BType btype = unaryOp.rhsOp.variableDcl.type;
            if (btype.tag == TypeTags.INT) {
                this.mv.visitInsn(LNEG);
            } else if (btype.tag == TypeTags.BYTE) {
                this.mv.visitInsn(INEG);
            } else if (btype.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DNEG);
            } else if (btype.tag == TypeTags.DECIMAL) {
                this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "negate",
                        String.format("()L%s;", DECIMAL_VALUE), false);
            } else {
                BLangCompilerException err = new BLangCompilerException(String.format("Negation is not supported for type: %s", btype));
                throw err;
            }

            this.storeToVar(unaryOp.lhsOp.variableDcl);
        }

        static void generateNewTypedescIns(BIRNewTypeDesc newTypeDesc) {
            this.mv.visitTypeInsn(NEW, TYPEDESC_VALUE);
            this.mv.visitInsn(DUP);
            loadType(this.mv, newTypeDesc.type);
            this.mv.visitMethodInsn(INVOKESPECIAL, TYPEDESC_VALUE, "<init>",
                    String.format("(L%s;)V", BTYPE), false);
            this.storeToVar(newTypeDesc.lhsOp.variableDcl);
        }

        private static void loadVar(BIRVariableDcl varDcl) {
            generateVarLoad(this.mv, varDcl, this.currentPackageName, this.getJVMIndexOfVarRef(varDcl));
        }

        private static void storeToVar(BIRVariableDcl varDcl) {
            generateVarStore(this.mv, varDcl, this.currentPackageName, this.getJVMIndexOfVarRef(varDcl));
        }

        void __init(MethodVisitor mv, BalToJVMIndexMap indexMap, BIRPackage moduleId) {
            this.mv = mv;
            this.indexMap = indexMap;
            this.currentPackage = moduleId;
            this.currentPackageName = getPackageName(moduleId.org.value, moduleId.name.value);
        }

        void generateConstantLoadIns(ConstantLoad loadIns, boolean useBString) {
            BType bType = loadIns.type;

            if (bType.tag == TypeTags.INT || bType.tag == TypeTags.BYTE) {
                Object val = loadIns.value;
                this.mv.visitLdcInsn(val);
            } else if (bType.tag == TypeTags.FLOAT) {
                Object val = loadIns.value;
                this.mv.visitLdcInsn(val);
            } else if (bType.tag == TypeTags.STRING) {
                String val = (String) loadIns.value;
                if (useBString) {
                    List<Integer> highSurrogates = listHighSurrogates(val);

                    this.mv.visitTypeInsn(NEW, NON_BMP_STRING_VALUE);
                    this.mv.visitInsn(DUP);
                    this.mv.visitLdcInsn(val);
                    this.mv.visitIntInsn(BIPUSH, highSurrogates.size());
                    this.mv.visitIntInsn(NEWARRAY, T_INT);

                    int i = 0;
                    for (Integer ch : highSurrogates) {
                        this.mv.visitInsn(DUP);
                        this.mv.visitIntInsn(BIPUSH, i);
                        this.mv.visitIntInsn(BIPUSH, ch);
                        i = i + 1;
                        this.mv.visitInsn(IASTORE);
                    }
                    this.mv.visitMethodInsn(INVOKESPECIAL, NON_BMP_STRING_VALUE, "<init>", String.format("(L%s;[I)V", STRING_VALUE), false);
                } else {
                    this.mv.visitLdcInsn(val);
                }
            } else if (bType.tag == TypeTags.DECIMAL) {
                Object val = loadIns.value;
                this.mv.visitTypeInsn(NEW, DECIMAL_VALUE);
                this.mv.visitInsn(DUP);
                this.mv.visitLdcInsn(val);
                this.mv.visitMethodInsn(INVOKESPECIAL, DECIMAL_VALUE, "<init>", String.format("(L%s;)V", STRING_VALUE), false);
            } else if (bType.tag == TypeTags.BOOLEAN) {
                Object val = loadIns.value;
                this.mv.visitLdcInsn(val);
            } else if (bType.tag == TypeTags.NIL) {
                this.mv.visitInsn(ACONST_NULL);
            } else {
                BLangCompilerException err = new BLangCompilerException("JVM generation is not supported for type : " + String.format("%s", bType));
                throw err;
            }

            this.storeToVar(loadIns.lhsOp.variableDcl);
        }
    }

//   static void listHighSurrogates(String str) returns List<Integer>  = external;
}