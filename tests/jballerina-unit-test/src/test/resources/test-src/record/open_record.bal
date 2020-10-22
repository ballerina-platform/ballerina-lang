// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Department record {
    string dptName = "";
    Person[] employees = [];
};

type Person record {|
    string name = "default first name";
    string lname = "";
    map<any> adrs = {};
    int age = 999;
    Family family = {};
    Person? parent = ();
    anydata|error...;
|};

type Family record {
    string spouse = "";
    int noOfChildren = 0;
    string[] children = [];
};

type Employee record {
    string name = "default first name";
    string lname = "";
    map<any> address = {};
    int age = 999;
    Family family = {};
    Person? parent = ();
    string designation = "";
};

function testStructOfStruct () returns string {

    map<any> address = {"country":"USA", "state":"CA"};
    Person emp1 = {name:"Jack", adrs:address, age:25};
    Person emp2 = {};
    Person[] emps = [emp1, emp2];
    Department dpt = {employees:emps};

    string country;
    country = <string> dpt.employees[0].adrs["country"];
    return country;
}

function testReturnStructAttributes () returns string {
    map<any> address = {"country":"USA", "state":"CA"};
    string[] chldrn = [];
    Family fmly = {children:chldrn};
    Person emp1 = {name:"Jack", adrs:address, age:25, family:fmly};
    Person emp2 = {};
    Person[] employees = [emp1, emp2];
    Department dpt = {employees:employees};

    dpt.employees[0].family.children[0] = "emily";

    return dpt.employees[0].family.children[0];
}

function testExpressionAsIndex () returns string {
    Family family = {spouse:"Kate"};
    int a = 2;
    int b = 5;
    family.children = ["Emma", "Rose", "Jane"];
    return family.children[a * b - 8];
}

function testStructExpressionAsIndex () returns string {
    string country;
    Department dpt = {};
    Family fmly = {};
    fmly.children = [];
    Person emp2 = {};
    map<any> address = {"country":"USA", "state":"CA"};
    Person emp1 = {name:"Jack", adrs:address, age:25, family:fmly};

    emp1.adrs["street"] = "20";
    emp1.age = 0;

    dpt.employees = [emp1, emp2];
    dpt.employees[0].family.children[0] = "emily";
    dpt.employees[0].family.noOfChildren = 1;

    return dpt.employees[0].family.children[dpt.employees[0].family.noOfChildren - 1];
}

function testDefaultVal () returns [string, string, int] {
    Person p = {};
    return [p.name, p.lname, p.age];
}

function testNestedFieldDefaultVal () returns [string, string, int] {
    Department dpt = {};
    dpt.employees = [];
    dpt.employees[0] = {lname:"Smith"};
    return [dpt.employees[0].name, dpt.employees[0].lname, dpt.employees[0].age];
}

function testNestedStructInit () returns Person {
    Person p1 = {name:"aaa", age:25, parent:{name:"bbb", age:50}};
    return p1;
}

type NegativeValTest record {
    int negativeInt = -9;
    int negativeSpaceInt = -8;
    float negativeFloat = -88.234;
    float negativeSpaceFloat = -24.99;
};

function getStructNegativeValues () returns [int, int, float, float] {
    NegativeValTest tmp = {};
    return [tmp.negativeInt, tmp.negativeSpaceInt, tmp.negativeFloat, tmp.negativeSpaceFloat];
}

function getStruct () returns (Person) {
    Person p1 = {name:"aaa", age:25, parent:{name:"bbb", lname:"ccc", age:50}};
    return p1;
}

function testGetNonInitAttribute () returns string {
    Person emp1 = {};
    Person emp2 = {};
    Person[] emps = [emp1, emp2];
    Department dpt = {dptName:"HR", employees:emps};
    return dpt.employees[0].family.children[0];
}

function testGetNonInitArrayAttribute () returns string {
    Department dpt = {dptName:"HR"};
    return dpt.employees[0].family.children[0];
}

function testGetNonInitLastAttribute () returns Person {
    Department dpt = {};
    return dpt.employees[0];
}

function testSetFieldOfNonInitChildStruct () {
    Person person = {name:"Jack"};
    person.family.spouse = "Jane";
}

function testSetFieldOfNonInitStruct () {
    Department dpt = {};
    dpt.dptName = "HR";
}

function testAdditionOfARestField() returns Person {
    Person p = { name: "Foo", "mname": "Bar", age: 25, "height": 5.9};
    p["firstName"] = "John";
    return p;
}

function testAnydataOrErrorRestFieldRHSIndexAccess() returns anydata|error {
    Person p = {};
    anydata|error name = p["firstName"];
    return name;
}

type Person2 record {|
    string name = "";
    int age = 0;
    string...;
|};

function testStringRestField() returns Person2 {
    Person2 p = { name: "Foo", age: 25, "lname": "Bar", "address": "Colombo"};
    return p;
}

function testStringRestFieldRHSIndexAccess() returns [string?, string?] {
    Person2 p = {};
    string? name = p["name"];
    string? firstName = p["firstName"];
    return [name, firstName];
}

type Person3 record {|
    string name = "";
    int age = 0;
    int...;
|};

