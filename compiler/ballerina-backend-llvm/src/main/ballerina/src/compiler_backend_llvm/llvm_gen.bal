// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/llvm;
import ballerina/bir;
import ballerina/io;

// TODO: make non-global
llvm:LLVMValueRef? printfRef = ();
map<llvm:LLVMTypeRef> structMap = {"null" : llvm:llvmVoidType()};
map<int> precedenceMap = { "null":0, "boolean":1, "int":2};

function genPackage(bir:Package pkg, string targetObjectFilePath, boolean dumpLLVMIR, boolean noOptimize) {
    var mod = createModule(pkg.org, pkg.name, pkg.versionValue);
    genFunctions(mod, pkg.functions);
    if (!noOptimize) { // Don't optimize for debugging usage.
        optimize(mod);
    }
    if(dumpLLVMIR) {
        llvm:llvmDumpModule(mod);
    }
    createObjectFile(targetObjectFilePath, mod);
}

function createModule(bir:Name orgName, bir:Name pkgName, bir:Name ver) returns llvm:LLVMModuleRef {
    var moduleName = orgName.value + pkgName.value + ver.value;
    return llvm:llvmModuleCreateWithName(moduleName);
}

function genFunctions(llvm:LLVMModuleRef mod, bir:Function?[] funcs) {
    var builder = llvm:llvmCreateBuilder();

    genPrintfDeclration(mod);

    map<FuncGenrator> funcGenrators = mapFuncsToNameAndGenrator(mod, builder, funcs);

    foreach var g in funcGenrators  {
        g.genFunctionDecl();
    }
    foreach var g in funcGenrators  {
        g.genFunctionBody(funcGenrators);
    }
    llvm:llvmDisposeBuilder(builder);
}

function createObjectFile(string targetObjectFilePath, llvm:LLVMModuleRef mod) {
    llvm:LLVMTargetMachineRef targetMachine;
    targetMachine = createTargetMachine();
    var filenameBytes = createNullTermiatedString(targetObjectFilePath);
    byte[] errorMsg = [];
    int|error i = llvm:llvmTargetMachineEmitToFile(targetMachine, mod, filenameBytes, 1, errorMsg);
    if (i is int) {
        llvm:llvmDisposeTargetMachine(targetMachine);
    } else {
        error err = error("NollvmTargetMachineEmitToFileErr", message = "cannot emit to target machine");
        panic err;
    }
}

function createNullTermiatedString(string str) returns byte[] {
    byte[] filenameBytes = str.toBytes();
    filenameBytes[filenameBytes.length()] = 0;
    return filenameBytes;
}

function createTargetMachine() returns llvm:LLVMTargetMachineRef {
    initAllTargets();

    llvm:BytePointer targetTripleBP = llvm:llvmGetDefaultTargetTriple();
    llvm:LLVMTargetRef targetRef = llvm:llvmGetFirstTarget();
    llvm:BytePointer cpu = {};
    llvm:BytePointer features = {};

    return llvm:llvmCreateTargetMachine(targetRef, targetTripleBP, cpu, features, 0, 0, 0);
}

function initAllTargets() {
    llvm:llvmInitializeAllTargetInfos();
    llvm:llvmInitializeAllTargetMCs();
    llvm:llvmInitializeAllTargets();
    llvm:llvmInitializeAllAsmParsers();
    llvm:llvmInitializeAllAsmPrinters();
}

function readFileFully(string path) returns byte[] = external;

function genPrintfDeclration(llvm:LLVMModuleRef mod) {
    llvm:LLVMTypeRef[] pointer_to_char_type = [llvm:llvmPointerType(llvm:llvmInt8Type(), 0)];
    llvm:LLVMTypeRef printfType = llvm:llvmFunctionType1(llvm:llvmInt32Type(), pointer_to_char_type, 1, 1);
    printfRef = llvm:llvmAddFunction(mod, "printf", printfType);
}

function mapFuncsToNameAndGenrator(llvm:LLVMModuleRef mod, llvm:LLVMBuilderRef builder, bir:Function?[] funcs)
             returns map<FuncGenrator> {
    map<FuncGenrator> genrators = {};
    foreach var func in funcs {
        if (!(func is ())) {
            FuncGenrator funcGen = new(func, mod, builder);
            genrators[func.name.value] = funcGen;
        }
    }
    return genrators;
}

