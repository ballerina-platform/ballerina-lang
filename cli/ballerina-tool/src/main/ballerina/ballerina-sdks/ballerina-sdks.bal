import ballerina/http;
import ballerina/io;

public function main() {
   // Creates an HTTP client to interact with a remote endpoint.
    http:Client clientEP = new("http://localhost:9090");
    http:Request req = new;
    req.setHeader("user-agent", "jBallerina/0.992.0 (win-64) Updater/1.0");
    var resp = clientEP->get("/distributions/", message = req);

    if (resp is http:Response) {
        var payload = resp.getJsonPayload();
        if (payload is json) {
            io:println(payload.list);
            json[] list = <json[]>payload.list;
            foreach var v in list {
                string output = v.^"type".toString() + " - " + v.^"version".toString();
                io:println(output);
            }
        } else {
            io:println(payload.detail().message);
        }
    } else {
        io:println(resp.detail().message);
    }
}
