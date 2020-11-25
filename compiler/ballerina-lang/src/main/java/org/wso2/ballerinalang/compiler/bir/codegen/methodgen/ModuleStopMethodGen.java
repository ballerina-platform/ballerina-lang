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
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.util.Name;

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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_STOP_PANIC_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STARTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_START_ATTEMPTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STOP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PANIC_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER_START_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.THROWABLE;

/**
 * Generates Jvm byte code for the stop method.
 *
 * @since 2.0.0
 */
public class ModuleStopMethodGen {
    private final SymbolTable symbolTable;

    public ModuleStopMethodGen(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void generateExecutionStopMethod(ClassWriter cw, String initClass, BIRNode.BIRPackage module,
                                     List<PackageID> imprtMods, AsyncDataCollector asyncDataCollector) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, MODULE_STOP,
                                          String.format("(L%s;)V", JvmConstants.LISTENER_REGISTRY_CLASS), null, null);
        mv.visitCode();

        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap(1);

        BIRNode.BIRVariableDcl argsVar = new BIRNode.BIRVariableDcl(symbolTable.anyType, new Name("schedulerVar"),
                                                                    VarScope.FUNCTION, VarKind.ARG);
        int schedulerIndex = indexMap.addToMapIfNotFoundAndGetIndex(argsVar);
        BIRNode.BIRVariableDcl futureVar = new BIRNode.BIRVariableDcl(symbolTable.anyType, new Name("futureVar"),
                                                                      VarScope.FUNCTION, VarKind.ARG);
        int futureIndex = indexMap.addToMapIfNotFoundAndGetIndex(futureVar);
        // Create a scheduler. A new scheduler is used here, to make the stop function to not to
        // depend/wait on whatever is being running on the background. eg: a busy loop in the main.
        mv.visitTypeInsn(NEW, SCHEDULER);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKESPECIAL, SCHEDULER, JVM_INIT_METHOD, "(IZ)V", false);
        mv.visitVarInsn(ASTORE, schedulerIndex);

        PackageID currentModId = MethodGenUtils.packageToModuleId(module);
        String moduleInitClass = getModuleInitClassName(currentModId);
        String fullFuncName = MethodGenUtils.calculateLambdaStopFuncName(currentModId);
        String lambdaName = generateStopDynamicListenerLambdaBody(cw);
        generateCallStopDynamicListenersLambda(mv, lambdaName, moduleInitClass, futureIndex, asyncDataCollector,
                                               schedulerIndex, indexMap);
        scheduleStopLambda(mv, initClass, fullFuncName, schedulerIndex, futureIndex, moduleInitClass,
                           asyncDataCollector);
        int i = imprtMods.size() - 1;
        while (i >= 0) {
            PackageID id = imprtMods.get(i);
            i -= 1;
            fullFuncName = MethodGenUtils.calculateLambdaStopFuncName(id);
            moduleInitClass = getModuleInitClassName(id);
            scheduleStopLambda(mv, initClass, fullFuncName, schedulerIndex, futureIndex, moduleInitClass,
                               asyncDataCollector);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private String generateStopDynamicListenerLambdaBody(ClassWriter cw) {
        String lambdaName = "$lambda$stopdynamic";
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, lambdaName, String.format(
                "([L%s;)L%s;", OBJECT, OBJECT), null, null);
        mv.visitCode();
        generateCallSchedulerStopDynamicListeners(mv);
        return lambdaName;
    }

    private void generateCallStopDynamicListenersLambda(MethodVisitor mv, String lambdaName, String moduleInitClass,
                                                        int futureIndex, AsyncDataCollector asyncDataCollector,
                                                        int schedulerVarIndex, BIRVarToJVMIndexMap indexMap) {
        BIRNode.BIRVariableDcl arrVar = new BIRNode.BIRVariableDcl(symbolTable.anyType, new Name("arrVar"),
                                                                   VarScope.FUNCTION, VarKind.ARG);
        int arrIndex = indexMap.addToMapIfNotFoundAndGetIndex(arrVar);
        addListenerRegistryAsParameter(mv, schedulerVarIndex, arrIndex);
        generateMethodBody(mv, moduleInitClass, lambdaName, futureIndex, asyncDataCollector, schedulerVarIndex);

        // handle any runtime errors
        Label labelIf = new Label();
        mv.visitVarInsn(ALOAD, futureIndex);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, String.format("L%s;", THROWABLE));
        mv.visitJumpInsn(IFNULL, labelIf);

        mv.visitVarInsn(ALOAD, futureIndex);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, String.format("L%s;", THROWABLE));
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_STOP_PANIC_METHOD, String.format("(L%s;)V", THROWABLE),
                           false);
        mv.visitLabel(labelIf);
    }

    private void generateCallSchedulerStopDynamicListeners(MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, JvmConstants.LISTENER_REGISTRY_CLASS);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, STRAND_CLASS);
        mv.visitMethodInsn(INVOKEVIRTUAL, JvmConstants.LISTENER_REGISTRY_CLASS, "stopListeners",
                           String.format("(L%s;)V", STRAND_CLASS), false);
        mv.visitInsn(ACONST_NULL);
        MethodGenUtils.visitReturn(mv);
    }

    private void addListenerRegistryAsParameter(MethodVisitor mv, int schedulerIndex, int arrIndex) {
        mv.visitIntInsn(BIPUSH, 2);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
        mv.visitVarInsn(ASTORE, arrIndex);
        mv.visitVarInsn(ALOAD, arrIndex);
        mv.visitInsn(ICONST_1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(AASTORE);
        mv.visitVarInsn(ALOAD, schedulerIndex);
        mv.visitVarInsn(ALOAD, arrIndex);
    }

    private void scheduleStopLambda(MethodVisitor mv, String initClass, String stopFuncName,
                                    int schedulerIndex, int futureIndex, String moduleClass,
                                    AsyncDataCollector asyncDataCollector) {
        Label labelIf = createIfLabel(mv, moduleClass);
        MethodGenUtils.genArgs(mv, schedulerIndex);

        // create FP value
        generateMethodBody(mv, initClass, stopFuncName, futureIndex, asyncDataCollector, schedulerIndex);

        // handle any runtime errors
        genHandleRuntimeErrors(mv, futureIndex, moduleClass, labelIf);
    }

    private void generateMethodBody(MethodVisitor mv, String initClass, String stopFuncName, int futureIndex,
                                    AsyncDataCollector asyncDataCollector, int schedulerIndex) {
        // create FP value
        JvmCodeGenUtil.createFunctionPointer(mv, initClass, stopFuncName);

        // no parent strand
        mv.visitInsn(ACONST_NULL);
        JvmTypeGen.loadType(mv, new BNilType());
        MethodGenUtils.submitToScheduler(mv, initClass, "stop", asyncDataCollector);
        mv.visitVarInsn(ASTORE, futureIndex);

        mv.visitVarInsn(ALOAD, futureIndex);

        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, STRAND, String.format("L%s;", STRAND_CLASS));
        mv.visitIntInsn(BIPUSH, 100);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
        mv.visitFieldInsn(PUTFIELD, STRAND_CLASS, MethodGenUtils.FRAMES, String.format("[L%s;", OBJECT));

        mv.visitVarInsn(ALOAD, schedulerIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULER_START_METHOD, "()V", false);

    }


    private void genHandleRuntimeErrors(MethodVisitor mv, int futureIndex, String moduleClass, Label labelIf) {
        mv.visitVarInsn(ALOAD, futureIndex);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, String.format("L%s;", THROWABLE));
        mv.visitJumpInsn(IFNULL, labelIf);
        mv.visitFieldInsn(GETSTATIC, moduleClass, MODULE_STARTED, "Z");
        mv.visitJumpInsn(IFEQ, labelIf);

        mv.visitVarInsn(ALOAD, futureIndex);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, String.format("L%s;", THROWABLE));
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_STOP_PANIC_METHOD, String.format("(L%s;)V", THROWABLE),
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
        return JvmCodeGenUtil.getModuleLevelClassName(id.orgName.value, id.name.value, id.version.value,
                                                      MODULE_INIT_CLASS_NAME);
    }

}
