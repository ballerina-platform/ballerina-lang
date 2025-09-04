/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.bir.codegen.utils;

import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmErrorGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTerminatorGen;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.AsyncDataCollector;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LabelGenerator;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LazyLoadBirBasicBlock;
import org.wso2.ballerinalang.compiler.bir.codegen.split.JvmConstantsGen;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;

import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.V21;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JVM_STATIC_INIT_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.VALUE_VAR_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.VOID_METHOD_DESC;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.genMethodReturn;
import static org.wso2.ballerinalang.compiler.bir.codegen.utils.JvmCodeGenUtil.generateDiagnosticPos;

/**
 * The common functions used in lazy loading constructs generation.
 *
 * @since 2201.13.0
 */
public final class LazyLoadingCodeGenUtils {

    private LazyLoadingCodeGenUtils() {
    }

    public static void generateConstantsClassInit(ClassWriter cw, String constantsClass) {
        cw.visit(V21, ACC_PUBLIC | ACC_SUPER, constantsClass, null, JvmConstants.OBJECT, null);
        MethodVisitor methodVisitor =
                cw.visitMethod(ACC_PRIVATE, JvmConstants.JVM_INIT_METHOD, VOID_METHOD_DESC, null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, JvmConstants.OBJECT, JvmConstants.JVM_INIT_METHOD,
                VOID_METHOD_DESC, false);
        genMethodReturn(methodVisitor);
    }

    public static void genLazyLoadingClass(ClassWriter cw, String lazyLoadingClass, String descriptor) {
        cw.visit(V21, ACC_PUBLIC | ACC_SUPER, lazyLoadingClass, null, OBJECT, null);
        FieldVisitor fv = cw.visitField(ACC_PUBLIC + ACC_STATIC, VALUE_VAR_NAME, descriptor, null, null);
        fv.visitEnd();
        generateConstantsClassInit(cw, lazyLoadingClass);
    }

    public static void addDebugField(ClassWriter cw, String varName) {
        if (varName.startsWith("$")) {
            return;
        }
        FieldVisitor fv = cw.visitField(ACC_PRIVATE, varName, "B", null, null);
        fv.visitEnd();
    }

    public static void loadIdentifierValue(ClassWriter cw, String varName, BIRNode.BIRPackage module,
                                           Map<String, LazyLoadBirBasicBlock> lazyBBMap, JvmPackageGen jvmPackageGen,
                                           JvmTypeGen jvmTypeGen, JvmCastGen jvmCastGen,
                                           JvmConstantsGen jvmConstantsGen, AsyncDataCollector asyncDataCollector) {
        LazyLoadBirBasicBlock lazyBB = lazyBBMap.get(varName);
        if (lazyBB == null) {
            return;
        }
        // Initialize global value
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, JVM_STATIC_INIT_METHOD, VOID_METHOD_DESC, null, null);
        mv.visitCode();
        BIRVarToJVMIndexMap indexMap = new BIRVarToJVMIndexMap();
        JvmInstructionGen instructionGen = new JvmInstructionGen(mv, indexMap, module.packageID, jvmPackageGen,
                jvmTypeGen, jvmCastGen, jvmConstantsGen, asyncDataCollector);
        BIRTerminator.Call call = lazyBB.call;
        if (call != null) {
            JvmErrorGen errorGen = new JvmErrorGen(mv, indexMap, instructionGen);
            LabelGenerator labelGen = new LabelGenerator();
            PackageID packageID = module.packageID;
            JvmTerminatorGen termGen = new JvmTerminatorGen(mv, indexMap, labelGen, errorGen, packageID,
                    instructionGen, jvmPackageGen, jvmTypeGen, jvmCastGen, asyncDataCollector);
            JvmCodeGenUtil.generateDiagnosticPos(call.pos, mv);
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ASTORE, 1);
            termGen.genCall(call, call.calleePkg, -1);
            termGen.storeReturnFromCallIns(call.lhsOp != null ? call.lhsOp.variableDcl : null);
        }
        List<BIRNonTerminator> instructions = lazyBB.instructions;
        if (instructions != null) {
            for (BIRNonTerminator instruction : instructions) {
                generateDiagnosticPos(instruction.pos, mv);
                instructionGen.generateInstructions(-1, instruction);
            }
        }
        genMethodReturn(mv);
    }
}
