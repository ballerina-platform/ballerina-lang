import ballerina/http;
import ballerina/test;

service hello on new http:Listener(9090) {
    resource function sayHello(http:Caller caller,
        http:Request req) returns error? {
        check caller->respond("Hello Test!");
    }
}

# Test function
@test:Config{}
function directoryTestServiceFunction ()  {
    string payload = "Invalid";
    http:Client httpClient = new("http://localhost:9090");
    http:Response | error response = httpClient->get("/hello/sayHello");
    if (response is http:Response) {
        string | error res = response.getTextPayload();
        if (res is string){
            payload = res;
        }
        test:assertEquals(payload, "Hello Test!", "Service involation test");
    } else {
        test:assertFail(response.toString());
    }
}
