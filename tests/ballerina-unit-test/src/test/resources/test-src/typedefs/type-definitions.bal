type T1 A[];

type A int;

function testArray() returns T1 {
    A[] a = [1, 2, 3];
    return a;
}

// ---------------------------------------------------------------------------------------------------------------------

type T2 (B, C);

type B int;

type C string;

function testSimpleTuple() returns T2 {
    (B, C) value = (10, "Ten");
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

type T6 object { G g = ""; };

type G string;

function testObject() returns T6 {
    T6 t6 = new;
    return t6;
}

// ---------------------------------------------------------------------------------------------------------------------

// Todo - Add initial value to object field after fixing -
//https://github.com/ballerina-platform/ballerina-lang/issues/12958
type T7 int[]|A[]|(B, C)|map<string>|map<D>|E|int|record { F f; }|object { public G g; }|error;

function testUnion() returns T7 {
    object { public G g; } o = new;
    T7 t7 = o;
    return t7;
}

// ---------------------------------------------------------------------------------------------------------------------

// Todo - Add initial value to object field after fixing -
//https://github.com/ballerina-platform/ballerina-lang/issues/12958
type T8 (int[], A[], (B, C), map<string>, map<D>, E, int, record { F f; }, object { public G g; }, error);

function testComplexTuple() returns T8 {
    int[] iarr = [1, 2];
    A[] aarr = [3, 4];
    (B, C) bc = (2, "Two");
    map<string> ms = { "k": "v" };
    map<D> md = { "k": 1 };
    E e = "Ballerina";
    int i = 10;
    record { F f; } r = { f: "Ballerina" };
    object { public G g; } o = new;
    error err = error("reason");
    T8 t8 = (iarr, aarr, bc, ms, md, e, i, r, o, err);
    return t8;
}
