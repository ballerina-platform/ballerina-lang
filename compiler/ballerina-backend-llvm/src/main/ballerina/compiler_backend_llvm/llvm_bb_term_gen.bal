import ballerina/llvm;
import ballerina/bir;

type BbTermGenrator object {

    llvm:LLVMBuilderRef builder;
    FuncGenrator parent;
    bir:BasicBlock bb;
    llvm:LLVMBasicBlockRef bbRef;

    function __init(llvm:LLVMBuilderRef builder, bir:BasicBlock bb, llvm:LLVMBasicBlockRef bbRef, FuncGenrator parent) {
        self.builder = builder;
        self.parent = parent;
        self.bb = bb;
        self.bbRef = bbRef;
    }

    function genBasicBlockTerminator(map<FuncGenrator> funcGenrators, map<BbTermGenrator> bbGenrators) {
        llvm:LLVMPositionBuilderAtEnd(self.builder,  self.bbRef);

        var instruction = self.bb.terminator;
        if (instruction is bir:GOTO) {
            self.genGoToTerm(instruction, bbGenrators);
        } else if (instruction is bir:Branch) {
            self.genBranchTerm(instruction, bbGenrators);
        } else if (instruction is bir:Call) {
            self.genCallTerm(instruction, funcGenrators, bbGenrators);
        } else {
            self.genReturnTerm();
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

    function mapOverGenVarLoad(bir:Operand?[] ops) returns llvm:LLVMValueRef[] {
        llvm:LLVMValueRef[] loaddedVars = [];
        var argsCount = ops.length();
        int i = 0;
        while (i < argsCount) {
            bir:Operand operand = <bir:Operand> ops[i];
            loaddedVars[i] = self.parent.genLoadLocalToTempVar(operand);
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

        var result = callIns.lhsOp;

        if (result is bir:VarRef) {
            llvm:LLVMValueRef lhsRef = self.parent.getLocalVarRefById(result.variableDcl.name.value);
            var loaded = llvm:LLVMBuildStore(self.builder, callReturn, lhsRef);
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


