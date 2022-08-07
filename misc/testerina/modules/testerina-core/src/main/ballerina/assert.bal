// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

const string assertFailureErrorCategory = "assert-failure";
const string arraysNotEqualMessage = "Arrays are not equal";
const string arrayLengthsMismatchMessage = " (Array lengths are not the same)";
const int maxArgLength = 80;
const int mapValueDiffLimit = 5;

# The error struct for assertion errors.
#
# + message - The assertion error message
# + cause - The error which caused the assertion error
# + category - The assert error category
type AssertError record {
    string message = "";
    error? cause = ();
    string category = "";
};

# Creates an `AssertError` with the custom message and category.
#
# + errorMessage - Custom message for the Ballerina error
# + category - Error category
#
# + return - An AssertError with custom message and category
public isolated function createBallerinaError(string errorMessage, string category) returns TestError {
    return error(errorMessage);
}

# Asserts whether the given condition is true. If it is not, a AssertError is thrown with the given errorMessage.
#
#### Example
# ```ballerina
# import ballerina/test;
#
#  @test:Config {}
#  function testAssertTrue() {
#      boolean value = false;
#      test:assertTrue(value, msg = "AssertTrue failed");
#  }
# ```
#
# + condition - Boolean condition to evaluate
# + msg - Assertion error message
public isolated function assertTrue(boolean condition, string msg = "Assertion Failed!") {
    if (!condition) {
        panic createBallerinaError(msg, assertFailureErrorCategory);
    }
}

# Asserts whether the given condition is false. If it is not, a AssertError is thrown with the given errorMessage.
#
#### Example
# ```ballerina
# import ballerina/test;
#
#  @test:Config {}
#  function testAssertFalse() {
#      boolean value = false;
#      test:assertFalse(value, msg = "AssertFalse failed");
#  }
# ```
#
# + condition - Boolean condition to evaluate
# + msg - Assertion error message
public isolated function assertFalse(boolean condition, string msg = "Assertion Failed!") {
    if (condition) {
        panic createBallerinaError(msg, assertFailureErrorCategory);
    }
}

# Asserts whether the given values are equal. If it is not, an AssertError is thrown with the given errorMessage.
#
#### Example
# ```ballerina
# import ballerina/test;
#
# @test:Config {}
#  function testAssertIntEquals() {
#      int answer = intAdd(5, 3);
#      test:assertEquals(answer, 8, msg = "IntAdd function failed");
#  }
#
#  function intAdd(int a, int b) returns (int) {
#      return (a + b);
#  }
# ```
#
# + actual - Actual value
# + expected - Expected value
# + msg - Assertion error message
public isolated function assertEquals(any|error actual, anydata expected, string msg = "Assertion Failed!") {
    if (actual is error || actual != expected) {
        string errorMsg = getInequalityErrorMsg(actual, expected, msg);
        panic createBallerinaError(errorMsg, assertFailureErrorCategory);
    }
}

# Asserts whether the given values are not equal. If it is equal, an AssertError is thrown with the given errorMessage.
#
#### Example
# ```ballerina
# import ballerina/test;
#
#  @test:Config {}
#  function testAssertIntEquals() {
#      int answer = intAdd(5, 3);
#      test:assertNotEquals(answer, 8, msg = "Matches");
#  }
#
#  function intAdd(int a, int b) returns (int) {
#      return (a + b);
#  }
# ```
#
# + actual - Actual value
# + expected - Expected value
# + msg - Assertion error message
public isolated function assertNotEquals(any actual, anydata expected, string msg = "Assertion Failed!") {
    if (actual == expected) {
        string expectedStr = sprintf("%s", expected);
        string errorMsg = string `${msg}: expected the actual value not to be '${expectedStr}'`;
        panic createBallerinaError(errorMsg, assertFailureErrorCategory);
    }
}

