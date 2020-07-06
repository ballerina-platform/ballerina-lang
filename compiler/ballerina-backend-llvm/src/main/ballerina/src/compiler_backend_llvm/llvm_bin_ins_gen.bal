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

type BinaryInsGenrator object {

    llvm:LLVMBuilderRef builder;
    string lhsTmpName;
    llvm:LLVMValueRef lhsRef;
    llvm:LLVMValueRef rhsOp1;
    llvm:LLVMValueRef rhsOp2;

    function init(llvm:LLVMBuilderRef builder, string lhsTmpName, llvm:LLVMValueRef lhsRef, llvm:LLVMValueRef rhsOp1,
                    llvm:LLVMValueRef rhsOp2) {
        self.builder = builder;
        self.lhsTmpName = lhsTmpName;
        self.lhsRef = lhsRef;
        self.rhsOp1 = rhsOp1;
        self.rhsOp2 = rhsOp2;
    }

    function genAdd() {
        var addReturn = llvm:llvmBuildAdd(self.builder, self.rhsOp1, self.rhsOp2, self.lhsTmpName);
        var loaded = llvm:llvmBuildStore(self.builder, addReturn, self.lhsRef);
    }

    function genDiv() {
        var ifReturn = llvm:llvmBuildSDiv(self.builder, self.rhsOp1, self.rhsOp2, self.lhsTmpName);
        var loaded = llvm:llvmBuildStore(self.builder, ifReturn, self.lhsRef);
    }

    function genEqual() {
        var ifReturn = llvm:llvmBuildICmp(self.builder, llvm:LLVMIntEQ, self.rhsOp1, self.rhsOp2, self.lhsTmpName);
        var loaded = llvm:llvmBuildStore(self.builder, ifReturn, self.lhsRef);
    }

    function genGreaterEqual() {
        var ifReturn = llvm:llvmBuildICmp(self.builder, llvm:LLVMIntSGE, self.rhsOp1, self.rhsOp2, self.lhsTmpName);
        var loaded = llvm:llvmBuildStore(self.builder, ifReturn, self.lhsRef);
    }

    function genGreaterThan() {
        var ifReturn = llvm:llvmBuildICmp(self.builder, llvm:LLVMIntSGT, self.rhsOp1, self.rhsOp2, self.lhsTmpName);
        var loaded = llvm:llvmBuildStore(self.builder, ifReturn, self.lhsRef);
    }

    function genLessEqual() {
        var ifReturn = llvm:llvmBuildICmp(self.builder, llvm:LLVMIntSLE, self.rhsOp1, self.rhsOp2, self.lhsTmpName);
        var loaded = llvm:llvmBuildStore(self.builder, ifReturn, self.lhsRef);
    }

    function genLessThan() {
        var ifReturn = llvm:llvmBuildICmp(self.builder, llvm:LLVMIntSLT, self.rhsOp1, self.rhsOp2, self.lhsTmpName);
        var loaded = llvm:llvmBuildStore(self.builder, ifReturn, self.lhsRef);
    }

    function genMul() {
        var ifReturn = llvm:llvmBuildMul(self.builder, self.rhsOp1, self.rhsOp2, self.lhsTmpName);
        var loaded = llvm:llvmBuildStore(self.builder, ifReturn, self.lhsRef);
    }

    function genNotEqual() {
        var ifReturn = llvm:llvmBuildICmp(self.builder, llvm:LLVMIntNE, self.rhsOp1, self.rhsOp2, self.lhsTmpName);
        var loaded = llvm:llvmBuildStore(self.builder, ifReturn, self.lhsRef);
    }

    function genSub() {
        var ifReturn = llvm:llvmBuildSub(self.builder, self.rhsOp1, self.rhsOp2, self.lhsTmpName);
        var loaded = llvm:llvmBuildStore(self.builder, ifReturn, self.lhsRef);
    }
};


