import ballerina/io;

function testFloatArrayLength(float[] arg) returns [int, int]{
    float[] defined;
    defined = [10.1, 11.1];
    defined[2] = 12.1;
    return [arg.length() , defined.length()];
}

function testIntArrayLength(int[] arg) returns [int, int]{
    int[] defined;
    defined = [ 1, 2, 3];
    defined[3] = 4;
    return [arg.length() , defined.length()];
}

function testStringArrayLength(string[] arg) returns [int, int]{
    string[] defined;
    defined = [ "hello" , "world", "I", "am"];
    defined[4] = "Ballerina";
    return [arg.length() , defined.length()];
}

function testXMLArrayLength() returns (int){
    xml[] defined;
    xml v1 = xml `<test>a</test>`;
    xml v2 = xml `<test>b</test>`;
    defined = [v1, v2];
    defined[2] = xml `<test>c</test>`;
    return defined.length();
}

function testJSONArrayLength() returns [int, int]{
    json[] arg = [{"test": 1}, {"test" : 1}];
    json[] defined;
    json v1;
    json v2;
    v1 = { "test" : "1"};
    v2 = { "test" : "2"};
    defined = [v1, v2];
    defined[2] = { "test" : "3"};
    return [arg.length() , defined.length()];
}

function testArrayWithNilElement() returns string {
    string?[] ar = ["abc", "d", (), "s"];
    return io:sprintf("%s", ar);
}

type Foo 1|2|3;

function testElementTypesWithoutImplicitInitVal() returns Foo[] {
    Foo[] arr;
    Foo[*] arr2 = [1, 2];
    arr = arr2;
    return arr;
}

type BarRec record {
    Foo[] fArr;
};

function testArrayFieldInRecord() returns BarRec {
    Foo[*] arr = [1, 2];
    BarRec rec = {fArr: arr};
    return rec;
}

class BarObj {
    Foo[] fArr;

    function init() {
        Foo[*] arr = [1, 2];
        self.fArr = arr;
    }
}

function testArrayFieldInObject() returns BarObj {
    BarObj obj = new;
    return obj;
}

function fnWithArrayParam(Foo[] arr) returns Foo[] {
    Foo[*] newArr = [arr[0],3];
    return newArr;
}

function testArraysAsFuncParams() returns Foo[] {
    Foo[*] arr = [1, 2];
    return fnWithArrayParam(arr);
}

type A1 record {
    B1 b = {};
    string a1?;
};

type B1 record {
    A1 a?;
    string b1 = "B1";
};

function testArraysOfCyclicDependentTypes() returns A1[] {
    A1[] arr = [];
    arr[3] = {a1: "A1", b: {b1: "B1"}};
    return arr;
}

function testArraysOfCyclicDependentTypes2() returns B1[] {
    B1[] arr = [];
    arr[3] = {b1: "B1"};
    return arr;
}

class P1 {
    Q1 q;
    string p1;

    function init() {
        self.q = new;
        self.p1 = "P1";
    }
}

class Q1 {
    P1 p;
    string q1;

    function init() {
        self.p = new;
        self.q1 = "Q1";
    }
}

function testArraysOfCyclicDependentTypes3() returns P1[] {
    P1[] arr = [];
    arr[3] = new;
    return arr;
}

function testArraysOfCyclicDependentTypes4() returns Q1[] {
    Q1[] arr = [];
    arr[3] = new;
    return arr;
}

function testGetFromFrozenArray() returns int {
    anydata[] array = [1, 4, 7];
    anydata[] newArray = array.cloneReadOnly();
    if (newArray is int[]) {
        return newArray[1];
    }

    return -1;
}

class Age {
    public int age;
    public function init(int age) {
    	 self.age = age;
    }
}

function testObjectDynamicArrayFilling() {
    Age[] y = [];
    y[0] = new(5);
    y[1] = new(5);
    assertArrayLengthPanic(2, y);
}

type AbstractPersonObject object {
    public string fName;
    public string lName;
    function getFullName() returns string;
};

class Employee {
    *AbstractPersonObject;
    function init(string fname, string lname) {
        self.fName = fname;
        self.lName = lname;
    }
    function getFullName() returns string {
        return self.fName + " " + self.lName;
    }
}

function createAbstractObjectEmptyArray() {
    AbstractPersonObject[5][] y = [];
    AbstractPersonObject e1 = new Employee("John", "Doe");
    y[0] = [e1];
    AbstractPersonObject[][5] r = [];
    r[0] = [e1, e1, e1, e1, e1];
    assertArrayLengthPanic(5, r[0]);
    assertArrayLengthPanic(1, y[0]);
}

function assertArrayLengthPanic(int expected, any[] arr, string message = "Array length did not match") {
    int actual = arr.length();
    if (expected != actual) {
        panic error(message + " Expected : " + expected.toString() + " Actual : " + actual.toString());
    }
}

const TYPEDESC_ARRAY = "typedesc int[][2]";

function testMultidimensionalArrayString() {
    int[][2] arr = [];
    typedesc<any> t = typeof arr;
    assertEquality(TYPEDESC_ARRAY, t.toString());

}

function testArrayMapString() {
    map<Foo>[2][] arr = [];
    typedesc<any> t = typeof arr;
    assertEquality("typedesc map<Foo>[2][]", t.toString());

}

function testArrayUnionType() {
    (int|string[4][3])[][2][4] arr = [];
    typedesc<any> t = typeof arr;
    assertEquality("typedesc (int|string[4][3])[][2][4]", t.toString());
}

function testArrayTupleType() {
    [string[2],int,float[3][4]][][] arr = [];
    typedesc<any> t = typeof arr;
    assertEquality("typedesc [string[2],int,float[3][4]][][]", t.toString());
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }
    if expected === actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

function testUpdatingJsonTupleViaArrayTypedVar() {
    [json...] a = [];
    json[] b = a;

    b[0] = {hello: "world"};
    b[1] = 2;

    assertArrayLengthPanic(2, b);

    if a[0] == <map<json>> {hello: "world"} && a[1] == 2 {
        return;
    }
    panic error("AssertionError", message = "expected 'hello=world 2', found '" + a.toString() +"'");
}
