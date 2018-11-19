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

endpoint grpc:Listener ep {
    port:9100
};

service TestService bind ep {
    @grpc:ResourceConfig {
        requestType: string,
        responseType: string
    }
    hello(endpoint caller, string name) {
    }

    @grpc:ResourceConfig {
        requestType: int,
        responseType: int,
        streaming: true
    }
    testInt(endpoint caller, int age) {
    }

    @grpc:ResourceConfig {
        requestType: float,
        responseType: float
    }
    testFloat(endpoint caller, float salary) {
    }

    @grpc:ResourceConfig {
        requestType: boolean,
        responseType: boolean
    }
    testBoolean(endpoint caller, boolean available) {
    }

    @grpc:ResourceConfig {
        requestType: Request,
        responseType: Response
    }
    testStruct(endpoint caller, Request msg) {
    }

    @grpc:ResourceConfig {
        responseType: string
    }
    testNoRequest(endpoint caller) {
        string resp = "service invoked with no request";
        io:println("Server send response : " + resp);
        error? err = caller->send(resp);
        if (err is error) {
            io:println("Error from Connector: " + err.reason());
        }
        _ = caller->complete();
    }

    @grpc:ResourceConfig {
        requestType: string
    }
    testNoResponse(endpoint caller, string msg) {
        io:println("Request: " + msg);
    }


    @grpc:ResourceConfig {
        requestType: Person,
        responseType: string
    }
    testInputNestedStruct(endpoint caller, Person req) {
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
