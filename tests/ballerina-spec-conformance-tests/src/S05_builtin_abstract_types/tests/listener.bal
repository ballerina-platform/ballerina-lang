// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/test;
import ballerina/lang.'object;

int globalVar = 0;
const START_METHOD_INCREMENT_VALUE = 3;
const ATTACH_METHOD_INCREMENT_VALUE = 2;
const DETACH_METHOD_INCREMENT_VALUE = 5;

// The Listener type is defined as follows.
//
// abstract object {
//    function __attach (service s, string? name = ()) returns error?;
//    function __detach (service s) returns error?;
//    function __start () returns error?;
//    function __gracefulStop() returns error?;
//    function __immediateStop() returns error?;
// }
type CustomListener object {
    *'object:Listener;

    private string listenerName;

    public function init(string name) {
        self.listenerName = name;
    }

    public function __start() returns error? {
        globalVar += START_METHOD_INCREMENT_VALUE;
    }

    public function __gracefulStop() returns error? {
        return ();
    }

    public function __immediateStop() returns error? {
        return ();
    }

    public function __attach(service s, string? name = ()) returns error? {
        globalVar += ATTACH_METHOD_INCREMENT_VALUE;
    }

    public function __detach(service s) returns error? {
        globalVar += DETACH_METHOD_INCREMENT_VALUE;
    }
};

@test:Config {
}
function testAbstractListener() {
    test:assertEquals(globalVar, START_METHOD_INCREMENT_VALUE + ATTACH_METHOD_INCREMENT_VALUE,
        msg = "expected service to attach to listener object");
    error? err = customListener.__detach(testService);
    test:assertEquals(globalVar, START_METHOD_INCREMENT_VALUE + ATTACH_METHOD_INCREMENT_VALUE +
                                    DETACH_METHOD_INCREMENT_VALUE,
            msg = "expected service to detach from the listener");
}

listener CustomListener customListener = new CustomListener("custom-listener");

service testService on customListener {
    resource function sayHello() {
        // do nothing
    }
}
