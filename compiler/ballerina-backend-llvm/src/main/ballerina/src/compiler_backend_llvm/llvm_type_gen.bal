import ballerina/bir;
import ballerina/llvm;

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
        self.sizeOp = newArrayIns.sizeOp;
        self.typeValue = <bir:BArrayType>newArrayIns.typeValue;
    }

    function allocateArray() returns llvm:LLVMValueRef {
        if (self.typeValue.eType is bir:BTypeInt) {
            llvm:LLVMValueRef newArrayRef = self.createNewIntArray();
            return newArrayRef;
        } else {
            panic error("InvalidDataTypeForArrayError", message = "Arrays only supports integers yet!");
        }
    }

    function createNewIntArray() returns llvm:LLVMValueRef {
        llvm:LLVMValueRef[] sizeOpValRef = [self.parent.genLoadLocalToTempVar(self.sizeOp)];
        llvm:LLVMValueRef newIntArrayFunc = nativeFunctionBuilder.getFunctionValueRef("new_int_array");
        llvm:LLVMValueRef newIntArrayRef = llvm:llvmBuildCall(self.builder, newIntArrayFunc, sizeOpValRef, 0, self.getNewArrayTempName());
        return self.storeReferenceArray(newIntArrayRef);
    }

    function getNewArrayTempName() returns string{
        return self.getLhsOpName() + "_temp";
    }

    function getLhsOpName() returns string {
        return self.lhsOp.variableDcl.name.value;
    }

    function storeReferenceArray(llvm:LLVMValueRef referenceArray) returns llvm:LLVMValueRef {
        llvm:LLVMValueRef lhsOp = llvm:llvmBuildAlloca(self.builder, llvm:llvmPointerType(llvm:llvmInt64Type(), 0), self.getLhsOpName());
        _ = llvm:llvmBuildStore(self.builder, referenceArray, lhsOp);
        return lhsOp;
    }
};
