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

import io.ballerina.identifier.Utils;
import io.ballerina.tools.diagnostics.Location;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LabelGenerator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.NameHashComparator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.ScheduleFunctionInfo;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JType;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRAbstractInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BirScope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.Unifier;
import org.wso2.ballerinalang.util.Flags;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static io.ballerina.runtime.api.constants.RuntimeConstants.UNDERSCORE;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BALLERINA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_EXTENSION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BUILT_IN_PACKAGE_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CHANNEL_DETAILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ENCODED_DOT_CHARACTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FILE_NAME_PERIOD_SEPERATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.GET_VALUE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JAVA_PACKAGE_SEPERATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_TO_STRING_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAKE_CONCAT_WITH_CONSTANTS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_STRINGS_PER_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.START_OF_HEADING_WITH_SEMICOLON;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_BUILDER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_CONCAT_FACTORY;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STREAM_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TABLE_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TYPEDESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_XML;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_DESCRIPTOR_FOR_STRING_CONCAT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INITIAL_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_CHANNEL_DETAILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_ERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_WITH_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INT_TO_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_B_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_JOBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_REGEX_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_STREAM_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_TABLE_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.RETURN_XML_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.STRING_BUILDER_APPEND;
import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getMajorVersion;

/**
 * The common functions used in CodeGen.
 */
public class JvmCodeGenUtil {
    public static final Unifier UNIFIER = new Unifier();
    private static final Pattern JVM_RESERVED_CHAR_SET = Pattern.compile("[\\.:/<>]");
    public static final String SCOPE_PREFIX = "_SCOPE_";
    public static final NameHashComparator NAME_HASH_COMPARATOR = new NameHashComparator();

    static void visitInvokeDynamic(MethodVisitor mv, String currentClass, String lambdaName, int size) {
        String mapDesc = getMapsDesc(size);
        Handle handle = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory",
                                   "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;" +
                "Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;" +
                "Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);

        mv.visitInvokeDynamicInsn("apply", "(" + mapDesc + ")Ljava/util/function/Function;", handle,
                                  Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;"),
                                  new Handle(Opcodes.H_INVOKESTATIC, currentClass, lambdaName, "(" + mapDesc + "[" +
                        "Ljava/lang/Object;)Ljava/lang/Object;", false),
                                  Type.getType("([Ljava/lang/Object;" + ")Ljava/lang/Object;"));
    }

    private static String getMapsDesc(long count) {
        StringBuilder builder = new StringBuilder();
        for (long i = count; i > 0; i--) {
            builder.append("Lio/ballerina/runtime/internal/values/MapValue;");
        }
        return builder.toString();
    }

