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

type ErrorHandlerGenerator object {
    jvm:MethodVisitor mv;
    BalToJVMIndexMap indexMap;
    string currentPackageName;

    public function __init(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap, string currentPackageName) {
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

    function generateTryIns(jvm:Label endLabel, jvm:Label handlerLabel) {
        jvm:Label startLabel = new;
        self.mv.visitTryCatchBlock(startLabel, endLabel, handlerLabel, ERROR_VALUE);
        self.mv.visitLabel(startLabel);
    }

    function generateTryInsForTrap(bir:ErrorEntry currentEE, string[] errorVarNames, jvm:Label endLabel,
                                   jvm:Label handlerLabel, jvm:Label jumpLabel) {
        var varDcl = <bir:VariableDcl>currentEE.errorOp.variableDcl;
        int lhsIndex = self.getJVMIndexOfVarRef(varDcl);
        if (!stringArrayContains(errorVarNames, currentEE.errorOp.variableDcl.name.value)) {
            errorVarNames[errorVarNames.length()] =  currentEE.errorOp.variableDcl.name.value;
            self.mv.visitInsn(ACONST_NULL);
            generateVarStore(self.mv, varDcl, self.currentPackageName, lhsIndex);
        }

        jvm:Label startLabel = new;
        self.mv.visitTryCatchBlock(startLabel, endLabel, handlerLabel, ERROR_VALUE);
        jvm:Label temp = new;
        self.mv.visitLabel(temp);

        generateVarLoad(self.mv, varDcl, self.currentPackageName, lhsIndex);
        self.mv.visitJumpInsn(IFNONNULL, jumpLabel);
        self.mv.visitLabel(startLabel);
    }

    function generateCatchInsForTrap(bir:ErrorEntry currentEE, jvm:Label endLabel, jvm:Label handlerLabel,
                                     jvm:Label jumpLabel) {
        self.mv.visitLabel(endLabel);
        self.mv.visitJumpInsn(GOTO, jumpLabel);
        self.mv.visitLabel(handlerLabel);

        var varDcl = <bir:VariableDcl>currentEE.errorOp.variableDcl;
        int lhsIndex = self.getJVMIndexOfVarRef(varDcl);
        generateVarStore(self.mv, varDcl, self.currentPackageName, lhsIndex);
        self.mv.visitLabel(jumpLabel);
    }

    function printStackTraceFromFutureValue(jvm:MethodVisitor mv, BalToJVMIndexMap indexMap) {
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, "strand", io:sprintf("L%s;", STRAND));
        mv.visitFieldInsn(GETFIELD, STRAND, "scheduler", io:sprintf("L%s;", SCHEDULER)); 
        mv.visitMethodInsn(INVOKEVIRTUAL, SCHEDULER, SCHEDULER_START_METHOD, "()V", false);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, io:sprintf("L%s;", THROWABLE));
        jvm:Label labelIf = new;
        mv.visitJumpInsn(IFNULL, labelIf);
        mv.visitFieldInsn(GETFIELD, FUTURE_VALUE, PANIC_FIELD, io:sprintf("L%s;", THROWABLE));
        bir:VariableDcl runtimePanicVar = {
                                            typeValue: "any",
                                            name: { value: "runtimePanicVar" },
                                            kind: "ARG"
                                          };
        int runtimePanicVarIndex = indexMap.getIndex(runtimePanicVar);
        mv.visitVarInsn(ASTORE, runtimePanicVarIndex);
        mv.visitVarInsn(ALOAD, runtimePanicVarIndex);
        mv.visitTypeInsn(INSTANCEOF, ERROR_VALUE);
        jvm:Label runtimePanicErrorLabel = new;
        mv.visitJumpInsn(IFNE, runtimePanicErrorLabel);
        mv.visitVarInsn(ALOAD, runtimePanicVarIndex);
        mv.visitMethodInsn(INVOKESTATIC, RUNTIME_UTILS, HANDLE_THROWABLE_METHOD, io:sprintf("(L%s;)V", THROWABLE),
                            false);
        mv.visitInsn(RETURN);
        mv.visitLabel(runtimePanicErrorLabel);
        mv.visitVarInsn(ALOAD, runtimePanicVarIndex);
        mv.visitInsn(ATHROW);

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

            string positionStr = io:sprintf("%s:%s:%s:%s", pkgIdStr, fileName, log.pos.sLine, log.pos.sCol);
            string errorStr = io:sprintf("error: %s: %s %s", positionStr, log.err.reason(), log.err.detail());
            io:println(errorStr);
        }
    }
};

type DiagnosticLog record {|
    error err;
    bir:DiagnosticPos pos;
    bir:Package module;
|};