function testIntRestField() returns Person3 {
    Person3 p = { name: "Foo", age: 25, "year": 3 };
    return p;
}

function testIntRestFieldRHSIndexAccess() returns [int?, int?] {
    Person3 p = {};
    int? age = p["age"];
    int? birthYear = p["birthYear"];
    return [age, birthYear];
}

type Person4 record {|
    string name = "";
    int age = 0;
    float...;
|};

function testFloatRestField() returns Person4 {
    Person4 p = { name: "Foo", age: 25, "height": 5.9};
    return p;
}

function testFloatRestFieldRHSIndexAccess() returns [float?, float?] {
    Person4 p = { "weight": 61.5 };
    float? height = p["height"];
    return [p["weight"], height];
}

type Person5 record {|
    string name = "";
    int age = 0;
    boolean...;
|};

function testBooleanRestField() returns Person5 {
    Person5 p = { name: "Foo", age: 25, "isEmployed": true};
    return p;
}

function testBooleanRestFieldRHSIndexAccess() returns [boolean?, boolean?] {
    Person5 p = { "isStudent": true };
    boolean? isEmployed = p["isEmployed"];
    return [p["isStudent"], isEmployed];
}

type Person6 record {|
    string name = "";
    int age = 0;
    map<any>...;
|};

function testMapRestField() returns Person6 {
    Person6 p = { name:"Foo", age: 25, "misc": { lname: "Bar", height: 5.9, isEmployed: true } };
    return p;
}

function testMapRestFieldRHSIndexAccess() returns [map<any>?, map<any>?] {
    map<any> misc = {};
    Person6 p = { "misc": misc };
    map<any>? invMap = p["invMap"];
    return [p["misc"], invMap];
}

type Person7 record {|
    string name = "";
    int age = 0;
    (float|string|boolean)...;
|};

function testUnionRestField() returns Person7 {
    Person7 p = { name: "Foo", age: 25, "lname": "Bar", "height": 5.9, "isEmployed": true };
    return p;
}

function testUnionRestFieldRHSIndexAccess() returns [(float|string|boolean)?, (float|string|boolean)?] {
    Person7 p = { "miscField": "Foo" };
    float|string|boolean|() invField = p["invField"];
    return [p["miscField"], invField];
}

type Person8 record {|
    string name = "";
    int age = 0;
    ()...;
|};

function testNilRestField() returns Person8 {
    Person8 p = { name: "Foo", age: 25, "lname": () };
    return p;
}

type Person9 record {|
    string name = "";
    int age = 0;
    Department...;
|};

function testRecordRestField() returns Person9 {
    Person9 p = { name: "Foo", age: 25, "dpt": { dptName: "Engineering", employees: [] } };
    return p;
}

function testRecordRestFieldRHSIndexAccess() returns [Department?, Department?] {
    Person9 p = { "dept": {} };
    Department? dept = p["department"];
    return [p["dept"], dept];
}

class Animal {
    public string kind = "";
    public string name = "";

    function init(string name, string kind) {
        self.name = name;
        self.kind = kind;
    }
}

type Person10 record {|
    string name = "";
    int age = 0;
    Animal...;
|};

function testObjectRestField() returns Person10 {
    Person10 p = { name: "Foo", age: 25, "pet": new Animal("Miaw", "Cat") };
    return p;
}

function testObjectRestFieldRHSIndexAccess() returns [Animal?, Animal?] {
    Animal anim = new("Rocky", "Dog");
    Person10 p = { "pet": anim };
    Animal? pet = p["invPet"];
    return [p["pet"], pet];
}

type Person11 record {|
    string name = "";
    int age = 0;
    [float, string, Animal]...;
|};

function testTupleRestField() returns Person11 {
    Person11 p = { name: "Foo", age: 25, "misc": [5.9, "Bar", new Animal("Miaw", "Cat")] };
    return p;
}

function testTupleRestFieldRHSIndexAccess() returns [[float, string, Animal]?, [float, string, Animal]?] {
    Person11 p = { "tup": [4.5, "foo", new Animal("Miaw", "Cat")] };
    [float, string, Animal]? tupType = p["invTup"];
    return [p["tup"], tupType];
}

type Person12 record {|
    string name = "";
    int age = 0;
    any...;
|};

function testAnyRestField() returns Person12 {
    Animal?[] pets = [new Animal("Miaw", "Cat"), new Animal("Woof", "Dog")];
    Person12 p = { name: "Foo", age: 25, "pets": pets};
    return p;
}

function testAnyRestFieldRHSIndexAccess() returns [any, any] {
    Animal?[] pets = [new Animal("Miaw", "Cat"), new Animal("Woof", "Dog")];
    Person12 p = { "pets": pets };
    any a = p["anyField"];
    return [p["pets"], a];
}

type PersonA record {
    string fname = "";
    string lname = "";
    function() returns string fullName?;
};

function testFuncPtrAsRecordField() returns string {
    PersonA p = {fname:"John", lname:"Doe"};
    p.fullName = function () returns string {
        return p.lname + ", " + p.fname;
    };

    return p.fullName();
}

