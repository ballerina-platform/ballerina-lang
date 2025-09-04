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

package org.wso2.ballerinalang.compiler.bir.codegen.methodgen;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.types.RecordType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.LCONST_1;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BALLERINA_VERSION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_RUNTIME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLI_SPEC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.COMPATIBILITY_CHECKER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CONFIGURATION_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CONFIGURE_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CONFIG_DETAILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CURRENT_MODULE_STOP_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CURRENT_MODULE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_FUTURE_AND_EXIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_FUTURE_AND_RETURN_IS_PANIC_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_THROWABLE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HASH_MAP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JAVA_RUNTIME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LAMBDA_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LAUNCH_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAIN_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_EXECUTE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OPERAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OPTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.REPOSITORY_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER_VARIABLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.START_ISOLATED_WORKER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.START_NON_ISOLATED_WORKER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TEST_ARGUMENTS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TEST_CONFIG_ARGS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TEST_EXECUTION_STATE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.WAIT_ON_LISTENERS_METHOD_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ADD_BALLERINA_INFO;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.ADD_SHUTDOWN_HOOK;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CURRENT_MODULE_STOP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_CONFIG_DETAILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MAIN_ARGS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_MODULE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_PATH;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_RUNTIME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRING;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRING_ARRAY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_TEST_CONFIG_PATH;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_FUTURE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_FUTURE_AND_RETURN_IS_PANIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_THROWABLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_CLI_SPEC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_CONFIG;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_CONFIGURABLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_OPERAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_OPTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_RUNTIME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_SIGNAL_LISTENER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_TEST_ARGS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.MAIN_METHOD_SIGNATURE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.METHOD_STRING_PARAM;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SCHEDULE_CALL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.getVarStoreClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmModuleUtils.getModuleLevelClassName;

/**
 * Generates Jvm byte code for the main method.
 *
 * @since 2.0.0
 */
public class MainMethodGen {

    public static final String RUNTIME_VAR = "$runtime";
    public static final String FUTURE_VAR = "$future";
    public static final String SCHEDULER_VAR = "$schedulerVar";
    public static final String CONFIG_VAR = "$configVar";
    private final SymbolTable symbolTable;
    private final String globalVarsPkgName;
    private final BIRVarToJVMIndexMap indexMap;
    private final JvmTypeGen jvmTypeGen;
    private final boolean isRemoteMgtEnabled;

    public MainMethodGen(SymbolTable symbolTable, JvmTypeGen jvmTypeGen, boolean isRemoteMgtEnabled,
                         String globalVarsPkgName) {
        this.symbolTable = symbolTable;
        // add main string[] args param first
        indexMap = new BIRVarToJVMIndexMap(1);
        this.jvmTypeGen = jvmTypeGen;
        this.isRemoteMgtEnabled = isRemoteMgtEnabled;
        this.globalVarsPkgName = globalVarsPkgName;
    }

    public void generateMainMethod(BIRNode.BIRFunction userMainFunc, ClassWriter cw, BIRNode.BIRPackage pkg,
                                   String initClass, boolean serviceEPAvailable, boolean isTestable) {

        int runtimeVarIndex = indexMap.addIfNotExists(RUNTIME_VAR, symbolTable.anyType);
        int schedulerVarIndex = indexMap.addIfNotExists(SCHEDULER_VAR, symbolTable.anyType);
        int futureVarIndex = indexMap.addIfNotExists(FUTURE_VAR, symbolTable.anyType);

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, MAIN_METHOD, MAIN_METHOD_SIGNATURE,
                null, null);
        mv.visitCode();
        Label tryCatchStart = new Label();
        Label tryCatchEnd = new Label();
        Label tryCatchHandle = new Label();
        mv.visitTryCatchBlock(tryCatchStart, tryCatchEnd, tryCatchHandle, "java/lang/Error");
        mv.visitLabel(tryCatchStart);

