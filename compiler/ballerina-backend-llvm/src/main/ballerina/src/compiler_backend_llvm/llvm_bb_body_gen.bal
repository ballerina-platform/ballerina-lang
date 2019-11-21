import ballerina/llvm;
import ballerina/bir;
//import ballerina/io;

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

    //TODO : Remove this typeCast function as I have written a better version
    //function genTypeCast(bir:TypeCast castIns) {
    //    bir:VariableDcl lhsVariableDcl = castIns.lhsOp.variableDcl;
    //    llvm:LLVMTypeRef lhsType = self.getLhsType(lhsVariableDcl);
    //    boolean typesCompatible = checkIfTypesAreCompatible(castIns.castType, lhsType);
    //    if (!typesCompatible) {
    //        error typesNotCompatibleError = error("SimpleEroor", message = "Types Not Compatible for Type Cast");
    //        panic typesNotCompatibleError;
    //    }
    //    llvm:LLVMValueRef lhsVarValueRef = self.parent.getLocalVarRefById("1");
    //    string tagOfUnion = genStructGepName(localVariableName(lhsVariableDcl), TAGGED_UNION_FLAG_INDEX);
    //    string lhsVariableName = localVariableName(lhsVariableDcl);
    //    io:println(lhsVariableName);
    //    //llvm:LLVMValueRef taggedStructFlag = llvm:llvmBuildStructGEP(self.builder, lhsVarValueRef, 1, "");
    //    //self.parent.testStructElementPointer();
    //}

    function genTypeCast(bir:TypeCast castIns) {
        if (self.isUnionToSingleCast(castIns)) {
            return;
        } else if (self.isSingleToUnionCast(castIns)) {
            self.genSingleToUnionCast(castIns);
        } else {
            return;
        }
    }

    function isUnionToSingleCast(bir:TypeCast castIns) returns boolean {
        boolean isCastTypeUnion = isUnionType(castIns.castType);
        boolean isRhsTypeUnion = isUnionType(castIns.rhsOp.typeValue);
        return (!isCastTypeUnion && isRhsTypeUnion);
    }
    
    function isSingleToUnionCast(bir:TypeCast castIns) returns boolean {
        boolean isCastTypeUnion = isUnionType(castIns.castType);
        boolean isRhsTypeUnion = isUnionType(castIns.rhsOp.typeValue);
        return (isCastTypeUnion && !isRhsTypeUnion);
    }

    function genSingleToUnionCast(bir:TypeCast castIns) {
        if (self.isLhsTypePartOfCastUnion(castIns)) {
            llvm:LLVMValueRef tagOfUnion = self.buildStructGepForVariable(castIns.lhsOp.variableDcl,TAGGED_UNION_FLAG_INDEX);
            self.storeTagValueInTaggedUnion(castIns.rhsOp.typeValue, tagOfUnion);
        }
    }

    function storeTagValueInTaggedUnion(bir:BType typeValue, llvm:LLVMValueRef tagOfUnion) {
        int tagValue = getTagValue(typeValue);
        llvm:LLVMValueRef loadTagValue = llvm:llvmConstInt(llvm:llvmInt64Type(), tagValue, 0);
        llvm:LLVMValueRef storeTagValue = llvm:llvmBuildStore(self.builder, tagOfUnion, loadTagValue);
    }

    function buildStructGepForVariable(bir:VariableDcl variable, int elementIndex) returns llvm:LLVMValueRef {
            string elementName = genStructGepName(localVariableName(variable), elementIndex);
            llvm:LLVMValueRef rhsValueRef = self.parent.getLocalVarRef(variable);
            return llvm:llvmBuildStructGEP(self.builder, rhsValueRef, elementIndex, elementName);
    }

    function isLhsTypePartOfCastUnion(bir:TypeCast castIns) returns boolean {
        bir:BUnionType castUnionType = <bir:BUnionType>castIns.castType;
        bir:BType lhsType = castIns.lhsOp.typeValue;
        foreach var typeMember in castUnionType.members {
            if (typeMember is lhsType) {
                return true;
            }
        }
        return false;
    }

    function getLhsType(bir:VariableDcl variable) returns llvm:LLVMTypeRef {
        return self.parent.getLocalVarTypeRefById(localVariableName(variable));
    }

    function genMoveIns(bir:Move moveIns) {
        llvm:LLVMValueRef lhsRef = self.parent.getLocalVarRef(moveIns.lhsOp.variableDcl);
        var rhsVarOp = moveIns.rhsOp;
        llvm:LLVMValueRef rhsVarOpRef = self.parent.genLoadLocalToTempVar(rhsVarOp);
        var loaded = <llvm:LLVMValueRef> llvm:llvmBuildStore(self.builder, rhsVarOpRef, lhsRef);
    }

    function genBinaryOpIns(bir:BinaryOp binaryIns) {
        var lhsTmpName = localVarNameWithPreFix(binaryIns.lhsOp.variableDcl) + "_temp";
        var lhsRef = self.parent.getLocalVarRef(binaryIns.lhsOp.variableDcl);
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
        llvm:LLVMValueRef lhsRef = self.parent.getLocalVarRef(constLoad.lhsOp.variableDcl);
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


