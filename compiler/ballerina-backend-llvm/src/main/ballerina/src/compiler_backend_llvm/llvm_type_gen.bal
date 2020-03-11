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
        llvm:LLVMValueRef newIntArrayRef = llvm:llvmBuildCall(self.builder, newIntArrayFunc, sizeOpValRef, 1, self.getNewArrayTempName());
        return newIntArrayRef;
    }

    function getNewArrayTempName() returns string{
        return  "tempOf_" + localVariableName(self.lhsOp);
    }

    function genInstruction() {
        llvm:LLVMValueRef  newArrayRef = self.allocateArray();
        self.storeArrayRefInLocal(newArrayRef);
    }

    function storeArrayRefInLocal(llvm:LLVMValueRef newArrayRef) {
        llvm:LLVMValueRef localVariable = self.parent.getLocalVarRef(self.lhsOp);
        _ = llvm:llvmBuildStore(self.builder, newArrayRef, localVariable);
    }
};

type FieldAccess object {
    llvm:LLVMBuilderRef builder;
    FuncGenrator parent;
    bir:VarRef lhsOp;
    bir:VarRef keyOp;
    bir:VarRef rhsOp;
    bir:InstructionKind kind;

    function __init(llvm:LLVMBuilderRef builder, FuncGenrator parent, bir:FieldAccess fieldAccess) {
        self.builder = builder;
        self.parent = parent;
        self.lhsOp = fieldAccess.lhsOp;
        self.keyOp = fieldAccess.keyOp;
        self.rhsOp = fieldAccess.rhsOp;
        self.kind = fieldAccess.kind;
    }

    function storeIntValueAtIndex() {
        llvm:LLVMValueRef intArrayStoreFunc = nativeFunctionBuilder.getFunctionValueRef("int_array_store");
        llvm:LLVMValueRef intArrayRef = self.parent.getLocalVarRef(self.lhsOp); 
        llvm:LLVMValueRef indexRef = self.parent.genLoadLocalToTempVar(self.keyOp);
        llvm:LLVMValueRef valueRef = self.parent.getLocalVarRef(self.rhsOp);
        llvm:LLVMValueRef[] arguments = [intArrayRef, indexRef, valueRef];
        _ = llvm:llvmBuildCall(self.builder, intArrayStoreFunc, arguments, 3, "");
    }

    function loadIntValueAtIndex() {
        llvm:LLVMValueRef intArrayLoadFunc = nativeFunctionBuilder.getFunctionValueRef("int_array_load");
        llvm:LLVMValueRef intArrayRef = self.parent.getLocalVarRef(self.rhsOp);
        llvm:LLVMValueRef indexRef = self.parent.genLoadLocalToTempVar(self.keyOp);
        llvm:LLVMValueRef lhsOp = self.parent.getLocalVarRef(self.lhsOp);
        llvm:LLVMValueRef[] arguments = [intArrayRef, indexRef];
        llvm:LLVMValueRef valueInArrayPointer = llvm:llvmBuildCall(self.builder, intArrayLoadFunc, arguments, 2, "");
        llvm:LLVMValueRef valueFromArrayTemp = llvm:llvmBuildLoad(self.builder, valueInArrayPointer, ""); 
        _ = llvm:llvmBuildStore(self.builder, valueFromArrayTemp, lhsOp);
    }

    function genInstruction() {
        if (self.kind is bir:INS_KIND_ARRAY_STORE) {
            self.storeIntValueAtIndex();
        } else if (self.kind is bir:INS_KIND_ARRAY_LOAD){
            self.loadIntValueAtIndex();
        }
    }
};
