package ballerina.test;

const string assertFailureErrorCategory = "assert-failure";

const string arraysNotEqualMessage = "Arrays are not equal";
const string arrayLengthsMismatchMessage = " (Array lengths are not the same)";

@Description{value:"The error struct for assertion errors"}
@Field{value:"The assertion error message"}
@Field{value:"The error which caused the assertion error"}
@Field{value:"The assert error category"}
public struct AssertError {
    string message;
    error cause;
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

@Description{value:"Asserts whether the given string values are equal.
                  If it is not, a AssertError is thrown with the given errorMessage."}
@Param{value:"actual: Actual string value"}
@Param{value:"expected: Expected string value"}
@Param{value:"errorMessage: Assertion error message"}
public function assertStringEquals(string actual, string expected, string errorMessage) {
    if (actual != expected) {
        if(errorMessage != ""){
            errorMessage = errorMessage + " ";
        }
        string msg = string `{{errorMessage}}expected [{{expected}}] but found [{{actual}}]`;
        throw createBallerinaError(msg, assertFailureErrorCategory);
    }
}

@Description{value:"Asserts whether the given integer values are equal.
                  If it is not, a AssertError is thrown with the given errorMessage."}
@Param{value:"actual: Actual integer value"}
@Param{value:"expected: Expected integer value"}
@Param{value:"errorMessage: Assertion error message"}
public function assertIntEquals(int actual, int expected, string errorMessage) {
    if (actual != expected) {
        if(errorMessage != ""){
            errorMessage = errorMessage + " ";
        }
        string msg = string `{{errorMessage}}expected [{{expected}}] but found [{{actual}}]`;
        throw createBallerinaError(msg, assertFailureErrorCategory);
    }
}

@Description{value:"Asserts whether the given float values are equal.
                  If it is not, a AssertError is thrown with the given errorMessage."}
@Param{value:"actual: Actual float value"}
@Param{value:"expected: Expected float value"}
@Param{value:"errorMessage: Assertion error message"}
public function assertFloatEquals(float actual, float expected, string errorMessage) {
    if (actual != expected) {
        if(errorMessage != ""){
            errorMessage = errorMessage + " ";
        }
        string msg = string `{{errorMessage}}expected [{{expected}}] but found [{{actual}}]`;
        throw createBallerinaError(msg, assertFailureErrorCategory);
    }
}

@Description{value:"Asserts whether the given boolean values are equal.
                  If it is not, a AssertError is thrown with the given errorMessage."}
@Param{value:"actual: Actual boolean value"}
@Param{value:"expected: Expected boolean value"}
@Param{value:"errorMessage: Assertion error message"}
public function assertBooleanEquals(boolean actual, boolean expected, string errorMessage) {
    if (actual != expected) {
        if(errorMessage != ""){
            errorMessage = errorMessage + " ";
        }
        string msg = string `{{errorMessage}}expected [{{expected}}] but found [{{actual}}]`;
        throw createBallerinaError(msg, assertFailureErrorCategory);
    }
}

@Description{value:"Asserts whether the given string arrays are equal.
                  If it is not, a AssertError is thrown with the given errorMessage
                  including differed string values and array index."}
@Param{value:"actual: Actual string array"}
@Param{value:"expected: Expected string array"}
@Param{value:"errorMessage: Assertion error message"}
public function assertStringArrayEquals(string[] actual, string[] expected, string errorMessage) {
    if (errorMessage == "") {
        errorMessage = arraysNotEqualMessage;
    }
    if (lengthof actual != lengthof expected) {
        throw createBallerinaError(errorMessage + arrayLengthsMismatchMessage, assertFailureErrorCategory);
    } else {
        if (lengthof expected > 0) {
            int i = 0;
            while (i < lengthof expected) {
                try {
                    assertStringEquals(actual[i], expected[i], "");
                } catch (AssertError e) {
                    if (e.category == assertFailureErrorCategory) {
                        throw createBallerinaError(
                                                      errorMessage + ". " + e.message +
                                                      " (at index " + i + ") " ,
                                                      assertFailureErrorCategory);
                    }
                }
                i = i + 1;
            }
        }
    }
}

@Description{value:"Asserts whether the given float arrays are equal.
                  If it is not, a AssertError is thrown with the given errorMessage
                  including differed float values and array index."}
@Param{value:"actual: Actual float array"}
@Param{value:"expected: Expected float array"}
@Param{value:"errorMessage: Assertion error message"}
public function assertFloatArrayEquals(float[] actual, float[] expected, string errorMessage) {
    if (errorMessage == "") {
        errorMessage = arraysNotEqualMessage;
    }
    if (lengthof actual != lengthof expected) {
        throw createBallerinaError(errorMessage + arrayLengthsMismatchMessage, assertFailureErrorCategory);
    } else {
        if (lengthof expected > 0) {
            int i = 0;
            while (i < lengthof expected) {
                try {
                    assertFloatEquals(actual[i], expected[i], "");
                } catch (AssertError e) {
                    if (e.category == assertFailureErrorCategory) {
                        throw createBallerinaError(
                                                      errorMessage + ". " + e.message +
                                                      " (at index " + i + ") " ,
                                                      assertFailureErrorCategory);
                    }
                }
                i = i + 1;
            }
        }
    }
}

@Description{value:"Asserts whether the given integer arrays are equal.
                  If it is not, a AssertError is thrown with the given errorMessage
                  including differed integer values and array index."}
@Param{value:"actual: Actual integer array"}
@Param{value:"expected: Expected integer array"}
@Param{value:"errorMessage: Assertion error message"}
public function assertIntArrayEquals(int[] actual, int[] expected, string errorMessage) {
    if (errorMessage == "") {
        errorMessage = arraysNotEqualMessage;
    }
    if (lengthof actual != lengthof expected) {
        throw createBallerinaError(errorMessage + arrayLengthsMismatchMessage, assertFailureErrorCategory);
    } else {
        if (lengthof expected > 0) {
            int i = 0;
            while (i < lengthof expected) {
                try {
                    assertIntEquals(actual[i], expected[i], "");
                } catch (AssertError e) {
                    if (e.category == assertFailureErrorCategory) {
                        throw createBallerinaError(
                                                      errorMessage + ". " + e.message +
                                                      " (at index " + i + ") " ,
                                                      assertFailureErrorCategory);
                    }
                }
                i = i + 1;
            }
        }
    }
}

@Description{value:"Assert failure is triggered based on user discretion.
                  AssertError is thrown with the given errorMessage"}
@Param{value:"errorMessage: Assertion error message"}
public function assertFail(string errorMessage) {
    throw createBallerinaError(errorMessage, assertFailureErrorCategory);
}

