import ballerina/http;
public function main() {
    http:Client clientEndpoint = new ("http://postman-echo.com");
    http:Response response = checkpanic clientEndpoint->get("/get?test=123");

}

