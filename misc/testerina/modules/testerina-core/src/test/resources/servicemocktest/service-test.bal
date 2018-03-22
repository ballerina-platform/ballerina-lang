package servicemocktest;

import ballerina/net.http;
import ballerina/io;
import ballerina/test;
import ballerina/config;

endpoint http:ServiceEndpoint eventEP {
    port: 9092
};

string url1 = "http://0.0.0.0:9092/events";
string url2 = "http://0.0.0.0:9090/portal";
boolean isEventServiceStarted;
boolean isPortalServiceStarted;
boolean isNonExistingServiceStarted;

@http:ServiceConfig {
    basePath: "/events"
}
service<http:Service> EventServiceMock bind eventEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    getEvents (endpoint client, http:Request req) {
        http:Response res = {};
        json j = {"a":"b"};
        res.setJsonPayload(j);
        _ = client -> respond(res);
    }
}

function init() {
    isEventServiceStarted = test:startServices("servicemocktest");
    isPortalServiceStarted = test:startServices("servicemocktest2");
    isNonExistingServiceStarted = test:startServices("servicemocktestX");
}

@test:Config{before: "init"}
function testService () {
    endpoint http:ClientEndpoint httpEndpoint {
        targets:[{
            uri:url2
        }]
    };

    test:assertTrue(isEventServiceStarted, msg = "Event service failed to start");
    test:assertTrue(isPortalServiceStarted, msg = "Portal service failed to start");
    test:assertFalse(isNonExistingServiceStarted);

    http:Request req = {};
    // Send a GET request to the specified endpoint
    io:println("GET request:");
    var response = httpEndpoint -> get("/events", req);
    match response {
               http:Response resp => {
                    var jsonRes = resp.getJsonPayload();
                    json expected = {"a":"b"};
                    test:assertEquals(jsonRes, expected);
               }
               http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint: "+url2);
    }

    test:stopServices("servicemocktest");
    test:stopServices("servicemocktest2");
}