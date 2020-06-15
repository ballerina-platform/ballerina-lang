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

import ballerina/java;
import ballerina/mime;
import ballerina/io;

# Represents HTTP/1.0 protocol
const string HTTP_1_0 = "1.0";

# Represents HTTP/1.1 protocol
const string HTTP_1_1 = "1.1";

# Represents HTTP/2.0 protocol
const string HTTP_2_0 = "2.0";

# Defines the supported HTTP protocols.
#
# `HTTP_1_0`: HTTP/1.0 protocol
# `HTTP_1_1`: HTTP/1.1 protocol
# `HTTP_2_0`: HTTP/2.0 protocol
public type HttpVersion HTTP_1_0|HTTP_1_1|HTTP_2_0;

# Represents http protocol scheme
const string HTTP_SCHEME = "http://";

# Represents https protocol scheme
const string HTTPS_SCHEME = "https://";

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

# constant for the HTTP SUBMIT method
public const HTTP_SUBMIT = "SUBMIT";

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

# The types of messages that are accepted by HTTP `client` when sending out the outbound request.
public type RequestMessage Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|();

# The types of messages that are accepted by HTTP `listener` when sending out the outbound response.
public type ResponseMessage Response|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|();

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
# `SUBMIT`: Submits a http request and returns an HttpFuture object
# `NONE`: No operation should be performed
public type HttpOperation HTTP_FORWARD|HTTP_GET|HTTP_POST|HTTP_DELETE|HTTP_OPTIONS|HTTP_PUT|HTTP_PATCH|HTTP_HEAD
                                                                                                |HTTP_SUBMIT|HTTP_NONE;

// Common type used for HttpFuture and Response used for resiliency clients.
type HttpResponse Response|HttpFuture;

# A record for configuring SSL/TLS protocol and version to be used.
#
# + name - SSL Protocol to be used (e.g.: TLS1.2)
# + versions - SSL/TLS protocols to be enabled (e.g.: TLSv1,TLSv1.1,TLSv1.2)
public type Protocols record {|
    string name = "";
    string[] versions = [];
|};

# A record for providing configurations for certificate revocation status checks.
#
# + enable - The status of `validateCertEnabled`
# + cacheSize - Maximum size of the cache
# + cacheValidityPeriod - The time period for which a cache entry is valid
public type ValidateCert record {|
    boolean enable = false;
    int cacheSize = 0;
    int cacheValidityPeriod = 0;
|};

# A record for providing configurations for certificate revocation status checks.
#
# + enable - The status of OCSP stapling
# + cacheSize - Maximum size of the cache
# + cacheValidityPeriod - The time period for which a cache entry is valid
public type ListenerOcspStapling record {|
    boolean enable = false;
    int cacheSize = 0;
    int cacheValidityPeriod = 0;
|};

# A record for providing configurations for content compression.
#
# + enable - The status of compression
# + contentTypes - Content types which are allowed for compression
public type CompressionConfig record {|
    Compression enable = COMPRESSION_AUTO;
    string[] contentTypes = [];
|};

type HTTPError record {
    string message = "";
};

# Common client configurations for the next level clients.
#
# + httpVersion - The HTTP version understood by the client
# + http1Settings - Configurations related to HTTP/1.x protocol
# + http2Settings - Configurations related to HTTP/2 protocol
# + timeoutInMillis - The maximum time to wait (in milliseconds) for a response before closing the connection
# + forwarded - The choice of setting `forwarded`/`x-forwarded` header
# + followRedirects - Configurations associated with Redirection
# + poolConfig - Configurations associated with request pooling
# + cache - HTTP caching related configurations
# + compression - Specifies the way of handling compression (`accept-encoding`) header
# + auth - HTTP authentication-related configurations
# + circuitBreaker - Configurations associated with the behaviour of the Circuit Breaker
# + retryConfig - Configurations associated with retrying
# + cookieConfig - Configurations associated with cookies
public type CommonClientConfiguration record {|
    string httpVersion = HTTP_1_1;
    ClientHttp1Settings http1Settings = {};
    ClientHttp2Settings http2Settings = {};
    int timeoutInMillis = 60000;
    string forwarded = "disable";
    FollowRedirects? followRedirects = ();
    PoolConfiguration? poolConfig = ();
    CacheConfig cache = {};
    Compression compression = COMPRESSION_AUTO;
    OutboundAuthConfig? auth = ();
    CircuitBreakerConfig? circuitBreaker = ();
    RetryConfig? retryConfig = ();
    CookieConfig? cookieConfig = ();
|};

//////////////////////////////
/// Native implementations ///
//////////////////////////////

# Parses the given header value to extract its value and parameter map.
#
# + headerValue - The header value
# + return - A tuple containing the value and its parameter map or else an `http:ClientError` if the header parsing fails
//TODO: Make the error nillable
public function parseHeader(string headerValue) returns [string, map<any>]|ClientError = @java:Method {
    class: "org.ballerinalang.net.http.nativeimpl.ParseHeader",
    name: "parseHeader"
} external;

function buildRequest(RequestMessage message) returns Request {
    Request request = new;
    if (message is ()) {
        request.noEntityBody = true;
        return request;
    } else if (message is Request) {
        request = message;
        request.noEntityBody = !request.checkEntityBodyAvailability();
    } else if (message is string) {
        request.setTextPayload(message);
    } else if (message is xml) {
        request.setXmlPayload(message);
    } else if (message is byte[]) {
        request.setBinaryPayload(message);
    } else if (message is json) {
        request.setJsonPayload(message);
    } else if (message is io:ReadableByteChannel) {
        request.setByteChannel(message);
    } else {
        request.setBodyParts(message);
    }
    return request;
}

