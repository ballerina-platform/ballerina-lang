import stacktrace_project.stacktrace as stacktrace;

public function testStackTraceElements() {
    error:CallStackElement[] callStackElements = stacktrace:getStackTrace();

    assertEquality(callStackElements.length(), 2);
    assertEquality(callStackElements[0].toString(), "{\"callableName\":\"getStackTrace\",\"moduleName\":\"test_org.stacktrace_project.stacktrace.0_1_0\",\"fileName\":\"stacktrace.bal\",\"lineNumber\":2}");
    assertEquality(callStackElements[1].toString(), "{\"callableName\":\"testStackTraceElements\",\"moduleName\":\"test_org.stacktrace_project.0_1_0\",\"fileName\":\"main.bal\",\"lineNumber\":4}");

    error:CallStackElement callStackElement = callStackElements[0];
    string callableName = callStackElement["callableName"];
    string? moduleName = callStackElement["moduleName"];
    string fileName = callStackElement["fileName"];
    int lineNumber = callStackElement["lineNumber"];

    assertEquality(callableName, "getStackTrace");
    assertEquality(moduleName, "test_org.stacktrace_project.stacktrace.0_1_0");
    assertEquality(fileName, "stacktrace.bal");
    assertEquality(lineNumber, 2);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(string `expected '${expectedValAsString}', found '${actualValAsString}'`);
}
