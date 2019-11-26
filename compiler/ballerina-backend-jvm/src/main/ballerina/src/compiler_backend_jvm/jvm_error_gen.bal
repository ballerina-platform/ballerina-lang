// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;
import ballerina/jvm;
import ballerina/bir;
import ballerinax/java;

type ErrorHandlerGenerator object {
    jvm:MethodVisitor mv;
    BalToJVMIndexMap indexMap;
    string currentPackageName;

    function __init(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, string currentPackageName) {
        self.mv = mv;
        self.indexMap = indexMap;
        self.currentPackageName = currentPackageName;
    }

    function genPanic(bir:Panic panicTerm) {
        var varDcl = panicTerm.errorOp.variableDcl;
        int errorIndex = self.getJVMIndexOfVarRef(varDcl);
        generateVarLoad(self.mv, varDcl, self.currentPackageName, errorIndex);
        self.mv.visitInsn(ATHROW);
    }

    function generateTryInsForTrap(bir:ErrorEntry currentEE, string previousTargetBB, jvm:Label startLabel, jvm:Label jumpLabel) {
        if (currentEE is JErrorEntry) {
            self.mv.visitLabel(startLabel);
            return;
        }
        var varDcl = <bir:VariableDcl>currentEE.errorOp.variableDcl;
        int lhsIndex = self.getJVMIndexOfVarRef(varDcl);
        // Handle cases where the same variable used to trap multiple expressions with single trap statement.
        // Here we will check whether result error variable value and if it is null, we will skip the execution of
        // rest of the expressions trapped by error variable.
        if (previousTargetBB == currentEE.targetBB.id.value) {
            generateVarLoad(self.mv, varDcl, self.currentPackageName, lhsIndex);
            self.mv.visitJumpInsn(IFNONNULL, jumpLabel);
            self.mv.visitLabel(startLabel);
        } else {
            // Handle cases where the same variable used to trap multiple expressions with multiple trap statements.
            self.mv.visitLabel(startLabel);
            self.mv.visitInsn(ACONST_NULL);
            generateVarStore(self.mv, varDcl, self.currentPackageName, lhsIndex);
        }
    }

    function generateCatchInsForTrap(bir:Function func, bir:ErrorEntry currentEE, jvm:Label startLabel, jvm:Label endLabel, jvm:Label jumpLabel, 
    						InstructionGenerator instGen, TerminatorGenerator termGen) {
        self.mv.visitLabel(endLabel);
        self.mv.visitJumpInsn(GOTO, jumpLabel);
        if (currentEE is JErrorEntry) {
            var retVarDcl = <bir:VariableDcl>currentEE.errorOp.variableDcl;
            int retIndex = self.getJVMIndexOfVarRef(retVarDcl);
            foreach CatchIns catchIns in currentEE.catchIns {
                jvm:Label errorValueLabel = new;
                self.mv.visitTryCatchBlock(startLabel, endLabel, errorValueLabel, catchIns.errorClass);
                self.mv.visitLabel(errorValueLabel);
                self.mv.visitMethodInsn(INVOKESTATIC, BAL_ERRORS, "createInteropError", io:sprintf("(L%s;)L%s;", THROWABLE, ERROR_VALUE), false);
                generateVarStore(self.mv, retVarDcl, self.currentPackageName, retIndex);
                bir:Return term = catchIns.term;
                termGen.genReturnTerm(term, retIndex, func);
                self.mv.visitJumpInsn(GOTO, jumpLabel);
            }
            jvm:Label otherErrorLabel = new;
            self.mv.visitTryCatchBlock(startLabel, endLabel, otherErrorLabel, THROWABLE);

            self.mv.visitLabel(otherErrorLabel);
            self.mv.visitMethodInsn(INVOKESTATIC, BAL_ERRORS, "createInteropError", io:sprintf("(L%s;)L%s;", THROWABLE, ERROR_VALUE), false);
            self.mv.visitInsn(ATHROW);
            self.mv.visitJumpInsn(GOTO, jumpLabel);
            self.mv.visitLabel(jumpLabel);
            return;
	    }

        jvm:Label errorValueLabel = new;
        jvm:Label otherErrorLabel = new;
        self.mv.visitTryCatchBlock(startLabel, endLabel, errorValueLabel, ERROR_VALUE);
        self.mv.visitTryCatchBlock(startLabel, endLabel, otherErrorLabel, STACK_OVERFLOW_ERROR);
        self.mv.visitLabel(errorValueLabel);

        var varDcl = <bir:VariableDcl>currentEE.errorOp.variableDcl;
        int lhsIndex = self.getJVMIndexOfVarRef(varDcl);
        generateVarStore(self.mv, varDcl, self.currentPackageName, lhsIndex);
        self.mv.visitJumpInsn(GOTO, jumpLabel);
        self.mv.visitLabel(otherErrorLabel);
        self.mv.visitMethodInsn(INVOKESTATIC, BAL_ERRORS, TRAP_ERROR_METHOD,
                                                io:sprintf("(L%s;)L%s;", THROWABLE, ERROR_VALUE), false);
        self.mv.visitVarInsn(ASTORE, lhsIndex);
        self.mv.visitLabel(jumpLabel);
    }

    function printStackTraceFromFutureValue(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap) {
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "strand", io:sprintf("L%s;", STRAND));
        mv.visitFieldInsn(GETFIELD, STRAND, "scheduler", io:sprintf("L%s;", SCHEDULER)); 
        mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULER_START_METHOD, "()V", false);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, io:sprintf("L%s;", THROWABLE));

        // handle any runtime errors
        jvm:Label labelIf = new;
        mv.visitJumpInsn(IFNULL, labelIf);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, io:sprintf("L%s;", THROWABLE));
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_THROWABLE_METHOD, io:sprintf("(L%s;)V", THROWABLE),
                            false);
        mv.visitInsn(RETURN);
        mv.visitLabel(labelIf);
    }

    function getJVMIndexOfVarRef(bir:VariableDcl varDcl) returns int {
        return self.indexMap.getIndex(varDcl);
    }
};

