import ballerina/module1;

function testFunction() {
    json payload = {"hello": "world"};
    module1:Client cl = new("http://localhost/test");
    module1:Response|error response = cl -> post("foo/bar/", payload);
    module1:Response|error response1 = wait response;
}
