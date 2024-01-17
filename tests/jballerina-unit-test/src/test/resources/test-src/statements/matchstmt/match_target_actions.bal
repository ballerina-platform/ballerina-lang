function sum(int a, int b) returns int {
    return a + b;
}

function matchTest(int x, int y) returns string {
    future<int> f1 = start sum(x, y);
    return matchTestWaitAction(f1);
}

function sumE(int a, int b) returns int|error {
    if (a > 20) {
        return error("Sum failed", message = "a > 20");
    }
    return a + b;
}

function matchTestWaitAction(future<any|error> f) returns string {
    match wait f {
        17 => {
            return "17";
        }
        18 => {
            return "18";
        }
        var v => {
            return v is error ? <string>checkpanic v.detail()["message"] : v.toString();
        }
    }
}

function guardTest(any|error a1, function (float value) returns boolean func) returns boolean {
    if (a1 is float) {
        return func(a1);
    }
    return false;
}

public function testMatchTargetWaitActionTest() {
    assertValueEquality("17", matchTest(15, 2));
    future<int> f1 = start sum(15, 3);
    assertValueEquality("18", matchTestWaitAction(f1));
    assertValueEquality("23", matchTest(15, 8));
    future<int|error> f2 = start sumE(15, 3);
    future<int|error> f3 = start sumE(30, 3);
    assertValueEquality("18", matchTestWaitAction(f2));
    assertValueEquality("a > 20", matchTestWaitAction(f3));
}

enum TimeType {
    S,
    M,
    H,
    U
}

type TimeString [float, TimeType, string];

function matchTestTypeCast(int aSeconds, int bSeconds) returns TimeString {
    var isInvalidRange = function(float value) returns boolean => value < 0.0;
    var isInSecondsRange = function(float value) returns boolean => value < 60.0;
    var isInMinutesRange = function(float value) returns boolean => value < 60.0 * 60.0;
    var isInHoursRange = function(float value) returns boolean => value < 60.0 * 60.0 * 60.0;
    string time = "";
    match <float>sum(aSeconds, bSeconds) {
        var undetermined if isInvalidRange(undetermined) => {
            return [-1.0, U, time];
        }
        var seconds if isInSecondsRange(seconds) => {
            time += seconds.toString() + " s";
            return [seconds, S, time];
        }
        var minutes if isInMinutesRange(minutes) => {
            time += (<int>minutes / 60).toString() + "m:" + (<int>minutes % 60).toString() + "s";
            return [minutes / 60, M, time];
        }
        var hours if isInHoursRange(hours) => {
            int h = <int>hours / 3600;
            int m = (<int>hours % 3600) / 60;
            int s = ((<int>hours % 3600) % 60);
            time += h.toString() + "h:" + m.toString() + "m:" + s.toString() + "s";
            return [hours / (60 * 60), H, time];
        }
        _ => {
            return [0, U, time];
        }
    }
}

public function testMatchTargetWitchMatchGuardTest() {
    assertFloatEquality([1.0, M, "1m:2s"], matchTestTypeCast(60, 2));
    assertFloatEquality([2.0, M, "2m:0s"], matchTestTypeCast(60, 60));
    assertFloatEquality([1.0, H, "1h:0m:2s"], matchTestTypeCast(60 * 60, 2));
    assertFloatEquality([1.9, H, "1h:58m:20s"], matchTestTypeCast(60 * 60, 58 * 60 + 20));
    assertFloatEquality([-1.0, U, ""], matchTestTypeCast(-2, -3));
}

public function main() {
    testMatchTargetWaitActionTest();
    testMatchTargetWitchMatchGuardTest();
}

type AssertionError distinct error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertTrue(any|error actual) {
    if actual is boolean && actual {
        return;
    }
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON, message = "expected 'true', found '" + actualValAsString + "'");
}

function assertFalse(any|error actual) {
    if actual is boolean && !actual {
        return;
    }
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON, message = "expected 'false', found '" + actualValAsString + "'");
}

function abs(float value) returns float {
    if (value < 0.0) {
        return -1.0 * value;
    }
    return value;
}

function assertFloatEquality(TimeString expected, TimeString actual) {
    if expected == actual {
        return;
    }
    if (expected[1] != actual[1]) {
        panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
    }
    if (expected[2] != actual[2]) {
        panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
    }
    if (abs(expected[0] - actual[0]) > 0.1) {
        panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
    }
}

function assertValueEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
