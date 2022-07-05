import ballerina/httpx;

listener httpx:MockListener mockEP = new(9090);

service myService1 on mockEP {
    resource function foo(httpx:Caller caller, httpx:Request req) {
        caller->respond()
    }
}