        // check for java compatibility
        generateJavaCompatibilityCheck(mv);
        generateBallerinaRuntimeInformation(mv);
        genRuntimeAndGetScheduler(mv, initClass, runtimeVarIndex, schedulerVarIndex);
        invokeConfigInit(mv, pkg.packageID, runtimeVarIndex);
        // TRAP signal handler
        genStartTrapSignalHandler(mv);
        // register a shutdown hook to call package stop() method.
        if (!isTestable) {
            genShutdownHook(mv, initClass, runtimeVarIndex);
        }

        boolean hasInitFunction = MethodGenUtils.hasInitFunction(pkg);
        // handle calling init and start during module initialization.
        generateSetModuleInitialedAndStarted(mv, runtimeVarIndex);
        generateExecuteFunctionCall(initClass, mv, userMainFunc, isTestable, schedulerVarIndex, futureVarIndex);
        handleFutureValue(mv, isTestable, futureVarIndex);
        if (isTestable) {
            generateModuleStopCall(initClass, mv, runtimeVarIndex);
        } else {
            if (hasInitFunction) {
                setListenerFound(mv, serviceEPAvailable, runtimeVarIndex);
            }
            if (!serviceEPAvailable) {
                JvmCodeGenUtil.generateExitRuntime(mv);
            }
        }
        mv.visitLabel(tryCatchEnd);
        mv.visitInsn(RETURN);
        mv.visitLabel(tryCatchHandle);
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_THROWABLE_METHOD, HANDLE_THROWABLE, false);
        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, "main", initClass);
        mv.visitEnd();
    }

    private void generateBallerinaRuntimeInformation(MethodVisitor mv) {
        String property = System.getProperty(BALLERINA_HOME);
        mv.visitLdcInsn(property == null ? "" : property);
        property = System.getProperty(BALLERINA_VERSION);
        mv.visitLdcInsn(property == null ? "" : property);
        if (isRemoteMgtEnabled) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, REPOSITORY_IMPL, "addBallerinaRuntimeInformation", ADD_BALLERINA_INFO,
                false);
    }

    private void generateSetModuleInitialedAndStarted(MethodVisitor mv, int runtimeVarIndex) {
        mv.visitVarInsn(ALOAD, runtimeVarIndex);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, BAL_RUNTIME, "moduleInitialized", "Z");
        mv.visitVarInsn(ALOAD, runtimeVarIndex);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, BAL_RUNTIME, "moduleStarted", "Z");
    }

    private void generateExecuteFunctionCall(String initClass, MethodVisitor mv, BIRNode.BIRFunction userMainFunc,
                                             boolean isTestable, int schedulerVarIndex, int futureVarIndex) {
        mv.visitVarInsn(ALOAD, schedulerVarIndex);
        // invoke the module execute method
        genSubmitToScheduler(initClass, mv, userMainFunc, isTestable, futureVarIndex);
    }

    private void generateModuleStopCall(String initClass, MethodVisitor mv, int runtimeVarIndex) {
        mv.visitVarInsn(ALOAD, runtimeVarIndex);
        mv.visitMethodInsn(INVOKESTATIC, initClass, CURRENT_MODULE_STOP_METHOD, CURRENT_MODULE_STOP, false);
    }

    private void invokeConfigInit(MethodVisitor mv, PackageID packageID, int runtimeVarIndex) {
        String configClass = getModuleLevelClassName(packageID, CONFIGURATION_CLASS_NAME);
        mv.visitTypeInsn(NEW, HASH_MAP);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, HASH_MAP, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
        mv.visitVarInsn(ASTORE, 6);
        mv.visitVarInsn(ALOAD, 6);

        if (!packageID.isTestPkg) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, LAUNCH_UTILS, "getConfigurationDetails", GET_CONFIG_DETAILS,
                    false);
        } else {
            loadCLIArgsForTestConfigInit(mv);
            String initClass = getModuleLevelClassName(packageID, MODULE_INIT_CLASS_NAME);
            mv.visitFieldInsn(GETSTATIC, initClass, CURRENT_MODULE_VAR_NAME, GET_MODULE);
            mv.visitLdcInsn(packageID.pkgName.toString());
            mv.visitLdcInsn(packageID.sourceRoot);
            mv.visitMethodInsn(INVOKESTATIC, LAUNCH_UTILS, "getTestConfigPaths", GET_TEST_CONFIG_PATH, false);
        }
        int configDetailsIndex = indexMap.addIfNotExists(CONFIG_VAR, symbolTable.anyType);

        mv.visitVarInsn(ASTORE, configDetailsIndex);

        mv.visitVarInsn(ALOAD, configDetailsIndex);
        mv.visitFieldInsn(GETFIELD, CONFIG_DETAILS, "paths", GET_PATH);
        mv.visitVarInsn(ALOAD, configDetailsIndex);
        mv.visitFieldInsn(GETFIELD, CONFIG_DETAILS, "configContent", GET_STRING);
        mv.visitVarInsn(ALOAD, runtimeVarIndex);
        mv.visitMethodInsn(INVOKESTATIC, configClass, CONFIGURE_INIT, INIT_CONFIG, false);
        String moduleInitClass = getModuleLevelClassName(packageID, MODULE_INIT_CLASS_NAME);
        mv.visitFieldInsn(GETSTATIC, moduleInitClass, CURRENT_MODULE_VAR_NAME, GET_MODULE);
        mv.visitVarInsn(ALOAD, 6);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, configDetailsIndex);
        mv.visitFieldInsn(GETFIELD, CONFIG_DETAILS, "paths", GET_PATH);
        mv.visitVarInsn(ALOAD, configDetailsIndex);
        mv.visitFieldInsn(GETFIELD, CONFIG_DETAILS, "configContent", GET_STRING);
        mv.visitMethodInsn(INVOKESTATIC, LAUNCH_UTILS, "initConfigurableVariables", INIT_CONFIGURABLE, false);
    }

    private void generateJavaCompatibilityCheck(MethodVisitor mv) {
        mv.visitLdcInsn(getJavaVersion());
        mv.visitMethodInsn(INVOKESTATIC, COMPATIBILITY_CHECKER, "verifyJavaCompatibility", METHOD_STRING_PARAM, false);
    }

    private String getJavaVersion() {
        String versionProperty = "java.version";
        String javaVersion = System.getProperty(versionProperty);
        return Objects.requireNonNullElse(javaVersion, "");
    }

    private void genStartTrapSignalHandler(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, LAUNCH_UTILS, "startTrapSignalHandler", VOID_METHOD_DESC, false);
    }

    private void genShutdownHook(MethodVisitor mv, String initClass, int runtimeVarIndex) {
        String shutdownClassName = initClass + "$SignalListener";
        mv.visitMethodInsn(INVOKESTATIC, JAVA_RUNTIME, "getRuntime", GET_RUNTIME, false);
        mv.visitTypeInsn(NEW, shutdownClassName);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, runtimeVarIndex);
        mv.visitMethodInsn(INVOKESPECIAL, shutdownClassName, JVM_INIT_METHOD, INIT_SIGNAL_LISTENER, false);
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_RUNTIME, "addShutdownHook", ADD_SHUTDOWN_HOOK, false);
    }

    private void genRuntimeAndGetScheduler(MethodVisitor mv, String initClass, int runtimeVarIndex,
                                           int schedulerVarIndex) {
        mv.visitTypeInsn(NEW, BAL_RUNTIME);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, initClass, CURRENT_MODULE_VAR_NAME, GET_MODULE);
        mv.visitMethodInsn(INVOKESPECIAL, BAL_RUNTIME, JVM_INIT_METHOD, INIT_RUNTIME, false);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ASTORE, runtimeVarIndex);
        mv.visitFieldInsn(GETFIELD, BAL_RUNTIME, SCHEDULER_VARIABLE, GET_SCHEDULER);
        mv.visitVarInsn(ASTORE, schedulerVarIndex);
    }

    private void setListenerFound(MethodVisitor mv, boolean serviceEPAvailable, int runtimeVarIndex) {
        mv.visitVarInsn(ALOAD, runtimeVarIndex);
        if (serviceEPAvailable) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }
        mv.visitMethodInsn(INVOKEVIRTUAL , BAL_RUNTIME, WAIT_ON_LISTENERS_METHOD_NAME, "(Z)V", false);
    }

    private void loadCLIArgsForMain(MethodVisitor mv, List<BIRNode.BIRFunctionParameter> params,
                                    List<BIRNode.BIRAnnotationAttachment> annotAttachments) {
        mv.visitInsn(ICONST_2);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(AASTORE);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitTypeInsn(NEW , CLI_SPEC);
        mv.visitInsn(DUP);
        // get defaultable arg names from function annotation
        List<String> defaultableNames = getDefaultableNames(annotAttachments);
        // create function info array
        createFunctionInfoArray(mv, params, defaultableNames);
        // load string[] that got parsed into java main
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL , CLI_SPEC, JVM_INIT_METHOD, INIT_CLI_SPEC, false);
        mv.visitInsn(AASTORE);
    }

    private void loadCLIArgsForTestConfigInit(MethodVisitor mv) {
        mv.visitTypeInsn(NEW , TEST_CONFIG_ARGS);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL , TEST_CONFIG_ARGS, JVM_INIT_METHOD, INIT_TEST_ARGS, false);
        mv.visitMethodInsn(INVOKEVIRTUAL , TEST_CONFIG_ARGS, "getConfigCliArgs", GET_STRING_ARRAY, false);
    }

    private void loadCLIArgsForTestExecute(MethodVisitor mv) {
        mv.visitTypeInsn(NEW , TEST_ARGUMENTS);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL , TEST_ARGUMENTS, JVM_INIT_METHOD, INIT_TEST_ARGS, false);
        mv.visitMethodInsn(INVOKEVIRTUAL , TEST_ARGUMENTS, "getArgValues", GET_MAIN_ARGS, false);
    }

    private void createFunctionInfoArray(MethodVisitor mv, List<BIRNode.BIRFunctionParameter> params,
                                         List<String> defaultableNames) {
        int size = params.size();
        if (!params.isEmpty() &&
                JvmCodeGenUtil.getImpliedType(params.get(size - 1).type) instanceof RecordType) {
            BIRNode.BIRFunctionParameter param = params.get(size - 1);
            createOption(mv, param, size - 1);
            size--;
        } else if (params.size() >= 2
                && JvmCodeGenUtil.getImpliedType(params.get(size - 2).type) instanceof RecordType) {
            BIRNode.BIRFunctionParameter param = params.get(size - 2);
            createOption(mv, param, size - 2);
            size--;
        } else {
            mv.visitInsn(ACONST_NULL);
        }
        mv.visitIntInsn(BIPUSH, size);
        mv.visitTypeInsn(ANEWARRAY , OPERAND);
        int defaultableIndex = 0;
        int arrIndex = 0;
        for (BIRNode.BIRFunctionParameter birFunctionParameter : params) {
            if (birFunctionParameter != null
                    && JvmCodeGenUtil.getImpliedType(birFunctionParameter.type) instanceof RecordType) {
                defaultableIndex++;
                continue;
            }
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, arrIndex++);
            mv.visitTypeInsn(NEW , OPERAND);
            mv.visitInsn(DUP);
            if (birFunctionParameter != null) {
                if (birFunctionParameter.hasDefaultExpr) {
                    mv.visitInsn(ICONST_1);
                } else {
                    mv.visitInsn(ICONST_0);
                }
                if (!defaultableNames.isEmpty()) {
                    mv.visitLdcInsn(defaultableNames.get(defaultableIndex++));
                }
                jvmTypeGen.loadType(mv, birFunctionParameter.type);
            }
            mv.visitMethodInsn(INVOKESPECIAL , OPERAND , JVM_INIT_METHOD, INIT_OPERAND, false);
            mv.visitInsn(AASTORE);
        }
    }

    private void createOption(MethodVisitor mv, BIRNode.BIRFunctionParameter param, int location) {
        mv.visitTypeInsn(NEW , OPTION);
        mv.visitInsn(DUP);
        jvmTypeGen.loadType(mv, param.type);
        mv.visitIntInsn(BIPUSH, location);
        mv.visitMethodInsn(INVOKESPECIAL , OPTION , JVM_INIT_METHOD, INIT_OPTION, false);
    }

    private List<String> getDefaultableNames(List<BIRNode.BIRAnnotationAttachment> annotAttachments) {
        List<String> defaultableNames = new ArrayList<>();
        int defaultableIndex = 0;
        for (BIRNode.BIRAnnotationAttachment attachment : annotAttachments) {
            if (attachment != null && attachment.annotTagRef.value.equals(JvmConstants.DEFAULTABLE_ARGS_ANOT_NAME)) {
                Map<?, ?> annotFieldMap = (Map<?, ?>) ((BIRNode.BIRConstAnnotationAttachment) attachment)
                        .annotValue.value;
                BIRNode.ConstValue annConstValue =
                        (BIRNode.ConstValue) annotFieldMap.get(JvmConstants.DEFAULTABLE_ARGS_ANOT_FIELD);
                BIRNode.ConstValue[] annotArrayValue = (BIRNode.ConstValue[]) annConstValue.value;
                for (BIRNode.ConstValue entryOptional : annotArrayValue) {
                    defaultableNames.add(defaultableIndex, (String) entryOptional.value);
                    defaultableIndex += 1;
                }
                break;
            }
        }
        return defaultableNames;
    }

    private void genSubmitToScheduler(String initClass, MethodVisitor mv, BIRNode.BIRFunction userMainFunc,
                                      boolean isTestable, int futureVarIndex) {
        JvmCodeGenUtil.createFunctionPointer(mv, initClass, LAMBDA_PREFIX + MODULE_EXECUTE_METHOD + "$");
        // no parent strand
        mv.visitInsn(ACONST_NULL);
        BType anyType = symbolTable.anyType;
        jvmTypeGen.loadType(mv, anyType);
        mv.visitLdcInsn(MAIN_METHOD);
        mv.visitInsn(ACONST_NULL);
        if (userMainFunc != null) {
            loadCLIArgsForMain(mv, userMainFunc.parameters, userMainFunc.annotAttachments);
        } else if (isTestable) {
            loadCLIArgsForTestExecute(mv);
        } else {
            mv.visitIntInsn(BIPUSH, 1);
            mv.visitTypeInsn(ANEWARRAY, OBJECT);
        }
        if (userMainFunc != null && (userMainFunc.flags & Flags.ISOLATED) == Flags.ISOLATED) {
            mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, START_ISOLATED_WORKER, SCHEDULE_CALL, false);
        } else {
            mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, START_NON_ISOLATED_WORKER, SCHEDULE_CALL, false);
        }
        mv.visitVarInsn(ASTORE, futureVarIndex);
    }

    private void handleFutureValue(MethodVisitor mv, boolean isTestable, int futureVarIndex) {
        mv.visitVarInsn(ALOAD, futureVarIndex);
        if (isTestable) {
            mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_FUTURE_AND_RETURN_IS_PANIC_METHOD,
                    HANDLE_FUTURE_AND_RETURN_IS_PANIC, false);
            Label ifLabel = new Label();
            mv.visitJumpInsn(IFEQ, ifLabel);
            mv.visitInsn(LCONST_1);
            String testExecutionStateGlobalClass = getVarStoreClass(this.globalVarsPkgName, TEST_EXECUTION_STATE);
            mv.visitFieldInsn(PUTSTATIC, testExecutionStateGlobalClass, VALUE_VAR_NAME, "J");
            mv.visitLabel(ifLabel);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_FUTURE_AND_EXIT_METHOD, HANDLE_FUTURE, false);
        }
    }
}
