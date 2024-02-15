import ballerina/lang.'error as lang;

string output = "";

json jdata = {
    name: "bob",
    age: 10,
    pass: true,
    subjects: [
        { subject: "maths", marks: 75 },
        { subject: "English", marks: 85 }
    ]
};

type Detail record {
    *lang:Detail;
    boolean fatal;
};

type Error error<Detail>;

type Employee record {
    int id;
    string name;
    float salary;
};

//table<Employee> data = table {
//    { key id, name, salary },
//    [
//        { 1, "Mary",  300.5 },
//        { 2, "John",  200.5 },
//        { 3, "Jim", 330.5 }
//    ]
//};

function test1(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    foreach var [i, s, f] in data {
        println(i + " " + s + " " + f);
    }
}

function test2(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    float i = 10.0;
    boolean s = true;
    foreach var [i, s] in data {
        println(i + " " + s);
    }
}

function test3(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    foreach var [i, j] in data {
        println(i + " ");
    }
    println(i);
}

function test4(){
    int vals = 1000;
    foreach var s in vals {
        string s1 = s + s;
        println(s1);
    }
}

type person record {
    int id = 0;
};

function test5(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    person p = {};
    foreach var [i, s] in data {
        string s1 = s + s;
        println(s1);
    }
}

function test6(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    foreach var [i, j, k] in data {
        println("hello");
    }
}

function test8() returns error? {
    json j = ["a" , "b", "c"];
    var jsonArray = <json[]> j;

    foreach var [x, y] in jsonArray {
        print(x);
        println(y);
    }

    return ();
}

function test9(){
    string[] slist = ["a" , "b", "c"];
    foreach var v in slist {
        println(v);
        break;
        println(v);
    }
    foreach var y in slist {
        println(y);
        continue;
        println(y);
    }
    continue;
    println("done");
}

function test10(){
    string[] data = ["mon", "tue", "wed", "thu", "fri"];
    foreach var [i, {j, k: {l, m}}] in data {
        println(i + " " + j + " " + l + " " + m);
    }
}

function test11() {
    [[string, string], [string, string], [string, string]] sTuple = [["ddd", "d1"], ["rrr", "d1"], ["fef", "d1"]];
    output = "";
    int i = 0;
    foreach var [v, u] in sTuple {
        v = "GG";
        if (i == 1) {
            output = output + "continue ";
            i += 1;
            continue;
        }
        concatString(i, v);
        i += 1;
    }
}

function test12() {
    Error err1 = Error("Error One", message = "msgOne", fatal = true);
    Error err2 = Error("Error Two", message = "msgTwo", fatal = false);
    Error err3 = Error("Error Three", message = "msgThree", fatal = true);
    Error[3] errorArray = [err1, err2, err3];

    string result1 = "";
    foreach var error(reason, message = message, fatal = fatal) in errorArray {
        reason = "updated reason";
        fatal = false;
        message = "msgNew";
    }
}

//function test13() {
//    output = "";
//    int i = 0;
//    foreach var {id, name, salary} in data {
//        id = 2;
//        name = "John";
//        salary = 250.5;
//    }
//}

function test14() returns string {
    output = "";
    json subjects = <json>jdata.subjects;

    int i = 0;
    if subjects is json[] {
        foreach var v in subjects {
            v = {};
        }
    }
    return output;
}

function test15() returns string {
    output = "";

    Employee d = { id: 1, name: "AbuTharek", salary: 100.0 };

    int i = 0;
    foreach any v in d {
        if (v is string) {
            v = "Kanaka";
        }
    }
    return output;
}

function test17() {
    output = "";
    int i = 0;
    Employee d1 = { id: 1, name: "Abu", salary: 1000.0, "married": false };
    Employee d2 = { id: 2, name: "Tharek", salary: 1000.0, "married": false };
    Employee d3 = { id: 3, name: "Kanaka", salary: 1000.0, "married": false };
    Employee[] data = [d1, d2, d3];

    foreach var {id, name, salary, ...status} in data {
        status = {};
    }
}

function test18() {
    map<anydata> keyValsMap = {foo:"sss", bar:"ffff"};
    foreach var {k} in keyValsMap {

    }
}

function test19() {
    map<any> keyValsMap = {foo:"sss", bar:"ffff"};
    foreach var {k} in keyValsMap {

    }
}

public function main () {
    println("done");
}

function println(any... v) {
     output = v[0].toString();
}

