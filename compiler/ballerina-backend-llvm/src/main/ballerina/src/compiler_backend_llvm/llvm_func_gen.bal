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
import ballerina/io;
import ballerina/bir;

type FuncGenrator object {
    bir:Function func;
    llvm:LLVMValueRef? funcRef = ();
    llvm:LLVMModuleRef mod;
    map<llvm:LLVMValueRef>? localVarRefs = ();
    llvm:LLVMValueRef? varAllocBB = ();
    llvm:LLVMBuilderRef builder;

    function __init(bir:Function func, llvm:LLVMModuleRef mod, llvm:LLVMBuilderRef builder) {
        self.func = func;
        //self.funcRef = funcRef;
        self.mod = mod;
        //self.localVarRefs = localVarRefs;
        //self.varAllocBB = varAllocBB;
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
        while (i < self.func.argsCount) {
            argTypes[i] = llvm:llvmInt64Type();
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
        foreach var localVar in self.func.localVars{
            var varName = localVarName(localVar);
            var varType = genBType(localVar.typeValue);
            llvm:LLVMValueRef localVarRef = llvm:llvmBuildAlloca(self.builder, varType, varName);
            self.localVarRefs[localVar.name.value] = localVarRef;

            if (self.isParamter(localVar)){
                var parmRef = llvm:llvmGetParam(self.funcRef, paramIndex);
                var loaded = llvm:llvmBuildStore(self.builder, parmRef, localVarRef);
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
        foreach var bb in self.func.basicBlocks {
            BbBodyGenrator g = new(self.builder, self, bb);
            bbTermGenrators[bb.id.value] = g.genBasicBlockBody();
        }
        return bbTermGenrators;
    }

    function genLocalVarAllocationBBTerminator(map<BbTermGenrator> bbTermGenrators) {
        llvm:llvmPositionBuilderAtEnd(self.builder, self.varAllocBB);
        var brInsRef = llvm:llvmBuildBr(self.builder, findBbRefById(bbTermGenrators, "bb0"));
    }

    function genBbTerminators(map<FuncGenrator> funcGenrators, map<BbTermGenrator> bbTermGenrators) {
        foreach var g in bbTermGenrators {
            g.genBasicBlockTerminator(funcGenrators, bbTermGenrators);
        }
    }

    function genLoadLocalToTempVar(bir:Operand oprand) returns llvm:LLVMValueRef {
        bir:VarRef refOprand = oprand;
        string tempName = localVarName(refOprand.variableDcl) + "_temp";
        var localVarRef = self.getLocalVarRefById(refOprand.variableDcl.name.value);
        return llvm:llvmBuildLoad(self.builder, localVarRef, tempName);
    }

    function getLocalVarRefById(string id) returns llvm:LLVMValueRef|error {
        if (self.localVarRefs[id] is llvm:LLVMValueRef) {
            return self.localVarRefs[id];
        } else {
            error err = { message: "Local var by name '" + id + "' dosn't exist in " + self.func.name.value };
            return err;
        }
    }

    function genBbDecl(string name) returns llvm:LLVMValueRef {
        var bbRef = llvm:llvmAppendBasicBlock(self.funcRef, name);
        llvm:llvmPositionBuilderAtEnd(self.builder, bbRef);
        return bbRef;
    }

    function isVoidFunc() returns boolean {
        return self.func.typeValue.retType != "()";
    }
};



