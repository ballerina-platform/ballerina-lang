type Rec record {|
    string a = ns1:baz;
|};

xmlns "http://ballerina.com/b" as ns1;

function testXMLNSDefinition() {
    string s1 = ns0:foo;
    assert(s1, "{http://ballerina.com/a}foo");

    string s2 = ns1:bar;
    assert(s2, "{http://ballerina.com/b}bar");

    Rec rec = {};
    assert(rec.a, "{http://ballerina.com/b}baz");
}

xmlns "http://ballerina.com/a" as ns0;

function assert(anydata actual, anydata expected) {
    if (expected != actual) {
        string reason = "expected `" + expected.toString() + "`, but found `" + actual.toString() + "`";
        error e = error(reason);
        panic e;
    }
}
