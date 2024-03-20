// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

Listener dynamicListener = check new Listener("dynamic listener");
listener Listener staticListener = check new Listener("static listener");


public function main() returns error? {
    print("main called");
    runtime:registerListener(dynamicListener);
    check dynamicListener.'start();
}

public class Listener {

    *runtime:DynamicListener;
    private string name = "";

    public isolated function init(string name) returns error? {
        print("Calling init for " + name);
        self.name = name;
    }

    public isolated function attach(service object {} s, string[]|string? name = ()) returns error? {
        print("Calling attach for " + self.name);
    }

    public isolated function detach(service object {} s) returns error? {
        print("Calling detach for " + self.name);
    }

    public isolated function 'start() returns error? {
        print("Calling start for " + self.name);
    }

    public isolated function gracefulStop() returns error? {
        print("Calling stop for " + self.name);
        return error("error during the gracefulStop call of " + self.name);
    }

    public isolated function immediateStop() returns error? {
        print("Calling immediateStop for " + self.name);
    }
}

function init() {
    runtime:onGracefulStop(stopHandler1);
    runtime:onGracefulStop(stopHandler2);
}

public function stopHandler1() returns error? {
    print("stopHandler1 called");
}

public function stopHandler2() returns error? {
    print("stopHandler2 called");
    return error("error during the gracefulStop call of StopHandler2");
}

isolated function print(string value) {
    handle strValue = java:fromString(value);
    handle stdout1 = stdout();
    printInternal(stdout1, strValue);
}

isolated function stdout() returns handle = @java:FieldGet {
    name: "out",
    'class: "java/lang/System"
} external;

isolated function printInternal(handle receiver, handle strValue) = @java:Method {
    name: "println",
    'class: "java/io/PrintStream",
    paramTypes: ["java.lang.String"]
} external;
