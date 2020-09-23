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

class Student {
    final string name;
    final int id;
    float avg = 80.0;
    function init(string n, int i) {
        self.name = n;
        self.id = i;
    }
}

class Employee {
    final Details details;
    string department;

    function init(Details & readonly details, string department) {
        self.details = details;
        self.department = department;
    }
}

type Details record {
    string name;
    int id;
};

function testObjectWithStructuredFinalFields() {
    Details details = {
        name: "Kim",
        id: 1000
    };

    Employee e = new (details, "finance");
}

type Foo object {
    int[] arr;
    map<string> mp;

    function baz() returns string;
};

class Bar {
    final int[] arr = [1, 2];
    final map<string> mp = {a: "abc"};
    int? oth = ();

    function baz() returns string {
        return "Bar";
    }
}

class Baz {
    final int[] arr = [1, 2];
    map<string> mp = {a: "abc"};

    function baz() returns string {
        return "Baz";
    }
}

class Qux {
    readonly & int[] arr = [1, 2];
    map<string> & readonly mp = {a: "abc"};

    function baz() returns string {
        return "Qux";
    }
}

function testInvalidImmutableTypeAssignmentForNotAllFinalFields() {
    Bar bar = new;

    Foo & readonly f1 = bar;
    Foo & readonly f2 = new Baz();
    Foo & readonly f3 = new Qux();
}

class Quux {
    final float i;
    string s;
    boolean b;
}

function testFinalModifierInStringRepresentation() {
    var b = object {
        final int i = 2;
        string s = "world";
        final boolean b = false;
    };

    Quux y = b;
}

class Controller {
    final string id;
    final map<int> config;

    function init(string id, map<int> config) {
        self.id = id;
        self.config = config;
    }
}

function testMutabilityOfFinalFieldOnlyObjectWithMutableTypes() {
    Controller cr = new ("default", {count: 100});
    readonly rd = cr;

    var ob = object {
        final string id;
        final map<int> config;

        function init() {
            self.id = "inline";
            self.config = {};
        }
    };
    readonly rd2 = ob;
}
