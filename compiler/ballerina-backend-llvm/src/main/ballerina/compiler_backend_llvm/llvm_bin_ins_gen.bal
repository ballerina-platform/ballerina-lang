import ballerina/llvm;

type BinaryInsGenrator object {

    llvm:LLVMBuilderRef builder;
    string lhsTmpName;
    llvm:LLVMValueRef lhsRef;
    llvm:LLVMValueRef rhsOp1;
    llvm:LLVMValueRef rhsOp2;

    new(builder, lhsTmpName, lhsRef, rhsOp1, rhsOp2) {
    }

    function genAdd() {
        var addReturn = llvm:LLVMBuildAdd(self.builder, self.rhsOp1, self.rhsOp2,  self.lhsTmpName);
        var loaded = llvm:LLVMBuildStore(self.builder, addReturn,  self.lhsRef);
    }

    function genDiv() {
        var ifReturn = llvm:LLVMBuildSDiv(self.builder, self.rhsOp1, self.rhsOp2,  self.lhsTmpName);
        var loaded = llvm:LLVMBuildStore(self.builder, ifReturn,  self.lhsRef);
    }

    function genEqual() {
        var ifReturn = llvm:LLVMBuildICmp(self.builder, llvm:LLVMIntEQ, self.rhsOp1, self.rhsOp2,  self.lhsTmpName);
        var loaded = llvm:LLVMBuildStore(self.builder, ifReturn,  self.lhsRef);
    }

    function genGreaterEqual() {
        var ifReturn = llvm:LLVMBuildICmp(self.builder, llvm:LLVMIntSGE, self.rhsOp1, self.rhsOp2,  self.lhsTmpName);
        var loaded = llvm:LLVMBuildStore(self.builder, ifReturn,  self.lhsRef);
    }

    function genGreaterThan() {
        var ifReturn = llvm:LLVMBuildICmp(self.builder, llvm:LLVMIntSGT, self.rhsOp1, self.rhsOp2,  self.lhsTmpName);
        var loaded = llvm:LLVMBuildStore(self.builder, ifReturn,  self.lhsRef);
    }

    function genLessEqual() {
        var ifReturn = llvm:LLVMBuildICmp(self.builder, llvm:LLVMIntSLE, self.rhsOp1, self.rhsOp2,  self.lhsTmpName);
        var loaded = llvm:LLVMBuildStore(self.builder, ifReturn,  self.lhsRef);
    }

    function genLessThan() {
        var ifReturn = llvm:LLVMBuildICmp(self.builder, llvm:LLVMIntSLT, self.rhsOp1, self.rhsOp2,  self.lhsTmpName);
        var loaded = llvm:LLVMBuildStore(self.builder, ifReturn,  self.lhsRef);
    }

    function genMul() {
        var ifReturn = llvm:LLVMBuildMul(self.builder, self.rhsOp1, self.rhsOp2,  self.lhsTmpName);
        var loaded = llvm:LLVMBuildStore(self.builder, ifReturn,  self.lhsRef);
    }

    function genNotEqual() {
        var ifReturn = llvm:LLVMBuildICmp(self.builder, llvm:LLVMIntNE, self.rhsOp1, self.rhsOp2,  self.lhsTmpName);
        var loaded = llvm:LLVMBuildStore(self.builder, ifReturn,  self.lhsRef);
    }

    function genSub() {
        var ifReturn = llvm:LLVMBuildSub(self.builder, self.rhsOp1, self.rhsOp2,  self.lhsTmpName);
        var loaded = llvm:LLVMBuildStore(self.builder, ifReturn,  self.lhsRef);
    }
};

