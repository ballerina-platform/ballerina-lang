import ballerina/llvm;
import ballerina/io;

type FuncGenrator object {
    BIRFunction func,
    llvm:LLVMValueRef funcRef,
    llvm:LLVMModuleRef mod,
    map<llvm:LLVMValueRef> localVarRefs,
    llvm:LLVMBuilderRef builder;

    new(mod, builder, func) {
    }

    function genFunctionDecl() {
        var name = func.name.value;
        llvm:LLVMTypeRef[] argTypes = genFunctionArgTypes(func.argsCount);
        var retTypeRef = genBType(func.typeValue.retType);
        var functionType = llvm:LLVMFunctionType1(retTypeRef, argTypes, func.argsCount, 0);
        funcRef = llvm:LLVMAddFunction(mod, name, functionType);
    }

    function genFunctionArgTypes(int argsCount) returns llvm:LLVMTypeRef[] {
        if (func.argsCount == 0){
            return genVoidFunctionArgTypes();
        } else {
            return genNonVoidFunctionArgTypes(argsCount);
        }
    }

    function genVoidFunctionArgTypes() returns llvm:LLVMTypeRef[] {
        return [llvm:LLVMVoidType()];
    }

    function genNonVoidFunctionArgTypes(int argsCount) returns llvm:LLVMTypeRef[] {
        llvm:LLVMTypeRef[] argTypes = [];
        int i = 0;
        while (i < func.argsCount) {
            argTypes[i] = llvm:LLVMInt64Type();
            i++;
        }
        return argTypes;
    }

    function genFunctionBody(map<FuncGenrator> funcGenrators) {
        var varAllocBB = genBbDecl("var_allloc");
        int i = 0;
        foreach localVar in func.localVars{
            var varName = localVarName(localVar);
            var varType = genBType(localVar.typeValue);
            llvm:LLVMValueRef llvmValueRef;
            llvmValueRef = llvm:LLVMBuildAlloca(builder, varType, varName);
            localVarRefs[func.name.value + "." + localVar.name.value] = llvmValueRef;
            match localVar.kind {
                ArgVarKind => {
                    var parmRef = llvm:LLVMGetParam(funcRef, i);
                    var loaded = llvm:LLVMBuildStore(builder, parmRef, llvmValueRef);
                    i++;
                }
                any => {}
            }
        }

        map<BbTermGenrator> bbTermGenrators;
        foreach bb in func.basicBlocks {
            BbBodyGenrator g = new(builder, funcRef, func, self, bb);
            bbTermGenrators[bb.id.value] = g.genBasicBlockBody();
        }

        llvm:LLVMPositionBuilderAtEnd(builder, varAllocBB);
        var brInsRef = llvm:LLVMBuildBr(builder, findBbRefById(bbTermGenrators, "bb0"));

        foreach g in bbTermGenrators {
            g.genBasicBlockTerminator(funcGenrators, bbTermGenrators);
        }

    }

    function genVarLoad(BIROperand oprand) returns llvm:LLVMValueRef {
        match oprand {
            BIRVarRef refOprand => {
                string tempName = localVarName(refOprand.variableDcl) + "_temp";
                return llvm:LLVMBuildLoad(builder, getLocalVarRefById(func, refOprand.variableDcl.name.value),
                    tempName);
            }

        }
    }

    function getLocalVarRefById(BIRFunction fun, string id) returns llvm:LLVMValueRef {
        match localVarRefs[fun.name.value + "." + id] {
            llvm:LLVMValueRef varRef => return varRef;
            any => {
                error err = { message: "Local var by name '" + id + "' dosn't exist in " + fun.name.value };
                throw err;
            }
        }
    }

    function genBbDecl(string name) returns llvm:LLVMValueRef {
        var bbRef = llvm:LLVMAppendBasicBlock(funcRef, name);
        llvm:LLVMPositionBuilderAtEnd(builder, bbRef);
        return bbRef;
    }

};

