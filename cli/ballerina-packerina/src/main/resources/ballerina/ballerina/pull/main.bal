package ballerina.pull;

import ballerina.net.http;
import ballerina.compression;
import ballerina.io;
import ballerina.file;

function main (string[] args) {
    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(args[0], getConnectorConfigs(args));
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
        string pkgName = args[2];
        string fullPkgPath = args[4];
        // Create directories of the home repo to which the package will be pulled
        string homeRepoDirPath = args[1];
        createDirectories(homeRepoDirPath);
        compression:unzipBytes(resp.getBinaryPayload(), homeRepoDirPath, pkgName);
        io:println(fullPkgPath + " pulled [central.ballerina.io -> home repo]");

        if (args[3] != null){
            // Create directories of the project repo to which the package will be pulled
            string projectRepoDirPath = args[3];
            createDirectories(projectRepoDirPath);
            compression:unzipBytes(resp.getBinaryPayload(), projectRepoDirPath, pkgName);
            io:println(fullPkgPath + " pulled [central.ballerina.io -> project repo]");
        }
    }
}

function getConnectorConfigs(string [] args) (http:Options) {
    string host = args[5];
    string port = args[6];
    string username = args[7];
    string password = args[8];
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

function createDirectories(string directoryPath) {
    file:AccessDeniedError adError;
    file:IOError ioError;
    boolean folderCreated;

    file:File folder = { path: directoryPath };
    folderCreated, adError, ioError = folder.mkdirs();

    if (folderCreated) {
        if (adError != null) {
            error err = { message: "Error occurred while creating the directories", cause: adError.cause };
            throw err;
        } else if (ioError != null) {
            error err = { message: "I/O error occurred while creating the directories", cause: ioError.cause };
            throw err;
        }
    }
}
