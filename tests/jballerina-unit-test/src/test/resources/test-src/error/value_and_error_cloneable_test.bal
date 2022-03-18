import ballerina/lang.value;

function testSimpleErrorDetailsMatchToCloneable() {
    any|error err6 = error("Error", code1 = [12, "A", 10.5, 1]);
    assertValueEquality("message:Error cause:No Cause details:{\"a\":12,\"rest\":[\"A\",10.5,1]}",
        errorMatchPatternValueCloneable(err6));
    assertValueEquality("message:Error cause:No Cause details:{\"a\":12,\"rest\":[\"A\",10.5,1]}",
        errorMatchPatternErrorCloneable(err6));

}

function testSimpleAssignabilityRules() {
    value:Cloneable vc = 3;
    error:Cloneable ec = 3;
    ec = vc;
    assertTrue(ec is int);
}

function testSimpleIsChecks() {
    value:Cloneable vc = 1;
    error:Cloneable ec = 3;
    ec = <error:Cloneable>vc;
    assertTrue(ec is value:Cloneable);
}

function errorMatchPatternValueCloneable(any|error x) returns string {
    match x {
        error(var m, var c, code1 = [var d, ...var e]) => {
            string m2 = m;
            error? c2 = c;
            value:Cloneable x1 = d;
            value:Cloneable x2 = e;
            map<any|error> details = {};
            if !(x1 is ()) {
                details["a"] = x1;
            }
            if !(x2 is ()) {
                details["rest"] = x2;
            }
            return "message:" + m2 + " cause:" + (c2 is () ? "No Cause" : c2.message())
                    + " details:" + details.toString();
        }
    }

    return "Default";
}

function errorMatchPatternErrorCloneable(any|error x) returns string {
    match x {
        error(var m, var c, code1 = [var d, ...var e]) => {
            string m2 = m;
            error? c2 = c;
            error:Cloneable x1 = d;
            error:Cloneable x2 = e;
            map<any|error> details = {};
            if !(x1 is ()) {
                details["a"] = x1;
            }
            if !(x2 is ()) {
                details["rest"] = x2;
            }
            return "message:" + m2 + " cause:" + (c2 is () ? "No Cause" : c2.message())
                    + " details:" + details.toString();
        }
    }

    return "Default";
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    if actual is boolean && actual {
        return;
    }

    string actualValAsString = "";
    if (actual is error) {
        actualValAsString = actual.toString();
    } else {
        actualValAsString = actual.toString();
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected 'true', found '" + actualValAsString + "'");
}

isolated function isEqual(anydata|error actual, anydata|error expected) returns boolean {
    if (actual is anydata && expected is anydata) {
        return (actual == expected);
    } else {
        return (actual === expected);
    }
}

function assertValueEquality(anydata|error expected, anydata|error actual) {
    if isEqual(actual, expected) {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
