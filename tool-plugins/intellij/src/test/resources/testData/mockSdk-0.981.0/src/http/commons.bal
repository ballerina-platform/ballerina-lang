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

@final string HTTP_SCHEME = "http://";
@final string HTTPS_SCHEME = "https://";

@final int DEFAULT_LISTENER_TIMEOUT = 120000; //2 mins

documentation {Constant for the default failover starting index for failover endpoints}
@final int DEFAULT_FAILOVER_EP_STARTING_INDEX = 0;

documentation {Represents multipart primary type}
@final public string MULTIPART_AS_PRIMARY_TYPE = "multipart/";
// TODO: Document these. Should we make FORWARD a private constant?
documentation {Constant for the HTTP FORWARD method}
@final public HttpOperation HTTP_FORWARD = "FORWARD";
documentation {Constant for the HTTP GET method}
@final public HttpOperation HTTP_GET = "GET";
documentation {Constant for the HTTP POST method}
@final public HttpOperation HTTP_POST = "POST";
documentation {Constant for the HTTP DELETE method}
@final public HttpOperation HTTP_DELETE = "DELETE";
documentation {Constant for the HTTP OPTIONS method}
@final public HttpOperation HTTP_OPTIONS = "OPTIONS";
documentation {Constant for the HTTP PUT method}
@final public HttpOperation HTTP_PUT = "PUT";
documentation {Constant for the HTTP PATCH method}
@final public HttpOperation HTTP_PATCH = "PATCH";
documentation {Constant for the HTTP HEAD method}
@final public HttpOperation HTTP_HEAD = "HEAD";
@final public HttpOperation HTTP_NONE = "NONE";

documentation {
    Defines the HTTP operations related to circuit breaker, failover and load balancer.

    `FORWARD`: Forward the specified payload
    `GET`: Request a resource
    `POST`: Create a new resource
    `DELETE`: Deletes the specified resource
    `OPTIONS`: Request communication options available
    `PUT`: Replace the target resource
    `PATCH`: Apply partial modification to the resource
    `HEAD`: Identical to `GET` but no resource body should be returned
    `NONE`: No operation should be performed
}
public type HttpOperation "FORWARD" | "GET" | "POST" | "DELETE" | "OPTIONS" | "PUT" | "PATCH" | "HEAD" | "NONE";

// makes the actual endpoints call according to the http operation passed in.
documentation {
    The HEAD action implementation of the Circuit Breaker. This wraps the `head()` function of the underlying
    HTTP actions provider.

    P{{path}} Resource path
    P{{outRequest}} A Request struct
    P{{requestAction}} `HttpOperation` related to the request
    P{{httpClient}} HTTP client which uses to call the relavant functions
    R{{}} The response for the request or an `error` if failed to establish communication with the upstream server
}
public function invokeEndpoint (string path, Request outRequest,
                                HttpOperation requestAction, CallerActions httpClient) returns Response|error {
    if (HTTP_GET == requestAction) {
        return httpClient.get(path, message = outRequest);
    } else if (HTTP_POST == requestAction) {
        return httpClient.post(path, outRequest);
    } else if (HTTP_OPTIONS == requestAction) {
        return httpClient.options(path, message = outRequest);
    } else if (HTTP_PUT == requestAction) {
        return httpClient.put(path, outRequest);
    } else if (HTTP_DELETE == requestAction) {
        return httpClient.delete(path, outRequest);
    } else if (HTTP_PATCH == requestAction) {
        return httpClient.patch(path, outRequest);
    } else if (HTTP_FORWARD == requestAction) {
        return httpClient.forward(path, outRequest);
    } else if (HTTP_HEAD == requestAction) {
        return httpClient.head(path, message = outRequest);
    } else {
        return getError();
    }
}

// Extracts HttpOperation from the Http verb passed in.
function extractHttpOperation (string httpVerb) returns HttpOperation {
    HttpOperation inferredConnectorAction = HTTP_NONE;
    if ("GET" == httpVerb) {
        inferredConnectorAction = HTTP_GET;
    } else if ("POST" == httpVerb) {
        inferredConnectorAction = HTTP_POST;
    } else if ("OPTIONS" == httpVerb) {
        inferredConnectorAction = HTTP_OPTIONS;
    } else if ("PUT" == httpVerb) {
        inferredConnectorAction = HTTP_PUT;
    } else if ("DELETE" == httpVerb) {
        inferredConnectorAction = HTTP_DELETE;
    } else if ("PATCH" == httpVerb) {
        inferredConnectorAction = HTTP_PATCH;
    } else if ("FORWARD" == httpVerb) {
        inferredConnectorAction = HTTP_FORWARD;
    } else if ("HEAD" == httpVerb) {
        inferredConnectorAction = HTTP_HEAD;
    }
    return inferredConnectorAction;
}

// Populate boolean index array by looking at the configured Http status codes to get better performance
// at runtime.
function populateErrorCodeIndex (int[] errorCode) returns boolean[] {
    boolean[] result = [];
    foreach i in errorCode {
        result[i] = true;
    }
    return result;
}

function getError() returns error {
    error httpConnectorErr = {};
    httpConnectorErr.message = "Unsupported connector action received.";
    return httpConnectorErr;
}

function populateRequestFields (Request originalRequest, Request newRequest)  {
    newRequest.rawPath = originalRequest.rawPath;
    newRequest.method = originalRequest.method;
    newRequest.httpVersion = originalRequest.httpVersion;
    newRequest.cacheControl = originalRequest.cacheControl;
    newRequest.userAgent = originalRequest.userAgent;
    newRequest.extraPathInfo = originalRequest.extraPathInfo;
}

function populateMultipartRequest(Request inRequest) returns Request {
    if (isMultipartRequest(inRequest)) {
        mime:Entity[] bodyParts = check inRequest.getBodyParts();
        foreach bodyPart in bodyParts {
            if (isNestedEntity(bodyPart)) {
                mime:Entity[] childParts = check bodyPart.getBodyParts();
                foreach childPart in childParts {
                    // When performing passthrough scenarios, message needs to be built before
                    // invoking the endpoint to create a message datasource.
                    var childBlobContent = childPart.getByteArray();
                }
                bodyPart.setBodyParts(childParts, contentType = untaint bodyPart.getContentType());
            } else {
                var bodyPartBlobContent = bodyPart.getByteArray();
            }
        }
        inRequest.setBodyParts(bodyParts, contentType = untaint inRequest.getContentType());
    }
    return inRequest;
}

function isMultipartRequest(Request request) returns boolean {
    return request.hasHeader(mime:CONTENT_TYPE) &&
        request.getHeader(mime:CONTENT_TYPE).hasPrefix(MULTIPART_AS_PRIMARY_TYPE);
}

function isNestedEntity(mime:Entity entity) returns boolean {
    return entity.hasHeader(mime:CONTENT_TYPE) &&
        entity.getHeader(mime:CONTENT_TYPE).hasPrefix(MULTIPART_AS_PRIMARY_TYPE);
}

function createFailoverRequest(Request request, mime:Entity requestEntity) returns Request {
    if (isMultipartRequest(request)) {
        return populateMultipartRequest(request);
    } else {
        Request newOutRequest = new;
        populateRequestFields(request, newOutRequest);
        newOutRequest.setEntity(requestEntity);
        return newOutRequest;
    }
}

//Resolve a given path against a given URI.
extern function resolve(string baseUrl, string path) returns string|error;
