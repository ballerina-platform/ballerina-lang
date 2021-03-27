
type Foo distinct error;

function testFooError() {
    Foo foo = error Foo("error message", detailField=true);
    var x = foo.detail();
    Foo f = foo;

    assertEquality("error message", f.message());
    assertEquality(true, x["detailField"]);
}

public type GraphAPIError distinct error<GraphAPIErrorDetails>;
public type GraphAPIErrorDetails record {|
    string code;
    map<anydata> details;
|};

public function testFunctionCallInDetailArgExpr() {
    json codeJson = "1234";
    map<anydata> details = {};
    var x = error GraphAPIError("Concurrent graph modification", code = codeJson.toString(), details = details);
    assertEquality(x.message(), "Concurrent graph modification");
    assertEquality(x.detail().code, "1234");
    assertEquality(x.detail().details, details);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error actual, any|error expected) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
