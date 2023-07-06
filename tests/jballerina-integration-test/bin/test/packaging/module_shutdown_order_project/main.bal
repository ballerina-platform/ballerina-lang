// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import module_shutdown_order_project.moduleC;
import module_shutdown_order_project.moduleA;
import module_shutdown_order_project.moduleB;
import ballerina/lang.runtime;

int initCount = 0;

function init() {
    initCount += 1;
    moduleC:incrementAndAssertInt(4);

    runtime:onGracefulStop(stopHandlerFunc);
}

function stopHandlerFunc() returns error? {
    moduleC:assertCallsToStopHandlers("Stopped current module");
}

public function main() {
    moduleC:incrementAndAssertInt(5);

    string errorMsg = "Assertion Failed for service - ";
    test:assertEquals(initCount, 1, errorMsg + "current" );
    test:assertEquals(moduleA:getInitCount(), 1, errorMsg + "first dependent");
    test:assertEquals(moduleB:getInitCount(), 1, errorMsg + "second dependent");
    test:assertEquals(moduleC:getInitCount(), 1, errorMsg + "moduleC");

}

listener moduleC:TestListener ep = new moduleC:TestListener("current");
