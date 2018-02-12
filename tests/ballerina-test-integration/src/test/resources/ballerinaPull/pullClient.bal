import ballerina.compression;
import ballerina.log;
import ballerina.net.http;

function main (string[] args) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient("http://localhost:9090", {});
    }
    http:OutRequest req = {};
    json jsonMsg = {payload:args[0]};
    req.setJsonPayload(jsonMsg);
    http:InResponse resp = {};
    log:printInfo("Sending request");
    resp, _ = httpEndpoint.get("/echo/", req);
    compression:unzipBytes(resp.getBinaryPayload(), args[1]);
    log:printInfo("Ballerina package pulled successfully");
}