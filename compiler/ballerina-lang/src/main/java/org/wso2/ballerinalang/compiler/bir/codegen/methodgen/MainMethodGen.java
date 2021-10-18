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
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CONFIGURATION_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CONFIGURE_INIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LAUNCH_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PATH;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TOML_DETAILS;

/**
 * Generates Jvm byte code for the main method.
 *
 * @since 2.0.0
 */
public class MainMethodGen {

    public static final String INIT_FUTURE_VAR = "initFutureVar";
    public static final String START_FUTURE_VAR = "startFutureVar";
    public static final String MAIN_FUTURE_VAR = "mainFutureVar";
    public static final String SCHEDULER_VAR = "schedulerVar";
    public static final String CONFIG_VAR = "configVar";
    private final SymbolTable symbolTable;
    private final BIRVarToJVMIndexMap indexMap;
    private final JvmTypeGen jvmTypeGen;
    private final JvmCastGen jvmCastGen;
    private final AsyncDataCollector asyncDataCollector;

    public MainMethodGen(SymbolTable symbolTable, JvmTypeGen jvmTypeGen,
                         JvmCastGen jvmCastGen, AsyncDataCollector asyncDataCollector) {
        this.symbolTable = symbolTable;
        // add main string[] args param first
        indexMap = new BIRVarToJVMIndexMap(1);
        this.jvmTypeGen = jvmTypeGen;
        this.jvmCastGen = jvmCastGen;
        this.asyncDataCollector = asyncDataCollector;
    }

    public void generateMainMethod(BIRNode.BIRFunction userMainFunc, ClassWriter cw, BIRNode.BIRPackage pkg,
                            String initClass, boolean serviceEPAvailable) {

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
                                          null);
        mv.visitCode();
        Label tryCatchStart = new Label();
        Label tryCatchEnd = new Label();
        Label tryCatchHandle = new Label();
        mv.visitTryCatchBlock(tryCatchStart, tryCatchEnd, tryCatchHandle, JvmConstants.THROWABLE);
        mv.visitLabel(tryCatchStart);

        // check for java compatibility
        generateJavaCompatibilityCheck(mv);

        invokeConfigInit(mv, pkg.packageID);
        // start all listeners
        startListeners(mv, serviceEPAvailable);

        genInitScheduler(mv);
        // register a shutdown hook to call package stop() method.
        genShutdownHook(mv, initClass);


        boolean hasInitFunction = MethodGenUtils.hasInitFunction(pkg);
        if (hasInitFunction) {
            generateMethodCall(initClass, mv, JvmConstants.MODULE_INIT,
                               MethodGenUtils.INIT_FUNCTION_SUFFIX, INIT_FUTURE_VAR);
        }

        if (userMainFunc != null) {
            generateUserMainFunctionCall(userMainFunc, initClass, mv);
        }


        if (hasInitFunction) {
            generateMethodCall(initClass, mv, JvmConstants.MODULE_START, "start", START_FUTURE_VAR);
            setListenerFound(mv, serviceEPAvailable);
        }
        stopListeners(mv, serviceEPAvailable);
        if (!serviceEPAvailable) {
            generateExitRuntime(mv);
        }

        mv.visitLabel(tryCatchEnd);
        mv.visitInsn(RETURN);
        mv.visitLabel(tryCatchHandle);
        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.RUNTIME_UTILS, JvmConstants.HANDLE_ALL_THROWABLE_METHOD,
                           String.format("(L%s;)V", JvmConstants.THROWABLE), false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateMethodCall(String initClass, MethodVisitor mv,
                                    String lambdaName, String funcName, String futureVar) {
        mv.visitVarInsn(ALOAD, indexMap.get(SCHEDULER_VAR));
        mv.visitIntInsn(BIPUSH, 1);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
        genSubmitToScheduler(initClass, mv, String.format("$lambda$%s$", lambdaName), funcName,
                             futureVar);
        genReturn(mv, indexMap, futureVar);
    }

