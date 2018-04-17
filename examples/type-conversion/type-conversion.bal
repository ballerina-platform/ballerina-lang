import ballerina/io;

function main (string... args) {
    //A 'float' to 'int' conversion is considered as a type conversion in Ballerina
    // because the underlying representation of the value changes with this conversion.
    float f = 10.0;
    var i = <int>f;
    io:println(i);

    //An 'int' to 'string' conversion is always safe.
    int intVal = 45;
    var strVal = <string>intVal;

    //A 'string' to 'int' conversion is not always safe. Therefore we consider it as an unsafe conversion.
    //The compiler enforces use of the multi-return conversion expression as follows.
    //A TypeConversionError represents an error that occurs during a TypeConversion.
    strVal = "Sri Lanka";
    var intResult = <int>strVal;
    match intResult{
        int value =>{
            io:println(value);
        }
        error err =>{
            io:println("error: " + err.message);
        }
    }

    //If you know that this conversion will always be successful, you can ignore the error as follows.
    strVal = "5";
    var stringResult = <int> strVal;
    match stringResult{
        int value =>{
            io:println(value);
        }
        error err =>{
            io:println("Error occurred while casting" + err.message);
        }
    }

    //A 'boolean' to 'int' conversion is always safe. You get 0 for a 'false' value and 1 for a 'true' value.
    boolean boolVal = true;
    intVal = <int>boolVal;
    io:println(intVal);

    //This is an 'int' to 'boolean' conversion. The boolean value will be 'false' only if the int value is 0.
    intVal = -10;
    boolVal = <boolean> intVal;
    io:println(boolVal);

    //This is a 'string' to 'boolean' conversion.
    strVal = "true";
    boolVal = <boolean>strVal;
    io:println(boolVal);
}
