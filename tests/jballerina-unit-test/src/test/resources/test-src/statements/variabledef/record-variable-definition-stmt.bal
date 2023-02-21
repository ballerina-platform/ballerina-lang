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

type Age record {
    int age;
    string format;
};

type Person record {|
    string name;
    boolean married;
|};

function simpleDefinition() returns [string, boolean] {
    Person p = {name: "Peter", married: true};
    Person {name: fName, married} = p;
    return [fName, married];
}

type PersonWithAge record {
    string name;
    Age age;
    boolean married;
};

function recordVarInRecordVar() returns [string, int, string, boolean] {
    PersonWithAge {name: fName, age: {age: theAge, format}, married} = getPersonWithAge();
    return [fName, theAge, format, married];
}

function getPersonWithAge() returns PersonWithAge {
    return { name: "Peter", age: {age:29, format: "Y"}, married: true, "work": "SE" };
}

function recordVarInRecordVar2() returns [string, Age] {
    PersonWithAge p = { name: "Peter", age: {age:29, format: "Y"}, married: true, "work": "SE" };
    PersonWithAge {name: fName, age} = p;
    return [fName, age];
}

type StreetCity record {
    string streetName;
    string city;
};

type Address record {
    int postalCode;
    StreetCity street;
};

type PersonWithAddress record {
    string name;
    boolean married;
    Address address;
};

function recordVarInRecordVarInRecordVar() returns [string, boolean, int, string, string] {
    PersonWithAddress personWithAdd =  {name: "Peter", married: true, address: {postalCode: 1000, street: {streetName: "PG", city: "Colombo 10"}}};
    PersonWithAddress {name: fName, married, address: {postalCode, street: {streetName: sName, city}}} = personWithAdd;
    return [fName, married, postalCode, sName, city];
}

type Employee record {
    string name;
    [int, string] address;
};

function tupleVarInRecordVar() returns [string, int, string] {
    Employee e = {name: "John", address: [20, "PG"]};
    Employee {name, address: [number, street]} = e;
    return [name, number, street];
}

function defineThreeRecordVariables() returns [string, int] {
    PersonWithAge p1 = { name: "John", age: {age:30, format: "YY"}, married: true, "work": "SE" };
    PersonWithAge p2 = { name: "Doe", age: {age:15, format: "MM"}, married: true, "work": "SE" };
    PersonWithAge p3 = { name: "Peter", age: {age:5, format: "DD"}, married: true, "work": "SE" };
    PersonWithAge {name: fName1, age: {age: theAge1, format: format1}, married: married1} = p1;
    PersonWithAge {name: fName2, age: {age: theAge2, format: format2}, married: married2} = p2;
    PersonWithAge {name: fName3, age: {age: theAge3, format: format3}, married: married3} = p3;

    string stringAddition = fName1 + fName2 + fName3 + format1 + format2 + format3;
    int intAddition = theAge1 + theAge2 + theAge3;
    return [stringAddition, intAddition];
}

function recordVariableWithRHSInvocation() returns string {
    Person {name: fName, married} = getPersonRecord();
    string name = fName + " Jill";
    return name;
}

function getPersonRecord() returns Person {
    Person person = {name: "Jack", married: true};
    return person;
}

function nestedRecordVariableWithRHSInvocation() returns string {
    PersonWithAge person = {name: "Peter", age: getAgeRecord(), married: true, "work": "SE"};
    PersonWithAge {name: fName, age: {age: theAge, format}, married} = person;
    string name = fName + " Parker";
    return name;
}

function getAgeRecord() returns Age {
    Age a = {age: 99, format:"MM"};
    return a;
}

function testRestParameter() returns map<anydata|error> {
    PersonWithAge p = {name: "John", age: {age:30, format: "YY"}, married: true, "work": "SE", "other": getAgeRecord()};
    PersonWithAge {name, age: {age, format}, married, ...rest} = p;
    return rest;
}

