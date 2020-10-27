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
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
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

/**
 * Generates Jvm byte code for the main method.
 *
 * @since 2.0.0
 */
public class MainMethodGen {

    private final SymbolTable symbolTable;

    public MainMethodGen(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void generateMainMethod(BIRNode.BIRFunction userMainFunc, ClassWriter cw, BIRNode.BIRPackage pkg,
                            String initClass, boolean serviceEPAvailable, AsyncDataCollector asyncDataCollector) {

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

        // set system properties
        initConfigurations(mv);
        // start all listeners
        startListeners(mv, serviceEPAvailable);

        // register a shutdown hook to call package stop() method.
        registerShutdownListener(mv, initClass);

        // add main string[] args param first
        BIRNode.BIRVariableDcl argsVar = new BIRNode.BIRVariableDcl(symbolTable.anyType, new Name("argsdummy"),
                                                                    VarScope.FUNCTION,
                                                                    VarKind.ARG);
        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        indexMap.addToMapIfNotFoundAndGetIndex(argsVar);

        int schedulerVarIndex = getSchedulerVarIndex(mv, indexMap);

        if (MethodGenUtils.hasInitFunction(pkg)) {
            generateMethodCall(initClass, asyncDataCollector, mv, indexMap, schedulerVarIndex, JvmConstants.MODULE_INIT,
                               MethodGenUtils.INIT_FUNCTION_SUFFIX, "initdummy");
        }

        if (userMainFunc != null) {
            generateUserMainFunctionCall(userMainFunc, initClass, asyncDataCollector, mv, indexMap, schedulerVarIndex);
        }

        if (MethodGenUtils.hasInitFunction(pkg)) {
            scheduleStartMethod(mv, initClass, serviceEPAvailable, indexMap, schedulerVarIndex,
                                asyncDataCollector);
        }

        // stop all listeners
        stopListeners(mv, serviceEPAvailable);
        if (!serviceEPAvailable) {
            mv.visitMethodInsn(INVOKESTATIC, JvmConstants.JAVA_RUNTIME, "getRuntime",
                               String.format("()L%s;", JvmConstants.JAVA_RUNTIME), false);
            mv.visitInsn(ICONST_0);
            mv.visitMethodInsn(INVOKEVIRTUAL, JvmConstants.JAVA_RUNTIME, "exit", "(I)V", false);
        }

        mv.visitLabel(tryCatchEnd);
        mv.visitInsn(RETURN);
        mv.visitLabel(tryCatchHandle);
        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.RUNTIME_UTILS, JvmConstants.HANDLE_THROWABLE_METHOD,
                           String.format("(L%s;)V", JvmConstants.THROWABLE), false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
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

    private void initConfigurations(MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.LAUNCH_UTILS,
                           "initConfigurations",
                           String.format("([L%s;)[L%s;", JvmConstants.STRING_VALUE, JvmConstants.STRING_VALUE), false);
        mv.visitVarInsn(ASTORE, 0);
    }

    private void startListeners(MethodVisitor mv, boolean isServiceEPAvailable) {
        mv.visitLdcInsn(isServiceEPAvailable);
        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.LAUNCH_UTILS, "startListeners", "(Z)V", false);
    }

