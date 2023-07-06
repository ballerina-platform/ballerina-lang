// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import testorg/module_invocation_project.basic as basic;
import testorg/module_invocation_project.dependent1 as dependent1;
import testorg/module_invocation_project.dependent2 as dependent2;

import ballerina/test;

int initCount = 0;

function init() {
    initCount += 1;
    basic:incrementAndAssertInt(4);
}

public function main() {
    basic:incrementAndAssertInt(5);

    string errorMsg = "Assertion Failed for service - ";
    test:assertEquals(initCount, 1, errorMsg + "current" );
    test:assertEquals(dependent1:getInitCount(), 1, errorMsg + "first dependent");
    test:assertEquals(dependent2:getInitCount(), 1, errorMsg + "second dependent");
    test:assertEquals(basic:getInitCount(), 1, errorMsg + "basic");

}

listener basic:TestListener ep = new basic:TestListener("current");
