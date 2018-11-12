import ballerina/llvm;
import ballerina/bir;

type BbTermGenrator object {

    llvm:LLVMBuilderRef builder;
    FuncGenrator parent;
    bir:BasicBlock bb;
    llvm:LLVMBasicBlockRef bbRef;

    new(builder, bb, bbRef, parent) {
    }

    function genBasicBlockTerminator(map<FuncGenrator> funcGenrators, map<BbTermGenrator> bbGenrators) {
        llvm:LLVMPositionBuilderAtEnd(self.builder,  self.bbRef);

        match self.bb.terminator {
            bir:GOTO gotoIns => self.genGoToTerm(gotoIns, bbGenrators);
            bir:Branch brIns => self.genBranchTerm(brIns, bbGenrators);
            bir:Call callIns => self.genCallTerm(callIns, funcGenrators, bbGenrators);
            bir:Return => self.genReturnTerm();
        }
    }

    function genGoToTerm(bir:GOTO gotoIns, map<BbTermGenrator> bbGenrators) {
        var brInsRef = llvm:LLVMBuildBr(self.builder, findBbRefById(bbGenrators, gotoIns.targetBB.id.value));
    }

    function genBranchTerm(bir:Branch brIns, map<BbTermGenrator> bbGenrators) {
        var ifTrue = findBbRefById(bbGenrators, brIns.trueBB.id.value);
        var ifFalse = findBbRefById(bbGenrators, brIns.falseBB.id.value);
        var vrInsRef = llvm:LLVMBuildCondBr(self.builder, self.parent.genLoadLocalToTempVar(brIns.op), ifTrue, ifFalse);
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
        var brInsRef = llvm:LLVMBuildBr(self.builder, thenBB);
    }

    function mapOverGenVarLoad(bir:Operand[] ops) returns llvm:LLVMValueRef[] {
        llvm:LLVMValueRef[] loaddedVars = [];
        var argsCount = ops.length();
        int i = 0;
        while (i < argsCount) {
            loaddedVars[i] = self.parent.genLoadLocalToTempVar(ops[i]);
            i += 1;
        }
        return loaddedVars;
    }

    function genCallToPrintf(llvm:LLVMValueRef[] args, string suffix) {
        var argsCount = args.length();
        var printfPatten = stringMul("%ld", argsCount) + suffix;
        var printLnIntPatten = llvm:LLVMBuildGlobalStringPtr(self.builder, printfPatten, "");
        llvm:LLVMValueRef[] printArgs = [printLnIntPatten];
        appendAllTo(printArgs, args);
        llvm:LLVMValueRef callReturn = llvm:LLVMBuildCall(self.builder, printfRef, printArgs, printArgs.length(), "");
    }

    function genCallToSamePkgFunc(map<FuncGenrator> funcGenrators, bir:Call callIns, llvm:LLVMValueRef[] args) {
        llvm:LLVMValueRef calleFuncRef = findFuncRefByName(funcGenrators, callIns.name);
        llvm:LLVMValueRef callReturn = llvm:LLVMBuildCall(self.builder, calleFuncRef, args, args.length(), "");
        match callIns.lhsOp {
            bir:VarRef lhsOp => {
                llvm:LLVMValueRef lhsRef = self.parent.getLocalVarRefById(lhsOp.variableDcl.name.value);
                var loaded = llvm:LLVMBuildStore(self.builder, callReturn, lhsRef);
            }
            () => {
                // void function call, no need to store
            }
        }

    }

    function genReturnTerm() {
        if (self.parent.isVoidFunc()){
            var retValueRef = llvm:LLVMBuildLoad(self.builder, self.parent.getLocalVarRefById("%0"), "retrun_temp");
            var ret = llvm:LLVMBuildRet(self.builder, retValueRef);
        } else {
            var ret = llvm:LLVMBuildRetVoid(self.builder);
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


