import stacktrace_project.stacktrace as stacktrace;

public function testStackTraceElements() {
    any st = stacktrace:getStackTrace();
    assertEquality("[{\"callableName\":\"getStackTrace\",\"moduleName\":\"test_org.stacktrace_project.stacktrace.0_1_0\",\"fileName\":\"stacktrace.bal\",\"lineNumber\":2},{\"callableName\":\"testStackTraceElements\",\"moduleName\":\"test_org.stacktrace_project.0_1_0\",\"fileName\":\"main.bal\",\"lineNumber\":4}]", st.toString());
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
