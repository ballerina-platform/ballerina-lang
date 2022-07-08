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

public function main() {
    ListenerObject1 ep = new ListenerObject1("ModDynA");
    runtime:registerListener(ep);
    runtime:registerListener(lo);
    checkpanic ep.gracefulStop();
    runtime:deregisterListener(ep);
    checkpanic lo.gracefulStop();
    runtime:deregisterListener(lo);
}

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

listener ListenerObject1 ep = new ListenerObject1("ModA");

final ListenerObj2 lo = new ListenerObj2();


public class ListenerObj2 {
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
