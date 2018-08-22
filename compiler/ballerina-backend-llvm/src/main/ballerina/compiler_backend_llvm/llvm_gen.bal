import ballerina/llvm;
import ballerina/io;

// TODO: make these non-globle variables
map localVarRefs;
map functionRefs;
map<llvm:LLVMBasicBlockRef> bbs;
llvm:LLVMValueRef printfRef;

function genPackage(BIRPackage pkg, string targetObjectFilePath, boolean dumpLLVMIR) {
    var c = pkg.org.value + pkg.name.value + pkg.versionValue.value;
    var mod = llvm:LLVMModuleCreateWithName(c);
    var builder = llvm:LLVMCreateBuilder();

    // Adding printf decl
    llvm:LLVMTypeRef[] pointer_to_char_type = [llvm:LLVMPointerType(llvm:LLVMInt8Type(), 0)];
    llvm:LLVMTypeRef printfType = llvm:LLVMFunctionType1(llvm:LLVMInt32Type(), pointer_to_char_type, 1, 1);
    printfRef = llvm:LLVMAddFunction(mod, "printf", printfType);

    // Gen each func's sig and body execpt terminals
    foreach func in pkg.functions {
        genFunctionBody(func, builder, mod);
    }

    // Fill each func's each bb's last instruction (aka terminals)
    foreach func in pkg.functions {
        genFunctionEnd(func, builder);
    }

    optimize(mod);

    if(dumpLLVMIR) {
        llvm:LLVMDumpModule(mod);
    }
    //var out = llvm:LLVMWriteBitcodeToFile(mod, targetObjectFilePath);

    // Initialize all the targets for emitting object code
    llvm:LLVMInitializeAllTargetInfos();
    llvm:LLVMInitializeAllTargetMCs();
    llvm:LLVMInitializeAllTargets();
    llvm:LLVMInitializeAllAsmParsers();
    llvm:LLVMInitializeAllAsmPrinters();

    llvm:BytePointer targetTripleBP = llvm:LLVMGetDefaultTargetTriple();
    llvm:LLVMTargetRef targetRef = llvm:LLVMGetFirstTarget();
    llvm:BytePointer cpu;
    llvm:BytePointer features;
    var targetMachine = llvm:LLVMCreateTargetMachine(targetRef, targetTripleBP, cpu, features, 0, 0, 0);

    //string nullTermObjectFilePath = targetObjectFilePath;
    byte[] filenameBytes = targetObjectFilePath.toByteArray("UTF-8");
    // Creates a null terminated string
    filenameBytes[lengthof filenameBytes] = 0;

    byte[] errorMsg;
    int i = llvm:LLVMTargetMachineEmitToFile(targetMachine, mod, filenameBytes, 1, errorMsg);
    // TODO error reporting

    llvm:LLVMDisposeBuilder(builder);
    llvm:LLVMDisposeTargetMachine(targetMachine);
}


function optimize(llvm:LLVMModuleRef mod) {
    var passBuilder = llvm:LLVMPassManagerBuilderCreate();
    llvm:LLVMPassManagerBuilderSetOptLevel(passBuilder, 3);

    var pass = llvm:LLVMCreatePassManager();
    llvm:LLVMPassManagerBuilderPopulateFunctionPassManager(passBuilder, pass);
    llvm:LLVMPassManagerBuilderPopulateModulePassManager(passBuilder, pass);
    // comment above two lines and uncomment below when debuing
    //llvm:LLVMAddPromoteMemoryToRegisterPass(pass);

    var optResult = llvm:LLVMRunPassManager(pass, mod);

    // TODO error reporting
    llvm:LLVMPassManagerBuilderDispose(passBuilder);
    llvm:LLVMDisposePassManager(pass);
}

function genFunctionBody(BIRFunction func, llvm:LLVMBuilderRef builder, llvm:LLVMModuleRef mod) {
    var name = func.name.value;
    llvm:LLVMTypeRef[] argTypes;
    if (func.argsCount == 0){
        argTypes = [llvm:LLVMVoidType()];
    } else {
        int i = 0;
        while (i < func.argsCount) {
            argTypes[i] = llvm:LLVMInt64Type();
            i++;
        }
    }
    var retType = func.typeValue.retType;
    var retTypeRef = genBType(retType);
    var functionTy = llvm:LLVMFunctionType1(retTypeRef, argTypes, func.argsCount, 0);
    var funcRef = llvm:LLVMAddFunction(mod, name, functionTy);
    llvm:LLVMSetFunctionCallConv(funcRef, 0);
    functionRefs[name] = funcRef;

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
            ArgVarKind => {
                var parmRef = llvm:LLVMGetParam(funcRef, argI);
                var loaded = llvm:LLVMBuildStore(builder, parmRef, llvmValueRef);
                argI++;
            }
            any => {}
        }
    }

    foreach bb in func.basicBlocks {
        var bbRef = genBasicBlockBody(func, bb, funcRef, builder);
        bbs[func.name.value + "." + bb.id.value] = bbRef;
    }

    llvm:LLVMPositionBuilderAtEnd(builder, varAllocBB);
    var brInsRef = llvm:LLVMBuildBr(builder, getBBById(func, "bb0"));
}

