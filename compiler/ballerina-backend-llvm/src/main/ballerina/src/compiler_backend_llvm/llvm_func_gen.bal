import ballerina/llvm;
import ballerina/io;
import ballerina/bir;

type FuncGenrator object {
    bir:Function func;
    llvm:LLVMValueRef funcRef;
    llvm:LLVMModuleRef mod;
    map<llvm:LLVMValueRef> localVarRefs;
    llvm:LLVMValueRef varAllocBB;
    llvm:LLVMBuilderRef builder;

    function __init(bir:Function func, llvm:LLVMValueRef funcRef, llvm:LLVMModuleRef mod,
                    map<llvm:LLVMValueRef> localVarRefs, llvm:LLVMValueRef varAllocBB, llvm:LLVMBuilderRef builder) {
        self.func = func;
        self.funcRef = funcRef;
        self.mod = mod;
        self.localVarRefs = localVarRefs;
        self.varAllocBB = varAllocBB;
        self.builder = builder;
    }

    function genFunctionDecl() {
        var name = func.name.value;
        llvm:LLVMTypeRef[] argTypes = genFunctionArgTypes(func.argsCount);
        var retTypeRef = genBType(func.typeValue.retType);
        var functionType = llvm:llvmFunctionType1(retTypeRef, argTypes, func.argsCount, 0);
        funcRef = llvm:llvmAddFunction(mod, name, functionType);
    }

    function genFunctionArgTypes(int argsCount) returns llvm:LLVMTypeRef[] {
        if (func.argsCount == 0){
            return genVoidFunctionArgTypes();
        } else {
            return genNonVoidFunctionArgTypes(argsCount);
        }
    }

    function genVoidFunctionArgTypes() returns llvm:LLVMTypeRef[] {
        return [llvm:llvmVoidType()];
    }

    function genNonVoidFunctionArgTypes(int argsCount) returns llvm:LLVMTypeRef[] {
        llvm:LLVMTypeRef[] argTypes = [];
        int i = 0;
        while (i < func.argsCount) {
            argTypes[i] = llvm:llvmInt64Type();
            i += 1;
        }
        return argTypes;
    }

    function genFunctionBody(map<FuncGenrator> funcGenrators) {
        genLocalVarAllocationBbBody();
        var bbTermGenrators = genBbBodies();
        genLocalVarAllocationBBTerminator(bbTermGenrators);
        genBbTerminators(funcGenrators, bbTermGenrators);
    }

    function genLocalVarAllocationBbBody() {
        varAllocBB = genBbDecl("var_allloc");
        int paramIndex = 0;
        foreach var localVar in func.localVars{
            var varName = localVarName(localVar);
            var varType = genBType(localVar.typeValue);
            llvm:LLVMValueRef localVarRef = llvm:llvmBuildAlloca(builder, varType, varName);
            localVarRefs[localVar.name.value] = localVarRef;

            if (isParamter(localVar)){
                var parmRef = llvm:llvmGetParam(funcRef, paramIndex);
                var loaded = llvm:llvmBuildStore(builder, parmRef, localVarRef);
                paramIndex += 1;
            }
        }
    }

    function isParamter(bir:VariableDcl localVar) returns boolean {
        if (localVar.kind is bir:ArgVarKind) {
            return true;
        } else {
            return false;
        }
    }

    function genBbBodies() returns map<BbTermGenrator> {
        map<BbTermGenrator> bbTermGenrators;
        foreach var bb in func.basicBlocks {
            BbBodyGenrator g = new(builder, self, bb);
            bbTermGenrators[bb.id.value] = g.genBasicBlockBody();
        }
        return bbTermGenrators;
    }

    function genLocalVarAllocationBBTerminator(map<BbTermGenrator> bbTermGenrators) {
        llvm:llvmPositionBuilderAtEnd(builder, varAllocBB);
        var brInsRef = llvm:llvmBuildBr(builder, findBbRefById(bbTermGenrators, "bb0"));
    }

    function genBbTerminators(map<FuncGenrator> funcGenrators, map<BbTermGenrator> bbTermGenrators) {
        foreach var g in bbTermGenrators {
            g.genBasicBlockTerminator(funcGenrators, bbTermGenrators);
        }
    }

    function genLoadLocalToTempVar(bir:Operand oprand) returns llvm:LLVMValueRef {
        bir:VarRef refOprand = oprand;
        string tempName = localVarName(refOprand.variableDcl) + "_temp";
        var localVarRef = getLocalVarRefById(refOprand.variableDcl.name.value);
        return llvm:llvmBuildLoad(builder, localVarRef, tempName);
    }

    function getLocalVarRefById(string id) returns llvm:LLVMValueRef|error {
        if (localVarRefs[id] is llvm:LLVMValueRef) {
            return localVarRefs[id];
        } else {
            error err = { message: "Local var by name '" + id + "' dosn't exist in " + func.name.value };
            return err;
        }
    }

    function genBbDecl(string name) returns llvm:LLVMValueRef {
        var bbRef = llvm:llvmAppendBasicBlock(funcRef, name);
        llvm:llvmPositionBuilderAtEnd(builder, bbRef);
        return bbRef;
    }

    function isVoidFunc() returns boolean {
        return func.typeValue.retType != "()";
    }
};



