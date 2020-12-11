// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class NoPathListener {
    public isolated function 'start() returns error? {
    }
    public isolated function gracefulStop() returns error? {
    }
    public isolated function immediateStop() returns error? {
    }
    public isolated function detach(service object {} s) returns error? {
    }
    public isolated function attach(service object {} s, () name = ()) returns error? {
    }
    isolated function register(service object {} s, string[]|string? name) returns error? {
    }
}

type S service object {
    resource function get processRequest() returns json;
};

service S / on new NoPathListener() {

    resource function get processRequest() returns json {
        return { output: "Hello" };
    }

    function createError() returns @tainted error? {
        return ();
    }
}

service on new NoPathListener() {

}

listener NoPathListener l = new();

service on l {

}

service / on new NoPathListener() {

}

service /foo on new NoPathListener() {

}

service /foo/bar on new NoPathListener() {

}

service "service-name" on new NoPathListener() {

}

public class LiteralNameListener {
    public isolated function 'start() returns error? {
    }
    public isolated function gracefulStop() returns error? {
    }
    public isolated function immediateStop() returns error? {
    }
    public isolated function detach(service object {} s) returns error? {
    }
    public isolated function attach(service object {} s, string name) returns error? {
    }
    isolated function register(service object {} s, string[]|string? name) returns error? {
    }
}

service "service-name" on new LiteralNameListener() {

}

service on new LiteralNameListener() {

}

service / on new LiteralNameListener() {

}

public class OptionalLiteralNameListener {
    public isolated function 'start() returns error? {
    }
    public isolated function gracefulStop() returns error? {
    }
    public isolated function immediateStop() returns error? {
    }
    public isolated function detach(service object {} s) returns error? {
    }
    public isolated function attach(service object {} s, string? name = ()) returns error? {
    }
    isolated function register(service object {} s, string[]|string? name) returns error? {
    }
}

service "service-name" on new OptionalLiteralNameListener() {

}

service on new OptionalLiteralNameListener() {

}

service / on new OptionalLiteralNameListener() {

}

public class PathOnlyListener {
    public isolated function 'start() returns error? {
    }
    public isolated function gracefulStop() returns error? {
    }
    public isolated function immediateStop() returns error? {
    }
    public isolated function detach(service object {} s) returns error? {
    }
    public isolated function attach(service object {} s, string[] name) returns error? {
    }
    isolated function register(service object {} s, string[]|string? name) returns error? {
    }
}

service "service-name" on new PathOnlyListener() {

}

service on new PathOnlyListener() {

}

service / on new PathOnlyListener() {

}
