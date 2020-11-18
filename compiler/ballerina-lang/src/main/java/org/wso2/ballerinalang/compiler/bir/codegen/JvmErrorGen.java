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
import org.wso2.ballerinalang.compiler.bir.codegen.interop.CatchIns;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JErrorEntry;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;

import java.util.List;

import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STACK_OVERFLOW_ERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.THROWABLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TRAP_ERROR_METHOD;

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
        int errorIndex = this.getJVMIndexOfVarRef(varDcl);
        jvmInstructionGen.generateVarLoad(this.mv, varDcl, errorIndex);
        this.mv.visitInsn(ATHROW);
    }

    public void generateTryCatch(BIRNode.BIRFunction func, String funcName, BIRNode.BIRBasicBlock currentBB,
                          JvmTerminatorGen termGen, LabelGenerator labelGen) {

        BIRNode.BIRErrorEntry currentEE = findErrorEntry(func.errorTable, currentBB);
        if (currentEE == null) {
            return;
        }

        Label startLabel = labelGen.getLabel(funcName + currentEE.trapBB.id.value);
        Label endLabel = new Label();
        Label jumpLabel = new Label();

        this.mv.visitLabel(endLabel);
        this.mv.visitJumpInsn(GOTO, jumpLabel);
        if (currentEE instanceof JErrorEntry) {
            JErrorEntry jCurrentEE = ((JErrorEntry) currentEE);
            BIRNode.BIRVariableDcl retVarDcl = currentEE.errorOp.variableDcl;
            int retIndex = this.indexMap.addToMapIfNotFoundAndGetIndex(retVarDcl);
            boolean exeptionExist = false;
            for (CatchIns catchIns : jCurrentEE.catchIns) {
                if (ERROR_VALUE.equals(catchIns.errorClass)) {
                    exeptionExist = true;
                }
                Label errorValueLabel = new Label();
                this.mv.visitTryCatchBlock(startLabel, endLabel, errorValueLabel, catchIns.errorClass);
                this.mv.visitLabel(errorValueLabel);
                this.mv.visitMethodInsn(INVOKESTATIC, ERROR_UTILS, "createInteropError",
                                        String.format("(L%s;)L%s;", THROWABLE, ERROR_VALUE), false);
                jvmInstructionGen.generateVarStore(this.mv, retVarDcl, retIndex);
                termGen.genReturnTerm(retIndex, func);
                this.mv.visitJumpInsn(GOTO, jumpLabel);
            }
            if (!exeptionExist) {
                Label errorValErrorLabel = new Label();
                this.mv.visitTryCatchBlock(startLabel, endLabel, errorValErrorLabel, ERROR_VALUE);

                this.mv.visitLabel(errorValErrorLabel);
                this.mv.visitInsn(ATHROW);
                this.mv.visitJumpInsn(GOTO, jumpLabel);
            }
            Label otherErrorLabel = new Label();
            this.mv.visitTryCatchBlock(startLabel, endLabel, otherErrorLabel, THROWABLE);

            this.mv.visitLabel(otherErrorLabel);
            this.mv.visitMethodInsn(INVOKESTATIC, ERROR_UTILS, "createInteropError",
                                    String.format("(L%s;)L%s;", THROWABLE, ERROR_VALUE),
                                    false);
            this.mv.visitInsn(ATHROW);
            this.mv.visitJumpInsn(GOTO, jumpLabel);
            this.mv.visitLabel(jumpLabel);
            return;
        }

        Label errorValueLabel = new Label();
        Label otherErrorLabel = new Label();
        this.mv.visitTryCatchBlock(startLabel, endLabel, errorValueLabel, ERROR_VALUE);
        this.mv.visitTryCatchBlock(startLabel, endLabel, otherErrorLabel, STACK_OVERFLOW_ERROR);
        this.mv.visitLabel(errorValueLabel);

        BIRNode.BIRVariableDcl varDcl = currentEE.errorOp.variableDcl;
        int lhsIndex = this.indexMap.addToMapIfNotFoundAndGetIndex(varDcl);
        jvmInstructionGen.generateVarStore(this.mv, varDcl, lhsIndex);
        this.mv.visitJumpInsn(GOTO, jumpLabel);
        this.mv.visitLabel(otherErrorLabel);
        this.mv.visitMethodInsn(INVOKESTATIC, ERROR_UTILS, TRAP_ERROR_METHOD,
                                String.format("(L%s;)L%s;", THROWABLE, ERROR_VALUE), false);
        this.mv.visitVarInsn(ASTORE, lhsIndex);
        this.mv.visitLabel(jumpLabel);
    }

    private int getJVMIndexOfVarRef(BIRNode.BIRVariableDcl varDcl) {
        return this.indexMap.addToMapIfNotFoundAndGetIndex(varDcl);
    }
}
