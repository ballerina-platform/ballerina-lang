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

endpoint grpc:Service ep {
    host:"localhost",
    port:9090
};

@grpc:serviceConfig {generateClientConnector:false}
service<grpc:Endpoint> helloWorld bind ep {
    hello (endpoint client, string name) {
        io:println("name: " + name);
        string message = "Hello " + name;
        grpc:ConnectorError err = client -> send(message);
        io:println("Server send response : " + message );
        if (err != ()) {
            io:println("Error at helloWorld : " + err.message);
        }
        _ = client -> complete();
    }

    testInt (endpoint client, int age) {
        io:println("age: " + age);
        int displayAge = age - 2;
        grpc:ConnectorError err = client -> send(displayAge);
        io:println("display age : " + displayAge);
        if (err != ()) {
            io:println("Error at test : " + err.message);
        }
        _ = client -> complete();
    }

    testFloat (endpoint client, float salary) {
        io:println("gross salary: " + salary);
        float netSalary = salary * 0.88;
        grpc:ConnectorError err = client -> send(netSalary);
        io:println("net salary : " + netSalary);
        if (err != ()) {
            io:println("Error at test : " + err.message);
        }
        _ = client -> complete();
    }

    testBoolean (endpoint client, boolean available) {
        io:println("is available: " + available);
        boolean aval = available || true;
        grpc:ConnectorError err = client -> send(aval);
        io:println("avaliability : " + aval);
        if (err != ()) {
            io:println("Error at test : " + err.message);
        }
        _ = client -> complete();
    }

    testStruct (endpoint client, Request msg) {
        io:println(msg.name + " : " + msg.message);
        Response response = {resp:"Acknowledge " + msg.name};
        grpc:ConnectorError err = client -> send(response);
        io:println("msg : " + response.resp);
        if (err != ()) {
            io:println("Error at test : " + err.message);
        }
        _ = client -> complete();
    }

    testNoRequest (endpoint client) {
        string resp = "service invoked with no request";
        grpc:ConnectorError err = client -> send(resp);
        io:println("response : " + resp);
        if (err != ()) {
            io:println("Error at test : " + err.message);
        }
        _ = client -> complete();
    }

    testNoResponse (endpoint client, string msg) {
        io:println("Request: " + msg);
    }
}

type Request {
    string name;
    string message;
    int age;
}

type Response {
    string resp;
}
