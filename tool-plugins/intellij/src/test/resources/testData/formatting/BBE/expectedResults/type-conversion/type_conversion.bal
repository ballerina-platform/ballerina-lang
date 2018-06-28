import ballerina/io;

function main(string... args) {
    // A `float` to `int` conversion can result in some of the information getting lost.
    // However, this type of conversion is always considered safe because the conversion can never fail at runtime.
    float f = 10.0;
    var i = <int>f;
    io:println(i);

    // An `int` to `string` conversion is always considered safe.
    int intVal = 45;
    var strVal = <string>intVal;

    // A `string` to `int` conversion is considered unsafe.
    // The compiler requires the user to assign the result of conversion expression to an `int|error` union typed variable.
    // The `error` typed variable represents an error that occurs during the type conversion.
    strVal = "Sri Lanka";
    var intResult = <int>strVal;
    match intResult {
        int value => io:println(value);
        error err => io:println("error: " + err.message);
    }

    // A `boolean` to `int` conversion is always considered safe. In such conversions, `0` represents a `false` value, and `1` represents a `true` value.
    boolean boolVal = true;
    intVal = <int>boolVal;
    io:println(intVal);

    // This is an `int` to `boolean` conversion. The boolean value is `false` only if the int value is `0`.
    intVal = -10;
    boolVal = <boolean>intVal;
    io:println(boolVal);

    // This is a `string` to `boolean` conversion.
    strVal = "true";
    boolVal = <boolean>strVal;
    io:println(boolVal);

    // This assigns a value of the `float` type to a variable of the `any` type.
    any a = 3.14;

    // This shows how to convert a variable of the `any` type to the `float` type.
    // This conversion is unsafe because the value of the `a` variable is unknown.
    float? af = check <float>a;
    io:println(af);
}
