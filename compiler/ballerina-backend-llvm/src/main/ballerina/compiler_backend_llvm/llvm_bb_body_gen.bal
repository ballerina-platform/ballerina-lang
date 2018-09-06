import ballerina/llvm;
import ballerina/io;

type BbBodyGenrator object {

    llvm:LLVMBuilderRef builder,
    llvm:LLVMValueRef funcRef,
    FuncGenrator parent,
    BIRFunction func,
    BIRBasicBlock bb;

    new(builder, funcRef, func, parent, bb) {
    }

    function genBasicBlockBody() returns BbTermGenrator {
        llvm:LLVMBasicBlockRef bbRef = llvm:LLVMAppendBasicBlock(funcRef, bb.id.value);
        llvm:LLVMPositionBuilderAtEnd(builder, bbRef);
        foreach i in bb.instructions {
            genInstruction(i);
        }
        return new(builder, funcRef, func, bb, bbRef, parent);
    }

    function genInstruction(BIRInstruction instruction) {
        match instruction {
            Move moveIns => genMoveIns(moveIns);
            BinaryOp binaryIns => genBinaryOpIns(binaryIns);
            ConstantLoad constIns => genConstantLoadIns(constIns);
        }
    }

    function genMoveIns(Move moveIns) {
        llvm:LLVMValueRef lhsRef = parent.getLocalVarRefById(func, moveIns.lhsOp.variableDcl.name.value);
        var rhsVarOp = moveIns.rhsOp;
        llvm:LLVMValueRef rhsVarOpRef = parent.genVarLoad(rhsVarOp);
        var loaded = llvm:LLVMBuildStore(builder, rhsVarOpRef, lhsRef);
    }

    function genBinaryOpIns(BinaryOp binaryIns) {
        var lhsTmpName = localVarName(binaryIns.lhsOp.variableDcl) + "_temp";
        var lhsRef = parent.getLocalVarRefById(func, binaryIns.lhsOp.variableDcl.name.value);
        var rhsOp1 = parent.genVarLoad(binaryIns.rhsOp1);
        var rhsOp2 = parent.genVarLoad(binaryIns.rhsOp2);
        var kind = binaryIns.kind;

        BinaryInsGenrator binaryGen = new(builder, lhsTmpName, lhsRef, rhsOp1, rhsOp2);
        match kind {
            ADD => binaryGen.genAdd();
            DIV => binaryGen.genDiv();
            EQUAL => binaryGen.genEqual();
            GREATER_EQUAL => binaryGen.genGreaterEqual();
            GREATER_THAN => binaryGen.genGreaterThan();
            LESS_EQUAL => binaryGen.genLessEqual();
            LESS_THAN => binaryGen.genLessThan();
            MUL => binaryGen.genMul();
            NOT_EQUAL => binaryGen.genNotEqual();
            SUB => binaryGen.genSub();
        }

    }

    function genConstantLoadIns(ConstantLoad constLoad) {
        llvm:LLVMValueRef lhsRef = parent.getLocalVarRefById(func, constLoad.lhsOp.variableDcl.name.value);
        var constRef = llvm:LLVMConstInt(llvm:LLVMInt64Type(), constLoad.value, 0);
        var loaded = llvm:LLVMBuildStore(builder, constRef, lhsRef);
    }

};

function findBbRefById(map<BbTermGenrator> bbGenrators, string id) returns llvm:LLVMBasicBlockRef {
    match bbGenrators[id] {
        BbTermGenrator foundBB => {
            return foundBB.bbRef;
        }
        any => {
            error err = { message: "bb '" + id + "' dosn't exist" };
            throw err;
        }
    }
}

function findFuncRefByName(map<FuncGenrator> funcGenrators, Name name) returns llvm:LLVMValueRef {
    match funcGenrators[name.value] {
        FuncGenrator foundFunc => return foundFunc.funcRef;
        any => {
            error err = { message: "function '" + name.value + "' dosn't exist" };
            throw err;
        }
    }
}

