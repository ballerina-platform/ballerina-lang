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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.methodgen.MethodGenUtils;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;

import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
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
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V17;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CLASS_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_STOP_PANIC_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JAVA_THREAD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LAMBDA_PREFIX;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STOP_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PANIC_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PREDEFINED_TYPES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_REGISTRY_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_REGISTRY_VARIABLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER_START_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STACK;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_RUNTIME_REGISTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_THROWABLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_STOP_PANIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.INIT_RUNTIME_REGISTRY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LAMBDA_STOP_DYNAMIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.LOAD_NULL_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.MODULE_STOP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SET_STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.STACK_FRAMES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;

/**
 * Generate the thread for the addShutDownHook.
 *
 * @since 2.0.0
 */
public class ShutDownListenerGen {
    private final String strandMetadataClass;

    public ShutDownListenerGen(JvmConstantsGen jvmConstantsGen) {
        this.strandMetadataClass = jvmConstantsGen.getStrandMetadataConstantsClass();
    }

    void generateShutdownSignalListener(String initClass, Map<String, byte[]> jarEntries,
                                        AsyncDataCollector asyncDataCollector) {
        String innerClassName = initClass + "$SignalListener";
        ClassWriter cw = new BallerinaClassWriter(COMPUTE_FRAMES);
        cw.visit(V17, ACC_SUPER, innerClassName, null, JAVA_THREAD, null);
        FieldVisitor fv = cw.visitField(ACC_PRIVATE, RUNTIME_REGISTRY_VARIABLE,
                GET_RUNTIME_REGISTRY, null, null);
        fv.visitEnd();

        // create constructor
        genConstructor(innerClassName, cw);

        // implement run() method
        genRunMethod(initClass, innerClassName, cw, asyncDataCollector);

        cw.visitEnd();
        jarEntries.put(innerClassName + CLASS_FILE_SUFFIX, cw.toByteArray());
    }

    private void genConstructor(String innerClassName, ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, JVM_INIT_METHOD, INIT_RUNTIME_REGISTRY, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, JAVA_THREAD, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, innerClassName , RUNTIME_REGISTRY_VARIABLE,
                GET_RUNTIME_REGISTRY);
        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, JVM_INIT_METHOD, innerClassName);
        mv.visitEnd();
    }

    private void genRunMethod(String initClass, String innerClassName, ClassWriter cw,
                              AsyncDataCollector asyncDataCollector) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "run", VOID_METHOD_DESC, null, null);
        mv.visitCode();
        // Create a scheduler. A new scheduler is used here, to make the stop function to not
        // depend/wait on whatever is being running on the background. eg: a busy loop in the main.
        mv.visitTypeInsn(NEW, SCHEDULER);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ICONST_0);
        mv.visitMethodInsn(INVOKESPECIAL, SCHEDULER, JVM_INIT_METHOD, "(IZ)V", false);
        mv.visitVarInsn(ASTORE, 1); // Scheduler var1
        String lambdaName = generateStopDynamicLambdaBody(cw, initClass);
        generateCallStopDynamicLambda(mv, lambdaName, initClass, asyncDataCollector);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKESTATIC, initClass, MODULE_STOP_METHOD, MODULE_STOP, false);
        mv.visitInsn(RETURN);
        JvmCodeGenUtil.visitMaxStackForMethod(mv, "run", innerClassName);
        mv.visitEnd();
    }

    private String generateStopDynamicLambdaBody(ClassWriter cw, String initClass) {
        String lambdaName = LAMBDA_PREFIX + "stopdynamic";
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + ACC_STATIC, lambdaName, LAMBDA_STOP_DYNAMIC, null, null);
        mv.visitCode();
        MethodGenUtils.callSetDaemonStrand(mv);
        generateCallSchedulerStopDynamicListeners(mv, lambdaName, initClass);
        return lambdaName;
    }

    private void generateCallSchedulerStopDynamicListeners(MethodVisitor mv, String lambdaName, String initClass) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, RUNTIME_REGISTRY_CLASS);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitTypeInsn(CHECKCAST, STRAND_CLASS);
        mv.visitMethodInsn(INVOKEVIRTUAL, RUNTIME_REGISTRY_CLASS, "gracefulStop", SET_STRAND, false);
        mv.visitInsn(ACONST_NULL);
        MethodGenUtils.visitReturn(mv, lambdaName, initClass);
    }

    private void generateCallStopDynamicLambda(MethodVisitor mv, String lambdaName, String moduleInitClass,
                                               AsyncDataCollector asyncDataCollector) {
        addRuntimeRegistryAsParameter(mv, moduleInitClass + "$SignalListener");
        generateMethodBody(mv, moduleInitClass, lambdaName, asyncDataCollector);
        // handle any runtime errors
        Label labelIf = new Label();
        mv.visitVarInsn(ALOAD, 3);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, GET_THROWABLE);
        mv.visitJumpInsn(IFNULL, labelIf);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, GET_THROWABLE);
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_STOP_PANIC_METHOD, HANDLE_STOP_PANIC, false);
        mv.visitLabel(labelIf);
    }

    private void addRuntimeRegistryAsParameter(MethodVisitor mv, String innerClassName) {
        mv.visitIntInsn(BIPUSH, 2);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitInsn(ICONST_1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, innerClassName, RUNTIME_REGISTRY_VARIABLE, GET_RUNTIME_REGISTRY);
        mv.visitInsn(AASTORE);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
    }

    private void generateMethodBody(MethodVisitor mv, String initClass, String stopFuncName,
                                    AsyncDataCollector asyncDataCollector) {
        JvmCodeGenUtil.createFunctionPointer(mv, initClass + "$SignalListener", stopFuncName);
        mv.visitInsn(ACONST_NULL);
        mv.visitFieldInsn(GETSTATIC, PREDEFINED_TYPES, "TYPE_NULL", LOAD_NULL_TYPE);
        MethodGenUtils.submitToScheduler(mv, this.strandMetadataClass, "stop", asyncDataCollector);
        mv.visitVarInsn(ASTORE, 3);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, STRAND, GET_STRAND);
        mv.visitTypeInsn(NEW, STACK);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, STACK, JVM_INIT_METHOD, VOID_METHOD_DESC, false);
        mv.visitFieldInsn(PUTFIELD, STRAND_CLASS, MethodGenUtils.FRAMES, STACK_FRAMES);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULER_START_METHOD, VOID_METHOD_DESC, false);
    }
}