# Asserts whether the given values are exactly equal. If it is not, an AssertError is thrown with the given errorMessage.
#
#### Example
# ```ballerina
# import ballerina/test;
#
#  class Person {
#      public string name = "";
#      public int age = 0;
#      public Person? parent = ();
#      private string email = "default@abc.com";
#      string address = "No 20, Palm grove";
#  }
#
#  @test:Config {}
#  function testAssertExactEqualsObject() {
#      Person p1 = new;
#      Person p2 = p1;
#      test:assertExactEquals(p1, p2, msg = "Objects are not exactly equal");
#  }
# ```
#
# + actual - Actual value
# + expected - Expected value
# + msg - Assertion error message
public isolated function assertExactEquals(any|error actual, any|error expected, string msg = "Assertion Failed!") {
    boolean isEqual = (actual === expected);
    if (!isEqual) {
        string errorMsg = getInequalityErrorMsg(actual, expected, msg);
        panic createBallerinaError(errorMsg, assertFailureErrorCategory);
    }
}

# Asserts whether the given values are not exactly equal. If it is equal, an AssertError is thrown with the given errorMessage.
#
#### Example
# ```ballerina
# import ballerina/test;
#
# class Person {
#      public string name = "";
#      public int age = 0;
#      public Person? parent = ();
#      private string email = "default@abc.com";
#      string address = "No 20, Palm grove";
#  }
#
#  @test:Config {}
#  function testAssertNotExactEqualsObject() {
#      Person p1 = new;
#      Person p2 = new ();
#      test:assertNotExactEquals(p1, p2, msg = "Objects are exactly equal");
#  }
# ```
#
# + actual - Actual value
# + expected - Expected value
# + msg - Assertion error message
public isolated function assertNotExactEquals(any|error actual, any|error expected, string msg = "Assertion Failed!") {
    boolean isEqual = (actual === expected);
    if (isEqual) {
        string expectedStr = sprintf("%s", expected);
        string errorMsg = string `${msg}: expected the actual value not to be '${expectedStr}'`;
        panic createBallerinaError(errorMsg, assertFailureErrorCategory);
    }
}

# Assert failure is triggered based on your discretion. AssertError is thrown with the given errorMessage.
#
#### Example
# ```ballerina
# import ballerina/test;
#
#  @test:Config {}
#  function foo() {
#      error? e = trap bar(); // Expecting `bar()` to panic
#      if (e is error) {
#          test:assertEquals(e.message().toString(), "Invalid Operation", msg = "Invalid error reason");
#      } else {
#          test:assertFail(msg = "Expected an error");
#      }
#  }
#
#  function bar() {
#      panic error("Invalid Operation");
#  }
# ```
#
# + msg - Assertion error message
# + return - never returns a value
public isolated function assertFail(string msg = "Test Failed!") returns never {
    panic createBallerinaError(msg, assertFailureErrorCategory);
}

# Gets the error message to be shown when there is an inequality while asserting two values.
#
# + actual - Actual value
# + expected - Expected value
# + msg - Assertion error message
# + return - Error message constructed based on the compared values
isolated function getInequalityErrorMsg(any|error actual, any|error expected, string msg = "\nAssertion Failed!") returns @tainted string {
        string expectedType = getBallerinaType(expected);
        string actualType = getBallerinaType(actual);
        string errorMsg = "";
        string expectedStr = sprintf("%s", expected);
        string actualStr = sprintf("%s", actual);
        if (expectedStr.length() > maxArgLength) {
            expectedStr = getFormattedString(expectedStr);
        }
        if (actualStr.length() > maxArgLength) {
            actualStr = getFormattedString(actualStr);
        }
        if (expectedType != actualType) {
            errorMsg = string `${msg}` + "\n \nexpected: " + string `<${expectedType}> '${expectedStr}'` + "\nactual\t: "
                + string `<${actualType}> '${actualStr}'`;
        } else if (actual is string && expected is string) {
            string diff = getStringDiff(<string>actual, <string>expected);
            errorMsg = string `${msg}` + "\n \nexpected: " + string `'${expectedStr}'` + "\nactual\t: "
                                     + string `'${actualStr}'` + "\n \nDiff\t:\n" + string `${diff}`;
        } else if (actual is map<anydata> && expected is map<anydata>) {
            string diff = getMapValueDiff(<map<anydata>>actual, <map<anydata>>expected);
            errorMsg = string `${msg}` + "\n \nexpected: " + string `'${expectedStr}'` + "\nactual\t: " +
                            string `'${actualStr}'` + "\n \nDiff\t:\n" + string `${diff}`;
        } else {
            errorMsg = string `${msg}` + "\n \nexpected: " + string `'${expectedStr}'` + "\nactual\t: "
                                                 + string `'${actualStr}'`;
        }
        return errorMsg;
}

