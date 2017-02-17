package org.ballerinalang.test;

import ballerina.lang.exceptions;

const string assertTrueErrorCategory = "assert-true";
const string assertFalseErrorCategory = "assert-false";
const string assertEqualsErrorCategory = "assert-equals";



function createBallerinaException (string message, string category) (exception) {
    // Creates a BallerinaException with custom message and category.
    // @param message custom message for the ballerina exception
    // @param category exception category
    // @return exception
    exception e = {};
    exceptions:setMessage(e, message);
    exceptions:setCategory(e, category);
    return e;
}

function assertTrue(boolean condition) {
    // Asserts whether the given condition is true.
    // If it is not, a BallerinaException is thrown with
    // the given message.
    // @param condition the boolean condition to evaluate
    //
    assertTrue(condition, "");
}

function assertTrue(boolean condition, string message) {
    // Asserts whether the given condition is true.
    // If it is not, a BallerinaException is thrown with
    // the given message.
    // @param condition the boolean condition to evaluate
    // @param message the assertion error message
    //
    if (!condition) {
        if (message == "") {
            message = "Assert Failed";
        }
        throw createBallerinaException(message, assertTrueErrorCategory);
    }
}

function assertFalse(boolean condition) {
    // Asserts whether the given condition is false.
    // If it is not, a BallerinaException is thrown with
    // the given message.
    // @param condition the boolean condition to evaluate
    //
    assertFalse(condition, "");
}

function assertFalse(boolean condition, string message) {
    // Asserts whether the given condition is false.
    // If it is not, a BallerinaException is thrown with
    // the given message.
    // @param condition the boolean condition to evaluate
    // @param message the assertion error message
    //
    if (condition) {
        if (message == "") {
            message = "Assert Failed";
        }
        throw createBallerinaException(message, assertFalseErrorCategory);
    }
}

function assertEquals(string actual, string expected) {
    // Asserts whether the given string values are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message.
    // @param actual the actual string value
    // @param expected the expected string value
    //
    assertEquals(actual, expected, "");
}

function assertEquals(string actual, string expected, string message) {
    // Asserts whether the given string values are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message.
    // @param actual the actual string value
    // @param expected the expected string value
    // @param message the assertion error message
    //
    if (actual != expected) {
        if (message == "") {
            message = "String not equal: expected: " + expected + " and actual: "+ actual;
        }
        throw createBallerinaException(message, assertEqualsErrorCategory);
    }
}

function assertEquals(int actual, int expected) {
    // Asserts whether the given integer values are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message.
    // @param actual the actual integer value
    // @param expected the expected integer value
    //
    assertEquals(actual, expected, "");
}

function assertEquals(int actual, int expected, string message) {
    // Asserts whether the given integer values are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message.
    // @param actual the actual integer value
    // @param expected the expected integer value
    // @param message the assertion error message
    //
    if (actual != expected) {
        if (message == "") {
            message = "Integer not equal: expected: " + expected + " and actual: "+ actual;
        }
        throw createBallerinaException(message, assertEqualsErrorCategory);
    }
}

function assertEquals(double actual, double expected) {
    // Asserts whether the given double values are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message.
    // @param actual the actual double value
    // @param expected the expected double value
    //
    assertEquals(actual, expected, "");
}

function assertEquals(double actual, double expected, string message) {
    // Asserts whether the given double values are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message.
    // @param actual the actual double value
    // @param expected the expected double value
    // @param message the assertion error message
    //
    if (actual != expected) {
        if (message == "") {
            message = "Double not equal: expected: " + expected + " and actual: "+ actual;
        }
        throw createBallerinaException(message, assertEqualsErrorCategory);
    }
}

function assertEquals(boolean actual, boolean expected) {
    // Asserts whether the given boolean values are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message.
    // @param actual the actual boolean value
    // @param expected the expected boolean value
    //
    assertEquals(actual, expected, "");
}

function assertEquals(boolean actual, boolean expected, string message) {
    // Asserts whether the given boolean values are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message.
    // @param actual the actual boolean value
    // @param expected the expected boolean value
    // @param message the assertion error message
    //
    if (actual != expected) {
        if (message == "") {
            message = "Boolean not equal: expected: " + expected + " and actual: "+ actual;
        }
        throw createBallerinaException(message, assertEqualsErrorCategory);
    }
}

function assertEquals(string[] actual, string[] expected) {
    // Asserts whether the given string arrays are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message including differed string values and array index.
    // @param actual the actual string array
    // @param expected the expected string array
    //
    assertEquals(actual, expected, "");
}

