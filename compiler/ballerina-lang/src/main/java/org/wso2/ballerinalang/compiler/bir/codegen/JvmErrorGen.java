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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.BIRVarToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.internal.LabelGenerator;
import org.wso2.ballerinalang.compiler.bir.codegen.model.CatchIns;
import org.wso2.ballerinalang.compiler.bir.codegen.model.JErrorEntry;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.CREATE_INTEROP_ERROR_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STACK_OVERFLOW_ERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.THROWABLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TRAP_ERROR_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmSignatures.CREATE_ERROR_FROM_THROWABLE;

/**
 * Error generator class used for holding errors and the index map.
 *
 * @since 1.2.0
 */
public class JvmErrorGen {

    private final MethodVisitor mv;
    private final BIRVarToJVMIndexMap indexMap;
    private final JvmInstructionGen jvmInstructionGen;

    public JvmErrorGen(MethodVisitor mv, BIRVarToJVMIndexMap indexMap, JvmInstructionGen jvmInstructionGen) {

        this.mv = mv;
        this.indexMap = indexMap;
        this.jvmInstructionGen = jvmInstructionGen;
    }

    private BIRNode.BIRErrorEntry findErrorEntry(List<BIRNode.BIRErrorEntry> errors, BIRNode.BIRBasicBlock currentBB) {

        for (BIRNode.BIRErrorEntry err : errors) {
            if (err != null && err.endBB.id.value.equals(currentBB.id.value)) {
                return err;
            }
        }
        return null;
    }

    void genPanic(BIRTerminator.Panic panicTerm) {
        BIRNode.BIRVariableDcl varDcl = panicTerm.errorOp.variableDcl;
        jvmInstructionGen.generateVarLoad(this.mv, varDcl);
        this.mv.visitTypeInsn(CHECKCAST, BERROR);
        this.mv.visitInsn(ATHROW);
    }

    public void generateTryCatch(BIRNode.BIRFunction func, String funcName, BIRNode.BIRBasicBlock currentBB,
                                 JvmTerminatorGen termGen, LabelGenerator labelGen, int channelMapVarIndex,
                                 int sendWorkerChannelNamesVar, int receiveWorkerChannelNamesVar, int localVarOffset) {

        BIRNode.BIRErrorEntry currentEE = findErrorEntry(func.errorTable, currentBB);
        if (currentEE == null) {
            return;
        }

        Label startLabel = labelGen.getLabel(funcName + currentEE.trapBB.id.value);
        Label endLabel = new Label();
        Label jumpLabel = new Label();

        this.mv.visitLabel(endLabel);
        this.mv.visitJumpInsn(GOTO, jumpLabel);
        if (currentEE instanceof JErrorEntry jCurrentEE) {
            BIRNode.BIRVariableDcl retVarDcl = currentEE.errorOp.variableDcl;
            boolean exeptionExist = false;
            if (!jCurrentEE.catchIns.isEmpty()) {
                int retIndex = this.indexMap.addIfNotExists(retVarDcl.name.value, retVarDcl.type);
                for (CatchIns catchIns : jCurrentEE.catchIns) {
                    if (ERROR_VALUE.equals(catchIns.errorClass)) {
                        exeptionExist = true;
                    }
                    Label errorValueLabel = new Label();
                    this.mv.visitTryCatchBlock(startLabel, endLabel, errorValueLabel, catchIns.errorClass);
                    this.mv.visitLabel(errorValueLabel);
                    this.mv.visitMethodInsn(INVOKESTATIC, ERROR_UTILS, CREATE_INTEROP_ERROR_METHOD,
                            CREATE_ERROR_FROM_THROWABLE, false);
                    jvmInstructionGen.generateVarStore(this.mv, retVarDcl);
                    termGen.genReturnTerm(retIndex, func, channelMapVarIndex, sendWorkerChannelNamesVar,
                            receiveWorkerChannelNamesVar, localVarOffset);
                    this.mv.visitJumpInsn(GOTO, jumpLabel);
                }
            }
            if (!exeptionExist) {
                Label errorValErrorLabel = new Label();
                this.mv.visitTryCatchBlock(startLabel, endLabel, errorValErrorLabel, ERROR_VALUE);

                this.mv.visitLabel(errorValErrorLabel);
                this.mv.visitInsn(ATHROW);
                this.mv.visitJumpInsn(GOTO, jumpLabel);
            }
            Label otherErrorLabel = new Label();
            Label sOErrorlabel = new Label();
            this.mv.visitTryCatchBlock(startLabel, endLabel, sOErrorlabel, STACK_OVERFLOW_ERROR);
            this.mv.visitTryCatchBlock(startLabel, endLabel, otherErrorLabel, THROWABLE);
            this.mv.visitLabel(sOErrorlabel);
            this.mv.visitMethodInsn(INVOKESTATIC, ERROR_UTILS, TRAP_ERROR_METHOD, CREATE_ERROR_FROM_THROWABLE,
                    false);
            this.mv.visitInsn(ATHROW);
            this.mv.visitJumpInsn(GOTO, jumpLabel);
            this.mv.visitLabel(otherErrorLabel);
            this.mv.visitMethodInsn(INVOKESTATIC, ERROR_UTILS, CREATE_INTEROP_ERROR_METHOD,
                    CREATE_ERROR_FROM_THROWABLE, false);
            this.mv.visitInsn(ATHROW);
            this.mv.visitLabel(jumpLabel);
            return;
        }

        Label errorValueLabel = new Label();
        Label otherErrorLabel = new Label();
        this.mv.visitTryCatchBlock(startLabel, endLabel, errorValueLabel, ERROR_VALUE);
        this.mv.visitTryCatchBlock(startLabel, endLabel, otherErrorLabel, STACK_OVERFLOW_ERROR);
        this.mv.visitLabel(errorValueLabel);

        BIRNode.BIRVariableDcl varDcl = currentEE.errorOp.variableDcl;
        int lhsIndex = this.indexMap.addIfNotExists(varDcl.name.value, varDcl.type);
        jvmInstructionGen.generateVarStore(this.mv, varDcl);

        // Clear block-local reference variables in the try block on exception paths.
        // When an exception is caught, variables declared in the protected region are
        // no longer needed. Clearing their JVM slots allows GC to collect them.
        genExceptionPathCleanup(func, currentEE);

        this.mv.visitJumpInsn(GOTO, jumpLabel);
        this.mv.visitLabel(otherErrorLabel);
        this.mv.visitMethodInsn(INVOKESTATIC, ERROR_UTILS, TRAP_ERROR_METHOD, CREATE_ERROR_FROM_THROWABLE, false);
        this.mv.visitVarInsn(ASTORE, lhsIndex);
        this.mv.visitLabel(jumpLabel);
    }

