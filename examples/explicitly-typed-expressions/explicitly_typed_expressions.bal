import ballerina/io;

public function main() {
    // A `float` to `int` conversion can result in some of the information getting lost.
    // However, this type of conversion is always considered safe because the conversion can never fail at runtime.
    float f = 10.0;
    var i = <int>f;
    io:println(i);

    // An `int` to `string` conversion is always considered safe.
    int intVal = 45;
    var strVal = <string>intVal;

    // A `boolean` to `int` conversion is always considered safe. In such conversions, `0` represents a `false`
    // value, and any other value represents a `true` value.
    boolean boolVal = true;
    intVal = <int>boolVal;
    io:println(intVal);

    // This is an `int` to `boolean` conversion. The boolean value is `false` only if the int value is `0`.
    intVal = -10;
    boolVal = <boolean>intVal;
    io:println(boolVal);

    // This is a `string` to `boolean` conversion.
    // This conversion is safe because `string true` always evaluates to `boolean true` and any other `string` value
    // evaluates to `false`.
    strVal = "true";
    boolean|error strBoolVal = boolean.convert(strVal);
    io:println(strBoolVal);

    // This assigns a value of the `float` type to a variable of the `any` type.
    any a = 3.14;

    // This shows how to assert a variable of the `any` type to the `float` type.
    // This assertion panics if the variable is not of type `float`.
    float af = <float>a;
    io:println(af);
}
