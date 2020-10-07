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

string aString = "foo";
int anInt = 10;

function test() {
    string greet = "Hello " + aString;

    var greetFn = function (string name) returns string => HELLO + " " + name;
    greet = greetFn("Pubudu");

    if (true) {
        int a = 20;

        while(true) {
            var x = 0;

        }

        int y = 10;
    }

    int z = 20;
}

function testTypeRefs() {
    Person p = {};
    PersonObj pObj = new("Jane", "Doe");
    string name = pObj.getFullName();
    name = p.name;
    name = <string>p["name"];
}

const HELLO = "Hello";

type Person record {|
    string name = "John Doe";
|};

class PersonObj {
    string fname;
    string lname;

    public function init(string fname, string lname) {
        self.fname = fname;
        self.lname = lname;
    }

    public function getFullName() returns string {
        return self.fname + " " + self.lname;
    }
}

enum Colour {
    RED, GREEN, BLUE
}

function testFunctionCall() {
    test();
}