type DiagnosticLogger object {
    DiagnosticLog[] errors = [];
    int size = 0;

    function logError(error err, bir:DiagnosticPos pos, bir:Package module) {
        self.errors[self.size] = {err:err, pos:pos, module:module};
        self.size += 1;
    }

    function getErrorCount() returns int {
        return self.size;
    }

    function printErrors() {
        foreach DiagnosticLog log in self.errors {
            string fileName = log.pos.sourceFileName;
            string orgName = log.module.org.value;
            string moduleName = log.module.name.value;

            string pkgIdStr;
            if (moduleName == "." && orgName == "$anon") {
                pkgIdStr = ".";
            } else {
                pkgIdStr = orgName + ":" + moduleName;
            }

            string positionStr;
            if (fileName == ".") {
                positionStr = io:sprintf("%s:%s:%s", pkgIdStr, log.pos.sLine, log.pos.sCol);
            } else {
                positionStr = io:sprintf("%s:%s:%s:%s", pkgIdStr, fileName, log.pos.sLine, log.pos.sCol);
            }

            string errorStr;
            string detail = log.err.detail().toString();
            if (detail == "") {
                errorStr = io:sprintf("error: %s: %s", positionStr, log.err.reason());
            } else {
                errorStr = io:sprintf("error: %s: %s %s", positionStr, log.err.reason(), detail);
            }
            print(errorStr);
        }
    }
};

function print(string message) {
    handle errStream = getSystemErrorStream();
    printToErrorStream(errStream, java:fromString(message));
}

public function getSystemErrorStream() returns handle = @java:FieldGet {
    name:"err",
    class:"java/lang/System"
} external;

public function printToErrorStream(handle receiver, handle message) = @java:Method {
    name:"println",
    class:"java/io/PrintStream",
    paramTypes:["java.lang.String"]
} external;

public function exit(int status) = @java:Method {
    class:"java/lang/System"
} external;

type DiagnosticLog record {|
    error err;
    bir:DiagnosticPos pos;
    bir:Package module;
|};
