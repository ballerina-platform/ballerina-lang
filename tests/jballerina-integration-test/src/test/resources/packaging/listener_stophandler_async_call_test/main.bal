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
import listener_stophandler_async_call_test.moduleA;

public function stopHandlerFunc1() returns error? {
    runtime:sleep(1);
    moduleA:println("calling StopHandler1 of current module");
}

public function stopHandlerFunc2() returns error? {
    runtime:sleep(1);
    moduleA:println("calling StopHandler2 of current module");
}

function init() {
    runtime:onGracefulStop(stopHandlerFunc1);
    runtime:onGracefulStop(stopHandlerFunc2);
}


public class Listener {

    *runtime:DynamicListener;
    private string name = "";

    public function init(string name) {
        self.name = name;
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
        runtime:sleep(1);
        moduleA:println("calling gracefulStop for " + self.name);
    }

    public function immediateStop() returns error? {
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

listener Listener staticListener = new Listener("static listener of current module");

public function main() {
    Listener dynamicListener = new Listener("dynamic listener of current module");
    runtime:registerListener(dynamicListener);
    runtime:sleep(2);
    runtime:deregisterListener(dynamicListener);
    moduleA:main();
}
