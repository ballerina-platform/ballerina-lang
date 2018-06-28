import ballerina/io;
import ballerina/http;
import ballerina/log;
import ballerina/mime;

endpoint http:Client clientEP {
    url: "http://localhost:9090",
    followRedirects: { enabled: true, maxCount: 5 }
};

function main(string... args) {

    // Send a GET request to the specified endpoint.
    var returnResult = clientEP->get("/redirect1");

    match returnResult {
        error connectionErr => log:printError("Error in connection",
            err = connectionErr);
        http:Response resp => {
            match resp.getTextPayload() {
                error e => log:printError("Error in payload", err = e);
                string payload => io:println("Response received : " + payload);
            }
        }
    }
}

@http:ServiceConfig {
    basePath: "/redirect1"
}
service<http:Service> redirect1 bind { port: 9090 } {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    redirect1(endpoint client, http:Request req) {
        http:Response res = new;
        //Send a redirect response with a location
        _ = client->redirect(res, http:REDIRECT_TEMPORARY_REDIRECT_307,
            ["http://localhost:9093/redirect2"]);
    }
}

@http:ServiceConfig {
    basePath: "/redirect2"
}
service<http:Service> redirect2 bind { port: 9093 } {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    redirect2(endpoint client, http:Request req) {
        http:Response res = new;
        res.setPayload("Hello World!");
        _ = client->respond(res) but { error e => log:printError("Error in
        responding", err = e) };
    }
}