    private void registerShutdownListener(MethodVisitor mv, String initClass) {
        String shutdownClassName = initClass + "$SignalListener";
        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.JAVA_RUNTIME, "getRuntime",
                           String.format("()L%s;", JvmConstants.JAVA_RUNTIME), false);
        mv.visitTypeInsn(NEW, shutdownClassName);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, shutdownClassName, JvmConstants.JVM_INIT_METHOD, "()V", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, JvmConstants.JAVA_RUNTIME, "addShutdownHook",
                           String.format("(L%s;)V", JvmConstants.JAVA_THREAD),
                           false);
    }

    private int getSchedulerVarIndex(MethodVisitor mv, BIRVarToJVMIndexMap indexMap) {
        mv.visitTypeInsn(NEW, JvmConstants.SCHEDULER);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKESPECIAL, JvmConstants.SCHEDULER, JvmConstants.JVM_INIT_METHOD, "(Z)V", false);
        BIRNode.BIRVariableDcl schedulerVar = new BIRNode.BIRVariableDcl(
                symbolTable.anyType, new Name("schedulerdummy"), VarScope.FUNCTION, VarKind.ARG);
        int schedulerVarIndex = indexMap.addToMapIfNotFoundAndGetIndex(schedulerVar);
        mv.visitVarInsn(ASTORE, schedulerVarIndex);
        return schedulerVarIndex;
    }

    private void scheduleStartMethod(MethodVisitor mv, String initClass, boolean serviceEPAvailable,
                                     BIRVarToJVMIndexMap indexMap, int schedulerVarIndex,
                                     AsyncDataCollector asyncDataCollector) {
        generateMethodCall(initClass, asyncDataCollector, mv, indexMap, schedulerVarIndex, JvmConstants.MODULE_START,
                           "start",
                           "startdummy");
        // need to set immortal=true and start the scheduler again
        if (serviceEPAvailable) {
            mv.visitVarInsn(ALOAD, schedulerVarIndex);
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_1);
            mv.visitFieldInsn(PUTFIELD, JvmConstants.SCHEDULER, "immortal", "Z");

            mv.visitMethodInsn(INVOKEVIRTUAL, JvmConstants.SCHEDULER, JvmConstants.SCHEDULER_START_METHOD, "()V",
                               false);
        }
    }

    private void generateMethodCall(String initClass, AsyncDataCollector asyncDataCollector, MethodVisitor mv,
                                    BIRVarToJVMIndexMap indexMap, int schedulerVarIndex, String moduleInit,
                                    String funcName, String dummy) {
        MethodGenUtils.genArgs(mv, schedulerVarIndex);
        genSubmitToScheduler(initClass, asyncDataCollector, mv, String.format("$lambda$%s$", moduleInit),
                             funcName);
        genReturn(mv, indexMap, dummy);
    }

    private void generateUserMainFunctionCall(BIRNode.BIRFunction userMainFunc, String initClass,
                                              AsyncDataCollector asyncDataCollector, MethodVisitor mv,
                                              BIRVarToJVMIndexMap indexMap, int schedulerVarIndex) {
        mv.visitVarInsn(ALOAD, schedulerVarIndex);
        loadCLIArgsForMain(mv, new ArrayList<>(userMainFunc.parameters.keySet()), userMainFunc.restParam != null,
                           userMainFunc.annotAttachments);

        // invoke the user's main method
        genSubmitToScheduler(initClass, asyncDataCollector, mv, "$lambda$main$", "main");

        // At this point we are done executing all the functions including asyncs
        boolean isVoidFunction = userMainFunc.type.retType.tag == TypeTags.NIL;
        if (!isVoidFunction) {
            genReturn(mv, indexMap, "dummy");
        }
    }

    private void loadCLIArgsForMain(MethodVisitor mv, List<BIRNode.BIRFunctionParameter> params,
                                    boolean hasRestParam,
                                    List<BIRNode.BIRAnnotationAttachment> annotAttachments) {
        // get defaultable arg names from function annotation
        List<String> defaultableNames = getDefaultableNames(annotAttachments);
        // create function info array
        createFunctionInfoArray(mv, params, defaultableNames);
        // load string[] that got parsed into to java main
        loadStrings(mv, hasRestParam);
        // invoke ArgumentParser.extractEntryFuncArgs()
        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.ARGUMENT_PARSER, "extractEntryFuncArgs",
                           String.format("([L%s$ParamInfo;[L%s;Z)[L%s;", JvmConstants.RUNTIME_UTILS,
                                         JvmConstants.STRING_VALUE, JvmConstants.OBJECT), false);
    }

    private void loadStrings(MethodVisitor mv, boolean hasRestParam) {
        mv.visitVarInsn(ALOAD, 0);
        if (hasRestParam) {
            mv.visitInsn(ICONST_1);
        } else {
            mv.visitInsn(ICONST_0);
        }
    }

    private void createFunctionInfoArray(MethodVisitor mv, List<BIRNode.BIRFunctionParameter> params,
                                         List<String> defaultableNames) {
        mv.visitIntInsn(BIPUSH, params.size());
        mv.visitTypeInsn(ANEWARRAY, String.format("%s$ParamInfo", JvmConstants.RUNTIME_UTILS));
        int index = 0;
        int defaultableIndex = 0;
        for (BIRNode.BIRFunctionParameter param : params) {
            mv.visitInsn(DUP);
            mv.visitIntInsn(BIPUSH, index);
            index += 1;
            mv.visitTypeInsn(NEW, String.format("%s$ParamInfo", JvmConstants.RUNTIME_UTILS));
            mv.visitInsn(DUP);
            if (param != null) {
                if (param.hasDefaultExpr) {
                    mv.visitInsn(ICONST_1);
                } else {
                    mv.visitInsn(ICONST_0);
                }
                mv.visitLdcInsn(defaultableNames.get(defaultableIndex));
                defaultableIndex += 1;
                JvmTypeGen.loadType(mv, param.type);
            }
            mv.visitMethodInsn(INVOKESPECIAL, String.format("%s$ParamInfo", JvmConstants.RUNTIME_UTILS),
                               JvmConstants.JVM_INIT_METHOD,
                               String.format("(ZL%s;L%s;)V", JvmConstants.STRING_VALUE, JvmConstants.TYPE), false);
            mv.visitInsn(AASTORE);
        }
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

    private void genReturn(MethodVisitor mv, BIRVarToJVMIndexMap indexMap, String dummy) {
        // store future value
        BIRNode.BIRVariableDcl futureVar = new BIRNode.BIRVariableDcl(symbolTable.anyType, new Name(dummy),
                                                                      VarScope.FUNCTION, VarKind.ARG);
        int futureVarIndex = indexMap.addToMapIfNotFoundAndGetIndex(futureVar);
        mv.visitVarInsn(ASTORE, futureVarIndex);
        mv.visitVarInsn(ALOAD, futureVarIndex);
        mv.visitFieldInsn(GETFIELD, JvmConstants.FUTURE_VALUE, "result", String.format("L%s;", JvmConstants.OBJECT));

        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.RUNTIME_UTILS, JvmConstants.HANDLE_RETURNED_ERROR_METHOD,
                           String.format("(L%s;)V", JvmConstants.OBJECT), false);
    }

    private void genSubmitToScheduler(String initClass, AsyncDataCollector asyncDataCollector, MethodVisitor mv,
                                      String lambdaName, String funcName) {
        JvmCodeGenUtil.createFunctionPointer(mv, initClass, lambdaName);

        // no parent strand
        mv.visitInsn(ACONST_NULL);

        //submit to the scheduler
        BType anyType = symbolTable.anyType;
        JvmTypeGen.loadType(mv, anyType);
        MethodGenUtils.submitToScheduler(mv, initClass, funcName, asyncDataCollector);
        mv.visitInsn(DUP);

        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, JvmConstants.FUTURE_VALUE, JvmConstants.STRAND,
                          String.format("L%s;", JvmConstants.STRAND_CLASS));
        mv.visitIntInsn(BIPUSH, 100);
        mv.visitTypeInsn(ANEWARRAY, JvmConstants.OBJECT);
        mv.visitFieldInsn(PUTFIELD, JvmConstants.STRAND_CLASS, MethodGenUtils.FRAMES,
                          String.format("[L%s;", JvmConstants.OBJECT));
        handleErrorFromFutureValue(mv);
    }

    private void stopListeners(MethodVisitor mv, boolean isServiceEPAvailable) {
        mv.visitLdcInsn(isServiceEPAvailable);
        mv.visitMethodInsn(INVOKESTATIC, JvmConstants.LAUNCH_UTILS, "stopListeners", "(Z)V", false);
    }

    private void handleErrorFromFutureValue(MethodVisitor mv) {
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, JvmConstants.FUTURE_VALUE, JvmConstants.STRAND,
                          String.format("L%s;", JvmConstants.STRAND_CLASS));
        mv.visitFieldInsn(GETFIELD, JvmConstants.STRAND_CLASS, "scheduler",
                          String.format("L%s;", JvmConstants.SCHEDULER));
        mv.visitMethodInsn(INVOKEVIRTUAL, JvmConstants.SCHEDULER, JvmConstants.SCHEDULER_START_METHOD, "()V", false);
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
            JvmCastGen.addUnboxInsn(mv, pType);
            paramIndex += 1;
        }

        mv.visitMethodInsn(INVOKESTATIC, mainClass, userMainFunc.name.value,
                           JvmCodeGenUtil.getMethodDesc(paramTypes, returnType), false);
        JvmCastGen.addBoxInsn(mv, returnType);
        MethodGenUtils.visitReturn(mv);
    }
}