isolated function getFormattedString(string str) returns string {
    string formattedString = "";
    // Number of iterations in the loop
    int itr = (str.length() / maxArgLength) + 1;
    foreach int i in 0 ..< itr {
        // If the calculated substring index is less than string length
        if ((i + 1) * maxArgLength < str.length()) {
            // Formulate the substring
            string subString = str.substring((i * maxArgLength), ((i + 1) * maxArgLength));
            // Append substring with newline
            formattedString += subString + "\n";
        } else {
            // If the calculated substring is equal to or greater than the string length
            // Modify the substring to include only the string length
            string subString = str.substring((i * maxArgLength), str.length());
            formattedString += subString;
        }
    }
    return formattedString;
}

isolated function getKeyArray(map<anydata> valueMap) returns @tainted string[] {
    string[] keyArray = valueMap.keys();
    foreach string keyVal in keyArray {
        var value = valueMap.get(keyVal);
        if (value is map<anydata>) {
            string[] childKeys = getKeyArray(<map<anydata>>value);
            foreach string childKey in childKeys {
                keyArray.push(keyVal + "." + childKey);
            }
        }
    }
    return keyArray;
}

isolated function getMapValueDiff(map<anydata> actualMap, map<anydata> expectedMap) returns @tainted string {
    string diffValue = "";
    string[] actualKeyArray = getKeyArray(actualMap);
    string[] expectedKeyArray = getKeyArray(expectedMap);
    string keyDiff = getKeysDiff(actualKeyArray, expectedKeyArray);
    string valueDiff = compareMapValues(actualMap, expectedMap);
    if (keyDiff != "") {
        diffValue = diffValue.concat(keyDiff, "\n", valueDiff);
    } else {
        diffValue = diffValue.concat(valueDiff);
    }
    return diffValue;
}

isolated function getValueComparison(anydata actual, anydata expected, string keyVal, int count) returns @tainted ([string, int])  {
    int diffCount = count;
    string diff = "";
    string expectedType = getBallerinaType(expected);
    string actualType = getBallerinaType(actual);
    if (expectedType != actualType) {
        diff = diff.concat("\n", "key: ", keyVal, "\n \nexpected value\t: <", expectedType, "> ", expected.toString(),
        "\nactual value\t: <", actualType, "> ", actual.toString());
        diffCount = diffCount + 1;
    } else {
        if (actual is map<anydata> && expected is map<anydata>) {
            string[] expectedkeyArray = (<map<anydata>>expected).keys();
            string[] actualKeyArray = (<map<anydata>>actual).keys();
            int orderCount = diffCount;
            foreach string childKey in actualKeyArray {
                if (expectedkeyArray.indexOf(childKey) != ()){
                    anydata expectedChildVal = expected.get(childKey);
                    anydata actualChildVal = actual.get(childKey);
                    string childDiff;
                    if (expectedChildVal != actualChildVal) {
                        [childDiff, diffCount] = getValueComparison(actualChildVal, expectedChildVal, keyVal + "." + childKey, diffCount);
                        if (diffCount != (orderCount + 1)) {
                            diff = diff.concat("\n");
                        }
                        diff = diff.concat(childDiff);
                    }
                }

            }
        } else {
            diff = diff.concat("\n", "key: ", keyVal, "\n \nexpected value\t: ", expected.toString(),
            "\nactual value\t: ", actual.toString());
            diffCount = diffCount + 1;
        }
    }
    return [diff, diffCount];
}

isolated function compareMapValues(map<anydata> actualMap, map<anydata> expectedMap) returns @tainted string {
    string diff = "";
    string[] actualKeyArray = actualMap.keys();
    int count = 0;
    foreach string keyVal in actualKeyArray {
        if (expectedMap.hasKey(keyVal)) {
            anydata expected = expectedMap.get(keyVal);
            anydata actual = actualMap.get(keyVal);
            if (expected != actual) {
                string diffVal;
                [diffVal, count] = getValueComparison(actual, expected, keyVal, count);
                if (count != 1) {
                    diff = diff.concat("\n");
                }
                diff = diff.concat(diffVal);
            }
        }
    }
    if (count > mapValueDiffLimit) {
        diff = diff.concat("\n \nTotal value mismatches: " + count.toString() + "\n");
    }
    return diff;
}
