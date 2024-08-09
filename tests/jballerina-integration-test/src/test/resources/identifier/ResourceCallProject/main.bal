// Copyright (c) 2024 WSO2 LLC. (http://www.wso2.com).
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
import ballerina/test;

import a_b/foo;

public function main() returns error? {
    foo:Client cl = check new ("test");
    var response = check cl->/users\.getPresence();
    test:assertEquals(response, "/users.getPresence test");
    print("Tests passed");
}

function print(string value) {
    handle strValue = java:fromString(value);
    handle stdout1 = stdout();
    printInternal(stdout1, strValue);
}

public function stdout() returns handle = @java:FieldGet {
    name: "out",
    'class: "java/lang/System"
} external;

public function printInternal(handle receiver, handle strValue) = @java:Method {
    name: "println",
    'class: "java/io/PrintStream",
    paramTypes: ["java.lang.String"]
} external;

public isolated client class Client {
    final string greeting;

    public isolated function init(string greeting = "Hello World!") returns error? {
        self.greeting = greeting;
        return;
    }

    resource isolated function get users\.getPresence(map<string|string[]> headers = {}) returns string|error {
        return string `/users.getPresence ${self.greeting} ${headers.toString()}`;
    }
}
