// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/io;
import ballerina/lang.'object;

function init() {
	io:println("Initializing module 'basic'");
}

public function main() {
}

public class TestListener {

    *'object:Listener;
    private string name = "";

    public function init(string name){
        self.name = name;
    }

    public function __start() returns error? {
        io:println("basic:TestListener listener __start called, service name - " + self.name);
    }

    public function __gracefulStop() returns error? {
        io:println("basic:TestListener listener __gracefulStop called, service name - " + self.name);
        if (self.name == "dependent") {
            io:println("listener __gracefulStop panicked, service name - " + self.name);
            error sampleErr = error("panicked while stopping module 'dependent'");
            panic sampleErr;
        }
        return ();
    }

    public function __immediateStop() returns error? {
        io:println("basic:TestListener listener __immediateStop called, service name - " + self.name);
        return ();
    }

    public function __attach(service s, string? name = ()) returns error? {
        io:println("basic:TestListener listener __attach called, service name - " + self.name);
    }

    public function __detach(service s) returns error? {
        io:println("basic:TestListener listener __detach called, service name - " + self.name);
    }
}

listener TestListener ep = new TestListener("basic");
