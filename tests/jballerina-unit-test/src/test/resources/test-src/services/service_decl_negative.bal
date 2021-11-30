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

type JustObj object {

};

type UnionType PathOnlyListener|JustObj;
listener UnionType ul = new PathOnlyListener();

type UnionWithError PathOnlyListener|error;
listener UnionWithError ue = new PathOnlyListener();

type UnionWithInt PathOnlyListener|int;
listener UnionWithInt ui = new PathOnlyListener();

service / on ui {

}

type ServType service object {
    function exec() returns any|error;
};

public class ServTypeListener {
    public isolated function 'start() returns error? {
    }
    public isolated function gracefulStop() returns error? {
    }
    public isolated function immediateStop() returns error? {
    }
    public isolated function detach(ServType s) returns error? {
    }
    public isolated function attach(ServType s, string[] name) returns error? {
    }
    isolated function register(service object {} s, string[]|string? name) returns error? {
    }
}

service / on new ServTypeListener() {

}

public class Listener {
    public isolated function 'start() returns error? {
    }
    public isolated function gracefulStop() returns error? {
    }
    public isolated function immediateStop() returns error? {
    }
    public isolated function detach(service object {} s) returns error? {
    }
    public isolated function attach(service object {} s, string[]|string name = "") returns error? {
    }
}

listener Listener x = new;

service on x {

}

service ServType / on new ServTypeListener() {

}

type DServ distinct service object {

};

public class DServListener {
    public isolated function 'start() returns error? {
    }
    public isolated function gracefulStop() returns error? {
    }
    public isolated function immediateStop() returns error? {
    }
    public isolated function detach(DServ s) returns error? {
    }
    public isolated function attach(DServ s, string[]|string name = "") returns error? {
    }
}

service DServ / on new DServListener() {

}

service / on new DServListener() {

}

public type Int int;

public isolated class ListenerWithNonNilInitReturnType {
    public function init() returns Int? { // invalid object constructor return type 'Int?', expected a subtype of 'error?' containing '()'
    }

    public function attach(service object {} s, string[]|string|() name = ()) returns error? {
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }

    public function detach(service object {} s) returns error? {
    }
}

service on new ListenerWithNonNilInitReturnType() { // error: incompatible types: expected 'listener', found '(ListenerWithNonNilInitReturnType|Int)'
}
