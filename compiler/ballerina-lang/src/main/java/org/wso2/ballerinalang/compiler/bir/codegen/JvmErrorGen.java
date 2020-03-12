/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.codegen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.InstructionGenerator;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmLabelGen.LabelGenerator;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.BalToJVMIndexMap;
import org.wso2.ballerinalang.compiler.bir.codegen.JvmTerminatorGen.TerminatorGenerator;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.CatchIns;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.JErrorEntry;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRErrorEntry;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Panic;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Return;

import java.util.List;

import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BAL_ERRORS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUTURE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.HANDLE_THROWABLE_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.PANIC_FIELD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.RUNTIME_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SCHEDULER_START_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STACK_OVERFLOW_ERROR;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.THROWABLE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TRAP_ERROR_METHOD;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.generateVarLoad;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.generateVarStore;

/**
 * Error generation related methods for JVM bytecode generation.
 *
 * @since 1.2.0
 */
public class JvmErrorGen {

    private static @Nilable
    BIRErrorEntry findErrorEntry(@Nilable List<BIRErrorEntry> errors, BIRBasicBlock currentBB) {

        for (BIRErrorEntry err : errors) {
            if (err != null && err.endBB.id.value.equals(currentBB.id.value)) {
                return err;
            }
        }
        return null;
    }

    /**
     * Error handler generator class used for holding errors and the index map.
     *
     * @since 1.2.0
     */
    public static class ErrorHandlerGenerator {

        MethodVisitor mv;
        BalToJVMIndexMap indexMap;
        String currentPackageName;

        public ErrorHandlerGenerator(MethodVisitor mv, BalToJVMIndexMap indexMap, String currentPackageName) {

            this.mv = mv;
            this.indexMap = indexMap;
            this.currentPackageName = currentPackageName;
        }

        void printStackTraceFromFutureValue(MethodVisitor mv, BalToJVMIndexMap indexMap) {

            mv.visitInsn(DUP);
            mv.visitInsn(DUP);
            mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "strand", String.format("L%s;", STRAND));
            mv.visitFieldInsn(GETFIELD, STRAND, "scheduler", String.format("L%s;", SCHEDULER));
            mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULER_START_METHOD, "()V", false);
            mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, String.format("L%s;", THROWABLE));

            // handle any runtime errors
            Label labelIf = new Label();
            mv.visitJumpInsn(IFNULL, labelIf);
            mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, String.format("L%s;", THROWABLE));
            mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_THROWABLE_METHOD,
                    String.format("(L%s;)V", THROWABLE), false);
            mv.visitInsn(RETURN);
            mv.visitLabel(labelIf);
        }

        void genPanic(Panic panicTerm) {

            BIRVariableDcl varDcl = panicTerm.errorOp.variableDcl;
            int errorIndex = this.getJVMIndexOfVarRef(varDcl);
            generateVarLoad(this.mv, varDcl, this.currentPackageName, errorIndex);
            this.mv.visitInsn(ATHROW);
        }

        void generateTryCatch(BIRFunction func, String funcName, BIRBasicBlock currentBB,
                              InstructionGenerator instGen, TerminatorGenerator termGen, LabelGenerator labelGen) {

            @Nilable BIRErrorEntry currentEE = findErrorEntry(func.errorTable, currentBB);
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
                BIRVariableDcl retVarDcl = currentEE.errorOp.variableDcl;
                int retIndex = this.indexMap.getIndex(retVarDcl);
                boolean exeptionExist = false;
                for (CatchIns catchIns : jCurrentEE.catchIns) {
                    if (ERROR_VALUE.equals(catchIns.errorClass)) {
                        exeptionExist = true;
                    }
                    Label errorValueLabel = new Label();
                    this.mv.visitTryCatchBlock(startLabel, endLabel, errorValueLabel, catchIns.errorClass);
                    this.mv.visitLabel(errorValueLabel);
                    this.mv.visitMethodInsn(INVOKESTATIC, BAL_ERRORS, "createInteropError", String.format("(L%s;)L%s;",
                            THROWABLE, ERROR_VALUE), false);
                    generateVarStore(this.mv, retVarDcl, this.currentPackageName, retIndex);
                    Return term = catchIns.term;
                    termGen.genReturnTerm(term, retIndex, func, false, -1);
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
                this.mv.visitMethodInsn(INVOKESTATIC, BAL_ERRORS, "createInteropError", String.format("(L%s;)L%s;",
                        THROWABLE, ERROR_VALUE), false);
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

            BIRVariableDcl varDcl = currentEE.errorOp.variableDcl;
            int lhsIndex = this.indexMap.getIndex(varDcl);
            generateVarStore(this.mv, varDcl, this.currentPackageName, lhsIndex);
            this.mv.visitJumpInsn(GOTO, jumpLabel);
            this.mv.visitLabel(otherErrorLabel);
            this.mv.visitMethodInsn(INVOKESTATIC, BAL_ERRORS, TRAP_ERROR_METHOD,
                    String.format("(L%s;)L%s;", THROWABLE, ERROR_VALUE), false);
            this.mv.visitVarInsn(ASTORE, lhsIndex);
            this.mv.visitLabel(jumpLabel);
        }

        int getJVMIndexOfVarRef(BIRVariableDcl varDcl) {

            return this.indexMap.getIndex(varDcl);
        }
    }
}
