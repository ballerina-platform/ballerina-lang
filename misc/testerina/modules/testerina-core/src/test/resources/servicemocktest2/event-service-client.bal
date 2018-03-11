package src.test.resources.servicemocktest2;

import ballerina.config;
import ballerina.net.http;

string eventServiceEP = "http://localhost:9092/events";

public function getEvents () (json resPl) {

    endpoint<http:HttpClient> httpEndpoint {url: eventServiceEP}
    http:OutRequest req = {};
    http:InResponse resp = {};
    resp, _ = httpEndpoint -> get("/", req);
    resPl, _ = resp.getJsonPayload();
    return;
}