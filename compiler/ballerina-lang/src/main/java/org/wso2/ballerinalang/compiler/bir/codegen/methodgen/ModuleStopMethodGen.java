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
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;

import java.util.Set;

import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_FUTURE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAIN_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_START_ATTEMPTED;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STOP_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.NO_OF_DEPENDANT_MODULES;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.START_ISOLATED_WORKER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.GET_STRAND_METADATA;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.HANDLE_FUTURE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.MODULE_STOP;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.SCHEDULE_CALL;

/**
 * Generates Jvm byte code for the stop method.
 *
 * @since 2.0.0
 */
public class ModuleStopMethodGen {
    private final JvmTypeGen jvmTypeGen;
    private final String strandMetadataClass;

    public ModuleStopMethodGen(JvmTypeGen jvmTypeGen, JvmConstantsGen jvmConstantsGen) {
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

        // create FP value
        generateMethodBody(mv, initClass, stopFuncName, asyncDataCollector);
        // handle any runtime errors
        genHandleRuntimeErrors(mv, labelIf);
    }

    private void generateMethodBody(MethodVisitor mv, String initClass, String stopFuncName,
                                    AsyncDataCollector asyncDataCollector) {
        // create FP value
        JvmCodeGenUtil.createFunctionPointer(mv, initClass, stopFuncName);
        // no parent strand
        mv.visitInsn(ACONST_NULL);
        jvmTypeGen.loadType(mv, new BNilType());
        String metaDataVarName = JvmCodeGenUtil.setAndGetStrandMetadataVarName(MAIN_METHOD, asyncDataCollector);
        mv.visitLdcInsn("stop");
        mv.visitFieldInsn(GETSTATIC, strandMetadataClass, metaDataVarName, GET_STRAND_METADATA);
        mv.visitInsn(ACONST_NULL);
        mv.visitIntInsn(BIPUSH, 1);
        mv.visitTypeInsn(ANEWARRAY, OBJECT);
        mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, START_ISOLATED_WORKER, SCHEDULE_CALL, false);
        mv.visitVarInsn(ASTORE, 1);
    }

    private void genHandleRuntimeErrors(MethodVisitor mv, Label labelJump) {
        // load future value
        mv.visitVarInsn(ALOAD, 1);
        // get future result and handle result
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_FUTURE_METHOD, HANDLE_FUTURE, false);
        mv.visitLabel(labelJump);
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
