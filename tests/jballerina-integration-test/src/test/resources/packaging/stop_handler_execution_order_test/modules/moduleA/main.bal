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
import ballerina/lang.runtime;

int initCount = 0;

function init() {
    initCount += 1;
    moduleB:incrementAndAssertInt(2);
    runtime:onGracefulStop(stopHandlerFunc1);
}

public function stopHandlerFunc1() returns error? {
    moduleB:incrementAndAssertInt(9);
    moduleB:println("stopHandlerFunc1 in moduleA");
    runtime:onGracefulStop(stopHandlerFunc2);
}

public function stopHandlerFunc2() returns error? {
    moduleB:incrementAndAssertInt(11);
    moduleB:println("stopHandlerFunc2 in moduleA");
}

public function stopHandlerFunc3() returns error? {
    moduleB:println("stopHandlerFunc3 in moduleA");
}

public function main() {
}

public function getInitCount() returns int {
    return initCount;
}
