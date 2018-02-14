package ballerina.pull;

import ballerina.net.http;
import ballerina.compression;

function main (string[] args) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(args[0], {});
    }
    http:OutRequest req = {};
    http:InResponse resp = {};
    resp, _ = httpEndpoint.get("", req);
    compression:unzipBytes(resp.getBinaryPayload(), args[1]);
    println("Ballerina package pulled successfully");
}