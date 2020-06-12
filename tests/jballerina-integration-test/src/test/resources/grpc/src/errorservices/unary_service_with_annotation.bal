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
import ballerina/grpc;
import ballerina/io;

listener grpc:Listener server8 = new (9100);

service TestService on server8 {

    resource function hello(grpc:Caller caller, string name) {
    }

    resource function testInt(grpc:Caller caller, int age) {
    }

    resource function testFloat(grpc:Caller caller, float salary) {
    }

    resource function testBoolean(grpc:Caller caller, boolean available) {
    }

    resource function testStruct(grpc:Caller caller, Request msg) {
    }

    resource function testNoRequest(grpc:Caller caller) {
        string resp = "service invoked with no request";
        io:println("Server send response : " + resp);
        grpc:Error? err = caller->send(resp);
        if (err is grpc:Error) {
            io:println("Error from Connector: " + err.message());
        }
        checkpanic caller->complete();
    }

    resource function testNoResponse(grpc:Caller caller, string msg) {
        io:println("Request: " + msg);
    }

    resource function testInputNestedStruct(grpc:Caller caller, Person req) {
    }
}

type Request record {
    string name = "";
    string message = "";
    int age = 0;
};

type Response record {
    string resp = "";
};

type Person record {
    string name = "";
    Address address = {};
};

type Address record {
    int postalCode = 0;
    string state = "";
    string country = "";
};
