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

function init() {
   incrementCount();
   assertCount(1);
}

public class ListenerObject1 {

    private string name = "";

    public function init(string name){
        self.name = name;
    }

    public function 'start() returns error? {
        incrementCount();
        if (self.name == "DynamicListenerObject1") {
            assertCount(2);
        } else if (self.name == "ListenerObject1") {
            assertCount(3);
        }
    }

    public function gracefulStop() returns error? {
        incrementCount();
        future<int> f = start waitAndReturnInt(850);
        int i = check wait f;
        if (self.name == "ListenerObject1") {
            assertCount(4);
            panic error("Stopped module A");
        }
    }

    public function immediateStop() returns error? {
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

public class ListenerObject2 {
    public function init() {}

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
        future<int> f = start waitAndReturnInt(750);
        int _ = check wait f;
    }

    public function immediateStop() returns error? {}

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

listener ListenerObject1 ep1 = new ListenerObject1("ListenerObject1");
listener ListenerObject2 ep2 = new();

public function main() {
    ListenerObject1 ep1 = new("DynamicListenerObject1");
    checkpanic ep1.'start();
    runtime:registerListener(ep1);

    ListenerObject2 ep2 = new();
    checkpanic ep2.'start();
    runtime:registerListener(ep2);

    runtime:deregisterListener(ep1);
    runtime:deregisterListener(ep2);
}

function waitAndReturnInt(int i) returns int {
    runtime:sleep(<decimal> i/1000);
    return i;
}

public function incrementCount() {
    count += 1;
}

public function assertCount(int val) {
    test:assertEquals(count, val);
}
