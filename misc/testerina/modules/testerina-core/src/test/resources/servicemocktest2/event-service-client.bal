package servicemocktest2;

import ballerina/http;
import ballerina/io;
import ballerina/mime;

string eventServiceEP = "http://localhost:9092/events";

public function getEvents () returns (json) {

    endpoint http:Client httpEndpoint {
        targets:[{ url:eventServiceEP }]
    };
    http:Request req = new;
    var response = httpEndpoint -> get("/", req);

    match response {
                   http:Response resp => {
                        var jsonRes = resp.getJsonPayload();
                        match jsonRes {
                            json payload => return payload;
                            mime:EntityError err => io:println(err);
                        }
                   }
                   http:HttpConnectorError err => io:println(err);
    }

    return {};
}