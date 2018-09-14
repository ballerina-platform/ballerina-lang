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

boolean blockWorker1 = true;
boolean blockWorker2 = true;

type Person object {
    string name;
    int age;
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
        blockWorker1 = false;
        http:Response res = new;
        var response = conn->respond(res);
    }

    r3(endpoint conn, http:Request req) {
        blockWorker2 = false;
        http:Response res = new;
        var response = conn->respond(res);
    }
}

function f1() {
    fork {
        worker w1 {
            Person p = new;
            p.name = "worker 1";
            while (blockWorker1){
            }
            runtime:checkpoint();
            io:println("Worker 1 parameter name " + p.name);
            p -> fork;
        }
        worker w2 {
            Person p = new;
            p.name = "worker 2";
            while (blockWorker2){
            }
            runtime:checkpoint();
            io:println("Worker 2 parameter name " + p.name);
            p -> fork;
        }
    } join (all) (map results) {
        Person pw1 = check <Person>results["w1"];
        Person pw2 = check <Person>results["w2"];
        io:println("[join-block] fW1: ", pw1.name);
        io:println("[join-block] fW2: ", pw2.name);
    }
}
