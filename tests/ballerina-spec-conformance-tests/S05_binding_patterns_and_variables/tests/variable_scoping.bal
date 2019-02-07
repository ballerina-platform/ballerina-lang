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

import ballerina/test;

// There are two kinds of scope: module scope and block scope.
// A variable with module scope can be referenced anywhere within a module.
// Identifiers with module scope are used to identify not only variables
// but other module-level entities such as functions.
string moduleLevelVariable = "module level variable";

function moduleLevelFunction() returns string {
    return moduleLevelVariable;
}

@test:Config {}
function testModuleScope() {
    test:assertEquals(moduleLevelVariable, moduleLevelFunction(), 
                      msg = "expected module level access to result in the same values");
}

// A variable with block scope can be referenced only within a particular block (always delimited with curly braces). 
// Block-scope variables are created by a variety of different constructs, many of which use a typed-binding-pattern. 
@test:Config {}
function testBlockScope() {
    string s1 = moduleLevelVariable;
    string s2 = "";

    if (true) {
        string s3 = moduleLevelVariable;
        s2 = s3;
    }
 
    test:assertEquals(s1, s2, msg = "expected block scope variables to hold the same value");
}
