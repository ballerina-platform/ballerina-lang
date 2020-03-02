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

import ballerinax/java;
import ballerina/mime;
import ballerina/io;

//////////////////////////////
/// Native implementations ///
//////////////////////////////

# Parses the given header value to extract its value and parameter map.
#
# + headerValue - The header value
# + return - Returns a tuple containing the value and its parameter map
//TODO: Make the error nillable
public function parseHeader(string headerValue) returns [string, map<any>]|ClientError {
    return externParseHeader(java:fromString(headerValue));
}

function externParseHeader(handle headerValue) returns [string, map<any>]|ClientError =
@java:Method {
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
        string verb = "") returns @tainted HttpResponse|ClientError {

    if (HTTP_GET == requestAction) {
        var result = httpClient->get(path, message = outRequest);
        return getResponseOrError(result);
    } else if (HTTP_POST == requestAction) {
        var result = httpClient->post(path, outRequest);
        return getResponseOrError(result);
    } else if (HTTP_OPTIONS == requestAction) {
        var result = httpClient->options(path, message = outRequest);
        return getResponseOrError(result);
    } else if (HTTP_PUT == requestAction) {
        var result = httpClient->put(path, outRequest);
        return getResponseOrError(result);
    } else if (HTTP_DELETE == requestAction) {
        var result = httpClient->delete(path, outRequest);
        return getResponseOrError(result);
    } else if (HTTP_PATCH == requestAction) {
        var result = httpClient->patch(path, outRequest);
        return getResponseOrError(result);
    } else if (HTTP_FORWARD == requestAction) {
        var result = httpClient->forward(path, outRequest);
        return getResponseOrError(result);
    } else if (HTTP_HEAD == requestAction) {
        var result = httpClient->head(path, message = outRequest);
        return getResponseOrError(result);
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
    string message = "Unsupported connector action received.";
    UnsupportedActionError unsupportedActionError = error(UNSUPPORTED_ACTION, message = message);
    return unsupportedActionError;
}

function getIllegalDataBindingStateError() returns IllegalDataBindingStateError {
    string message = "Payload cannot be retrived";
    IllegalDataBindingStateError payloadRetrievalErr = error(ILLEGAL_DATA_BINDING_STATE_ERROR, message = message);
    return payloadRetrievalErr;
}

function getResponseOrError(Response|PayloadType|ClientError result) returns HttpResponse|ClientError {
    if (result is HttpResponse|ClientError) {
        return result;
    } else {
        return getIllegalDataBindingStateError();
    }
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
                    return getGenericClientError(result.reason(), result);
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
    string message = "Invalid return type found for the HTTP operation";
    GenericClientError invalidTypeError = error(GENERIC_CLIENT_ERROR, message = message);
    return invalidTypeError;
}

function createErrorForNoPayload(mime:Error err) returns GenericClientError {
    string message = "No payload";
    return getGenericClientError(message, err);
}

//Resolve a given path against a given URI.
function resolve(string baseUrl, string path) returns string|ClientError {
    var result = externResolve(java:fromString(baseUrl), java:fromString(path));
    if (result is handle) {
        return <string>java:toString(result);
    } else {
        return result;
    }
}

function externResolve(handle baseUrl, handle path) returns handle|ClientError =
@java:Method {
    class: "org.ballerinalang.net.uri.nativeimpl.Resolve",
    name: "resolve"
} external;