function buildResponse(ResponseMessage message) returns Response {
    Response response = new;
    if (message is ()) {
        return response;
    } else if (message is Response) {
        response = message;
    } else if (message is string) {
        response.setTextPayload(message);
    } else if (message is xml) {
        response.setXmlPayload(message);
    } else if (message is byte[]) {
        response.setBinaryPayload(message);
    } else if (message is json) {
        response.setJsonPayload(message);
    } else if (message is io:ReadableByteChannel) {
        response.setByteChannel(message);
    } else {
        response.setBodyParts(message);
    }
    return response;
}

# The HEAD remote function implementation of the Circuit Breaker. This wraps the `head` function of the underlying
# HTTP remote function provider.

# + path - Resource path
# + outRequest - A Request struct
# + requestAction - `HttpOperation` related to the request
# + httpClient - HTTP client which uses to call the relevant functions
# + verb - HTTP verb used for submit method
# + return - The response for the request or an `http:ClientError` if failed to establish communication with the upstream server
public function invokeEndpoint (string path, Request outRequest, HttpOperation requestAction, HttpClient httpClient,
                                                                    string verb = "") returns HttpResponse|ClientError {

    if (HTTP_GET == requestAction) {
        var result = httpClient->get(path, message = outRequest);
        return result;
    } else if (HTTP_POST == requestAction) {
        var result = httpClient->post(path, outRequest);
        return result;
    } else if (HTTP_OPTIONS == requestAction) {
        var result = httpClient->options(path, message = outRequest);
        return result;
    } else if (HTTP_PUT == requestAction) {
        var result = httpClient->put(path, outRequest);
        return result;
    } else if (HTTP_DELETE == requestAction) {
        var result = httpClient->delete(path, outRequest);
        return result;
    } else if (HTTP_PATCH == requestAction) {
        var result = httpClient->patch(path, outRequest);
        return result;
    } else if (HTTP_FORWARD == requestAction) {
        var result = httpClient->forward(path, outRequest);
        return result;
    } else if (HTTP_HEAD == requestAction) {
        var result = httpClient->head(path, message = outRequest);
        return result;
    } else if (HTTP_SUBMIT == requestAction) {
        return httpClient->submit(verb, path, outRequest);
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
    } else if ("SUBMIT" == httpVerb) {
        inferredConnectorAction = HTTP_SUBMIT;
    }
    return inferredConnectorAction;
}

// Populate boolean index array by looking at the configured Http status codes to get better performance
// at runtime.
function populateErrorCodeIndex (int[] errorCode) returns boolean[] {
    boolean[] result = [];
    foreach var i in errorCode {
        result[i] = true;
    }
    return result;
}

function getError() returns UnsupportedActionError {
    return UnsupportedActionError("Unsupported connector action received.");
}

function populateRequestFields (Request originalRequest, Request newRequest)  {
    newRequest.rawPath = originalRequest.rawPath;
    newRequest.method = originalRequest.method;
    newRequest.httpVersion = originalRequest.httpVersion;
    newRequest.cacheControl = originalRequest.cacheControl;
    newRequest.userAgent = originalRequest.userAgent;
    newRequest.extraPathInfo = originalRequest.extraPathInfo;
}

function populateMultipartRequest(Request inRequest) returns Request|ClientError {
    if (isMultipartRequest(inRequest)) {
        mime:Entity[] bodyParts = check inRequest.getBodyParts();
        foreach var bodyPart in bodyParts {
            if (isNestedEntity(bodyPart)) {
                mime:Entity[]|error result = bodyPart.getBodyParts();

                if (result is error) {
                    return GenericClientError(result.message(), result);
                }

                mime:Entity[] childParts = <mime:Entity[]> result;

                foreach var childPart in childParts {
                    // When performing passthrough scenarios, message needs to be built before
                    // invoking the endpoint to create a message datasource.
                    var childBlobContent = childPart.getByteArray();
                }
                bodyPart.setBodyParts(childParts, <@untainted> bodyPart.getContentType());
            } else {
                var bodyPartBlobContent = bodyPart.getByteArray();
            }
        }
        inRequest.setBodyParts(bodyParts, <@untainted> inRequest.getContentType());
    }
    return inRequest;
}

function isMultipartRequest(Request request) returns @tainted boolean {
    return request.hasHeader(mime:CONTENT_TYPE) &&
        request.getHeader(mime:CONTENT_TYPE).startsWith(MULTIPART_AS_PRIMARY_TYPE);
}

function isNestedEntity(mime:Entity entity) returns @tainted boolean {
    return entity.hasHeader(mime:CONTENT_TYPE) &&
        entity.getHeader(mime:CONTENT_TYPE).startsWith(MULTIPART_AS_PRIMARY_TYPE);
}

function createFailoverRequest(Request request, mime:Entity requestEntity) returns Request|ClientError {
    if (isMultipartRequest(request)) {
        return populateMultipartRequest(request);
    } else {
        Request newOutRequest = new;
        populateRequestFields(request, newOutRequest);
        newOutRequest.setEntity(requestEntity);
        return newOutRequest;
    }
}

function getInvalidTypeError() returns ClientError {
    return GenericClientError("Invalid return type found for the HTTP operation");
}

function createErrorForNoPayload(mime:Error err) returns GenericClientError {
    string message = "No payload";
    return GenericClientError(message, err);
}

//Resolve a given path against a given URI.
function resolve(string baseUrl, string path) returns string|ClientError = @java:Method {
    class: "org.ballerinalang.net.uri.nativeimpl.Resolve",
    name: "resolve"
} external;
