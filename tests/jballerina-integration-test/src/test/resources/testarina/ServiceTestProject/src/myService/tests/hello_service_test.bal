import ballerina/test;
import ballerina/io;
import ballerina/http;

# Before Suite Function
@test:BeforeSuite
function beforeSuiteServiceFunc () {
    io:println("This will test if a service start during testing");
}


# Test function
@test:Config{}
function testServiceFunction ()  {
    string payload = "Invalid";
    http:Client httpClient = new("http://localhost:9393");
    http:Response | error response = httpClient->get("/hello/sayHello");
    if (response is http:Response) {
        string | error res = response.getTextPayload();
        if (res is string){
            payload = res;
        }
        test:assertEquals(payload, "Hello Test!", "Service involation test");
        io:println(payload);
        io:println("Service 1 completed");
    } else {
        test:assertFail(response.toString());
    }
}


# After Suite Function
@test:AfterSuite
function afterSuiteServiceFunc () {
    io:println("Service should stop after this.");
}