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

import stop_handler_execution_order_test.moduleB;
import stop_handler_execution_order_test.moduleA;
import ballerina/test;
import ballerina/lang.runtime;

int initCount = 0;

function init() {
    initCount += 1;
    moduleB:incrementAndAssertInt(3);
    runtime:onGracefulStop(stopHandlerFunc1);
    runtime:onGracefulStop(stopHandlerFunc2);
    runtime:onGracefulStop(stopHandlerFunc5);
    runtime:onGracefulStop(stopHandlerFunc5);
    runtime:onGracefulStop(moduleA:stopHandlerFunc3);
}

public function stopHandlerFunc1() returns error? {
    moduleB:incrementAndAssertInt(7);
    moduleB:println("Stopped current module");
}

public function stopHandlerFunc2() returns error? {
    moduleB:incrementAndAssertInt(6);
    moduleB:println("StopHandlerFunc2 in current module");
}

public function stopHandlerFunc3() returns error? {
    moduleB:incrementAndAssertInt(5);
    moduleB:println("StopHandlerFunc3 in current module");
    runtime:onGracefulStop(stopHandlerFunc4);
}

public function stopHandlerFunc4() returns error? {
    moduleB:incrementAndAssertInt(7);
    moduleB:println("StopHandlerFunc4 in current module");
    runtime:onGracefulStop(stopHandlerFunc5);
}

public function stopHandlerFunc5() returns error? {
    moduleB:println("StopHandlerFunc5 in current module");
}

public function main() {
    moduleB:incrementAndAssertInt(4);
    runtime:onGracefulStop(stopHandlerFunc3);

    string errorMsg = "Assertion Failed for StopHandler at ";
    test:assertEquals(initCount, 1, errorMsg + "current" );
    test:assertEquals(moduleA:getInitCount(), 1, errorMsg + "moduleA");
    test:assertEquals(moduleB:getInitCount(), 1, errorMsg + "moduleB");
    runtime:sleep(3);
}
