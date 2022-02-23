/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.codegen.methodgen;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;

import java.util.List;

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
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SIPUSH;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BALLERINA_MAX_YIELD_DEPTH;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_STOP_PANIC_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LISTENER_REGISTRY_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STARTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_START_ATTEMPTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STOP_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PANIC_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER_START_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_THROWABLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_STOP_PANIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_LISTENER_REGISTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LAMBDA_STOP_DYNAMIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.PUT_FRAMES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_STRAND;

/**
 * Generates Jvm byte code for the stop method.
 *
 * @since 2.0.0
 */
public class ModuleStopMethodGen {
    public static final String SCHEDULER_VAR = "schedulerVar";
    public static final String FUTURE_VAR = "futureVar";
    public static final String ARR_VAR = "arrVar";
    private final SymbolTable symbolTable;
    private final BIRVarToJVMIndexMap indexMap;
    private final JvmTypeGen jvmTypeGen;

    public ModuleStopMethodGen(SymbolTable symbolTable, JvmTypeGen jvmTypeGen) {
        this.symbolTable = symbolTable;
        indexMap = new BIRVarToJVMIndexMap(1);
        this.jvmTypeGen = jvmTypeGen;
    }

    public void generateExecutionStopMethod(ClassWriter cw, String initClass, BIRNode.BIRPackage module,
                                            List<PackageID> imprtMods, AsyncDataCollector asyncDataCollector) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, MODULE_STOP_METHOD,
                                          INIT_LISTENER_REGISTRY, null, null);
        mv.visitCode();

        int schedulerIndex = indexMap.addIfNotExists(SCHEDULER_VAR, symbolTable.anyType);
        // Create a scheduler. A new scheduler is used here, to make the stop function to not to
        // depend/wait on whatever is being running on the background. eg: a busy loop in the main.
        mv.visitTypeInsn(NEW, SCHEDULER);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKESPECIAL, SCHEDULER, JVM_INIT_METHOD, "(IZ)V", false);
        mv.visitVarInsn(ASTORE, schedulerIndex);

        String moduleInitClass = getModuleInitClassName(module.packageID);
        String fullFuncName = MethodGenUtils.calculateLambdaStopFuncName(module.packageID);
        String lambdaName = generateStopDynamicListenerLambdaBody(cw);
        generateCallStopDynamicListenersLambda(mv, lambdaName, moduleInitClass, asyncDataCollector);
        scheduleStopLambda(mv, initClass, fullFuncName, moduleInitClass, asyncDataCollector);
        int i = imprtMods.size() - 1;
        while (i >= 0) {
            PackageID id = imprtMods.get(i);
            i -= 1;
            fullFuncName = MethodGenUtils.calculateLambdaStopFuncName(id);
            moduleInitClass = getModuleInitClassName(id);
            scheduleStopLambda(mv, initClass, fullFuncName, moduleInitClass, asyncDataCollector);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private String generateStopDynamicListenerLambdaBody(ClassWriter cw) {
        String lambdaName = "$lambda$stopdynamic";
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, lambdaName, LAMBDA_STOP_DYNAMIC, null, null);
        mv.visitCode();
        generateCallSchedulerStopDynamicListeners(mv);
        return lambdaName;
    }

    private void generateCallStopDynamicListenersLambda(MethodVisitor mv, String lambdaName, String moduleInitClass,
                                                        AsyncDataCollector asyncDataCollector) {
        addListenerRegistryAsParameter(mv);
        int futureIndex = indexMap.addIfNotExists(FUTURE_VAR, symbolTable.anyType);
        generateMethodBody(mv, moduleInitClass, lambdaName, asyncDataCollector);

        // handle any runtime errors
        Label labelIf = new Label();
        mv.visitVarInsn(ALOAD, futureIndex);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, GET_THROWABLE);
        mv.visitJumpInsn(IFNULL, labelIf);

        mv.visitVarInsn(ALOAD, futureIndex);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, GET_THROWABLE);
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_STOP_PANIC_METHOD, HANDLE_STOP_PANIC,
                           false);
        mv.visitLabel(labelIf);
    }

    private void generateCallSchedulerStopDynamicListeners(MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, LISTENER_REGISTRY_CLASS);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, STRAND_CLASS);
        mv.visitMethodInsn(INVOKEVIRTUAL, LISTENER_REGISTRY_CLASS, "stopListeners",
                           SET_STRAND, false);
        mv.visitInsn(ACONST_NULL);
        MethodGenUtils.visitReturn(mv);
    }

    private void addListenerRegistryAsParameter(MethodVisitor mv) {
        int arrIndex = indexMap.addIfNotExists(ARR_VAR, symbolTable.anyType);
        mv.visitIntInsn(BIPUSH, 2);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
        mv.visitVarInsn(ASTORE, arrIndex);
        mv.visitVarInsn(ALOAD, arrIndex);
        mv.visitInsn(ICONST_1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(AASTORE);
        mv.visitVarInsn(ALOAD, indexMap.get(SCHEDULER_VAR));
        mv.visitVarInsn(ALOAD, arrIndex);
    }

    private void scheduleStopLambda(MethodVisitor mv, String initClass, String stopFuncName, String moduleClass,
                                    AsyncDataCollector asyncDataCollector) {
        Label labelIf = createIfLabel(mv, moduleClass);
        mv.visitVarInsn(ALOAD, indexMap.get(SCHEDULER_VAR));
        mv.visitIntInsn(BIPUSH, 1);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);

        // create FP value
        generateMethodBody(mv, initClass, stopFuncName, asyncDataCollector);

        // handle any runtime errors
        genHandleRuntimeErrors(mv, moduleClass, labelIf);
    }

    private void generateMethodBody(MethodVisitor mv, String initClass, String stopFuncName,
                                    AsyncDataCollector asyncDataCollector) {
        // create FP value
        JvmCodeGenUtil.createFunctionPointer(mv, initClass, stopFuncName);

        // no parent strand
        mv.visitInsn(ACONST_NULL);
        jvmTypeGen.loadType(mv, new BNilType());
        MethodGenUtils.submitToScheduler(mv, initClass, "stop", asyncDataCollector);
        int futureIndex = indexMap.get(FUTURE_VAR);
        mv.visitVarInsn(ASTORE, futureIndex);

        mv.visitVarInsn(ALOAD, futureIndex);

        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, STRAND, GET_STRAND);
        mv.visitIntInsn(SIPUSH, BALLERINA_MAX_YIELD_DEPTH);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
        mv.visitFieldInsn(PUTFIELD, STRAND_CLASS, MethodGenUtils.FRAMES, PUT_FRAMES);
        int schedulerIndex = indexMap.get(SCHEDULER_VAR);
        mv.visitVarInsn(ALOAD, schedulerIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULER_START_METHOD, "()V", false);

    }


    private void genHandleRuntimeErrors(MethodVisitor mv, String moduleClass, Label labelIf) {
        int futureIndex = indexMap.get(FUTURE_VAR);
        mv.visitVarInsn(ALOAD, futureIndex);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, GET_THROWABLE);
        mv.visitJumpInsn(IFNULL, labelIf);
        mv.visitFieldInsn(GETSTATIC, moduleClass, MODULE_STARTED, "Z");
        mv.visitJumpInsn(IFEQ, labelIf);

        mv.visitVarInsn(ALOAD, futureIndex);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, GET_THROWABLE);
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_STOP_PANIC_METHOD, HANDLE_STOP_PANIC,
                           false);
        mv.visitLabel(labelIf);
    }

    private Label createIfLabel(MethodVisitor mv, String moduleClass) {
        mv.visitFieldInsn(GETSTATIC, moduleClass, MODULE_START_ATTEMPTED, "Z");
        Label labelIf = new Label();
        mv.visitJumpInsn(IFEQ, labelIf);
        return labelIf;
    }

    private String getModuleInitClassName(PackageID id) {
        return JvmCodeGenUtil.getModuleLevelClassName(id, MODULE_INIT_CLASS_NAME);
    }

}
