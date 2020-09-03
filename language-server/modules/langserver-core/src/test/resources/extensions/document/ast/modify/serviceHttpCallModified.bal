import ballerina/http;
service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller, http:Request req) {
        http:Client clientEndpoint = new ("http://postman-echo.com");
        http:Response response = checkpanic clientEndpoint->get("/get?test=123");
    }
}

