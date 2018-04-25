import ballerina/reflect;

@final string assertFailureErrorCategory = "assert-failure";

@Description{value:"Creates an AssertError with custom message and category"}
@Param{value:"errorMessage: Custom message for the ballerina error"}
@Param{value:"category: error category"}
function createBallerinaError (string errorMessage, string category) returns (error) {
    error e = { message : errorMessage };
    return e;
}

@Description{value:"Asserts whether the given condition is true.
                  If it is not, a AssertError is thrown with the given errorMessage"}
@Param{value:"condition: Boolean condition to evaluate"}
@Param{value:"msg: Assertion error message"}
public function assertTrue(boolean condition, string msg = "Assertion Failed!") {
    if (!condition) {
        throw createBallerinaError(msg, assertFailureErrorCategory);
    }
}

@Description{value:"Asserts whether the given condition is false.
                  If it is not, a AssertError is thrown with the given errorMessage"}
@Param{value:"condition: Boolean condition to evaluate"}
@Param{value:"errorMessage: Assertion error message"}
public function assertFalse(boolean condition, string msg = "Assertion Failed!") {
    if (condition) {
        throw createBallerinaError(msg, assertFailureErrorCategory);
    }
}

@Description{value:"Asserts whether the given values are equal. If it is not, an AssertError is thrown with the given errorMessage."}
@Param{value:"expected: Expected value"}
@Param{value:"actual: Actual value"}
@Param{value:"msg: Assertion error message"}
public function assertEquals(any expected, any actual, string msg = "Assertion Failed!") {
    if (!reflect:equals(expected,actual)) {
        string expectedStr = <string> expected;
        string actualStr = <string>actual;
        string errorMsg = string `{{msg}}: expected {{expectedStr}} but found {{actualStr}}`;
        throw createBallerinaError(errorMsg, assertFailureErrorCategory);
    }
}

@Description{value:"Asserts whether the given values are not equal. If it is equal, an AssertError is thrown with the given errorMessage."}
@Param{value:"actual: Actual value"}
@Param{value:"expected: Expected value"}
@Param{value:"msg: Assertion error message"}
public function assertNotEquals(any expected, any actual, string msg = "Assertion Failed!") {
    if (reflect:equals(actual,expected)) {
        string expectedStr = <string> expected;
        string actualStr = <string> actual;
        string errorMsg = string `{{msg}}: expected the actual value not to be {{expectedStr}}`;
        throw createBallerinaError(errorMsg, assertFailureErrorCategory);
    }
}

@Description{value:"Assert failure is triggered based on user discretion.
                  AssertError is thrown with the given errorMessage"}
@Param{value:"msg: Assertion error message"}
public function assertFail(string msg = "Test Failed!") {
    throw createBallerinaError(msg, assertFailureErrorCategory);
}