function assertEquals(string[] actual, string[] expected, string message) {
    // Asserts whether the given string arrays are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message including differed string values and array index.
    // @param actual the actual string array
    // @param expected the expected string array
    // @param message the assertion error message
    //
    if (message == "") {
        message = "Arrays are not equal";
    }
    if (array:length(actual) != array:length(expected)) {
        throw createBallerinaException(message + " (Array lengths are not the same)", assertEqualsErrorCategory);
    } else {
        if (array:length(expected) > 0) {
            int i = 0;
            while (i < array:length(expected)) {
                try {
                    assertEquals(actual[i], expected[i]);
                } catch (exception e) {
                    if (exceptions:getCategory(e) == "assert-equals") {
                        throw createBallerinaException(
                                                      message + ". " + exceptions:getMessage(e) + " (at index " + i + ") " ,
                                                      assertEqualsErrorCategory);
                    }
                }
                i = i + 1;
            }
        }
    }
}

function assertEquals(int[] actual, int[] expected) {
    // Asserts whether the given integer arrays are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message including differed integer values and array index.
    // @param actual the actual integer array
    // @param expected the expected integer array
    //
    assertEquals(actual, expected, "");
}

function assertEquals(int[] actual, int[] expected, string message) {
    // Asserts whether the given integer arrays are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message including differed integer values and array index.
    // @param actual the actual integer array
    // @param expected the expected integer array
    // @param message the assertion error message
    //
    if (message == "") {
        message = "Arrays are not equal";
    }
    if (array:length(actual) != array:length(expected)) {
        throw createBallerinaException(message + " (Array lengths are not the same)", assertEqualsErrorCategory);
    } else {
        if (array:length(expected) > 0) {
            int i = 0;
            while (i < array:length(expected)) {
                try {
                    assertEquals(actual[i], expected[i]);
                } catch (exception e) {
                    if (exceptions:getCategory(e) == "assert-equals") {
                        throw createBallerinaException(
                                                      message + ". " + exceptions:getMessage(e) + " (at index " + i + ") " ,
                                                      assertEqualsErrorCategory);
                    }
                }
                i = i + 1;
            }
        }
    }
}

function assertEquals(double[] actual, double[] expected) {
    // Asserts whether the given double arrays are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message including differed double values and array index.
    // @param actual the actual double array
    // @param expected the expected double array
    //
    assertEquals(actual, expected, "");
}

function assertEquals(double[] actual, double[] expected, string message) {
    // Asserts whether the given double arrays are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message including differed double values and array index.
    // @param actual the actual double array
    // @param expected the expected double array
    // @param message the assertion error message
    //
    if (message == "") {
        message = "Arrays are not equal";
    }
    if (array:length(actual) != array:length(expected)) {
        throw createBallerinaException(message + " (Array lengths are not the same)", assertEqualsErrorCategory);
    } else {
        if (array:length(expected) > 0) {
            int i = 0;
            while (i < array:length(expected)) {
                try {
                    assertEquals(actual[i], expected[i]);
                } catch (exception e) {
                    if (exceptions:getCategory(e) == "assert-equals") {
                        throw createBallerinaException(
                                                      message + ". " + exceptions:getMessage(e) + " (at index " + i + ") " ,
                                                      assertEqualsErrorCategory);
                    }
                }
                i = i + 1;
            }
        }
    }
}

function assertEquals(boolean[] actual, boolean[] expected) {
    // Asserts whether the given boolean arrays are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message including differed boolean values and array index.
    // @param actual the actual boolean array
    // @param expected the expected boolean array
    //
    assertEquals(actual, expected, "");
}

function assertEquals(boolean[] actual, boolean[] expected, string message) {
    // Asserts whether the given boolean arrays are equal.
    // If it is not, a BallerinaException is thrown with
    // the given message including differed boolean values and array index.
    // @param actual the actual boolean array
    // @param expected the expected boolean array
    // @param message the assertion error message
    //
    if (message == "") {
        message = "Arrays are not equal";
    }
    // undefined function 'array:length' (no length function for boolean array in ballerinalang)
    if (array:length(actual) != array:length(expected)) {
        throw createBallerinaException(message + " (Array lengths are not the same)", assertEqualsErrorCategory);
    } else {
        if (array:length(expected) > 0) {
            int i = 0;
            while (i < array:length(expected)) {
                try {
                    assertEquals(actual[i], expected[i]);
                } catch (exception e) {
                    if (exceptions:getCategory(e) == "assert-equals") {
                        throw createBallerinaException(
                                                      message + ". " + exceptions:getMessage(e) + " (at index " + i + ") " ,
                                                      assertEqualsErrorCategory);
                    }
                }
                i = i + 1;
            }
        }
    }
}