function testNestedRestParameter() returns [map<anydata|error>, map<anydata|error>] {
    PersonWithAge p = { name: "John", age: { age: 30, format: "YY", "year": 1990 }, married: true, "work": "SE" };
    PersonWithAge {name, age: {age, format, ...rest1}, married, ...rest2} = p;
    return [rest1, rest2];
}

function testVariableAssignment() returns [string, int, string, boolean, map<anydata|error>] {
    PersonWithAge person = { name: "Peter", age: { age: 29, format: "Y" }, married: true, "work": "SE" };
    var {name: fName, age: {age, format}, married, ...rest} = person;
    return [fName, age, format, married, rest];
}

function testVariableAssignment2() returns [string, int, string, boolean, map<anydata|error>] {
    PersonWithAge person = { name: "Peter", age: { age: 29, format: "Y" }, married: true, "work": "SE"};
    var {name: fName, age: {age, format}, married, ...rest} = person;
    fName = "James";
    age = 30;
    format = "N";
    married = false;
    rest["added"] = "later";
    return [fName, age, format, married, rest];
}

// -------------------------

type Student record {
    string name;
    [int, int, int] dob;
    byte gender;
};

function testTupleVarDefInRecordVarDef() returns [string, [int, int, int], byte, string, int, int, int] {
    Student st1 = {name: "Mark", dob: [1, 1, 1990], gender: 1};
    Student {name, dob, gender} = st1;
    Student {name: sName, dob: [a, b, c]} = st1;
    return [name, dob, gender, sName, a, b, c];
}

type Parent record {
    string[] namesOfChildren;
    Child[] children;
    Child child;

};

type Child record {
    string name;
    [int, Age] yearAndAge;
};

function testRecordInsideTupleInsideRecord() returns [string[], string, map<anydata|error>] {
    [int, Age] yearAndAge1 = [1992, {age: 26, format: "Y"}];
    [int, Age] yearAndAge2 = [1994, {age: 24, format: "X"}];
    [int, Age] yearAndAge3 = [1996, {age: 22, format: "Z"}];
    Child ch1 = {name: "A", yearAndAge: yearAndAge1};
    Child ch2 = {name: "B", yearAndAge: yearAndAge2};
    Child ch3 = {name: "C", yearAndAge: yearAndAge3};

    Parent parent = {namesOfChildren: ["A", "B"], children: [ch1, ch2], child: ch3};
    Parent {namesOfChildren, children, ...child} = parent;
    return [namesOfChildren, children[0].name, child];
}

function testRecordInsideTupleInsideRecord2() returns [string, int, int, string] {
    [int, Age] yearAndAge1 = [1992, {age: 26, format: "Y"}];
    [int, Age] yearAndAge2 = [1994, {age: 24, format: "X"}];
    [int, Age] yearAndAge3 = [1996, {age: 22, format: "Z"}];
    Child ch1 = {name: "A", yearAndAge: yearAndAge1};
    Child ch2 = {name: "B", yearAndAge: yearAndAge2};
    Child ch3 = {name: "C", yearAndAge: yearAndAge3};

    Parent parent = {namesOfChildren: ["A", "B"], children: [ch1, ch2], child: ch3};
    Parent {namesOfChildren, children, child: {name, yearAndAge: [yearInt, {age, format}]}} = parent;
    return [name, yearInt, age, format];
}

function testRecordInsideTupleInsideRecordWithVar() returns [string[], string, map<anydata|error>] {
    [int, Age] yearAndAge1 = [1992, {age: 26, format: "Y"}];
    [int, Age] yearAndAge2 = [1994, {age: 24, format: "X"}];
    [int, Age] yearAndAge3 = [1996, {age: 22, format: "Z"}];
    Child ch1 = {name: "A", yearAndAge: yearAndAge1};
    Child ch2 = {name: "B", yearAndAge: yearAndAge2};
    Child ch3 = {name: "C", yearAndAge: yearAndAge3};

    Parent parent = {namesOfChildren: ["A", "B"], children: [ch1, ch2], child: ch3};
    var {namesOfChildren, children, ...child} = parent;
    return [namesOfChildren, children[0].name, child];
}

