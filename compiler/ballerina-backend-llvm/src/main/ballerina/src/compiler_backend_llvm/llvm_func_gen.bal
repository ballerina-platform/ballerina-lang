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

type FuncGenrator object {
    bir:Function func;
    llvm:LLVMValueRef? funcRef = ();
    llvm:LLVMModuleRef mod;
    map<llvm:LLVMValueRef> localVarRefs = {};
    map<llvm:LLVMTypeRef> localVarTypeRefs = {};
    llvm:LLVMValueRef? varAllocBB = ();
    llvm:LLVMBuilderRef builder;

    function __init(bir:Function func, llvm:LLVMModuleRef mod, llvm:LLVMBuilderRef builder) {
        self.func = func;
        self.mod = mod;
        self.builder = builder;
    }

    function genFunctionDecl() {
        var name = self.func.name.value;
        llvm:LLVMTypeRef[] argTypes = self.genFunctionArgTypes(self.func.argsCount);
        var retTypeRef = genBType(self.func.typeValue?.retType);
        var functionType = llvm:llvmFunctionType1(retTypeRef, argTypes, self.func.argsCount, 0);
        self.funcRef = llvm:llvmAddFunction(self.mod, name, functionType);
    }

    function genFunctionArgTypes(int argsCount) returns llvm:LLVMTypeRef[] {
        if (self.func.argsCount == 0) {
            return self.genVoidFunctionArgTypes();
        } else {
            return self.genNonVoidFunctionArgTypes(argsCount);
        }
    }

    function genVoidFunctionArgTypes() returns llvm:LLVMTypeRef[] {
        return [llvm:llvmVoidType()];
    }

    function genNonVoidFunctionArgTypes(int argsCount) returns llvm:LLVMTypeRef[] {
        llvm:LLVMTypeRef[] argTypes = [];
        int i = 0;
        foreach var param in  self.func.params {
            if (param is bir:FunctionParam) {
                argTypes[i] = genBType(param.typeValue);
                i += 1;
            }
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
        foreach var localVar in self.func.localVars {
            var varName = localVariableNameWithPrefix(localVar);
            var varType = genBType(localVar["typeValue"]);
            llvm:LLVMValueRef localVarRef = llvm:llvmBuildAlloca(self.builder, varType, varName);
            self.cacheLocVarValue(localVar, localVarRef);
            self.cacheLocVarType(localVar, varType);
            if (self.isParameter(localVar)) {
                llvm:LLVMValueRef? funcTemp = self.funcRef;
                if (funcTemp is llvm:LLVMValueRef) {
                    var parmRef = llvm:llvmGetParam(funcTemp, paramIndex);
                    var loaded = llvm:llvmBuildStore(self.builder, parmRef, localVarRef);
                    paramIndex += 1;
                }
            }
        }
    }

    function cacheLocVarValue(bir:VariableDcl? localVar, llvm:LLVMValueRef localVarRef) {
        map<llvm:LLVMValueRef> localVarRefsTemp = self.localVarRefs;
        string localVarRefName = localVariableName(localVar);
        localVarRefsTemp[localVarRefName] = localVarRef;
    }

    function cacheLocVarType(bir:VariableDcl? localVar, llvm:LLVMTypeRef localVarTypeRef) {
        map<llvm:LLVMValueRef>? localVarTypeRefsTemp = self.localVarTypeRefs;
        if (localVarTypeRefsTemp is map<llvm:LLVMTypeRef>) {
            string localVarRefName = localVariableName(localVar);
            localVarTypeRefsTemp[localVarRefName] = localVarTypeRef;
        }
    }

    function isParameter(bir:VariableDcl? localVar) returns boolean {
        if (localVar["kind"] is bir:ArgVarKind) {
            return true;
        } else {
            return false;
        }
    }

    function genBbBodies() returns map<BbTermGenrator> {
        map<BbTermGenrator> bbTermGenrators = {};
        foreach var bb in self.func.basicBlocks {
            bir:BasicBlock? bbTemp = bb;
            if (bbTemp is bir:BasicBlock) {
                BbBodyGenrator g = new(self.builder, self, bbTemp);
                bbTermGenrators[<string> bb["id"]?.value] = g.genBasicBlockBody();
            }
        }
        return bbTermGenrators;
    }


    function genLocalVarAllocationBBTerminator(map<BbTermGenrator> bbTermGenrators) {
        if (self.varAllocBB is llvm:LLVMValueRef) {
            llvm:llvmPositionBuilderAtEnd(self.builder, <llvm:LLVMValueRef> self.varAllocBB);
        }
        var brInsRef = llvm:llvmBuildBr(self.builder, findBbRefById(bbTermGenrators, "bb0"));
    }

    function genBbTerminators(map<FuncGenrator> funcGenrators, map<BbTermGenrator> bbTermGenrators) {
        foreach var g in bbTermGenrators {
            g.genBasicBlockTerminator(funcGenrators, bbTermGenrators);
        }
    }

    function genLoadLocalToTempVar(bir:VarRef oprand) returns llvm:LLVMValueRef {
        bir:VarRef refOprand = oprand;
        string tempName = localVariableNameWithPrefix(refOprand) + "_temp";
        var localVarRef = self.getLocalVarRef(refOprand);
        return llvm:llvmBuildLoad(self.builder, localVarRef, tempName);
    }

    function getLocalVarRefById(string id) returns llvm:LLVMValueRef {
        llvm:LLVMValueRef? tempVarRef = self.localVarRefs[id];
        if (tempVarRef is llvm:LLVMValueRef) {
            return tempVarRef;
        }
        else {
            panic error("SimpleErrorType", message = "Local var by name '" + id + "' dosn't exist in " +
                self.func.name.value);
        }
    }

    function getLocalVarRef(bir:VarRef? variable) returns llvm:LLVMValueRef {
        string id = localVariableName(variable);
        return self.getLocalVarRefById(id);
    }

    function storeValueRefForLocalVarRef(llvm:LLVMValueRef valueRef, bir:VarRef variable) {
        string id = localVariableName(variable);
        map<llvm:LLVMValueRef> localVarRefsTemp = self.localVarRefs;
        localVarRefsTemp[id] = valueRef;
    }

    function getLocalVarTypeRefById(string id) returns llvm:LLVMTypeRef {
        llvm:LLVMTypeRef? tempTypeRef = self.localVarTypeRefs[id];
        if (tempTypeRef is llvm:LLVMTypeRef) {
            return tempTypeRef;
        }
        else {
            panic error("SimpleErrorType", message = "Local var by name '" + id + "' dosn't exist in " +
                self.func.name.value);
        }
    }

    function genBbDecl(string name) returns llvm:LLVMValueRef {
        llvm:LLVMValueRef? funcRefTemp = self.funcRef;
        if !(funcRefTemp is ()) {
            var bbRef = llvm:llvmAppendBasicBlock(funcRefTemp, name);
            llvm:llvmPositionBuilderAtEnd(self.builder, bbRef);
            return bbRef;
        }
        panic error( "Found null variable : ");
    }

    function isVoidFunc() returns boolean {
        return !(self.func.typeValue["retType"] is ());
    }

    function loadElementFromStruct(bir:VarRef variable, int elementIndex) returns llvm:LLVMValueRef {
        string elementName = self.getLoadElementFromStructName(variable, elementIndex);
        llvm:LLVMValueRef ptrToElementOfUnion = self.buildStructGepForVariable(variable, elementIndex);
        return llvm:llvmBuildLoad(self.builder, ptrToElementOfUnion, elementName);
    }

    function buildStructGepForVariable(bir:VarRef variable, int elementIndex) returns llvm:LLVMValueRef {
        string elementName = self.getStructGepName(localVariableNameWithPrefix(variable), elementIndex);
        llvm:LLVMValueRef rhsValueRef = self.getLocalVarRef(variable);
        return llvm:llvmBuildStructGEP(self.builder, rhsValueRef, elementIndex, elementName);
    }

    function getLoadElementFromStructName(bir:VarRef variable, int elementIndex) returns string {
        string elementName = self.getStructGepName(localVariableNameWithPrefix(variable), elementIndex);
        return elementName + "_temp";
    }

    function getStructGepName(string variableName, int index) returns string {
        return variableName + "_index" + index.toString();
    }
};
