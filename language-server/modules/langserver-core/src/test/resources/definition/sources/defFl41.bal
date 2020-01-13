public type SimpleTypeDesc int;
public type SimpleTypeDesc2 float;

public type UnionTypeDesc SimpleTypeDesc|SimpleTypeDesc2;

public type OptionalTypeDesc SimpleTypeDesc?;

function testTypeDescFunction() {
    UnionTypeDesc tDesc1;
    SimpleTypeDesc|SimpleTypeDesc2 tDesc2 = 1;

    OptionalTypeDesc tDesc3;
    SimpleTypeDesc? tDesc4;
}

public type ErrorTypeDesc1 error<TestReason, TestDetail>;

public type ErrorTypeDesc2 error<TestReason>;

public type ErrorTypeDesc3 error<REASON_CONST, TestDetail>;

public type TestReason string;

public const string REASON_CONST = "Test Reason";

type TestDetail record {|
    string message?;
    error cause?;
|};

function testErrorTypeDesc() {
    ErrorTypeDesc1 err1;
    ErrorTypeDesc2 err2;
}

function testErrorTypeDesc2() {
    error eCause = error("errorCode", message = "");
    string eMessage = "Sample Message";
    string testFieldVal = "Test Field Value";
    string testErrorCode = "errorCode";
    // Direct Error Constructor
    error testError = error(testErrorCode, message = eMessage, cause = eCause, testField = testFieldVal);

    // Indirect Error Constructor
    ErrorTypeDesc1 testError2 = ErrorTypeDesc3(message = eMessage, cause = eCause);
}
