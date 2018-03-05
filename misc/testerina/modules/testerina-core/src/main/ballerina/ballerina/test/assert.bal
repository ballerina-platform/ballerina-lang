package ballerina.test;
import ballerina.reflect;

const string assertFailureErrorCategory = "assert-failure";

const string arraysNotEqualMessage = "Arrays are not equal";
const string arrayLengthsMismatchMessage = " (Array lengths are not the same)";

@Description{value:"The error struct for assertion errors"}
@Field{value:"The assertion error message"}
@Field{value:"The error which caused the assertion error"}
@Field{value:"The assert error category"}
public struct AssertError {
    string message;
    error[] cause;
    string category;
}

@Description{value:"Creates an AssertError with custom message and category"}
@Param{value:"errorMessage: Custom message for the ballerina error"}
@Param{value:"category: error category"}
public function createBallerinaError (string errorMessage, string category) (AssertError) {
    AssertError e = { message : errorMessage, category : category };
    return e;
}

@Description{value:"Asserts whether the given condition is true.
                  If it is not, a AssertError is thrown with the given errorMessage"}
@Param{value:"condition: Boolean condition to evaluate"}
@Param{value:"errorMessage: Assertion error message"}
public function assertTrue(boolean condition, string errorMessage) {
    if (!condition) {
        if (errorMessage == "") {
            errorMessage = "Assert Failed";
        }
        throw createBallerinaError(errorMessage, assertFailureErrorCategory);
    }
}

@Description{value:"Asserts whether the given condition is false.
                  If it is not, a AssertError is thrown with the given errorMessage"}
@Param{value:"condition: Boolean condition to evaluate"}
@Param{value:"errorMessage: Assertion error message"}
public function assertFalse(boolean condition, string errorMessage) {
    if (condition) {
        if (errorMessage == "") {
            errorMessage = "Assert Failed";
        }
        throw createBallerinaError(errorMessage, assertFailureErrorCategory);
    }
}

@Description{value:"Asserts whether the given values are equal. If it is not, an AssertError is thrown with the given errorMessage."}
@Param{value:"actual: Actual value"}
@Param{value:"expected: Expected value"}
@Param{value:"errorMessage: Assertion error message"}
public function assertEquals(any actual, any expected, string errorMessage) {
    if (!reflect:equals(actual,expected)) {
        if(errorMessage != ""){
            errorMessage = errorMessage + " ";
        }
        string expectedStr = <string> expected;
        string actualStr = <string> actual;
        string msg = string `{{errorMessage}}: expected {{expectedStr}} but found {{actualStr}}`;
        throw createBallerinaError(msg, assertFailureErrorCategory);
    }
}

@Description{value:"Asserts whether the given values are not equal. If it is equal, an AssertError is thrown with the given errorMessage."}
@Param{value:"actual: Actual value"}
@Param{value:"expected: Expected value"}
@Param{value:"errorMessage: Assertion error message"}
public function assertNotEquals(any actual, any expected, string errorMessage) {
    if (reflect:equals(actual,expected)) {
        if(errorMessage != ""){
            errorMessage = errorMessage + " ";
        }
        string expectedStr = <string> expected;
        string actualStr = <string> actual;
        string msg = string `{{errorMessage}}: expected the actual value not to be {{expectedStr}}`;
        throw createBallerinaError(msg, assertFailureErrorCategory);
    }
}

@Description{value:"Assert failure is triggered based on user discretion.
                  AssertError is thrown with the given errorMessage"}
@Param{value:"errorMessage: Assertion error message"}
public function assertFail(string errorMessage) {
    throw createBallerinaError(errorMessage, assertFailureErrorCategory);
}

