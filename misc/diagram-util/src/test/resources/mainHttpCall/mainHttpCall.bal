import ballerina/http;

http:Client myclient = check new ("http://abcd");

public function main() returns error? {
    http:Client clientEndpoint = check new ("http://postman-echo.com");
    var response = check clientEndpoint->get("");
}