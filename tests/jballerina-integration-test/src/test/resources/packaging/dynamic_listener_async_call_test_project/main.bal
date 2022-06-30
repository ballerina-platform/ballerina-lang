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

public class ListenerObject1 {

    private string name = "";

    public function init(string name){
        self.name = name;
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
        runtime:sleep(2);
        if (self.name == "ModA") {
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

    public function 'start() returns error? {}

    public function gracefulStop() returns error? {
        runtime:sleep(2);
    }

    public function immediateStop() returns error? {}

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

public class ListenerObject3 {

    private string name = "";

    public function init(string name){
        self.name = name;
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

final ListenerObject1 ep = new ListenerObject1("ModA");
final ListenerObject2 lo2 = new ListenerObject2();
final ListenerObject3 lo3 = new ListenerObject3("ListenerObject3");

public function main() {
    runtime:registerListener(ep);
    runtime:registerListener(lo2);
    runtime:registerListener(lo3);
    runtime:sleep(3);
    runtime:deregisterListener(ep);
    runtime:deregisterListener(lo2);
    runtime:deregisterListener(lo3);
}