function testRecordInsideTupleInsideRecord2WithVar() returns [string, int, int, string] {
    [int, Age] yearAndAge1 = [1992, {age: 26, format: "Y"}];
    [int, Age] yearAndAge2 = [1994, {age: 24, format: "X"}];
    [int, Age] yearAndAge3 = [1998, {age: 20, format: "A"}];
    Child ch1 = {name: "A", yearAndAge: yearAndAge1};
    Child ch2 = {name: "B", yearAndAge: yearAndAge2};
    Child ch3 = {name: "D", yearAndAge: yearAndAge3};

    Parent parent = {namesOfChildren: ["A", "B"], children: [ch1, ch2], child: ch3};
    var {namesOfChildren, children, child: {name, yearAndAge: [yearInt, {age, format}]}} = parent;
    return [name, yearInt, age, format];
}

type UnionOne record {
    boolean var1;
    int var2;
    float var3 = 0;
};

type UnionTwo record {
    int var1;
    float var2;
};

type UnionThree record {
    int var1;
    float var2;
    UnionOne|UnionTwo var3;
};

function testRecordVarWithUnionType() returns [int, float, UnionOne|UnionTwo] {
    UnionOne u1 = { var1: false, var2: 12, "restP1": "stringP1", "restP2": true };
    UnionThree u3 = {var1: 50, var2: 51.1, var3: u1};
    UnionThree {var1, var2, var3, ...rest} = u3;
    return [var1, var2, var3];
}

type UnionRec1 record {|
    string var1;
    string var2;
    string var3?;
    int...;
|};

type UnionRec2 record {|
    boolean var1;
    boolean var2;
    boolean var3;
    float...;
|};

function testUnionRecordVariable() returns [string|boolean, string|boolean, string|boolean?, int|float?] {
    UnionRec1 rec = {var1: "A", var2: "B"};
    UnionRec1|UnionRec2 {var1, var2, var3, var4} = rec;

    return [var1, var2, var3, var4];
}

function testIgnoreVariable() returns [string, int] {
    PersonWithAge p = { name: "John", age: { age:30, format: "YY", "year": 1990 }, married: true, "work": "SE" };
    PersonWithAge {name, age: {age, format: _, ...rest1}, married: _, ...rest2} = p;
    return [name, age];
}

function testRecordVariableWithOnlyRestParam() returns map<anydata|error> {
    PersonWithAge p = { name: "John", age: { age:30, format: "YY", "year": 1990}, married: true, "work": "SE" };
    PersonWithAge { ...rest } = p;
    return rest;
}

class Object {
    private int 'field;

    public function init() {
        self.'field = 12;
    }

    public function getField() returns int {
        return self.'field;
    }
}

type IntRestRecord record {|
    string name;
    boolean married;
    int...;
|};

type ObjectRestRecord record {|
    string name;
    boolean married;
    Object...;
|};

type IntStringMap map<int|string>;

type ObjectMap map<Object>;

function testRestParameterType() returns [boolean, boolean, boolean, boolean, boolean] {
    IntRestRecord rec1 = { name: "A", married: true, "age": 19, "token": 200 };
    IntRestRecord { name: name1, ...other1 } = rec1;
    var { name: name2, ...other2 } = rec1;

    IntRestRecord|ObjectRestRecord rec3 = rec1;
    IntRestRecord|ObjectRestRecord { name: name5, ...other5 } = rec3;

    IntStringMap map1 = { name: "A", "age": 19, "token": 200 };

    IntStringMap|ObjectMap map2 = map1;
    IntStringMap|ObjectMap { name: name8, ...other9 } = map2;

    any a1 = other1;
    any a2 = other2;
    any a5 = other5;
    any a9 = other9;

    return [a1 is record{|never name?; boolean married; int...;|}, a2 is record{|int...;|},
    a5 is record{|never name?; boolean married; int|Object...;|}, a5 is map<anydata>,
    a9 is record {| never name?; (int|string|Object)...; |}];
}

