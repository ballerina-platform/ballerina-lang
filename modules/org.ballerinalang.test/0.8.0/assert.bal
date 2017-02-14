package org.ballerinalang.test;

import ballerina.lang.system;

// Asserts whether the given condition is true.
// If it is not, a BallerinaException is thrown with
// the given message.
// @param condition the boolean condition to evaluate
//
function assertTrue(boolean condition) {
    // TODO: once ballerinalang supports null values, pass null here
    assertTrue(condition, "");
}

// Asserts whether the given condition is true.
// If it is not, a BallerinaException is thrown with
// the given message.
// @param condition the boolean condition to evaluate
// @param message the assertion error message
//
function assertTrue(boolean condition, string message) {
    if (!condition) {
        // TODO: once ballerinalang supports null values, do the check here
        if (message == "") {
            message = "Assert Failed";
        }
        // TODO: once ballerinalang supports BallerinaExceptions, throw it here
        system:println(message);
    }
}

// Asserts whether the given condition is false.
// If it is not, a BallerinaException is thrown with
// the given message.
// @param condition the boolean condition to evaluate
//
function assertFalse(boolean condition) {
    // TODO: once ballerinalang supports null values, pass null here
    assertFalse(condition, "");
}

// Asserts whether the given condition is false.
// If it is not, a BallerinaException is thrown with
// the given message.
// @param condition the boolean condition to evaluate
// @param message the assertion error message
//
function assertFalse(boolean condition, string message) {
    if (condition) {
        // TODO: once ballerinalang supports null values, do the check here
        if (message == "") {
            message = "Assert Failed";
        }
        // TODO: once ballerinalang supports BallerinaExceptions, throw it here
        system:println(message);
    }
}

// Asserts whether the given string values are equal.
// If it is not, a BallerinaException is thrown with
// the given message.
// @param actual the actual string value
// @param expected the expected string value
//
function assertEquals(string actual, string expected) {
    // TODO: once ballerinalang supports null values, pass null here
    assertEquals(actual, expected, "");
}

// Asserts whether the given string values are equal.
// If it is not, a BallerinaException is thrown with
// the given message.
// @param actual the actual string value
// @param expected the expected string value
// @param message the assertion error message
//
function assertEquals(string actual, string expected, string message) {
    // Since string is a primitive type in ballerina
    if (actual != expected) {
        // TODO: once ballerinalang supports null values, do the check here
        if (message == "") {
            message = "String not equal: expected: " + expected + " and actual: "+ actual;
        }
        // TODO: once ballerinalang supports BallerinaExceptions, throw it here
        system:println(message);
    }
}

// Asserts whether the given integer values are equal.
// If it is not, a BallerinaException is thrown with
// the given message.
// @param actual the actual integer value
// @param expected the expected integer value
//
function assertEquals(int actual, int expected) {
    // TODO: once ballerinalang supports null values, pass null here
    assertEquals(actual, expected, "");
}

// Asserts whether the given integer values are equal.
// If it is not, a BallerinaException is thrown with
// the given message.
// @param actual the actual integer value
// @param expected the expected integer value
// @param message the assertion error message
//
function assertEquals(int actual, int expected, string message) {
    if (actual != expected) {
        // TODO: once ballerinalang supports null values, do the check here
        if (message == "") {
            message = "Integer not equal: expected: " + expected + " and actual: "+ actual;
        }
        // TODO: once ballerinalang supports BallerinaExceptions, throw it here
        system:println(message);
    }
}

// Asserts whether the given double values are equal.
// If it is not, a BallerinaException is thrown with
// the given message.
// @param actual the actual double value
// @param expected the expected double value
//
function assertEquals(double actual, double expected) {
    // TODO: once ballerinalang supports null values, pass null here
    assertEquals(actual, expected, "");
}

// Asserts whether the given double values are equal.
// If it is not, a BallerinaException is thrown with
// the given message.
// @param actual the actual double value
// @param expected the expected double value
// @param message the assertion error message
//
function assertEquals(double actual, double expected, string message) {
    if (actual != expected) {
        // TODO: once ballerinalang supports null values, do the check here
        if (message == "") {
            message = "Double not equal: expected: " + expected + " and actual: "+ actual;
        }
        // TODO: once ballerinalang supports BallerinaExceptions, throw it here
        system:println(message);
    }
}

// Asserts whether the given boolean values are equal.
// If it is not, a BallerinaException is thrown with
// the given message.
// @param actual the actual boolean value
// @param expected the expected boolean value
//
function assertEquals(boolean actual, boolean expected) {
    // TODO: once ballerinalang supports null values, pass null here
    assertEquals(actual, expected, "");
}

// Asserts whether the given boolean values are equal.
// If it is not, a BallerinaException is thrown with
// the given message.
// @param actual the actual boolean value
// @param expected the expected boolean value
// @param message the assertion error message
//
function assertEquals(boolean actual, boolean expected, string message) {
    if (actual != expected) {
        // TODO: once ballerinalang supports null values, do the check here
        if (message == "") {
            message = "Boolean not equal: expected: " + expected + " and actual: "+ actual;
        }
        // TODO: once ballerinalang supports BallerinaExceptions, throw it here
        system:println(message);
    }
}