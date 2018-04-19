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
import ballerina/grpc;

endpoint grpc:Listener ep {
    host:"localhost",
    port:9090
};

@grpc:serviceConfig
service HelloWorld bind ep {
    hello (endpoint caller, string name) {
        io:println("name: " + name);
        string message = "Hello " + name;
        error? err = caller -> send(message);
        io:println(err.message but {() => ("Server send response : " + message)});
        _ = caller -> complete();
    }

    testInt (endpoint caller, int age) {
        io:println("age: " + age);
        int displayAge = age - 2;
        error? err = caller -> send(displayAge);
        io:println(err.message but {() => ("display age : " + displayAge)});
        _ = caller -> complete();
    }

    testFloat (endpoint caller, float salary) {
        io:println("gross salary: " + salary);
        float netSalary = salary * 0.88;
        error? err = caller -> send(netSalary);
        io:println(err.message but {() => ("net salary : " + netSalary)});
        _ = caller -> complete();
    }

    testBoolean (endpoint caller, boolean available) {
        io:println("is available: " + available);
        boolean aval = available || true;
        error? err = caller -> send(aval);
        io:println(err.message but {() => ("avaliability : " + aval)});
        _ = caller -> complete();
    }

    testStruct (endpoint caller, Request msg) {
        io:println(msg.name + " : " + msg.message);
        Response response = {resp:"Acknowledge " + msg.name};
        error? err = caller -> send(response);
        io:println(err.message but {() => ("msg : " + response.resp)});
        _ = caller -> complete();
    }

    testNoRequest (endpoint caller) {
        string resp = "service invoked with no request";
        error? err = caller -> send(resp);
        io:println(err.message but {() => ("response : " + resp)});
        _ = caller -> complete();
    }

    testNoResponse (endpoint caller, string msg) {
        io:println("Request: " + msg);
    }
}

type Request {
    string name;
    string message;
    int age;
};

type Response {
    string resp;
};
