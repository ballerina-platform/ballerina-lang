const A = "http://ballerina.com/a";
const string|int B = "http://ballerina.com/b";
const C = "http://ballerina.com/c";
const D = C;

xmlns A as ns0;
xmlns xml:XMLNS_NAMESPACE_URI as ns1;
xmlns B as ns2;
xmlns D as ns3;

function testXMLNSDefinitionUsingConstant() {
    string s1 = ns0:foo;
    assert(s1, "{http://ballerina.com/a}foo");

    string s2 = ns1:foo;
    assert(s2, "{http://www.w3.org/2000/xmlns/}foo");

    string s3 = ns2:bar;
    assert(s3, "{http://ballerina.com/b}bar");

    string s4 = ns3:bar;
    assert(s4, "{http://ballerina.com/c}bar");
}

function assert(anydata actual, anydata expected) {
    if (expected != actual) {
        string reason = "expected `" + expected.toString() + "`, but found `" + actual.toString() + "`";
        error e = error(reason);
        panic e;
    }
}
