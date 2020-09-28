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

type Person record {
    PersonalDetails details;
    Person employer?;
    int id;
};

type PersonalDetails record {
    string name;
    int yob;
};

type Student record {|
    PersonalDetails details;
|};

function testReadOnlyPropagationForNestedTypes() {
    readonly & Person p = {
        details: {
            name: "Jo",
            yob: 1990
        },
        id: 1234
    };

    Student w = p;
    int|string x = p.details;
    Student y = p?.employer;
    string z = p["id"];
}

function testNonReadOnlyValueForReadOnlyCET() {
    PersonalDetails d = {
        name: "May",
        yob: 1992
    };

    Student st = {
        details: d
    };

    Student & readonly rst = st;

    Person & readonly p = {
        details: d,
        id: 1234
    };
}

type AB "A"|"B";

class Obj {
    int i;

    function init(int i) {
        self.i = i;
    }
}

type ABAny AB|any;

function testInvalidAssignmentToWideReadOnlyIntersection() {
    ABAny & readonly w = new Obj(1);

    string[] arr = ["foo"];
    anydata & readonly x = arr;

    any & readonly y = start testInvalidReaoOnlyRecordInit();

    (Obj|int[]) & readonly z = arr;
}

type Employee record {|
    readonly PersonalDetails details;
    Department & readonly dept;
|};

type Department record {|
    string name;
|};

function testInvalidReaoOnlyRecordInit() {
    PersonalDetails details = {
        name: "Anne",
        yob: 1995
    };

    record {
        Department dept;
    } rec = {
        dept: {
            name: "finance"
        }
    };

    Employee e = {
        details: details,
        ...rec
    };
}

function testInvalidReaoOnlyRecordFieldUpdates() {
    PersonalDetails & readonly details = {
        name: "Anne",
        yob: 1995
    };

    record {
        Department dept;
    } & readonly rec = {
        dept: {
            name: "finance"
        }
    };

    Employee e = {
        details: details,
        ...rec
    };

    e.details = {
        name: "May",
        yob: 1992
    };
    e["details"]["year"] = 1990;

    Department dept2 = {
        name: "IT"
    };
    e.dept = dept2;
    e["dept"] = dept2;
}

class Foo {

}

type Bar record {|
    readonly string name;
    Foo id;
|};

function testInvalidNeverReadOnlyConstraint() {
    table<Bar> key(name) & readonly tb = table [
        {name: "Jo", id: new}
    ];
}

type Baz object {
    future<()> ft;

    function getFt();
};

function testNeverReadOnlyObject() {
    Baz & readonly bz;
}

type Config object {
    string name;

    function getName() returns string;
};

class MyConfig {
    final string name;

    public function init(string name) {
        self.name = name;
    }

    function getName() returns string {
        return self.name;
    }
}

function testInvalidObjectUpdate() {
    Config & readonly config = new MyConfig("client config");
    config.name = "new name";

    MyConfig myConfig = new MyConfig("client config");
    myConfig.name = "new name";
}

type ABC record {|
    DEF & readonly d;
|};

type DEF record {|
    future<int> fr;
|};

class GHI {
    JKL & readonly j;

    function init(JKL & readonly j) {
        self.j = j;
    }
}

type JKL object {
    future<int> fr;
};

function testInvalidMemberWithConstructorReadOnlyCast() {
    int[] w = [1, 2];
    int[] x = [];

    record {|int[]...;|} y = {
        "c": [56, 78, 4456]
    };

    map<int[]> z = <readonly> {
        a: [22, 12123],
        b: w,
        x,
        ...y
    };
}

function testInvalidValueForEffectiveReadOnlyCast() {
    future<int> f = start getInt();
    NeverImmutable|map<int> w = <readonly> {x: f};

    float[] a = [11.2, 12.122];
    stream<float> str = a.toStream();
    string[]|stream<float>[] x = <readonly> [str, a.toStream()];

    NeverImmutable ni = <readonly> {x: f};
}

type NeverImmutable record {
    future<int> x;
};

function getInt() returns int => 1;

readonly class ReadOnlyObj {
    int j = 3;
}

function testInvalidUpdateOfAnonTypeField() {
    readonly & record {int i;} x = {i: 1};
    x.i = 2; // error

    readonly & object {int j;} y = new ReadOnlyObj();
    y.j = 4; // error
}
