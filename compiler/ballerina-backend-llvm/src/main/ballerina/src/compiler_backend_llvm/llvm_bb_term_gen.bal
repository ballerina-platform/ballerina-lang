import ballerina/llvm;
import ballerina/bir;

type BbTermGenrator object {

    llvm:LLVMBuilderRef builder;
    bir:BasicBlock bb;
    llvm:LLVMBasicBlockRef bbRef;
    FuncGenrator parent;

    function __init(llvm:LLVMBuilderRef builder, bir:BasicBlock bb, llvm:LLVMBasicBlockRef bbRef,
                FuncGenrator parent) {
        self.builder = builder;
        self.bb = bb;
        self.bbRef = bbRef;
        self.parent = parent;
    }

    function genBasicBlockTerminator(map<FuncGenrator> funcGenrators, map<BbTermGenrator> bbGenrators) {
        llvm:llvmPositionBuilderAtEnd(self.builder, self.bbRef);

        if (self.bb.terminator is bir:GOTO) {
            self.genGoToTerm(<bir:GOTO>self.bb.terminator, bbGenrators);
        } else if (self.bb.terminator is bir:Branch) {
            self.genBranchTerm(<bir:Branch>self.bb.terminator, bbGenrators);
        } else if (self.bb.terminator is bir:Call) {
            self.genCallTerm(<bir:Call>self.bb.terminator, funcGenrators, bbGenrators);
        } else if (self.bb.terminator is bir:Return) {
            self.genReturnTerm();
        }
    }

    function genGoToTerm(bir:GOTO gotoIns, map<BbTermGenrator> bbGenrators) {
        var brInsRef = llvm:llvmBuildBr(self.builder, findBbRefById(bbGenrators, gotoIns.targetBB.id.value));
    }

    function genBranchTerm(bir:Branch brIns, map<BbTermGenrator> bbGenrators) {
        var ifTrue = findBbRefById(bbGenrators, brIns.trueBB.id.value);
        var ifFalse = findBbRefById(bbGenrators, brIns.falseBB.id.value);
        var vrInsRef = llvm:llvmBuildCondBr(self.builder, self.parent.genLoadLocalToTempVar(brIns.op), ifTrue, ifFalse);
    }

    function genCallTerm(bir:Call callIns, map<FuncGenrator> funcGenrators, map<BbTermGenrator> bbGenrators) {
        llvm:LLVMValueRef[] args = self.mapOverGenVarLoad(callIns.args);

        if (callIns.name.value == "print"){
            self.genCallToPrintf(args, "");
        } else if (callIns.name.value == "println"){
            self.genCallToPrintf(args, "\n");
        } else {
            self.genCallToSamePkgFunc(funcGenrators, callIns, args);
        }

        var thenBB = findBbRefById(bbGenrators, callIns.thenBB.id.value);
        var brInsRef = llvm:llvmBuildBr(self.builder, thenBB);
    }

    function mapOverGenVarLoad(bir:VarRef?[] ops) returns llvm:LLVMValueRef[] {
        llvm:LLVMValueRef[] loaddedVars = [];
            var argsCount = ops.length();
            int i = 0;
            while (i < argsCount) {
                if (ops[i] is bir:VarRef) {
                    loaddedVars[i] = self.parent.genLoadLocalToTempVar(<bir:VarRef> ops[i]);
                }
                i += 1;
            }

        return loaddedVars;
    }

    function genCallToPrintf(llvm:LLVMValueRef[] args, string suffix) {
        var argsCount = args.length();
        var printfPatten = stringMul("%ld", argsCount) + suffix;
        var printLnIntPatten = llvm:llvmBuildGlobalStringPtr(self.builder, printfPatten, "");
        llvm:LLVMValueRef[] printArgs = [printLnIntPatten];
        appendAllTo(printArgs, args);
        if (printfRef is llvm:LLVMValueRef) {
            llvm:LLVMValueRef callReturn = llvm:llvmBuildCall(self.builder, <llvm:LLVMValueRef>printfRef, printArgs,
                        printArgs.length(), "");
        }
    }

    function genCallToSamePkgFunc(map<FuncGenrator> funcGenrators, bir:Call callIns, llvm:LLVMValueRef[] args) {
        llvm:LLVMValueRef calleFuncRef = findFuncRefByName(funcGenrators, callIns.name);
        llvm:LLVMValueRef callReturn = llvm:llvmBuildCall(self.builder, calleFuncRef, args, args.length(), "");
        if (callIns.lhsOp is bir:VarRef) {
            bir:VarRef lhsOpVar = <bir:VarRef>callIns.lhsOp;
            var lhsRef = self.parent.getLocalVarRef(lhsOpVar);
            var loaded = llvm:llvmBuildStore(self.builder, callReturn, <llvm:LLVMValueRef> lhsRef);
        }
    }

    function genReturnTerm() {
        if (self.parent.isVoidFunc()){
            var lhsRef = self.parent.getLocalVarRefById("0");
            var retValueRef = llvm:llvmBuildLoad(self.builder, <llvm:LLVMValueRef> lhsRef, "retrun_temp");
            var ret = llvm:llvmBuildRet(self.builder, retValueRef);
        } else {
            var ret = llvm:llvmBuildRetVoid(self.builder);
        }

    }
};


function stringMul(string str, int factor) returns string {
    int i = 0;
    string result = "";
    while i < factor {
        result = result + str;
        i += 1;
    }
    return result;
}