type Address record {
    string city;
    string country;
};

type PersonRec record {
    string name = "John Doe";
    int age = 25;
    Address adr = {city: "Colombo", country: "Sri Lanka"};
};

type EmployeeRec record {
    *PersonRec;
    string company = "WSO2";
};

type ManagerRec record {
    string dept = "";
    *EmployeeRec;
};

function testDefaultValueInit() returns ManagerRec {
    ManagerRec mgr = {};
    return mgr;
}

public type Foo record {
    int a = getAsInt("777");
};

isolated function getAsInt(string s) returns int {
    return 777;
}

public type Bar record {
    *Foo;
    int b = 56;
};

public function testRecordInitWithFuncCalls() returns Bar {
    Bar b = {};
    return b;
}

type Qux record {
    string bar;
    int baz;
};

function testLiteralsAsMappingConstructorKeys() returns boolean {
    Qux f = { "bar": "hello", baz: 1 };
    return f.bar == "hello" && f.baz == 1;
}

type Baz record {
    string s;
    int i?;
    float? f = ();
};

string iValue = "i";

function testExpressionAsKeys() returns boolean {
    Baz b = { s: "hello", [iValue]: 1, [getChar("f")]: 2.0, [getChar("b")]: true };
    return b.s == "hello" && b?.i == 1 && b?.f == 2.0 && b["b"] == true;
}

string mapValue = "";

function testExpressionAsKeysWithSameKeysDefinedAsLiteralsOrFieldNames() returns boolean {
    Baz b = {
        f: 1.0,
        [getChar("f")]: 4.0,
        [getChar("s")]: addStringToMapValue(" world"),
        [getChar("s")]: addStringToMapValue(" from Ballerina"),
        s: addStringToMapValue("hello")
    };
    return b.s == "hello world from Ballerina" && b?.f == 4.0;
}

function getChar(string st) returns string {
    return st;
}

function addStringToMapValue(string s) returns string {
    mapValue = mapValue + s;
    return mapValue;
}

public type CustomDetail record {
    string message;
    error cause?;
};

const FOO_REASON = "FooError";
type FooError distinct error<CustomDetail>;

const BAR_REASON = "BarError";
type BarError error<CustomDetail>;

type Error FooError|BarError;

type MyRecord record {
    typedesc<Error>[] myErrorTypes = [FooError];
};

function testCustomErrorTypeDescFieldOnRecord() {
    MyRecord m = {};
    typedesc<Error> e = m.myErrorTypes[0];
    if (!(e is typedesc<FooError>)) {
            panic error("AssertionError", message = "expected typedesc<FooError> but found: " + e.toString());
    }
}

type FooRecord record {
    string a;
    int b?;
};

function removeOptional() {
    FooRecord fooRecord = {a : "a", b : 1};
    _ = fooRecord.remove("b");
    int? testValue = fooRecord?.b;
    if (testValue is ()) {
        return;
    } else {
        typedesc<any> resultType = typeof testValue;
        panic error("Wrong Result : expected result is null but recieved : " + resultType.toString());
    }
}

function removeRest() {
    FooRecord fooRecord = { a: "a", b : 1, "c" : 10};
    _ = fooRecord.remove("c");
    anydata testValue = fooRecord["c"];
    if (testValue is ()) {
        return;
    } else {
        typedesc<any> resultType = typeof testValue;
        panic error("Wrong Result : expected result is null but recieved : " + resultType.toString());
    }
}

type Student record {
    int id;
    string name?;
};

type Grades record {
    int maths;
    int physics;
};

function removeIfHasKeyOptional() {
    Student s = {id : 1, name : "Andrew"};
    string? n = <string?> s.removeIfHasKey("name");
    if (n is ()) {
         panic error("Returned value should be an string.");
    }

    if (<string>n !== "Andrew") {
         panic error("Returned value should equals 'Andrew'.");
    }

    var age = s.removeIfHasKey("age");
    if !(age is ()) {
         panic error("Returned value should be nil.");
    }
}


function removeIfHasKeyRest() {
    Grades g1 = {maths: 80, physics:75};
    Student s = {id : 1, name : "Andrew", "grade": g1};
    Grades? g2 = <Grades?> s.removeIfHasKey("grade");
    if (g2 is ()) {
         panic error("Returned value should be an string.");
    }

    Grades g3 = <Grades>g2;
    if !(g3.maths == g1.maths && g3.physics == g1.physics) {
         panic error("Returned value should be identical with expected value.");
    }

    var g4 = s.removeIfHasKey("grade");
    if !(g4 is ()) {
         panic error("Returned value should be nil.");
    }
}

final int a = 10;

type Foo2 record {
    int a;
    int b = a;
};

function testScopingRules() {
    Foo2 f = {a: 20};
    assert(<Foo2>{a: 20, b: 10}, f);

    record {
        int a;
        int b = a;
        record {
            int p = a;
        } c = {};
    } f2 = {a: 20};

    assert(<record {int a; int b; record {int p;} c;}>{a: 20, b: 10, c: {p: 10}}, f2);
}

// Util functions

function assert(anydata expected, anydata actual) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        panic error(reason);
    }
}
