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
import ballerina/stringutils;
import ballerina/java;
import ballerina/time;

# Represents an HTTP request.
#
# + rawPath - Resource path of the request URL
# + method - The HTTP request method
# + httpVersion - The HTTP version supported by the client
# + userAgent - The user-agent. This value is used when setting the `user-agent` header
# + extraPathInfo - The part of the URL, which matched to '*' if the request is dispatched to a wildcard resource
# + cacheControl - The cache-control directives for the request. This needs to be explicitly initialized if intending
#                  on utilizing HTTP caching.
# + mutualSslHandshake - A record providing mutual ssl handshake results.
public type Request object {

    public string rawPath = "";
    public string method = "";
    public string httpVersion = "";
    public string userAgent = "";
    public string extraPathInfo = "";
    public RequestCacheControl? cacheControl = ();
    public MutualSslHandshake? mutualSslHandshake = ();

    private mime:Entity? entity = ();
    private boolean dirtyRequest;
    boolean noEntityBody;

    public function __init() {
        self.dirtyRequest = false;
        self.noEntityBody = false;
        self.entity = self.createNewEntity();
    }

    # Create a new `Entity` and link it with the request.
    #
    # + return - Newly created `Entity` that has been set to the request
    function createNewEntity() returns mime:Entity {
        return externCreateNewReqEntity(self);
    }

    # Sets the provided `Entity` to the request.
    #
    # + e - The `Entity` to be set to the request
    public function setEntity(mime:Entity e) {
        return externSetReqEntity(self, e);
    }

    # Gets the query parameters of the request as a map consisting of a string array.
    #
    # + return - String array map of the query params
    public function getQueryParams() returns map<string[]> {
        return externGetQueryParams(self);
    }

    # Gets the query param value associated with the given key.
    #
    # + key - Represents the query param key
    # + return - The query param value associated with the given key as a string. If multiple param values are
    #            present, then the first value is returned. `()` is returned if no key is found.
    public function getQueryParamValue(@untainted string key) returns @tainted string? {
        map<string[]> params = self.getQueryParams();
        var result = params[key];
        return result is () ? () : result[0];
    }

    # Gets all the query param values associated with the given key.
    #
    # + key - Represents the query param key
    # + return - All the query param values associated with the given key as a `string[]`. `()` is returned if no key
    #            is found.
    public function getQueryParamValues(@untainted string key) returns @tainted string[]? {
        map<string[]> params = self.getQueryParams();
        return params[key];
    }

    # Gets the matrix parameters of the request.
    #
    # + path - Path to the location of matrix parameters
    # + return - A map of matrix parameters which can be found for the given path
    public function getMatrixParams(string path) returns map<any> {
        return externGetMatrixParams(self, path);
    }

    # Gets the `Entity` associated with the request.
    #
    # + return - The `Entity` of the request. An `http:ClientError` is returned, if entity construction fails
    public function getEntity() returns mime:Entity|ClientError {
        return externGetReqEntity(self);
    }

    //Gets the `Entity` from the request without the body. This function is exposed only to be used internally.
    function getEntityWithoutBody() returns mime:Entity {
        return externGetReqEntityWithoutBody(self);
    }

    # Checks whether the requested header key exists in the header map.
    #
    # + headerName - The header name
    # + return - Returns true if the specified header key exists
    public function hasHeader(string headerName) returns boolean {
        mime:Entity entity = self.getEntityWithoutBody();
        return entity.hasHeader(headerName);
    }

    # Returns the value of the specified header. If the specified header key maps to multiple values, the first of
    # these values is returned.
    #
    # + headerName - The header name
    # + return - The first header value for the specified header name. Panic if the header is not found. Use the
    #            `Request.hasHeader()` beforehand to check the existence of a header.
    public function getHeader(string headerName) returns @tainted string {
        mime:Entity entity = self.getEntityWithoutBody();
        return entity.getHeader(headerName);
    }

    # Gets all the header values to which the specified header key maps to.
    #
    # + headerName - The header name
    # + return - The header values the specified header key maps to. Panic if the header is not found. Use the
    #            `Request.hasHeader()` beforehand to check the existence of a header.
    public function getHeaders(string headerName) returns @tainted string[] {
        mime:Entity entity = self.getEntityWithoutBody();
        return entity.getHeaders(headerName);
    }

    # Sets the specified header to the request. If a mapping already exists for the specified header key, the existing
    # header value is replaced with the specified header value. Panic if an illegal header is passed.
    #
    # + headerName - The header name
    # + headerValue - The header value
    public function setHeader(string headerName, string headerValue) {
        mime:Entity entity = self.getEntityWithoutBody();
        entity.setHeader(headerName, headerValue);
    }

    # Adds the specified header to the request. Existing header values are not replaced. Panic if an illegal header is passed.
    #
    # + headerName - The header name
    # + headerValue - The header value
    public function addHeader(string headerName, string headerValue) {
        mime:Entity entity = self.getEntityWithoutBody();
        entity.addHeader(headerName, headerValue);
    }

    # Removes the specified header from the request.
    #
    # + key - The header name
    public function removeHeader(string key) {
        mime:Entity entity = self.getEntityWithoutBody();
        entity.removeHeader(key);
    }

    # Removes all the headers from the request.
    public function removeAllHeaders() {
        mime:Entity entity = self.getEntityWithoutBody();
        entity.removeAllHeaders();
    }

    # Gets all the names of the headers of the request.
    #
    # + return - An array of all the header names
    public function getHeaderNames() returns @tainted string[] {
        mime:Entity entity = self.getEntityWithoutBody();
        return entity.getHeaderNames();
    }

    # Checks whether the client expects a `100-continue` response.
    #
    # + return - Returns true if the client expects a `100-continue` response
    public function expects100Continue() returns boolean {
        return <@untainted> (self.hasHeader(EXPECT) ? self.getHeader(EXPECT) == "100-continue" : false);
    }

    # Sets the `content-type` header to the request.
    #
    # + contentType - Content type value to be set as the `content-type` header
    # + return - Nil if successful, error in case of invalid content-type
    public function setContentType(string contentType) returns error? {
        mime:Entity entity = self.getEntityWithoutBody();
        var result = entity.setContentType(contentType);
        if (result is mime:Error) {
            string message = "Error occurred while setting content type header of the request";
            return GenericClientError(message, result);
        } else {
            return;
        }
    }

    # Gets the type of the payload of the request (i.e: the `content-type` header value).
    #
    # + return - The `content-type` header value as a string
    public function getContentType() returns @tainted string {
        mime:Entity entity = self.getEntityWithoutBody();
        return entity.getContentType();
    }

    # Extracts `json` payload from the request. If the content type is not JSON, an `http:ClientError` is returned.
    #
    # + return - The `json` payload or `http:ClientError` in case of errors
    public function getJsonPayload() returns @tainted json|ClientError {
        var result = self.getEntity();
        if (result is error) {
            return result;
        } else {
            var payload = result.getJson();
            if (payload is mime:Error) {
                if (payload.cause() is mime:NoContentError) {
                    return createErrorForNoPayload(payload);
                } else {
                    string message = "Error occurred while retrieving the json payload from the request";
                    return GenericClientError(message, payload);
               }
            } else {
                return payload;
            }
        }
    }

    # Extracts `xml` payload from the request. If the content type is not XML, an `http:ClientError` is returned.
    #
    # + return - The `xml` payload or `http:ClientError` in case of errors
    public function getXmlPayload() returns @tainted xml|ClientError {
        var result = self.getEntity();
        if (result is error) {
            return result;
        } else {
            var payload = result.getXml();
            if (payload is mime:Error) {
                if (payload.cause() is mime:NoContentError) {
                    return createErrorForNoPayload(payload);
                } else {
                    string message = "Error occurred while retrieving the xml payload from the request";
                    return GenericClientError(message, payload);
                }
            } else {
                return payload;
            }
        }
    }

    # Extracts `text` payload from the request. If the content type is not of type text, an `http:ClientError` is returned.
    #
    # + return - The `text` payload or `http:ClientError` in case of errors
    public function getTextPayload() returns @tainted string|ClientError {
        var result = self.getEntity();
        if (result is error) {
            return result;
        } else {
            var payload = result.getText();
            if (payload is mime:Error) {
                if (payload.cause() is mime:NoContentError) {
                    return createErrorForNoPayload(payload);
                } else {
                    string message = "Error occurred while retrieving the text payload from the request";
                    return GenericClientError(message, payload);
                }
            } else {
                return payload;
            }
        }
    }

    # Gets the request payload as a `ByteChannel` except in the case of multiparts. To retrieve multiparts, use
    # `Request.getBodyParts()`.
    #
    # + return - A byte channel from which the message payload can be read or `http:ClientError` in case of errors
    public function getByteChannel() returns @tainted io:ReadableByteChannel|ClientError {
        var result = self.getEntity();
        if (result is error) {
            return result;
        } else {
            var payload = result.getByteChannel();
            if (payload is mime:Error) {
                string message = "Error occurred while retrieving the byte channel from the request";
                return GenericClientError(message, payload);
            } else {
                return payload;
            }
        }
    }

    # Gets the request payload as a `byte[]`.
    #
    # + return - The byte[] representation of the message payload or `http:ClientError` in case of errors
    public function getBinaryPayload() returns @tainted byte[]|ClientError {
        var result = self.getEntity();
        if (result is error) {
            return result;
        } else {
            var payload = result.getByteArray();
            if (payload is mime:Error) {
                string message = "Error occurred while retrieving the binary payload from the request";
                return GenericClientError(message, payload);
            } else {
                return payload;
            }
        }
    }

    # Gets the form parameters from the HTTP request as a `map` when content type is application/x-www-form-urlencoded.
    #
    # + return - The map of form params or `http:ClientError` in case of errors
    public function getFormParams() returns @tainted map<string>|ClientError {
        var mimeEntity = self.getEntity();
        if (mimeEntity is error) {
            return mimeEntity;
        } else {
            string message = "Error occurred while retrieving form parameters from the request";
            if (!mimeEntity.hasHeader(mime:CONTENT_TYPE)) {
                string errMessage = "Content-Type header is not available";
                mime:HeaderUnavailableError typeError = mime:HeaderUnavailableError(errMessage);
                return GenericClientError(message, typeError);
            }
            string contentTypeHeaderValue = "";
            var mediaType = mime:getMediaType(mimeEntity.getHeader(mime:CONTENT_TYPE));
            if (mediaType is mime:InvalidContentTypeError) {
                return GenericClientError(message, mediaType);
            } else {
                contentTypeHeaderValue = mediaType.primaryType + "/" + mediaType.subType;
            }
            if (!(stringutils:equalsIgnoreCase(mime:APPLICATION_FORM_URLENCODED, contentTypeHeaderValue))) {
                string errorMessage = "Invalid content type : expected 'application/x-www-form-urlencoded'";
                mime:InvalidContentTypeError typeError = mime:InvalidContentTypeError(errorMessage);
                return GenericClientError(message, typeError);
            }
            var formData = mimeEntity.getText();
            map<string> parameters = {};
            if (formData is error) {
                return GenericClientError(message, formData);
            } else {
                if (formData != "") {
                    string[] entries = stringutils:split(formData, "&");
                    int entryIndex = 0;
                    while (entryIndex < entries.length()) {
                        int? index = entries[entryIndex].indexOf("=");
                        if (index is int && index != -1) {
                            string name = entries[entryIndex].substring(0, index);
                            name = name.trim();
                            int size = entries[entryIndex].length();
                            string value = entries[entryIndex].substring(index + 1, size);
                            value = value.trim();
                            if (value != "") {
                                parameters[name] = value;
                            }
                        }
                        entryIndex = entryIndex + 1;
                    }
                }
            }
            return parameters;
        }
    }

    # Extracts body parts from the request. If the content type is not a composite media type, an error
    # is returned.

    # + return - The body parts as an array of entities or else an `http:ClientError` if there were any errors
    #            constructing the body parts from the request
    public function getBodyParts() returns mime:Entity[]|ClientError {
        var result = self.getEntity();
        if (result is ClientError) {
            return result;
        } else {
            var bodyParts = result.getBodyParts();
            if (bodyParts is mime:Error) {
                string message = "Error occurred while retrieving body parts from the request";
                return GenericClientError(message, bodyParts);
            } else {
                return bodyParts;
            }
        }
    }

    # Sets a `json` as the payload.
    #
    # + payload - The `json` payload
    # + contentType - The content type of the payload. Set this to override the default `content-type` header value
    #                 for `json`
    public function setJsonPayload(json payload, public string contentType = "application/json") {
        mime:Entity entity = self.getEntityWithoutBody();
        entity.setJson(payload, contentType);
        self.setEntity(entity);
    }

    # Sets an `xml` as the payload.
    #
    # + payload - The `xml` payload
    # + contentType - The content type of the payload. Set this to override the default `content-type` header value
    #                 for `xml`
    public function setXmlPayload(xml payload, public string contentType = "application/xml") {
        mime:Entity entity = self.getEntityWithoutBody();
        entity.setXml(payload, contentType);
        self.setEntity(entity);
    }

    # Sets a `string` as the payload.
    #
    # + payload - The `string` payload
    # + contentType - The content type of the payload. Set this to override the default `content-type` header value
    #                 for `string`
    public function setTextPayload(string payload, public string contentType = "text/plain") {
        mime:Entity entity = self.getEntityWithoutBody();
        entity.setText(payload, contentType);
        self.setEntity(entity);
    }

    # Sets a `byte[]` as the payload.
    #
    # + payload - The `byte[]` payload
    # + contentType - The content type of the payload. Set this to override the default `content-type` header value
    #                 for `byte[]`
    public function setBinaryPayload(byte[] payload, public string contentType = "application/octet-stream") {
        mime:Entity entity = self.getEntityWithoutBody();
        entity.setByteArray(payload, contentType);
        self.setEntity(entity);
    }

    # Set multiparts as the payload.
    #
    # + bodyParts - The entities which make up the message body
    # + contentType - The content type of the top level message. Set this to override the default
    #                 `content-type` header value
    public function setBodyParts(mime:Entity[] bodyParts, public string contentType = "multipart/form-data") {
        mime:Entity entity = self.getEntityWithoutBody();
        entity.setBodyParts(bodyParts, contentType);
        self.setEntity(entity);
    }

    # Sets the content of the specified file as the entity body of the request.
    #
    # + filePath - Path to the file to be set as the payload
    # + contentType - The content type of the specified file. Set this to override the default `content-type`
    #                 header value
    public function setFileAsPayload(string filePath, public string contentType = "application/octet-stream") {
        mime:Entity entity = self.getEntityWithoutBody();
        entity.setFileAsEntityBody(filePath, contentType);
        self.setEntity(entity);
    }

    # Sets a `ByteChannel` as the payload.
    #
    # + payload - A `ByteChannel` through which the message payload can be read
    # + contentType - The content type of the payload. Set this to override the default `content-type`
    #                 header value
    public function setByteChannel(io:ReadableByteChannel payload, public string contentType = "application/octet-stream") {
        mime:Entity entity = self.getEntityWithoutBody();
        entity.setByteChannel(payload, contentType);
        self.setEntity(entity);
    }

    # Sets the request payload. Note that any string value is set as `text/plain`. To send a JSON-compatible string,
    # set the content-type header to `application/json` or use the `setJsonPayload` method instead.
    #
    # + payload - Payload can be of type `string`, `xml`, `json`, `byte[]`, `ByteChannel`, or `Entity[]` (i.e., a set
    # of body parts).
    public function setPayload(string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[] payload) {
        if (payload is string) {
            self.setTextPayload(payload);
        } else if (payload is xml) {
            self.setXmlPayload(payload);
        } else if (payload is byte[]) {
            self.setBinaryPayload(payload);
        } else if (payload is json) {
            self.setJsonPayload(payload);
        } else if (payload is io:ReadableByteChannel) {
            self.setByteChannel(payload);
        } else {
            self.setBodyParts(payload);
        }
    }

    // For use within the module. Takes the Cache-Control header and parses it to a RequestCacheControl object.
    function parseCacheControlHeader() {
        // If the request doesn't contain a cache-control header, resort to default cache control settings
        if (!self.hasHeader(CACHE_CONTROL)) {
            return;
        }

        RequestCacheControl reqCC = new;
        string cacheControl = self.getHeader(CACHE_CONTROL);
        string[] directives = stringutils:split(cacheControl, ",");

        foreach var dir in directives {
            var directive = dir.trim();
            if (directive == NO_CACHE) {
                reqCC.noCache = true;
            } else if (directive == NO_STORE) {
                reqCC.noStore = true;
            } else if (directive == NO_TRANSFORM) {
                reqCC.noTransform = true;
            } else if (directive == ONLY_IF_CACHED) {
                reqCC.onlyIfCached = true;
            } else if (directive.startsWith(MAX_AGE)) {
                reqCC.maxAge = getExpirationDirectiveValue(directive);
            } else if (directive == MAX_STALE) {
                reqCC.maxStale = MAX_STALE_ANY_AGE;
            } else if (directive.startsWith(MAX_STALE)) {
                reqCC.maxStale = getExpirationDirectiveValue(directive);
            } else if (directive.startsWith(MIN_FRESH)) {
                reqCC.minFresh = getExpirationDirectiveValue(directive);
            }
            // non-standard directives are ignored
        }

        self.cacheControl = reqCC;
    }

    # Check whether the entity body is present.
    #
    # + return - A boolean indicating the availability of the entity body
    function checkEntityBodyAvailability() returns boolean {
        return externCheckReqEntityBodyAvailability(self);
    }

    # Adds cookies to the request.
    #
    # + cookiesToAdd - Represents the cookies to be added
    public function addCookies(Cookie[] cookiesToAdd) {
        string cookieheader = "";
        Cookie[] sortedCookies = cookiesToAdd.sort(comparator);
        foreach var cookie in sortedCookies {
            var cookieName = cookie.name;
            var cookieValue = cookie.value;
            if (cookieName is string && cookieValue is string) {
                cookieheader = cookieheader + cookieName + EQUALS + cookieValue + SEMICOLON + SPACE;
            }
            cookie.lastAccessedTime = time:currentTime();
        }
        if (cookieheader != "") {
            cookieheader = cookieheader.substring(0, cookieheader.length() - 2);
            if (self.hasHeader("Cookie")) {
                self.setHeader("Cookie", cookieheader);
            } else {
                self.addHeader("Cookie", cookieheader);
            }
        }
    }

    # Gets cookies from the request.
    #
    # + return - An array of cookie objects, which are included in the request
    public function getCookies() returns Cookie[] {
        string cookiesStringValue = "";
        Cookie[] cookiesInRequest = [];
        if (self.hasHeader("Cookie")) {
            cookiesInRequest = parseCookieHeader(self.getHeader("Cookie"));
        }
        return cookiesInRequest;
    }
};

