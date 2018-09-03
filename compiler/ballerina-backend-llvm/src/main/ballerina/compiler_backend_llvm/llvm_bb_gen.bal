import ballerina/llvm;
import ballerina/io;

type BbGenrator object {

    llvm:LLVMBuilderRef builder,
    llvm:LLVMValueRef funcRef,
    BIRFunction func,
    BIRBasicBlock bb,
    llvm:LLVMBasicBlockRef bbRef;

    new(builder, funcRef, func, bb) {
    }

    function genBasicBlockBody() {
        bbRef = llvm:LLVMAppendBasicBlock(funcRef, bb.id.value);
        llvm:LLVMPositionBuilderAtEnd(builder, bbRef);
        foreach i in bb.instructions {
            match i {
                Move moveIns => {
                    llvm:LLVMValueRef lhsRef = getLocalVarById(func, moveIns.lhsOp.variableDcl.name.value);
                    var rhsVarOp = moveIns.rhsOp;
                    llvm:LLVMValueRef rhsVarOpRef = loadOprand(func, rhsVarOp, builder);
                    var loaded = llvm:LLVMBuildStore(builder, rhsVarOpRef, lhsRef);
                }
                BinaryOp binaryIns => {
                    var lhsTmpName = localVarName(binaryIns.lhsOp.variableDcl) + "_temp";
                    var lhsRef = getLocalVarById(func, binaryIns.lhsOp.variableDcl.name.value);
                    var rhsOp1 = loadOprand(func, binaryIns.rhsOp1, builder);
                    var rhsOp2 = loadOprand(func, binaryIns.rhsOp2, builder);
                    var kind = binaryIns.kind;
                    match kind {
                        ADD => {
                            var addReturn = llvm:LLVMBuildAdd(builder, rhsOp1, rhsOp2, lhsTmpName);
                            var loaded = llvm:LLVMBuildStore(builder, addReturn, lhsRef);
                        }
                        DIV => {
                            var ifReturn = llvm:LLVMBuildSDiv(builder, rhsOp1, rhsOp2, lhsTmpName);
                            var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                        }
                        EQUAL => {
                            var ifReturn = llvm:LLVMBuildICmp(builder, 32, rhsOp1, rhsOp2, lhsTmpName);
                            var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                        }
                        GREATER_EQUAL => {
                            var ifReturn = llvm:LLVMBuildICmp(builder, 39, rhsOp1, rhsOp2, lhsTmpName);
                            var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                        }
                        GREATER_THAN => {
                            var ifReturn = llvm:LLVMBuildICmp(builder, 38, rhsOp1, rhsOp2, lhsTmpName);
                            var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                        }
                        LESS_EQUAL => {
                            // LLVMIntSLE = 41
                            var ifReturn = llvm:LLVMBuildICmp(builder, 41, rhsOp1, rhsOp2, lhsTmpName);
                            var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                        }
                        LESS_THAN => {
                            // TODO: import these consts from llvm pkg
                            // LLVMIntSLT = 40
                            var ifReturn = llvm:LLVMBuildICmp(builder, 40, rhsOp1, rhsOp2, lhsTmpName);
                            var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                        }
                        MUL => {
                            var ifReturn = llvm:LLVMBuildMul(builder, rhsOp1, rhsOp2, lhsTmpName);
                            var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                        }
                        NOT_EQUAL => {
                            // LLVMIntNE = 33
                            var ifReturn = llvm:LLVMBuildICmp(builder, 33, rhsOp1, rhsOp2, lhsTmpName);
                            var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                        }
                        SUB => {
                            var ifReturn = llvm:LLVMBuildSub(builder, rhsOp1, rhsOp2, lhsTmpName);
                            var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                        }
                    }
                }
                ConstantLoad constOp => {
                    llvm:LLVMValueRef lhsRef = getLocalVarById(func, constOp.lhsOp.variableDcl.name.value);
                    var constRef = llvm:LLVMConstInt(llvm:LLVMInt64Type(), constOp.value, 0);
                    var loaded = llvm:LLVMBuildStore(builder, constRef, lhsRef);
                }

            }
        }
    }

    function genBasicBlockTerminator(map<FuncGenrator> funcGenrators, map<BbGenrator> bbGenrators) {
        llvm:LLVMPositionBuilderAtEnd(builder, bbRef);

        match bb.terminator {
            GOTO gotoIns => {
                var brInsRef = llvm:LLVMBuildBr(builder, findBbRefById(bbGenrators, gotoIns.targetBB.id.value));
            }
            Branch brIns => {
                var ifTrue = findBbRefById(bbGenrators, brIns.trueBB.id.value);
                var ifFalse = findBbRefById(bbGenrators, brIns.falseBB.id.value);
                var vrInsRef = llvm:LLVMBuildCondBr(builder, loadOprand(func, brIns.op, builder), ifTrue, ifFalse);
            }
            Call callIns => {
                var thenBB = findBbRefById(bbGenrators, callIns.thenBB.id.value);
                llvm:LLVMValueRef[] args = [];
                var argsCount = lengthof callIns.args;
                int i = 0;
                while (i < argsCount) {
                    args[i] = loadOprand(func, callIns.args[i], builder);
                    i++;
                }
                // TODO: check pkg, evntulay remove
                if (callIns.name.value == "print"){
                    genCallToPrintf(builder, args, false);
                } else if (callIns.name.value == "println"){
                    genCallToPrintf(builder, args, true);
                } else {
                    llvm:LLVMValueRef calleFuncRef = findFuncRefByName(funcGenrators, callIns.name);
                    llvm:LLVMValueRef callReturn = llvm:LLVMBuildCall(builder, calleFuncRef, args, argsCount, "");
                    match callIns.lhsOp {
                        BIRVarRef lhsOp => {
                            llvm:LLVMValueRef lhsRef = getLocalVarById(func, lhsOp.variableDcl.name.value);
                            var loaded = llvm:LLVMBuildStore(builder, callReturn, lhsRef);
                        }
                        () => {}
                    }
                }
                var brInsRef = llvm:LLVMBuildBr(builder, thenBB);

            }
            Return => {
                if (func.typeValue.retType != "()"){ //TODO: use BTypeNil
                    var retValueRef = llvm:LLVMBuildLoad(builder, getLocalVarById(func, "%0"), "retrun_temp");
                    var ret = llvm:LLVMBuildRet(builder, retValueRef);
                } else {
                    var ret = llvm:LLVMBuildRetVoid(builder);
                }
            }

        }
    }

};

function findBbRefById(map<BbGenrator> bbGenrators, string id) returns llvm:LLVMBasicBlockRef {
    match bbGenrators[id] {
        BbGenrator foundBB => {
            return foundBB.bbRef;
        }
        any => {
            error err = { message: "bb '" + id + "' dosn't exist" };
            throw err;
        }
    }
}

function findFuncRefByName(map<FuncGenrator> funcGenrators, Name name) returns llvm:LLVMValueRef {
    match funcGenrators[name.value] {
        FuncGenrator foundFunc => return foundFunc.funcRef;
        any => {
            error err = { message: "function '" + name.value + "' dosn't exist" };
            throw err;
        }
    }
}

