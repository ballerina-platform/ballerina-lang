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
            return; // Since no other cast is yet supported.
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
        self.bitCastLhs();
        self.storeTag();
        self.storeActual();
    }

    function bitCastLhs() {
        boolean shouldBitCast = self.isRhsSubtypeOfCast();
        if (shouldBitCast) {
            llvm:LLVMValueRef bitCastedStruct = self.buildBitCastedUnion(self.lhsOp, self.rhsOp.typeValue);
            self.parent.storeValueRefForLocalVarRef(bitCastedStruct, self.lhsOp);
        }
    }

    function isRhsSubtypeOfCast() returns boolean {
        int unionTypeTagValue = getTag(self.castType);
        int memberTypeTagValue = getTag(self.rhsOp.typeValue);
        return (memberTypeTagValue < unionTypeTagValue);
    }

    function buildBitCastedUnion(bir:VarRef variable, bir:BType bitcastType) returns llvm:LLVMValueRef {
        string castInsName = self.genStructCastName(localVariableNameWithPrefix(variable), getTypeStringName(bitcastType));
        llvm:LLVMValueRef genericUnion = self.parent.getLocalVarRef(variable);
        llvm:LLVMTypeRef toType = getTypePointerForTaggedStructType(bitcastType, self.builder);
        return llvm:llvmBuildBitCast(self.builder, genericUnion, toType, castInsName);
    }

    function storeTag() {
        llvm:LLVMValueRef tagOfUnion = self.parent.buildStructGepForVariable(self.lhsOp, TAGGED_UNION_FLAG_INDEX);
        int tagValue = getTag(self.rhsOp.typeValue);
        llvm:LLVMValueRef loadedTagValue = getValueRefFromInt(tagValue, 0);
        _ = llvm:llvmBuildStore(self.builder, loadedTagValue, tagOfUnion);
    }

    function storeActual() {
        llvm:LLVMValueRef ptrToActualValueInTaggedUnion =
                        self.parent.buildStructGepForVariable(self.lhsOp, TAGGED_UNION_VALUE_INDEX);
        llvm:LLVMValueRef rhsValue = self.parent.genLoadLocalToTempVar(self.rhsOp);
        _ = llvm:llvmBuildStore(self.builder, rhsValue, ptrToActualValueInTaggedUnion);
    }

    function genUnionToSingleCast() {
        self.bitCastRhs();
        self.storeUnionInSingle();
    }

    function bitCastRhs() {
        llvm:LLVMValueRef bitCastedStruct = self.buildBitCastedUnion(self.rhsOp, self.castType);
        self.parent.storeValueRefForLocalVarRef(bitCastedStruct, self.rhsOp);
    }

    function storeUnionInSingle() {
        llvm:LLVMValueRef actualValueInTaggedUnion =
                    self.parent.loadElementFromStruct(self.rhsOp, TAGGED_UNION_VALUE_INDEX);
        llvm:LLVMValueRef lhsValuePtr = self.parent.getLocalVarRef(self.lhsOp);
        _ = llvm:llvmBuildStore(self.builder, actualValueInTaggedUnion, lhsValuePtr);
    }

    function genStructCastName(string variableName, string castName) returns string {
        return variableName + "_" + castName;
    }
};
