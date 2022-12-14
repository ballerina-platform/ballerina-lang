import ballerina/module1;

function testFunction() {
    final module1:Client clientEP = check new("http://example.com", );
}
