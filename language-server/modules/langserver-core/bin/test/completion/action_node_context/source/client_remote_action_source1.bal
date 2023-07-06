import ballerina/module1;

function testFunction() {
    string url = "/base";
    int testInt = 12; 
    module1:Client cl = new module1:Client("http://localhost:8080");
    cl->post()
}
