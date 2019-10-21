import ballerina/llvm;

type BinaryInsGenrator object {

    llvm:LLVMBuilderRef builder;
    string lhsTmpName;
    llvm:LLVMValueRef lhsRef;
    llvm:LLVMValueRef rhsOp1;
    llvm:LLVMValueRef rhsOp2;

    function __init(llvm:LLVMBuilderRef builder, string lhsTmpName, llvm:LLVMValueRef lhsRef, llvm:LLVMValueRef rhsOp1,
                    llvm:LLVMValueRef rhsOp2) {
        self.builder = builder;
        self.lhsTmpName = lhsTmpName;
        self.lhsRef = lhsRef;
        self.rhsOp1 = rhsOp1;
        self.rhsOp2 = rhsOp2;
    }

    function genAdd() {
        var addReturn = llvm:llvmBuildAdd(builder, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:llvmBuildStore(builder, addReturn, lhsRef);
    }

    function genDiv() {
        var ifReturn = llvm:llvmBuildSDiv(builder, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:llvmBuildStore(builder, ifReturn, lhsRef);
    }

    function genEqual() {
        var ifReturn = llvm:llvmBuildICmp(builder, llvm:LLVMIntEQ, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:llvmBuildStore(builder, ifReturn, lhsRef);
    }

    function genGreaterEqual() {
        var ifReturn = llvm:llvmBuildICmp(builder, llvm:LLVMIntSGE, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:llvmBuildStore(builder, ifReturn, lhsRef);
    }

    function genGreaterThan() {
        var ifReturn = llvm:llvmBuildICmp(builder, llvm:LLVMIntSGT, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:llvmBuildStore(builder, ifReturn, lhsRef);
    }

    function genLessEqual() {
        var ifReturn = llvm:llvmBuildICmp(builder, llvm:LLVMIntSLE, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:llvmBuildStore(builder, ifReturn, lhsRef);
    }

    function genLessThan() {
        var ifReturn = llvm:llvmBuildICmp(builder, llvm:LLVMIntSLT, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:llvmBuildStore(builder, ifReturn, lhsRef);
    }

    function genMul() {
        var ifReturn = llvm:llvmBuildMul(builder, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:llvmBuildStore(builder, ifReturn, lhsRef);
    }

    function genNotEqual() {
        var ifReturn = llvm:llvmBuildICmp(builder, llvm:LLVMIntNE, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:llvmBuildStore(builder, ifReturn, lhsRef);
    }

    function genSub() {
        var ifReturn = llvm:llvmBuildSub(builder, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:llvmBuildStore(builder, ifReturn, lhsRef);
    }
};


