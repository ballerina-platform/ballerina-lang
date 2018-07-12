import ballerina/io;

// The types `int`, `float`, `string`, `boolean`, `blob`, and `nil` are called simple basic types
// because they are basic types with only simple values. Simple values are always immutable.
function main(string... args) {
    // The `int` type represents the set of 64-bit signed integers.
    // The implicit initial value of the `int` type is `0`.
    int i = 10;
    io:println(i);

    // The `float` type represents the set of double precision IEEE 754 floating point numbers.
    // The implicit initial value of the `float` type is `+0.0.`
    float f = 20.0;
    io:println(f);

    // The `byte` type represents the set of 8-bit unsigned integers.
    // The implicit initial value of the `byte` type is `0`. Value space for `byte` is 0-255 both inclusive.
    byte c = 23;
    io:println(c);

    // The `string` type represents the set of sequences of Unicode code points.
    // The implicit initial value of the `string` type is `""` (empty string).
    string s = "Ballerina";
    io:println(s);

    // The `boolean` type has only two values named `true` and `false`.
    // The implicit initial value of the `boolean` type is `false`.
    boolean b = true;
    io:println(b);
}
