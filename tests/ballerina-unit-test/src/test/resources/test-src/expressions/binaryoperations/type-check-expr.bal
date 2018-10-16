// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

// ========================== Basics ==========================

function testValueTypeInUnion() returns string {
    int|string x = "hello";
    int y = 10;
    if (x is int) {
        return "int";
    } else {
        return "string";
    }
}

function testUnionTypeInUnion() returns string {
    int|string|float x = 5;
    if (x is int|float) {
        return "numeric";
    } else {
        return "string";
    }
}

function testNestedTypeCheck() returns (any, any, any) {
    return (bar(true), bar(1234), bar("hello"));
}

function bar (string | int | boolean i)  returns string {
    if (i is int){
        return "int";
    } else if (i is string | boolean) {
        if (i is string) {
            return "string";
        } else if (i is boolean) {
            return "boolean";
        }
    }

    return "n/a";
}

function testTypeInAny() returns (string) {
    any a = "This is working";
    if (a is string) {
        return "string value: " + <string> a;
    } else if(a is int) {
        return "int";
    } else {
        return "any";
    }
}

function testNilType() returns (string) {
    any a = ();
    if (a is string) {
        return "string";
    } else if(a is int) {
        return "int";
    } else if(a is ()) {
        return "nil";
    }else {
        return "any";
    }
}

// ========================== Records ==========================

type A1 record {
    int x;
};

type B1 record {
    int x;
    string y;
};

function testSimpleRecordTypes_1() returns string {
    A1 a = {};
     if (a is A1) {
        return "a is A1";
    } else if (a is B1) {
        return "a is B1";
    }

    return "n/a";
}

function testSimpleRecordTypes_2() returns string {
    B1 b = {};
    A1 a = b;

    if (a is A1) {
        return "a is A1";
    } else if (a is B1) {
        return "a is B1";
    }

    return "n/a";
}

type A2 record {
    int x;
};

type B2 record {
    int x;
};

function testSimpleRecordTypes_3() returns string {
    B2 b = {};
     if (b is A2) {
        return "b is A2";
    } else if (b is B2) {
        return "b is B2";
    }

    return "n/a";
}

type Human record {
    string name;
    function (int, string) returns string | () foo;
};

type Man record {
    string name;
    function (int, string) returns string | () foo;
    int age;
};

function testRecordsWithFunctionType() returns string {
    Human m = {name:"Piyal"};
    if (m is Man) {
        return "Man: " + m.name;
    } else if (m is Human) {
        return "Human: " + m.name;
    }
    return "n/a";
}

type X record {
    int p;
    string q;
    A1 r;
};

type Y record {
    int p;
    string q;
    B1 r;   // Assignable to A1. Hence Y is assignable to X.
};

function testNestedRecordTypes() returns string {
    Y y = {};
    X x = y;
    if (x is X) {
        return "x is X";
    } else if (x is Y) {
        return "x is Y";
    }

    return "n/a";
}

type A3 record {
    int x;
};

type B3 record {
    int x;
    !...
};

function testSealedRecordTypes() returns string {
    A3 a = {};
     if (a is B3) {
        return "a is B3";
    } else if (a is A3) {
        return "a is A3";
    }

    return "n/a";
}

// ========================== Objects ==========================

public type Person object {
    public int age;
    public string name;
    public string address;

    public new (name, age) {}

    public function getName() returns (string) {
        return name;
    }

    public function getAge() returns (int) {
        return age;
    }

    public function getAddress() returns (string) {
        return address;
    }
};

public type SameAsPerson object {
    public int age;
    public string name;
    public string address;

    public new (name, age) {}

    public function getName() returns (string) {
        return name;
    }

    public function getAge() returns (int) {
        return age;
    }

    public function getAddress() returns (string) {
        return address;
    }
};

function testObjectWithSameMembersButDifferentAlias() returns (string, string) {
    Person p1 = new("John", 35);
    SameAsPerson p2 = p1;

    SameAsPerson p3 = new ("Doe", 45);
    Person p4 = p3;

    string s1;
    string s2;

    if(p2 is SameAsPerson) {    // this should not be true
        s1 = "I am same as person";
    } else if (p2 is Person) {  // this should be true
        s1 = "I am a person";
    } else {
        s1 = "I am no one";
    }

    if (p4 is Person) {
        s2 = "I am a person";
    } else if(p4 is SameAsPerson) {
        s2 = "I am same as person";
    } else {
        s2 = "I am no one";
    }

    return (s1, s2);
}

public type PersonInOrder object {
    public int age;
    public string name;
    public string address;

    public new (name, age) {}

    public function getName() returns (string) {
        return name;
    }

    public function getAge() returns (int) {
        return age;
    }

    public function getAddress() returns (string) {
        return address;
    }
};

public type PersonNotInOrder object {

    public function getName() returns (string) {
        return name;
    }

    public int age;

    public function getAge() returns (int) {
        return age;
    }

    public new (name, age) {}

    public string name;

    public function getAddress() returns (string) {
        return address;
    }

    public string address;
};

