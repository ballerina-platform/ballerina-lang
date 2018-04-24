import ballerina/io;

function main(string... args) {
    //A 'float' to 'int' is a conversion where you will possible lost some information.
    //But the conversion is always considered safe, since the conversion can never fail at runtime. 
    float f = 10.0;
    var i = <int>f;
    io:println(i);

    //An 'int' to 'string' conversion is always safe.
    int intVal = 45;
    var strVal = <string>intVal;

    //A 'string' to 'int' conversion is not always safe. Therefore we consider it as an unsafe conversion.
    //The compiler enforces the user to assign the result of conversion expression to an int|error union typed variable.
    //The "error" typed variable represents an error that occurs during the type conversion.
    strVal = "Sri Lanka";
    var intResult = <int>strVal;
    match intResult {
        int value => {
            io:println(value);
        }
        error err => {
            io:println("error: " + err.message);
        }
    }

    //If you know that this conversion will always be successful, you can ignore the error as follows.
    strVal = "5";
    intResult = check <int>strVal;
    io:println(intResult);

    //A 'boolean' to 'int' conversion is always safe. You get 0 for a 'false' value and 1 for a 'true' value.
    boolean boolVal = true;
    intVal = <int>boolVal;
    io:println(intVal);

    //This is an 'int' to 'boolean' conversion. The boolean value will be 'false' only if the int value is 0.
    intVal = -10;
    boolVal = <boolean>intVal;
    io:println(boolVal);

    //This is a 'string' to 'boolean' conversion.
    strVal = "true";
    boolVal = <boolean>strVal;
    io:println(boolVal);

    //Here we assign a 'float' type value to a variable of type 'any'.
    any a = 3.14;
    //Here is how you can cast an 'any' type variable to the 'float' type.
    //This conversion is unsafe, because the value of the variable 'a' is unknown till runtime.
    //Therefore the type after the cast expression would be of the union type 'float|error'.
    //A check expression can be used to extract the value removing the error from the union type as shown below.
    float af = check <float>a;
    io:println(af);
}
