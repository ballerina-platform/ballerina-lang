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

# Represents http protocol scheme
const string HTTP_SCHEME = "http://";

# Represents https protocol scheme
const string HTTPS_SCHEME = "https://";

# Constant for the http error code
public const string HTTP_ERROR_CODE = "{ballerina/http}HTTPError";

# Constant for the default listener endpoint timeout
const int DEFAULT_LISTENER_TIMEOUT = 120000; //2 mins

# Constant for the default failover starting index for failover endpoints
const int DEFAULT_FAILOVER_EP_STARTING_INDEX = 0;

# Maximum number of requests that can be processed at a given time on a single connection.
const int MAX_PIPELINED_REQUESTS = 10;

# Represents multipart primary type
public const string MULTIPART_AS_PRIMARY_TYPE = "multipart/";

# Constant for the HTTP FORWARD method
public const HTTP_FORWARD = "FORWARD";

# Constant for the HTTP GET method
public const HTTP_GET = "GET";

# Constant for the HTTP POST method
public const HTTP_POST = "POST";

# Constant for the HTTP DELETE method
public const HTTP_DELETE = "DELETE";

# Constant for the HTTP OPTIONS method
public const HTTP_OPTIONS = "OPTIONS";

# Constant for the HTTP PUT method
public const HTTP_PUT = "PUT";

# Constant for the HTTP PATCH method
public const HTTP_PATCH = "PATCH";

# Constant for the HTTP HEAD method
public const HTTP_HEAD = "HEAD";

# Constant for the identify not an HTTP Operation
public const HTTP_NONE = "NONE";

# Defines the possible values for the chunking configuration in HTTP services and clients.
#
# `AUTO`: If the payload is less than 8KB, content-length header is set in the outbound request/response,
#         otherwise chunking header is set in the outbound request/response
# `ALWAYS`: Always set chunking header in the response
# `NEVER`: Never set the chunking header even if the payload is larger than 8KB in the outbound request/response
public type Chunking CHUNKING_AUTO|CHUNKING_ALWAYS|CHUNKING_NEVER;

# If the payload is less than 8KB, content-length header is set in the outbound request/response,
# otherwise chunking header is set in the outbound request/response.}
public const CHUNKING_AUTO = "AUTO";

# Always set chunking header in the response.
public const CHUNKING_ALWAYS = "ALWAYS";

# Never set the chunking header even if the payload is larger than 8KB in the outbound request/response.
public const CHUNKING_NEVER = "NEVER";

# Options to compress using gzip or deflate.
#
# `AUTO`: When service behaves as a HTTP gateway inbound request/response accept-encoding option is set as the
#         outbound request/response accept-encoding/content-encoding option
# `ALWAYS`: Always set accept-encoding/content-encoding in outbound request/response
# `NEVER`: Never set accept-encoding/content-encoding header in outbound request/response
public type Compression COMPRESSION_AUTO|COMPRESSION_ALWAYS|COMPRESSION_NEVER;

# When service behaves as a HTTP gateway inbound request/response accept-encoding option is set as the
# outbound request/response accept-encoding/content-encoding option.
public const COMPRESSION_AUTO = "AUTO";

# Always set accept-encoding/content-encoding in outbound request/response.
public const COMPRESSION_ALWAYS = "ALWAYS";

# Never set accept-encoding/content-encoding header in outbound request/response.
public const COMPRESSION_NEVER = "NEVER";

# Defines the HTTP operations related to circuit breaker, failover and load balancer.
#
# `FORWARD`: Forward the specified payload
# `GET`: Request a resource
# `POST`: Create a new resource
# `DELETE`: Deletes the specified resource
# `OPTIONS`: Request communication options available
# `PUT`: Replace the target resource
# `PATCH`: Apply partial modification to the resource
# `HEAD`: Identical to `GET` but no resource body should be returned
# `NONE`: No operation should be performed
public type HttpOperation HTTP_FORWARD|HTTP_GET|HTTP_POST|HTTP_DELETE|HTTP_OPTIONS|HTTP_PUT|HTTP_PATCH|HTTP_HEAD|HTTP_NONE;

