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
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.bir.model.VarScope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.List;

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
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, MODULE_STOP, "()V", null, null);
        mv.visitCode();

        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();

        BIRNode.BIRVariableDcl argsVar = new BIRNode.BIRVariableDcl(symbolTable.anyType, new Name("schedulerVar"),
                                                                    VarScope.FUNCTION, VarKind.ARG);
        int schedulerIndex = indexMap.addToMapIfNotFoundAndGetIndex(argsVar);
        BIRNode.BIRVariableDcl futureVar = new BIRNode.BIRVariableDcl(symbolTable.anyType, new Name("futureVar"),
                                                                      VarScope.FUNCTION, VarKind.ARG);
        int futureIndex = indexMap.addToMapIfNotFoundAndGetIndex(futureVar);

        mv.visitTypeInsn(NEW, SCHEDULER);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKESPECIAL, SCHEDULER, JVM_INIT_METHOD, "(IZ)V", false);

        mv.visitVarInsn(ASTORE, schedulerIndex);


        PackageID currentModId = MethodGenUtils.packageToModuleId(module);
        String moduleInitClass = getModuleInitClassName(currentModId);
        String fullFuncName = MethodGenUtils.calculateModuleSpecialFuncName(currentModId,
                                                                            MethodGenUtils.STOP_FUNCTION_SUFFIX);

        scheduleStopMethod(mv, initClass, JvmCodeGenUtil.cleanupFunctionName(fullFuncName), schedulerIndex,
                           futureIndex, moduleInitClass, asyncDataCollector);
        int i = imprtMods.size() - 1;
        while (i >= 0) {
            PackageID id = imprtMods.get(i);
            i -= 1;
            fullFuncName = MethodGenUtils.calculateModuleSpecialFuncName(id, MethodGenUtils.STOP_FUNCTION_SUFFIX);
            moduleInitClass = getModuleInitClassName(id);
            scheduleStopMethod(mv, initClass, JvmCodeGenUtil.cleanupFunctionName(fullFuncName), schedulerIndex,
                               futureIndex, moduleInitClass, asyncDataCollector);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void scheduleStopMethod(MethodVisitor mv, String initClass, String stopFuncName,
                                    int schedulerIndex, int futureIndex, String moduleClass,
                                    AsyncDataCollector asyncDataCollector) {
        // Create a scheduler. A new scheduler is used here, to make the stop function to not to
        // depend/wait on whatever is being running on the background. eg: a busy loop in the main.
        mv.visitFieldInsn(GETSTATIC, moduleClass, MODULE_START_ATTEMPTED, "Z");
        Label labelIf = new Label();
        mv.visitJumpInsn(IFEQ, labelIf);
        MethodGenUtils.genArgs(mv, schedulerIndex);

        // create FP value
        String lambdaFuncName = "$lambda$" + stopFuncName;
        JvmCodeGenUtil.createFunctionPointer(mv, initClass, lambdaFuncName);

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

        mv.visitVarInsn(ALOAD, futureIndex);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, STRAND, String.format("L%s;", STRAND_CLASS));
        mv.visitFieldInsn(GETFIELD, STRAND_CLASS, "scheduler", String.format("L%s;", SCHEDULER));
        mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULER_START_METHOD, "()V", false);

        mv.visitVarInsn(ALOAD, futureIndex);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, String.format("L%s;", THROWABLE));

        // handle any runtime errors
        mv.visitJumpInsn(IFNULL, labelIf);
        mv.visitFieldInsn(GETSTATIC, moduleClass, MODULE_STARTED, "Z");
        mv.visitJumpInsn(IFEQ, labelIf);

        mv.visitVarInsn(ALOAD, futureIndex);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, String.format("L%s;", THROWABLE));
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_STOP_PANIC_METHOD, String.format("(L%s;)V", THROWABLE),
                           false);
        mv.visitLabel(labelIf);
    }

    private String getModuleInitClassName(PackageID id) {
        return JvmCodeGenUtil.getModuleLevelClassName(id.orgName.value, id.name.value, id.version.value,
                                                      MODULE_INIT_CLASS_NAME);
    }

}
