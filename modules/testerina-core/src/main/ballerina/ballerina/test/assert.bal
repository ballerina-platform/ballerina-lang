package ballerina.test;

import ballerina.doc;
import ballerina.lang.errors;

const string assertFailureErrorCategory = "assert-failure";

const string arraysNotEqualMessage = "Arrays are not equal";
const string arrayLengthsMismatchMessage = " (Array lengths are not the same)";

struct AssertError {
    string msg;
    errors:Error cause;
    string category;
}

@doc:Description{value:"Creates a AssertError with custom message and category"}
@doc:Param{value:"errorMessage: Custom message for the ballerina error"}
@doc:Param{value:"category: error category"}
function createBallerinaError (string errorMessage, string category) (AssertError) {
    AssertError e = { msg : errorMessage, category : category };
    return e;
}

@doc:Description{value:"Asserts whether the given condition is true.
                  If it is not, a AssertError is thrown with the given errorMessage"}
@doc:Param{value:"condition: Boolean condition to evaluate"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertTrue(boolean condition, string errorMessage) {
    if (!condition) {
        if (errorMessage == "") {
            errorMessage = "Assert Failed";
        }
        throw createBallerinaError(errorMessage, assertFailureErrorCategory);
    }
}

@doc:Description{value:"Asserts whether the given condition is false.
                  If it is not, a AssertError is thrown with the given errorMessage"}
@doc:Param{value:"condition: Boolean condition to evaluate"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertFalse(boolean condition, string errorMessage) {
    if (condition) {
        if (errorMessage == "") {
            errorMessage = "Assert Failed";
        }
        throw createBallerinaError(errorMessage, assertFailureErrorCategory);
    }
}

@doc:Description{value:"Asserts whether the given string values are equal.
                  If it is not, a AssertError is thrown with the given errorMessage."}
@doc:Param{value:"actual: Actual string value"}
@doc:Param{value:"expected: Expected string value"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertStringEquals(string actual, string expected, string errorMessage) {
    if (actual != expected) {
        if(errorMessage != ""){
            errorMessage = errorMessage + " ";
        }
        string msg = string `{{errorMessage}}expected [{{expected}}] but found [{{actual}}]`;
        throw createBallerinaError(msg, assertFailureErrorCategory);
    }
}

@doc:Description{value:"Asserts whether the given integer values are equal.
                  If it is not, a AssertError is thrown with the given errorMessage."}
@doc:Param{value:"actual: Actual integer value"}
@doc:Param{value:"expected: Expected integer value"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertIntEquals(int actual, int expected, string errorMessage) {
    if (actual != expected) {
        if(errorMessage != ""){
            errorMessage = errorMessage + " ";
        }
        string msg = string `{{errorMessage}}expected [{{expected}}] but found [{{actual}}]`;
        throw createBallerinaError(msg, assertFailureErrorCategory);
    }
}

@doc:Description{value:"Asserts whether the given float values are equal.
                  If it is not, a AssertError is thrown with the given errorMessage."}
@doc:Param{value:"actual: Actual float value"}
@doc:Param{value:"expected: Expected float value"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertFloatEquals(float actual, float expected, string errorMessage) {
    if (actual != expected) {
        if(errorMessage != ""){
            errorMessage = errorMessage + " ";
        }
        string msg = string `{{errorMessage}}expected [{{expected}}] but found [{{actual}}]`;
        throw createBallerinaError(msg, assertFailureErrorCategory);
    }
}

@doc:Description{value:"Asserts whether the given boolean values are equal.
                  If it is not, a AssertError is thrown with the given errorMessage."}
@doc:Param{value:"actual: Actual boolean value"}
@doc:Param{value:"expected: Expected boolean value"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertBooleanEquals(boolean actual, boolean expected, string errorMessage) {
    if (actual != expected) {
        if(errorMessage != ""){
            errorMessage = errorMessage + " ";
        }
        string msg = string `{{errorMessage}}expected [{{expected}}] but found [{{actual}}]`;
        throw createBallerinaError(msg, assertFailureErrorCategory);
    }
}

@doc:Description{value:"Asserts whether the given string arrays are equal.
                  If it is not, a AssertError is thrown with the given errorMessage
                  including differed string values and array index."}
@doc:Param{value:"actual: Actual string array"}
@doc:Param{value:"expected: Expected string array"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertStringArrayEquals(string[] actual, string[] expected, string errorMessage) {
    if (errorMessage == "") {
        errorMessage = arraysNotEqualMessage;
    }
    if (actual.length != expected.length) {
        throw createBallerinaError(errorMessage + arrayLengthsMismatchMessage, assertFailureErrorCategory);
    } else {
        if (expected.length > 0) {
            int i = 0;
            while (i < expected.length) {
                try {
                    assertStringEquals(actual[i], expected[i], "");
                } catch (AssertError e) {
                    if (e.category == assertFailureErrorCategory) {
                        throw createBallerinaError(
                                                      errorMessage + ". " + e.msg +
                                                      " (at index " + i + ") " ,
                                                      assertFailureErrorCategory);
                    }
                }
                i = i + 1;
            }
        }
    }
}

@doc:Description{value:"Asserts whether the given float arrays are equal.
                  If it is not, a AssertError is thrown with the given errorMessage
                  including differed float values and array index."}
@doc:Param{value:"actual: Actual float array"}
@doc:Param{value:"expected: Expected float array"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertFloatArrayEquals(float[] actual, float[] expected, string errorMessage) {
    if (errorMessage == "") {
        errorMessage = arraysNotEqualMessage;
    }
    if (actual.length != expected.length) {
        throw createBallerinaError(errorMessage + arrayLengthsMismatchMessage, assertFailureErrorCategory);
    } else {
        if (expected.length > 0) {
            int i = 0;
            while (i < expected.length) {
                try {
                    assertFloatEquals(actual[i], expected[i], "");
                } catch (AssertError e) {
                    if (e.category == assertFailureErrorCategory) {
                        throw createBallerinaError(
                                                      errorMessage + ". " + e.msg +
                                                      " (at index " + i + ") " ,
                                                      assertFailureErrorCategory);
                    }
                }
                i = i + 1;
            }
        }
    }
}

@doc:Description{value:"Asserts whether the given integer arrays are equal.
                  If it is not, a AssertError is thrown with the given errorMessage
                  including differed integer values and array index."}
@doc:Param{value:"actual: Actual integer array"}
@doc:Param{value:"expected: Expected integer array"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertIntArrayEquals(int[] actual, int[] expected, string errorMessage) {
    if (errorMessage == "") {
        errorMessage = arraysNotEqualMessage;
    }
    if (actual.length != expected.length) {
        throw createBallerinaError(errorMessage + arrayLengthsMismatchMessage, assertFailureErrorCategory);
    } else {
        if (expected.length > 0) {
            int i = 0;
            while (i < expected.length) {
                try {
                    assertIntEquals(actual[i], expected[i], "");
                } catch (AssertError e) {
                    if (e.category == assertFailureErrorCategory) {
                        throw createBallerinaError(
                                                      errorMessage + ". " + e.msg +
                                                      " (at index " + i + ") " ,
                                                      assertFailureErrorCategory);
                    }
                }
                i = i + 1;
            }
        }
    }
}

@doc:Description{value:"Assert failure is triggered based on user discretion.
                  AssertError is thrown with the given errorMessage"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertFail(string errorMessage) {
    throw createBallerinaError(errorMessage, assertFailureErrorCategory);
}

