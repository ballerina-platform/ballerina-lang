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

type NativeFunctionBuilder object {
    map<llvm:LLVMValueRef> functionMap = {};
    llvm:LLVMModuleRef module = {};
    llvm:LLVMTypeRef int64PointerType = {};

    function setModule(llvm:LLVMModuleRef module) {
        self.module = module;
        self.int64PointerType = llvm:llvmPointerType(llvm:llvmInt64Type(), 0);
    }

    function getFunctionValueRef(string key) returns llvm:LLVMValueRef {
        llvm:LLVMValueRef? functionValue = self.functionMap[key];
        if (functionValue is ()) {
            panic error("NativeFunctionNotFound", message = "Native Function was not found in the map : " + key);
        }
        return <llvm:LLVMValueRef>functionValue;
    }

    function genFunctions() {
        self.functionMap["print"] = self.genPrintfDeclration();
        self.functionMap["new_int_array"] =  self.genNewIntArrayDeclaration();
        self.functionMap["int_array_store"] =  self.genIntArrayStoreDeclaration();
        self.functionMap["int_array_load"] = self.genIntArrayLoadDeclaration();
    }

    function genIntArrayLoadDeclaration() returns llvm:LLVMValueRef {
        llvm:LLVMTypeRef[] arguments = [self.int64PointerType, llvm:llvmInt64Type()];
        llvm:LLVMTypeRef newIntArrayLoadFunctionType = llvm:llvmFunctionType1(self.int64PointerType, arguments, 2, 0);
        return llvm:llvmAddFunction(self.module, "int_array_load", newIntArrayLoadFunctionType);
    }

    function genPrintfDeclration() returns llvm:LLVMValueRef {
        llvm:LLVMTypeRef[] pointer_to_char_type = [llvm:llvmPointerType(llvm:llvmInt8Type(), 0)];
        llvm:LLVMTypeRef printfType = llvm:llvmFunctionType1(llvm:llvmInt32Type(), pointer_to_char_type, 1, 1);
        return llvm:llvmAddFunction(self.module, "printf", printfType);
    }

    function genNewIntArrayDeclaration() returns llvm:LLVMValueRef {
        llvm:LLVMTypeRef[] arguments = [llvm:llvmInt64Type()];
        llvm:LLVMTypeRef newIntArrayFunctionType = llvm:llvmFunctionType1(self.int64PointerType, arguments, 1, 0);
        return llvm:llvmAddFunction(self.module, "new_int_array", newIntArrayFunctionType);
    }

    function genIntArrayStoreDeclaration() returns llvm:LLVMValueRef {
        llvm:LLVMTypeRef[] arguments = [self.int64PointerType, llvm:llvmInt64Type(), self.int64PointerType];
        llvm:LLVMTypeRef newIntArrayStoreFunctionType = llvm:llvmFunctionType1(llvm:llvmVoidType(), arguments, 3, 0);
        return llvm:llvmAddFunction(self.module, "int_array_store", newIntArrayStoreFunctionType);
    }
};
