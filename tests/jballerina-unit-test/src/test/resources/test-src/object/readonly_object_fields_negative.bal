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

type Student object {
    readonly string name;
    readonly int id;
    float avg = 80.0;
    function init(string n, int i) {
        self.name = n;
        self.id = i;
    }
};

function testInvalidUpdateOfObjectWithSimpleReadonlyFields() {
    Student st = new ("Maryam", 5);

    st.name = "Mary";
}

type Employee object {
    readonly Details details;
    string department;

    function init(Details & readonly details, string department) {
        self.details = details;
        self.department = department;
    }
};

type Details record {
    string name;
    int id;
};

function testObjectWithStructuredReadonlyFields() {
    Details details = {
        name: "Kim",
        id: 1000
    };

    Employee e = new (details, "finance");

    e.details = details;
    e.details.name = "Jo";
}

type Customer object {
    readonly string name;
    int id;

    function init(string n, int i) {
        self.name = n;
        self.id = i;
    }
};

function testInvalidUpdateOfReadonlyFieldInUnion() {
    Customer customer = new ("Jo", 1234);

    Student|Customer sd = customer;
    sd.name = "May";
}

type Foo abstract object {
    int[] arr;
    map<string> mp;

    function baz() returns string;
};

type Bar object {
    readonly int[] arr = [1, 2];
    readonly map<string> mp = {a: "abc"};
    int? oth = ();

    function baz() returns string {
        return "Bar";
    }
};

type Baz object {
    readonly int[] arr = [1, 2];
    map<string> mp = {a: "abc"};

    function baz() returns string {
        return "Baz";
    }
};

type Qux object {
    readonly & int[] arr = [1, 2];
    map<string> & readonly mp = {a: "abc"};

    function baz() returns string {
        return "Qux";
    }
};

function testInvalidImmutableTypeAssignmentForNotAllReadOnlyFields() {
    Bar bar = new;

    Foo & readonly f1 = bar;
    Foo & readonly f2 = new Baz();
    Foo & readonly f3 = new Qux();
}

type Person object {
    readonly Particulars particulars;
    int id;

    function init(Particulars & readonly particulars) {
        self.particulars = particulars;
        self.id = 1021;
    }
};

type Undergraduate object {
    Particulars & readonly particulars;
    int id = 1234;

    function init(Particulars & readonly particulars) {
        self.particulars = particulars;
    }
};

type Graduate object {
    Particulars particulars;
    int id;

    function init(Particulars particulars, int id) {
        self.particulars = particulars;
        self.id = id;
    }
};

type Particulars record {|
    string name;
|};

type AbstractPerson abstract object {
    Particulars particulars;
    int id;
};

function testSubTypingWithReadOnlyFieldsNegative() {
    Undergraduate u = new ({name: "Jo"});
    Person p1 = u;

    Graduate g = new ({name: "Amy"}, 1121);
    Person p2 = g;

    AbstractPerson ap = g;
    Person p3 = ap;
}