    private void startScheduler(int schedulerVarIndex, MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, schedulerVarIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, JvmConstants.SCHEDULER, JvmConstants.SCHEDULER_START_METHOD, "()V", false);
    }

    private void invokeConfigInit(MethodVisitor mv, PackageID packageID) {
        String configClass = JvmCodeGenUtil.getModuleLevelClassName(packageID, CONFIGURATION_CLASS_NAME);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, LAUNCH_UTILS, "getConfigurationDetails", "()L" + TOML_DETAILS + ";", false);
        int configDetailsIndex = indexMap.addIfNotExists(CONFIG_VAR, symbolTable.anyType);

        mv.visitVarInsn(ASTORE, configDetailsIndex);

        mv.visitVarInsn(ALOAD, configDetailsIndex);
        mv.visitFieldInsn(GETFIELD, TOML_DETAILS, "paths", "[L" + PATH + ";");
        mv.visitVarInsn(ALOAD, configDetailsIndex);
        mv.visitFieldInsn(GETFIELD, TOML_DETAILS, "configContent", "L" + STRING_VALUE + ";");
        mv.visitMethodInsn(INVOKESTATIC, configClass, CONFIGURE_INIT, String.format("([L%s;[L%s;L%s;)V", STRING_VALUE
                , PATH, STRING_VALUE), false);
    }

    private void generateJavaCompatibilityCheck(MethodVisitor mv) {
        mv.visitLdcInsn(getJavaVersion());
        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.COMPATIBILITY_CHECKER, "verifyJavaCompatibility",
                           String.format("(L%s;)V", JvmConstants.STRING_VALUE), false);
    }

    private String getJavaVersion() {
        String versionProperty = "java.version";
        String javaVersion = System.getProperty(versionProperty);
        return Objects.requireNonNullElse(javaVersion, "");
    }

    private void startListeners(MethodVisitor mv, boolean isServiceEPAvailable) {
        mv.visitLdcInsn(isServiceEPAvailable);
        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.LAUNCH_UTILS, "startListeners", "(Z)V", false);
    }

    private void genShutdownHook(MethodVisitor mv, String initClass) {
        String shutdownClassName = initClass + "$SignalListener";
        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.JAVA_RUNTIME, "getRuntime",
                           String.format("()L%s;", JvmConstants.JAVA_RUNTIME), false);
        mv.visitTypeInsn(NEW, shutdownClassName);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, indexMap.get(SCHEDULER_VAR));
        mv.visitMethodInsn(INVOKEVIRTUAL, JvmConstants.SCHEDULER, "getListenerRegistry",
                           String.format("()L%s;", JvmConstants.LISTENER_REGISTRY_CLASS), false);
        mv.visitMethodInsn(INVOKESPECIAL, shutdownClassName, JvmConstants.JVM_INIT_METHOD,
                           String.format("(L%s;)V", JvmConstants.LISTENER_REGISTRY_CLASS), false);
        mv.visitMethodInsn(INVOKEVIRTUAL, JvmConstants.JAVA_RUNTIME, "addShutdownHook",
                           String.format("(L%s;)V", JvmConstants.JAVA_THREAD), false);
    }

    private void genInitScheduler(MethodVisitor mv) {
        mv.visitTypeInsn(NEW, JvmConstants.SCHEDULER);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKESPECIAL, JvmConstants.SCHEDULER, JvmConstants.JVM_INIT_METHOD, "(Z)V", false);
        int schedulerVarIndex = indexMap.addIfNotExists(SCHEDULER_VAR, symbolTable.anyType);
        mv.visitVarInsn(ASTORE, schedulerVarIndex);
    }

    private void setListenerFound(MethodVisitor mv, boolean serviceEPAvailable) {
        // need to set immortal=true and start the scheduler again
        if (serviceEPAvailable) {
            int schedulerVarIndex = indexMap.get(SCHEDULER_VAR);
            mv.visitVarInsn(ALOAD, schedulerVarIndex);
            mv.visitInsn(ICONST_1);
            mv.visitMethodInsn(INVOKEVIRTUAL, JvmConstants.SCHEDULER, "setListenerDeclarationFound", "(Z)V", false);
            startScheduler(schedulerVarIndex, mv);
        }
    }

    private void generateUserMainFunctionCall(BIRNode.BIRFunction userMainFunc, String initClass, MethodVisitor mv) {
        int schedulerVarIndex = indexMap.get(SCHEDULER_VAR);
        mv.visitVarInsn(ALOAD, schedulerVarIndex);
        loadCLIArgsForMain(mv, userMainFunc.parameters, userMainFunc.annotAttachments);

        // invoke the user's main method
        genSubmitToScheduler(initClass, mv, "$lambda$main$", "main", MAIN_FUTURE_VAR);
        handleErrorFromFutureValue(mv, MAIN_FUTURE_VAR);
        // At this point we are done executing all the functions including asyncs
        boolean isVoidFunction = userMainFunc.type.retType.tag == TypeTags.NIL;
        if (!isVoidFunction) {
            genReturn(mv, indexMap, MAIN_FUTURE_VAR);
        }
    }

    private void storeFuture(BIRVarToJVMIndexMap indexMap, MethodVisitor mv, String futureVar) {
        int mainFutureVarIndex = indexMap.addIfNotExists(futureVar, symbolTable.anyType);
        mv.visitVarInsn(ASTORE, mainFutureVarIndex);
        mv.visitVarInsn(ALOAD, mainFutureVarIndex);
    }

    private void loadCLIArgsForMain(MethodVisitor mv, List<BIRNode.BIRFunctionParameter> params,
                                    List<BIRNode.BIRAnnotationAttachment> annotAttachments) {
        mv.visitTypeInsn(NEW, JvmConstants.CLI_SPEC);
        mv.visitInsn(DUP);
        // get defaultable arg names from function annotation
        List<String> defaultableNames = getDefaultableNames(annotAttachments);
        // create function info array
        createFunctionInfoArray(mv, params, defaultableNames);
        // load string[] that got parsed into java main
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, JvmConstants.CLI_SPEC, JvmConstants.JVM_INIT_METHOD,
                           String.format("(L%s;[L%s;[L%s;)V", JvmConstants.OPTION, JvmConstants.OPERAND,
                                         JvmConstants.STRING_VALUE), false);
        mv.visitMethodInsn(INVOKEVIRTUAL, JvmConstants.CLI_SPEC, "getMainArgs", String.format("()[L%s;", OBJECT),
                           false);
    }

    private void createFunctionInfoArray(MethodVisitor mv, List<BIRNode.BIRFunctionParameter> params,
                                         List<String> defaultableNames) {
        int size = params.size();
        if (!params.isEmpty() && params.get(size - 1).type instanceof RecordType) {
            BIRNode.BIRFunctionParameter param = params.get(size - 1);
            createOption(mv, param, size - 1);
            size--;
        } else if (params.size() >= 2 && params.get(size - 2).type instanceof RecordType) {
            BIRNode.BIRFunctionParameter param = params.get(size - 2);
            createOption(mv, param, size - 2);
            size--;
        } else {
            mv.visitInsn(ACONST_NULL);
        }
        mv.visitIntInsn(BIPUSH, size);
        mv.visitTypeInsn(ANEWARRAY, JvmConstants.OPERAND);
        int defaultableIndex = 0;
        int arrIndex = 0;
        for (BIRNode.BIRFunctionParameter birFunctionParameter : params) {
            if (birFunctionParameter != null && birFunctionParameter.type instanceof RecordType) {
                defaultableIndex++;
                continue;
            }
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, arrIndex++);
            mv.visitTypeInsn(NEW, JvmConstants.OPERAND);
            mv.visitInsn(DUP);
            if (birFunctionParameter != null) {
                if (birFunctionParameter.hasDefaultExpr) {
                    mv.visitInsn(ICONST_1);
                } else {
                    mv.visitInsn(ICONST_0);
                }
                mv.visitLdcInsn(defaultableNames.get(defaultableIndex++));
                jvmTypeGen.loadType(mv, birFunctionParameter.type);
            }
            mv.visitMethodInsn(INVOKESPECIAL, JvmConstants.OPERAND, JvmConstants.JVM_INIT_METHOD,
                               String.format("(ZL%s;L%s;)V", JvmConstants.STRING_VALUE, JvmConstants.TYPE), false);
            mv.visitInsn(AASTORE);
        }
    }

    private void createOption(MethodVisitor mv, BIRNode.BIRFunctionParameter param, int location) {
        mv.visitTypeInsn(NEW, JvmConstants.OPTION);
        mv.visitInsn(DUP);
        jvmTypeGen.loadType(mv, param.type);
        mv.visitTypeInsn(CHECKCAST, JvmConstants.RECORD_TYPE);
        mv.visitIntInsn(BIPUSH, location);
        mv.visitMethodInsn(INVOKESPECIAL, JvmConstants.OPTION, JvmConstants.JVM_INIT_METHOD,
                           String.format("(L%s;I)V", JvmConstants.TYPE), false);
    }

    private List<String> getDefaultableNames(List<BIRNode.BIRAnnotationAttachment> annotAttachments) {
        List<String> defaultableNames = new ArrayList<>();
        int defaultableIndex = 0;
        for (BIRNode.BIRAnnotationAttachment attachment : annotAttachments) {
            if (attachment != null && attachment.annotTagRef.value.equals(JvmConstants.DEFAULTABLE_ARGS_ANOT_NAME)) {
                BIRNode.BIRAnnotationRecordValue
                        annotRecValue = (BIRNode.BIRAnnotationRecordValue) attachment.annotValues.get(0);
                Map<String, BIRNode.BIRAnnotationValue> annotFieldMap = annotRecValue.annotValueEntryMap;
                BIRNode.BIRAnnotationArrayValue annotArrayValue =
                        (BIRNode.BIRAnnotationArrayValue) annotFieldMap.get(JvmConstants.DEFAULTABLE_ARGS_ANOT_FIELD);
                for (BIRNode.BIRAnnotationValue entryOptional : annotArrayValue.annotArrayValue) {
                    BIRNode.BIRAnnotationLiteralValue argValue = (BIRNode.BIRAnnotationLiteralValue) entryOptional;
                    defaultableNames.add(defaultableIndex, (String) argValue.value);
                    defaultableIndex += 1;
                }
                break;
            }
        }
        return defaultableNames;
    }

    private void genReturn(MethodVisitor mv, BIRVarToJVMIndexMap indexMap, String futureVar) {
        // store future value
        mv.visitVarInsn(ALOAD, indexMap.get(futureVar));
        mv.visitFieldInsn(GETFIELD, JvmConstants.FUTURE_VALUE, "result", String.format("L%s;", JvmConstants.OBJECT));

        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.RUNTIME_UTILS, JvmConstants.HANDLE_RETURNED_ERROR_METHOD,
                           String.format("(L%s;)V", JvmConstants.OBJECT), false);
    }

    private void genSubmitToScheduler(String initClass, MethodVisitor mv, String lambdaName,
                                      String funcName, String futureVar) {
        JvmCodeGenUtil.createFunctionPointer(mv, initClass, lambdaName);

        // no parent strand
        mv.visitInsn(ACONST_NULL);

        BType anyType = symbolTable.anyType;
        jvmTypeGen.loadType(mv, anyType);
        MethodGenUtils.submitToScheduler(mv, initClass, funcName, asyncDataCollector);
        storeFuture(indexMap, mv, futureVar);
        mv.visitFieldInsn(GETFIELD, JvmConstants.FUTURE_VALUE, JvmConstants.STRAND,
                          String.format("L%s;", JvmConstants.STRAND_CLASS));
        mv.visitIntInsn(BIPUSH, 100);
        mv.visitTypeInsn(ANEWARRAY, JvmConstants.OBJECT);
        mv.visitFieldInsn(PUTFIELD, JvmConstants.STRAND_CLASS, MethodGenUtils.FRAMES,
                          String.format("[L%s;", JvmConstants.OBJECT));

        startScheduler(indexMap.get(SCHEDULER_VAR), mv);
        handleErrorFromFutureValue(mv, futureVar);
    }

    private void stopListeners(MethodVisitor mv, boolean isServiceEPAvailable) {
        mv.visitLdcInsn(isServiceEPAvailable);
        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.LAUNCH_UTILS, "stopListeners", "(Z)V", false);
    }

    private void generateExitRuntime(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.JAVA_RUNTIME, "getRuntime",
                           String.format("()L%s;", JvmConstants.JAVA_RUNTIME), false);
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKEVIRTUAL, JvmConstants.JAVA_RUNTIME, "exit", "(I)V", false);
    }

    private void handleErrorFromFutureValue(MethodVisitor mv, String futureVar) {
        mv.visitVarInsn(ALOAD, indexMap.get(futureVar));
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, JvmConstants.FUTURE_VALUE, JvmConstants.PANIC_FIELD,
                          String.format("L%s;", JvmConstants.THROWABLE));

        // handle any runtime errors
        Label labelIf = new Label();
        mv.visitJumpInsn(IFNULL, labelIf);
        mv.visitFieldInsn(GETFIELD, JvmConstants.FUTURE_VALUE, JvmConstants.PANIC_FIELD,
                          String.format("L%s;", JvmConstants.THROWABLE));
        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.RUNTIME_UTILS, JvmConstants.HANDLE_THROWABLE_METHOD,
                           String.format("(L%s;)V", JvmConstants.THROWABLE), false);
        mv.visitInsn(RETURN);
        mv.visitLabel(labelIf);
    }

    /**
     * Generate a lambda function to invoke ballerina main.
     *
     * @param userMainFunc ballerina main function
     * @param cw           class visitor
     * @param mainClass    main class that contains the user main
     */
    public void generateLambdaForMain(BIRNode.BIRFunction userMainFunc, ClassWriter cw, String mainClass) {
        BType returnType = userMainFunc.type.retType;

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, "$lambda$main$",
                                          String.format("([L%s;)L%s;", JvmConstants.OBJECT, JvmConstants.OBJECT), null,
                                          null);
        mv.visitCode();

        //load strand as first arg
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, JvmConstants.STRAND_CLASS);

        // load and cast param values
        List<BType> paramTypes = userMainFunc.type.paramTypes;

        int paramIndex = 1;
        for (BType pType : paramTypes) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitIntInsn(BIPUSH, paramIndex);
            mv.visitInsn(AALOAD);
            jvmCastGen.addUnboxInsn(mv, pType);
            paramIndex += 1;
        }

        mv.visitMethodInsn(INVOKESTATIC, mainClass, userMainFunc.name.value,
                           JvmCodeGenUtil.getMethodDesc(paramTypes, returnType), false);
        jvmCastGen.addBoxInsn(mv, returnType);
        MethodGenUtils.visitReturn(mv);
    }
}
