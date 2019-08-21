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


service HelloWorld on new grpc:Listener(9090) {

    resource function returnInt(grpc:Caller caller, string name) returns int {
        return 10;
    }

    resource function returnString(grpc:Caller caller, string name) returns string {
        return "Test";
    }

    resource function returnUnion(grpc:Caller caller, string name) returns string|error {
        error err = error("Return Error");
        return err;
    }

    resource function returnUserType(grpc:Caller caller, string name) returns Person {
        Person p = {};
        return p;
    }

    resource function returnTupleType(grpc:Caller caller, string name) returns [Person,grpc:Headers] {
        Person p = {};
        grpc:Headers headers = new;
        return [p,headers];
    }
}

type Person record {
    string name = "";
    int age = 0;
};