type XY record {
    int x;
    int y;
};

// any field other than x and y
type NotXY record {
    never x?;
    never y?;
};

function testInferredType(XY xy) returns NotXY {
    var {x: _, y: _, ...extra} = xy;
    return extra;
}

function testInferredResType() {
    NotXY extra = testInferredType({x: 10, y: 20, "foo": "bar"});
    assertEquality(extra["foo"], "bar");
}

function testRecordDestructuring1() {
    var {s: s, i: i, ...rest} = recordReturningFunc(10);
    int num1 = <int>rest.get("f");
    assertEquality(num1, 10);
    {s: s, i: i, ...rest} = recordReturningFunc((20));
    int num2 = <int>rest["f"];
    assertEquality(num2, 20);
}

function recordReturningFunc(int num) returns record { string s; int? i; } {
    return {s: "hello", i: 10, "f": num};
}

type Emp record {|
    string name;
    int age;
    string...;
|};

function testRecordDestructuring2() {
    string stdName;
    int age;
    map<string> details;
    {name: stdName, age, ...details} = <Emp>{name: "Jane Doe", age: 10, "foo": "bar"};
    assertEquality("bar", details["foo"]);
}

type StudentRecord record {
    int? Id;
    string studentName;
};

public record {
    int Id;
    string studentName;
} {Id, ...studentDetail} = {Id: 1001, studentName: "John", "Age": 24, "surName": "Paker"};

public function testRecordDestructuring3() {
    record {
        int? Id;
        string studentName;
    } {Id, ...studentDetail} = getStudentRecord(1);

    assertEquality("John", <string>studentDetail["studentName"]);
    assertEquality(24, <int>studentDetail["Age"]);
    assertEquality("Paker", <string>studentDetail["surName"]);
}

function getStudentRecord(int? id) returns record { int? Id; string studentName; } {
    return {Id: id, studentName: "John", "Age": 24, "surName": "Paker"};
}

type SchemaA record {|
    string name;
    int age;
    string...;
|};

type SchemaB record {|
    string name;
    boolean age;
    boolean married;
    int...;
|};

function testRestFieldResolvingWithUnion() {
    SchemaA recA = {name: "David", age:10, "foo":"bar"};
    SchemaA|SchemaB {name, ...rest} = recA;

    var age = rest.age;
    var fooVal = rest["foo"];
    assertEquality(10, age);
    assertEquality("bar", fooVal);

    rest.age = true;
    rest["foo"] = 100;
    age = rest.age;
    fooVal = rest["foo"];
    assertEquality(true, age);
    assertEquality(100, fooVal);

    rest.married = true;
    assertEquality(true, rest?.married);
}

public function testClosedRecordDefinedRestField() {
    string fullName;
    boolean isMarried;
    map<string> rest1;
    map<never> rest2;
    record {| never name?; boolean married; never...; |} rest3;

    Person p = {name: "Jane Doe", married: false};
    {name: fullName, married: isMarried, ...rest1} = p;

    record {| never name?; never married?; |} rec = {};
    rest2 = rec;
    {name: fullName, married: isMarried, ...rest2} = p;
    {name: fullName, ...rest3} = p;
    assertEquality(false, rest3.married);
}

function testRestFieldResolving() {
    testInferredResType();
    testRecordDestructuring1();
    testRecordDestructuring2();
    testRecordDestructuring3();
    testRestFieldResolvingWithUnion();
    testClosedRecordDefinedRestField();
}

type Topt1 record {
    int x?;
    int y?;
};

function testOptionalFieldRecordAssignment1() {
    Topt1 topt = {x: 2, y: 3};
    var {x: xx1, y: yy1} = topt;
    assertEquality(2, xx1);
    assertEquality(3, yy1);

    topt = {y: 4};
    var {x: xx2, y: yy2} = topt;
    assertTrue(xx2 is ());
    assertEquality(4, yy2);

    topt = {x: 5, y: 6};
    var {x, y} = topt;
    assertEquality(5, x);
    assertEquality(6, y);
}

