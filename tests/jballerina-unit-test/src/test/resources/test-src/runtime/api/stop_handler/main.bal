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

import ballerina/lang.runtime;
import ballerina/test;

import stop_handler.moduleA;

function stopHandlerFunc1() returns error? {
    runtime:sleep(1);
    moduleA:incrementCount();
    moduleA:assertCount(10);
}

function stopHandlerFunc2() returns error? {
    moduleA:incrementCount();
    moduleA:assertCount(9);
}

function stopHandlerFunc3() returns error? {
    runtime:sleep(0.5);
    moduleA:incrementCount();
    moduleA:assertCount(7);
}

function stopHandlerFunc4() returns error? {
    runtime:sleep(1.5);
    moduleA:incrementCount();
    moduleA:assertCount(8);
}

function init() {
    moduleA:incrementCount();
    moduleA:assertCount(1);
    runtime:onGracefulStop(stopHandlerFunc1);
    runtime:onGracefulStop(stopHandlerFunc2);
    runtime:onGracefulStop(stopHandlerFunc4);
}

final moduleA:ListenerObj lo = new moduleA:ListenerObj("ModDyncListener");

public function main() {
    moduleA:incrementCount();
    moduleA:assertCount(2);
    runtime:registerListener(lo);
    runtime:onGracefulStop(stopHandlerFunc3);

    checkpanic lo.'start();
    error? v = lo.gracefulStop();
    if (v is error) {
        test:assertEquals(v.message(), "listener in main stopped");
    }
    runtime:deregisterListener(lo);
    moduleA:main();
}
