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

import org.ballerinalang.compiler.BLangCompilerException;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRBasicBlock;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRErrorEntry;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRFunction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Return;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.io.PrintStream;
import java.util.ArrayList;
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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.InstructionGenerator;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.generateVarLoad;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmInstructionGen.generateVarStore;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmLabelGen.LabelGenerator;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.BalToJVMIndexMap;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTerminatorGen.TerminatorGenerator;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.CatchIns;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.JErrorEntry;
import static org.wso2.ballerinalang.compiler.bir.model.BIRTerminator.Panic;


//import ballerina/io;
//import ballerina/jvm;
//import ballerina/bir;
//import ballerinax/java;

public class JvmErrorGen {
    static @Nilable
    BIRErrorEntry findErrorEntry(@Nilable List<BIRErrorEntry> errors, BIRBasicBlock currentBB) {
        for (BIRErrorEntry err : errors) {
            if (err instanceof BIRErrorEntry && err.endBB.id.value.equals(currentBB.id.value)) {
                return err;
            }
        }
        return null;
    }

    static void print(String message) {
        PrintStream errStream = getSystemErrorStream();
        errStream.println(message);
//       printToErrorStream(errStream, message);
    }

    ;

    public static PrintStream getSystemErrorStream() {
        return System.err;
    }

    static class ErrorHandlerGenerator {
        MethodVisitor mv;
        BalToJVMIndexMap indexMap;
        String currentPackageName;

        static void printStackTraceFromFutureValue(MethodVisitor mv, BalToJVMIndexMap indexMap) {
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
            mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_THROWABLE_METHOD, String.format("(L%s;)V", THROWABLE),
                    false);
            mv.visitInsn(RETURN);
            mv.visitLabel(labelIf);
        }

        ErrorHandlerGenerator(MethodVisitor mv, BalToJVMIndexMap indexMap, String currentPackageName) {
            this.mv = mv;
            this.indexMap = indexMap;
            this.currentPackageName = currentPackageName;
        }

        void genPanic(Panic panicTerm) {
            BIRVariableDcl varDcl = panicTerm.errorOp.variableDcl;
            int errorIndex = this.getJVMIndexOfVarRef(varDcl);
            generateVarLoad(this.mv, varDcl, this.currentPackageName, errorIndex);
            this.mv.visitInsn(ATHROW);
        }

        void generateTryCatch(BIRFunction func, String funcName, BIRBasicBlock currentBB,
                              InstructionGenerator instGen, TerminatorGenerator termGen, LabelGenerator labelGen) {
            @Nilable BIRErrorEntry nilableEE = findErrorEntry(func.errorTable, currentBB);
            if (nilableEE == null) {
                return;
            }
            BIRErrorEntry currentEE = (BIRErrorEntry) nilableEE;

            Label startLabel = labelGen.getLabel(funcName + currentEE.trapBB.id.value);
            Label endLabel = new Label();
            Label jumpLabel = new Label();


            this.mv.visitLabel(endLabel);
            this.mv.visitJumpInsn(GOTO, jumpLabel);
            if (currentEE instanceof JErrorEntry) {
                JErrorEntry jCurrentEE = ((JErrorEntry) currentEE);
                BIRVariableDcl retVarDcl = (BIRVariableDcl) currentEE.errorOp.variableDcl;
                int retIndex = this.indexMap.getIndex(retVarDcl);
                boolean exeptionExist = false;
                for (CatchIns catchIns : jCurrentEE.catchIns) {
                    if (ERROR_VALUE.equals(catchIns.errorClass)) {
                        exeptionExist = true;
                    }
                    Label errorValueLabel = new Label();
                    this.mv.visitTryCatchBlock(startLabel, endLabel, errorValueLabel, catchIns.errorClass);
                    this.mv.visitLabel(errorValueLabel);
                    this.mv.visitMethodInsn(INVOKESTATIC, BAL_ERRORS, "createInteropError", String.format("(L%s;)L%s;", THROWABLE, ERROR_VALUE), false);
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
                this.mv.visitMethodInsn(INVOKESTATIC, BAL_ERRORS, "createInteropError", String.format("(L%s;)L%s;", THROWABLE, ERROR_VALUE), false);
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

            BIRVariableDcl varDcl = (BIRVariableDcl) currentEE.errorOp.variableDcl;
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

    static class DiagnosticLogger {
        List<DiagnosticLog> errors = new ArrayList<>();

        int getErrorCount() {
            return this.errors.size();
        }

        void printErrors() {
            for (DiagnosticLog log : this.errors) {
                String fileName = log.pos.getSource().cUnitName;
                String orgName = log.module.org.value;
                String moduleName = log.module.name.value;

                String pkgIdStr;
                if (moduleName.equals(".") && orgName.equals("$anon")) {
                    pkgIdStr = ".";
                } else {
                    pkgIdStr = orgName + ":" + moduleName;
                }

                String positionStr;
                if (fileName.equals(".")) {
                    positionStr = String.format("%s:%s:%s", pkgIdStr, log.pos.sLine, log.pos.sCol);
                } else {
                    positionStr = String.format("%s:%s:%s:%s", pkgIdStr, fileName, log.pos.sLine, log.pos.sCol);
                }

                String errorStr;
                String detail = log.err.getCause() != null ? log.err.getCause().getMessage() : "";
                if (detail.equals("")) {
                    errorStr = String.format("error: %s: %s", positionStr, log.err.getMessage());
                } else {
                    errorStr = String.format("error: %s: %s %s", positionStr, log.err.getMessage(), detail);
                }
                print(errorStr);
            }
        }

        void logError(BLangCompilerException err, DiagnosticPos pos, BIRPackage module) {
            this.errors.add(new DiagnosticLog(err, pos, module));
        }
    }

//   public static printToErrorStream(Object receiver , Object message) {
//       PrintStream.
//    name:"println",
//    klass:"java/io/PrintStream",
//    paramTypes:["java.lang.String"]
//    }

//   public static /* = @java:Method exit(int status */) {
//    klass:"java/lang/System"
//} external;

    static class DiagnosticLog {
        BLangCompilerException err;
        DiagnosticPos pos;
        BIRPackage module;

        public DiagnosticLog(BLangCompilerException err, DiagnosticPos pos, BIRPackage module) {
            this.err = err;
            this.pos = pos;
            this.module = module;
        }

    }
}