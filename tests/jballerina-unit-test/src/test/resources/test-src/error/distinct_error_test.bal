
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

public function testFunctionCallInDetailArgExpr() returns error {
    json codeJson = "1234";
    map<anydata> details = {};
    var x = GraphAPIError("Concurrent graph modification", code = codeJson.toString(), details = details);
    return x;
}