    /**
     * Clear block-local reference variables on exception paths. When an exception
     * is caught in a try block, variables declared in the protected region are no
     * longer needed. Clearing their JVM slots allows GC to collect them.
     *
     * <p>For each variable whose startBB is within the trap region [trapBB, endBB],
     * and whose endBB is at or after the catch handler, emit ACONST_NULL + ASTORE.
     */
    private void genExceptionPathCleanup(BIRNode.BIRFunction func, BIRNode.BIRErrorEntry errorEntry) {
        int trapBBIndex = func.basicBlocks.indexOf(errorEntry.trapBB);
        int endBBIndex = func.basicBlocks.indexOf(errorEntry.endBB);
        if (trapBBIndex < 0 || endBBIndex < 0) {
            return;
        }

        for (BIRNode.BIRVariableDcl localVar : func.localVars) {
            if (localVar.kind != VarKind.LOCAL || localVar.startBB == null || localVar.endBB == null) {
                continue;
            }
            BType bType = JvmCodeGenUtil.getImpliedType(localVar.type);
            if (!isReferenceType(bType)) {
                continue;
            }
            int index = this.indexMap.get(localVar.name.value);
            if (index == -1) {
                continue;
            }

            int startBBIndex = func.basicBlocks.indexOf(localVar.startBB);
            int varEndBBIndex = func.basicBlocks.indexOf(localVar.endBB);

            if (startBBIndex < 0 || varEndBBIndex < 0) {
                continue;
            }

            // Variable is in the try block if its startBB is within [trapBB, endBB]
            if (startBBIndex >= trapBBIndex && startBBIndex <= endBBIndex) {
                this.mv.visitInsn(ACONST_NULL);
                this.mv.visitVarInsn(ASTORE, index);
            }
        }
    }

    private boolean isReferenceType(BType bType) {
        bType = JvmCodeGenUtil.getImpliedType(bType);
        if (TypeTags.isStringTypeTag(bType.tag) || TypeTags.isXMLTypeTag(bType.tag)
                || TypeTags.REGEXP == bType.tag) {
            return true;
        }
        return switch (bType.tag) {
            case TypeTags.MAP, TypeTags.ARRAY, TypeTags.STREAM, TypeTags.TABLE, TypeTags.ERROR,
                 TypeTags.NIL, TypeTags.NEVER, TypeTags.ANY, TypeTags.ANYDATA, TypeTags.OBJECT,
                 TypeTags.DECIMAL, TypeTags.UNION, TypeTags.RECORD, TypeTags.TUPLE, TypeTags.FUTURE,
                 TypeTags.JSON, TypeTags.INVOKABLE, TypeTags.FINITE, TypeTags.HANDLE, TypeTags.TYPEDESC,
                 TypeTags.READONLY -> true;
            default -> false;
        };
    }
}
