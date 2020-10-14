import ballerina/test;
import wso2test/baz;

string testStr1 = baz:bazStr1;
string testStr2 = baz:bazFn();

# Test function
@test:Config {
}
function testFunction() {
    test:assertEquals(testStr1, "this is a baz string");
    test:assertEquals(testStr2, "invoked bazFn");
}
