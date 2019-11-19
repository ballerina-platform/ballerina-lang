import ballerina/llvm;
import ballerina/bir;

type BbBodyGenrator object {

    llvm:LLVMBuilderRef builder;
    FuncGenrator parent;
    bir:BasicBlock bb;

    function __init(llvm:LLVMBuilderRef builder, FuncGenrator parent, bir:BasicBlock bb) {
        self.builder = builder;
        self.parent = parent;
        self.bb = bb;
    }

    function genBasicBlockBody() returns BbTermGenrator {
        if (self.parent.funcRef is ()) {
            error funcRefErr =  error("funcRefIsNullError", message = "funcref doesnt exist in" +
                self.parent.func.name.value);
            panic funcRefErr;
        }
        llvm:LLVMBasicBlockRef bbRef = llvm:llvmAppendBasicBlock(<llvm:LLVMValueRef>self.parent.funcRef, self.bb.id.value);
        llvm:llvmPositionBuilderAtEnd(self.builder, bbRef);
        foreach var i in self.bb.instructions {
            if (i is ()) {
                continue;
            }
            self.genInstruction(<bir:Instruction>i);
        }
        return new(self.builder, self.bb, bbRef, self.parent);
    }

    function genInstruction(bir:Instruction instruction) {
        if (instruction is bir:Move) {
            self.genMoveIns(instruction);
        } else if (instruction is bir:BinaryOp) {
            self.genBinaryOpIns(instruction);
        } else if (instruction is bir:ConstantLoad) {
            self.genConstantLoadIns(instruction);
        } else if (instruction is bir:TypeCast) {
            self.genTypeCast(instruction);
        }
    }

    function genTypeCast(bir:TypeCast castIns) {
        llvm:LLVMValueRef lhsRef = self.parent.getLocalVarRefById(castIns.lhsOp.variableDcl.name.value);
        llvm:LLVMTypeRef lhsRefType = llvm:llvmTypeOf(lhsRef);
        boolean typesCompatible = checkIfTypesAreCompatible(castIns.castType, lhsRefType);
    }

    function genMoveIns(bir:Move moveIns) {
        llvm:LLVMValueRef lhsRef = self.parent.getLocalVarRefById(moveIns.lhsOp.variableDcl.name.value);
        var rhsVarOp = moveIns.rhsOp;
        llvm:LLVMValueRef rhsVarOpRef = self.parent.genLoadLocalToTempVar(rhsVarOp);
        var loaded = <llvm:LLVMValueRef> llvm:llvmBuildStore(self.builder, rhsVarOpRef, lhsRef);
    }

    function genBinaryOpIns(bir:BinaryOp binaryIns) {
        var lhsTmpName = localVarName(binaryIns.lhsOp.variableDcl) + "_temp";
        var lhsRef = self.parent.getLocalVarRefById(binaryIns.lhsOp.variableDcl.name.value);
        var rhsOp1 = self.parent.genLoadLocalToTempVar(binaryIns.rhsOp1);
        var rhsOp2 = self.parent.genLoadLocalToTempVar(binaryIns.rhsOp2);
        var kind = binaryIns.kind;

        BinaryInsGenrator binaryGen = new(self.builder, lhsTmpName, <llvm:LLVMValueRef>lhsRef, rhsOp1, rhsOp2);
        if (kind == bir:BINARY_ADD) {
            binaryGen.genAdd();
        } else if (kind == bir:BINARY_DIV) {
            binaryGen.genDiv();
        } else if (kind == bir:BINARY_EQUAL) {
            binaryGen.genEqual();
        } else if (kind == bir:BINARY_GREATER_EQUAL) {
            binaryGen.genGreaterEqual();
        } else if (kind == bir:BINARY_GREATER_THAN) {
            binaryGen.genGreaterThan();
        } else if (kind == bir:BINARY_LESS_EQUAL) {
            binaryGen.genLessEqual();
        } else if (kind == bir:BINARY_LESS_THAN) {
            binaryGen.genLessThan();
        } else if (kind == bir:BINARY_MUL) {
            binaryGen.genMul();
        } else if (kind == bir:BINARY_NOT_EQUAL) {
            binaryGen.genNotEqual();
        } else if (kind == bir:BINARY_SUB) {
            binaryGen.genSub();
        }
    }

    function genConstantLoadIns(bir:ConstantLoad constLoad) {
        if !(constLoad.value is int) {
            error err = error("NotIntErr", message = "invalid value in constLoad");
            panic err;
        }
        llvm:LLVMValueRef lhsRef = self.parent.getLocalVarRefById(constLoad.lhsOp.variableDcl.name.value);
        var constRef = llvm:llvmConstInt(llvm:llvmInt64Type(), <int>constLoad.value, 0);
        var loaded = llvm:llvmBuildStore(self.builder, constRef, <llvm:LLVMValueRef>lhsRef);
    }
};

function findBbRefById(map<BbTermGenrator> bbGenrators, string id) returns llvm:LLVMBasicBlockRef {
    if !(bbGenrators[id] is BbTermGenrator) {
        error err = error("NotBbTermGenratorError", message = "bb '" + id + "' doesn't exist");
        panic err;
    }
    BbTermGenrator genrator = <BbTermGenrator>bbGenrators[id];
    return genrator.bbRef;
}

function findFuncRefByName(map<FuncGenrator> funcGenrators, bir:Name name) returns llvm:LLVMValueRef {
    if !(funcGenrators[name.value] is FuncGenrator) {
        error err = error("NotFuncGenratorTypeError", message = "function '" + name.value + "' doesn't exist");
        panic err;
    }
    FuncGenrator genrator = <FuncGenrator> funcGenrators[name.value];
    if (genrator.funcRef is ()) {
        error err = error("FuncRefIsNull", message = "function genrator doesn't have funcion reference");
        panic err;
    }
    return <llvm:LLVMValueRef>genrator.funcRef;
}