function genBType(BType bType) returns llvm:LLVMTypeRef {
    match bType {
        BTypeInt => return llvm:LLVMInt64Type();
        BTypeBoolean => return llvm:LLVMInt1Type();
        BTypeNil => return llvm:LLVMVoidType();
    }
}

function genFunctionEnd(BIRFunction func, llvm:LLVMBuilderRef builder) {
    foreach bb in func.basicBlocks {
        genBasicBlockTerminator(func, bb, builder);
    }
}

function stringMul(string str, int factor) returns string {
    int i;
    string result;
    while i < factor {
        result = result + str;
        i++;
    }
    return result;
}

function genBasicBlockTerminator(BIRFunction func, BIRBasicBlock bb, llvm:LLVMBuilderRef builder) {
    var bbRef = getBBById(func, bb.id.value);
    llvm:LLVMPositionBuilderAtEnd(builder, bbRef);

    match bb.terminator {
        GOTO gotoIns => {
            var brInsRef = llvm:LLVMBuildBr(builder, getBBById(func, gotoIns.targetBB.id.value));
        }
        Branch brIns => {
            var ifTrue = getBBById(func, brIns.trueBB.id.value);
            var ifFalse = getBBById(func, brIns.falseBB.id.value);
            var vrInsRef = llvm:LLVMBuildCondBr(builder, loadOprand(func, brIns.op, builder), ifTrue, ifFalse);
        }
        Call callIns => {
            var thenBB = getBBById(func, callIns.thenBB.id.value);
            llvm:LLVMValueRef[] args = [];
            var argsCount = lengthof callIns.args;
            int i = 0;
            while (i < argsCount) {
                args[i] = loadOprand(func, callIns.args[i], builder);
                i++;
            }
            // TODO: check pkg, evntulay remove
            if (callIns.name.value == "print"){
                genCallToPrintf(builder, args, false);
            } else if (callIns.name.value == "println"){
                genCallToPrintf(builder, args, true);
            } else {
                llvm:LLVMValueRef funcRef = getFuncByName(callIns.name);
                llvm:LLVMValueRef callReturn = llvm:LLVMBuildCall(builder, funcRef, args, argsCount, "");
                match callIns.lhsOp {
                    BIRVarRef lhsOp => {
                        llvm:LLVMValueRef lhsRef = getLocalVarById(func, lhsOp.variableDcl.name.value);
                        var loaded = llvm:LLVMBuildStore(builder, callReturn, lhsRef);
                    }
                    () => {}
                }
            }
            var brInsRef = llvm:LLVMBuildBr(builder, thenBB);

        }
        Return => {
            if (func.typeValue.retType != "()"){ //TODO: use BTypeNil
                io:println(func.typeValue);
                var retValueRef = llvm:LLVMBuildLoad(builder, getLocalVarById(func, "%0"), "retrun_temp");
                var ret = llvm:LLVMBuildRet(builder, retValueRef);
            } else {
                var ret = llvm:LLVMBuildRetVoid(builder);
            }
        }

    }
}

function genCallToPrintf(llvm:LLVMBuilderRef builder, llvm:LLVMValueRef[] args, boolean hasNewLine) {
    var argsCount = lengthof args;
    var newLine = hasNewLine ? "\n" : "";
    var printLnIntPatten = llvm:LLVMBuildGlobalStringPtr(builder, stringMul("%ld", argsCount) + newLine, "");
    llvm:LLVMValueRef[] printArgs = [printLnIntPatten];
    appendAllTo(printArgs, args);
    llvm:LLVMValueRef callReturn = llvm:LLVMBuildCall(builder, printfRef, printArgs, argsCount + 1, "");
}

function appendAllTo(any[] a, any[] b) {
    int i = lengthof a;
    foreach bI in b{
        a[i] = bI;
        i++;
    }
}

