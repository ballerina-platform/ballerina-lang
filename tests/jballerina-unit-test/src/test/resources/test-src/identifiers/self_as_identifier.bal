// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/test;

class MyClass {
    int n;

    function init(int n) {
        self.n = n;
    }

    function func() {
        self.n += 1;
    }
}

function funcWithSelfAsParamName(int self) returns int {
    return self + 5;
}

function funcWithSelfAsDefaultableParamName(string self = "abc") returns string {
    return self;
}

function funcWithQuotedSelfAsParamName(int 'self) returns int {
    return 'self + 5;
}

function testFuncWithSelfAsParamName() {
    test:assertEquals(funcWithSelfAsParamName(2), 7);
    test:assertEquals(funcWithQuotedSelfAsParamName(2), 7);
    test:assertEquals(funcWithSelfAsDefaultableParamName(), "abc");
}

function testSelfAsIdentifier() {
    int self = funcWithSelfAsParamName(2);
    MyClass x = new MyClass(12);
    x.func();
    int n = x.n + self;
    test:assertEquals(n, 20);
}
