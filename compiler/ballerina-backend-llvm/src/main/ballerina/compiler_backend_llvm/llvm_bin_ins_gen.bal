import ballerina/io;
import ballerina/llvm;

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
        var ifReturn = llvm:LLVMBuildICmp(builder, llvm:LLVMIntEQ, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }

    function genGreaterEqual() {
        var ifReturn = llvm:LLVMBuildICmp(builder, llvm:LLVMIntSGE, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }

    function genGreaterThan() {
        var ifReturn = llvm:LLVMBuildICmp(builder, llvm:LLVMIntSGT, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }

    function genLessEqual() {
        var ifReturn = llvm:LLVMBuildICmp(builder, llvm:LLVMIntSLE, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }

    function genLessThan() {
        var ifReturn = llvm:LLVMBuildICmp(builder, llvm:LLVMIntSLT, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }

    function genMul() {
        var ifReturn = llvm:LLVMBuildMul(builder, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }

    function genNotEqual() {
        var ifReturn = llvm:LLVMBuildICmp(builder, llvm:LLVMIntNE, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }

    function genSub() {
        var ifReturn = llvm:LLVMBuildSub(builder, rhsOp1, rhsOp2, lhsTmpName);
        var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
    }
};

