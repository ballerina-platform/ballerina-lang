package ballerina.pull;

import ballerina.net.http;
import ballerina.compression;
import ballerina.io;

function main (string[] args) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(args[0], getConnectorConfigs(args[4], args[5], args[6], args[7]));
    }
    http:OutRequest req = {};
    var resp, errRes = httpEndpoint.get("", req);
    if (errRes != null) {
        error err = {message: errRes.message};
        throw err;
    }
    if (resp.statusCode != 200) {
        io:println("Internal server error occured when pulling the ballerina package");
    } else {
        compression:unzipBytes(resp.getBinaryPayload(), args[1], args[2]);
        io:println("[remote -> home-cache] " + args[2]);

        if (args[3] != null){
            compression:unzipBytes(resp.getBinaryPayload(), args[3], args[2]);
            io:println("[remote -> project-cache] " + args[2]);
        }
    }
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
