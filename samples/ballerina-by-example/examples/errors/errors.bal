import ballerina.doc;
import ballerina.lang.errors;
import ballerina.lang.system;

@doc:Description {value:"The Developer can create their own error types. Defining both 'msg' and 'cause' struct fields make this struct structurally equivalent to ballerina.lang.errors:Error struct. Additional fields explain more about the error occurred."}
struct CalculationError {
    string msg;
    errors:Error cause;
    string operation;
}

@doc:Description {value:"By convention, any returned error should be the last return value."}
function calculate (int x, int y, string operation) (int result, CalculationError error) {
    if (operation != "add") {
        //Constructing a CalculationError by explaining the error occurred.
        error = {msg:"Not supported Operation", operation:operation};
        return 0, error;
    }
    result = x + y;
    //Assigning a null value to the error indicates that there was no error.
    error = null;
    return;
}

function main (string[] args) {
    int result;
    //The Developer can ignore the error return by the function (or any return value) using the underscore "_".
    result, _ = calculate(5, 10, "add");
    system:println("result 1 is " + result);

    CalculationError error;
    result, error = calculate(5, 10, "multiply");
    //Checks for errors.
    if (error != null) {
        //Recover from the error.
        system:println("Error1, " + error.msg + ", operation : " + error.operation);
        result = 5 * 10;
    }
    system:println("result 2 is " + result);

    result, error = calculate(5, 10, "subtract");
    system:println("Error2, " + error.msg + ", operation : " + error.operation);
    system:println("result 3 is " + result);
    // It is not always a good idea to ignore errors, as it may lead to other errors.
    int value = 100 / result;
    system:println("final value " + value);
}