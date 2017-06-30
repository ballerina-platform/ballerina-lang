import ballerina.lang.system;
import ballerina.lang.errors;

function main (string[] args) {
    // 'strVar' holds a string variable.
    var strVar = "Hello!";
    system:println("'helloVar' variable value - " + strVar);

    // 'intVar' holds an int variable.
    var intVar = 100;
    system:println("'intVar' variable value - " + intVar);

    // Cast string value "not an int" to int value, this returns a cast error.
    var _ , error = castToInteger("not an int");
    system:println("cast error msg - " + error.msg);

    // Cast int value 81 to int value, this returns a cast result without error.
    var castResult, _ = castToInteger(81);
    system:println("cast result value - " + castResult);
}

@doc:Description {value:"Cast function accepts 'any' as a param value, returns cast result and cast error."}
function castToInteger (any age) (int, errors:TypeCastError) {
    int castResult;
    errors:TypeCastError error;
    castResult, error = (int)age;
    return castResult, error;
}