// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function init() {
	println("Initializing module 'basic'");
}

public class TestListener {

    private string name = "";

    public function init(string name){
        self.name = name;
    }

    public function 'start() returns error? {
        println("basic:TestListener listener start called, service name - " + self.name);
        if (self.name == "dependent") {
            println("listener start panicked for service name - " + self.name);
            error sampleErr = error("panicked while starting module 'dependent'");
            panic sampleErr;
        }
    }

    public function gracefulStop() returns error? {
        println("basic:TestListener listener gracefulStop called, service name - " + self.name);
        if (self.name == "dependent") {
            println("listener gracefulStop panicked, service name - " + self.name);
            error sampleErr = error("panicked while stopping module 'dependent'");
            panic sampleErr;
        }
        return ();
    }

    public function immediateStop() returns error? {
        println("basic:TestListener listener immediateStop called, service name - " + self.name);
        return ();
    }

    public function attach(service object {} s, string[]? name) returns error? {
        println("basic:TestListener listener attach called, service name - " + self.name);
    }

    public function detach(service object {} s) returns error? {
        println("basic:TestListener listener detach called, service name - " + self.name);
    }
}

listener TestListener testListner = new TestListener("basic");

public function println(string value) {
    handle strValue = java:fromString(value);
    handle stdout1 = stdout();
    printlnInternal(stdout1, strValue);
}

function stdout() returns handle = @java:FieldGet {
    name: "out",
    'class: "java/lang/System"
} external;

function printlnInternal(handle receiver, handle strValue)  = @java:Method {
    name: "println",
    'class: "java/io/PrintStream",
    paramTypes: ["java.lang.String"]
} external;
