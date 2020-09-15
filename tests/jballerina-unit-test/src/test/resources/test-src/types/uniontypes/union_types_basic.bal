import ballerina/io;

function testUnionTypeBasics1() returns [int|float|string, int|string] {
    int|float|string aaa = 12330;
    int|string bbb = "string value";

    aaa = 12.0;
    bbb = "sameera";

    return [aaa, bbb];
}

function testUnionTypeBasics2() returns (int|float|string|boolean) {
    int|float|string|boolean ttt = getUnion("jayasoma");
    return ttt;
}


function getUnion(string|int|float si) returns (int|float|string) {
    return "union types";
}


function testNullableTypeBasics1() returns (int|json|string|float|map<any>|boolean|()) {
    int|string|float|json|map<any>|boolean|() k = 5;

    k = "sss";
    k = 1.0;

    json j = { name: "ddd" };
    k = j;

    map<any> m = { name: "dddd" };
    k = m;

    k = true;

    k = ();
    return k;

 }

 function testNullableTypeArrayFill() returns (int|json|string|float|map<any>|boolean|()) {
     (int|string|float|json|boolean)[] unionArr = [];
     unionArr[5] = true;

     (int|string|float|())[] unionArr2 = [];
     unionArr2[5] = 6;
 }


function testNullableTypeBasics2() returns (int|boolean|()) {

    int|float|() x = ();

    if x is float|int {
        io:println("int");
    } else {
        io:println("null");
    }

    int|boolean|() i = ();

    if i is int {
        io:println("int");
    } else if i is boolean {
        io:println("boolean");
    } else {
        io:println(i);
    }

    return i;

}

public type ParamAny record {
    any value;
};

public type GlobalParam string|int|boolean|float|ParamAny;

function testUnionTypeArrays() returns int {
    ParamAny para1 = { value: "Colombo" };
    ParamAny para2 = { value: 10 };
    ParamAny[] paramAnyArray = [para1, para2];
    GlobalParam?[] globalParamArray = paramAnyArray;
    return globalParamArray.length();
}


function testUnionTypeArrayWithValueTypeArrayAssignment() returns int {
    int[] intArray = [10, 20, 30];
    GlobalParam?[] globalParamArray = intArray;
    return globalParamArray.length();
}

public class Person {
    string name = "";
}

public type RecPerson record {
    string name;
    int id;
};

function testRecordLiteralAssignment() returns string {
    Person|RecPerson x = {name:"John", id:12};
    if x is Person {
        return "Invalid";
    } else {
        return <string> x.name;
    }
}

type Foo record {|
    string s;
    int i = 0;
|};

type Bar record {|
    string x;
    int y = 0;
|};

function testUnionTypeWithMultipleRecordTypes() returns string[] {

    string[] returnValues = [];

    Foo|Bar var1 = {s : "dummy string"};
    Foo|Bar var2 = {x : "dummy string"};

    if (var1 is Foo) {
        returnValues[0] = "FOO";
    } else {
        returnValues[0] = "BAR";
    }

    if (var2 is Foo) {
        returnValues[1] = "FOO";
    } else {
        returnValues[1] = "BAR";
    }

    return returnValues;
}

const ASSERTION_ERR_REASON = "AssertionError";

function testUnionTypeWithMultipleRecordTypesWithLiteralKeysInLiteral() {

    Foo|Bar v1 = {s: "dummy string", "i": 1};
    Foo|Bar v2 = {"x": "dummy string", y: 2};

    if !(v1 is Foo) {
        panic error(ASSERTION_ERR_REASON, message = "expected v1 to be of type Foo");
    }

    if !(v2 is Bar) {
        panic error(ASSERTION_ERR_REASON, message = "expected v2 to be of type Bar");
    }
}

function testUnionLhsWithDiscriminatedFloatDecimalLiterals() returns [(float|decimal), (float|decimal), (float|decimal)] {
    float|decimal a = 1.0;
    float|decimal b = 1.0f;
    float|decimal c = 1.0d;
    return [a, b, c];
}

type Employee object {
    public int age;
    public string firstName;
    public string lastName;

    function getFullName() returns string;
};

class Engineer {
    public int age;
    public string firstName;
    public string lastName;

    function init(int age, string firstName, string lastName) {
        self.age = age;
        self.firstName = firstName;
        self.lastName = lastName;
    }

    function getFullName() returns string {
        return self.firstName + self.lastName;
    }

}

function testUnionTypeWithFunctionPointerAccess() {
    Engineer engineer = new Engineer(20, "John", "Doe");
    Employee employee = new Engineer(20, "Jane", "Doe");

    Engineer|Employee person1 = engineer;
    Engineer|Employee person2 = employee;

    person1.age = 25;
    person2.age = 25;

    var setAge = function () {
        person1.age = 30;
        person2.age = 30;
    };
    setAge();
    assertEquality(30, person1.age);
    assertEquality(30, person2.age);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }
    if expected === actual {
        return;
    }
    panic error(ASSERTION_ERR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
