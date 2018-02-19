import ballerina.compression;
import ballerina.log;
import ballerina.net.http;

function main (string[] args) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient("http://localhost:9090", getConnectorConfigs(args[2], args[3]));
    }
    http:OutRequest req = {};
    json jsonMsg = {payload:args[0]};
    req.setJsonPayload(jsonMsg);
    http:InResponse resp = {};
    log:printInfo("Sending request");
    resp, _ = httpEndpoint.get("/echo/", req);
    compression:unzipBytes(resp.getBinaryPayload(), args[1], "");
    log:printInfo("Ballerina package pulled successfully");
}

function getConnectorConfigs (string host, string port ) (http:Options) {
    var portI, _ = <int>port;
    http:Options option = {
                              proxy:{   host: host,
                                        port: portI,
                                        userName:"",
                                        password:""
                                    }
                          };
    return option;
}