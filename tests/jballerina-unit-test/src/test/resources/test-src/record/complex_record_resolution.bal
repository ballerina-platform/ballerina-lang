import testorg/foo.records as Records;

function testCreatingAComplexRecordWithIncludingType() {
    Records:CodedMessage codm = {
        message: "FirstCodedMessage",
        code: "FirstCode"
    };

    Records:JobScheduler joq = {
        reasons: [codm]
    };

    var codedMsgList = <Records:CodedMessage[]>joq.reasons;
    var codedMessage = <Records:CodedMessage>codedMsgList[0];
    assertValueEquality("FirstCodedMessage", codedMessage.message);
    assertValueEquality("FirstCode", codedMessage.code);
}

function testCreatingComplexRecordWithIncludedType() {
    Records:CodedMessage codm = {
        code: "128",
        message: "FirstCodedMessage"
    };

    Records:JobRequest joq = {
        reasons: [codm]
    };

    var lmtCodedMsgList = <Records:LimitedCodedMessage[]>joq.reasons;
    var lmtCodedMessage = <Records:LimitedCodedMessage>lmtCodedMsgList[0];
    assertValueEquality("FirstCodedMessage", lmtCodedMessage.message);
    assertValueEquality("128", lmtCodedMessage.code);
}

function testCreatingComplexRecWithIncTypeWithActualTypes() {
    Records:LimitedCodedMessage codm = {
        code: 255,
        message: "FirstCodedMessage"
    };

    Records:JobRequest joq = {
        reasons: [codm]
    };

    var lmtCodedMsgList = <Records:LimitedCodedMessage[]>joq.reasons;
    var lmtCodedMessage = <Records:LimitedCodedMessage>lmtCodedMsgList[0];
    assertValueEquality("FirstCodedMessage", lmtCodedMessage.message);
    assertValueEquality(255, <int>lmtCodedMessage.code);
}

public type JobSchedulerWithCurrentModuleCM record {
    string gridId?;
    *Records:JobRequest;
    CodedMessageNew[] reasons?;
};

public type JobSchedulerNew record {
    string gridId?;
    *Records:JobRequest;
    Records:CodedMessage[] reasons?;
};

public type CodedMessageNew record {
    string code?;
    string message?;
};

function testCreatingComplexRecWithIncTypeFromBala() {
    Records:CodedMessage codm = {
        message: "FirstCodedMessage",
        code: "FirstCode"
    };

    JobSchedulerNew joq = {
        reasons: [codm]
    };

    var codedMsgList = <Records:CodedMessage[]>joq.reasons;
    var codedMessage = <Records:CodedMessage>codedMsgList[0];
    assertValueEquality("FirstCodedMessage", codedMessage.message);
    assertValueEquality("FirstCode", codedMessage.code);
}

function testCreatingComplexRecWithIncTypeFromBalaWithCM() {
    CodedMessageNew codm = {
        message: "FirstCodedMessage",
        code: "FirstCode"
    };

    JobSchedulerWithCurrentModuleCM joq = {
        reasons: [codm]
    };

    var codedMsgList = <CodedMessageNew[]>joq.reasons;
    var codedMessage = <CodedMessageNew>codedMsgList[0];
    assertValueEquality("FirstCodedMessage", codedMessage.message);
    assertValueEquality("FirstCode", codedMessage.code);
}

const ASSERTION_ERROR_REASON = "AssertionError";

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
