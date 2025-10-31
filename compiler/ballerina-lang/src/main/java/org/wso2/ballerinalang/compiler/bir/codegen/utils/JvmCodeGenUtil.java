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

package org.wso2.ballerinalang.compiler.bir.codegen.utils;

import io.ballerina.identifier.Utils;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.types.Env;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmErrorGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTerminatorGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LabelGenerator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadBirBasicBlock;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.NameHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.model.JType;
import org.wso2.ballerinalang.compiler.bir.codegen.model.JTypeTags;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRAbstractInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BirScope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.Unifier;
import org.wso2.ballerinalang.util.Flags;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_FUNC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.APPLY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_EXTENSION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_VALUE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JAVA_PACKAGE_SEPERATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JAVA_RUNTIME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_TO_STRING_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LAMBDA_META_FACTORY_HANDLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_START_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OVERFLOW_LINE_NUMBER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_WORKER_CHANNEL_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_BUILDER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WINDOWS_PATH_SEPERATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.FP_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.FROM_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_BDECIMAL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_BOBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_BSTRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_JSTRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_REGEXP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_RUNTIME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRAND_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STREAM_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TABLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPEDESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_WORKER_CHANNEL_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_XML;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INITIAL_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_CLASS_CONSTRUCTOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_ERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_OBJECT_ARRAY_RETURN_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_WITH_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.PASS_OBJECT_ARRAY_RETURN_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.PASS_OBJECT_RETURN_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_JOBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_REGEX_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_STREAM_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_TABLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_XML_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.STRING_BUILDER_APPEND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.optimizer.LargeMethodOptimizer.SPLIT_METHOD;
import static org.wso2.ballerinalang.compiler.util.Constants.RECORD_DELIMITER;

/**
 * The common functions used in CodeGen.
 */
public final class JvmCodeGenUtil {

    public static final Unifier UNIFIER = new Unifier();
    private static final Pattern JVM_RESERVED_CHAR_SET = Pattern.compile("[.:/<>]");
    public static final String SCOPE_PREFIX = "_SCOPE_";
    public static final NameHashComparator NAME_HASH_COMPARATOR = new NameHashComparator();
    private static final Type PASS_OBJECT_RETURN_OBJECT_TYPE = Type.getType(PASS_OBJECT_RETURN_OBJECT);
    private static final Type PASS_OBJECT_ARRAY_RETURN_OBJECT_TYPE = Type.getType(PASS_OBJECT_ARRAY_RETURN_OBJECT);


    private JvmCodeGenUtil() {
    }

    public static void visitInvokeDynamic(MethodVisitor mv, String currentClass, String lambdaName, int size) {
        String mapDesc = getMapsDesc(size);
        mv.visitInvokeDynamicInsn(APPLY, "(" + mapDesc + RETURN_FUNCTION, LAMBDA_META_FACTORY_HANDLE,
                PASS_OBJECT_RETURN_OBJECT_TYPE, new Handle(H_INVOKESTATIC, currentClass, lambdaName, "(" + mapDesc + 
                        INIT_OBJECT_ARRAY_RETURN_OBJECT, false), PASS_OBJECT_ARRAY_RETURN_OBJECT_TYPE);
    }

    private static String getMapsDesc(long count) {
        StringBuilder builder = new StringBuilder();
        for (long i = count; i > 0; i--) {
            builder.append(GET_MAP_VALUE);
        }
        return builder.toString();
    }

