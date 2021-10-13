// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.value as val;

xmlns "http://ballerina.io" as b7a;

const GR = "green";

listener Listener lsn = new;

int gFoo = 100;

type Error error<record {int code = 0;}>;

type Person record {|
    string name;
    int age = 0;
|};

enum Colour {
    RED, GREEN = GR, BLUE
}

public annotation v1 on var;

function foo(int a, int b = 10, int... c) {

}

class PersonClz {
    string name = "";
    int age;

    function getName() returns string {
        return self.name;
    }
}


service  / on lsn {

    string greet = "Hello World!";
    int x = 0;

    resource function get greeting() returns json => { output: self.greet };
}


// utils
public class Listener {

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }

    public function detach(service object {} s) returns error? {
    }

    public function attach(service object {} s, string[]? name = ()) returns error? {
    }
}