function testObjectWithUnorderedFields() returns (string, string) {
    PersonInOrder p1 = new("John", 35);
    PersonNotInOrder p2 = p1;

    PersonNotInOrder p3 = new ("Doe", 45);
    PersonInOrder p4 = p3;

    string s1;
    string s2;

    if (p2 is PersonInOrder) {
        s1 = "I am a person in order";
    } else if(p2 is PersonNotInOrder) {
        s1 = "I am a person not in order";
    } else {
        s1 = "I am no one";
    }

    if (p4 is PersonInOrder) {
        s2 = "I am a person in order";
    } else if(p4 is PersonNotInOrder) {
        s2 = "I am a person not in order";
    } else {
        s2 = "I am no one";
    }

    return (s1, s2);
}

// ========================== Arrays ==========================

function testSimpleArrays() returns (boolean, boolean, boolean, boolean, boolean) {
    int[] a = [1, 2, 3];
    int[][] b = [[1, 2, 3], [4, 5, 6]];
    any c = a;
    any d = b;
    return ((c is int[] && d is int[][]), c is float[], d is json, d is json[], d is json[][]);
}

function testRecordArrays_1() returns (boolean, boolean) {
    A2[] a = [{}, {}];
    A2[][] b = [[{}, {}], [{}, {}]];
    return (a is B2[], b is B2[][]);
}

function testRecordArrays_2() returns (boolean, boolean, boolean, boolean) {
    X[] a = [{}, {}];
    X[][] b = [[{}, {}], [{}, {}]];
    any c = a;
    any d = b;
    return (c is X[], d is X[][], c is Y[], d is Y[][]);
}

// ========================== Tuples ==========================
function testSimpleTuples() returns (boolean, boolean, boolean, boolean, boolean) {
    (int, string) x = (4, "hello");
    any y = x;

    boolean b0 = y is (int, string);
    boolean b1 = y is (int, boolean);
    boolean b2 = y is (float, boolean);
    boolean b3 = y is (any, any);
    boolean b4 = y is (json, json);

    return (b0, b1, b2, b3, b4);
}

function testTupleWithAssignableTypes_1() returns (boolean, boolean) {
    (X, Y) a = ({}, {});
    boolean b0 = a is (X, Y);
    boolean b1 = a is (Y, Y);
    return (b0, b1);
}

function testTupleWithAssignableTypes_2() returns (boolean, boolean, boolean, boolean) {
    (X, Y) p = ({}, {});
    any q = p;
    boolean b0 = q is (X, X);
    boolean b1 = q is (X, Y);
    boolean b2 = q is (Y, X);
    boolean b3 = q is (Y, Y);
    return (b0, b1, b2, b3);
}

function testTupleWithAssignableTypes_3() returns (boolean, boolean, boolean, boolean) {
    Y y = {};
    X x = y;
    (any, Y) p = (x, {});
    any q = p;
    boolean b0 = q is (X, X);
    boolean b1 = q is (X, Y);
    boolean b2 = q is (Y, X);
    boolean b3 = q is (Y, Y);
    return (b0, b1, b2, b3);
}

// ========================== Map ==========================

function testSimpleUnconstrainedMap_1() returns (boolean, boolean, boolean, boolean) {
    map m = {"key1": "value1"};
    boolean b0 = m is map;
    boolean b1 = m is map<any>;
    boolean b2 = m is map<string>;
    boolean b3 = m is map<json>;
    return (b0, b1, b2, b3);
}

function testSimpleUnconstrainedMap_2() returns (boolean, boolean, boolean, boolean, boolean) {
    map m = {"key1": "value1"};
    any a = m;
    boolean b0 = a is map;
    boolean b1 = a is map<any>;
    boolean b2 = a is map<string>;
    boolean b3 = a is json;
    boolean b4 = a is map<json>;
    return (b0, b1, b2, b3, b4);
}

//function testSimpleConstrainedMap_1() returns (boolean, boolean, boolean, boolean, boolean) {
//    map<string> m = {"key1": "value1"};
//    boolean b0 = m is map;
//    boolean b1 = m is map<any>;
//    boolean b2 = m is map<string>;
//    boolean b3 = m is json;
//    boolean b4 = m is map<json>;
//    return (b0, b1, b2, b3, b4);
//}

function testSimpleConstrainedMap_2() returns (boolean, boolean, boolean, boolean) {
    map<string> m1 = {"key1": "value1"};
    map m2 = m1;
    boolean b0 = m2 is map;
    boolean b1 = m2 is map<any>;
    boolean b2 = m2 is map<string>;
    boolean b3 = m2 is map<json>;
    return (b0, b1, b2, b3);
}

// ========================== JSON ==========================

function testJSONTypeCheck() returns (string, string, string, string, string, string, string) {
    json j1 = 3;
    json j2 = 4.5;
    json j3 = "hello";
    json j4 = true;
    json j5 = [78, true, {"name": "john"}];
    json j6 = {"name": "john"};
    json j7 = null;
    return (checkJSON(j1), checkJSON(j2), checkJSON(j3), checkJSON(j4), checkJSON(j5), checkJSON(j6), checkJSON(j7));
}

function checkJSON(json j) returns string {
    if (j is string) {
        return "json string: " + check <string> j;
    } else if(j is int) {
        return "json int";
    } else if(j is float) {
        return "json float";
    } else if(j is boolean) {
        return "json boolean";
    } else if(j is ()) {
        return "json null";
    } else if(j is json[]) {
        return "json array";
    } else if(j is json) {
        return "json object";
    } else {
        return "not a json";
    }
}

// ========================== Function Types ==========================
// TODO:
