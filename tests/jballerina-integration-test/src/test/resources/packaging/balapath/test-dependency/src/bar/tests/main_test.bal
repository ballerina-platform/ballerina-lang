import ballerina/test;
import wso2test/foo;

string testStr1 = foo:fooStr1;
string testStr2 = foo:fooFn();

# Test function
@test:Config {
}
function testFunction() {
    test:assertEquals(testStr1, "this is a foo string");
    test:assertEquals(testStr2, "fooFn invoked");
}
