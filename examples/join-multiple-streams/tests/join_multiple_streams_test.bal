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

    json clientResp1 = { "message": "Raw material request successfully received" };
    json clientResp2 = { "message": "Production input request successfully received" };

    http:Request req = new;
    req.setJsonPayload({ "name": "Teak", "amount": 1000.0 });
    // Send a `POST` request to the specified endpoint.
    var response = httpEndpoint->post("/rawmaterial", req);

        if (response is http:Response) {
            var res = response.getJsonPayload();
            test:assertEquals(res, clientResp1);
        } else {
            test:assertFail(msg = "Failed to call the endpoint:");
        }

    // Introducce an idle time to make sure requests are send in order.
    runtime:sleep(3000);

    http:Request req2 = new;
    req2.setJsonPayload({ "name": "Teak", "amount": 500.0 });
    // Send a `GET` request to the specified endpoint.
    var response2 = httpEndpoint->post("/productionmaterial", req2);
    if (response2 is http:Response) {
        var res = response2.getJsonPayload();
        test:assertEquals(res, clientResp2);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }

    runtime:sleep(10000);

    string out = "ALERT!! : Material usage is higher than the expected limit for material : Teak , usage difference (%) : 50.0";
    test:assertEquals(outputs.length(), 1);
    test:assertEquals(outputs[0], out);
}