    public static void createFunctionPointer(MethodVisitor mv, String className, String lambdaName) {
        mv.visitTypeInsn(Opcodes.NEW, FUNCTION_POINTER);
        mv.visitInsn(Opcodes.DUP);
        visitInvokeDynamic(mv, className, lambdaName, 0);

        // load null here for type, since these are fps created for internal usage.
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitLdcInsn(lambdaName);
        mv.visitInsn(Opcodes.ICONST_0); // mark as not-concurrent ie: 'parent'
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, FUNCTION_POINTER, JVM_INIT_METHOD, FP_INIT, false);
    }

    public static String cleanupPathSeparators(String name) {
        name = cleanupBalExt(name);
        return name.replace(WINDOWS_PATH_SEPERATOR, JAVA_PACKAGE_SEPERATOR);
    }

    public static String rewriteVirtualCallTypeName(String value, BType objectType) {
        objectType = getImpliedType(objectType);
        String typeName = objectType.tsymbol.name.value;
        Name originalName = objectType.tsymbol.originalName;
        if (value.startsWith(typeName)) {
            // The call name will be in the format of`objectTypeName.funcName` for attached functions of imported
            // modules. Therefore, We need to remove the type name.
            value = value.replace(typeName + ".", "").trim();
        } else if (originalName != null && value.startsWith(originalName.value)) {
            // The call name will be in the format of`objectTypeOriginalName.funcName` for attached functions of
            // object definitions. Therefore, We need to remove it.
            value = value.replace(originalName + ".", "").trim();
        }
        return Utils.encodeFunctionIdentifier(value);
    }

    public static boolean isModuleInitializerMethod(String methodName) {
        return methodName.equals(MODULE_INIT_METHOD) || methodName.equals(MODULE_START_METHOD);
    }

    private static String cleanupBalExt(String name) {
        if (name.endsWith(BAL_EXTENSION)) {
            return name.substring(0, name.length() - 4); // 4 = BAL_EXTENSION.length
        }
        return name;
    }

    public static String getFieldTypeSignature(BType bType) {
        bType = getImpliedType(bType);
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            return "J";
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
            return GET_BSTRING;
        } else if (TypeTags.isXMLTypeTag(bType.tag)) {
            return GET_XML;
        } else {
            return switch (bType.tag) {
                case TypeTags.BYTE -> "I";
                case TypeTags.FLOAT -> "D";
                case TypeTags.DECIMAL -> GET_BDECIMAL;
                case TypeTags.BOOLEAN -> "Z";
                case TypeTags.NIL, TypeTags.NEVER, TypeTags.ANY, TypeTags.ANYDATA, TypeTags.UNION, TypeTags.JSON,
                     TypeTags.FINITE, TypeTags.READONLY -> GET_OBJECT;
                case TypeTags.MAP, TypeTags.RECORD -> GET_MAP_VALUE;
                case TypeTags.STREAM -> GET_STREAM_VALUE;
                case TypeTags.TABLE -> GET_TABLE_VALUE;
                case TypeTags.ARRAY, TypeTags.TUPLE -> GET_ARRAY_VALUE;
                case TypeTags.ERROR -> GET_ERROR_VALUE;
                case TypeTags.FUTURE -> GET_FUTURE_VALUE;
                case TypeTags.OBJECT -> GET_BOBJECT;
                case TypeTags.TYPEDESC -> GET_TYPEDESC;
                case TypeTags.INVOKABLE -> GET_FUNCTION_POINTER;
                case TypeTags.HANDLE -> GET_HANDLE_VALUE;
                case JTypeTags.JTYPE -> InteropMethodGen.getJTypeSignature((JType) bType);
                case TypeTags.REGEXP -> GET_REGEXP;
                default -> throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE + bType);
            };
        }
    }

    public static void generateDefaultConstructor(ClassWriter cw, String ownerClass) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, JVM_INIT_METHOD, VOID_METHOD_DESC, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, ownerClass, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public static void generateInitClassConstructor(ClassWriter cw, String ownerClass) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, JVM_INIT_METHOD, INIT_CLASS_CONSTRUCTOR, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, ownerClass, JVM_INIT_METHOD, INIT_CLASS_CONSTRUCTOR, false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public static boolean isExternFunc(BIRNode.BIRFunction func) {
        return (func.flags & Flags.NATIVE) == Flags.NATIVE;
    }

    public static void generateExitRuntime(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC , JAVA_RUNTIME, "getRuntime", GET_RUNTIME, false);
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKEVIRTUAL , JAVA_RUNTIME, "exit", "(I)V", false);
    }

    public static String getMethodDesc(Env typeEnv, List<BType> paramTypes, BType retType) {
        return INITIAL_METHOD_DESC + getMethodDescParams(paramTypes) + generateReturnType(retType, typeEnv);
    }

    public static String getMethodDesc(Env typeEnv, List<BType> paramTypes, BType retType, BType attachedType) {
        return INITIAL_METHOD_DESC + getArgTypeSignature(attachedType) + getMethodDescParams(paramTypes) +
                generateReturnType(retType, typeEnv);
    }

    public static String getMethodDesc(Env typeEnv, List<BType> paramTypes, BType retType,
                                       String attachedTypeClassName) {
        return INITIAL_METHOD_DESC + "L" + attachedTypeClassName + ";" + getMethodDescParams(paramTypes) +
                generateReturnType(retType, typeEnv);
    }

    public static String getMethodDescParams(List<BType> paramTypes) {
        StringBuilder descBuilder = new StringBuilder();
        for (BType type : paramTypes) {
            descBuilder.append(getArgTypeSignature(type));
        }
        return descBuilder.toString();
    }

    public static String getArgTypeSignature(BType bType) {
        bType = JvmCodeGenUtil.getImpliedType(bType);
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            return "J";
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
            return GET_BSTRING;
        } else if (TypeTags.isXMLTypeTag(bType.tag)) {
            return GET_XML;
        }

        return switch (bType.tag) {
            case TypeTags.BYTE -> "I";
            case TypeTags.FLOAT -> "D";
            case TypeTags.DECIMAL -> GET_BDECIMAL;
            case TypeTags.BOOLEAN -> "Z";
            case TypeTags.NIL, TypeTags.NEVER, TypeTags.ANYDATA, TypeTags.UNION, TypeTags.JSON, TypeTags.FINITE,
                 TypeTags.ANY, TypeTags.READONLY -> GET_OBJECT;
            case TypeTags.ARRAY, TypeTags.TUPLE -> GET_ARRAY_VALUE;
            case TypeTags.ERROR -> GET_ERROR_VALUE;
            case TypeTags.MAP, TypeTags.RECORD -> GET_MAP_VALUE;
            case TypeTags.FUTURE -> GET_FUTURE_VALUE;
            case TypeTags.STREAM -> GET_STREAM_VALUE;
            case TypeTags.TABLE -> GET_TABLE_VALUE;
            case TypeTags.INVOKABLE -> GET_FUNCTION_POINTER;
            case TypeTags.TYPEDESC -> GET_TYPEDESC;
            case TypeTags.OBJECT -> GET_BOBJECT;
            case TypeTags.HANDLE -> GET_HANDLE_VALUE;
            case TypeTags.REGEXP -> GET_REGEXP;
            default -> throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE + bType);
        };
    }

    public static String generateReturnType(BType bType, Env typeEnv) {
        bType = JvmCodeGenUtil.getImpliedType(bType);
        if (bType == null) {
            return RETURN_JOBJECT;
        }

        bType = JvmCodeGenUtil.UNIFIER.build(typeEnv, bType);
        if (bType == null || bType.tag == TypeTags.NIL || bType.tag == TypeTags.NEVER) {
            return RETURN_JOBJECT;
        } else if (TypeTags.isIntegerTypeTag(bType.tag)) {
            return ")J";
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
            return RETURN_B_STRING_VALUE;
        } else if (TypeTags.isXMLTypeTag(bType.tag)) {
            return RETURN_XML_VALUE;
        }

        return switch (bType.tag) {
            case TypeTags.BYTE -> ")I";
            case TypeTags.FLOAT -> ")D";
            case TypeTags.DECIMAL -> RETURN_DECIMAL_VALUE;
            case TypeTags.BOOLEAN -> ")Z";
            case TypeTags.ARRAY, TypeTags.TUPLE -> RETURN_ARRAY_VALUE;
            case TypeTags.MAP, TypeTags.RECORD -> RETURN_MAP_VALUE;
            case TypeTags.ERROR -> RETURN_ERROR_VALUE;
            case TypeTags.STREAM -> RETURN_STREAM_VALUE;
            case TypeTags.TABLE -> RETURN_TABLE_VALUE;
            case TypeTags.FUTURE -> RETURN_FUTURE_VALUE;
            case TypeTags.TYPEDESC -> RETURN_TYPEDESC_VALUE;
            case TypeTags.ANY, TypeTags.ANYDATA, TypeTags.UNION, TypeTags.INTERSECTION, TypeTags.JSON,
                 TypeTags.FINITE, TypeTags.READONLY -> RETURN_JOBJECT;
            case TypeTags.OBJECT -> RETURN_B_OBJECT;
            case TypeTags.INVOKABLE -> RETURN_FUNCTION_POINTER;
            case TypeTags.HANDLE -> RETURN_HANDLE_VALUE;
            case TypeTags.REGEXP -> RETURN_REGEX_VALUE;
            default -> throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE + bType);
        };
    }

    public static String toNameString(BType t) {
        BTypeSymbol typeSymbol = t.tsymbol;
        if ((typeSymbol.kind == SymbolKind.RECORD || typeSymbol.kind == SymbolKind.OBJECT) &&
                ((BStructureTypeSymbol) typeSymbol).typeDefinitionSymbol != null) {
            return Utils.encodeNonFunctionIdentifier(((BStructureTypeSymbol) typeSymbol)
                    .typeDefinitionSymbol.name.value);
        }
        return Utils.encodeNonFunctionIdentifier(typeSymbol.name.value);
    }

    public static BirScope getLastScopeFromBBInsGen(MethodVisitor mv, LabelGenerator labelGen,
                                                    JvmInstructionGen instGen, int localVarOffset,
                                                    String funcName, BIRNode.BIRBasicBlock bb,
                                                    Set<BirScope> visitedScopesSet, BirScope lastScope) {

        int insCount = bb.instructions.size();
        for (int i = 0; i < insCount; i++) {
            Label insLabel = labelGen.getLabel(funcName + bb.id.value + "ins" + i);
            mv.visitLabel(insLabel);
            BIRAbstractInstruction inst = bb.instructions.get(i);
            if (inst != null) {
                generateDiagnosticPos(inst.pos, mv);
                instGen.generateInstructions(localVarOffset, inst);
                lastScope = getLastScope(inst, funcName, labelGen, visitedScopesSet, lastScope, mv);
            }
        }

        return lastScope;
    }

    public static void generateDiagnosticPos(Location pos, MethodVisitor mv) {
        Label label = new Label();
        if (pos != null && pos.lineRange().startLine().line() != OVERFLOW_LINE_NUMBER) {
            mv.visitLabel(label);
            // Adding +1 since 'pos' is 0-based and we want 1-based positions at run time
            mv.visitLineNumber(pos.lineRange().startLine().line() + 1, label);
        }
    }

    public static void lazyLoadAnnotations(MethodVisitor mv, LazyLoadBirBasicBlock lazyBB,
                                           JvmPackageGen jvmPackageGen, JvmCastGen jvmCastGen,
                                           JvmConstantsGen jvmConstantsGen, JvmTypeGen jvmTypeGen,
                                           AsyncDataCollector asyncDataCollector) {
        BIRTerminator.Call call = lazyBB.call;
        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        PackageID packageID = jvmPackageGen.currentModule.packageID;
        JvmInstructionGen instructionGen =
                new JvmInstructionGen(mv, indexMap, packageID, jvmPackageGen, jvmTypeGen,
                        jvmCastGen, jvmConstantsGen, asyncDataCollector);
        if (call != null) {
            JvmErrorGen errorGen = new JvmErrorGen(mv, indexMap, instructionGen);
            LabelGenerator labelGen = new LabelGenerator();
            JvmTerminatorGen termGen = new JvmTerminatorGen(mv, indexMap, labelGen, errorGen, packageID,
                    instructionGen, jvmPackageGen, jvmTypeGen, jvmCastGen, asyncDataCollector);
            JvmCodeGenUtil.generateDiagnosticPos(call.pos, mv);
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ASTORE, 1);
            termGen.genCall(call, call.calleePkg, 1);
            termGen.storeReturnFromCallIns(call.lhsOp != null ? call.lhsOp.variableDcl : null);
        }
        for (BIRNonTerminator instruction : lazyBB.instructions) {
            JvmCodeGenUtil.generateDiagnosticPos(instruction.pos, mv);
            instructionGen.generateInstructions(0, instruction);
        }
    }

    private static BirScope getLastScope(BIRAbstractInstruction instruction, String funcName, LabelGenerator labelGen,
                                         Set<BirScope> visitedScopesSet, BirScope lastScope, MethodVisitor mv) {
        BirScope scope = instruction.scope;
        if (scope != null && scope != lastScope) {
            lastScope = scope;
            Label scopeLabel = labelGen.getLabel(funcName + SCOPE_PREFIX + scope.id());
            mv.visitLabel(scopeLabel);
            storeLabelForParentScopes(scope, scopeLabel, labelGen, funcName, visitedScopesSet);
            visitedScopesSet.add(scope);
        }
        return lastScope;
    }

    private static void storeLabelForParentScopes(BirScope scope, Label scopeLabel, LabelGenerator labelGen,
                                                  String funcName, Set<BirScope> visitedScopesSet) {

        BirScope parent = scope.parent();
        if (parent != null && !visitedScopesSet.contains(parent)) {
            String labelName = funcName + SCOPE_PREFIX + parent.id();
            labelGen.putLabel(labelName, scopeLabel);
            visitedScopesSet.add(parent);

            storeLabelForParentScopes(parent, scopeLabel, labelGen, funcName, visitedScopesSet);
        }
    }

    public static BirScope getLastScopeFromTerminator(MethodVisitor mv, BIRNode.BIRBasicBlock bb, String funcName,
                                                      LabelGenerator labelGen, BirScope lastScope,
                                                      Set<BirScope> visitedScopesSet) {
        BirScope scope = bb.terminator.scope;
        if (scope != null && scope != lastScope) {
            lastScope = scope;
            Label scopeLabel = labelGen.getLabel(funcName + SCOPE_PREFIX + scope.id());
            mv.visitLabel(scopeLabel);
            visitedScopesSet.add(scope);
        }
        return lastScope;
    }

    public static void genGotoThenBB(MethodVisitor mv, BIRNode.BIRBasicBlock thenBB, LabelGenerator labelGen,
                                     BIRTerminator terminator, String funcName) {
        if (thenBB != null) {
            Label gotoLabel = labelGen.getLabel(funcName + terminator.thenBB.id.value);
            mv.visitJumpInsn(GOTO, gotoLabel);
        }
    }

    public static String cleanupFunctionName(String functionName) {
        return StringUtils.containsAny(functionName, "\\.:/<>") ?
                "$" + JVM_RESERVED_CHAR_SET.matcher(functionName).replaceAll("_") : functionName;
    }

    public static boolean isSimpleBasicType(BType bType) {
        bType = JvmCodeGenUtil.getImpliedType(bType);
        return switch (bType.tag) {
            case TypeTags.BYTE, TypeTags.FLOAT, TypeTags.BOOLEAN, TypeTags.DECIMAL, TypeTags.NIL, TypeTags.NEVER ->
                    true;
            default -> (TypeTags.isIntegerTypeTag(bType.tag)) || (TypeTags.isStringTypeTag(bType.tag));
        };
    }

    public static boolean needNoTypeGeneration(int bTypeTag) {
        return switch (bTypeTag) {
            case TypeTags.RECORD, TypeTags.ERROR, TypeTags.OBJECT, TypeTags.UNION, TypeTags.TUPLE -> false;
            default -> true;
        };
    }

    /**
     * Retrieve the referred type if a given type is a type reference type or
     * retrieve the effective type if the given type is an intersection type.
     *
     * @param type type to retrieve the implied type
     * @return the implied type if provided with a type reference type or an intersection type,
     * else returns the original type
     */
    public static BType getImpliedType(BType type) {
        if (type == null) {
            return null;
        }

        if (type.tag == TypeTags.TYPEREFDESC) {
            return getImpliedType(((BTypeReferenceType) type).referredType);
        }

        if (type.tag == TypeTags.INTERSECTION) {
            return getImpliedType(((BIntersectionType) type).effectiveType);
        }

        return type;
    }

    public static void loadConstantValue(BType bType, Object constVal, String varName, MethodVisitor mv,
                                         JvmConstantsGen jvmConstantsGen, String constantVarClassName,
                                         boolean isConstant) {
        int typeTag = getImpliedType(bType).tag;
        if (TypeTags.isIntegerTypeTag(typeTag)) {
            long intValue = constVal instanceof Long ? (long) constVal : Long.parseLong(String.valueOf(constVal));
            mv.visitLdcInsn(intValue);
            return;
        } else if (TypeTags.isStringTypeTag(typeTag)) {
            String val = String.valueOf(constVal);
            jvmConstantsGen.loadBStringConstant(mv, val, varName, constantVarClassName, isConstant);
            return;
        }
        switch (typeTag) {
            case TypeTags.BYTE -> {
                int byteValue = ((Number) constVal).intValue();
                mv.visitLdcInsn(byteValue);
            }
            case TypeTags.FLOAT -> {
                double doubleValue = constVal instanceof Double ? (double) constVal :
                        Double.parseDouble(String.valueOf(constVal));
                mv.visitLdcInsn(doubleValue);
            }
            case TypeTags.BOOLEAN -> {
                boolean booleanVal = constVal instanceof Boolean ? (boolean) constVal :
                        Boolean.parseBoolean(String.valueOf(constVal));
                mv.visitLdcInsn(booleanVal);
            }
            case TypeTags.DECIMAL -> {
                mv.visitTypeInsn(NEW, DECIMAL_VALUE);
                mv.visitInsn(DUP);
                mv.visitLdcInsn(removeDecimalDiscriminator(String.valueOf(constVal)));
                mv.visitMethodInsn(INVOKESPECIAL, DECIMAL_VALUE, JVM_INIT_METHOD, INIT_WITH_STRING, false);
            }
            case TypeTags.NIL, TypeTags.NEVER -> mv.visitInsn(ACONST_NULL);
            default -> throw new BLangCompilerException("JVM generation is not supported for type : " + bType);
        }
    }

    public static String removeDecimalDiscriminator(String value) {
        int length = value.length();
        if (length < 2) {
            return value;
        }
        char lastChar = value.charAt(length - 1);
        if (lastChar == 'd' || lastChar == 'D') {
            return value.substring(0, length - 1);
        }
        return value;
    }


    public static void createDefaultCaseReturnNull(MethodVisitor mv, Label defaultCaseLabel) {
        mv.visitLabel(defaultCaseLabel);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
    }

    public static void createDefaultCaseThrowError(MethodVisitor mv, Label defaultCaseLabel, int nameRegIndex,
                                                   String errorMessage) {
        mv.visitLabel(defaultCaseLabel);
        mv.visitTypeInsn(NEW, ERROR_VALUE);
        mv.visitInsn(DUP);

        // Create error message
        mv.visitTypeInsn(NEW, STRING_BUILDER);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(errorMessage);
        mv.visitMethodInsn(INVOKESPECIAL, STRING_BUILDER, JVM_INIT_METHOD, INIT_WITH_STRING, false);
        mv.visitVarInsn(ALOAD, nameRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER, "append", STRING_BUILDER_APPEND, false);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER, JVM_TO_STRING_METHOD, GET_JSTRING, false);
        mv.visitMethodInsn(INVOKESTATIC, STRING_UTILS, "fromString", FROM_STRING, false);
        mv.visitMethodInsn(INVOKESPECIAL, ERROR_VALUE, JVM_INIT_METHOD, INIT_ERROR, false);
        mv.visitInsn(ATHROW);
    }

    public static void castToJavaString(MethodVisitor mv, int fieldNameRegIndex, int strKeyVarIndex) {
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitTypeInsn(CHECKCAST, B_STRING_VALUE);
        mv.visitMethodInsn(INVOKEINTERFACE, B_STRING_VALUE, GET_VALUE_METHOD, GET_JSTRING, true);
        mv.visitVarInsn(ASTORE, strKeyVarIndex);
    }

    public static String getRefTypeConstantName(BTypeReferenceType type) {
        return Utils.encodeNonFunctionIdentifier(type.tsymbol.name.value);
    }

    public static void visitMaxStackForMethod(MethodVisitor mv, String funcName, String className) {
        try {
            mv.visitMaxs(0, 0);
        } catch (Throwable e) {
            throw new BLangCompilerException("error while generating method '" + Utils.decodeIdentifier(funcName) +
                    "' in class '" + Utils.decodeIdentifier(className) + "'", e);
        }
    }

    public static String getMethodSig(Class<?> returnType, Class<?>... parameterTypes) {

        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Class<?> type : parameterTypes) {
            sb.append(getSig(type));
        }
        sb.append(')');
        return sb.append(getSig(returnType)).toString();
    }

    public static String getSig(Class<?> c) {

        if (c.isPrimitive()) {
            if (int.class == c) {
                return "I";
            } else if (long.class == c) {
                return "J";
            } else if (boolean.class == c) {
                return "Z";
            } else if (byte.class == c) {
                return "B";
            } else if (short.class == c) {
                return "S";
            } else if (char.class == c) {
                return "C";
            } else if (float.class == c) {
                return "F";
            } else if (double.class == c) {
                return "D";
            } else {
                // This is void
                return "V";
            }
        } else if (void.class == c || Void.class == c) {
            return "V";
        } else {
            String className = c.getName().replace('.', '/');
            if (c.isArray()) {
                return className;
            } else {
                return 'L' + className + ';';
            }
        }
    }

    public static void loadStrand(MethodVisitor mv, int localVarOffset) {
        if (localVarOffset == -1) {
            mv.visitMethodInsn(INVOKESTATIC, SCHEDULER, GET_STRAND, GET_STRAND_METHOD, false);
        } else {
            mv.visitVarInsn(ALOAD, localVarOffset);
        }
    }

    public static void loadWorkerChannelMap(MethodVisitor mv, BIRNode.BIRFunction func, int channelMapVarIndex,
                                            int localVarOffset) {
        if (func.hasWorkers) {
            mv.visitVarInsn(ALOAD, channelMapVarIndex);
        } else {
            loadStrand(mv, localVarOffset);
            mv.visitFieldInsn(GETFIELD, STRAND_CLASS, STRAND_WORKER_CHANNEL_MAP, GET_WORKER_CHANNEL_MAP);
        }
    }

    public static String getVarStoreClass(String varClassPkgName, String varName) {
        return varClassPkgName + varName;
    }

    public static void genMethodReturn(MethodVisitor mv) {
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    public static boolean canSkipFromCallByFunctionName(String functionName) {
        if (functionName.charAt(0) != '$') {
            return false;
        }
        return functionName.startsWith(RECORD_DELIMITER)
                || functionName.startsWith(SPLIT_METHOD)
                || functionName.startsWith(ANNOTATION_FUNC);
    }
}