function optimize(llvm:LLVMModuleRef mod) {
    var passBuilder = llvm:llvmPassManagerBuilderCreate();
    llvm:llvmPassManagerBuilderSetOptLevel(passBuilder, 3);

    var pass = llvm:llvmCreatePassManager();
    llvm:llvmPassManagerBuilderPopulateFunctionPassManager(passBuilder, pass);
    llvm:llvmPassManagerBuilderPopulateModulePassManager(passBuilder, pass);
    // comment above two lines and uncomment below when debuing
    //llvm:LLVMAddPromoteMemoryToRegisterPass(pass);

    var optResult = llvm:llvmRunPassManager(pass, mod);

    // TODO error reporting
    llvm:llvmPassManagerBuilderDispose(passBuilder);
    llvm:llvmDisposePassManager(pass);
}

// TODO : Should revamp this method again
function genBType(bir:BType? bType) returns llvm:LLVMTypeRef {
    if (bType is bir:BTypeInt) {
        return llvm:llvmInt64Type();
    } else if (bType is bir:BTypeBoolean) {
        return llvm:llvmInt1Type();
    } else if (bType is bir:BTypeNil) {
        return llvm:llvmVoidType();
    } else if (bType is ()) {
        return llvm:llvmVoidType();
    } else if (bType is bir:BArrayType) { 
        return llvm:llvmVoidType();
    } else if (bType is bir:BUnionType) {
        return createUnion(<bir:BUnionType>bType);
    }
    typedesc<any> T = typeof bType;
    error err = error( "Undefined type :" + T.toString());
    panic err;
}

function localVarName(bir:VariableDcl? localVar) returns string {
    string? temp = localVar["name"]?.value;
    if (temp is string) {
        return localVarNameFromId(temp);
    }
    error err = error("Local Variable name is not a string");
    panic err;
}

function localVarNameFromId(string localVarStr) returns string {
    return "local" + localVarStr.substring(1, localVarStr.length());
}

function appendAllTo(any[] toArr, any[] fromArr) {
    int i = toArr.length();
    foreach var bI in fromArr {
        toArr[i] = bI;
        i = i + 1;
    }
}

function createUnion(bir:BUnionType bType) returns llvm:LLVMTypeRef {
    io:println("Creating Struct type for Union");
    string largestType = "null";
    foreach var member in bType.members {
        if (member is bir:BTypeInt) {
            largestType = checkForLargerType(largestType, "int");
            createTaggedInt();
        } else if (member is bir:BTypeBoolean) {
            largestType = checkForLargerType(largestType, "boolean");
            createTaggedBool();
        } else {
            io:println("any");
        }
    }
    io:println(largestType);
    return <llvm:LLVMTypeRef>structMap[largestType];
}

function checkForLargerType(string first, string second) returns string { 
    if (<int>precedenceMap[first] > <int>precedenceMap[second]) {
        return first;
    } else {
        return second;
    }
}

function createTaggedInt() {
    io:println("BeforeMapCheckInt");
    if (structMap.hasKey("int")) {
        return;
    }
    io:println("AfterMapCheckInt");
    llvm:LLVMTypeRef structType = createNamedStruct("tagged_int");
    llvm:LLVMTypeRef[] argTypes = [llvm:llvmInt64Type(), llvm:llvmInt64Type()];
    llvm:llvmStructSetBody1(structType, argTypes, 2, 0);
    structMap["int"] = structType;
    if (structMap.hasKey("int")) {
        io:println("HasInt");
    }
}

function createTaggedBool() {
    io:println("BeforeMapCheckBool");
    if (structMap.hasKey("boolean")) {
        return;
    }
    io:println("AfterMapCheckBool");
    llvm:LLVMTypeRef structType = createNamedStruct("tagged_bool");
    var argTypes = [llvm:llvmInt64Type(), llvm:llvmInt1Type()];
    llvm:llvmStructSetBody1(structType, argTypes, 2, 0);
    structMap["boolean"] = structType;
    if (structMap.hasKey("boolean")) {
        io:println("HasBoolean");
    }
}

function createNamedStruct(string name) returns llvm:LLVMTypeRef {
    var structType = llvm:llvmStructCreateNamed(llvm:llvmGetGlobalContext(), name);
    return structType;
}

function checkIfTypesAreCompatible(bir:BType castType, llvm:LLVMTypeRef lhsType) returns boolean {
    LLVMTypeRef castTypeRef = genBType(castType);
    return llvm:checkIfTypesMatch(castTypeRef, lhsType);   
}
