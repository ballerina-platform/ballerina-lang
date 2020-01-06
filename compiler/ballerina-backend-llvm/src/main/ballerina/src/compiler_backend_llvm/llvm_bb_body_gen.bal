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
        } else if (instruction is bir:TypeTest) {
            self.genTypeTest(instruction);
        }
    }

    function genTypeTest(bir:TypeTest typeTestIns) {
        bir:BType rhsOpType = typeTestIns.rhsOp.typeValue;
        boolean isLhsUnionType = isUnionType(rhsOpType);
        if (isLhsUnionType) {
            llvm:LLVMValueRef unionTagValue =
                        self.parent.loadElementFromStruct(typeTestIns.rhsOp, TAGGED_UNION_FLAG_INDEX);
            llvm:LLVMValueRef valueOfType = getValueRefFromInt(getTag(typeTestIns.typeValue), 0);
            llvm:LLVMValueRef isEq = llvm:llvmBuildICmp(self.builder, llvm:LLVMIntEQ, unionTagValue, valueOfType, "typeTest");
            _ = llvm:llvmBuildStore(self.builder, isEq, self.parent.getLocalVarRef(typeTestIns.lhsOp));
        } 
    }

    function genTypeCast(bir:TypeCast castIns) {
        CastInsGenerator castGen = new(self.builder, self.parent, castIns);
        castGen.genTypeCast();
    }

    function genBinaryOpIns(bir:BinaryOp binaryIns) {
        var lhsTmpName = localVariableNameWithPrefix(binaryIns.lhsOp.variableDcl) + "_temp";
        var lhsRef = self.parent.getLocalVarRef(binaryIns.lhsOp);
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
        if (constLoad.value is int) {
            self.genIntConstLoadIns(constLoad);
        } else if (constLoad.value is boolean) {
            self.genBoolConstLoadIns(constLoad);
        } else {
            panic error("InvalidDataTypeForConstLoad", message = "Unsupported Data type provided for constant load");
        }
    }

    function genIntConstLoadIns(bir:ConstantLoad constLoad) {
        llvm:LLVMValueRef lhsRef = self.parent.getLocalVarRef(constLoad.lhsOp);
        var constRef = llvm:llvmConstInt(llvm:llvmInt64Type(), <int>constLoad.value, 0);
        _ = llvm:llvmBuildStore(self.builder, constRef, <llvm:LLVMValueRef>lhsRef);
    }

    function genBoolConstLoadIns(bir:ConstantLoad constLoad) {
        llvm:LLVMValueRef lhsRef = self.parent.getLocalVarRef(constLoad.lhsOp);
        var constRef = llvm:llvmConstInt(llvm:llvmInt1Type(), <boolean>constLoad.value?1:0, 0);
        _ = llvm:llvmBuildStore(self.builder, constRef, <llvm:LLVMValueRef>lhsRef);
    }

    function genMoveIns(bir:Move moveIns) {
        if (moveIns.lhsOp.typeValue is bir:BUnionType) {
            self.castLhsOpForMoveIns(moveIns);
        }
        llvm:LLVMValueRef lhsRef = self.parent.getLocalVarRef(moveIns.lhsOp);
        var rhsVarOp = moveIns.rhsOp;
        llvm:LLVMValueRef rhsVarOpRef = self.parent.genLoadLocalToTempVar(rhsVarOp);
        _ = llvm:llvmBuildStore(self.builder, rhsVarOpRef, lhsRef);
    }

    function castLhsOpForMoveIns (bir:Move moveIns) {
        llvm:LLVMTypeRef castTypePointer = llvm:llvmTypeOf(self.parent.getLocalVarRef(moveIns.rhsOp));
        llvm:LLVMValueRef castedLhsValPointer =
          llvm:llvmBuildBitCast(self.builder, self.parent.getLocalVarRef(moveIns.lhsOp), castTypePointer, "moveTempBitCast");
        self.parent.storeValueRefForLocalVarRef(castedLhsValPointer, moveIns.lhsOp);
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