function externCreateNewReqEntity(Request request) returns mime:Entity =
@java:Method {
    class: "org.ballerinalang.net.http.nativeimpl.ExternRequest",
    name: "createNewEntity"
} external;

function externSetReqEntity(Request request, mime:Entity entity) =
@java:Method {
    class: "org.ballerinalang.net.http.nativeimpl.ExternRequest",
    name: "setEntity"
} external;

function externGetQueryParams(Request request) returns map<string[]> =
@java:Method {
    class: "org.ballerinalang.net.http.nativeimpl.ExternRequest",
    name: "getQueryParams"
} external;

function externGetMatrixParams(Request request, string path) returns map<any> =
@java:Method {
    class: "org.ballerinalang.net.http.nativeimpl.ExternRequest",
    name: "getMatrixParams"
} external;

function externGetReqEntity(Request request) returns mime:Entity|ClientError =
@java:Method {
    class: "org.ballerinalang.net.http.nativeimpl.ExternRequest",
    name: "getEntity"
} external;

function externGetReqEntityWithoutBody(Request request) returns mime:Entity =
@java:Method {
    class: "org.ballerinalang.net.http.nativeimpl.ExternRequest",
    name: "getEntityWithoutBody"
} external;

function externCheckReqEntityBodyAvailability(Request request) returns boolean =
@java:Method {
    class: "org.ballerinalang.net.http.nativeimpl.ExternRequest",
    name: "checkEntityBodyAvailability"
} external;

# A record for providing mutual SSL handshake results.
#
# + status - Status of the handshake.
# + base64EncodedCert - Base64 encoded certificate.
public type MutualSslHandshake record {|
    MutualSslStatus status = ();
    string? base64EncodedCert = ();
|};

# Defines the possible values for the mutual ssl status.
#
# `passed`: Mutual SSL handshake is successful.
# `failed`: Mutual SSL handshake has failed.
public type MutualSslStatus PASSED | FAILED | ();

# Mutual SSL handshake is successful.
public const PASSED = "passed";

# Mutual SSL handshake has failed.
public const FAILED = "failed";

# Not a mutual ssl connection.
public const NONE = ();