function testOptionalFieldRecordAssignment2() {
    Topt1 topt = {};
    var {x, y} = topt;
    assertTrue(x is () && y is ());
}

type Topt2 record {
    int a;
    record {
        int b?;
    }[1] c;
};

function testOptionalFieldRecordAssignment3() {
    Topt2 topt = {a: 4, c: [{b: 5}]};
    var {a: aa, c: cc} = topt;
    var {b} = cc[0];
    assertEquality(4, aa);
    assertEquality(5, b);
}

function testOptionalFieldRecordAssignment4() {
    Topt2 topt = {a: 4, c: [{b: 5}]};
    var {a, c} = topt;
    var {b} = c[0];
    assertEquality(4, a);
    assertEquality(5, b);

    var {a: _, c: cc} = topt;
    record {int b?;}[1] ccc = cc;
    assertEquality(5, ccc[0].b);
}

type Topt3 record {
    int a;
    record {
        int b?;
    }[1] c?;
};

function testOptionalFieldRecordAssignment5() {
    Topt3 topt = {a: 4, c: [{b: 5}]};
    var {a, c} = topt;
    assertEquality(4, a);
    assertEquality(c is record {int b?;}[1], true);
    if c is record {int b?;}[1] {
        var {b} = c[0];
        assertEquality(5, b);
    }

    topt = {a: 4};
    var {a: _, c: cc} = topt;
    assertTrue(cc is ());
}

type Topt4 record {
    int x?;
    int? y?;
};

function testOptionalFieldRecordAssignment6() {
    Topt4 topt = {x: 2, y: 3};
    var {x: xx, y: yy} = topt;
    assertEquality(2, xx);
    assertEquality(3, yy);
}

function testOptionalFieldRecordAssignment7() {
    Topt2 topt = {a: 4, c: [{b: 5}]};
    var {a, c: [{b}]} = topt;
    assertEquality(4, a);
    assertEquality(5, b);
}

function testOptionalFieldRecordAssignment8() {
    Topt3 topt = {a: 4, c: [{b: 5}]};
    var {a, c} = topt;
    assertEquality(a, 4);
    record {int b?;}[1]? _ = c;
    var {b} = (<record {int b?;}[1]> c)[0];
    assertEquality(b, 5);
}

function testOptionalFieldRecordAssignment9() {
    Topt3 topt = {a: 4};
    var {a, c} = topt;
    assertTrue(c is ());
}

function testOptionalFieldRecordAssignment10() {
    [record {int i?;}] y = [{}];
    var [{i}] = y;
    assertTrue(i is ());
    var [{i: i2}] = y;
    assertTrue(i2 is ());
    y = [{i: 3}];
    var [{i: i3}] = y;
    assertEquality(i3, 3);
}

function testRecordDefinitionWithOptionalFields1() {
    int? x = 2;
    int? y = ();
    Topt1 topt = {x, y};
    assertEquality(topt.x, 2);
    assertTrue(topt.y is ());
}

function testRecordDefinitionWithOptionalFields2() {
    int? x = ();
    int? y = ();
    Topt1 topt = {x, y};
    assertTrue(topt.x is ());
    assertTrue(topt.y is ());
}

function testRecordDefinitionWithOptionalFields3() {
    Topt1 topt = {x: 3, y: ()};
    assertEquality(topt.x, 3);
    assertTrue(topt.y is ());
}

function testRecordDefinitionWithOptionalFields4() {
    int? a = 4;
    int? b = ();
    Topt2 topt = {a: 4, c: [{b}]};
    assertEquality(4, topt.a);
    assertTrue((topt.c)[0].b is ());
}

