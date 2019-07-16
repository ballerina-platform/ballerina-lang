// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;
import ballerina/mime;
import ballerina/http;
import ballerina/'lang\.int as lint;
import ballerina/internal;

# This functions pulls a module from ballerina central.
#
# + definedEndpoint - Endpoint defined with the proxy configurations
# + accessToken - Access token
# + url - URL to be invoked to push the module
# + baloPath - Path to balo file
# + outputLog - Message printed when the module is pushed successfully which includes module info
function pushPackage (http:Client definedEndpoint, string accessToken, string url, string baloPath, string outputLog) {
    http:Client httpEndpoint = definedEndpoint;
    http:Request req = new;
    req.addHeader("Authorization", "Bearer " + accessToken);
    req.setFileAsPayload(baloPath, mime:APPLICATION_OCTET_STREAM);
    http:Response|error response = httpEndpoint->post("", req);

    if (response is error) {
        error e = response;
        string? errMsg = e.detail()?.message;
        if (errMsg is string) {
            panic createError("connection to the remote host failed : " + errMsg);
        } else {
            panic createError("connection to the remote host failed.");
        }
    } else {
        string statusCode = response.statusCode.toString();
        if (internal:hasPrefix(statusCode, "5")) {
            panic createError("error occured in remote registry. url: " + url);
        } else if (statusCode != "200") {
            json|error jsonResponse = response.getJsonPayload();
            if (jsonResponse is error) {
                panic createError("invalid response json");
            } else {
                string message = jsonResponse.message.toString();
                panic createError(message);
            }
        } else {
            io:println(outputLog);
        }
    }
}

# This function will invoke the method to push the module.
#
# + args - Arguments passed.
public function main(string... args) {
    http:Client httpEndpoint;
    string urlWithModulePath = args[0];
    string proxyHost = args[1];
    string proxyPortAsString = args[2];
    string proxyUsername = args[3];
    string proxyPassword = args[4];
    string accessToken = args[5];
    string pathToBalo = args[6];
    string outputLog = args[7];
    if (proxyHost != "" && proxyPortAsString != "") {
        int|error proxyPort = lint:fromString(proxyPortAsString);
        if (proxyPort is int) {
            http:Client|error result = trap defineEndpointWithProxy(urlWithModulePath, proxyHost, proxyPort, proxyUsername, proxyPassword);
            if (result is http:Client) {
                httpEndpoint = result;
                pushPackage(httpEndpoint, accessToken, urlWithModulePath, pathToBalo, outputLog);
            } else {
                panic createError("failed to resolve host : " + proxyHost + " with port " + proxyPortAsString);
            }
        } else {
            panic createError("invalid port : " + proxyPortAsString);
        }
    } else  if (proxyHost != "" || proxyPortAsString != "") {
        panic createError("both host and port should be provided to enable proxy");
    } else {
        httpEndpoint = defineEndpointWithoutProxy(urlWithModulePath);
        return <@untainted> pushPackage(httpEndpoint, accessToken, urlWithModulePath, pathToBalo, outputLog);
    }
}

# This function defines an endpoint with proxy configurations.
#
# + url - URL to be invoked
# + hostname - Host name of the proxy
# + port - Port of the proxy
# + username - Username of the proxy
# + password - Password of the proxy
# + return - Endpoint defined
function defineEndpointWithProxy(string url, string hostname, int port, string username, string password) returns http:Client {
    http:Client httpEndpoint = new(url, {
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            verifyHostname: false,
            shareSession: true
        },
        proxy : getProxyConfigurations(hostname, port, username, password),
        cache: {
            enabled: false
        }
    });
    return httpEndpoint;
}

# This function defines an endpoint without proxy configurations.
#
# + url - URL to be invoked
# + return - Endpoint defined
function defineEndpointWithoutProxy (string url) returns http:Client{
    http:Client httpEndpoint = new(url, {
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            verifyHostname: false,
            shareSession: true
        },
        cache: {
            enabled: false
        }
    });
    return httpEndpoint;
}

# Get the byte array of a file.
#
# + filePath - The path of the file
# + return - The byte array or error when reading
function getByteArrayOfFile(string filePath) returns byte[]|error {
    io:ReadableByteChannel|error src = io:openReadableFile(filePath);
    if (src is error) {
        panic createError("error reading balo file. path: " + filePath);
    } else {
        int readCount = 1;
        byte[] readContent;

        while (readCount > 0) {
            [byte[], int]|error result = src.read(1024);
            if (result is error) {
                panic createError("error reading bytes of balo file. path: " + filePath);
            } else {
                [readContent, readCount] = result;
            }
        }
        return <@untainted> readContent;
    }
}

# This function sets the proxy configurations for the endpoint.
#
# + hostName - Host name of the proxy
# + port - Port of the proxy
# + username - Username of the proxy
# + password - Password of the proxy
# + return - Proxy configurations for the endpoint
function getProxyConfigurations(string hostName, int port, string username, string password) returns http:ProxyConfig {
    http:ProxyConfig proxy = {
        host: hostName,
        port: port,
        userName: username,
        password: password
    };
    return proxy;
}

# Creates an error record.

# + errMessage - The error message.
# + return - Newly created error record.
function createError (string errMessage) returns error {
    error err = error(errMessage);
    return err;
}
