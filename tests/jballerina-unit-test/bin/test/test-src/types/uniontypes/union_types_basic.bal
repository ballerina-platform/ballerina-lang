import ballerina/jballerina.java;

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
        println("int");
    } else {
        println("null");
    }

    int|boolean|() i = ();

    if i is int {
        println("int");
    } else if i is boolean {
        println("boolean");
    } else {
        println(i);
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

type MyUnion int|int[]|string[];

function testCastToImmutableUnion() {
    MyUnion v = <readonly> [1, 2, 3];
    MyUnion & readonly w = <MyUnion & readonly> v;
    assertEquality(true, w is int[] & readonly);
    assertEquality(true, <any> w is MyUnion & readonly);
    assertEquality(<int[]> [1, 2, 3], w);

    MyUnion x = [1, 2, 3];
    (MyUnion & readonly)|error y = trap <MyUnion & readonly> x;
    assertEquality(true, y is error);
    error err = <error> y;
    assertEquality("{ballerina}TypeCastError", err.message());
    assertEquality("incompatible types: 'int[]' cannot be cast to '(MyUnion & readonly)'",
                   <string> checkpanic err.detail()["message"]);
}

type SomeTypes1 int:Unsigned32|object{};
type SomeTypes2 string:Char|object{};

type AA record {
    int:Unsigned32|object{} a = 0;
    string:Char|int b = "A";
};

class BB {
    int:Unsigned32|object{} a = 0;
    string:Char|int b = "A";
}

function testUnionWithIntegerSubTypes() {
    int:Unsigned32|object{} v1 = 0;
    SomeTypes1 v2 = 1;
    SomeTypes1|string v3 = 2;
    int:Unsigned32|string v4 = 3;
    (int:Unsigned32|object{})[] v5 = [12];
    [int:Unsigned32|object{}] v6 = [12];
    map<int:Unsigned32|object{}> v7 = {"a": 12};
    AA v8 = {};
    BB v9 = new;
    any|byte v10 = 1;
    any|byte v11 = 1000;
    byte|anydata v12 = 1;
    byte|anydata v13 = 1000;
    int:Unsigned32|json v14 = 6500;

    assertEquality(0, v1);
    assertEquality(1, v2);
    assertEquality(2, v3);
    assertEquality(3, v4);
    assertEquality(12, v5[0]);
    assertEquality(12, v6[0]);
    assertEquality(12, v7["a"]);
    assertEquality(0, v8.a);
    assertEquality(1, foo1());
    assertEquality(0, v9.a);
    assertEquality(1, v10);
    assertEquality(1000, v11);
    assertEquality(1, v12);
    assertEquality(1000, v13);
    assertEquality(6500, v14);
}

function testUnionWithStringSubTypes() {
    string:Char|object{} v1 = "A";
    SomeTypes2 v2 = "A";
    SomeTypes2|int v3 = "A";
    string:Char|int v4 = "A";
    (string:Char|object{})[] v5 = ["A"];
    [string:Char|object{}] v6 = ["A"];
    map<string:Char|object{}> v7 = {"a": "A"};
    AA v8 = {};
    BB v9 = new;
    any|string:Char v10 = "A";
    any|string:Char v11 = "A";
    string:Char|anydata v12 = "A";
    string:Char|anydata v13 = "A";

    assertEquality("A", v1);
    assertEquality("A", v2);
    assertEquality("A", v3);
    assertEquality("A", v4);
    assertEquality("A", v5[0]);
    assertEquality("A", v6[0]);
    assertEquality("A", v7["a"]);
    assertEquality("A", v8.b);
    assertEquality("A", foo2());
    assertEquality("A", v9.b);
    assertEquality("A", v10);
    assertEquality("A", v11);
    assertEquality("A", v12);
    assertEquality("A", v13);
}

function foo1() returns int:Unsigned32|object{} {
    return 1;
}

function foo2() returns string:Char|object{} {
    return "A";
}

const A = 1.0f;
const B = 2.0d;

type Decimals A|B|2;

function testUnionWithDecimalFiniteTypes() {
    Decimals value = 2.0d;
    assertEquality(true, value is decimal);
    assertEquality(4.3d, <decimal>value + 2.3d);
}

type UnionTypeWithArray1 int|UnionTypeWithArray1[];
type intArray int[];
type UnionTypeWithArray3 UnionTypeWithArray1|string|intArray;

function testRecursiveUnionTypeWithArray() {
    UnionTypeWithArray1 a = [1, 2, 3];
    assertEquality(true, a == [1, 2, 3]);

    UnionTypeWithArray3 b = [[1, 2, 3]];
    assertEquality(true, b == [[1, 2, 3]]);
}

type UnionTypeWithMap1 int|map<UnionTypeWithMap1>;
type UnionTypeWithMap2 int|map<UnionTypeWithMap2>|UnionTypeWithMap1;

function testRecuriveUnionTypeWithMap() {
    UnionTypeWithMap1 a = {"one":1, "two":2};
    assertEquality(true, a == {"one":1, "two":2});
    UnionTypeWithMap2 b =  {"one":{"one":1, "two":2}};
    assertEquality(true, b == {"one":{"one":1, "two":2}});
}

type UnionTypeWithMapAndArray int|UnionTypeWithMapAndArray[]|map<UnionTypeWithMapAndArray>;

function testRecursiveUnionTypeWithArrayAndMap() {
    UnionTypeWithMapAndArray a = {"one":1, "two":2};
    assertEquality(true, a == {"one":1, "two":2});

    UnionTypeWithMapAndArray b = [1, 2, 3];
    assertEquality(true, b == [1, 2, 3]);

    UnionTypeWithMapAndArray c = {"one" : [1,2,3]};
    assertEquality(true, c == {"one" : [1,2,3]});

    UnionTypeWithMapAndArray d = [{"one":1, "two":2}];
    assertEquality(true, d ==[{"one":1, "two":2}]);
}

type UnionTypeWithRecord int|record {UnionTypeWithRecord u;};

function testRecursiveUnionTypeWithRecord() {
    UnionTypeWithRecord a = {u: 5};
    assertEquality(true, a == {u: 5});
    UnionTypeWithRecord b = {u: {u:5}};
    assertEquality(true, b == {u: {u:5}});
}

type UnionTypeWithTuple1 int|[UnionTypeWithTuple1];
type UnionTypeWithTuple2 int|[UnionTypeWithTuple2, UnionTypeWithTuple1];
type UnionTypeWithTuple3 int|[UnionTypeWithTuple3...];

function testRecursiveUnionTypeWithTuple() {
    UnionTypeWithTuple1 a = [5];
    assertEquality(true, a == [5]);
    UnionTypeWithTuple2 b = [4, [5]];
    assertEquality(true, b == [4, [5]]);
    UnionTypeWithTuple3 c = [4, 5, 6, 7];
    assertEquality(true, c == [4, 5, 6, 7]);
}

type UnionTypeWithTable int|table<map<UnionTypeWithTable>>;

function testRecursiveUnionWithTable() {
    UnionTypeWithTable a = 1;
    UnionTypeWithTable b = 2;
    UnionTypeWithTable tb = table [
            {
                one: a
            },
            {
                one: b
            }
    ];
    assertEquality("[{\"one\":1},{\"one\":2}]", (<table<map<UnionTypeWithTable>>> tb).toString());
}

type UnionTest1 anydata|-9223372036854775808f|-1|-1d|"string";
function testLiteralWithUnionExpectedType() {
    UnionTest1 a = 9223372036854775808;
    UnionTest1 b = 17976931348623157000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000;
    UnionTest1 c = 304;

    assertEquality(9.223372036854776E18, a);
    assertEquality(1.797693134862315700000000000000000E+331d, b);
    assertEquality(304, c);
}

function testParenthesisedSingletonUnionType() {
    (true|false?) b = true;
    assertEquality(true, b);
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

    panic error(ASSERTION_ERR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
