import ballerina/llvm;
import ballerina/io;

type BbTermGenrator object {

    llvm:LLVMBuilderRef builder,
    llvm:LLVMValueRef funcRef,
    BIRFunction func,
    FuncGenrator parent,
    BIRBasicBlock bb,
    llvm:LLVMBasicBlockRef bbRef;

    new(builder, funcRef, func, bb, bbRef, parent) {
    }

    function genBasicBlockTerminator(map<FuncGenrator> funcGenrators, map<BbTermGenrator> bbGenrators) {
        llvm:LLVMPositionBuilderAtEnd(builder, bbRef);

        match bb.terminator {
            GOTO gotoIns => {
                var brInsRef = llvm:LLVMBuildBr(builder, findBbRefById(bbGenrators, gotoIns.targetBB.id.value));
            }
            Branch brIns => {
                var ifTrue = findBbRefById(bbGenrators, brIns.trueBB.id.value);
                var ifFalse = findBbRefById(bbGenrators, brIns.falseBB.id.value);
                var vrInsRef = llvm:LLVMBuildCondBr(builder, loadOprand( brIns.op), ifTrue, ifFalse);
            }
            Call callIns => {
                var thenBB = findBbRefById(bbGenrators, callIns.thenBB.id.value);
                llvm:LLVMValueRef[] args = [];
                var argsCount = lengthof callIns.args;
                int i = 0;
                while (i < argsCount) {
                    args[i] = loadOprand(callIns.args[i]);
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
                            llvm:LLVMValueRef lhsRef = parent.getLocalVarRefById(func, lhsOp.variableDcl.name.value);
                            var loaded = llvm:LLVMBuildStore(builder, callReturn, lhsRef);
                        }
                        () => {}
                    }
                }
                var brInsRef = llvm:LLVMBuildBr(builder, thenBB);

            }
            Return => {
                if (func.typeValue.retType != "()"){ //TODO: use BTypeNil
                    var retValueRef = llvm:LLVMBuildLoad(builder, parent.getLocalVarRefById(func, "%0"), "retrun_temp");
                    var ret = llvm:LLVMBuildRet(builder, retValueRef);
                } else {
                    var ret = llvm:LLVMBuildRetVoid(builder);
                }
            }

        }
    }

    function loadOprand(BIROperand oprand) returns llvm:LLVMValueRef {
        match oprand {
            BIRVarRef refOprand => {
                string tempName = localVarName(refOprand.variableDcl) + "_temp";
                return llvm:LLVMBuildLoad(builder, parent.getLocalVarRefById(func, refOprand.variableDcl.name.value), tempName);
            }
        }
    }


};

