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

// A variable with block scope can have the same name as a variable with module scope; 
// the former variable will hide the latter variable while the former variable is in scope.
// TODO: allow block scope variables with the same name as module scope variables
// https://github.com/ballerina-platform/ballerina-lang/issues/13273
@test:Config {
    groups: ["deviation"]
}
function testBlockScopeOverridingModuleScopeBroken() {
    //string s1 = "module level variable redefined";
    //string moduleLevelVariable = s1;
    //test:assertEquals(moduleLevelVariable, s1,
    //                  msg = "expected block scope variable to override module scope variable");
}
