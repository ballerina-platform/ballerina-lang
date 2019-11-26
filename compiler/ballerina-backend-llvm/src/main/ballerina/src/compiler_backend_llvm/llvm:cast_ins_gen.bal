import ballerina/llvm;
import ballerina/bir;

type CastInsGenerator object {

    llvm:LLVMBuilderRef builder;
    FuncGenrator parent;
    bir:VarRef lhsOp;
    bir:VarRef rhsOp;
    bir:BType castType;

    function __init(llvm:LLVMBuilderRef builder, FuncGenrator parent, bir:TypeCast castIns) {
        self.builder = builder;
        self.parent = parent;
        self.lhsOp = castIns.lhsOp;
        self.rhsOp = castIns.rhsOp;
        self.castType = castIns.castType;
    }

    function genTypeCast() {
        if (self.isUnionToSingleCast()) {
            self.genUnionToSingleCast();
        } else if (self.isSingleToUnionCast()) {
            self.genSingleToUnionCast();
        } else {
            return;
        }
    }

    function isUnionToSingleCast() returns boolean {
        boolean isCastTypeUnion = isUnionType(self.castType);
        boolean isRhsTypeUnion = isUnionType(self.rhsOp.typeValue);
        return (!isCastTypeUnion && isRhsTypeUnion);
    }

    function isSingleToUnionCast() returns boolean {
        boolean isCastTypeUnion = isUnionType(self.castType);
        boolean isRhsTypeUnion = isUnionType(self.rhsOp.typeValue);
        return (isCastTypeUnion && !isRhsTypeUnion);
    }

    function genSingleToUnionCast() {
        self.genCorrectUnionTypeToStore(self.lhsOp, self.rhsOp);
        self.storeTagForTaggedUnion(self.lhsOp, self.rhsOp.typeValue);
        self.storeActualValueInTaggedUnion(self.lhsOp, self.rhsOp);
    }

    function storeTagForTaggedUnion(bir:VarRef variable, bir:BType typeValue) {
        llvm:LLVMValueRef tagOfUnion = self.parent.buildStructGepForVariable(variable, TAGGED_UNION_FLAG_INDEX);
        self.storeTagValueInTaggedUnion(typeValue, tagOfUnion);
    }

    function storeActualValueInTaggedUnion(bir:VarRef lhsVariable, bir:VarRef rhsVariable) {
        llvm:LLVMValueRef ptrToActualValueInTaggedUnion =
                        self.parent.buildStructGepForVariable(lhsVariable, TAGGED_UNION_VALUE_INDEX);
        llvm:LLVMValueRef rhsValue = self.parent.genLoadLocalToTempVar(rhsVariable);
        _ = llvm:llvmBuildStore(self.builder, rhsValue, ptrToActualValueInTaggedUnion);
    }

    function genCorrectUnionTypeToStore(bir:VarRef lhsVariable, bir:VarRef rhsVariable) {
        boolean shouldBitCast = self.isLowerOrderTypeOfUnion(lhsVariable.typeValue, rhsVariable.typeValue);
        if (shouldBitCast) {
            llvm:LLVMValueRef bitCastedStruct = self.buildBitCastedUnion(lhsVariable, rhsVariable.typeValue);
            self.parent.storeValueRefForLocalVarRef(bitCastedStruct, self.lhsOp);
        }
    }

    function buildBitCastedUnion(bir:VarRef variable, bir:BType bitcastType) returns llvm:LLVMValueRef {
        string castInsName = genStructCastName(localVariableNameWithPrefix(variable), getTypeStringName(bitcastType));
        llvm:LLVMValueRef genericUnion = self.parent.getLocalVarRef(variable);
        llvm:LLVMTypeRef toType = getTypePointerForTaggedStructType((bitcastType));
        return llvm:llvmBuildBitCast(self.builder, genericUnion, toType, castInsName);
    }

    function isLowerOrderTypeOfUnion(bir:BType unionType, bir:BType member) returns boolean {
        int unionTypeTagValue = getTagValue(unionType);
        int memberTypeTagValue = getTagValue(member);
        return (memberTypeTagValue < unionTypeTagValue);
    }

    function genUnionToSingleCast() {
        llvm:LLVMValueRef actualValueInTaggedUnion =
                    self.parent.loadElementFromStruct(self.rhsOp, TAGGED_UNION_VALUE_INDEX);
        llvm:LLVMValueRef lhsValuePtr = self.parent.getLocalVarRef(self.lhsOp);
        _ = llvm:llvmBuildStore(self.builder, actualValueInTaggedUnion, lhsValuePtr);
    }

    function storeTagValueInTaggedUnion(bir:BType typeValue, llvm:LLVMValueRef tagOfUnion) {
        int tagValue = getTagValue(typeValue);
        llvm:LLVMValueRef loadedTagValue = getValueRefFromInt(tagValue, 0);
        _ = llvm:llvmBuildStore(self.builder, loadedTagValue, tagOfUnion);
    }

    function getLhsType(bir:VarRef variable) returns llvm:LLVMTypeRef {
        return self.parent.getLocalVarTypeRefById(localVariableName(variable));
    }
};
