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

package ballerina.net.http.resiliency;

import ballerina.net.http;

//Constants to represent Failover connector actions.
public const string FORWARD = "forward";
public const string GET = "get";
public const string POST = "post";
public const string DELETE = "delete";
public const string OPTIONS = "options";
public const string PUT = "put";
public const string PATCH = "patch";
public const string HEAD = "head";

enum HttpOperation {
    GET, POST, DELETE, OPTIONS, PUT, PATCH, HEAD, FORWARD
}

// makes the actual endpoints call according to the http operation passed in.
public function invokeEndpoint (string path, http:OutRequest outRequest, http:InRequest inRequest,
                                HttpOperation requestAction, http:HttpClient httpClient)
(http:InResponse, http:HttpConnectorError) {

    endpoint<http:HttpClient> endPoint {
    }

    bind httpClient with endPoint;

    if (HttpOperation.GET == requestAction) {
        return endPoint.get(path, outRequest);
    } else if (HttpOperation.POST == requestAction) {
        return endPoint.post(path, outRequest);
    } else if (HttpOperation.OPTIONS == requestAction) {
        return endPoint.options(path, outRequest);
    } else if (HttpOperation.PUT == requestAction) {
        return endPoint.put(path, outRequest);
    } else if (HttpOperation.DELETE == requestAction) {
        return endPoint.delete(path, outRequest);
    } else if (HttpOperation.PATCH == requestAction) {
        return endPoint.patch(path, outRequest);
    } else if (HttpOperation.FORWARD == requestAction) {
        return endPoint.forward(path, inRequest);
    } else if (HttpOperation.HEAD == requestAction) {
        return endPoint.head(path, outRequest);
    } else {
        http:HttpConnectorError httpConnectorError = {};
        httpConnectorError.statusCode = 400;
        httpConnectorError.message = "Unsupported connector action received.";
        return null, httpConnectorError;
    }
}

// Extracts HttpOperation from the Http verb passed in.
function extractHttpOperation (string httpVerb) (HttpOperation connectorAction) {
    HttpOperation inferredConnectorAction;
    if (GET == httpVerb) {
        inferredConnectorAction = HttpOperation.GET;
    } else if (POST == httpVerb) {
        inferredConnectorAction = HttpOperation.POST;
    } else if (OPTIONS == httpVerb) {
        inferredConnectorAction = HttpOperation.OPTIONS;
    } else if (PUT == httpVerb) {
        inferredConnectorAction = HttpOperation.PUT;
    } else if (DELETE == httpVerb) {
        inferredConnectorAction = HttpOperation.DELETE;
    } else if (PATCH == httpVerb) {
        inferredConnectorAction = HttpOperation.PATCH;
    } else if (FORWARD == httpVerb) {
        inferredConnectorAction = HttpOperation.FORWARD;
    } else if (HEAD == httpVerb) {
        inferredConnectorAction = HttpOperation.HEAD;
    }
    return inferredConnectorAction;
}

// Populate boolean index array by looking at the configured Http status codes to get better performance
// at runtime.
function populateErrorCodeIndex (int[] errorCode) (boolean[] result) {
    result = [];
    foreach i in errorCode {
        result[i] = true;
    }
    return result;
}
