type SourcePatient record {
    string message;
    string detail;
    string cause;
};

function foo(string m, int c, int t, string d, string cause) {
}

function bar(SourcePatient sourcePatient, int errorCode, int errorType) {
    return foo(sourcePatient.message, errorCode, errorType, sourcePatient.detail,
            sourcePatient.cause);
}
