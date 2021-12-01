type T1 A[];

type A int;

function testArray() returns T1 {
    A[] a = [1, 2, 3];
    return a;
}

// ---------------------------------------------------------------------------------------------------------------------

type T2 [B, C];

type B int;

type C string;

function testSimpleTuple() returns T2 {
    [B, C] value = [10, "Ten"];
    return value;
}

// ---------------------------------------------------------------------------------------------------------------------

type T3 map<D>;

type D int;

function testMap() returns T3 {
    map<D> m = { "Five": 5 };
    return m;
}

// ---------------------------------------------------------------------------------------------------------------------

type T4 E;

type E string;

function testValueType() returns T4 {
    E e = "Ballerina";
    return e;
}

// ---------------------------------------------------------------------------------------------------------------------

type T5 record { F f = ""; };

type F string;

function testRecord() returns T5 {
    T5 t5 = { f: "Ballerina" };
    return t5;
}

// ---------------------------------------------------------------------------------------------------------------------

class T6 { G g = ""; }

type G string;

function testObject() returns T6 {
    T6 t6 = new;
    return t6;
}

// ---------------------------------------------------------------------------------------------------------------------

type T7 int[]|A[]|[B, C]|map<string>|map<D>|E|int|record { F f; }|object { public G g; }|error;

function testUnion() returns T7 {
    var o = object { public G g = ""; };
    T7 t7 = o;
    return t7;
}

// ---------------------------------------------------------------------------------------------------------------------

type Err error<record{| |}>;
type T8 [int[], A[], [B, C], map<string>, map<D>, E, int, record { F f; }, object { public G g; }, Err];

function testComplexTuple() returns T8 {
    int[] iarr = [1, 2];
    A[] aarr = [3, 4];
    [B, C] bc = [2, "Two"];
    map<string> ms = { "k": "v" };
    map<D> md = { "k": 1 };
    E e = "Ballerina";
    int i = 10;
    record { F f; } r = { f: "Ballerina" };
    var o = object { public G g = ""; };
    Err err = error("reason");
    T8 t8 = [iarr, aarr, bc, ms, md, e, i, r, o, err];
    return t8;
}

// ---------------------------------------------------------------------------------------------------------------------

type T9 H|I;

type T10 J|K|T9|L;

type H A[];

type I [A, B];

type J map<A>;

type K record { F f = ""; };

type L error|object { public G g; };

function testComplexUnion() returns T10 {
    A[] a = [4, 5, 6];
    T10 t10 = a;
    return t10;
}

// ---------------------------------------------------------------------------------------------------------------------

type T11 [T7, T10];

function testUnionInTuple() returns T11 {
    A[] a = [4, 5, 6];
    [int, int] t = [10, 20];
    T11 t11 = [a, t];
    return t11;
}

// ---------------------------------------------------------------------------------------------------------------------

type T12 xml;

function testXml() returns T12 {
    T12 x = xml `<name>ballerina</name>`;
    return x;
}


// ---------------------------------------------------------------------------------------------------------------------

type FB "A" | object { string f;};

class Foo {
    string f;

    function init(string f) {
        self.f = f;
    }
}

function testAnonObjectUnionTypeDef() {
    FB a = new Foo("FOO");

    if (!(a is Foo)) {
        panic error("Invalid type for anonObjectUnionTypeDef");
    }
}

type FB2 "A" | record { string f; };

function testAnonRecordUnionTypeDef() {
    FB2 a = { f : "FOO"};

    if (!(a is record { string f; })) {
        panic error("Error in union with anonymous record type definitions");
    }
}

type FB3 "A" | record {| string f; |};

function testAnonExclusiveRecordUnionTypeDef() {
    FB3 a = { f : "FOO" };

    if (!(a is record {| string f; |})) {
        panic error("Error in union with anonymous record type definitions");
    }
}

// ---------------------------------------------------------------------------------------------------------------------
type IntArray int[];
type Int_String [int, string];

function testIntArrayTypeDef() {
    IntArray s = [1, 2];
    anydata y = s;
    IntArray|error b = y.cloneWithType(IntArray);
    if (b is IntArray) {
        assertEquality(s[0], b[0]);
        assertEquality(s[1], b[1]);
    } else {
        assertFalse(true);
    }
}

function testTupleTypeDef() {
    Int_String x = [10, "XX"];
    anydata y = x;
    Int_String|error z = y.cloneWithType(Int_String);
    if (z is Int_String) {
        assertEquality(z[0], x[0]);
        assertEquality(z[1], x[1]);
    } else {
        assertFalse(true);
    }
}

type PersonOrInt Person|int;
type PersonOrNil Person?;

public type Person record {
    int id;
    string name;
};

function testTypeDefReferringToTypeDefDefinedAfter() {
    PersonOrInt a = 1;
    assertTrue(a is int);
    assertEquality(1, a);

    Person person = {id: 1, name: "Maryam"};
    PersonOrInt b = person;
    assertTrue(b is Person);
    assertEquality(person, b);

    PersonOrNil c = ();
    assertTrue(c is ());

    PersonOrNil d = person;
    assertTrue(d is Person);
    assertEquality(person, d);
}

type FuncTypeDef function (int x, int y) returns int;

final FuncTypeDef sumFunc = function(int x, int y) returns int {
    return x + y;
};

class C1 {
    FuncTypeDef func = sumFunc;

    function sum() returns int {
        return self.func(4, 100);
    }
}

function testFuncInvocation() {
    var obj = new C1();
    assertEquality(104, obj.sum());
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    assertEquality(true, actual);
}

function assertFalse(any|error actual) {
    assertEquality(false, actual);
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
