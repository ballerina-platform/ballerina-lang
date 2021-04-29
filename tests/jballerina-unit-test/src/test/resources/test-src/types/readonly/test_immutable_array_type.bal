
import ballerina/test;

function testCastOfReadonlyIntArrayToByteArray() {
    readonly & int[] a = [1, 255];
    any b = a;

    test:assertTrue(b is (float|byte)[]);
    test:assertFalse(b is (boolean|float)[]);

    byte[] c = <byte[]> b;

    test:assertTrue(c === b);
    test:assertEquals("[1,255]", c.toString());
    test:assertEquals(1, c[0]);
    test:assertEquals(255, c[1]);
}

function testCastOfReadonlyIntArrayToByteArrayNegative() {
    readonly & int[] e = [1, 100, 1000];
    any f = e;
    byte[]|error g = trap <byte[]> f;
    test:assertTrue(g is error);
    error err = <error> g;
    test:assertEquals("{ballerina}TypeCastError", err.message());
    test:assertEquals("incompatible types: 'int[] & readonly' cannot be cast to 'byte[]'", <string> checkpanic err.detail()["message"]);
}

function testCastOfReadonlyAnyToByteArray() {
    readonly & any x = [1, 2, 3];
    any xx = x;
    byte[] y = <byte[]> xx;
    test:assertTrue(xx === y);
    test:assertEquals(1, y[0]);
}

function testCastOfReadonlyArrayToUnion() {
    readonly & int[] a = [1, 255];
    any b = a;
    (float|byte)[] i = <(float|byte)[]> b;
    test:assertTrue(i === b);
    test:assertEquals("[1,255]", i.toString());

    readonly & float[] d = [1, 2.5, 27.5f];
    any e = d;
    (int|float|byte)[] f = <(int|float|byte)[]> e;
    test:assertEquals("[1.0,2.5,27.5]", f.toString());
}

function testCastOfReadonlyUnionArrayToByteArray() {
    readonly & (int|byte)[] a = [1,2,3];
    any b = a;
    byte[] c = <byte[]> b;
    test:assertEquals("[1,2,3]", c.toString());

    readonly & (any|byte)[] d = [1,2,3];
    any e = d;
    byte[] f = <byte[]> e;
    test:assertEquals("[1,2,3]", f.toString());
}

type Foo record {|
    string s;
    int[] arr;
|};

type Bar record {|
    string s;
    byte[] arr;
|};

function testCastOfReadonlyRecord() {
    (Foo & readonly) f = {s: "a", arr: [1,2,3]};
    any a = f;
    Bar b = <Bar> a;
    test:assertTrue(b === a);
    test:assertEquals("{\"s\":\"a\",\"arr\":[1,2,3]}", b.toString());
}

function testCastOfReadonlyRecordNegative() {
    (Foo & readonly) f = {s: "a", arr: [1,2,300]};
    any a = f;
    Bar b = <Bar> a;
}

const FOO = "foo";

public function testCastOfReadonlyStringArrayToStringConstantArray() {

    readonly & string[] d = [FOO, FOO];
    any e =  d;
    FOO[] f = <FOO[]> e;

    test:assertTrue(f === e);
    test:assertEquals("[\"foo\",\"foo\"]", f.toString());
}
