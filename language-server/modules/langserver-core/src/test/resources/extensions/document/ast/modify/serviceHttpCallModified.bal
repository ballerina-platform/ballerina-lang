import ballerina/http;

service / on new http:Listener(8090) {
    resource function get hello(http:Caller caller, http:Request request) returns error? {
        http:Client httpEndpoint = check new ("http://postman-echo.com");
        http:Response response = check httpEndpoint->get("/get?test=123");
    }
}
