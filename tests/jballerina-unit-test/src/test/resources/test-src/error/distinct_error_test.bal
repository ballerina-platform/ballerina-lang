
type Foo distinct error;

function testFooError() returns Foo {
    Foo foo = Foo("error message", detailField=true);
    var x = foo.detail();
    Foo f = foo;

    return foo;
}

public type GraphAPIError distinct error<GraphAPIErrorDetails>;
public type GraphAPIErrorDetails record {|
    string code;
    map<anydata> details;
|};

public function testFunctionCallInDetailArgExpr() {
    json codeJson = "1234";
    map<anydata> details = {};
    var x = GraphAPIError("Concurrent graph modification", code = codeJson.toString(), details = details);
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

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
