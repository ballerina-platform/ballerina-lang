import ballerina/llvm;
import ballerina/io;
import ballerina/bir;

type BbTermGenrator object {

    llvm:LLVMBuilderRef builder,
    llvm:LLVMValueRef funcRef,
    bir:BIRFunction func,
    FuncGenrator parent,
    bir:BIRBasicBlock bb,
    llvm:LLVMBasicBlockRef bbRef;

    new(builder, funcRef, func, bb, bbRef, parent) {
    }

    function genBasicBlockTerminator(map<FuncGenrator> funcGenrators, map<BbTermGenrator> bbGenrators) {
        llvm:LLVMPositionBuilderAtEnd(builder, bbRef);

        match bb.terminator {
            bir:GOTO gotoIns => genGoToTerm(gotoIns, bbGenrators);
            bir:Branch brIns => genBranchTerm(brIns, bbGenrators);
            bir:Call callIns => genCallTerm(callIns, funcGenrators, bbGenrators);
            bir:Return => genReturnTerm();
        }
    }

    function genGoToTerm(bir:GOTO gotoIns, map<BbTermGenrator> bbGenrators) {
        var brInsRef = llvm:LLVMBuildBr(builder, findBbRefById(bbGenrators, gotoIns.targetBB.id.value));
    }

    function genBranchTerm(bir:Branch brIns, map<BbTermGenrator> bbGenrators) {
        var ifTrue = findBbRefById(bbGenrators, brIns.trueBB.id.value);
        var ifFalse = findBbRefById(bbGenrators, brIns.falseBB.id.value);
        var vrInsRef = llvm:LLVMBuildCondBr(builder, genVarLoad(brIns.op), ifTrue, ifFalse);
    }

    function genCallTerm(bir:Call callIns, map<FuncGenrator> funcGenrators, map<BbTermGenrator> bbGenrators) {
        llvm:LLVMValueRef[] args = mapOverGenVarLoad(callIns.args);

        if (callIns.name.value == "print"){
            genCallToPrintf(args, "");
        } else if (callIns.name.value == "println"){
            genCallToPrintf(args, "\n");
        } else {
            genCallToSamePkgFunc(funcGenrators, callIns, args);
        }

        var thenBB = findBbRefById(bbGenrators, callIns.thenBB.id.value);
        var brInsRef = llvm:LLVMBuildBr(builder, thenBB);
    }

    function mapOverGenVarLoad(bir:BIROperand[] ops) returns llvm:LLVMValueRef[] {
        llvm:LLVMValueRef[] loaddedVars = [];
        var argsCount = lengthof ops;
        int i = 0;
        while (i < argsCount) {
            loaddedVars[i] = genVarLoad(ops[i]);
            i++;
        }
        return loaddedVars;
    }

    function genCallToPrintf(llvm:LLVMValueRef[] args, string suffix) {
        var argsCount = lengthof args;
        var printfPatten = stringMul("%ld", argsCount) + suffix;
        var printLnIntPatten = llvm:LLVMBuildGlobalStringPtr(builder, printfPatten, "");
        llvm:LLVMValueRef[] printArgs = [printLnIntPatten];
        appendAllTo(printArgs, args);
        llvm:LLVMValueRef callReturn = llvm:LLVMBuildCall(builder, printfRef, printArgs, lengthof printArgs, "");
    }

    function genCallToSamePkgFunc(map<FuncGenrator> funcGenrators, bir:Call callIns, llvm:LLVMValueRef[] args) {
        llvm:LLVMValueRef calleFuncRef = findFuncRefByName(funcGenrators, callIns.name);
        llvm:LLVMValueRef callReturn = llvm:LLVMBuildCall(builder, calleFuncRef, args, lengthof args, "");
        match callIns.lhsOp {
            bir:BIRVarRef lhsOp => {
                llvm:LLVMValueRef lhsRef = parent.getLocalVarRefById(func, lhsOp.variableDcl.name.value);
                var loaded = llvm:LLVMBuildStore(builder, callReturn, lhsRef);
            }
            () => {
                // void function call, no need to store
            }
        }

    }

    function genReturnTerm() {
        if (isVoidFunc()){
            var retValueRef = llvm:LLVMBuildLoad(builder, parent.getLocalVarRefById(func, "%0"), "retrun_temp");
            var ret = llvm:LLVMBuildRet(builder, retValueRef);
        } else {
            var ret = llvm:LLVMBuildRetVoid(builder);
        }

    }
    function isVoidFunc() returns boolean {
        return func.typeValue.retType != "()"; //TODO: use cont instead "()" eg: BTypeNil
    }

    // TODO remove duplicate func
    function genVarLoad(bir:BIROperand oprand) returns llvm:LLVMValueRef {
        match oprand {
            bir:BIRVarRef refOprand => {
                string tempName = localVarName(refOprand.variableDcl) + "_temp";
                return llvm:LLVMBuildLoad(builder, parent.getLocalVarRefById(func, refOprand.variableDcl.name.value),
                    tempName);
            }
        }
    }


};

