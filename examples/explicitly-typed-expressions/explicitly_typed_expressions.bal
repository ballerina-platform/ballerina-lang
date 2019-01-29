import ballerina/io;

public function main() {
    // Specifying `string` as the explicit type for an `int` will produce the `string` representation of the `int`.
    int intVal = 45;
    var strVal = <string>intVal;
    io:println(strVal);

    // Specifying `int` as the explicit type for a `boolean` would result in the conversion of `false` as `0` and `true`
    // as `1`.
    boolean boolVal = true;
    intVal = <int>boolVal;
    io:println(intVal);

    // With `boolean` as the `explicit` type for a `decimal`, the boolean value would be `false` only if the `decimal`
    // value is `0`.
    decimal decimalVal = -10;
    boolVal = <boolean>decimalVal;
    io:println(boolVal);
}
