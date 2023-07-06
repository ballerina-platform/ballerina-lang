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

// Empty main bal

import ballerina/test;
import waruna/http.'client as httpClient;
import waruna/websub.server as websubServer;
import myPackage.mod1;
import ballerina/jballerina.java;

public function main() {
    test:assertEquals(getHtppVersion(), "1.0.1");
    test:assertEquals(getWebSubHtppVersion(), "1.0.1");
    test:assertEquals(mod1:getHtppVersion(), "1.0.1");
    test:assertEquals(mod1:getWebSubHtppVersion(), "1.0.1");
    print("Tests passed");
}

public function getWebSubHtppVersion() returns string {
     return websubServer:getHttpVersion();
}

public function getHtppVersion() returns string {
     return httpClient:getVersion();
}

function system_out() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

function println(handle receiver, handle arg0) = @java:Method {
    name: "println",
    'class: "java.io.PrintStream",
    paramTypes: ["java.lang.String"]
} external;

function print(string str) {
    println(system_out(), java:fromString(str));
}
