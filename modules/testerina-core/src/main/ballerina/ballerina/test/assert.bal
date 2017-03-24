package ballerina.test;

import ballerina.doc;
import ballerina.lang.exceptions;
import ballerina.lang.arrays as array;

const string assertFailureErrorCategory = "assert-failure";

const string arraysNotEqualMessage = "Arrays are not equal";
const string arrayLengthsMismatchMessage = " (Array lengths are not the same)";

@doc:Description{value:"Creates a BallerinaException with custom message and category"}
@doc:Param{value:"errorMessage: Custom message for the ballerina exception"}
@doc:Param{value:"category: Exception category"}
function createBallerinaException (string errorMessage, string category) (exception) {
    exception e = {};
    exceptions:setMessage(e, errorMessage);
    exceptions:setCategory(e, category);
    return e;
}

@doc:Description{value:"Asserts whether the given condition is true.
                  If it is not, a BallerinaException is thrown with the given errorMessage"}
@doc:Param{value:"condition: Boolean condition to evaluate"}
function assertTrue(boolean condition) {
    assertTrue(condition, "");
}

@doc:Description{value:"Asserts whether the given condition is true.
                  If it is not, a BallerinaException is thrown with the given errorMessage"}
@doc:Param{value:"condition: Boolean condition to evaluate"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertTrue(boolean condition, string errorMessage) {
    if (!condition) {
        if (errorMessage == "") {
            errorMessage = "Assert Failed";
        }
        throw createBallerinaException(errorMessage, assertFailureErrorCategory);
    }
}

@doc:Description{value:"Asserts whether the given condition is false.
                  If it is not, a BallerinaException is thrown with the given errorMessage"}
@doc:Param{value:"condition: Boolean condition to evaluate"}
function assertFalse(boolean condition) {
    assertFalse(condition, "");
}

@doc:Description{value:"Asserts whether the given condition is false.
                  If it is not, a BallerinaException is thrown with the given errorMessage"}
@doc:Param{value:"condition: Boolean condition to evaluate"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertFalse(boolean condition, string errorMessage) {
    if (condition) {
        if (errorMessage == "") {
            errorMessage = "Assert Failed";
        }
        throw createBallerinaException(errorMessage, assertFailureErrorCategory);
    }
}

@doc:Description{value:"Asserts whether the given string values are equal.
                  If it is not, a BallerinaException is thrown with the given errorMessage."}
@doc:Param{value:"actual: Actual string value"}
@doc:Param{value:"expected: Expected string value"}
function assertEquals(string actual, string expected) {
    assertEquals(actual, expected, "");
}

@doc:Description{value:"Asserts whether the given string values are equal.
                  If it is not, a BallerinaException is thrown with the given errorMessage."}
@doc:Param{value:"actual: Actual string value"}
@doc:Param{value:"expected: Expected string value"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertEquals(string actual, string expected, string errorMessage) {
    if (actual != expected) {
        if (errorMessage == "") {
            errorMessage = "String not equal: expected: " + expected + " and actual: "+ actual;
        }
        throw createBallerinaException(errorMessage, assertFailureErrorCategory);
    }
}

@doc:Description{value:"Asserts whether the given integer values are equal.
                  If it is not, a BallerinaException is thrown with the given errorMessage."}
@doc:Param{value:"actual: Actual integer value"}
@doc:Param{value:"expected: Expected integer value"}
function assertEquals(int actual, int expected) {
    assertEquals(actual, expected, "");
}

@doc:Description{value:"Asserts whether the given integer values are equal.
                  If it is not, a BallerinaException is thrown with the given errorMessage."}
@doc:Param{value:"actual: Actual integer value"}
@doc:Param{value:"expected: Expected integer value"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertEquals(int actual, int expected, string errorMessage) {
    if (actual != expected) {
        if (errorMessage == "") {
            errorMessage = "Integer not equal: expected: " + expected + " and actual: "+ actual;
        }
        throw createBallerinaException(errorMessage, assertFailureErrorCategory);
    }
}

@doc:Description{value:"Asserts whether the given float values are equal.
                  If it is not, a BallerinaException is thrown with the given errorMessage."}
@doc:Param{value:"actual: Actual float value"}
@doc:Param{value:"expected: Expected float value"}
function assertEquals(float actual, float expected) {
    assertEquals(actual, expected, "");
}

@doc:Description{value:"Asserts whether the given float values are equal.
                  If it is not, a BallerinaException is thrown with the given errorMessage."}
@doc:Param{value:"actual: Actual float value"}
@doc:Param{value:"expected: Expected float value"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertEquals(float actual, float expected, string errorMessage) {
    if (actual != expected) {
        if (errorMessage == "") {
            errorMessage = "Float not equal: expected: " + expected + " and actual: "+ actual;
        }
        throw createBallerinaException(errorMessage, assertFailureErrorCategory);
    }
}

@doc:Description{value:"Asserts whether the given boolean values are equal.
                  If it is not, a BallerinaException is thrown with the given errorMessage."}
@doc:Param{value:"actual: Actual boolean value"}
@doc:Param{value:"expected: Expected boolean value"}
function assertEquals(boolean actual, boolean expected) {
    assertEquals(actual, expected, "");
}

@doc:Description{value:"Asserts whether the given boolean values are equal.
                  If it is not, a BallerinaException is thrown with the given errorMessage."}
@doc:Param{value:"actual: Actual boolean value"}
@doc:Param{value:"expected: Expected boolean value"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertEquals(boolean actual, boolean expected, string errorMessage) {
    if (actual != expected) {
        if (errorMessage == "") {
            errorMessage = "Boolean not equal: expected: " + expected + " and actual: "+ actual;
        }
        throw createBallerinaException(errorMessage, assertFailureErrorCategory);
    }
}

@doc:Description{value:"Asserts whether the given string arrays are equal.
                  If it is not, a BallerinaException is thrown with the given errorMessage
                  including differed string values and array index."}
@doc:Param{value:"actual: Actual string array"}
@doc:Param{value:"expected: Expected string array"}
function assertEquals(string[] actual, string[] expected) {
    assertEquals(actual, expected, "");
}

@doc:Description{value:"Asserts whether the given string arrays are equal.
                  If it is not, a BallerinaException is thrown with the given errorMessage
                  including differed string values and array index."}
@doc:Param{value:"actual: Actual string array"}
@doc:Param{value:"expected: Expected string array"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertEquals(string[] actual, string[] expected, string errorMessage) {
    if (errorMessage == "") {
        errorMessage = arraysNotEqualMessage;
    }
    if (array:length(actual) != array:length(expected)) {
        throw createBallerinaException(errorMessage + arrayLengthsMismatchMessage, assertFailureErrorCategory);
    } else {
        if (array:length(expected) > 0) {
            int i = 0;
            while (i < array:length(expected)) {
                try {
                    assertEquals(actual[i], expected[i]);
                } catch (exception e) {
                    if (exceptions:getCategory(e) == assertFailureErrorCategory) {
                        throw createBallerinaException(
                                                      errorMessage + ". " + exceptions:getMessage(e) +
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
                  If it is not, a BallerinaException is thrown with the given errorMessage
                  including differed float values and array index."}
@doc:Param{value:"actual: Actual float array"}
@doc:Param{value:"expected: Expected float array"}
function assertEquals(float[] actual, float[] expected) {
    assertEquals(actual, expected, "");
}

@doc:Description{value:"Asserts whether the given float arrays are equal.
                  If it is not, a BallerinaException is thrown with the given errorMessage
                  including differed float values and array index."}
@doc:Param{value:"actual: Actual float array"}
@doc:Param{value:"expected: Expected float array"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertEquals(float[] actual, float[] expected, string errorMessage) {
    if (errorMessage == "") {
        errorMessage = arraysNotEqualMessage;
    }
    if (array:length(actual) != array:length(expected)) {
        throw createBallerinaException(errorMessage + arrayLengthsMismatchMessage, assertFailureErrorCategory);
    } else {
        if (array:length(expected) > 0) {
            int i = 0;
            while (i < array:length(expected)) {
                try {
                    assertEquals(actual[i], expected[i]);
                } catch (exception e) {
                    if (exceptions:getCategory(e) == assertFailureErrorCategory) {
                        throw createBallerinaException(
                                                      errorMessage + ". " + exceptions:getMessage(e) +
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
                  If it is not, a BallerinaException is thrown with the given errorMessage
                  including differed integer values and array index."}
@doc:Param{value:"actual: Actual integer array"}
@doc:Param{value:"expected: Expected integer array"}
function assertEquals(int[] actual, int[] expected) {
    assertEquals(actual, expected, "");
}

@doc:Description{value:"Asserts whether the given integer arrays are equal.
                  If it is not, a BallerinaException is thrown with the given errorMessage
                  including differed integer values and array index."}
@doc:Param{value:"actual: Actual integer array"}
@doc:Param{value:"expected: Expected integer array"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertEquals(int[] actual, int[] expected, string errorMessage) {
    if (errorMessage == "") {
        errorMessage = arraysNotEqualMessage;
    }
    if (array:length(actual) != array:length(expected)) {
        throw createBallerinaException(errorMessage + arrayLengthsMismatchMessage, assertFailureErrorCategory);
    } else {
        if (array:length(expected) > 0) {
            int i = 0;
            while (i < array:length(expected)) {
                try {
                    assertEquals(actual[i], expected[i]);
                } catch (exception e) {
                    if (exceptions:getCategory(e) == assertFailureErrorCategory) {
                        throw createBallerinaException(
                                                      errorMessage + ". " + exceptions:getMessage(e) +
                                                      " (at index " + i + ") " ,
                                                      assertFailureErrorCategory);
                    }
                }
                i = i + 1;
            }
        }
    }
}

@doc:Description{value:"Assert failure is triggered based on user discretion."}
function assertFail() {
    throw createBallerinaException("Assert failure", assertFailureErrorCategory);
}

@doc:Description{value:"Assert failure is triggered based on user discretion.
                  BallerinaException is thrown with the given errorMessage"}
@doc:Param{value:"errorMessage: Assertion error message"}
function assertFail(string errorMessage) {
    throw createBallerinaException(errorMessage, assertFailureErrorCategory);
}