    public static void createFunctionPointer(MethodVisitor mv, String className, String lambdaName) {
        mv.visitTypeInsn(Opcodes.NEW, FUNCTION_POINTER);
        mv.visitInsn(Opcodes.DUP);
        visitInvokeDynamic(mv, className, lambdaName, 0);

        // load null here for type, since these are fps created for internal usage.
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(Opcodes.ICONST_0); // mark as not-concurrent ie: 'parent'
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, FUNCTION_POINTER, JVM_INIT_METHOD, FP_INIT, false);
    }

    public static String cleanupPathSeparators(String name) {
        name = cleanupBalExt(name);
        return name.replace(WINDOWS_PATH_SEPERATOR, JAVA_PACKAGE_SEPERATOR);
    }

    public static String rewriteVirtualCallTypeName(String value) {
        return Utils.encodeFunctionIdentifier(cleanupObjectTypeName(value));
    }

    private static String cleanupBalExt(String name) {
        if (name.endsWith(BAL_EXTENSION)) {
            return name.substring(0, name.length() - 4); // 4 = BAL_EXTENSION.length
        }
        return name;
    }

    public static String getFieldTypeSignature(BType bType) {
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            return "J";
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
            return GET_BSTRING;
        } else if (TypeTags.isXMLTypeTag(bType.tag)) {
            return GET_XML;
        } else {
            switch (bType.tag) {
                case TypeTags.BYTE:
                    return "I";
                case TypeTags.FLOAT:
                    return "D";
                case TypeTags.DECIMAL:
                    return GET_BDECIMAL;
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
                case TypeTags.MAP:
                case TypeTags.RECORD:
                    return GET_MAP_VALUE;
                case TypeTags.STREAM:
                    return GET_STREAM_VALUE;
                case TypeTags.TABLE:
                    return GET_TABLE_VALUE_IMPL;
                case TypeTags.ARRAY:
                case TypeTags.TUPLE:
                    return GET_ARRAY_VALUE;
                case TypeTags.ERROR:
                    return GET_ERROR_VALUE;
                case TypeTags.FUTURE:
                    return GET_FUTURE_VALUE;
                case TypeTags.OBJECT:
                    return GET_BOBJECT;
                case TypeTags.TYPEDESC:
                    return GET_TYPEDESC;
                case TypeTags.INVOKABLE:
                    return GET_FUNCTION_POINTER;
                case TypeTags.HANDLE:
                    return GET_HANDLE_VALUE;
                case JTypeTags.JTYPE:
                    return InteropMethodGen.getJTypeSignature((JType) bType);
                case TypeTags.TYPEREFDESC:
                    return getFieldTypeSignature(getReferredType(bType));
                case TypeTags.REGEXP:
                    return GET_REGEXP;
                default:
                    throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE + bType);
            }
        }
    }

    public static void generateDefaultConstructor(ClassWriter cw, String ownerClass) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, JVM_INIT_METHOD, "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, ownerClass, JVM_INIT_METHOD, "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    static void generateStrandMetadata(MethodVisitor mv, String moduleClass,
                                       PackageID packageID, AsyncDataCollector asyncDataCollector) {
        asyncDataCollector.getStrandMetadata().forEach(
                (varName, metaData) -> genStrandMetadataField(mv, moduleClass, packageID, varName, metaData));
    }

    public static void genStrandMetadataField(MethodVisitor mv, String moduleClass, PackageID packageID,
                                               String varName, ScheduleFunctionInfo metaData) {
        mv.visitTypeInsn(Opcodes.NEW, STRAND_METADATA);
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(Utils.decodeIdentifier(packageID.orgName.value));
        mv.visitLdcInsn(Utils.decodeIdentifier(packageID.name.value));
        mv.visitLdcInsn(getMajorVersion(packageID.version.value));
        if (metaData.typeName == null) {
            mv.visitInsn(Opcodes.ACONST_NULL);
        } else {
            mv.visitLdcInsn(metaData.typeName);
        }
        mv.visitLdcInsn(metaData.parentFunctionName);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, STRAND_METADATA,
                           JVM_INIT_METHOD, INIT_STRAND_METADATA, false);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, moduleClass, varName, GET_STRAND_METADATA);
    }

    static void visitStrandMetadataFields(ClassWriter cw, Map<String, ScheduleFunctionInfo> strandMetaDataMap) {
        strandMetaDataMap.keySet().forEach(varName -> visitStrandMetadataField(cw, varName));
    }

    private static void visitStrandMetadataField(ClassWriter cw, String varName) {
        FieldVisitor fv = cw.visitField(Opcodes.ACC_STATIC, varName,
                                        GET_STRAND_METADATA, null, null);
        fv.visitEnd();
    }

    public static String getStrandMetadataVarName(String parentFunction) {
        return STRAND_METADATA_VAR_PREFIX + parentFunction + "$";
    }

    public static boolean isExternFunc(BIRNode.BIRFunction func) {
        return (func.flags & Flags.NATIVE) == Flags.NATIVE;
    }

    public static String getPackageName(PackageID packageID) {
        return getPackageNameWithSeparator(packageID, "/");
    }

    public static String getSourcePackageName(PackageID packageID) {
        return getPackageNameWithSeparator(packageID, "/", true);
    }

    private static String getPackageNameWithSeparator(PackageID packageID, String separator) {
        return getPackageNameWithSeparator(packageID, separator, false);
    }

    private static String getPackageNameWithSeparator(PackageID packageID, String separator, boolean isSource) {
        String packageName = "";
        String orgName = Utils.encodeNonFunctionIdentifier(packageID.orgName.value);
        String moduleName;
        if (!packageID.isTestPkg || isSource) {
            moduleName = Utils.encodeNonFunctionIdentifier(packageID.name.value);
        } else {
            moduleName = Utils.encodeNonFunctionIdentifier(packageID.name.value) + Names.TEST_PACKAGE.value;
        }
        if (!moduleName.equals(ENCODED_DOT_CHARACTER)) {
            if (!packageID.version.value.equals("")) {
                packageName = getMajorVersion(packageID.version.value) + separator;
            }
            packageName = moduleName + separator + packageName;
        }

        if (!orgName.equalsIgnoreCase("$anon")) {
            packageName = orgName + separator + packageName;
        }
        return packageName;
    }

    public static String getModuleLevelClassName(PackageID packageID, String sourceFileName) {
        return getModuleLevelClassName(packageID, sourceFileName, "/");
    }

    static String getModuleLevelClassName(PackageID packageID, String sourceFileName, String separator) {
        String className = cleanupSourceFileName(sourceFileName);
        // handle source file path start with '/'.
        if (className.startsWith(JAVA_PACKAGE_SEPERATOR)) {
            className = className.substring(1);
        }
        return getPackageNameWithSeparator(packageID, separator) + className;
    }

    private static String cleanupSourceFileName(String name) {
        return name.replace(".", FILE_NAME_PERIOD_SEPERATOR);
    }

    public static String getMethodDesc(List<BType> paramTypes, BType retType) {
        return INITIAL_METHOD_DESC + populateMethodDesc(paramTypes) + generateReturnType(retType);
    }

    public static String getMethodDesc(List<BType> paramTypes, BType retType, BType attachedType) {
        return INITIAL_METHOD_DESC + getArgTypeSignature(attachedType) + populateMethodDesc(paramTypes) +
                generateReturnType(retType);
    }

    public static String populateMethodDesc(List<BType> paramTypes) {
        StringBuilder descBuilder = new StringBuilder();
        for (BType type : paramTypes) {
            descBuilder.append(getArgTypeSignature(type));
        }
        return descBuilder.toString();
    }

    public static String getArgTypeSignature(BType bType) {
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            return "J";
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
            return GET_BSTRING;
        } else if (TypeTags.isXMLTypeTag(bType.tag)) {
            return GET_XML;
        }

        switch (bType.tag) {
            case TypeTags.BYTE:
                return "I";
            case TypeTags.FLOAT:
                return "D";
            case TypeTags.DECIMAL:
                return GET_BDECIMAL;
            case TypeTags.BOOLEAN:
                return "Z";
            case TypeTags.NIL:
            case TypeTags.NEVER:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.INTERSECTION:
            case TypeTags.JSON:
            case TypeTags.FINITE:
            case TypeTags.ANY:
            case TypeTags.READONLY:
                return GET_OBJECT;
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                return GET_ARRAY_VALUE;
            case TypeTags.ERROR:
                return GET_ERROR_VALUE;
            case TypeTags.MAP:
            case TypeTags.RECORD:
                return GET_MAP_VALUE;
            case TypeTags.FUTURE:
                return GET_FUTURE_VALUE;
            case TypeTags.STREAM:
                return GET_STREAM_VALUE;
            case TypeTags.TABLE:
                return GET_TABLE_VALUE_IMPL;
            case TypeTags.INVOKABLE:
                return GET_FUNCTION_POINTER;
            case TypeTags.TYPEDESC:
                return GET_TYPEDESC;
            case TypeTags.OBJECT:
                return GET_BOBJECT;
            case TypeTags.HANDLE:
                return GET_HANDLE_VALUE;
            case TypeTags.TYPEREFDESC:
                return getArgTypeSignature(getReferredType(bType));
            case TypeTags.REGEXP:
                return GET_REGEXP;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                                                         bType);
        }
    }

    public static String generateReturnType(BType bType) {
        if (bType == null) {
            return RETURN_JOBJECT;
        }

        bType = JvmCodeGenUtil.UNIFIER.build(bType);
        if (bType == null || bType.tag == TypeTags.NIL || bType.tag == TypeTags.NEVER) {
            return RETURN_JOBJECT;
        } else if (TypeTags.isIntegerTypeTag(bType.tag)) {
            return ")J";
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
            return RETURN_B_STRING_VALUE;
        } else if (TypeTags.isXMLTypeTag(bType.tag)) {
            return RETURN_XML_VALUE;
        }

        switch (bType.tag) {
            case TypeTags.BYTE:
                return ")I";
            case TypeTags.FLOAT:
                return ")D";
            case TypeTags.DECIMAL:
                return RETURN_DECIMAL_VALUE;
            case TypeTags.BOOLEAN:
                return ")Z";
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                return RETURN_ARRAY_VALUE;
            case TypeTags.MAP:
            case TypeTags.RECORD:
                return RETURN_MAP_VALUE;
            case TypeTags.ERROR:
                return RETURN_ERROR_VALUE;
            case TypeTags.STREAM:
                return RETURN_STREAM_VALUE;
            case TypeTags.TABLE:
                return RETURN_TABLE_VALUE_IMPL;
            case TypeTags.FUTURE:
                return RETURN_FUTURE_VALUE;
            case TypeTags.TYPEDESC:
                return RETURN_TYPEDESC_VALUE;
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.INTERSECTION:
            case TypeTags.JSON:
            case TypeTags.FINITE:
            case TypeTags.READONLY:
                return RETURN_JOBJECT;
            case TypeTags.OBJECT:
                return RETURN_B_OBJECT;
            case TypeTags.INVOKABLE:
                return RETURN_FUNCTION_POINTER;
            case TypeTags.HANDLE:
                return RETURN_HANDLE_VALUE;
            case TypeTags.TYPEREFDESC:
                return generateReturnType(getReferredType(bType));
            case TypeTags.REGEXP:
                return RETURN_REGEX_VALUE;
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE + bType);
        }
    }

    static String cleanupObjectTypeName(String typeName) {
        int index = typeName.indexOf(".");
        if (index > 0 && index != typeName.length() - 1) { // Resource method name can contain . at the end
            return typeName.substring(index + 1);
        } else {
            return typeName;
        }
    }

    public static void loadChannelDetails(MethodVisitor mv, List<BIRNode.ChannelDetails> channels,
                                          int invocationVarIndex) {
        mv.visitIntInsn(BIPUSH, channels.size());
        mv.visitTypeInsn(ANEWARRAY, CHANNEL_DETAILS);
        int index = 0;
        for (BIRNode.ChannelDetails ch : channels) {
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, index);
            index += 1;

            mv.visitTypeInsn(NEW, CHANNEL_DETAILS);
            mv.visitInsn(DUP);
            mv.visitVarInsn(ILOAD, invocationVarIndex);
            mv.visitInvokeDynamicInsn(MAKE_CONCAT_WITH_CONSTANTS, INT_TO_STRING,
                    new Handle(H_INVOKESTATIC, STRING_CONCAT_FACTORY, MAKE_CONCAT_WITH_CONSTANTS,
                            HANDLE_DESCRIPTOR_FOR_STRING_CONCAT, false),
                    ch.name + START_OF_HEADING_WITH_SEMICOLON);

            if (ch.channelInSameStrand) {
                mv.visitInsn(ICONST_1);
            } else {
                mv.visitInsn(ICONST_0);
            }

            if (ch.send) {
                mv.visitInsn(ICONST_1);
            } else {
                mv.visitInsn(ICONST_0);
            }

            mv.visitMethodInsn(INVOKESPECIAL, CHANNEL_DETAILS, JVM_INIT_METHOD,
                    INIT_CHANNEL_DETAILS, false);
            mv.visitInsn(AASTORE);
        }
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

    public static boolean isBallerinaBuiltinModule(String orgName, String moduleName) {
        return orgName.equals("ballerina") && moduleName.equals("builtin");
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
        if (pos != null && pos.lineRange().startLine().line() != 0x80000000) {
            mv.visitLabel(label);
            // Adding +1 since 'pos' is 0-based and we want 1-based positions at run time
            mv.visitLineNumber(pos.lineRange().startLine().line() + 1, label);
        }
    }

    private static BirScope getLastScope(BIRAbstractInstruction instruction, String funcName, LabelGenerator labelGen,
                                         Set<BirScope> visitedScopesSet, BirScope lastScope, MethodVisitor mv) {
        BirScope scope = instruction.scope;
        if (scope != null && scope != lastScope) {
            lastScope = scope;
            Label scopeLabel = labelGen.getLabel(funcName + SCOPE_PREFIX + scope.id);
            mv.visitLabel(scopeLabel);
            storeLabelForParentScopes(scope, scopeLabel, labelGen, funcName, visitedScopesSet);
            visitedScopesSet.add(scope);
        }
        return lastScope;
    }

    private static void storeLabelForParentScopes(BirScope scope, Label scopeLabel, LabelGenerator labelGen,
                                                  String funcName, Set<BirScope> visitedScopesSet) {

        BirScope parent = scope.parent;
        if (parent != null && !visitedScopesSet.contains(parent)) {
            String labelName = funcName + SCOPE_PREFIX + parent.id;
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
            Label scopeLabel = labelGen.getLabel(funcName + SCOPE_PREFIX + scope.id);
            mv.visitLabel(scopeLabel);
            visitedScopesSet.add(scope);
        }
        return lastScope;
    }

    public static void genYieldCheck(MethodVisitor mv, LabelGenerator labelGen, BIRNode.BIRBasicBlock thenBB,
                                     String funcName, int localVarOffset, int yieldLocationVarIndex,
                                     Location terminatorPos, String fullyQualifiedFuncName,
                                     String yieldStatus, int yieldStatusVarIndex) {
        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS, "isYielded", "()Z", false);
        generateSetYieldedStatus(mv, labelGen, funcName, yieldLocationVarIndex, terminatorPos,
                fullyQualifiedFuncName, yieldStatus, yieldStatusVarIndex);

        // goto thenBB
        Label gotoLabel = labelGen.getLabel(funcName + thenBB.id.value);
        mv.visitJumpInsn(GOTO, gotoLabel);
    }

    protected static void generateSetYieldedStatus(MethodVisitor mv, LabelGenerator labelGen, String funcName,
                                                   int yieldLocationVarIndex, Location terminatorPos,
                                                   String fullyQualifiedFuncName, String yieldStatus,
                                                   int yieldStatusVarIndex) {
        Label yieldLocationLabel = new Label();
        mv.visitJumpInsn(IFEQ, yieldLocationLabel);

        StringBuilder yieldLocationData = new StringBuilder(fullyQualifiedFuncName);
        if (terminatorPos != null) {
            yieldLocationData.append("(").append(terminatorPos.lineRange().filePath()).append(":")
                    .append(terminatorPos.lineRange().startLine().line() + 1).append(")");
        }
        mv.visitLdcInsn(yieldLocationData.toString());
        mv.visitVarInsn(ASTORE, yieldLocationVarIndex);
        mv.visitLdcInsn(yieldStatus);
        mv.visitVarInsn(ASTORE, yieldStatusVarIndex);

        Label yieldLabel = labelGen.getLabel(funcName + "yield");
        mv.visitJumpInsn(GOTO, yieldLabel);
        mv.visitLabel(yieldLocationLabel);
    }

    public static PackageID cleanupPackageID(PackageID pkgID) {
        Name org = new Name(Utils.encodeNonFunctionIdentifier(pkgID.orgName.value));
        Name module = new Name(Utils.encodeNonFunctionIdentifier(pkgID.name.value));
        return new PackageID(org, module, pkgID.version);
    }

    public static boolean isBuiltInPackage(PackageID packageID) {
        packageID = cleanupPackageID(packageID);
        return BALLERINA.equals(packageID.orgName.value) && BUILT_IN_PACKAGE_NAME.equals(packageID.name.value);
    }

    public static boolean isSameModule(PackageID moduleId, PackageID importModule) {
        PackageID cleanedPkg = cleanupPackageID(importModule);
        if (!moduleId.orgName.value.equals(cleanedPkg.orgName.value)) {
            return false;
        } else if (!moduleId.name.value.equals(cleanedPkg.name.value)) {
            return false;
        } else {
            return getMajorVersion(moduleId.version.value).equals(getMajorVersion(cleanedPkg.version.value));
        }
    }

    public static String cleanupFunctionName(String functionName) {
        return StringUtils.containsAny(functionName, "\\.:/<>") ?
                "$" + JVM_RESERVED_CHAR_SET.matcher(functionName).replaceAll("_") : functionName;
    }

    public static boolean isSimpleBasicType(BType bType) {
        switch (bType.tag) {
            case TypeTags.BYTE:
            case TypeTags.FLOAT:
            case TypeTags.BOOLEAN:
            case TypeTags.DECIMAL:
            case TypeTags.NIL:
            case TypeTags.NEVER:
                return true;
            case TypeTags.TYPEREFDESC:
                return isSimpleBasicType(getReferredType(bType));
            default:
                return (TypeTags.isIntegerTypeTag(bType.tag)) || (TypeTags.isStringTypeTag(bType.tag));
        }
    }

    public static BType getReferredType(BType type) {
        BType constraint = type;
        if (type != null && type.tag == TypeTags.TYPEREFDESC) {
            constraint = getReferredType(((BTypeReferenceType) type).referredType);
        }
        return constraint;
    }

    public static void loadConstantValue(BType bType, Object constVal, MethodVisitor mv,
                                         JvmConstantsGen jvmConstantsGen) {

        int typeTag = getReferredType(bType).tag;
        if (TypeTags.isIntegerTypeTag(typeTag)) {
            long intValue = constVal instanceof Long ? (long) constVal : Long.parseLong(String.valueOf(constVal));
            mv.visitLdcInsn(intValue);
            return;
        } else if (TypeTags.isStringTypeTag(typeTag)) {
            String val = String.valueOf(constVal);
            int index = jvmConstantsGen.getBStringConstantVarIndex(val);
            String varName = B_STRING_VAR_PREFIX + index;
            String stringConstantsClass = getStringConstantsClass(index, jvmConstantsGen);
            mv.visitFieldInsn(GETSTATIC, stringConstantsClass, varName, GET_BSTRING);
            return;
        }

        switch (typeTag) {
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
                mv.visitLdcInsn(removeDecimalDiscriminator(String.valueOf(constVal)));
                mv.visitMethodInsn(INVOKESPECIAL, DECIMAL_VALUE, JVM_INIT_METHOD, INIT_WITH_STRING, false);
                break;
            case TypeTags.NIL:
            case TypeTags.NEVER:
                mv.visitInsn(ACONST_NULL);
                break;
            default:
                throw new BLangCompilerException("JVM generation is not supported for type : " + bType);
        }
    }

    private static String getStringConstantsClass(int varIndex, JvmConstantsGen jvmConstantsGen) {
        int classIndex = varIndex / MAX_STRINGS_PER_METHOD;
        return jvmConstantsGen.getStringConstantsClass() + UNDERSCORE + classIndex;
    }

    private static String removeDecimalDiscriminator(String value) {
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

    public static void createDefaultCase(MethodVisitor mv, Label defaultCaseLabel, int nameRegIndex,
                                         String errorMessage) {

        mv.visitLabel(defaultCaseLabel);
        mv.visitTypeInsn(NEW, ERROR_VALUE);
        mv.visitInsn(DUP);

        // Create error message
        mv.visitTypeInsn(NEW, STRING_BUILDER);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(errorMessage);
        mv.visitMethodInsn(INVOKESPECIAL, STRING_BUILDER, JVM_INIT_METHOD, INIT_WITH_STRING,
                false);
        mv.visitVarInsn(ALOAD, nameRegIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER, "append",
                STRING_BUILDER_APPEND, false);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER, JVM_TO_STRING_METHOD, GET_JSTRING
                , false);
        mv.visitMethodInsn(INVOKESTATIC, STRING_UTILS, "fromString", FROM_STRING, false);
        mv.visitMethodInsn(INVOKESPECIAL, ERROR_VALUE, JVM_INIT_METHOD, INIT_ERROR,
                false);
        mv.visitInsn(ATHROW);
    }

    public static void castToJavaString(MethodVisitor mv, int fieldNameRegIndex, int strKeyVarIndex) {
        mv.visitVarInsn(ALOAD, fieldNameRegIndex);
        mv.visitTypeInsn(CHECKCAST, B_STRING_VALUE);
        mv.visitMethodInsn(INVOKEINTERFACE, B_STRING_VALUE, GET_VALUE_METHOD,
                GET_JSTRING, true);
        mv.visitVarInsn(ASTORE, strKeyVarIndex);
    }

    private JvmCodeGenUtil() {
    }

    public static String getRefTypeConstantName(BTypeReferenceType type) {
        return JvmConstants.TYPEREF_TYPE_VAR_PREFIX + Utils.encodeNonFunctionIdentifier(type.tsymbol.name.value);
    }
}
