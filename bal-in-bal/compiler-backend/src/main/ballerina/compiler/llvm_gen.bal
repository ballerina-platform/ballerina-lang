import ballerina/llvm;
import ballerina/io;

map localVarRefs;
map<llvm:LLVMBasicBlockRef> bbs;

function genPackage(BIRPackage pkg, string path) {
    var c = pkg.org.value + pkg.name.value + pkg.versionValue.value;
    var mod = llvm:LLVMModuleCreateWithName(c);
    var builder = llvm:LLVMCreateBuilder();
    foreach func in pkg.functions {
        genFunction(func, builder, mod);
    }

    optimize(mod);

    llvm:LLVMDumpModule(mod);
    var out = llvm:LLVMWriteBitcodeToFile(mod, path);
    llvm:LLVMDisposeBuilder(builder);
}


function optimize(llvm:LLVMModuleRef mod) {
    llvm:LLVMPassManagerRef pass = llvm:LLVMCreatePassManager();
    llvm:LLVMAddConstantPropagationPass(pass);
    llvm:LLVMAddInstructionCombiningPass(pass);
    llvm:LLVMAddPromoteMemoryToRegisterPass(pass);
    llvm:LLVMAddGVNPass(pass);
    llvm:LLVMAddCFGSimplificationPass(pass);
    llvm:LLVMAddLoopUnrollPass(pass);
    var optResult = llvm:LLVMRunPassManager(pass, mod);
    llvm:LLVMDisposePassManager(pass);

}

function genFunction(BIRFunction func, llvm:LLVMBuilderRef builder, llvm:LLVMModuleRef mod) {
    var name = func.name.value;
    var main_arg_type = llvm:LLVMVoidType();
    var main_return_type = llvm:LLVMInt32Type();
    var functionTy = llvm:LLVMFunctionType0(main_return_type, main_arg_type, 0, 0);
    var main_func = llvm:LLVMAddFunction(mod, name, functionTy);

    var varAllocBB = llvm:LLVMAppendBasicBlock(main_func, "var_allloc");
    llvm:LLVMPositionBuilderAtEnd(builder, varAllocBB);
    foreach localVar in func.localVars{
        llvm:LLVMValueRef llvmValueRef = llvm:LLVMBuildAlloca(builder, llvm:LLVMInt32Type(), localVarName(localVar));
        localVarRefs[localVar.name.value] = llvmValueRef;
    }

    foreach bb in func.basicBlocks {
        genBasicBlockBody(bb, main_func, builder);
    }

    foreach bb in func.basicBlocks {
        genBasicBlockTerminator(bb, builder);
    }

    llvm:LLVMPositionBuilderAtEnd(builder, varAllocBB);

    var brInsRef = llvm:LLVMBuildBr(builder, getBBById("bb0"));
}

function genBasicBlockTerminator(BIRBasicBlock bb, llvm:LLVMBuilderRef builder) {
    var bbRef = getBBById(bb.id.value);
    llvm:LLVMPositionBuilderAtEnd(builder, bbRef);

    match bb.terminator {
        GOTO gotoIns => {
            var brInsRef = llvm:LLVMBuildBr(builder, getBBById(gotoIns.targetBB.id.value));
        }
        Branch brIns => {
            var ifTrue = getBBById(brIns.trueBB.id.value);
            var ifFalse = getBBById(brIns.falseBB.id.value);
            var vrInsRef = llvm:LLVMBuildCondBr(builder, loadOprand(brIns.op, builder), ifTrue, ifFalse);
        }
        Return => {
            var retValueRef = llvm:LLVMBuildLoad(builder, getLocalVarById("%0"), "retrun_temp");
            var ret = llvm:LLVMBuildRet(builder, retValueRef);
        }
    }
}

function genBasicBlockBody(BIRBasicBlock bb, llvm:LLVMValueRef func, llvm:LLVMBuilderRef builder) {
    var bbRef = llvm:LLVMAppendBasicBlock(func, bb.id.value);
    bbs[bb.id.value] = bbRef;
    llvm:LLVMPositionBuilderAtEnd(builder, bbRef);
    foreach i in bb.instructions {
        match i {
            Move moveIns => {
                llvm:LLVMValueRef lhsRef = getLocalVarById(moveIns.lhsOp.variableDcl.name.value);
                var rhsVarOp = moveIns.rhsOp;
                llvm:LLVMValueRef rhsVarOpRef = loadOprand(rhsVarOp, builder);
                var loaded = llvm:LLVMBuildStore(builder, rhsVarOpRef, lhsRef);
            }
            BinaryOp binaryIns => {
                var lhsTmpName = localVarName(binaryIns.lhsOp.variableDcl) + "_temp";
                var lhsRef = getLocalVarById(binaryIns.lhsOp.variableDcl.name.value);
                var rhsOp1 = loadOprand(binaryIns.rhsOp1, builder);
                var rhsOp2 = loadOprand(binaryIns.rhsOp2, builder);
                var kind = binaryIns.kind;
                if (kind == "LESS_THAN"){
                    // LLVMIntSLT = 40
                    var ifReturn = llvm:LLVMBuildICmp(builder, 40, rhsOp1, rhsOp2, lhsTmpName);
                    var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                } else if (kind == "ADD"){
                    var addReturn = llvm:LLVMBuildAdd(builder, rhsOp1, rhsOp2, lhsTmpName);
                    var loaded = llvm:LLVMBuildStore(builder, addReturn, lhsRef);

                }
            }
            ConstantLoad constOp => {
                llvm:LLVMValueRef lhsRef = getLocalVarById(constOp.lhsOp.variableDcl.name.value);
                var constRef = llvm:LLVMConstInt(llvm:LLVMInt32Type(), constOp.value, 0);
                var loaded = llvm:LLVMBuildStore(builder, constRef, lhsRef);
            }

        }
    }
}

function getBBById(string id) returns llvm:LLVMBasicBlockRef {
    match bbs[id] {
        llvm:LLVMBasicBlockRef bb => return bb;
        () => {
            error err = { message: "bb '" + id + "' dosn't exist" };
            throw err;
        }
    }
}


function loadOprand(BIROperand oprand, llvm:LLVMBuilderRef builder) returns llvm:LLVMValueRef {
    match oprand {
        BIRVarRef refOprand => {
            string tempName = localVarName(refOprand.variableDcl) + "_temp";
            return llvm:LLVMBuildLoad(builder, getLocalVarById(refOprand.variableDcl.name.value), tempName);
        }
    }
}

function getLocalVarById(string id) returns llvm:LLVMValueRef {
    return check <llvm:LLVMValueRef>localVarRefs[id];
}

function localVarName(BIRVariableDcl localVar) returns string {
    return localVarNameFromId(localVar.name.value);
}

function localVarNameFromId(string localVarStr) returns string {
    return "local" + localVarStr.substring(1, localVarStr.length());
}
