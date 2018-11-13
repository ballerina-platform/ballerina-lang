import ballerina/llvm;

type BbBodyGenrator object {

    llvm:LLVMBuilderRef builder;
    FuncGenrator parent;
    bir:BasicBlock bb;

    new(builder, parent, bb) {
    }

    function genBasicBlockBody() returns BbTermGenrator {
        llvm:LLVMBasicBlockRef bbRef = llvm:LLVMAppendBasicBlock(self.parent.funcRef, self.bb.id.value);
        llvm:LLVMPositionBuilderAtEnd(self.builder, bbRef);
        foreach i in self.bb.instructions {
            self.genInstruction(i);
        }
        return new(self.builder, self.bb, bbRef, self.parent);
    }

    function genInstruction(bir:Instruction instruction) {
        match instruction {
            bir:Move moveIns => self.genMoveIns(moveIns);
            bir:BinaryOp binaryIns => self.genBinaryOpIns(binaryIns);
            bir:ConstantLoad constIns => self.genConstantLoadIns(constIns);
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
        match kind {
            bir:ADD => binaryGen.genAdd();
            bir:DIV => binaryGen.genDiv();
            bir:EQUAL => binaryGen.genEqual();
            bir:GREATER_EQUAL => binaryGen.genGreaterEqual();
            bir:GREATER_THAN => binaryGen.genGreaterThan();
            bir:LESS_EQUAL => binaryGen.genLessEqual();
            bir:LESS_THAN => binaryGen.genLessThan();
            bir:MUL => binaryGen.genMul();
            bir:NOT_EQUAL => binaryGen.genNotEqual();
            bir:SUB => binaryGen.genSub();
        }

    }

    function genConstantLoadIns(bir:ConstantLoad constLoad) {
        llvm:LLVMValueRef lhsRef = self.parent.getLocalVarRefById(constLoad.lhsOp.variableDcl.name.value);
        var constRef = llvm:LLVMConstInt(llvm:LLVMInt64Type(), constLoad.value, 0);
        var loaded = llvm:LLVMBuildStore(self.builder, constRef, lhsRef);
    }

};

function findBbRefById(map<BbTermGenrator> bbGenrators, string id) returns llvm:LLVMBasicBlockRef {
    match bbGenrators[id] {
        BbTermGenrator foundBB => return foundBB.bbRef;
        () => {
            error err = error("bb '" + id + "' dosn't exist");
            panic err;
        }
    }
}

function findFuncRefByName(map<FuncGenrator> funcGenrators, bir:Name name) returns llvm:LLVMValueRef {
    match funcGenrators[name.value] {
        FuncGenrator foundFunc => return foundFunc.funcRef;
        any => {
            error err = error("function '" + name.value + "' dosn't exist");
            panic err;
        }
    }
}

