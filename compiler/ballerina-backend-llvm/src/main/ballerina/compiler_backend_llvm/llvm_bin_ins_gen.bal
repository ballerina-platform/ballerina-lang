import ballerina/io;

type BinaryInsGenrator object {

    llvm:LLVMBuilderRef builder,
    string lhsTmpName;
    llvm:LLVMValueRef lhsRef;
    llvm:LLVMValueRef rhsOp1;
    llvm:LLVMValueRef rhsOp2;
    new(builder, lhsTmpName, lhsRef, rhsOp1, rhsOp2) {
    }

    function genAdd() {
        var addReturn = llvm:LLVMBuildAdd(builder, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, addReturn, lhsRef);
    }

    function genDiv() {
        var ifReturn = llvm:LLVMBuildSDiv(builder, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }

    function genEqual() {
        var ifReturn = llvm:LLVMBuildICmp(builder, 32, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }

    function genGreaterEqual() {
        var ifReturn = llvm:LLVMBuildICmp(builder, 39, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }

    function genGreaterThan() {
        var ifReturn = llvm:LLVMBuildICmp(builder, 38, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }

    function genLessEqual() {
        // LLVMIntSLE = 41
        var ifReturn = llvm:LLVMBuildICmp(builder, 41, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }

    function genLessThan() {
        // TODO: import these consts from llvm pkg
        // LLVMIntSLT = 40
        var ifReturn = llvm:LLVMBuildICmp(builder, 40, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }

    function genMul() {
        var ifReturn = llvm:LLVMBuildMul(builder, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }

    function genNotEqual() {
        // LLVMIntNE = 33
        var ifReturn = llvm:LLVMBuildICmp(builder, 33, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }

    function genSub() {
        var ifReturn = llvm:LLVMBuildSub(builder, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }
};

