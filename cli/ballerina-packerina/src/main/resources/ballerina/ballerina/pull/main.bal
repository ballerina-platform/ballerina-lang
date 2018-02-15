package ballerina.pull;

import ballerina.net.http;
import ballerina.compression;

function main (string[] args) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(args[0], getConnectorConfigs(args[2], args[3], args[4], args[5]));
    }
    http:OutRequest req = {};
    http:InResponse resp = {};
    resp, _ = httpEndpoint.get("", req);
    compression:unzipBytes(resp.getBinaryPayload(), args[1]);
    println("Ballerina package pulled successfully");
}

function getConnectorConfigs(string host, string port, string username, string password) (http:Options) {
    http:Options option;
    int portInt = 0;
    if (host != ""){
        if (port != ""){
            var portI, _ = <int> port;
            portInt = portI;
        }
        option = {
                     proxy:{
                               host:host,
                               port:portInt,
                               userName:username,
                               password:password
                           }
                 };
    } else {
        option = {};
    }
    return option;
}