import ballerina/test;
import ballerina/http;

function startService() {
}

@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() {
    http:Client httpEndpoint = new("http://localhost:9090");
    http:Request req = new;
    json message = {"message":"message to channel"};

    req.setJsonPayload(message);
    var sendResponse = httpEndpoint->post("/channelService/send", req);

    json expResponse = {"send":"Success!!"};
    if(sendResponse is http:Response) {
        test:assertEquals(sendResponse.getJsonPayload(), expResponse);
    }

    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/channelService/receive");
    if (response is http:Response) {
        test:assertEquals(response.getJsonPayload(), message);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
}
