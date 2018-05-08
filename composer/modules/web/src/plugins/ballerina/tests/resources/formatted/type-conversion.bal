import ballerina/lang.system;

function main (string... args) {
    //floattype to int conversion is considered as a type conversion in Ballerina,
    // because the underlying representation of the value changes with this conversion.
    float f = 10.0;
    var i, _ = <int>f;
    system:println(i);

    //'int' to 'string' conversion is always safe.
    int intVal = 45;
    var strVal = <string>intVal;

    //'string' to 'int' conversion is not always safe. Therefore we consider it as an unsafe conversion.
    //Compiler will enforce you to use multi-return conversion expression as follows.
    strVal = "Sri Lanka";
    var intVal, conversionErr = <int>strVal;
    if (conversionErr != null) {
        system:println("error: " + conversionErr.msg);
    }

    //If you know that this conversion will always be successful, you can ignore the error as follows.
    strVal = "5";
    var val, _ = <int>strVal;
    system:println(val);

    //'boolean' to 'int' conversion is always safe. You get 0 for 'false' value and 1 for 'true' value;
    boolean boolVal = true;
    intVal = <int>boolVal;
    system:println(intVal);

    //'int' to 'boolean' conversion. The boolean value will be 'false' only if the int value is 0;
    intVal = -10;
    boolVal = <boolean>intVal;
    system:println(boolVal);

    //'string' to 'boolean conversion'
    strVal = "true";
    boolVal, _ = <boolean>strVal;
    system:println(boolVal);
}