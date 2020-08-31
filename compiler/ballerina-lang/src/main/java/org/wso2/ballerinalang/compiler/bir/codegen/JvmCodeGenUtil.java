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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LabelGenerator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.ScheduleFunctionInfo;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JType;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.ResolvedTypeBuilder;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.List;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_EXTENSION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BTYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.B_STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CHANNEL_DETAILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CONSTRUCTOR_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FILE_NAME_PERIOD_SEPERATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JAVA_PACKAGE_SEPERATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_METADATA_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STREAM_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TABLE_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WINDOWS_PATH_SEPERATOR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_VALUE;

/**
 * The common functions used in CodeGen.
 */
public class JvmCodeGenUtil {
    public static final ResolvedTypeBuilder TYPE_BUILDER = new ResolvedTypeBuilder();
    public static final String INITIAL_MEHOD_DESC = "(Lorg/ballerinalang/jvm/scheduling/Strand;";

    private JvmCodeGenUtil() {

    }

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
            builder.append("Lorg/ballerinalang/jvm/values/MapValue;");
        }
        return builder.toString();
    }

    static void createFunctionPointer(MethodVisitor mv, String className, String lambdaName) {
        mv.visitTypeInsn(Opcodes.NEW, FUNCTION_POINTER);
        mv.visitInsn(Opcodes.DUP);
        visitInvokeDynamic(mv, className, cleanupFunctionName(lambdaName), 0);

        // load null here for type, since these are fps created for internal usage.
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(Opcodes.ICONST_0); // mark as not-concurrent ie: 'parent'
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, FUNCTION_POINTER, JVM_INIT_METHOD,
                           String.format("(L%s;L%s;L%s;Z)V", FUNCTION, BTYPE, STRING_VALUE), false);
    }

    /**
     * Cleanup type name by replacing '$' with '_'.
     *
     * @param name name to be replaced and cleaned
     * @return cleaned name
     */
    static String cleanupTypeName(String name) {
        return name.replaceAll("[/$ .]", "_");
    }

    static String cleanupPathSeparators(String name) {
        name = cleanupBalExt(name);
        return name.replace(WINDOWS_PATH_SEPERATOR, JAVA_PACKAGE_SEPERATOR);
    }

    private static String cleanupBalExt(String name) {
        return name.replace(BAL_EXTENSION, "");
    }

    static String getFieldTypeSignature(BType bType) {
        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            return "J";
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
            return String.format("L%s;", B_STRING_VALUE);
        } else if (TypeTags.isXMLTypeTag(bType.tag)) {
            return String.format("L%s;", XML_VALUE);
        } else {
            switch (bType.tag) {
                case TypeTags.BYTE:
                    return "I";
                case TypeTags.FLOAT:
                    return "D";
                case TypeTags.DECIMAL:
                    return String.format("L%s;", DECIMAL_VALUE);
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
                case TypeTags.MAP:
                case TypeTags.RECORD:
                    return String.format("L%s;", MAP_VALUE);
                case TypeTags.STREAM:
                    return String.format("L%s;", STREAM_VALUE);
                case TypeTags.TABLE:
                    return String.format("L%s;", TABLE_VALUE_IMPL);
                case TypeTags.ARRAY:
                case TypeTags.TUPLE:
                    return String.format("L%s;", ARRAY_VALUE);
                case TypeTags.ERROR:
                    return String.format("L%s;", ERROR_VALUE);
                case TypeTags.FUTURE:
                    return String.format("L%s;", FUTURE_VALUE);
                case TypeTags.OBJECT:
                    return String.format("L%s;", OBJECT_VALUE);
                case TypeTags.TYPEDESC:
                    return String.format("L%s;", TYPEDESC_VALUE);
                case TypeTags.INVOKABLE:
                    return String.format("L%s;", FUNCTION_POINTER);
                case TypeTags.HANDLE:
                    return String.format("L%s;", HANDLE_VALUE);
                case JTypeTags.JTYPE:
                    return InteropMethodGen.getJTypeSignature((JType) bType);
                default:
                    throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE + bType);
            }
        }
    }

    static void generateDefaultConstructor(ClassWriter cw, String ownerClass) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, JVM_INIT_METHOD, "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, ownerClass, JVM_INIT_METHOD, "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    static void generateStrandMetadata(MethodVisitor mv, String moduleClass,
                                       BIRNode.BIRPackage module, AsyncDataCollector asyncDataCollector) {
        asyncDataCollector.getStrandMetadata().forEach(
                (varName, metaData) -> genStrandMetadataField(mv, moduleClass, module, varName, metaData));
    }

    private static void genStrandMetadataField(MethodVisitor mv, String moduleClass, BIRNode.BIRPackage module,
                                               String varName, ScheduleFunctionInfo metaData) {

        mv.visitTypeInsn(Opcodes.NEW, STRAND_METADATA);
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(module.org.value);
        mv.visitLdcInsn(module.name.value);
        mv.visitLdcInsn(module.version.value);
        if (metaData.typeName == null) {
            mv.visitInsn(Opcodes.ACONST_NULL);
        } else {
            mv.visitLdcInsn(metaData.typeName);
        }
        mv.visitLdcInsn(metaData.parentFunctionName);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, STRAND_METADATA,
                           CONSTRUCTOR_INIT_METHOD, String.format("(L%s;L%s;L%s;L%s;L%s;)V", STRING_VALUE, STRING_VALUE,
                                                                  STRING_VALUE, STRING_VALUE, STRING_VALUE), false);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, moduleClass, varName, String.format("L%s;", STRAND_METADATA));
    }

    static void visitStrandMetadataField(ClassWriter cw, AsyncDataCollector asyncDataCollector) {
        asyncDataCollector.getStrandMetadata().keySet().forEach(varName -> visitStrandMetadataField(cw, varName));
    }

    private static void visitStrandMetadataField(ClassWriter cw, String varName) {
        FieldVisitor fv = cw.visitField(Opcodes.ACC_STATIC, varName, String.format("L%s;", STRAND_METADATA), null,
                                        null);
        fv.visitEnd();
    }

    static String getStrandMetadataVarName(String parentFunction) {
        return STRAND_METADATA_VAR_PREFIX + parentFunction + "$";
    }

    public static String cleanupFunctionName(String functionName) {
        return functionName.replaceAll("[\\.:/<>]", "_");
    }

    static boolean isExternFunc(BIRNode.BIRFunction func) {
        return (func.flags & Flags.NATIVE) == Flags.NATIVE;
    }

    public static String getPackageName(PackageID packageID) {
        return getPackageName(packageID.orgName, packageID.name, packageID.version);
    }

    public static String getPackageName(BIRNode.BIRPackage module) {
        return getPackageName(module.org, module.name, module.version);
    }

    public static String getPackageName(Name orgName, Name moduleName, Name version) {
        return getPackageName(orgName.getValue(), moduleName.getValue(), version.getValue());
    }

    public static String getPackageName(String orgName, String moduleName, String version) {
        String packageName = "";
        if (!moduleName.equals(".")) {
            if (!version.equals("")) {
                packageName = cleanupName(version) + "/";
            }
            packageName = cleanupName(moduleName) + "/" + packageName;
        }

        if (!orgName.equalsIgnoreCase("$anon")) {
            packageName = cleanupName(orgName) + "/" + packageName;
        }

        return packageName;
    }

    static String cleanupName(String name) {
        return name.replace(".", "_");
    }

    static String getModuleLevelClassName(BIRNode.BIRPackage module, String sourceFileName) {
        return getModuleLevelClassName(module.org.value, module.name.value, module.version.value, sourceFileName);
    }

    static String getModuleLevelClassName(String orgName, String moduleName, String version, String sourceFileName) {
        return getModuleLevelClassName(orgName, moduleName, version, sourceFileName, "/");
    }

    static String getModuleLevelClassName(String orgName, String moduleName, String version, String sourceFileName,
                                          String separator) {
        String className = cleanupSourceFileName(sourceFileName);
        // handle source file path start with '/'.
        if (className.startsWith(JAVA_PACKAGE_SEPERATOR)) {
            className = className.substring(1);
        }

        if (!moduleName.equals(".")) {
            if (!version.equals("")) {
                className = cleanupName(version) + separator + className;
            }
            className = cleanupName(moduleName) + separator + className;
        }

        if (!orgName.equalsIgnoreCase("$anon")) {
            className = cleanupName(orgName) + separator + className;
        }

        return className;
    }

    private static String cleanupSourceFileName(String name) {
        return name.replace(".", FILE_NAME_PERIOD_SEPERATOR);
    }

    public static String getMethodDesc(List<BType> paramTypes, BType retType) {
        return INITIAL_MEHOD_DESC + populateMethodDesc(paramTypes) + generateReturnType(retType);
    }

    public static String getMethodDesc(List<BType> paramTypes, BType retType, BType attachedType) {
        return INITIAL_MEHOD_DESC + getArgTypeSignature(attachedType) + populateMethodDesc(paramTypes) +
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
            return String.format("L%s;", B_STRING_VALUE);
        } else if (TypeTags.isXMLTypeTag(bType.tag)) {
            return String.format("L%s;", XML_VALUE);
        }

        switch (bType.tag) {
            case TypeTags.BYTE:
                return "I";
            case TypeTags.FLOAT:
                return "D";
            case TypeTags.DECIMAL:
                return String.format("L%s;", DECIMAL_VALUE);
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
                return String.format("L%s;", OBJECT);
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                return String.format("L%s;", ARRAY_VALUE);
            case TypeTags.ERROR:
                return String.format("L%s;", ERROR_VALUE);
            case TypeTags.MAP:
            case TypeTags.RECORD:
                return String.format("L%s;", MAP_VALUE);
            case TypeTags.FUTURE:
                return String.format("L%s;", FUTURE_VALUE);
            case TypeTags.STREAM:
                return String.format("L%s;", STREAM_VALUE);
            case TypeTags.TABLE:
                return String.format("L%s;", TABLE_VALUE_IMPL);
            case TypeTags.INVOKABLE:
                return String.format("L%s;", FUNCTION_POINTER);
            case TypeTags.TYPEDESC:
                return String.format("L%s;", TYPEDESC_VALUE);
            case TypeTags.OBJECT:
                return String.format("L%s;", OBJECT_VALUE);
            case TypeTags.HANDLE:
                return String.format("L%s;", HANDLE_VALUE);
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE +
                                                         String.format("%s", bType));
        }
    }

    public static String generateReturnType(BType bType) {
        bType = JvmCodeGenUtil.TYPE_BUILDER.build(bType);
        if (bType == null || bType.tag == TypeTags.NIL || bType.tag == TypeTags.NEVER) {
            return String.format(")L%s;", OBJECT);
        } else if (TypeTags.isIntegerTypeTag(bType.tag)) {
            return ")J";
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
            return String.format(")L%s;", B_STRING_VALUE);
        } else if (TypeTags.isXMLTypeTag(bType.tag)) {
            return String.format(")L%s;", XML_VALUE);
        }

        switch (bType.tag) {
            case TypeTags.BYTE:
                return ")I";
            case TypeTags.FLOAT:
                return ")D";
            case TypeTags.DECIMAL:
                return String.format(")L%s;", DECIMAL_VALUE);
            case TypeTags.BOOLEAN:
                return ")Z";
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                return String.format(")L%s;", ARRAY_VALUE);
            case TypeTags.MAP:
            case TypeTags.RECORD:
                return String.format(")L%s;", MAP_VALUE);
            case TypeTags.ERROR:
                return String.format(")L%s;", ERROR_VALUE);
            case TypeTags.STREAM:
                return String.format(")L%s;", STREAM_VALUE);
            case TypeTags.TABLE:
                return String.format(")L%s;", TABLE_VALUE_IMPL);
            case TypeTags.FUTURE:
                return String.format(")L%s;", FUTURE_VALUE);
            case TypeTags.TYPEDESC:
                return String.format(")L%s;", TYPEDESC_VALUE);
            case TypeTags.ANY:
            case TypeTags.ANYDATA:
            case TypeTags.UNION:
            case TypeTags.INTERSECTION:
            case TypeTags.JSON:
            case TypeTags.FINITE:
            case TypeTags.READONLY:
                return String.format(")L%s;", OBJECT);
            case TypeTags.OBJECT:
                return String.format(")L%s;", OBJECT_VALUE);
            case TypeTags.INVOKABLE:
                return String.format(")L%s;", FUNCTION_POINTER);
            case TypeTags.HANDLE:
                return String.format(")L%s;", HANDLE_VALUE);
            default:
                throw new BLangCompilerException(JvmConstants.TYPE_NOT_SUPPORTED_MESSAGE + bType);
        }
    }

    static String cleanupObjectTypeName(String typeName) {
        int index = typeName.lastIndexOf(".");
        if (index > 0) {
            return typeName.substring(index + 1);
        } else {
            return typeName;
        }
    }

    static void loadChannelDetails(MethodVisitor mv, List<BIRNode.ChannelDetails> channels) {
        mv.visitIntInsn(BIPUSH, channels.size());
        mv.visitTypeInsn(ANEWARRAY, CHANNEL_DETAILS);
        int index = 0;
        for (BIRNode.ChannelDetails ch : channels) {
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, index);
            index += 1;

            mv.visitTypeInsn(NEW, CHANNEL_DETAILS);
            mv.visitInsn(DUP);
            mv.visitLdcInsn(ch.name);

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
                    String.format("(L%s;ZZ)V", STRING_VALUE), false);
            mv.visitInsn(AASTORE);
        }
    }

    public static String toNameString(BType t) {
        return t.tsymbol.name.value;
    }

    public static boolean isBallerinaBuiltinModule(String orgName, String moduleName) {
        return orgName.equals("ballerina") && moduleName.equals("builtin");
    }

    public static void generateBbInstructions(MethodVisitor mv, LabelGenerator labelGen, JvmInstructionGen instGen,
                                              int localVarOffset, AsyncDataCollector asyncDataCollector,
                                              String funcName,
                                              BIRNode.BIRBasicBlock bb) {
        int insCount = bb.instructions.size();
        for (int i = 0; i < insCount; i++) {
            Label insLabel = labelGen.getLabel(funcName + bb.id.value + "ins" + i);
            mv.visitLabel(insLabel);
            BIRInstruction inst = bb.instructions.get(i);
            if (inst != null) {
                generateDiagnosticPos(((BIRNode) inst).pos, mv);
                instGen.generateInstructions(localVarOffset, asyncDataCollector, inst);
            }
        }
    }

    public static void generateDiagnosticPos(DiagnosticPos pos, MethodVisitor mv) {
        if (pos != null && pos.sLine != 0x80000000) {
            Label label = new Label();
            mv.visitLabel(label);
            mv.visitLineNumber(pos.sLine, label);
        }
    }

    public static void genYieldCheck(MethodVisitor mv, LabelGenerator labelGen, BIRNode.BIRBasicBlock thenBB,
                                     String funcName, int localVarOffset) {
        mv.visitVarInsn(ALOAD, localVarOffset);
        mv.visitMethodInsn(INVOKEVIRTUAL, STRAND_CLASS, "isYielded", "()Z", false);
        Label yieldLabel = labelGen.getLabel(funcName + "yield");
        mv.visitJumpInsn(IFNE, yieldLabel);

        // goto thenBB
        Label gotoLabel = labelGen.getLabel(funcName + thenBB.id.value);
        mv.visitJumpInsn(GOTO, gotoLabel);
    }
}
