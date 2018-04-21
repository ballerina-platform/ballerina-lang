import ballerina/http;
import ballerina/io;
import ballerina/test;
import ballerina/config;

endpoint http:Listener eventEP {
    port: 9092
};

string url1 = "http://0.0.0.0:9092/events";
string url2 = "http://0.0.0.0:9093/portal";
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
    getEvents (endpoint caller, http:Request req) {
        http:Response res = new;
        json j = {"a":"b"};
        res.setJsonPayload(j);
        _ = caller -> respond(res);
    }
}

function init() {
    isEventServiceStarted = test:startServices("servicemocktest");
    isPortalServiceStarted = test:startServices("servicemocktest2");
    isNonExistingServiceStarted = test:startServices("servicemocktestX");
}

function verify() {
    // verifies whether the service got stopped correctly
    endpoint http:Client httpEndpoint {
        url:url2
    };

    http:Request req = new;
    // Send a GET request to the specified endpoint - this should return connection refused
    var response = httpEndpoint -> get("/events", req);
    match response {
        http:Response resp =>  test:assertFail(msg = "Service stop has failed for: "+url2);
        http:HttpConnectorError err => {
            test:assertEquals(err.message, "Connection refused: /0.0.0.0:9090");
        }
    }
}

@test:Config{before: "init", after: "verify"}
function testService () {
    endpoint http:Client httpEndpoint {
        url:url2
    };

    test:assertTrue(isEventServiceStarted, msg = "Event service failed to start");
    test:assertTrue(isPortalServiceStarted, msg = "Portal service failed to start");
    test:assertFalse(isNonExistingServiceStarted);

    http:Request req = new;
    // Send a GET request to the specified endpoint
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