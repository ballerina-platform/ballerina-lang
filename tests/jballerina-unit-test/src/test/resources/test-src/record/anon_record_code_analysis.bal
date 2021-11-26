function testCodeAnalyzerRunningOnAnonymousRecordsForDeprecatedFunctionAnnotation() {
    record {
        int b = Test();
        } _ = {};
}

@deprecated
isolated function Test() returns int { return 0;}
