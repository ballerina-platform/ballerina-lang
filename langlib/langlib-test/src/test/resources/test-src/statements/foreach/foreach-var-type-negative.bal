public type Person record {
    string name;
    int age;
};

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

function test6() {
    Person person = {name: "Aryaa", age: 22};
    foreach (boolean|float|xml) bf in person {

    }
}

function test7() {
    string str = "foo";
    foreach int s in str {

    }
}

function testForeachIterationOverReadOnlyArrayMembersOfReadOnlyArrayNegative() {
    readonly & (int[2])[] x = [[1, 2], [3, 4], [5, 6]];

    foreach (int[])[2] [a, b] in x {
    }
}

function testForeachIterationOverReadOnlyArrayMembersOfNonReadOnlyArrayNegative() {
    (readonly & int[2])[] x = [[1, 2], [3, 4], [5, 6]];

    foreach readonly & byte[2] [a, b] in x {
    }
}

function testForeachIterationOverReadOnlyTupleMembersOfReadOnlyArrayNegative() {
    readonly & ([int, string, boolean])[] x = [[1, "a", true], [3, "bc", false], [5, "def", true]];

    foreach [string, int, boolean] [a, b, c] in x {
    }
}

function testForeachIterationOverReadOnlyTupleMembersOfReadOnlyTupleNegative() {
    readonly & [[int, int], [int, int]...] x = [[1, 2], [3, 4], [5, 6]];

    foreach [[int, int], [string, string]] [a, b] in x {
    }
}

function testForeachIterationOverReadOnlyArrayMembersOfReadOnlyTupleNegative() {
    readonly & [int[2], string[2], boolean[3]] x = [[1, 2], ["a", "b"], [false, false, true]];

    foreach [int|boolean, int|boolean, int|string...] [a, b, ...c] in x {
    }
}

function testForeachIterationOverReadOnlyRecordOfNonReadOnlyArrayNegative() {
    (readonly & record {| int a; int b; |}|readonly & record {| string[] a; boolean? b = (); |})[] x =
        [{a: 1, b: 2}, {a: ["hello", "world", "ballerina"]}];


    foreach record {| int a; boolean? b; |} {a, b} in x {
    }
}

function testForeachIterationOverReadOnlyTupleMembersOfNonReadOnlyRecord() {
    record {| readonly & [int, string] a; readonly & [boolean, int] b; |} x = {"a": [1, "foo"], "b": [false, 2]};

    foreach [int, int] & readonly [a, b] in x {
    }
}

function testForeachIterationOverReadOnlyTupleMembersOfReadOnlyMap() {
    readonly & map<[int, string, boolean...]|[boolean, int]> x = {a: [1, "foo"], b: [true, 2], c: [3, "bar", false]};

    foreach [int, string|int, int...] [a, b, ...c] in x {
    }
}
