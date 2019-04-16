import ballerina/llvm;

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
        llvm:LLVMBasicBlockRef bbRef = llvm:LLVMAppendBasicBlock(self.parent.funcRef, self.bb.id.value);
        llvm:LLVMPositionBuilderAtEnd(self.builder, bbRef);
        foreach var i in self.bb.instructions {
            bir:Instruction ins = <bir:Instruction>i;
            self.genInstruction(ins);
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
        } else {
            error err = error("Invalid bir:Instruction");
            panic err;
        }
    }

    function genMoveIns(bir:Move moveIns) {
        llvm:LLVMValueRef lhsRef = self.parent.getLocalVarRefById(moveIns.lhsOp.variableDcl.name.value);
        var rhsVarOp = moveIns.rhsOp;
        llvm:LLVMValueRef rhsVarOpRef = self.parent.genLoadLocalToTempVar(rhsVarOp);
        var loaded = llvm:LLVMBuildStore(self.builder, rhsVarOpRef, lhsRef);
    }

    function genBinaryOpIns(bir:BinaryOp binaryIns) {
        var lhsTmpName = localVarName(binaryIns.lhsOp.variableDcl) + "_temp";
        var lhsRef = self.parent.getLocalVarRefById(binaryIns.lhsOp.variableDcl.name.value);
        var rhsOp1 = self.parent.genLoadLocalToTempVar(binaryIns.rhsOp1);
        var rhsOp2 = self.parent.genLoadLocalToTempVar(binaryIns.rhsOp2);
        var kind = binaryIns.kind;

        BinaryInsGenrator binaryGen = new(self.builder, lhsTmpName, lhsRef, rhsOp1, rhsOp2);

        if (kind is bir:ADD) {
            binaryGen.genAdd();
        } else if (kind is bir:DIV) {
            binaryGen.genDiv();
        } else if (kind is bir:EQUAL) {
            binaryGen.genEqual();
        } else if (kind is bir:GREATER_EQUAL) {
            binaryGen.genGreaterEqual();
        } else if (kind is bir:GREATER_THAN) {
            binaryGen.genGreaterThan();
        } else if (kind is bir:LESS_EQUAL) {
            binaryGen.genLessEqual();
        } else if (kind is bir:LESS_THAN) {
            binaryGen.genLessThan();
        } else if (kind is bir:MUL) {
            binaryGen.genMul();
        } else if (kind is bir:NOT_EQUAL) {
            binaryGen.genNotEqual();
        } else {
            binaryGen.genSub();
        }
    }

    function genConstantLoadIns(bir:ConstantLoad constLoad) {
        llvm:LLVMValueRef lhsRef = self.parent.getLocalVarRefById(constLoad.lhsOp.variableDcl.name.value);
        var constRef = llvm:LLVMConstInt(llvm:LLVMInt64Type(), constLoad.value, 0);
        var loaded = llvm:LLVMBuildStore(self.builder, constRef, lhsRef);
    }

};

function findBbRefById(map<BbTermGenrator> bbGenrators, string id) returns llvm:LLVMBasicBlockRef {
    var result = bbGenrators[id];
    if (result is BbTermGenrator) {
        return result.bbRef;
    } else {
        error err = error("bb '" + id + "' dosn't exist");
        panic err;
    }
}

function findFuncRefByName(map<FuncGenrator> funcGenrators, bir:Name name) returns llvm:LLVMValueRef {
    var result = funcGenrators[name.value];
    if (result is FuncGenrator) {
            return result.funcRef;
    } else {
        error err = error("function '" + name.value + "' dosn't exist");
        panic err;
    }
}

