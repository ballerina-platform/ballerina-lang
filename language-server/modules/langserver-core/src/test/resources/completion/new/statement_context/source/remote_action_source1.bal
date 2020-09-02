import ballerina/module1;

function testFunction() {
    module1:Client cl = new module1:Client("http://localhost:8080");
    cl->p
}
