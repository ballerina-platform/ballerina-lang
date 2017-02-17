import ballerina.lang.system;
import ballerina.lang.array;
import ballerina.lang.exceptions;

const string assertEqualsErrorCategory = "assert-equals";

function main (string[] args) {
    string[] stringArr1 = ["a", "b", "c"];
    string[] stringArr2 = ["a", "b", "d"];
    string[] stringArr3 = ["a", "b"];
    try {
        // Custom message
        assertEquals(stringArr1, stringArr2, "Two arrays are not equal");
        // No message
        //assertEquals(stringArr1, stringArr2);
        // Array length
        //assertEquals(stringArr1, stringArr3);
    } catch (exception e) {
        if( exceptions:getCategory(e) == "assert-equals" ) {
            system:println( "Error ..! " + exceptions:getMessage(e));
        }
    }
}

function assertEquals(string actual, string expected) {
    assertEquals(actual, expected, "");
}

function assertEquals(string actual, string expected, string message) {
    if (actual != expected) {
        if (message == "") {
            message = "String not equal: expected: " + expected + " and actual: "+ actual;
        }
        throw createBallerinaException(message, assertEqualsErrorCategory);
    }
}

function assertEquals(string[] actual, string[] expected) {
    assertEquals(actual, expected, "");
}

function assertEquals(string[] actual, string[] expected, string message) {
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

function createBallerinaException (string message, string category) (exception) {
    exception e = {};
    exceptions:setMessage(e, message);
    exceptions:setCategory(e, category);
    return e;
}
