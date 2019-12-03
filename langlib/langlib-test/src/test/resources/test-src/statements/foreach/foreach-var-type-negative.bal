public type Foo record {|
    float f;
    int d;
    string s;
    json j;
    anydata a;
|};

public type Bar record {|
    float f;
    int d;
    string s;
    json j;
    map<int> a;
|};


public type Student record {|
    string name;
    float weight;
    string school;
    int indexNo;
    boolean married;
    string...;
|};

function test1() {
    Foo foo = {f: 1.0, d: 23, s: "Shalala", j: true, a: 67};
    foreach json j in foo {

    }
}

function test2() {
    Student student = {name: "Aryaa", weight: 73, school: "MIT", indexNo: 3443, married: false, lastName: "Stark"};
    foreach boolean b in student {

    }
}

function test3() {
    Student student = {name: "Aryaa", weight: 73, school: "MIT", indexNo: 3443, married: false, lastName: "Stark"};
    foreach (boolean|float) bf in student {

    }
}

function test4() {
    Student student = {name: "Aryaa", weight: 73, school: "MIT", indexNo: 3443, married: false, lastName: "Stark"};
    foreach var v in student {
        xml x = v;
    }
}

function test5() {
    map<int> m = {"1" : 1, "2" : 2, "3" : 3};
    Bar bar = {f: 1.0, d: 23, s: "Shalala", j: true, a: m};
    foreach xml j in bar {

    }
}

