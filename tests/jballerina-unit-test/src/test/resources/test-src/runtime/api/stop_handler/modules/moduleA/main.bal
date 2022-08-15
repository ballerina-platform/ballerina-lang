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
    runtime:sleep(1);
    incrementCount();
    assertCount(11);
}

function stopHandlerFunc2() returns error? {
    incrementCount();
    assertCount(6);
}

function init() {
    runtime:onGracefulStop(stopHandlerFunc1);
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
            runtime:sleep(1.5);
            incrementCount();
            assertCount(4);
            return error("listener in main stopped");
        } else if (self.name == "ModDyncListenerA") {
            return error("listener in moduleA stopped");
        }
    }

    public function immediateStop() {
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

final ListenerObj lo = new ListenerObj("ModDyncListenerA");

public function main() {
    incrementCount();
    assertCount(5);
    runtime:registerListener(lo);
    runtime:onGracefulStop(stopHandlerFunc2);

    checkpanic lo.'start();
    error? v = lo.gracefulStop();
    if (v is error) {
        test:assertEquals(v.message(), "listener in moduleA stopped");
    }
    runtime:deregisterListener(lo);
}

public function incrementCount() {
    count += 1;
}

public function assertCount(int val) {
    test:assertEquals(count, val);
}
