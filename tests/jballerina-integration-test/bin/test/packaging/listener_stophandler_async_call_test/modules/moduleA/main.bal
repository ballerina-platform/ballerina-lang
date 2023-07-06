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

import ballerina/jballerina.java;
import ballerina/lang.runtime;

public function stopHandlerFunc() returns error? {
    runtime:sleep(1);
    println("StopHandler1 of moduleA");
}

public function stopHandlerFunc2() returns error? {
    runtime:sleep(1);
    println("StopHandler2 of moduleA");
}

function init() {
    runtime:onGracefulStop(stopHandlerFunc);
    runtime:onGracefulStop(stopHandlerFunc2);
}

public class ListenerA {

    private string name = "";
    public function init(string name) {
        self.name = name;
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
        runtime:sleep(1);
        if (self.name == "ModA") {
            panic error("graceful stop of ModuleA");
        }
    }

    public function immediateStop() returns error? {
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

public class ListenerB {

    private string name = "";
    public function init(string name) {
        self.name = name;
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
        runtime:sleep(2);
    }

    public function immediateStop() returns error? {
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

listener ListenerA listenerA = new ListenerA("ModA");

final ListenerB listenerB = new ListenerB("ListenerObjectB");

public function main() {
    final ListenerA listenerA = new ListenerA("ModDynA");
    runtime:registerListener(listenerA);
    runtime:registerListener(listenerB);
    runtime:sleep(2);
    runtime:deregisterListener(listenerA);
    runtime:deregisterListener(listenerB);
}

public function println(string value) {
    handle strValue = java:fromString(value);
    handle stdout1 = stdout();
    printlnInternal(stdout1, strValue);
}
public function stdout() returns handle = @java:FieldGet {
    name: "out",
    'class: "java/lang/System"
} external;

public function printlnInternal(handle receiver, handle strValue)  = @java:Method {
    name: "println",
    'class: "java/io/PrintStream",
    paramTypes: ["java.lang.String"]
} external;
