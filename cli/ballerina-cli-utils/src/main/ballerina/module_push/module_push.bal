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

# This functions pulls a module from ballerina central.
#
# + definedEndpoint - Endpoint defined with the proxy configurations
# + accessToken - Access token
# + mdFileContent - Module.md file content of the module
# + summary - Summary of the module
# + repositoryURL - Source code URL of the module
# + authors - Authors of the module
# + keywords - Keywords which describes the module
# + license - License of the module
# + url - URL to be invoked to push the module
# + dirPath - Directory path where the archived module resides
# + ballerinaVersion - Ballerina version the module is built
# + msg - Message printed when the module is pushed successfully which includes module info
# + baloVersion - Balo version of the module
# + return - Error if occurred, else nil
function pushPackage (http:Client definedEndpoint, string accessToken, string mdFileContent, string summary, string repositoryURL,
                string authors, string keywords, string license, string url, string dirPath, string ballerinaVersion, string msg,
                string baloVersion) returns @tainted error? {

    http:Client httpEndpoint = definedEndpoint;
    mime:Entity mdFileContentBodyPart = addStringBodyParts("description", mdFileContent);
    mime:Entity summaryBodyPart = addStringBodyParts("summary", summary);
    mime:Entity repositoryURLBodyPart = addStringBodyParts("repositoryURL", repositoryURL);
    mime:Entity authorsBodyPart = addStringBodyParts("authors", authors);
    mime:Entity keywordsBodyPart = addStringBodyParts("keywords", keywords);
    mime:Entity licenseBodyPart = addStringBodyParts("license", license);
    mime:Entity ballerinaVersionBodyPart = addStringBodyParts("ballerinaVersion", ballerinaVersion);
    mime:Entity baloVersionBodyPart = addStringBodyParts("baloVersion", baloVersion);

    // Artifact
    mime:Entity filePart = new;
    filePart.setContentDisposition(getContentDispositionForFormData("artifact"));
    filePart.setFileAsEntityBody(dirPath);
    var contentTypeSetResult = filePart.setContentType(mime:APPLICATION_OCTET_STREAM);
    if (contentTypeSetResult is error)  {
        panic contentTypeSetResult;
    }

    mime:Entity[] bodyParts = [filePart, mdFileContentBodyPart, summaryBodyPart, repositoryURLBodyPart,
                               authorsBodyPart, keywordsBodyPart, licenseBodyPart, ballerinaVersionBodyPart,
                               baloVersionBodyPart];
    http:Request req = new;
    req.addHeader("Authorization", "Bearer " + accessToken);
    req.setBodyParts(bodyParts, contentType = mime:MULTIPART_FORM_DATA);

    var result = httpEndpoint -> post("", req);
    http:Response httpResponse = new;
    if (result is http:Response) {
        httpResponse = result;
    } else {
        return createError("connection to the remote host failed : " + result.reason());
    }
    string statusCode = string.convert(httpResponse.statusCode);
    if (statusCode.hasPrefix("5")) {
        return createError("remote registry failed for url :" + url);
    } else if (statusCode != "200") {
        var jsonResponse = httpResponse.getJsonPayload();
        if (jsonResponse is json) {
            string message = jsonResponse.message.toString();
            return createError(message);
        } else {
            return createError("invalid response json");
        }
    } else {
        io:println(msg);
    }
}

# This function will invoke the method to push the module.
# + args - Arguments passed
# + return - nil if no error occurred, else error.
public function main (string... args) returns error? {
    http:Client httpEndpoint;
    string host = args[11];
    string strPort = args[12];
    if (host != "" && strPort != "") {
        var port = int.convert(strPort);
        if (port is int) {
            http:Client|error result = trap defineEndpointWithProxy(args[7], host, port, args[13], args[14]);
            if (result is http:Client) {
                httpEndpoint = result;
                return <@untainted> pushPackage(httpEndpoint, args[0], args[1], args[2], args[3], args[4], args[5],
                    args[6], args[7], args[8], args[10], args[9], args[15]);
            } else {
                return createError("failed to resolve host : " + host + " with port " + port);
            }
        } else {
            return createError("invalid port : " + strPort);
        }
    } else  if (host != "" || strPort != "") {
        return createError("both host and port should be provided to enable proxy");
    } else {
        httpEndpoint = defineEndpointWithoutProxy(args[9]);
        return <@untainted> pushPackage(httpEndpoint, args[0], args[1], args[2], args[3], args[4], args[5], args[6],
            args[7], args[8], args[10], args[9], args[15]);
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
function defineEndpointWithProxy (string url, string hostname, int port, string username, string password) returns http:Client {
    http:Client httpEndpoint = new (url, config = {
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            verifyHostname: false,
            shareSession: true
        },
            proxy : getProxyConfigurations(hostname, port, username, password)
    });
    return httpEndpoint;
}

# This function defines an endpoint without proxy configurations.
#
# + url - URL to be invoked
# + return - Endpoint defined
function defineEndpointWithoutProxy (string url) returns http:Client{
    http:Client httpEndpoint = new (url, config = {
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            verifyHostname: false,
            shareSession: true
        }
    });
    return httpEndpoint;
}

# This function will get the content disposition of the form data sent.
#
# + partName - Multipart name
# + return - `ContentDisposition` of the multipart
function getContentDispositionForFormData(string partName) returns (mime:ContentDisposition){
    mime:ContentDisposition contentDisposition = new;
    contentDisposition.name =  partName;
    contentDisposition.disposition = "form-data";
    return contentDisposition;
}

# This function will add string part information in multiparts.
#
# + key - Name of the multipart
# + value - String value to be included in the multipart
# + return - `Entity` with the part information.
function addStringBodyParts (string key, string value) returns (mime:Entity) {
    mime:Entity stringBodyPart = new;
    stringBodyPart.setContentDisposition(getContentDispositionForFormData(key));
    stringBodyPart.setText(value);
    var contentTypeSetResult = stringBodyPart.setContentType(mime:TEXT_PLAIN);
    if (contentTypeSetResult is error)  {
        panic contentTypeSetResult;
    }
    return stringBodyPart;
}

# This function sets the proxy configurations for the endpoint.
#
# + hostName - Host name of the proxy
# + port - Port of the proxy
# + username - Username of the proxy
# + password - Password of the proxy
# + return - Proxy configurations for the endpoint
function getProxyConfigurations(string hostName, int port, string username, string password) returns http:ProxyConfig {
    http:ProxyConfig proxy = { host : hostName, port : port , userName: username, password : password };
    return proxy;
}

# Creates an error record.

# + errMessage - The error message.
# + return - Newly created error record.
function createError (string errMessage) returns error {
    error err = error(errMessage);
    return err;
}
