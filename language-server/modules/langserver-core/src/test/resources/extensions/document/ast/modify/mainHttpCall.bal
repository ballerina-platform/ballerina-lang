import ballerina/http;
public function main() {
http:Client clientEndpoint = new ("http://postman-echo.com");
http:Response response = check clientEndpoint->get("/get?test=123");
}

