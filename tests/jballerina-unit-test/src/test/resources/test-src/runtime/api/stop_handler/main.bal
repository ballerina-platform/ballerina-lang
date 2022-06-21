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

int count = 0;

function stopHandlerFunc1() returns error? {
    runtime:sleep(2);
    incrementCount();
    assertCount(8);
}

function stopHandlerFunc2() returns error? {
    incrementCount();
    assertCount(7);
}

function stopHandlerFunc3() returns error? {
    runtime:sleep(2);
    incrementCount();
    assertCount(5);
}

function stopHandlerFunc4() returns error? {
    runtime:sleep(2);
    incrementCount();
    assertCount(6);
    runtime:onGracefulStop(stopHandlerFunc5);
}

function stopHandlerFunc5() returns error? {
    runtime:sleep(2);
    incrementCount();
    assertCount(4);
}

function init() {
    incrementCount();
    assertCount(1);
    runtime:onGracefulStop(stopHandlerFunc1);
    runtime:onGracefulStop(stopHandlerFunc2);
    runtime:onGracefulStop(stopHandlerFunc4);
}

public class ListenerObj {

    *runtime:DynamicListener;
    private string name = "";

    public function init(string name) {
        self.name = name;
    }

    public function 'start() returns error? {
        if (self.name == "ModDyncListener") {
            incrementCount();
            assertCount(3);
        }
    }

    public function gracefulStop() returns error? {
        if (self.name == "ModDyncListener") {
            incrementCount();
            assertCount(4);
        }
    }

    public function immediateStop() {
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

final ListenerObj lo = new ListenerObj("ModDyncListener");

public function main() {
    incrementCount();
    assertCount(2);
    runtime:registerListener(lo);
    runtime:onGracefulStop(stopHandlerFunc3);

    checkpanic lo.'start();
    checkpanic lo.gracefulStop();
    runtime:deregisterListener(lo);
}

function incrementCount() {
    count += 1;
}

function assertCount(int val) {
    test:assertEquals(count, val);
}
