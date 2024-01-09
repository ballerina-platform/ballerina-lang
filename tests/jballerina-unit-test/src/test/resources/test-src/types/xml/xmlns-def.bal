const A = "http://ballerina.com/a";

xmlns A as ns0;
xmlns xml:XMLNS_NAMESPACE_URI as ns1;

function testXMLNSDefinitionUsingConstant() {
    string s1 = ns0:foo;
    assert(s1, "{http://ballerina.com/a}foo");

    string s2 = ns1:foo;
    assert(s2, "{http://www.w3.org/2000/xmlns/}foo");
}

function assert(anydata actual, anydata expected) {
    if (expected != actual) {
        string reason = "expected `" + expected.toString() + "`, but found `" + actual.toString() + "`";
        error e = error(reason);
        panic e;
    }
}
