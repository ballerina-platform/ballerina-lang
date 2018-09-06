import ballerina/llvm;
import ballerina/io;

// TODO: make non-globle
llvm:LLVMValueRef printfRef;

function genPackage(BIRPackage pkg, string targetObjectFilePath, boolean dumpLLVMIR) {
    var mod = createModule(pkg.org, pkg.name, pkg.versionValue);
    genFunctions(mod, pkg.functions);
    optimize(mod);
    createObjectFile(targetObjectFilePath, mod);
    if(dumpLLVMIR) {
        llvm:LLVMDumpModule(mod);
    }
}

function createModule(Name orgName, Name pkgName, Name ver) returns llvm:LLVMModuleRef {
    var moduleName = orgName.value + pkgName.value + ver.value;
    return llvm:LLVMModuleCreateWithName(moduleName);
}

function genFunctions(llvm:LLVMModuleRef mod, BIRFunction[] funcs) {
    var builder = llvm:LLVMCreateBuilder();

    genPrintfDeclration(mod);

    map<FuncGenrator> funcGenrators = mapFuncsToNameAndGenrator(mod, builder, funcs);
    foreach g in funcGenrators  {
        g.genFunctionDecl();
    }
    foreach g in funcGenrators  {
        g.genFunctionBody(funcGenrators);
    }
    llvm:LLVMDisposeBuilder(builder);
}

function createObjectFile(string targetObjectFilePath, llvm:LLVMModuleRef mod) {
    llvm:LLVMTargetMachineRef targetMachine;
    try {
        targetMachine = createTargetMachine();
        var filenameBytes = createNullTermiatedString(targetObjectFilePath);
        byte[] errorMsg;
        int i = llvm:LLVMTargetMachineEmitToFile(targetMachine, mod, filenameBytes, 1, errorMsg);
        // TODO error reporting
    } finally {
        llvm:LLVMDisposeTargetMachine(targetMachine);
    }
}

function createNullTermiatedString(string str) returns byte[] {
    byte[] filenameBytes = str.toByteArray("UTF-8");
    filenameBytes[lengthof filenameBytes] = 0;
    return filenameBytes;
}


function createTargetMachine() returns llvm:LLVMTargetMachineRef {
    initAllTargets();

    llvm:BytePointer targetTripleBP = llvm:LLVMGetDefaultTargetTriple();
    llvm:LLVMTargetRef targetRef = llvm:LLVMGetFirstTarget();
    llvm:BytePointer cpu;
    llvm:BytePointer features;

    return llvm:LLVMCreateTargetMachine(targetRef, targetTripleBP, cpu, features, 0, 0, 0);
}

function initAllTargets() {
    llvm:LLVMInitializeAllTargetInfos();
    llvm:LLVMInitializeAllTargetMCs();
    llvm:LLVMInitializeAllTargets();
    llvm:LLVMInitializeAllAsmParsers();
    llvm:LLVMInitializeAllAsmPrinters();
}

function mapFuncsToNameAndGenrator(llvm:LLVMModuleRef mod, llvm:LLVMBuilderRef builder, BIRFunction[] funcs)
             returns map<FuncGenrator> {
    map<FuncGenrator> genrators;
    foreach func in funcs {
        FuncGenrator funcGen = new(mod, builder, func);
        genrators[func.name.value] = funcGen;
    }
    return genrators;
}

function genPrintfDeclration(llvm:LLVMModuleRef mod) {
    llvm:LLVMTypeRef[] pointer_to_char_type = [llvm:LLVMPointerType(llvm:LLVMInt8Type(), 0)];
    llvm:LLVMTypeRef printfType = llvm:LLVMFunctionType1(llvm:LLVMInt32Type(), pointer_to_char_type, 1, 1);
    printfRef = llvm:LLVMAddFunction(mod, "printf", printfType);

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


function genBType(BType bType) returns llvm:LLVMTypeRef {
    match bType {
        BTypeInt => return llvm:LLVMInt64Type();
        BTypeBoolean => return llvm:LLVMInt1Type();
        BTypeNil => return llvm:LLVMVoidType();
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

function localVarName(BIRVariableDcl localVar) returns string {
    return localVarNameFromId(localVar.name.value);
}

function localVarNameFromId(string localVarStr) returns string {
    return "local" + localVarStr.substring(1, localVarStr.length());
}
