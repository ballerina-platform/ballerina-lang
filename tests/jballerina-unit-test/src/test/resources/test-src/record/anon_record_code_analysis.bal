function testCodeAnalyzerRunningOnAnonymousRecordsForDeprecatedFunctionAnnotation() {
    record {
        int b = Test();
        } rec = {};
}

@deprecated
isolated function Test() returns int { return 0;}
