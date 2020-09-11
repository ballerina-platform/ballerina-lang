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
import ballerina/java;

type ErrorHandlerGenerator object {
    jvm:MethodVisitor mv;
    BalToJVMIndexMap indexMap;
    string currentPackageName;

    function init(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, string currentPackageName) {
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

    function generateTryCatch(bir:Function func, string funcName, bir:BasicBlock currentBB, 
                            InstructionGenerator instGen, TerminatorGenerator termGen, LabelGenerator labelGen) {
        bir:ErrorEntry? nilableEE = findErrorEntry(func.errorEntries, currentBB);
        if nilableEE is () {
            return; 
        }
        bir:ErrorEntry currentEE = <bir:ErrorEntry> nilableEE;
    
        jvm:Label startLabel = labelGen.getLabel(funcName + currentEE.trapBB.id.value);
        jvm:Label endLabel = new;
        jvm:Label jumpLabel = new;
    
    
        self.mv.visitLabel(endLabel);
        self.mv.visitJumpInsn(GOTO, jumpLabel);
        if (currentEE is JErrorEntry) {
            var retVarDcl = <bir:VariableDcl>currentEE.errorOp.variableDcl;
            int retIndex = self.indexMap.getIndex(retVarDcl);
            boolean exeptionExist = false;
            foreach CatchIns catchIns in currentEE.catchIns {
                if catchIns.errorClass == ERROR_VALUE {
                    exeptionExist = true;
                }
                jvm:Label errorValueLabel = new;
                self.mv.visitTryCatchBlock(startLabel, endLabel, errorValueLabel, catchIns.errorClass);
                self.mv.visitLabel(errorValueLabel);
                self.mv.visitMethodInsn(INVOKESTATIC, BAL_ERRORS, "createInteropError", io:sprintf("(L%s;)L%s;", THROWABLE, ERROR_VALUE), false);
                generateVarStore(self.mv, retVarDcl, self.currentPackageName, retIndex);
                bir:Return term = catchIns.term;
                termGen.genReturnTerm(term, retIndex, func);
                self.mv.visitJumpInsn(GOTO, jumpLabel);
            }
            if !exeptionExist {
                jvm:Label errorValErrorLabel = new;
                self.mv.visitTryCatchBlock(startLabel, endLabel, errorValErrorLabel, ERROR_VALUE);
        
                self.mv.visitLabel(errorValErrorLabel);
                self.mv.visitInsn(ATHROW);
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
        int lhsIndex = self.indexMap.getIndex(varDcl);
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

function findErrorEntry(bir:ErrorEntry?[] errors, bir:BasicBlock currentBB) returns bir:ErrorEntry? {
    foreach var err in errors {
        if err is bir:ErrorEntry && err.endBB.id.value == currentBB.id.value {
            return err;
        }
    }
    return ();
}

function print(string message) {
    handle errStream = getSystemErrorStream();
    printToErrorStream(errStream, java:fromString(message));
}

public function getSystemErrorStream() returns handle = @java:FieldGet {
    name:"err",
    'class:"java/lang/System"
} external;

public function printToErrorStream(handle receiver, handle message) = @java:Method {
    name:"println",
    'class:"java/io/PrintStream",
    paramTypes:["java.lang.String"]
} external;

public function exit(int status) = @java:Method {
    'class:"java/lang/System"
} external;

type DiagnosticLog record {|
    error err;
    bir:DiagnosticPos pos;
    bir:Package module;
|};
