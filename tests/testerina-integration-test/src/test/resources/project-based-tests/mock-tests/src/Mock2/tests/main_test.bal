import ballerina/test;
import ballerina/http;
import ballerina/io;

//
// MOCK FUNCTION OBJECTS
//

@test:Mock {
    functionName : "intAdd"
}
test:MockFunction mock1 = new();

//
// MOCK FUNCTIONS
//

public function mockIntAdd(int a, int b) returns (int) {
    return a - b;
}

//
// TESTS
//

http:Client clientEP = new("http://localhost:9090");

@test:Config {}
function service_Test1() {
    io:println("[serivce_Test1] Testing mock functionality within a service");
    http:Request req = new();
    test:when(mock1).call("mockIntAdd");
    http:Response|error res = clientEP->get("/addService/add");

    if (res is http:Response) {
        var payload = res.getJsonPayload();
       if (payload is json) {
           int value = <int>payload.value;
           test:assertEquals(value, 4);
       }
    } else {
        test:assertFail("Fail");
    }
}

@test:Config{}
function call_Test() {
    io:println("[call_Test] Testing mock functionality in separate module");
    test:when(mock1).call("mockIntAdd");
    int val = callIntAdd(7, 3);
    test:assertEquals(val, 4);
}
