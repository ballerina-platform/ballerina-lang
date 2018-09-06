import ballerina/llvm;
import ballerina/io;
import ballerina/bir;

type FuncGenrator object {
    bir:BIRFunction func,
    llvm:LLVMValueRef funcRef,
    llvm:LLVMModuleRef mod,
    llvm:LLVMBuilderRef builder;

    new(mod, builder, func) {
    }

    function genFunctionDecl() {
        var name = func.name.value;
        llvm:LLVMTypeRef[] argTypes = genFunctionArgTypes(func.argsCount);
        var retTypeRef = genBType(func.typeValue.retType);
        var functionType = llvm:LLVMFunctionType1(retTypeRef, argTypes, func.argsCount, 0);
        funcRef = llvm:LLVMAddFunction(mod, name, functionType);
        functionRefs[name] = funcRef;
    }

    function genFunctionArgTypes(int argsCount) returns llvm:LLVMTypeRef[] {
        if (func.argsCount == 0){
            return [llvm:LLVMVoidType()];
        } else {
            llvm:LLVMTypeRef[] argTypes = [];
            int i = 0;
            while (i < func.argsCount) {
                argTypes[i] = llvm:LLVMInt64Type();
                i++;
            }
            return argTypes;
        }
    }

    function genFunctionBody(map<FuncGenrator> funcGenrators) {
        var varAllocBB = llvm:LLVMAppendBasicBlock(funcRef, "var_allloc");
        llvm:LLVMPositionBuilderAtEnd(builder, varAllocBB);
        int argI = 0;
        foreach localVar in func.localVars{
            var varName = localVarName(localVar);
            var varType = genBType(localVar.typeValue);
            llvm:LLVMValueRef llvmValueRef;
            llvmValueRef = llvm:LLVMBuildAlloca(builder, varType, varName);
            localVarRefs[func.name.value + "." + localVar.name.value] = llvmValueRef;
            match localVar.kind {
                bir:ArgVarKind => {
                    var parmRef = llvm:LLVMGetParam(funcRef, argI);
                    var loaded = llvm:LLVMBuildStore(builder, parmRef, llvmValueRef);
                    argI++;
                }
                any => {}
            }
        }

        map<BbGenrator> bbGenrators;
        foreach bb in func.basicBlocks {
            BbGenrator g = new(builder, funcRef, func, bb);
            var bbRef = g.genBasicBlockBody();
            bbGenrators[bb.id.value] = g;
        }

        llvm:LLVMPositionBuilderAtEnd(builder, varAllocBB);
        var brInsRef = llvm:LLVMBuildBr(builder, findBbRefById(bbGenrators, "bb0"));

        foreach g in bbGenrators {
            g.genBasicBlockTerminator(funcGenrators, bbGenrators);
        }

    }

};

