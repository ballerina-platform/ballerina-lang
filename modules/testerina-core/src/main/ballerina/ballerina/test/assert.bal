package ballerina.test;

import ballerina.lang.exceptions;
import ballerina.lang.arrays as array;

const string assertFailureErrorCategory = "assert-failure";

const string arraysNotEqualMessage = "Arrays are not equal";
const string arrayLengthsMismatchMessage = " (Array lengths are not the same)";

@doc:Description("Creates a BallerinaException with custom message and category")
@doc:Param("message: Custom message for the ballerina exception")
@doc:Param("category: Exception category")
function createBallerinaException (string message, string category) (exception) {
    exception e = {};
    exceptions:setMessage(e, message);
    exceptions:setCategory(e, category);
    return e;
}

@doc:Description("Asserts whether the given condition is true.
                  If it is not, a BallerinaException is thrown with the given message")
@doc:Param("condition: Boolean condition to evaluate")
function assertTrue(boolean condition) {
    assertTrue(condition, "");
}

@doc:Description("Asserts whether the given condition is true.
                  If it is not, a BallerinaException is thrown with the given message")
@doc:Param("condition: Boolean condition to evaluate")
@doc:Param("message: Assertion error message")
function assertTrue(boolean condition, string message) {
    if (!condition) {
        if (message == "") {
            message = "Assert Failed";
        }
        throw createBallerinaException(message, assertFailureErrorCategory);
    }
}

@doc:Description("Asserts whether the given condition is false.
                  If it is not, a BallerinaException is thrown with the given message")
@doc:Param("condition: Boolean condition to evaluate")
function assertFalse(boolean condition) {
    assertFalse(condition, "");
}

@doc:Description("Asserts whether the given condition is false.
                  If it is not, a BallerinaException is thrown with the given message")
@doc:Param("condition: Boolean condition to evaluate")
@doc:Param("message: Assertion error message")
function assertFalse(boolean condition, string message) {
    if (condition) {
        if (message == "") {
            message = "Assert Failed";
        }
        throw createBallerinaException(message, assertFailureErrorCategory);
    }
}

@doc:Description("Asserts whether the given string values are equal.
                  If it is not, a BallerinaException is thrown with the given message.")
@doc:Param("actual: Actual string value")
@doc:Param("expected: Expected string value")
function assertEquals(string actual, string expected) {
    assertEquals(actual, expected, "");
}

@doc:Description("Asserts whether the given string values are equal.
                  If it is not, a BallerinaException is thrown with the given message.")
@doc:Param("actual: Actual string value")
@doc:Param("expected: Expected string value")
@doc:Param("message: Assertion error message")
function assertEquals(string actual, string expected, string message) {
    if (actual != expected) {
        if (message == "") {
            message = "String not equal: expected: " + expected + " and actual: "+ actual;
        }
        throw createBallerinaException(message, assertFailureErrorCategory);
    }
}

@doc:Description("Asserts whether the given integer values are equal.
                  If it is not, a BallerinaException is thrown with the given message.")
@doc:Param("actual: Actual integer value")
@doc:Param("expected: Expected integer value")
function assertEquals(int actual, int expected) {
    assertEquals(actual, expected, "");
}

@doc:Description("Asserts whether the given integer values are equal.
                  If it is not, a BallerinaException is thrown with the given message.")
@doc:Param("actual: Actual integer value")
@doc:Param("expected: Expected integer value")
@doc:Param("message: Assertion error message")
function assertEquals(int actual, int expected, string message) {
    if (actual != expected) {
        if (message == "") {
            message = "Integer not equal: expected: " + expected + " and actual: "+ actual;
        }
        throw createBallerinaException(message, assertFailureErrorCategory);
    }
}

@doc:Description("Asserts whether the given boolean values are equal.
                  If it is not, a BallerinaException is thrown with the given message.")
@doc:Param("actual: Actual boolean value")
@doc:Param("expected: Expected boolean value")
function assertEquals(boolean actual, boolean expected) {
    assertEquals(actual, expected, "");
}

@doc:Description("Asserts whether the given boolean values are equal.
                  If it is not, a BallerinaException is thrown with the given message.")
@doc:Param("actual: Actual boolean value")
@doc:Param("expected: Expected boolean value")
@doc:Param("message: Assertion error message")
function assertEquals(boolean actual, boolean expected, string message) {
    if (actual != expected) {
        if (message == "") {
            message = "Boolean not equal: expected: " + expected + " and actual: "+ actual;
        }
        throw createBallerinaException(message, assertFailureErrorCategory);
    }
}

@doc:Description("Asserts whether the given string arrays are equal.
                  If it is not, a BallerinaException is thrown with the given message
                  including differed string values and array index.")
@doc:Param("actual: Actual string array")
@doc:Param("expected: Expected string array")
function assertEquals(string[] actual, string[] expected) {
    assertEquals(actual, expected, "");
}

@doc:Description("Asserts whether the given string arrays are equal.
                  If it is not, a BallerinaException is thrown with the given message
                  including differed string values and array index.")
@doc:Param("actual: Actual string array")
@doc:Param("expected: Expected string array")
@doc:Param("message: Assertion error message")
function assertEquals(string[] actual, string[] expected, string message) {
    if (message == "") {
        message = arraysNotEqualMessage;
    }
    if (array:length(actual) != array:length(expected)) {
        throw createBallerinaException(message + arrayLengthsMismatchMessage, assertFailureErrorCategory);
    } else {
        if (array:length(expected) > 0) {
            int i = 0;
            while (i < array:length(expected)) {
                try {
                    assertEquals(actual[i], expected[i]);
                } catch (exception e) {
                    if (exceptions:getCategory(e) == assertFailureErrorCategory) {
                        throw createBallerinaException(
                                                message + ". " + exceptions:getMessage(e) + " (at index " + i + ") " ,
                                                assertFailureErrorCategory);
                    }
                }
                i = i + 1;
            }
        }
    }
}

@doc:Description("Asserts whether the given integer arrays are equal.
                  If it is not, a BallerinaException is thrown with the given message
                  including differed integer values and array index.")
@doc:Param("actual: Actual integer array")
@doc:Param("expected: Expected integer array")
function assertEquals(int[] actual, int[] expected) {
    assertEquals(actual, expected, "");
}

@doc:Description("Asserts whether the given integer arrays are equal.
                  If it is not, a BallerinaException is thrown with the given message
                  including differed integer values and array index.")
@doc:Param("actual: Actual integer array")
@doc:Param("expected: Expected integer array")
@doc:Param("message: Assertion error message")
function assertEquals(int[] actual, int[] expected, string message) {
    if (message == "") {
        message = arraysNotEqualMessage;
    }
    if (array:length(actual) != array:length(expected)) {
        throw createBallerinaException(message + arrayLengthsMismatchMessage, assertFailureErrorCategory);
    } else {
        if (array:length(expected) > 0) {
            int i = 0;
            while (i < array:length(expected)) {
                try {
                    assertEquals(actual[i], expected[i]);
                } catch (exception e) {
                    if (exceptions:getCategory(e) == assertFailureErrorCategory) {
                        throw createBallerinaException(
                                                message + ". " + exceptions:getMessage(e) + " (at index " + i + ") " ,
                                                assertFailureErrorCategory);
                    }
                }
                i = i + 1;
            }
        }
    }
}

