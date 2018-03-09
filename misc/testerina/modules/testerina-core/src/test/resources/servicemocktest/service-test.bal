package src.test.resources.servicemocktest;

import ballerina.net.http;
import ballerina.io;
import ballerina.test;
import ballerina.config;
import src.test.resources.servicemocktest2;

string url1;
string url2;

@http:configuration {
    basePath:"/events",
    port:9092
}
service<http> EventServiceMock {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource getEvents (http:Connection conn,http:InRequest req) {
        http:OutResponse res = {};
        json j = {"a":"b"};
        res.setJsonPayload(j);
        _ = conn.respond(res);
    }
}

function init() {
    url1 = test:startService("EventServiceMock");
    // currently the url is hard coded; need to set url1 in to config api here
    url2 = test:startService("PortalService");
}

@test:config{before: "init"}
function testService () {
    endpoint<http:HttpClient> httpEndpoint {
                              create http:HttpClient(url2, {});
    }

    http:OutRequest req = {};
    // Send a GET request to the specified endpoint
    http:InResponse resp = {};
    resp, _ = httpEndpoint.get("/events", req);

    io:println("GET request:");
    var jsonRes, _ = resp.getJsonPayload();
    io:println(jsonRes);
    json expected = {"a":"b"};
    test:assertEquals(jsonRes, expected, "failed");
}