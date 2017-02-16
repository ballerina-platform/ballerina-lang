package org.ballerinalang.test;

import ballerina.lang.exceptions;

const string assertTrueErrorCategory = "assert-true";
const string assertFalseErrorCategory = "assert-false";
const string assertEqualsErrorCategory = "assert-equals";

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
        createBallerinaException(message, assertTrueErrorCategory);
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
        createBallerinaException(message, assertFalseErrorCategory);
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
    // Since string is a primitive type in ballerina
    if (actual != expected) {
        if (message == "") {
            message = "String not equal: expected: " + expected + " and actual: "+ actual;
        }
        createBallerinaException(message, assertEqualsErrorCategory);
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
        createBallerinaException(message, assertEqualsErrorCategory);
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
        createBallerinaException(message, assertEqualsErrorCategory);
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
        createBallerinaException(message, assertEqualsErrorCategory);
    }
}

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