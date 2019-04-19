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

service HelloWorld on new grpc:Listener(9090) {

    resource function testInputNestedStruct(grpc:Caller caller, Person req) {
        io:println("name: " + req.name);
        io:println(req.address);
        string message = "Submitted name: " + req.name;
        error? err = caller->send(message);
        if (err is error) {
            io:println("Error from Connector: " + err.reason());
        } else {
            io:println("Server send response : " + message);
        }
        checkpanic caller->complete();
    }

    resource function testOutputNestedStruct(grpc:Caller caller, string name) {
        io:println("requested name: " + name);
        Person person = {name:"Sam", address:{postalCode:10300, state:"CA", country:"USA"}};
        io:println(person);
        error? err = caller->send(person);
        if (err is error) {
            io:println("Error from Connector: " + err.reason());
        }
        checkpanic caller->complete();
    }
}

type Person record {
    string name = "";
    Address address = {};
};

type Address record {
    int postalCode = 0;
    string state = "";
    string country = "";
};
