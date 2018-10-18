// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/runtime;
import ballerina/http;

boolean blockFunction = true;

type Student record {
    int id;
    string name;
    float height;
};

@http:ServiceConfig {
    basePath: "/s1"
}
service<http:Service> s1 bind { port: 9090 } {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/r1"
    }

    @interruptible
    r1(endpoint conn, http:Request req) {
        io:println("Starting flow...");
        http:Response res = new;
        var response = conn->respond(res);
        f1();
        io:println("State completed");
    }

    r2(endpoint conn, http:Request req) {
        blockFunction = false;
        http:Response res = new;
        var response = conn->respond(res);
    }
}

function f1() {
    // Basic types
    int x = 5;
    float f = 20.0;
    byte c = 23;
    string name = "Ballerina";
    boolean b = true;
    string template = string `Hello {{name}}!!!`;
    Student student1 = { id: 1, name: "Waruna", height: 175.1 };
    map addrMap = { line1: "No. 20", line2: "Palm Grove", city: "Colombo 03", country: "Sri Lanka" };
    (int, string) t1 = (10, "John");
    json j1 = [1, false, null, "foo", { first: "John", last: "Pala" }];
    xml x1 = xml `<book>The Lost World</book>`;

    // Function pointers
    function (int) returns (int) bar1 = option1;
    if (x < 10) {
        bar1 = option2;
    }
    runtime:checkpoint();
    io:println("Waiting on second request");
    while (blockFunction) {

    }
    int resultX = bar1(x);

    // Print results after checkpoint
    io:println("Int value:" + x);
    io:println(name);
    io:println(c);
    io:println("Float value:" + f);
    io:println("Boolean value:" + b);
    io:println("Template value:" + template);
    io:println(j1);
    io:println(x1);
    io:println(t1);
    io:println(student1);
    io:println("Function Pointer value :" + resultX);
}

function option1(int i) returns (int) {
    return i * 4;
}

function option2(int i) returns (int) {
    return i * 2;
}
