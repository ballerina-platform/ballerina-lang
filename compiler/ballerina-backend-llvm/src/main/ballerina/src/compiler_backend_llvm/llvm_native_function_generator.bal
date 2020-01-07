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

    function setModule(llvm:LLVMModuleRef module) {
        self.module = module;
    }

    function getFunctionValueRef(string key) returns llvm:LLVMValueRef {
        llvm:LLVMValueRef? functionValue = self.functionMap[key];
        if (functionValue is ()) {
            var newFunctionValue = self.genFunction(key);
            self.functionMap[key] = newFunctionValue;
            return newFunctionValue;
        }
        return <llvm:LLVMValueRef>functionValue;
    }

    function genFunction(string key) returns llvm:LLVMValueRef {
        if (key == "print") {
            return self.genPrintfDeclration();
        } else if (key == "new_int_array") {
            return self.genNewIntArrayDeclaration();
        } else {
            panic error("InvalidNativeFunctionError", message = "Invalid native function name");
        }
    }

    function genPrintfDeclration() returns llvm:LLVMValueRef {
        llvm:LLVMTypeRef[] pointer_to_char_type = [llvm:llvmPointerType(llvm:llvmInt8Type(), 0)];
        llvm:LLVMTypeRef printfType = llvm:llvmFunctionType1(llvm:llvmInt32Type(), pointer_to_char_type, 1, 1);
        return llvm:llvmAddFunction(self.module, "printf", printfType);
    }

    function genNewIntArrayDeclaration() returns llvm:LLVMValueRef {
        llvm:LLVMTypeRef[] arguments = [llvm:llvmInt64Type()];
        llvm:LLVMTypeRef newIntArrayFunctionType = llvm:llvmFunctionType1(llvm:llvmPointerType(llvm:llvmInt64Type(), 0), arguments, 1, 0);
        return llvm:llvmAddFunction(self.module, "new_int_array", newIntArrayFunctionType);
    }
};
