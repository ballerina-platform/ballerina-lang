import ballerina/test;
import ballerina/io;
import ballerina/http;
import ballerina/runtime;

any[] outputs = [];
int counter = 0;

@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter += 1;
}

@test:Config {
}
function testFunc() {
    // Invoke the main function. 
    http:Client httpEndpoint = new("http://localhost:9090");
    json clientResp = "{'message' : 'request successfully received'}";

    http:Request req = new;
    req.setJsonPayload({ "message": "hello" });

    int j = 0;
    while (j < 7) {
        // Send a `POST` request to the specified endpoint.
        var response = httpEndpoint->post("/requests", req);
        if (response is http:Response) {
            var res = response.getJsonPayload();
            test:assertEquals(res, clientResp);
        } else {
            test:assertFail(msg = "Failed to call the endpoint:");
        }

        j = j + 1;
    }

    runtime:sleep(15000);

    string out = "ALERT!! : Received more than 6 requests from the host within 10 seconds : localhost";
    test:assertEquals(outputs.length(), 1);
    test:assertEquals(outputs[0], out);
}