function genBasicBlockBody(BIRFunction func, BIRBasicBlock bb, llvm:LLVMValueRef funcRef, llvm:LLVMBuilderRef builder)
             returns llvm:LLVMBasicBlockRef {

    var bbRef = llvm:LLVMAppendBasicBlock(funcRef, bb.id.value);
    llvm:LLVMPositionBuilderAtEnd(builder, bbRef);
    foreach i in bb.instructions {
        match i {
            Move moveIns => {
                llvm:LLVMValueRef lhsRef = getLocalVarById(func, moveIns.lhsOp.variableDcl.name.value);
                var rhsVarOp = moveIns.rhsOp;
                llvm:LLVMValueRef rhsVarOpRef = loadOprand(func, rhsVarOp, builder);
                var loaded = llvm:LLVMBuildStore(builder, rhsVarOpRef, lhsRef);
            }
            BinaryOp binaryIns => {
                var lhsTmpName = localVarName(binaryIns.lhsOp.variableDcl) + "_temp";
                var lhsRef = getLocalVarById(func, binaryIns.lhsOp.variableDcl.name.value);
                var rhsOp1 = loadOprand(func, binaryIns.rhsOp1, builder);
                var rhsOp2 = loadOprand(func, binaryIns.rhsOp2, builder);
                var kind = binaryIns.kind;
                if (kind == "LESS_THAN"){
                    // TODO: import these consts from llvm pkg
                    // LLVMIntSLT = 40
                    var ifReturn = llvm:LLVMBuildICmp(builder, 40, rhsOp1, rhsOp2, lhsTmpName);
                    var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                } else if (kind == "LESS_EQUAL"){
                    // LLVMIntSLE = 41
                    var ifReturn = llvm:LLVMBuildICmp(builder, 41, rhsOp1, rhsOp2, lhsTmpName);
                    var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                } else if (kind == "GREATER_THAN"){
                    var ifReturn = llvm:LLVMBuildICmp(builder, 38, rhsOp1, rhsOp2, lhsTmpName);
                    var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                } else if (kind == "GREATER_EQUAL"){
                    var ifReturn = llvm:LLVMBuildICmp(builder, 39, rhsOp1, rhsOp2, lhsTmpName);
                    var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                } else if (kind == "ADD"){
                    var addReturn = llvm:LLVMBuildAdd(builder, rhsOp1, rhsOp2, lhsTmpName);
                    var loaded = llvm:LLVMBuildStore(builder, addReturn, lhsRef);
                } else if (kind == "SUB"){
                    var ifReturn = llvm:LLVMBuildSub(builder, rhsOp1, rhsOp2, lhsTmpName);
                    var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                } else if (kind == "MUL"){
                    var ifReturn = llvm:LLVMBuildMul(builder, rhsOp1, rhsOp2, lhsTmpName);
                    var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                } else if (kind == "DIV"){
                    var ifReturn = llvm:LLVMBuildSDiv(builder, rhsOp1, rhsOp2, lhsTmpName);
                    var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                } else if (kind == "EQUAL"){
                    var ifReturn = llvm:LLVMBuildICmp(builder, 32, rhsOp1, rhsOp2, lhsTmpName);
                    var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                } else if (kind == "NOT_EQUAL"){
                    // LLVMIntNE = 33
                    var ifReturn = llvm:LLVMBuildICmp(builder, 33, rhsOp1, rhsOp2, lhsTmpName);
                    var loaded = llvm:LLVMBuildStore(builder, ifReturn, lhsRef);
                } else {
                    error err = { message: "unknown binary op kind" };
                    throw err;
                }
            }
            ConstantLoad constOp => {
                llvm:LLVMValueRef lhsRef = getLocalVarById(func, constOp.lhsOp.variableDcl.name.value);
                var constRef = llvm:LLVMConstInt(llvm:LLVMInt64Type(), constOp.value, 0);
                var loaded = llvm:LLVMBuildStore(builder, constRef, lhsRef);
            }

        }
    }
    return bbRef;
}

function getBBById(BIRFunction func, string id) returns llvm:LLVMBasicBlockRef {
    match bbs[func.name.value + "." + id] {
        llvm:LLVMBasicBlockRef bb => {
            return bb;
        }
        any => {
            error err = { message: "bb '" + id + "' dosn't exist" };
            throw err;
        }
    }
}


function loadOprand(BIRFunction func, BIROperand oprand, llvm:LLVMBuilderRef builder) returns llvm:LLVMValueRef {
    match oprand {
        BIRVarRef refOprand => {
            string tempName = localVarName(refOprand.variableDcl) + "_temp";
            return llvm:LLVMBuildLoad(builder, getLocalVarById(func, refOprand.variableDcl.name.value), tempName);
        }
    }
}

function getLocalVarById(BIRFunction fun, string id) returns llvm:LLVMValueRef {
    match localVarRefs[fun.name.value + "." + id] {
        llvm:LLVMValueRef varRef => return varRef;
        any => {
            error err = { message: "Local var by name '" + id + "' dosn't exist in " + fun.name.value };
            throw err;
        }
    }
}

function getFuncByName(Name name) returns llvm:LLVMValueRef {
    match functionRefs[name.value] {
        llvm:LLVMValueRef func => return func;
        any => {
            error err = { message: "functtion '" + name.value + "' dosn't exist" };
            throw err;
        }
    }
}

function localVarName(BIRVariableDcl localVar) returns string {
    return localVarNameFromId(localVar.name.value);
}

function localVarNameFromId(string localVarStr) returns string {
    return "local" + localVarStr.substring(1, localVarStr.length());
}
