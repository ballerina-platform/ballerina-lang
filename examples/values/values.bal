import ballerina/io;

@Description {value: "The types int, float, string, boolean, blob, and nil are called simple basic types
because they are basic types with only simple values. Simple values are always immutable."}
function main (string... args) {
    //The `int` type represents the set of 64-bit signed integers.
    //The implicit initial value of `int` type is 0.
    int i = 10;
    io:println(i);

    //The `float` type represents the set of double precision IEEE 754 floating point numbers.
    //The implicit initial value of `float` type is +0.0.
    float f = 20.0;
    io:println(f);

    //The `string` type represents the set of sequences of Unicode code points.
    //The implicit initial value of `string` type is +0.0.
    string s = "Ballerina";
    io:println(s);

    //The `boolean` type has only two values: `true` and `faluse`.
    //The implicit initial value of `boolean` type is +0.0.
    boolean b = true;
    io:println(b);

    //The `boolean` type has only two values: `true` and `faluse`.
    //The implicit initial value of `boolean` type is +0.0.
    blob bl = s.toBlob("UTF-8");
    io:println(bl);
}
