import ballerina/llvm;

type BbBodyGenrator object {

    llvm:LLVMBuilderRef builder;
    FuncGenrator parent;
    bir:BasicBlock bb;

    new(builder, parent, bb) {
    }

    function genBasicBlockBody() returns BbTermGenrator {
        llvm:LLVMBasicBlockRef bbRef = llvm:LLVMAppendBasicBlock(parent.funcRef, bb.id.value);
        llvm:LLVMPositionBuilderAtEnd(builder, bbRef);
        foreach i in bb.instructions {
            genInstruction(i);
        }
        return new(builder, bb, bbRef, parent);
    }

    function genInstruction(bir:Instruction instruction) {
        match instruction {
            bir:Move moveIns => genMoveIns(moveIns);
            bir:BinaryOp binaryIns => genBinaryOpIns(binaryIns);
            bir:ConstantLoad constIns => genConstantLoadIns(constIns);
        }
    }

    function genMoveIns(bir:Move moveIns) {
        llvm:LLVMValueRef lhsRef = parent.getLocalVarRefById(moveIns.lhsOp.variableDcl.name.value);
        var rhsVarOp = moveIns.rhsOp;
        llvm:LLVMValueRef rhsVarOpRef = parent.genLoadLocalToTempVar(rhsVarOp);
        var loaded = llvm:LLVMBuildStore(builder, rhsVarOpRef, lhsRef);
    }

    function genBinaryOpIns(bir:BinaryOp binaryIns) {
        var lhsTmpName = localVarName(binaryIns.lhsOp.variableDcl) + "_temp";
        var lhsRef = parent.getLocalVarRefById(binaryIns.lhsOp.variableDcl.name.value);
        var rhsOp1 = parent.genLoadLocalToTempVar(binaryIns.rhsOp1);
        var rhsOp2 = parent.genLoadLocalToTempVar(binaryIns.rhsOp2);
        var kind = binaryIns.kind;

        BinaryInsGenrator binaryGen = new(builder, lhsTmpName, lhsRef, rhsOp1, rhsOp2);
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
        llvm:LLVMValueRef lhsRef = parent.getLocalVarRefById(constLoad.lhsOp.variableDcl.name.value);
        var constRef = llvm:LLVMConstInt(llvm:LLVMInt64Type(), constLoad.value, 0);
        var loaded = llvm:LLVMBuildStore(builder, constRef, lhsRef);
    }

};

function findBbRefById(map<BbTermGenrator> bbGenrators, string id) returns llvm:LLVMBasicBlockRef {
    match bbGenrators[id] {
        BbTermGenrator foundBB => return foundBB.bbRef;
        () => {
            error err = { message: "bb '" + id + "' dosn't exist" };
            throw err;
        }
    }
}

function findFuncRefByName(map<FuncGenrator> funcGenrators, bir:Name name) returns llvm:LLVMValueRef {
    match funcGenrators[name.value] {
        FuncGenrator foundFunc => return foundFunc.funcRef;
        any => {
            error err = { message: "function '" + name.value + "' dosn't exist" };
            throw err;
        }
    }
}