# A record for providing trust store related configurations.
#
# + path - Path to the trust store file
# + password - Trust store password
public type TrustStore record {
    string path = "";
    string password = "";
    !...
};

# A record for providing key store related configurations.
#
# + path - Path to the key store file
# + password - Key store password
public type KeyStore record {
    string path = "";
    string password = "";
    !...
};

# A record for configuring SSL/TLS protocol and version to be used.
#
# + name - SSL Protocol to be used (e.g.: TLS1.2)
# + versions - SSL/TLS protocols to be enabled (e.g.: TLSv1,TLSv1.1,TLSv1.2)
public type Protocols record {
    string name = "";
    string[] versions = [];
    !...
};

# A record for providing configurations for certificate revocation status checks.
#
# + enable - The status of `validateCertEnabled`
# + cacheSize - Maximum size of the cache
# + cacheValidityPeriod - The time period for which a cache entry is valid
public type ValidateCert record {
    boolean enable = false;
    int cacheSize = 0;
    int cacheValidityPeriod = 0;
    !...
};

# A record for providing configurations for certificate revocation status checks.
#
# + enable - The status of OCSP stapling
# + cacheSize - Maximum size of the cache
# + cacheValidityPeriod - The time period for which a cache entry is valid
public type ServiceOcspStapling record {
    boolean enable = false;
    int cacheSize = 0;
    int cacheValidityPeriod = 0;
    !...
};

# A record for providing configurations for content compression.
#
# + enable - The status of compression
# + contentTypes - Content types which are allowed for compression
public type CompressionConfig record {
    Compression enable = COMPRESSION_AUTO;
    string[] contentTypes = [];
    !...
};

type HTTPError record {
    string message = "";
};

//////////////////////////////
/// Native implementations ///
//////////////////////////////

# Parses the given header value to extract its value and parameter map.
#
# + headerValue - The header value
# + return - Returns a tuple containing the value and its parameter map
//TODO: Make the error nillable
public extern function parseHeader (string headerValue) returns (string, map)|error;

function buildRequest(Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message) returns Request {
    Request request = new;
    if (message is ()) {
        return request;
    } else if (message is Request) {
        request = message;
    } else if (message is string) {
        request.setTextPayload(message);
    } else if (message is xml) {
        request.setXmlPayload(message);
    } else if (message is json) {
        request.setJsonPayload(message);
    } else if (message is byte[]) {
        request.setBinaryPayload(message);
    } else if (message is io:ReadableByteChannel) {
        request.setByteChannel(message);
    } else if (message is mime:Entity[]) {
        request.setBodyParts(message);
    }
    return request;
}

function buildResponse(Response|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|() message) returns Response {
    Response response = new;
    if (message is ()) {
        return response;
    } else if (message is Response) {
        response = message;
    } else if (message is string) {
        response.setTextPayload(message);
    } else if (message is xml) {
        response.setXmlPayload(message);
    } else if (message is json) {
        response.setJsonPayload(message);
    } else if (message is byte[]) {
        response.setBinaryPayload(message);
    } else if (message is io:ReadableByteChannel) {
        response.setByteChannel(message);
    } else if (message is mime:Entity[]) {
        response.setBodyParts(message);
    }
    return response;
}

# The HEAD action implementation of the Circuit Breaker. This wraps the `head()` function of the underlying
# HTTP actions provider.

# + path - Resource path
# + outRequest - A Request struct
# + requestAction - `HttpOperation` related to the request
# + httpClient - HTTP client which uses to call the relavant functions
# + return - The response for the request or an `error` if failed to establish communication with the upstream server
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
    error httpConnectorErr = error("Unsupported connector action received.");
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

function populateMultipartRequest(Request inRequest) returns Request|error {
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

function createFailoverRequest(Request request, mime:Entity requestEntity) returns Request|error {
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
