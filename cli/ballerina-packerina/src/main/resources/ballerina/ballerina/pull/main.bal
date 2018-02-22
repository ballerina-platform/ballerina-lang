package ballerina.pull;

import ballerina.net.http;
import ballerina.compression;

function main (string[] args) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(args[0], getConnectorConfigs(args[3], args[4], args[5], args[6]));
    }
    http:OutRequest req = {};
    var resp, err = httpEndpoint.get("", req);
    if (err != null) {
        error err = {msg: err.msg};
        throw err;
    }
    if (resp.statusCode != 200) {
        println("Internal server error occured when pulling the ballerina package");
    } else {
        compression:unzipBytes(resp.getBinaryPayload(), args[1], args[2]);
        println("Ballerina package pulled successfully");
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