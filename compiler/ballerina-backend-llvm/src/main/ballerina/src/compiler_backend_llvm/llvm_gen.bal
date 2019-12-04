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

// TODO: make non-global
llvm:LLVMValueRef? printfRef = ();
map<llvm:LLVMTypeRef> structMap = {"null" : llvm:llvmVoidType()};
map<llvm:LLVMTypeRef> taggedTypeToTypePointerMap = {};
map<int> precedenceMap = { "null":0, "boolean":1, "int":2};
const int TAGGED_UNION_FLAG_INDEX = 0;
const int TAGGED_UNION_VALUE_INDEX = 1;

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
            //Skipping init, start, stop function temporarily. TODO: Remove this check
            if (!checkForValidFunction(func)) {
                continue;
            }
            FuncGenrator funcGen = new(func, mod, builder);
            genrators[func.name.value] = funcGen;
        }
    }
    return genrators;
}

function checkForValidFunction(bir:Function func) returns boolean {
    if (func.name.value =="init") {
        return false;
    } else if (func.name.value == "start") {
        return false;
    } else if (func.name.value == "stop") {
        return false;
    } else {
        return true;
    }
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

function localVariableNameWithPrefix(bir:VarRef|bir:VariableDcl|() localVar) returns string {
    string localVarName = localVariableName(localVar);
    string postFixName = addPrefixToLocalVarName(localVarName);
    return postFixName;
}

function localVariableName(bir:VarRef|bir:VariableDcl|() localVar) returns string {
    string? temp;
    if(localVar is bir:VarRef) {
        temp = localVar.variableDcl["name"]?.value;
    } else if (localVar is bir:VariableDcl) {
        temp = localVar["name"]?.value;
    } else {
        temp = ();
    }
    if (temp is string) {
        return temp.substring(1, temp.length());
    }
    error err = error("Local Variable name is not a string");
    panic err;
}

function addPrefixToLocalVarName(string localVarStr) returns string {
    return "local" + localVarStr;
}

function appendAllTo(any[] toArr, any[] fromArr) {
    int i = toArr.length();
    foreach var bI in fromArr {
        toArr[i] = bI;
        i = i + 1;
    }
}

function getTaggedStructType(string typeName) returns llvm:LLVMTypeRef {
    boolean hasType = structMap.hasKey(typeName);
    if (!hasType) {
        if (typeName == "int") {
            createTaggedInt();
        } else if (typeName == "boolean") {
            createTaggedBool();
        }    
    }
    return <llvm:LLVMTypeRef>structMap[typeName];
}

function createUnion(bir:BUnionType bType) returns llvm:LLVMTypeRef {
    string largestType = "null";
    foreach var member in bType.members {
        if (member is bir:BTypeInt) {
            largestType = getLargerType(largestType, "int");
        } else if (member is bir:BTypeBoolean) {
            largestType = getLargerType(largestType, "boolean");
        }
    }
    return getTaggedStructType(largestType);
}

function getLargerType(string first, string second) returns string { 
    if (<int>precedenceMap[first] > <int>precedenceMap[second]) {
        return first;
    } else {
        return second;
    }
}

function createTaggedInt() {
    if (structMap.hasKey("int")) {
        return;
    }
    llvm:LLVMTypeRef structType = createNamedStruct("tagged_int");
    llvm:LLVMTypeRef[] argTypes = [llvm:llvmInt64Type(), llvm:llvmInt64Type()];
    llvm:llvmStructSetBody1(structType, argTypes, 2, 0);
    structMap["int"] = structType;
}

function createTaggedBool() {
    if (structMap.hasKey("boolean")) {
        return;
    }
    llvm:LLVMTypeRef structType = createNamedStruct("tagged_bool");
    var argTypes = [llvm:llvmInt64Type(), llvm:llvmInt1Type()];
    llvm:llvmStructSetBody1(structType, argTypes, 2, 0);
    structMap["boolean"] = structType;
}

function createNamedStruct(string name) returns llvm:LLVMTypeRef {
    var structType = llvm:llvmStructCreateNamed(llvm:llvmGetGlobalContext(), name);
    return structType;
}

function isUnionType(bir:BType typeValue) returns boolean {
    return typeValue is bir:BUnionType;
}

function getTag(bir:BType typeValue) returns int {
    if (typeValue is bir:BUnionType) {
        return getTagForUnion(typeValue);
    } else {
        return getTagForSingleType(typeValue);
    }
}

function getTagForUnion(bir:BUnionType unionType) returns int {
    int largestTag = 0;
    foreach var member in unionType.members {
        if (member is bir:BType) {
            int currentTagVal = getTagForSingleType(member);
            if (currentTagVal > largestTag) {
                largestTag = currentTagVal;
            }
        }
    }
    return largestTag;
}

function getTagForSingleType(bir:BType typeValue) returns int {
    return getPrecedenceValueFromTypeString(getTypeStringName(typeValue));
}

function getPrecedenceValueFromTypeString(string typeString) returns int {
    if (!precedenceMap.hasKey(typeString)) {
        return 0;
    }
    return <int>precedenceMap[typeString];
}

function getValueRefFromInt(int value, int sign) returns llvm:LLVMValueRef {
    return llvm:llvmConstInt(llvm:llvmInt64Type(), value, sign);
}

function getTaggedStructTypeFromSingleType(bir:BType typeValue) returns llvm:LLVMTypeRef {
    return getTaggedStructType(getTypeStringName(typeValue));
}

function getTypeStringName(bir:BType typeValue) returns string {
    if (typeValue is bir:BTypeInt) {
        return "int";
    }
    if (typeValue is bir:BTypeBoolean) {
        return "boolean";
    }
    return "null";
}

function createTypePointerForTaggedStructType(string taggedType, llvm:LLVMBuilderRef builder) {
    llvm:LLVMTypeRef taggedUnionType = getTaggedStructType(taggedType);
    llvm:LLVMValueRef tempStructAllocation = llvm:llvmBuildAlloca(builder, taggedUnionType, ("tempStructOf"+taggedType));
    llvm:LLVMTypeRef typePointerToTaggedUnion = llvm:llvmTypeOf(tempStructAllocation);
    taggedTypeToTypePointerMap[taggedType] = typePointerToTaggedUnion;
}

function getTypePointerForTaggedStructType(bir:BType typeValue, llvm:LLVMBuilderRef builder) returns llvm:LLVMTypeRef {
    string typeName = getTypeStringName(typeValue);
    if (!taggedTypeToTypePointerMap.hasKey(typeName)) {
        createTypePointerForTaggedStructType(typeName, builder);
    }
    return <llvm:LLVMTypeRef>taggedTypeToTypePointerMap[typeName];
}