function testRecordDefinitionWithOptionalFields5() {
    int? a = 4;
    record {
        int b?;
    }[1]? c = ();
    Topt3 topt = {a: 4, c};
    assertEquality(4, topt.a);
    assertTrue(topt.c is ());
}

function testRecordDefinitionWithOptionalFields6() {
    int? a = 4;
    int? b = 41;
    record {
        int b?;
    }[1]? c = ();
    Topt3 topt = {a: 4, c: [{b}]};
    assertEquality(4, topt.a);
    record {int b?;}[] listResult = <record {int b?;}[1]> topt.c;
    assertEquality(41, listResult[0].b);
}

function testRecordDefinitionWithOptionalFields7() {
    int x = 2;
    int? y = ();
    Topt1 topt = {x, y};
    assertEquality(topt.x, 2);
    assertTrue(topt.y is ());
}

type NIL null;
const CONST_NIL = ();

function testRecordDefinitionWithOptionalFields8() {
    int x = 2;
    NIL? y = ();
    Topt1 topt = {x, y};
    assertEquality(topt.x, 2);
    assertTrue(topt.y is ());

    Topt1 topt1 = {x, y: ()};
    assertTrue(topt1.y is ());

    int? y1 = CONST_NIL;
    Topt1 topt2 = {x, y: y1};
    assertTrue(topt2.y is ());

    Topt1 topt3 = {x, y: CONST_NIL};
    assertTrue(topt3.y is ());
}

function testRecordDefinitionWithOptionalFields9() {
   int? x = 1;
   int? y = ();
   Topt4 t4 = {x, y};
   assertEquality(t4.toString(), "{\"x\":1,\"y\":null}");
   Topt1 t1 = {x, y};
   assertEquality(t1.toString(), "{\"x\":1}");
}

type EmployeeDetails record {|
    string emp\:name;
|};

type ProductDetails record {|
    string x\:code = "default";
    int quantity;
|};

function testRecordFieldBindingPatternsWithIdentifierEscapes() {
    EmployeeDetails empDetails = {
        emp\:name: "Jo"
    };
    var {emp\:name} = empDetails;
    assertEquality("Jo", emp\:name);

    ProductDetails prodDetails = {quantity: 1234};
    ProductDetails {x\:code, quantity} = prodDetails;
    assertEquality("default", x\:code);
    assertEquality(1234, quantity);

    [EmployeeDetails, ProductDetails] details = [{emp\:name: "Amy"}, {x\:code: "basic", quantity: 324}];
    var [{emp\:name: a}, {x\:code: b, quantity: c}] = details;
    assertEquality("Amy", a);
    assertEquality("basic", b);
    assertEquality(324, c);
}

type ReadOnlyRecord readonly & record {|
    int[] x;
    string y;
|};

function testReadOnlyRecordWithMappingBindingPatternInVarDecl() {
    ReadOnlyRecord f1 = {x: [1, 2], y: "s1"};
    ReadOnlyRecord {x, y} = f1;
    int[] & readonly rArr = x;
    assertEquality(<int[]> [1, 2], x);
    assertEquality(<int[]> [1, 2], rArr);
    string str = y;
    assertEquality("s1", y);
    assertEquality("s1", str);

    ReadOnlyRecord f2 = {x: [3], y: "s2"};
    var {x: x2, y: y2} = f2;
    rArr = x2;
    assertEquality(<int[]> [3], x2);
    assertEquality(<int[]> [3], rArr);
    str = y2;
    assertEquality("s2", y2);
    assertEquality("s2", str);

    readonly & record {
        int[] a;
        ReadOnlyRecord b;
    } r = {a: [12, 34, 56], b: f1};
    var {a, b} = r;
    readonly & int[] a1 = a;
    assertEquality(<int[]> [12, 34, 56], a1);
    ReadOnlyRecord b1 = b;
    assertEquality(<ReadOnlyRecord> {x: [1, 2], y: "s1"}, b1);
}

//////////////////////////////////////////////////////////////////////////////////////

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(boolean actual) {
    assertEquality(true, actual);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
