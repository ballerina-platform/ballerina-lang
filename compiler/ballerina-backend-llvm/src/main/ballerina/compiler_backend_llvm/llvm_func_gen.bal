import ballerina/llvm;
import ballerina/io;
import ballerina/bir;

type FuncGenrator object {
    bir:Function func;
    llvm:LLVMValueRef funcRef = {};
    llvm:LLVMModuleRef mod;
    map<llvm:LLVMValueRef> localVarRefs = {};
    llvm:LLVMValueRef varAllocBB = {};
    llvm:LLVMBuilderRef builder;

    function __init(llvm:LLVMModuleRef mod, llvm:LLVMBuilderRef builder, bir:Function func) {
        self.mod = mod;
        self.builder = builder;
        self.func = func;
    }

    function genFunctionDecl() {
        var name = self.func.name.value;
        llvm:LLVMTypeRef[] argTypes = self.genFunctionArgTypes(self.func.argsCount);
        var retTypeRef = genBType(self.func.typeValue.retType);
        var functionType = llvm:LLVMFunctionType1(retTypeRef, argTypes, self.func.argsCount, 0);
        self.funcRef = llvm:LLVMAddFunction(self.mod, name, functionType);
    }

    function genFunctionArgTypes(int argsCount) returns llvm:LLVMTypeRef[] {
        if (self.func.argsCount == 0){
            return self.genVoidFunctionArgTypes();
        } else {
            return self.genNonVoidFunctionArgTypes(argsCount);
        }
    }

    function genVoidFunctionArgTypes() returns llvm:LLVMTypeRef[] {
        return [llvm:LLVMVoidType()];
    }

    function genNonVoidFunctionArgTypes(int argsCount) returns llvm:LLVMTypeRef[] {
        llvm:LLVMTypeRef[] argTypes = [];
        int i = 0;
        while (i < self.func.argsCount) {
            argTypes[i] = llvm:LLVMInt64Type();
            i += 1;
        }
        return argTypes;
    }

    function genFunctionBody(map<FuncGenrator> funcGenrators) {
        self.genLocalVarAllocationBbBody();
        var bbTermGenrators = self.genBbBodies();
        self.genLocalVarAllocationBBTerminator(bbTermGenrators);
        self.genBbTerminators(funcGenrators, bbTermGenrators);
    }

    function genLocalVarAllocationBbBody() {
        self.varAllocBB = self.genBbDecl("var_allloc");
        int paramIndex = 0;
        foreach var lVar in self.func.localVars{
            bir:VariableDcl localVar = <bir:VariableDcl> lVar;
            var varName = localVarName(localVar);
            var varType = genBType(localVar.typeValue);
            llvm:LLVMValueRef localVarRef = llvm:LLVMBuildAlloca(self.builder, varType, varName);
            self.localVarRefs[localVar.name.value] = localVarRef;

            if (self.isParamter(localVar)){
                var parmRef = llvm:LLVMGetParam(self.funcRef, paramIndex);
                var loaded = llvm:LLVMBuildStore(self.builder, parmRef, localVarRef);
                paramIndex += 1;
            }
        }
    }

    function isParamter(bir:VariableDcl localVar) returns boolean {
        var kind = localVar.kind;
        if (kind is bir:ArgVarKind) {
            return true;
        }

        return false;
    }

    function genBbBodies() returns map<BbTermGenrator> {
        map<BbTermGenrator> bbTermGenrators = {};
        foreach var bBlock in self.func.basicBlocks {
            bir:BasicBlock bb = <bir:BasicBlock> bBlock;
            BbBodyGenrator g = new(self.builder, self, bb);
            bbTermGenrators[bb.id.value] = g.genBasicBlockBody();
        }
        return bbTermGenrators;
    }

    function genLocalVarAllocationBBTerminator(map<BbTermGenrator> bbTermGenrators) {
        llvm:LLVMPositionBuilderAtEnd(self.builder, self.varAllocBB);
        var brInsRef = llvm:LLVMBuildBr(self.builder, findBbRefById(bbTermGenrators, "bb0"));
    }

    function genBbTerminators(map<FuncGenrator> funcGenrators, map<BbTermGenrator> bbTermGenrators) {
        foreach var (k, g) in bbTermGenrators {
            g.genBasicBlockTerminator(funcGenrators, bbTermGenrators);
        }
    }

    function genLoadLocalToTempVar(bir:Operand oprand) returns llvm:LLVMValueRef {
        bir:VarRef refOprand = oprand;
        string tempName = localVarName(refOprand.variableDcl) + "_temp";
        var localVarRef = self.getLocalVarRefById(refOprand.variableDcl.name.value);
        return llvm:LLVMBuildLoad(self.builder, localVarRef, tempName);
    }

    function getLocalVarRefById(string id) returns llvm:LLVMValueRef {
        var result = self.localVarRefs[id];

        if (result is llvm:LLVMValueRef) {
            return result;
        } else {
            error err = error("Local var by name '" + id + "' dosn't exist in " + self.func.name.value);
            panic err;
        }
    }

    function genBbDecl(string name) returns llvm:LLVMValueRef {
        var bbRef = llvm:LLVMAppendBasicBlock(self.funcRef, name);
        llvm:LLVMPositionBuilderAtEnd(self.builder, bbRef);
        return bbRef;
    }

    function isVoidFunc() returns boolean {
        return self.func.typeValue.retType != "()";
    }
};

