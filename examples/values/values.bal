import ballerina/io;
import ballerina/lang.'float as floats;
import ballerina/lang.'int as ints;

// The types `int`, `float`, `decimal`, `string`, `boolean`, `byte`, and `nil` are called simple basic types
// because they are basic types with only simple values. Simple values are always immutable.
public function main() {
    // The `int` type represents the set of 64-bit signed integers.
    int i = 10;
    io:println(i);

    // The `ballerina/lang.int` module contains common functions that can be used
    // with `int` values.
    int|error i2 = ints:fromString("100");
    if (i2 is int) {
        io:println(i2);
    }

    // The `float` type represents the set of double precision IEEE 754 floating point numbers.
    float f = 20.0;
    io:println(f);

    // The `ballerina/lang.float` module contains common functions that can be used with `float` values.
    float f1 = floats:fromBitsInt(i);
    float f2 = 22.0;
    float max = floats:max(f1, f2);
    io:println("Max float: ", max);

    // The `.isNaN()`, `.isInfinite()`, and `.isFinite()` langlib functions are supported by the `float` type.
    // The `.isNaN()` function will return true if the `float` value is neither finite nor infinite.
    float nanVal = 0.0 / 0.0;
    io:println(nanVal.isNaN());

    // The `.isInfinite()` function will return true if the `float` value is neither NaN nor finite.
    float infiniteVal = 12.0 / 0.0;
    io:println(infiniteVal.isInfinite());

    // The `.isFinite()` function will return true if the `float` value is neither NaN nor infinite.
    float finiteVal = 6.0 / 3.0;
    io:println(finiteVal.isFinite());

    // The `decimal` type represents the set of 128-bits IEEE 754R decimal floating point numbers.
    decimal d = 27.5;
    io:println(d);

    // The `byte` type represents the set of 8-bit unsigned integers.
    byte c = 23;
    io:println(c);

    // The `string` type represents the set of sequences of Unicode code points.
    string s = "Ballerina";
    io:println(s);

    // The `boolean` type has only two values: `true` and `false`.
    boolean b = true;
    io:println(b);

    // The `nil` type has a single value and is used to represent the absence of any other value.
    // Both the `nil` type and the `nil` value are written as `()`.
    () n = ();
    io:println(n);
    // Another representation for the `nil` value is the `null` literal.
    // However, the use of the `null` literal in only allowed in JSON contexts.
    json j = null;
    io:println(j);
}
