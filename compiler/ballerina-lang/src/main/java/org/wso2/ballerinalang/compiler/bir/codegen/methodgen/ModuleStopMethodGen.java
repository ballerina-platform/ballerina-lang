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
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;

import java.util.Set;

import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_RETURNED_ERROR_METHOD_WITHOUT_EXIT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_STOP_PANIC_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STARTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_START_ATTEMPTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STOP_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.NO_OF_DEPENDANT_MODULES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PANIC_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER_START_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STACK;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_THROWABLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_ERROR_RETURN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_STOP_PANIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.MODULE_STOP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.STACK_FRAMES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;

/**
 * Generates Jvm byte code for the stop method.
 *
 * @since 2.0.0
 */
public class ModuleStopMethodGen {
    public static final String FUTURE_VAR = "futureVar";
    public static final String ARR_VAR = "arrVar";
    private final BIRVarToJVMIndexMap indexMap;
    private final JvmTypeGen jvmTypeGen;
    private final String strandMetadataClass;

    public ModuleStopMethodGen(JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen) {
        indexMap = new BIRVarToJVMIndexMap(1);
        this.jvmTypeGen = jvmTypeGen;
        this.strandMetadataClass = jvmConstantsGen.getStrandMetadataConstantsClass();
    }

    public void generateExecutionStopMethod(ClassWriter cw, String initClass, BIRNode.BIRPackage module,
                                            AsyncDataCollector asyncDataCollector, Set<PackageID> immediateImports) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, MODULE_STOP_METHOD,
                MODULE_STOP, null, null);
        mv.visitCode();
        String moduleInitClass = getModuleInitClassName(module.packageID);
        String fullFuncName = MethodGenUtils.calculateLambdaStopFuncName(module.packageID);
        mv.visitFieldInsn(GETSTATIC, initClass, NO_OF_DEPENDANT_MODULES, "I");
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ISUB);
        mv.visitFieldInsn(PUTSTATIC, initClass, NO_OF_DEPENDANT_MODULES, "I");

        mv.visitFieldInsn(GETSTATIC, initClass, NO_OF_DEPENDANT_MODULES, "I");
        Label labelIf = new Label();
        mv.visitJumpInsn(IFLE, labelIf);
        mv.visitInsn(RETURN);
        mv.visitLabel(labelIf);

        scheduleStopLambda(mv, initClass, fullFuncName, moduleInitClass, asyncDataCollector);
        for (PackageID immediateImport : immediateImports) {
            moduleInitClass = getModuleInitClassName(immediateImport);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, moduleInitClass, MODULE_STOP_METHOD, MODULE_STOP, false);
        }

        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, MODULE_STOP_METHOD, initClass);
        mv.visitEnd();
    }

    private void scheduleStopLambda(MethodVisitor mv, String initClass, String stopFuncName, String moduleClass,
                                    AsyncDataCollector asyncDataCollector) {
        Label labelIf = createIfLabel(mv, moduleClass);
        mv.visitVarInsn(ALOAD, 0);
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
        jvmTypeGen.loadType(mv, BType.createNilType());
        MethodGenUtils.submitToScheduler(mv, this.strandMetadataClass, "stop", asyncDataCollector);
        mv.visitVarInsn(ASTORE, 1);

        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, STRAND, GET_STRAND);
        mv.visitTypeInsn(NEW, STACK);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, STACK, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
        mv.visitFieldInsn(PUTFIELD, STRAND_CLASS, MethodGenUtils.FRAMES, STACK_FRAMES);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULER_START_METHOD, VOID_METHOD_DESC, false);
    }

    private void genHandleRuntimeErrors(MethodVisitor mv, String moduleClass, Label labelJump) {
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, GET_THROWABLE);
        Label labelIf = new Label();
        mv.visitJumpInsn(IFNULL, labelIf);
        mv.visitFieldInsn(GETSTATIC, moduleClass, MODULE_STARTED, "Z");
        mv.visitJumpInsn(IFEQ, labelIf);

        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, GET_THROWABLE);
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_STOP_PANIC_METHOD, HANDLE_STOP_PANIC,
                           false);
        mv.visitJumpInsn(GOTO, labelJump);
        mv.visitLabel(labelIf);
        genHandleErrorReturn(mv);
        mv.visitLabel(labelJump);
    }

    private void genHandleErrorReturn(MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(GETFIELD , FUTURE_VALUE, "result", GET_OBJECT);
        mv.visitMethodInsn(INVOKESTATIC , RUNTIME_UTILS , HANDLE_RETURNED_ERROR_METHOD_WITHOUT_EXIT,
                HANDLE_ERROR_RETURN, false);
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
