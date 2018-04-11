import ballerina/test;
import ballerina/io;

boolean serviceStarted;

function startService(){
    serviceStarted = test:startServices("header-based-routing");
}

@test:Config {
    before:"startService",
    after:"stopService"
}
function testFunc() {
    // Invoking the main function
    endpoint http:Client httpEndpoint { targets:[{ url:"http://localhost:9090" }] };
    // Chck whether the server is started
    test:assertTrue(serviceStarted, msg = "Unable to start the service");

    string response1 = "{
        \"name\": \"Colombo,Sri Lanka\",
        \"longitude\": -556.49,
        \"latitude\": 257.76,
        \"altitude\": 230,
    }";


    http:Request req = new;
    req.setHeader("type", "location");
    // Send a GET request to the specified endpoint
    var response = httpEndpoint -> get("/hbr/route", req);
    match response {
        http:Response resp => {
            var stringRes = check resp.getStringPayload();
            test:assertTrue(stringRes.contains("\"name\": \"Colombo,Sri Lanka\""));
            test:assertTrue(stringRes.contains("\"longitude\": -556.49"));
            test:assertTrue(stringRes.contains("\"latitude\": 257.76"));
            test:assertTrue(stringRes.contains("\"altitude\": 230"));
        }
        http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService(){
    test:stopServices("header-based-routing");
}