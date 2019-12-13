import ballerina/bir;

type NewArray object {
    llvm:LLVMBuilderRef builder;
    FuncGenrator parent;
    bir:VarRef lhsOp;
    bir:VarRef sizeOp;
    bir:BArrayType typeValue; 

    function __init(llvm:LLVMBuilderRef builder, FuncGenrator parent, bir:NewArray newArrayIns) {
        self.builder = builder;
        self.parent = parent;
        self.lhsOp = newArrayIns.lhsOp;
        self.sizeO = newArrayIns.sizeOp;
        self.typeValue = <bir:BArrayType>newArrayIns.typeValue;
    }

    function allocateArray() returns llvm:LLVMValueRef {
        if (self.typeValue.eType is bir:BIntType) {
            self.createIntArray();
        }
    }

    function createIntArray() returns llvm:LLVMValueRef {
        llvm:LLVMValueRef sizeOp = self.parent.genLoadLocalToTempVar(sizeOp);
    }
}