function print(any... v) {
    output = v[0].toString();
}

function concatString(int index, string value) {
    output = output + index.toString() + ":" + value + " ";
}

function testForeachIterationOverReadOnlyArrayMembersOfReadOnlyArrayWithVarNegative() {
    readonly & (int[2])[] x = [[1, 2], [3, 4], [5, 6]];

    foreach var [a, b] in x {
        string c = a;
        int[] d = b;
    }
}

function testForeachIterationOverReadOnlyArrayMembersOfNonReadOnlyArrayWithVarNegative() {
    (readonly & int[2])[] x = [[1, 2], [3, 4], [5, 6]];

    string[] arr = [];

    foreach var [a, b] in x {
        arr.push(a, b);
    }
}

function testForeachIterationOverReadOnlyTupleMembersOfReadOnlyArrayWithVarNegative() {
    readonly & ([int, string, boolean])[] x = [[1, "a", true], [3, "bc", false], [5, "def", true]];

    foreach var [a, b, c] in x {
        string d = a;
        boolean e = b;
        boolean f = c; // OK
    }
}

function testForeachIterationOverReadOnlyTupleMembersOfReadOnlyTupleWithVarNegative() {
    readonly & [[int, int], [int, int]...] x = [[1, 2], [3, 4], [5, 6]];

    foreach var [a, b] in x {
        int[] c = a;
        int d = b; // OK
    }
}

function testForeachIterationOverReadOnlyArrayMembersOfReadOnlyTupleWithVarNegative() {
    readonly & [int[2], string[2], boolean[3]] x = [[1, 2], ["a", "b"], [false, false, true]];

    (int|string|boolean)[] arr = [];

    foreach var [a, b, ...c] in x {
        int|string d = a;
        string|boolean e = b;
        string f = c[0];
    }
}

function testForeachIterationOverReadOnlyMapOfReadOnlyTupleWithVarNegative() {
    readonly & [map<int>, map<string>...] x = [{a: 1, b: 2}, {a: "hello", c: "world", d: "ballerina"}];

    foreach var {...a} in x {
        map<int> b = a;
    }
}

function testForeachIterationOverReadOnlyTupleMembersOfNonReadOnlyRecordWithVarNegative() {
    record {| readonly & [int, string] a; readonly & [boolean, int] b; |} x = {"a": [1, "foo"], "b": [false, 2]};

    foreach var [a, b] in x {
        int c = a;
        int d = b;
    }
}

function testForeachIterationOverReadOnlyArrayMembersOfReadOnlyMapWithVarNegative() {
    readonly & map<int[2]|string[3]|boolean[2]> x = {m: [1, 2], n: ["foo", "bar", "baz"], o: [true, false]};

    (int|boolean)[] arr = [];

    foreach var [a, b, ...c] in x {
        arr.push(a, b);
        int|string f = c[0];
    }
}

type Foo record {|
    int id;
|};

type Bar Foo;

type Foo2 readonly & record {|
    int id;
|};

type Bar2 Foo2;

function testInvalidTypeInVariableBinding() {
    Bar[] arr = [];
    foreach int item in arr {

    }

    Bar2[] arr2 = [];
    foreach int item in arr2 {

    }
}

function testInvalidTupleVarRefWithMismatchingTypesInForeach() {
    [[int, int]] a = [[10, 20]];
    foreach var [a1, a2, ...a3] in a {
    }

    [[[int, string], int[]]] b = [[[10, "a"], [20, 30, 40, 50]]];
    foreach var [b1, b2, ...b3] in b {
    }

    [[[int, string], int[]]] c = [[[10, "a"], [20, 30, 40, 50]]];
    foreach var [[c1, c2, ...c3], c4] in c {
    }
}

type SampleErrorData record {|
    int code;
    string reason;
|};

type SampleError error<SampleErrorData>;

type SampleComplexErrorData record {|
    int code;
    int[2] pos;
    record {string moreInfo;} infoDetails;
|};

type SampleComplexError error<SampleComplexErrorData>;

function testOnFailWithMultipleErrors() {
    boolean isPositiveState = false;
    int[] nestedData = [1];

    foreach var i in nestedData {
        if isPositiveState {
            fail error SampleComplexError("Transaction Failure", code = 20, pos = [30, 45], infoDetails = {moreInfo: "deadlock condition"});
        }
        fail error SampleError("Transaction Failure", code = 50, reason = "deadlock condition");
    } on fail var error(msg) {
    }
}
