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

import ballerina/lang.'object;

listener CustomListener listen = new CustomListener();

class CustomListener {
    *'object:Listener;

    public function __attach(service s, string? name) returns error? {
    }

    public function __detach(service s) returns error? {
    }

    public function __start() returns error? {
    }

    public function __gracefulStop() returns error? {
    }

    public function __immediateStop() returns error? {
    }
}

readonly class PersonObj {
    string fname;
    string lname;

    function init(string fname, string lname) {
        self.fname = fname;
        self.lname = lname;
    }

    function getName() returns string => self.fullName();

    private function fullName() returns string => self.fname + " " + self.lname;
}

isolated function add(int x, int y) returns int => x + y;

type Person record {|
    readonly string name;
    int age;
|};

client class TestEP {
    remote function action(int i) returns boolean {
        if (i > 5) {
            return true;
        }
        return false;
    }
}

service echo on listen {
    resource function print() {

    }
}

//type PersonType distinct object {
//    string name;
//
//    function getName() returns string;
//};

const PI = 3.14;
